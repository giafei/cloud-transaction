package net.giafei.cloud.db.transaction2pc.core.participant.impl;

import net.giafei.cloud.db.transaction2pc.core.participant.I2PcCommunicator;
import net.giafei.cloud.db.transaction2pc.core.participant.IParticipant;
import net.giafei.cloud.db.transaction2pc.core.participant.IWatchDog;
import net.giafei.cloud.db.transaction2pc.core.participant.impl.internal.CloudTransactionIdFeignInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;

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
 * Date 2019/7/4 6:37
 */

@Component
public class ParticipantImpl implements IParticipant {
    @Autowired
    private IWatchDog watchDog;

    @Autowired
    private I2PcCommunicator communicator;

    @Value("${net.giafei.transaction.max-wait-time}")
    private Long maxWaitTime;

    private String participantId = UUID.randomUUID().toString();

    private Logger logger = LoggerFactory.getLogger(IParticipant.class);
    private Set<BiConsumer<String, Boolean>> consumers = new HashSet<>();
    private Set<String> transactions = new ConcurrentSkipListSet<>();

    private static final String REQUEST_ATTRIBUTE_TRANSACTION_START_TIME = "X-TRANSACTION-START-TIME";

    @Override
    public String getCloudTransactionId() {
        String transactionId = CloudTransactionIdFeignInterceptor.getCloudTransactionId();
        if (StringUtils.isEmpty(transactionId)) {
            transactionId = watchDog.createCloudTransaction();
            logger.debug("开始事务{}，本服务作为watchdog", transactionId);
        }

        HttpServletRequest request = CloudTransactionIdFeignInterceptor.getCurrentRequest();
        if (request.getAttribute(REQUEST_ATTRIBUTE_TRANSACTION_START_TIME) == null) {
            request.setAttribute(REQUEST_ATTRIBUTE_TRANSACTION_START_TIME, System.currentTimeMillis());

            logger.debug("加入事务{}", transactionId);
        }

        if (!transactions.contains(transactionId)) {
            transactions.add(transactionId);

            logger.debug("注册监听 " + transactionId);
            communicator.addNoticeListener(transactionId, this::onCloudTransactionNotice);
        }

        return transactionId;
    }

    @Override
    public void sendVote(boolean commit) {
        String transactionId = getCloudTransactionId();
        long maxWaitAt = System.currentTimeMillis() + maxWaitTime;

        //超时设置
        Mono.delay(Duration.ofMillis(maxWaitTime))
                .map(t -> onTransactionTimeout(transactionId))
                .publishOn(Schedulers.parallel())
                .subscribe();

        while (System.currentTimeMillis() < maxWaitAt) {
            try {
                logger.debug("{} 尝试对事务 {} 投票，commit:{}", participantId, transactionId, commit);
                communicator.setVoteResult(transactionId, participantId, commit ? "COMMIT" : "ROLLBACK");
                watchDog.checkVote(commit);
                return;
            } catch (Exception e) {
                logger.error("cloud transaction 投票失败", e);

                //暂停10MS
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    //
                }
            }
        }

    }

    @Override
    public void addEndListener(BiConsumer<String, Boolean> endConsumer) {
        consumers.add(endConsumer);
    }

    //广播通知
    private void onCloudTransactionNotice(String txId, String result) {
        logger.debug("事务{}收到来自协调者的结果通知{}", txId, result);
        broadcastTransactionEnd(txId, "COMMIT".equals(result));
    }

    /**
     * 事务等待超时函数， 返回 COMMIT or ROLLBACK
     */
    private String onTransactionTimeout(String transactionId) {
        try {
            if (!transactions.contains(transactionId))
                return "DONE";

            Map<String, String> ackResult = communicator.getAckResult(transactionId);
            if (ackResult.containsKey(participantId))
                return ackResult.get(participantId);

            logger.info("cloud transaction 超时");
            Long state = communicator.getState(transactionId);
            if (state == null || (state != 2)) {
                //超时 且状态异常
                logger.info("cloud transaction 超时 状态异常 执行回滚");

                //替换投票
                communicator.setVoteResult(transactionId, participantId, "ROLLBACK");
                broadcastTransactionEnd(transactionId, false);
                return "ROLLBACK";
            } else {
                logger.info("cloud transaction 超时 但读取的到投票结果");

                String result = communicator.getResult(transactionId);
                broadcastTransactionEnd(transactionId, "COMMIT".equals(result));

                return result;
            }
        } catch (Exception e) {
            logger.error("cloud transaction 超时执行失败，数据回滚", e);
            broadcastTransactionEnd(transactionId, false);

            return "ROLLBACK";
        }
    }

    private void broadcastTransactionEnd(String txId, boolean commit) {
        logger.debug("执行事务 {} 结果 {}", txId, commit ? "COMMIT" : "ROLLBACK");

        Boolean v = Boolean.valueOf(commit);
        HashSet<BiConsumer<String, Boolean>> tmp = new HashSet<>(consumers);

        transactions.remove(txId);
        communicator.removeNoticeListener(txId);

        //换一个线程执行
        Schedulers.parallel().schedule(() -> {
            for (BiConsumer<String, Boolean> consumer : tmp) {
                consumer.accept(txId, v);
            }
        });

        try {
            communicator.setAckResult(txId, participantId, commit ? "COMMIT" : "ROLLBACK");
        } catch (Exception e) {
            logger.error("写入ack失败", e);
        }
    }
}
