import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

public class Server {
    protected static StateHolder state = new StateHolder("WAITING"); // Server startar alltid som WAITING
    static int portNumber = 4444;
    //anv Set för att flera trådar samtidigt ska kunna manipulera datan
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started on port " + portNumber);

            // Lyssna efter klienter
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // Varje ansluten klient körs på en egen tråd
                    if(state.getState().equals("WAITING")){
                        System.out.println("\n\n\nServer.java state: " + state.getState());
                        new ServerThread(clientSocket, state).start();
                    } else {
                        reject(clientSocket);
                    }
                } catch (Exception e) {
                    //Gick ej för klient att connecta
                    System.out.println("Connection failed.");
                }
            }
        } catch (Exception e) {
            //Gick ej att starta server på den porten
            System.out.println("Connection failed.");
        }
    }

    public static void reject(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("CLEAR");
            out.println("Could not connect, game not ready...");
            out.println("Closing connection...");
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("Connection failed.");
        }
    }
}