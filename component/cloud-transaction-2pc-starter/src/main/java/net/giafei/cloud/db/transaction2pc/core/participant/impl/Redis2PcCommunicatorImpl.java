package net.giafei.cloud.db.transaction2pc.core.participant.impl;

import net.giafei.cloud.db.transaction2pc.core.participant.I2PcCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
 * Date 2019/7/4 8:22
 */

@Component
public class Redis2PcCommunicatorImpl implements I2PcCommunicator, MessageListener {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisMessageListenerContainer container;

    private Logger logger = LoggerFactory.getLogger(Redis2PcCommunicatorImpl.class);

    private ConcurrentHashMap<String, List<BiConsumer<String, String>>> noticeListeners = new ConcurrentHashMap<>();

    @PostConstruct
    private void onReady() {
        //加速redis初始化
        container.addMessageListener(this, new PatternTopic("cloud-transaction/*/notice"));
    }

    private String stateKey(String txId) {
        return "cloud-transaction/" + txId + "/state";
    }

    private String resultKey(String txId) {
        return "cloud-transaction/" + txId + "/result";
    }

    private String voteKey(String txId) {
        return "cloud-transaction/" + txId + "/vote";
    }

    private String ackKey(String txId) {
        return "cloud-transaction/" + txId + "/ack";
    }

    private String noticeKey(String txId) {
        return "cloud-transaction/" + txId + "/notice";
    }

    private String testKey(String txId) {
        return "cloud-transaction/" + txId + "/test";
    }

    private boolean transactionExists(String txId) {
        return (getState(txId) != null);
    }

    @Override
    public Long getState(String txId) {
        String s = redisTemplate.opsForValue().get(stateKey(txId));
        if (s == null)
            return null;

        try {
            return Long.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setState(String txId, long state) {
        redisTemplate.opsForValue().set(stateKey(txId), Long.toString(state));
    }

    @Override
    public String getResult(String txId) {
        return redisTemplate.opsForValue().get(resultKey(txId));
    }

    @Override
    public void setResult(String txId, String result) {
        if (transactionExists(txId))
            redisTemplate.opsForValue().set(resultKey(txId), result);
    }

    @Override
    public Map<String, String> getVoteResult(String txId) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(voteKey(txId));
        if (map == null)
            return Collections.emptyMap();

        return map.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(), entry -> entry.getValue().toString()
        ));
    }

    @Override
    public void setVoteResult(String txId, String participant, String vote) {
        if (transactionExists(txId))
            redisTemplate.opsForHash().put(voteKey(txId), participant, vote);
    }

    @Override
    public Map<String, String> getAckResult(String txId) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(ackKey(txId));
        if (map == null)
            return Collections.emptyMap();

        return map.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(), entry -> entry.getValue().toString()
        ));
    }

    @Override
    public void setAckResult(String txId, String participant, String ack) {
        if (transactionExists(txId))
            redisTemplate.opsForHash().put(ackKey(txId), participant, ack);
    }

    @Override
    public void addNoticeListener(String txId, BiConsumer<String, String> listener) {
        List<BiConsumer<String, String>> consumers =
                noticeListeners.computeIfAbsent(txId, v -> new LinkedList<>());

        consumers.add(listener);
    }

    @Override
    public void removeNoticeListener(String txId) {
        noticeListeners.remove(txId);
    }

    @Override
    public void noticeResult(String txId, String result) {
        String key = noticeKey(txId);

        logger.debug("向Redis {} 发送 事务结果 {}", key, result);
        redisTemplate.convertAndSend(key, result);
    }

    @Override
    public void setDataTimeout(String txId, long timeoutAt) {
        //state提前半秒过期， state过期后其他值不再能写
        redisTemplate.expireAt(stateKey(txId), new Date(timeoutAt - 500));

        Date v = new Date(timeoutAt);
        redisTemplate.expireAt(resultKey(txId), v);
        redisTemplate.expireAt(voteKey(txId), v);
        redisTemplate.expireAt(ackKey(txId), v);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = new String(message.getChannel());
        String body = new String(message.getBody());

        if (!key.endsWith("/notice"))
            return;

        String txId = key.split("/")[1];
        List<BiConsumer<String, String>> consumers = this.noticeListeners.get(txId);
        if (consumers == null)
            return;

        logger.debug("从Redis收到 {} 的事务结果 {}", txId, body);

        //换一个线程 不要阻塞消息
        Schedulers.parallel().schedule(() -> {
            for (BiConsumer<String, String> consumer : consumers) {
                consumer.accept(txId, body);
            }
        });
    }
}
