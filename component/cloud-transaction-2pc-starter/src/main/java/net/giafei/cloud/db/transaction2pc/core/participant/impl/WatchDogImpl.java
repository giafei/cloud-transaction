package net.giafei.cloud.db.transaction2pc.core.participant.impl;

import net.giafei.cloud.db.transaction2pc.core.participant.I2PcCommunicator;
import net.giafei.cloud.db.transaction2pc.core.participant.IWatchDog;
import net.giafei.cloud.db.transaction2pc.core.participant.impl.internal.CloudTransactionIdFeignInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionTimedOutException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
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
 * Date 2019/7/3 20:05
 */

@Component
public class WatchDogImpl implements IWatchDog {
    @Value("${net.giafei.transaction.max-wait-time}")
    private Long maxWaitTime;

    @Autowired
    private I2PcCommunicator communicator;

    private Logger logger = LoggerFactory.getLogger(IWatchDog.class);

    private static final String REQUEST_ATTRIBUTE_STACK_BOTTOM = "X-STACK-BOTTOM";

    @Override
    public String createCloudTransaction() {
        if (maxWaitTime == null) {
            maxWaitTime = 10L * 1000;
        }

        String transactionId = UUID.randomUUID().toString();
        CloudTransactionIdFeignInterceptor.broadcastCloudTransactionId(transactionId);
        CloudTransactionIdFeignInterceptor.getCurrentRequest().setAttribute(REQUEST_ATTRIBUTE_STACK_BOTTOM, Boolean.TRUE);

        long maxWaitAt = System.currentTimeMillis() + maxWaitTime;

        while (System.currentTimeMillis() < maxWaitAt) {
            try {
                communicator.setDataTimeout(transactionId, System.currentTimeMillis() + maxWaitTime * 2);
                communicator.setState(transactionId, 1);
                return transactionId;
            } catch (Exception e) {
                logger.error("cloud transaction 设置状态失败", e);

                //暂停10MS
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    //
                }
            }
        }

        throw new TransactionTimedOutException("设置状态超时");
    }

    @Override
    public void checkVote(boolean commit) {
        HttpServletRequest request = CloudTransactionIdFeignInterceptor.getCurrentRequest();
        if (request.getAttribute(REQUEST_ATTRIBUTE_STACK_BOTTOM) == null)
            return;

        try {
            String transactionId = CloudTransactionIdFeignInterceptor.getCloudTransactionId();
            if (commit) {
                Map<String, String> voteResult = communicator.getVoteResult(transactionId);
                logger.debug("检查事务{}投票，共有{}个参与者", transactionId, voteResult.size());

                for (Map.Entry<String, String> entry : voteResult.entrySet()) {
                    String value = entry.getValue();
                    if ("ROLLBACK".equals(value)) {
                        commit = false;
                        break;
                    }
                }
            }

            Long state = communicator.getState(transactionId);
            if (state == null) {
                state = 1L;
            }

            if (state == 1) {
                String result = commit ? "COMMIT" : "ROLLBACK";
                logger.debug("发送事务指令:" + result);
                communicator.setResult(transactionId, result);
                communicator.setState(transactionId, 2);
                communicator.noticeResult(transactionId, result);
            } else {
                logger.debug("事务异常，发送回滚指令");
                communicator.setState(transactionId, 0);
                communicator.setResult(transactionId, "ROLLBACK");
                communicator.noticeResult(transactionId, "ROLLBACK");
            }

        } catch (Exception e) {
            //各事务将在超时后回滚
            logger.error("事务协调失败，各事务将在", e);
        }
    }
}
