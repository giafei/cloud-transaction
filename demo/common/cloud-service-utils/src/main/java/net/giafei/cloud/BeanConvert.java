package net.giafei.cloud;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * Date 2018-07-06 16:07
 */

public abstract class BeanConvert {
    public static <T> T convert(Class<T> tClass, Object... sources) {
        try {
            return convert(tClass.newInstance(), sources);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> convertList(Class<T> tClass, List<?>... sources) {
        if (sources == null || sources.length == 0)
            return Collections.emptyList();

        try {
            List<?> s = sources[0];
            List<T> result = new ArrayList<>(s.size());
            for (int i = 0; i < s.size(); i++) {
                T t = tClass.newInstance();

                for (List<?> source : sources) {
                    BeanUtils.copyProperties(source.get(i), t);
                }

                result.add(t);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convert(T target, Object... sources) {
        for (Object source : sources) {
            BeanUtils.copyProperties(source, target);
        }

        return target;
    }

    public static <T> T convert(Class<T> tClass, HttpServletRequest request) {
        try {
            return convert(tClass.newInstance(), request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convert(T target, HttpServletRequest request) {
        //抄的SpringMVC的代码
        RequestMappingHandlerAdapter bean = SpringApplicationContextHolder.getBean(RequestMappingHandlerAdapter.class);
        ConfigurableWebBindingInitializer initializer = (ConfigurableWebBindingInitializer)bean.getWebBindingInitializer();


        ServletRequestParameterPropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
        beanWrapper.setExtractOldValueForEditor(true);
        beanWrapper.setAutoGrowNestedPaths(true);
        beanWrapper.setAutoGrowCollectionLimit(256);

        //时间格式这些
        beanWrapper.setConversionService(initializer.getConversionService());
        beanWrapper.setPropertyValues(mpvs, true, true);

        return target;
    }
}
