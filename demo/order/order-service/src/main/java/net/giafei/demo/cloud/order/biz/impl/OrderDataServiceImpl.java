package net.giafei.demo.cloud.order.biz.impl;

import net.giafei.demo.cloud.order.biz.IOrderDataService;
import net.giafei.demo.cloud.order.biz.IOrderService;
import net.giafei.demo.cloud.order.dao.IOrderDataMapper;
import net.giafei.demo.cloud.order.dao.entity.OrderDataEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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
 * Date 2019/7/4 11:11
 */

@Service
public class OrderDataServiceImpl implements IOrderDataService {
    @Autowired
    private IOrderService orderService;

    @Autowired
    private IOrderDataMapper mapper;

    @Override
    @Transactional
    public void save(Integer soldItemId) {
        //订单ID应当在业务参数中  此处为了演示共用事务
        Integer id = orderService.getLastOrderId();

        OrderDataEntity entity = new OrderDataEntity();
        entity.setOrderHeadId(id);
        entity.setSolidItemId(soldItemId);
        entity.setCreateTime(new Date());

        mapper.save(entity);
    }
}
