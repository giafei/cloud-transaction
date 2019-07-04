package net.giafei.cloud.db.transaction2pc.core.datasource;

import net.giafei.cloud.db.transaction2pc.core.participant.IParticipant;
import net.giafei.cloud.db.transaction2pc.core.participant.IWatchDog;
import net.giafei.cloud.db.transaction2pc.core.participant.impl.internal.CloudTransactionIdFeignInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.*;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ////////////////////////////////////////////////////////////////////
 * //                          _ooOoo_
 * //                         o8888888o
 * //                         88" . "88
 * //                         (| ^_^ |)
 * //                         O\  =  /O
 * //                      ____/`---'\____
 * //                    .'  \\|     |//  `.
 * //                   /  \\|||  :  |||//  \
 * //                  /  _||||| -:- |||||-  \
 * //                  |   | \\\  -  /// |   |
 * //                  | \_|  ''\---/''  |   |
 * //                  \  .-\__  `-`  ___/-. /
 * //                ___`. .'  /--.--\  `. . ___
 * //              ."" '<  `.___\_<|>_/___.'  >'"".
 * //            | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 * //            \  \ `-.   \_ __\ /__ _/   .-` /  /
 * //      ========`-.____`-.___\_____/___.-`____.-'========
 * //                           `=---='
 * //      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //         佛祖保佑       永无BUG     永不修改
 * ////////////////////////////////////////////////////////////////////
 *
 * @author xjf
 * @version 1.0
 * Date 2019/7/2 20:12
 */
public class WrappedDataSourceTransactionManager implements PlatformTransactionManager {
    private IParticipant participant;
    private WrappedTransactionManager wrapped;

    private TransactionThreadDataContainer container = new TransactionThreadDataContainer();
    private Map<String, CloudTransactionData> transactionData = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(WrappedDataSourceTransactionManager.class);

    public WrappedDataSourceTransactionManager(DataSource dataSource, IParticipant participant) {
        wrapped = new WrappedTransactionManager(dataSource);
        this.participant = participant;
        participant.addEndListener(this::commitTransaction);
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        checkIfTransactionTimeout();

        boolean serviceBottom = !wrapped.isExistingTransaction();
        boolean stackBottom = !returnTransactionThreadData();

        TransactionStatus transaction = wrapped.getTransaction(definition);
        if (transaction instanceof WrappedTransactionStatus) {
            WrappedTransactionStatus status = (WrappedTransactionStatus) transaction;
            status.serviceBottom = serviceBottom;
            status.stackBottom = stackBottom;

            if (stackBottom) {
                transactionData.computeIfAbsent(participant.getCloudTransactionId(), v -> {
                    CloudTransactionData data = new CloudTransactionData();

                    data.status = transaction;
                    data.threadDataId = null;

                    return data;
                });
            }
        }

        return transaction;
    }

    private void checkIfTransactionTimeout() {
        String transactionId = CloudTransactionIdFeignInterceptor.getCloudTransactionId();
        if (StringUtils.isEmpty(transactionId))
            return;

        CloudTransactionData data = transactionData.get(transactionId);
        if (data == null)
            return;

        if (data.timeout)
            throw new TransactionTimedOutException("cloud transaction 超时");
    }

    public CloudTransactionData stealTransactionThreadData() {
        String transactionId = CloudTransactionIdFeignInterceptor.getCloudTransactionId();
        if (StringUtils.isEmpty(transactionId))
            return null;

        logger.debug("事务 {} 转移线程数据", transactionId);

        CloudTransactionData data = transactionData.get(transactionId);
        if (data == null) {
            logger.error("转移事务线程数据失败， 相关数据未初始化");
            return null;
        }

        data.threadDataId = container.moveThreadData();
        return data;
    }

    public boolean returnTransactionThreadData() {
        String transactionId = CloudTransactionIdFeignInterceptor.getCloudTransactionId();
        if (StringUtils.isEmpty(transactionId))
            return false;

        logger.debug("事务{}归还线程数据", transactionId);
        CloudTransactionData data = transactionData.get(transactionId);
        if (data == null) {
            logger.debug("事务{}归还线程数据失败，数据为空", transactionId);
            return false;
        }

        if (data.threadDataId != null) {
            container.restoreThreadData(data.threadDataId);
            data.threadDataId = null;
            return true;
        } else {
            return false;
        }
    }

