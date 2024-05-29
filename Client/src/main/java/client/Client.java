package client;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;
import org.jline.utils.InfoCmp.Capability;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 4444;
        try (Socket socket = new Socket(hostName, portNumber);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Terminal terminal = TerminalBuilder.terminal();
             NonBlockingReader reader = terminal.reader()) 
             {
            terminal.enterRawMode(); // Ensure terminal is in raw mode
            terminal.writer().write("\033[?25l"); //Hides the cursor
            //terminal.writer().write("\033[?25h"); //Shows the cursor again
            // Start a new thread to listen for messages from the server
            new Thread(new ServerListener(in, terminal)).start();
            String fromUser = "";
            int c = ' ';
            while (true) {
                c = reader.read(); // Read non-blocking key press
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
                    case 13:
                        fromUser = "ENTER";
                        break;
                    //ESC
                    case 27:
                        //Quits game
                        fromUser = "q";
                        break;
                    default:
                        //
                        break;
                }
                if (fromUser != "") { // Kolla så klienten tryckt på något okej
                    out.println(fromUser); // Skicka till servern
                    fromUser = "";
                }
            }
        } catch (Exception e) {
            System.out.println("Could not connect to Server");
            //e.printStackTrace();
        }
    }
}

class ServerListener implements Runnable {
    private BufferedReader in;
    private Terminal terminal;
    
    public ServerListener(BufferedReader in, Terminal terminal) {
        this.in = in;
        this.terminal = terminal;
    }
    @Override
    public void run() {
        String fromServer;
        try {
            while ((fromServer = in.readLine()) != null) {
                if (fromServer.equals("Closing connection...")) {
                    System.out.println("Closing connection...");
                    System.exit(0);
                    break;
                }
                if(fromServer.equals("CLEAR")){
                    clearScreen(terminal);
                } else {
                    //Skriv inte ut clear, men allt annat
                    System.out.println(fromServer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearScreen(Terminal terminal) {
        terminal.puts(Capability.clear_screen);
        terminal.flush();
    }
}