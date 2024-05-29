import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;

public class ServerThread extends Thread {
    private static int clientsConnected = 0; 
    private static int clientCounter = 0; // Static counter för att ge varje klient unikt ID
    private Socket socket = null;
    private PrintWriter out = null;
    private Set<PrintWriter> clientWriters;
    private int clientID; //unikt ID för varje klient
    private static String state = "WAITING";
    private static Object lock = new Object();
    private static boolean inProgress = false;

    public ServerThread(Socket socket, Set<PrintWriter> clientWriters) {
        this.socket = socket;
        this.clientWriters = clientWriters;

        synchronized(ServerThread.class){
            clientID = ++clientCounter;
            
        }
    }

    public void run() {
        BufferedReader in = null;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            synchronized (ServerThread.class) {
                clientWriters.add(out);
                clientsConnected++; 
                if(clientsConnected == 2){
                    synchronized(lock) {
                    state = "READY";
                    lock.notify();
                    }
                }
                waitOrReadyMessage();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Game game = null;

        while (true) {
            try {
                //Protocol protocol = new Protocol();
                
                synchronized (lock) {
                    try {
                        while (state.equals("WAITING")) {
                            lock.wait();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                synchronized (ServerThread.class) {
                    System.out.println("Creating new game for client id: " + clientID);
                    game = new Game();
                    inProgress = true;
                }

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inProgress == false) {
                        break;
                    }
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

                game.resetGame();
                game = null;

                synchronized (ServerThread.class) {
                    waitOrReadyMessage();
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Client " + clientID + " disconnected");
                synchronized (ServerThread.class) {
                    clientWriters.remove(out);
                    clientsConnected--;
                    if (game != null) {
                        System.out.println("Resetting game");
                        game.resetGame();
                        game = null;
                    }
                    if(clientsConnected == 1){
                        state = "WAITING";
                    }
                    inProgress = false;
                }
                break;
            }
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
            if(state == "WAITING"){
                send2all("Client count: " + clientCounter + "\n" +
                         "Clients connected = " + clientsConnected + "\n" +
                         "Waiting for another player to connect...");
            }
            if (state == "READY"){
                send2all(clientsConnected + " Clients connected" + "\n" +
                         "Press [enter] to play!");
            }
        }
    }
}