package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import biz.source_code.utils.RawConsoleInput;

public class Client {   
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 4444;
        try (
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            String fromServer;
            String fromUser = "";
            int c = ' ';
            
            while ((fromServer = in.readLine()) != null) {
                
                System.out.println("Server: " +  fromServer);
                if (fromServer.equals("Closing connection...")) {
                    break;
                }
                try {
                    c = RawConsoleInput.read(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                switch (c) {
                    case 'w':
                        fromUser = "UP";
                        break;
                    case 'a':
                        fromUser = "LEFT";
                        break;
                    case 's':
                        fromUser = "DOWN";
                        break;
                    case 'd':
                        fromUser = "RIGHT";
                        break;
                    //ESC
                    case 27:
                        //Quits game
                        fromUser = "";
                        break;
                    default:
                        fromUser = "INGET HÄNDER HAHA";
                        break;
                }
                if (fromUser != null) {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}