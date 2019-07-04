package net.giafei.cloud.db.transaction2pc.core.datasource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
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
 * Date 2019/7/3 20:59
 */

public class TransactionThreadDataContainer {
    private Map<String, TransactionThreadData> dataMap = new ConcurrentHashMap<>();

    public String moveThreadData() {
        String id = UUID.randomUUID().toString();
        while (dataMap.containsKey(id)) {
            id = UUID.randomUUID().toString();
        }

        return doMoveThreadData(id);
    }

    public String moveThreadData(String id) {
        if (dataMap.containsKey(id))
            throw new RuntimeException("key已存在");

        return doMoveThreadData(id);
    }

    private String doMoveThreadData(String id) {

        TransactionThreadData data = new TransactionThreadData();
        data.resources = new HashMap<>(TransactionSynchronizationManager.getResourceMap());
        data.synchronizations = TransactionSynchronizationManager.getSynchronizations();
        data.currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        data.currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        data.currentTransactionIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
        data.actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();

        for (Object key : data.resources.keySet()) {
            TransactionSynchronizationManager.unbindResource(key);
        }

        TransactionSynchronizationManager.clear();

        dataMap.put(id, data);

        return id;
    }

    public boolean restoreThreadData(String id) {
        TransactionThreadData data = dataMap.get(id);
        dataMap.remove(id);

        if (data == null)
            return false;

        for (Map.Entry<Object, Object> entry : data.resources.entrySet()) {
            TransactionSynchronizationManager.bindResource(entry.getKey(), entry.getValue());
        }

        TransactionSynchronizationManager.initSynchronization();
        for (TransactionSynchronization synchronization : data.synchronizations) {
            TransactionSynchronizationManager.registerSynchronization(synchronization);
        }

        TransactionSynchronizationManager.setCurrentTransactionName(data.currentTransactionName);
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(data.currentTransactionReadOnly);
        TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(data.currentTransactionIsolationLevel);
        TransactionSynchronizationManager.setActualTransactionActive(data.actualTransactionActive);

        return true;
    }

    private class TransactionThreadData {
        private Map<Object, Object> resources;
        private List<TransactionSynchronization> synchronizations;
        private String currentTransactionName;
        private Boolean currentTransactionReadOnly;
        private Integer currentTransactionIsolationLevel;
        private Boolean actualTransactionActive;
    }
}
