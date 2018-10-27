package cn.edu.xidian.ictt.yk.aio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by heart_sunny on 2018/10/22
 */
class EchoHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel clientChannel;
    private boolean exit = false;

    public EchoHandler(AsynchronousSocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        buffer.flip();
        String readMessage = new String(buffer.array(), 0, buffer.remaining()).trim();
        System.out.println("【服务器端接收到消息内容】" + readMessage);

        String resultMessage = "【ECHO】" + readMessage + "\n" ;
        if ("exit".equalsIgnoreCase(readMessage)) {
            resultMessage = "【EXIT】Bye Bye ... kiss" + "\n" ;
            this.exit = true;
        }
        this.echoWrite(resultMessage);
    }

    private void echoWrite(String result) {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.put(result.getBytes());
        buffer.flip();
        this.clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    EchoHandler.this.clientChannel.write(buffer, buffer, this);
                } else {
                    if (!EchoHandler.this.exit) {
                        ByteBuffer readBuffer = ByteBuffer.allocate(50);
                        EchoHandler.this.clientChannel.read(readBuffer, readBuffer, new EchoHandler(EchoHandler.this.clientChannel));
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                try {
                    EchoHandler.this.clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AIOServerThread>{

    @Override
    public void completed(AsynchronousSocketChannel result, AIOServerThread attachment) {
        attachment.getServerChannel().accept(attachment, this);
        ByteBuffer buffer = ByteBuffer.allocate(50);
        result.read(buffer, buffer, new EchoHandler(result));
    }

    @Override
    public void failed(Throwable exc, AIOServerThread attachment) {
        System.out.println("服务器端客户端连接失败...");
        attachment.getLatch().countDown();
    }
}

class AIOServerThread implements Runnable {

    private CountDownLatch latch;
    private AsynchronousServerSocketChannel serverChannel;

    public AIOServerThread() throws Exception{
        this.latch = new CountDownLatch(1);
        this.serverChannel = AsynchronousServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(9999));
        System.out.println("服务器启动成功，在9999端口上监听服务...");
    }

    public AsynchronousServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    @Override
    public void run() {
        serverChannel.accept(this, new AcceptHandler());
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class AIOEchoServer {

    public static void main(String[] args) {
        try {
            new Thread(new AIOServerThread()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
