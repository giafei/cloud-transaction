package net.giafei.demo.cloud.order.biz.impl;

import net.giafei.cloud.BeanConvert;
import net.giafei.cloud.BizException;
import net.giafei.demo.cloud.order.biz.IOrderService;
import net.giafei.demo.cloud.order.dao.IOrderMapper;
import net.giafei.demo.cloud.order.dao.entity.OrderEntity;
import net.giafei.demo.cloud.order.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
 * Date 2019/7/2 16:25
 */

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private IOrderMapper mapper;

    @Override
    @Transactional
    public int addOrder(OrderDTO dto) {
        return mapper.save(BeanConvert.convert(OrderEntity.class, dto));
    }

    @Override
    @Transactional
    public void addOrderFail(OrderDTO dto) {
        mapper.save(BeanConvert.convert(OrderEntity.class, dto));
        throw new BizException("0", "1");
    }

    @Override
    @Transactional
    public void addOrderTimeout(OrderDTO dto) {
        mapper.save(BeanConvert.convert(OrderEntity.class, dto));

        try {
            Thread.sleep(10 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getLastOrderId() {
        return mapper.getLastId();
    }
}
