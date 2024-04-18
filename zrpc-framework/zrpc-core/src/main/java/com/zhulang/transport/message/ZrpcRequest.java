package com.zhulang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务调用方发起的请求内容
 * @Author Nozomi
 * @Date 2024/4/18 21:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZrpcRequest {

    // 请求的id
    private long requestId;

    // 请求的类型，压缩的类型，序列化的方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    // 具体的消息体
    private RequestPayload requestPayload;
}
