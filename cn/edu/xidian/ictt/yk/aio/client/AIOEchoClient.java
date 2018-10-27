package cn.edu.xidian.ictt.yk.aio.client;

import cn.edu.xidian.ictt.yk.utils.InputUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by heart_sunny on 2018/10/22
 */
class ClientReadHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel clientChannel;
    private CountDownLatch latch;

    public ClientReadHandler(AsynchronousSocketChannel clientChannel, CountDownLatch latch) {
        this.clientChannel = clientChannel;
        this.latch = latch;
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        buffer.flip();
        String receiveMessage = new String(buffer.array(), 0, buffer.remaining());
        System.out.println(receiveMessage);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        try {
            this.clientChannel.close();
        } catch (IOException e) {
            this.latch.countDown();
        }
    }
}

class ClientWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel clientChannel;
    private CountDownLatch latch;

    public ClientWriteHandler(AsynchronousSocketChannel clientChannel, CountDownLatch latch) {
        this.clientChannel = clientChannel;
        this.latch = latch;
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            clientChannel.write(buffer, buffer, this);
        } else {
            ByteBuffer readBuffer = ByteBuffer.allocate(50);
            clientChannel.read(readBuffer, readBuffer, new ClientReadHandler(clientChannel, latch));
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        try {
            this.clientChannel.close();
        } catch (IOException e) {
            this.latch.countDown();
        }
    }
}

class AIOClientThread implements Runnable{

    private CountDownLatch latch;
    private AsynchronousSocketChannel clientChannel;

    public AIOClientThread() throws Exception{
        this.latch = new CountDownLatch(1);
        this.clientChannel = AsynchronousSocketChannel.open();
        this.clientChannel.connect(new InetSocketAddress("localhost", 9999));
    }

    @Override
    public void run() {
        try {
            this.latch.await();
            this.clientChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(String msg) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        byteBuffer.put(msg.getBytes());
        byteBuffer.flip();
        clientChannel.write(byteBuffer, byteBuffer, new ClientWriteHandler(clientChannel, latch));
        if ("exit".equalsIgnoreCase(msg)) {
            return false;
        }
        return true;
    }
}

public class AIOEchoClient {

    public static void main(String[] args) {
        try {
            AIOClientThread client = new AIOClientThread() ;
            new Thread(client).start();
            while (client.sendMessage(InputUtil.getString("请输入要发送的信息："))) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
