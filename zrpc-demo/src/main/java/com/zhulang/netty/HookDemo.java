package com.zhulang.netty;

/**
 * @Author Nozomi
 * @Date 2024/4/23 9:27
 */

public class HookDemo {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("程序正在关闭");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("合法请求已经被处理完成");

        }));

        while (true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("正在处理请求");
        }

    }
}
