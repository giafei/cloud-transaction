package net.giafei.cloud.db.transaction2pc.core.datasource;

import feign.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.lang.reflect.Type;

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
 * Date 2019/7/4 12:09
 */

@Configuration
public class TransactionThief {

    @Bean
    public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
                              SpringClientFactory clientFactory,
                              PlatformTransactionManager transactionManager) {
        return new LoadBalancerFeignClientWrapper(new Client.Default(null, null),
                cachingFactory, clientFactory, transactionManager);
    }

    public static class LoadBalancerFeignClientWrapper implements Client {
        private LoadBalancerFeignClient wrapped;
        private WrappedDataSourceTransactionManager transactionManager;

        public LoadBalancerFeignClientWrapper(Client delegate,
                                       CachingSpringLoadBalancerFactory lbClientFactory,
                                       SpringClientFactory clientFactory, PlatformTransactionManager transactionManager) {
            wrapped = new LoadBalancerFeignClient(delegate, lbClientFactory, clientFactory);
            this.transactionManager = (WrappedDataSourceTransactionManager)transactionManager;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            try {
                transactionManager.stealTransactionThreadData();
                Response response = wrapped.execute(request, options);
                transactionManager.returnTransactionThreadData();

                return response;
            } catch (Throwable e) {
                transactionManager.returnTransactionThreadData();
                throw e;
            }
        }
    }
}
