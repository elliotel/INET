package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;
import org.jline.utils.InfoCmp.Capability;

import java.io.IOException;

public class Client {   
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 4444;
        try (
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            NonBlockingReader reader = terminal.reader();
        ) {
            terminal.enterRawMode(); // Ensure terminal is in raw mode   
            clearScreen(terminal);
            String fromServer;
            String fromUser = "";
            int c = ' ';
            
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " +  fromServer);
                if (fromServer.equals("Closing connection...")) {
                    break;
                }
                try {
                    c = reader.read();
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
                    case 'c':
                        fromUser = "Nu clearade vi skärmen hehe";
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
                clearScreen(terminal);
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

    
    private static void clearScreen(Terminal terminal) {
        terminal.puts(Capability.clear_screen);
        terminal.flush();
    }
}
