package cn.edu.xidian.ictt.yk.bio.client;

import cn.edu.xidian.ictt.yk.utils.InputUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by heart_sunny on 2018/10/20
 */
class EchoClientHandle implements AutoCloseable{

    private Socket client;

    public EchoClientHandle() {

        try {
            this.client = new Socket("localhost", 9999);
            System.out.println("已经成功连接到服务器，可以进行消息处理。");
            this.accessServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void accessServer() throws IOException {
        Scanner scanner = new Scanner(client.getInputStream());
        PrintStream out = new PrintStream(client.getOutputStream());
        scanner.useDelimiter("\n");
        boolean flag = true;
        while (flag) {
            String data = InputUtil.getString("请输入您的数据：");
            out.println(data);
            if ("exit".equalsIgnoreCase(data)) {
                flag = false;
            }
            if (scanner.hasNext()) {
                System.out.println(scanner.next());
            }
        }
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class EchoClient {
    public static void main(String[] args) {
        EchoClientHandle echoClientHandle = new EchoClientHandle();
    }
}
