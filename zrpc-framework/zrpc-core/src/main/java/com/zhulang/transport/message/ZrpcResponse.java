package com.zhulang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Nozomi
 * @Date 2024/4/18 21:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZrpcResponse {

    // 请求的id
    private long requestId;

    // 请求的类型，压缩的类型，序列化的方式
    private byte compressType;
    private byte serializeType;

    private long timeStamp;

    // 1 成功，  2 异常
    private byte code;

    // 具体的消息体
    private Object body;
}
