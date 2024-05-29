import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;

public class ServerThread extends Thread {
    private static int clientsConnected = 0; 
    private static int clientCounter = 0; // Static counter för att ge varje klient unikt ID
    private Socket socket = null;
    private PrintWriter out;
    private Set<PrintWriter> clientWriters;
    private int clientID; //unikt ID för varje klient
    private static String state = "WAITING";

    public ServerThread(Socket socket, Set<PrintWriter> clientWriters) {
        this.socket = socket;
        this.clientWriters = clientWriters;

        synchronized(ServerThread.class){
            clientID = ++clientCounter;
            
        }
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Protocol protocol = new Protocol();

            synchronized (ServerThread.class) {
                clientWriters.add(out);
                clientsConnected++; 
                if(clientsConnected == 2){
                    state = "READY";
                }
                waitOrReadyMessage();
            }

            Game game = new Game(clientID);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("q")) {
                    out.println("Closing connection...");
                    break;
                } else {
                    //out.println("Received: " + inputLine);
                    
                    synchronized (ServerThread.class) {
                    game.movePlayer(inputLine);
                    send2all(game.printBoard());
                    }
                }
            }

            synchronized (ServerThread.class) {
                clientWriters.remove(out);
                clientsConnected--;
                if(clientsConnected == 1){
                    state = "WAITING";
                }
                waitOrReadyMessage();
            }

            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Client: " + clientID + " disconnected");
        }
    }

    public void send2all(String s) {
        synchronized (clientWriters) {
            for (PrintWriter out : clientWriters) {
                out.println("START");
                out.println(s);
            }
        }
    }

    private void waitOrReadyMessage() {
        synchronized (clientWriters) {
            for (PrintWriter out : clientWriters) {
                if(state == "WAITING"){
                    out.println("Client count: " + clientCounter);
                    out.println("Clients connected = " + clientsConnected);
                    out.println(" Waiting for another player to connect...");
                }
                if (state == "READY"){
                    out.println(clientsConnected + " Clients connected");
                    out.println("Press [enter] to play!");
                }
            }
        }
    }
}