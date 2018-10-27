package cn.edu.xidian.ictt.yk.bio.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by heart_sunny on 2018/10/19
 */
class EchoServerhandle implements AutoCloseable{

    private ServerSocket serverSocket = null;

    public EchoServerhandle() {
        try {
            this.serverSocket = new ServerSocket(9999);
            this.clientConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clientConnect() throws IOException{

        while (true) {
            Socket server = serverSocket.accept();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Scanner scanner = new Scanner(server.getInputStream());
                        PrintStream out = new PrintStream(server.getOutputStream());
                        scanner.useDelimiter("\n");
                        boolean clientFlag = true;
                        while (clientFlag) {
                            if (scanner.hasNext()) {
                                String inputData = scanner.next().trim();
                                if ("exit".equalsIgnoreCase(inputData)) {
                                    clientFlag = false;
                                    out.println("[echo] bye bye ... kiss");
                                } else {
                                    out.println("[echo] " + inputData);
                                }
                            }
                        }
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


public class EchoServer{

    public static void main(String[] args) {
        EchoServerhandle echoServerhandle = new EchoServerhandle();
    }
}
