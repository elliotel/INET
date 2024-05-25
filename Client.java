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
            String fromServer, fromUser;
            
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Server: " +  fromServer);
                if (fromServer.equals("Closing connection...")) {
                    break;
                }

                fromUser = stdIn.readLine();
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
