package server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = 5555;
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Server listening on " + port);
            while (true) {
                Socket s = ss.accept();
                System.out.println("Connected: " + s.getRemoteSocketAddress());
                new ClientHandler(s).start();
            }
        }
    }
}
