import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


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
            // Start a new thread to listen for messages from the server
            new Thread(new ServerListener(in)).start();

            String fromUser;
            while (true) {
                fromUser = stdIn.readLine();
                if (fromUser != null && !fromUser.isEmpty()) {
                    out.println(fromUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ServerListener implements Runnable {
    private BufferedReader in;

    public ServerListener(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        String fromServer;
        try {
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " + fromServer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}