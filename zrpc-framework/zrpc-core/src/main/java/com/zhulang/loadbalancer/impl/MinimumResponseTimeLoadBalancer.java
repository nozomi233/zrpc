package com.zhulang.loadbalancer.impl;

import com.zhulang.ZrpcBootstrap;
import com.zhulang.loadbalancer.AbstractLoadBalancer;
import com.zhulang.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 最短响应时间的负载均衡策略
 * @Author Nozomi
 * @Date 2024/4/21 12:31
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector {

        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = ZrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null) {
                if (log.isDebugEnabled()){
                    log.debug("选取了响应时间为【{}ms】的服务节点.",entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }

            // 直接从缓存中获取一个可用的就行了
            System.out.println("----->"+ Arrays.toString(ZrpcBootstrap.CHANNEL_CACHE.values().toArray()));
            Channel channel = (Channel) ZrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress)channel.remoteAddress();
        }

    }
}
