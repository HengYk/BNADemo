package cn.edu.xidian.ictt.yk.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by heart_sunny on 2018/10/21
 */
class NIOEchoServerHandle implements AutoCloseable{

    private ExecutorService executorService;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private SocketChannel socketChannel;

    public NIOEchoServerHandle() {
        try {
            this.executorService = Executors.newFixedThreadPool(5);

            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.bind(new InetSocketAddress(9999));

            this.selector = Selector.open();
            this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

            System.out.println("服务端程序启动，改程序在9999端口上监听...");

            this.handleClient();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClient() throws Exception{
        while (true) {
            int readyChannels = selector.select();
            if (readyChannels == 0) continue;

            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    this.socketChannel = serverSocketChannel.accept();
                    if (socketChannel != null) {
                        executorService.submit(new SocketChannelThread(socketChannel));
                    }
                }
                iterator.remove();
            }

            //注意：selector.close();这里不能有
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SocketChannelThread implements Runnable{

    private SocketChannel socketChannel;

    public SocketChannelThread(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        System.out.println("服务器端连接成功，可以与服务器端进行数据的交互操作...");
    }

    @Override
    public void run() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        boolean flag = true;

        while (flag) {
            try {
                byteBuffer.clear();
                int readCount = socketChannel.read(byteBuffer);
                //byteBuffer.flip();
                String readMessage = new String(byteBuffer.array(), 0, readCount);
                System.out.println("【服务器端接收消息】" + readMessage);

                //分隔符是一个很重要的概念
                String writeMessage = "【ECHO】" + readMessage + "\n";
                if ("exit".equalsIgnoreCase(readMessage)) {
                    writeMessage = "【ECHO】Bye Byte ... kiss";
                    flag = false;
                }
                byteBuffer.clear();
                byteBuffer.put(writeMessage.getBytes());
                byteBuffer.flip();
                this.socketChannel.write(byteBuffer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class NIOEchoServer {

    public static void main(String[] args) {

        new NIOEchoServerHandle();
    }
}
