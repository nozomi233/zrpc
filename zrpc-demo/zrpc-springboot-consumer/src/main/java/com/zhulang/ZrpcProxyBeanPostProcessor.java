package com.zhulang;

import com.zhulang.annotation.ZrpcReference;
import com.zhulang.proxy.ZrpcProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @Author Nozomi
 * @Date 2024/4/23 10:48
 */
@Component
public class ZrpcProxyBeanPostProcessor implements BeanPostProcessor {

    // 他会拦截所有的bean的创建，会在每一个bean初始化后被调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 想办法给他生成一个代理
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            ZrpcReference zrpcReference = field.getAnnotation(ZrpcReference.class);
            if(zrpcReference != null){
                // 获取一个代理
                Class<?> type = field.getType();
                Object proxy = ZrpcProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    // 把代理对象设置到bean里面
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return bean;
    }
}
