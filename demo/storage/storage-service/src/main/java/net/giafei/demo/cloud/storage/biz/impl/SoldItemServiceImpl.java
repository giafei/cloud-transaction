package net.giafei.demo.cloud.storage.biz.impl;

import net.giafei.cloud.BeanConvert;
import net.giafei.cloud.BizException;
import net.giafei.demo.cloud.order.api.IOrderDataClient;
import net.giafei.demo.cloud.storage.biz.ISoldItemService;
import net.giafei.demo.cloud.storage.dao.ISoldItemMapper;
import net.giafei.demo.cloud.storage.dao.entity.SoldItemEntity;
import net.giafei.demo.cloud.storage.dto.SoldItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * Date 2019/7/4 10:36
 */

@Service
public class SoldItemServiceImpl implements ISoldItemService {
    @Autowired
    private ISoldItemMapper mapper;

    @Autowired
    private IOrderDataClient client;

    @Override
    @Transactional
    public int addItem(SoldItemDTO dto) {
        SoldItemEntity item = BeanConvert.convert(SoldItemEntity.class, dto);
        mapper.save(item);
        client.assignOrderData(item.getId());

        return item.getId();
    }

    @Override
    @Transactional
    public void addItemFail(SoldItemDTO dto) {
        SoldItemEntity item = BeanConvert.convert(SoldItemEntity.class, dto);
        mapper.save(item);
        client.assignOrderData(item.getId());
        throw new BizException("1", "异常");
    }

    @Override
    @Transactional
    public void addItemTimeout(SoldItemDTO dto) {
        SoldItemEntity item = BeanConvert.convert(SoldItemEntity.class, dto);
        mapper.save(item);
        client.assignOrderData(item.getId());

        try {
            Thread.sleep(10 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
