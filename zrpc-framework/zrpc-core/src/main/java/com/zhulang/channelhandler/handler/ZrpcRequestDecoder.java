package com.zhulang.channelhandler.handler;

import com.zhulang.enumeration.RequestType;
import com.zhulang.serialize.Serializer;
import com.zhulang.serialize.SerializerFactory;
import com.zhulang.transport.message.MessageFormatConstant;
import com.zhulang.transport.message.RequestPayload;
import com.zhulang.transport.message.ZrpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * <p>
 * <pre>
 *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   14   15   16   17   18   19   20   21   22
 *   +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 *   |    magic          |ver |head  len|    full length    | qt | ser|comp|              RequestId                |
 *   +-----+-----+-------+----+----+----+----+-----------+----- ---+--------+----+----+----+----+----+----+---+---+
 *   |                                                                                                             |
 *   |                                         body                                                                |
 *   |                                                                                                             |
 *   +--------------------------------------------------------------------------------------------------------+---+
 * </pre>
 * <p>
 * 4B magic(魔数)   --->zrpc.getBytes()
 * 1B version(版本)   ----> 1
 * 2B header length 首部的长度
 * 4B full length 报文总长度
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 * <p>
 * 基于长度字段的帧解码器
 *
 * @Author Nozomi
 * @Date 2024/4/18 22:41
 */
@Slf4j
public class ZrpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    public ZrpcRequestDecoder() {
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文我们可以去进行解析
                // 最大帧的长度，超过这个maxFrameLength值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量，
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度的字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // todo 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 1、解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        // 检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if(magic[i] != MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("The request obtained is not legitimate。");
            }
        }

        // 2、解析版本号
        byte version = byteBuf.readByte();
        if(version > MessageFormatConstant.VERSION){
            throw new RuntimeException("获得的请求版本不被支持。");
        }

        // 3、解析头部的长度
        short headLength = byteBuf.readShort();

        // 4、解析总长度
        int fullLength = byteBuf.readInt();

        // 5、请求类型
        byte requestType = byteBuf.readByte();

        // 6、序列化类型
        byte serializeType = byteBuf.readByte();

        // 7、压缩类型
        byte compressType = byteBuf.readByte();

        // 8、请求id
        long requestId = byteBuf.readLong();

        // 我们需要封装
        ZrpcRequest zrpcRequest = new ZrpcRequest();
        zrpcRequest.setRequestType(requestType);
        zrpcRequest.setCompressType(compressType);
        zrpcRequest.setSerializeType(serializeType);
        zrpcRequest.setRequestId(requestId);

        // 心跳请求没有负载，此处可以判断并直接返回
        if( requestType == RequestType.HEART_BEAT.getId() ){
            return zrpcRequest;
        }

        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        // 有了字节数组之后就可以解压缩，反序列化
        // todo 解压缩

        // 反序列化
        // 1 --> jdk
        Serializer serializer = SerializerFactory.getSerializer(serializeType).getImpl();
        RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
        zrpcRequest.setRequestPayload(requestPayload);

//        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
//             ObjectInputStream ois = new ObjectInputStream(bis)
//        ) {
//            RequestPayload requestPayload = (RequestPayload) ois.readObject();
//            zrpcRequest.setRequestPayload(requestPayload);
//        } catch (IOException | ClassNotFoundException e){
//            log.error("请求【{}】反序列化时发生了异常",requestId,e);
//        }

        if (log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成解码工作。", zrpcRequest.getRequestId());
        }
        return zrpcRequest;
    }
}
