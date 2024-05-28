package server;

import java.net.ServerSocket;

public class Server{

    static int portNumber = 4444;
    public static void main(String[] args) {
        while (true) {
            try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
            ) {
                new ServerThread(serverSocket.accept()).start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}