package net.giafei.cloud;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * 全局未捕获异常
 *
 * @author xjf
 * @version 1.0
 * Date 2018-06-19 16:54
 */

@RestControllerAdvice
public class GlobalUncaughtExceptionHandler extends ResponseEntityExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(GlobalUncaughtExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Object> bizExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        BizException ex = (BizException)e;
        return new ResponseEntity<>(
                new BizExceptionErrorDecoder.BizExceptionData(ex.getCode(), ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler
    public ResponseEntity<Object> unHandleExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {

        logger.error("未捕获的Exception, url={}, method={}", request.getRequestURI(), request.getMethod(), e);
        return new ResponseEntity<>(
                new BizExceptionErrorDecoder.BizExceptionData("SYS0101", "系统错误，请稍后重试"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