    private void commitTransaction(String transactionId, Boolean isCommit) {
        CloudTransactionData data = transactionData.get(transactionId);
        if (data.free) {
            transactionData.remove(transactionId);

            container.restoreThreadData(data.threadDataId);
            if (isCommit) {
                wrapped.commit(data.status);
            } else {
                wrapped.rollback(data.status);
            }
        } else {
            //只有超时才会到这里
            //来这个函数有两条路
            //    从协调者通知过来， free肯定为true
            //    超时
            logger.debug("事务已经超时但业务还在运行中");
            data.timeout = true;
        }
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        if (isCloudTransaction(status)) {
            CloudTransactionData data = stealTransactionThreadData();

            //已经超时了
            if (data.timeout) {
                logger.debug("业务成功运行，但事务已超时");

                WrappedTransactionStatus s = (WrappedTransactionStatus)status;
                if (!s.stackBottom) {
                    wrapped.rollback(status);
                } else {
                    data.free = true;
                    //提交
                    commitTransaction(participant.getCloudTransactionId(), false);
                }
            } else {
                participant.sendVote(true);

                WrappedTransactionStatus s = (WrappedTransactionStatus)status;
                if (!s.stackBottom) {
                    wrapped.commit(status);
                } else {
                    data.free = true;
                }
            }
        } else {
            wrapped.commit(status);
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        if (isCloudTransaction(status)) {
            CloudTransactionData data = stealTransactionThreadData();
            participant.sendVote(false);

            WrappedTransactionStatus s = (WrappedTransactionStatus)status;
            if (!s.stackBottom) {
                wrapped.rollback(status);
            } else {
                data.free = true;
            }
        } else {
            wrapped.rollback(status);
        }
    }

    private boolean isCloudTransaction(TransactionStatus status) {
        if (status instanceof WrappedTransactionStatus) {
            WrappedTransactionStatus v = (WrappedTransactionStatus)status;
            return v.serviceBottom;
        }

        return false;
    }

    private DefaultTransactionStatus reuseTransaction() {
        String txId = participant.getCloudTransactionId();
        if (StringUtils.isEmpty(txId))
            return null;

        CloudTransactionData data = transactionData.get(txId);
        if (data != null) {
            container.restoreThreadData(data.threadDataId);
            data.threadDataId = null;

            return (DefaultTransactionStatus)data.status;
        }

        return null;
    }

    private class WrappedTransactionManager extends DataSourceTransactionManager {
        public WrappedTransactionManager() {
        }

        public WrappedTransactionManager(DataSource dataSource) {
            super(dataSource);
        }

        private boolean isExistingTransaction() {
            return super.isExistingTransaction(super.doGetTransaction());
        }

        @Override
        protected DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition, Object transaction, boolean newTransaction, boolean newSynchronization, boolean debug, Object suspendedResources) {
            boolean actualNewSynchronization = newSynchronization &&
                    !TransactionSynchronizationManager.isSynchronizationActive();

            return new WrappedTransactionStatus(
                    transaction, newTransaction, actualNewSynchronization,
                    definition.isReadOnly(), debug, suspendedResources);
        }
    }

    private class WrappedTransactionStatus extends DefaultTransactionStatus {
        //处在微服务的调用栈的最底部的事务
        private boolean stackBottom;

        //一次业务调用栈的最底部的事务
        private boolean serviceBottom;

        public WrappedTransactionStatus(Object transaction, boolean newTransaction, boolean newSynchronization, boolean readOnly, boolean debug, Object suspendedResources) {
            super(transaction, newTransaction, newSynchronization, readOnly, debug, suspendedResources);
        }
    }

    private class CloudTransactionData {
        //最底部 最开始的事务对象
        private TransactionStatus status;

        //线程数据存储的ID
        private String threadDataId;

        //事务是否已超时
        private boolean timeout;

        //事务是否未使用，只有最开始的事务提交或回滚 才为true
        //也表示 事务是否在业务中使用
        private boolean free;
    }
}
