package net.giafei.cloud.db.transaction2pc.core.participant;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
 * Date 2019/7/3 19:24
 */
public interface I2PcCommunicator {
    Long getState(String txId);
    void setState(String txId, long state);

    String getResult(String txId);
    void setResult(String txId, String result);

    Map<String, String> getVoteResult(String txId);
    void setVoteResult(String txId, String participant, String vote);

    Map<String, String> getAckResult(String txId);
    void setAckResult(String txId, String participant, String ack);

    void addNoticeListener(String txId, BiConsumer<String, String> listener);
    void removeNoticeListener(String txId);

    void noticeResult(String txId, String result);
    void setDataTimeout(String txId, long timeoutAt);
}
