package com.zhulang.serialize;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @Author Nozomi
 * @Date 2024/4/20 15:57
 */
@Slf4j
public class SerializeUtil {

    public static byte[] serialize(Object object) {
        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有payload
        if (object == null){
            return null;
        }

        // 希望可以通过一些设计模式，面向对象的编程，让我们可以配置修改序列化和压缩的方式
        // 对象怎么变成一个字节数据  序列化  压缩
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(object);

            // 压缩

            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }

}
