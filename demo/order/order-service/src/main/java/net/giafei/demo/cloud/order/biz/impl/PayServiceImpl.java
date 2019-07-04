package net.giafei.demo.cloud.order.biz.impl;

import net.giafei.cloud.BeanConvert;
import net.giafei.demo.cloud.order.biz.IOrderService;
import net.giafei.demo.cloud.order.biz.IPayService;
import net.giafei.demo.cloud.order.dto.OrderDTO;
import net.giafei.demo.cloud.order.dto.PayDTO;
import net.giafei.demo.cloud.storage.api.ISoldItemClient;
import net.giafei.demo.cloud.storage.dto.SoldItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.beancontext.BeanContext;

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
 * Date 2019/7/2 16:43
 */

@Service
public class PayServiceImpl implements IPayService {
    @Autowired
    private IOrderService orderService;

    @Autowired
    private ISoldItemClient client;

    @Override
    @Transactional
    public void pay(PayDTO payDTO) {
        OrderDTO dto = BeanConvert.convert(OrderDTO.class, payDTO);

        orderService.addOrder(dto);

        SoldItemDTO item = new SoldItemDTO();
        item.setItemId(payDTO.getSpuId());
        item.setNumber(1);

        client.save(item);
    }

    @Override
    @Transactional
    public void payFail(PayDTO payDTO) {
        OrderDTO dto = BeanConvert.convert(OrderDTO.class, payDTO);

        orderService.addOrderFail(dto);

        SoldItemDTO item = new SoldItemDTO();
        item.setItemId(payDTO.getSpuId());
        item.setNumber(1);

        client.save(item);
    }

    @Override
    @Transactional
    public void payTimeout(PayDTO payDTO) {
        OrderDTO dto = BeanConvert.convert(OrderDTO.class, payDTO);

        orderService.addOrderTimeout(dto);

        SoldItemDTO item = new SoldItemDTO();
        item.setItemId(payDTO.getSpuId());
        item.setNumber(1);

        client.save(item);
    }

    @Override
    @Transactional
    public void payStorageFail(PayDTO payDTO) {
        OrderDTO dto = BeanConvert.convert(OrderDTO.class, payDTO);

        orderService.addOrder(dto);

        SoldItemDTO item = new SoldItemDTO();
        item.setItemId(payDTO.getSpuId());
        item.setNumber(1);

        client.saveFail(item);
    }

    @Override
    @Transactional
    public void payStorageTimeout(PayDTO payDTO) {
        OrderDTO dto = BeanConvert.convert(OrderDTO.class, payDTO);

        orderService.addOrder(dto);

        SoldItemDTO item = new SoldItemDTO();
        item.setItemId(payDTO.getSpuId());
        item.setNumber(1);

        client.saveTimeout(item);
    }
}
