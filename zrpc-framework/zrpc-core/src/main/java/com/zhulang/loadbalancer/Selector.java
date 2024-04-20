package com.zhulang.loadbalancer;

import java.net.InetSocketAddress;

/**
 * @Author Nozomi
 * @Date 2024/4/20 19:34
 */

public interface Selector {
    /**
     * 根据服务列表执行一种算法获取一个服务节点
     * @return 具体的服务节点
     */
    InetSocketAddress getNext();
}
