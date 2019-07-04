package net.giafei.demo.cloud.order.controller;

import io.swagger.annotations.ApiOperation;
import net.giafei.cloud.BeanConvert;
import net.giafei.cloud.db.transaction2pc.core.datasource.WrappedDataSourceTransactionManager;
import net.giafei.demo.cloud.order.biz.IOrderService;
import net.giafei.demo.cloud.order.biz.IPayService;
import net.giafei.demo.cloud.order.dto.OrderDTO;
import net.giafei.demo.cloud.order.dto.PayDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
 * Date 2019/7/2 17:01
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private IPayService service;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private PlatformTransactionManager manager;

    @GetMapping("/save")
    @ApiOperation("正常")
    public String test() {
        PayDTO dto = new PayDTO();
        dto.setSpuId(1);
        dto.setAmount(1.5);
        dto.setCreateTime(new Date());

        try {
            service.pay(dto);
            return "ok";
        } catch (Exception e) {
            return "rollback";
        }
    }

    @GetMapping("/saveFail")
    @ApiOperation("订单失败")
    public String test1() {
        PayDTO dto = new PayDTO();
        dto.setSpuId(1);
        dto.setAmount(1.5);
        dto.setCreateTime(new Date());

        try {
            service.payFail(dto);
            return "ok";
        } catch (Exception e) {
            return "rollback";
        }
    }

    @GetMapping("/saveTimeout")
    @ApiOperation("订单超时")
    public String tes2t() {
        PayDTO dto = new PayDTO();
        dto.setSpuId(1);
        dto.setAmount(1.5);
        dto.setCreateTime(new Date());

        try {
            service.payTimeout(dto);
            return "ok";
        } catch (Exception e) {
            return "rollback";
        }
    }

    @GetMapping("/storageFail")
    @ApiOperation("仓储失败")
    public String test4() {
        PayDTO dto = new PayDTO();
        dto.setSpuId(1);
        dto.setAmount(1.5);
        dto.setCreateTime(new Date());

        try {
            service.payStorageFail(dto);
            return "ok";
        } catch (Exception e) {
            return "rollback";
        }
    }

    @GetMapping("/storageTimeout")
    @ApiOperation("仓储超时")
    public String test5() {
        PayDTO dto = new PayDTO();
        dto.setSpuId(1);
        dto.setAmount(1.5);
        dto.setCreateTime(new Date());

        try {
            service.payStorageTimeout(dto);
            return "ok";
        } catch (Exception e) {
            return "rollback";
        }
    }
}
