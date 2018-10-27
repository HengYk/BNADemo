package cn.edu.xidian.ictt.yk.nio.client;

import cn.edu.xidian.ictt.yk.utils.InputUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by heart_sunny on 2018/10/21
 */
class NIOEchoClientHandle implements AutoCloseable{

    private SocketChannel socketChannel;

    public NIOEchoClientHandle() {
        try {
            this.socketChannel = SocketChannel.open();
            this.socketChannel.connect(new InetSocketAddress("localhost", 9999));
            this.accessServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void accessServer() throws Exception{
        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        boolean flag = true;

        while (flag) {
            byteBuffer.clear();
            String msg = InputUtil.getString("请输入您的内容：");
            byteBuffer.put(msg.getBytes());
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            byteBuffer.clear();

            int readCount = socketChannel.read(byteBuffer);
            byteBuffer.flip();
            System.out.println(new String(byteBuffer.array(), 0, readCount));

            if ("exit".equalsIgnoreCase(msg)) {
                flag = false;
            }
        }
    }

    @Override
    public void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class NIOEchoClient {

    public static void main(String[] args) {
        new NIOEchoClientHandle();
    }
}
