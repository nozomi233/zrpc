package com.zhulang.proxy.handler;

import com.zhulang.NettyBootstrapInitializer;
import com.zhulang.ZrpcBootstrap;
import com.zhulang.compress.CompressorFactory;
import com.zhulang.discovery.Registry;
import com.zhulang.enumeration.RequestType;
import com.zhulang.exceptions.DiscoveryException;
import com.zhulang.exceptions.NetworkException;
import com.zhulang.serialize.SerializerFactory;
import com.zhulang.transport.message.RequestPayload;
import com.zhulang.transport.message.ZrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 该类封装了客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在了invoke方法中
 * 1、 发现可用服务    2、建立连接   3、发送请求   4、得到结果
 *
 * @Author Nozomi
 * @Date 2024/4/18 16:09
 */

@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    // 此处需要一个注册中心，和一个接口
    private final Registry registry;
    private final Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 我们调用saHi方法，事实上会走进这个代码段中
        // 我们已经知道 method（具体的方法），args(参数列表)
//        log.info("method-->{}", method.getName());
//        log.info("args-->{}", args);

        // 调整顺序，先封装请求，再获取服务端节点和连接，这样可以在选择节点的时候根据封装好的请求内容进行负载均衡
        /*
         * ------------------ 封装报文 ---------------------------
         */
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();

        // 1、 创建请求
        ZrpcRequest zrpcRequest = ZrpcRequest.builder()
                .requestId(ZrpcBootstrap.getInstance().getConfiguration().idGenerator.getId())
                .compressType(CompressorFactory.getCompressor(ZrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                .requestType(RequestType.REQUEST.getId())
                .serializeType(SerializerFactory.getSerializer(ZrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                .timeStamp(new Date().getTime())
                .requestPayload(requestPayload)
                .build();

        // 将请求存入本地线程，需要在合适的时候调用remove方法
        ZrpcBootstrap.REQUEST_THREAD_LOCAL.set(zrpcRequest);



        // 2、发现服务，从注册中心拉取服务列表，并通过客户端负载均衡寻找一个可用节点
        // 传入服务的名字,返回ip+端口
//        InetSocketAddress address = registry.lookup(interfaceRef.getName());
        InetSocketAddress address = ZrpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectServiceAddress(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("服务调用方，发现了服务【{}】的可用主机【{}】.",
                    interfaceRef.getName(), address);
        }

        // 使用netty连接服务器，发送 调用的 服务的名字+方法名字+参数列表，得到结果
        // 定义线程池，EventLoopGroup
        // q：整个连接过程放在这里行不行，也就意味着每次调用都会产生一个新的netty连接。如何缓存我们的连接
        // 也就意味着，每次在此处建立一个新的连接是不合适的


        // 解决方案？缓存channel，尝试从缓存中获取channel，如果未获取，则创建新的连接，并进行缓存
        // 2、尝试获取一个可用通道
        Channel channel = getAvailableChannel(address);
        if (log.isDebugEnabled()) {
            log.debug("获取了和【{}】建立的连接通道,准备发送数据.", address);
        }


        /*
         * ------------------同步策略-------------------------
         */
//                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
        // 需要学习channelFuture的简单的api get 阻塞获取结果，getNow 获取当前的结果，如果未处理完成，返回null
//                if(channelFuture.isDone()){
//                    Object object = channelFuture.getNow();
//                } else if( !channelFuture.isSuccess() ){
//                    // 需要捕获异常,可以捕获异步任务中的异常
//                    Throwable cause = channelFuture.cause();
//                    throw new RuntimeException(cause);
//                }

        /*
         * ------------------异步策略-------------------------
         */

        // 4、写出报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        // 将 completableFuture 暴露出去
        ZrpcBootstrap.PENDING_REQUEST.put(zrpcRequest.getRequestId(), completableFuture);

        // 这里这几 writeAndFlush 写出一个请求，这个请求的实例就会进入pipeline执行出站的一系列操作
        // 我们可以想象得到，第一个出站程序一定是将 zrpcRequest --> 二进制的报文
        channel.writeAndFlush(zrpcRequest).addListener((ChannelFutureListener) promise -> {
            // 当前的promise将来返回的结果是什么，writeAndFlush的返回结果
            // 一旦数据被写出去，这个promise也就结束了
            // 但是我们想要的是什么？  服务端给我们的返回值，所以这里处理completableFuture是有问题的
            // 是不是应该将 completableFuture 挂起并且暴露，并且在得到服务提供方的响应的时候调用complete方法
//                    if(promise.isDone()){
//                        completableFuture.complete(promise.getNow());
//                    }

            // 只需要处理以下异常就行了
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });

        // 清理ThreadLocal
        ZrpcBootstrap.REQUEST_THREAD_LOCAL.remove();
//
        // 如果没有地方处理这个 completableFuture ，这里会阻塞，等待complete方法的执行
        // q: 我们需要在哪里调用complete方法得到结果，很明显 pipeline 中最终的handler的处理结果
        // 5、获得响应的结果
        return completableFuture.get(10, TimeUnit.SECONDS);
    }


    /**
     * 根据地址获取一个可用的通道
     *
     * @param address
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {

        // 1、尝试从缓存中获取
        Channel channel = ZrpcBootstrap.CHANNEL_CACHE.get(address);

        // 2、拿不到就去建立连接
        if (channel == null) {
            // await 方法会阻塞，会等待连接成功在返回，netty还提供了异步处理的逻辑
            // sync和await都是阻塞当前线程，获取返回值（连接的过程是异步的，发生数据的过程是异步的）
            // 如果发生了异常，sync会主动在主线程抛出异常，await不会，异常在子线程中处理需要使用future中处理
//                    channel = NettyBootstrapInitializer.getBootstrap()
//                        .connect(address).await().channel();

            // 使用addListener执行的异步操作
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise -> {
                        if (promise.isDone()) {
                            // 异步的，我们已经完成
                            if (log.isDebugEnabled()) {
                                log.debug("已经和【{}】成功建立了连接。", address);
                            }
                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    }
            );

            // 阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时，发生异常。", e);
                throw new DiscoveryException(e);
            }

            // 缓存channel
            ZrpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }

        if (channel == null) {
            log.error("获取或建立与【{}】的通道时发生了异常。", address);
            throw new NetworkException("获取通道时发生了异常。");
        }

        return channel;
    }
}
