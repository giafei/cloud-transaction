package net.giafei.cloud.db.transaction2pc.core.participant.impl.internal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


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
 * Date 2019/7/3 21:50
 */

@Component
public class CloudTransactionIdFeignInterceptor implements RequestInterceptor {

    private static final String REQUEST_ATTRIBUTE_TRANSACTION_ID = "X-TRANSACTION-ID";
    private static final String REQUEST_HEADER_TRANSACTION_ID = "X-TRANSACTION-ID";

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest();
    }

    @Override
    public void apply(RequestTemplate template) {
        try {
            HttpServletRequest currentRequest = getCurrentRequest();
            Object attribute = currentRequest.getAttribute(REQUEST_ATTRIBUTE_TRANSACTION_ID);
            if (attribute != null) {
                template.header(REQUEST_HEADER_TRANSACTION_ID, attribute.toString());
            } else {
                String header = currentRequest.getHeader(REQUEST_HEADER_TRANSACTION_ID);
                if (StringUtils.hasText(header)) {
                    template.header(REQUEST_HEADER_TRANSACTION_ID, header);
                }
            }
        } catch (Throwable e) {
            //不能影响正常的流程运行
        }
    }

    public static String getCloudTransactionId() {
        HttpServletRequest currentRequest = getCurrentRequest();
        Object attribute = currentRequest.getAttribute(REQUEST_ATTRIBUTE_TRANSACTION_ID);
        if (attribute != null)
            return attribute.toString();

        return currentRequest.getHeader(REQUEST_HEADER_TRANSACTION_ID);
    }

    public static void broadcastCloudTransactionId(String id) {
        getCurrentRequest().setAttribute(REQUEST_ATTRIBUTE_TRANSACTION_ID, id);
    }
}
