import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server {
    static int portNumber = 4444;
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    //anv Set för att flera trådar samtidigt ska kunna manipulera datan
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started on port " + portNumber);

            while (true) {
                try {
                    new ServerThread(serverSocket.accept(), clientWriters).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}