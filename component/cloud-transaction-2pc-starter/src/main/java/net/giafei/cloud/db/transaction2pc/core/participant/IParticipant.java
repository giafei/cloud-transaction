package net.giafei.cloud.db.transaction2pc.core.participant;

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
 * Date 2019/7/4 6:36
 */
public interface IParticipant {
    /**
     * 返回当前的分布式事务的ID
     * @return 分布式事务的ID
     */
    String getCloudTransactionId();

    /**
     * 发送投票
     * @param commit 是commit还是rollback
     */
    void sendVote(boolean commit);

    /**
     * 添加分布式事务监听
     * @param endConsumer 监听，参数String为事务ID，另一个参数 true表示提交，false表示回滚
     */
    void addEndListener(BiConsumer<String, Boolean> endConsumer);
}
