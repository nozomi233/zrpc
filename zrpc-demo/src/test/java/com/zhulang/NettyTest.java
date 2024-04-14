package com.zhulang;

import com.zhulang.netty.AppClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NettyTest {

    @Test
    public void testByteBuf(){
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();

        // 通过逻辑组装而不是物理拷贝，实现在JVM中的零拷贝
        CompositeByteBuf httpBuf = Unpooled.compositeBuffer();
        httpBuf.addComponents(header, body);
    }


    @Test
    public void testWrapper(){
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];

        // 共享byte数组的内容而不是拷贝，零拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
    }


    @Test
    public void testSlice(){
        byte[] buf = new byte[1024];
        byte[] buf2 = new byte[1024];

        // 一个byteBuf分割成多个，使用共享地址而非拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buf, buf2);
        ByteBuf header = byteBuf.slice(0,5);
        ByteBuf body = byteBuf.slice(6,10);
    }

    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("zll".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);
        // body:随便写一个对象进去，用对象流转化为字节数据
        AppClient appClient = new AppClient();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(appClient);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);

        printAsBinary(message);

    }

    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);

        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();

        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary.append(binaryString.substring(i, i + 2)).append(" ");
        }

        System.out.println("Binary representation: " + formattedBinary.toString());
    }

    @Test
    public void testCompress() throws IOException {
        // 对特别少的数据因为增加辅助字段反而可能会变大，所以buf要大点压缩效果好
        byte[] buf = new byte[]{12,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14,12,12,12,12,25,34,23,25,14};

        // 本质就是，将buf作为输入，将结果输出到另一个字节数组当中
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);

        gzipOutputStream.write(buf);
        gzipOutputStream.finish();

        byte[] bytes = baos.toByteArray();
        System.out.println(buf.length + "--> " + bytes.length);
        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void testDeCompress() throws IOException {
        byte[] buf = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, -31, 1, 2, 73, 37, 113, 73, -66, 65, -62, 0, 0, 25, -102, -59, -115, -111, 0, 0, 0};

        // 本质就是，将buf作为输入，将结果输出到另一个字节数组当中
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream =   new GZIPInputStream(bais);

        byte[] bytes = gzipInputStream.readAllBytes();
        System.out.println(buf.length + "--> " + bytes.length);
        System.out.println(Arrays.toString(bytes));
    }
}
