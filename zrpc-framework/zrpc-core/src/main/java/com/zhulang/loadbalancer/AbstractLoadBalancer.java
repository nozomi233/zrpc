package com.zhulang.loadbalancer;

import com.zhulang.ZrpcBootstrap;
import com.zhulang.discovery.Registry;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Nozomi
 * @Date 2024/4/20 19:35
 */

public abstract class AbstractLoadBalancer implements LoadBalancer {
    // 一个服务会匹配一个selector
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {

        // 1、优先从cache中获取一个选择器
        Selector selector = cache.get(serviceName);

        // 2、如果没有，就需要为这个service创建一个selector
        if (selector == null) {
            // 对于这个负载均衡器，内部应该维护服务列表作为缓存
            List<InetSocketAddress> serviceList = ZrpcBootstrap.getInstance().getRegistry().lookup(serviceName,null);

            // 提供一些算法负责选取合适的节点
            selector = getSelector(serviceList);

            // 将select放入缓存当中
            cache.put(serviceName, selector);
        }

        // 获取可用节点
        return selector.getNext();
    }

    @Override
    public synchronized void reLoadBalance(String serviceName,List<InetSocketAddress> addresses) {
        // 我们可以根据新的服务列表生成新的selector
        cache.put(serviceName,getSelector(addresses));
    }

    /**
     * 由子类进行扩展
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);

}