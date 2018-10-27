package cn.edu.xidian.ictt.yk.aio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;

/**
 * Created by heart_sunny on 2018/10/23
 */
public class AIOEchoClient2 {

    public static void main(String[] args) {

        try {
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            Future<Void> future = client.connect(new InetSocketAddress("localhost", 9999));
            future.get();

            ByteBuffer buffer = ByteBuffer.allocate(100);
            client.read(buffer, null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer result, Void attachment) {
                    System.out.println("client received:" + new String(buffer.array()));
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    exc.printStackTrace();
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
