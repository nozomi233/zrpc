package com.zhulang.core;

/**
 * @Author Nozomi
 * @Date 2024/4/23 9:32
 */

public class ZrpcShutdownHook extends Thread {
    @Override
    public void run() {
        // 1、打开挡板   （boolean 需要线程安全）
        ShutDownHolder.BAFFLE.set(true);

        // 2、等待计数器归零（正常的请求处理结束）  AtomicInteger
        // 等待归零，继续执行  最多等十秒
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (ShutDownHolder.REQUEST_COUNTER.sum() == 0L
                    || System.currentTimeMillis() - start > 10000) {
                break;
            }
        }
        // 3、阻塞结束后，放行。执行其他操作，如释放资源
    }
}
