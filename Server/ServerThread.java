import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;

public class ServerThread extends Thread {
    protected static int clientsConnected = 0; 
    protected static int clientCounter = 0;       //Static counter för att ge varje klient unikt ID
    private Socket socket = null;
    private PrintWriter out;
    private Set<PrintWriter> clientWriters;
    public StateHolder state;
    protected int clientID;                       //unikt ID för varje klient
    private String output;
    private Protocol protocol;

    public ServerThread(Socket socket, Set<PrintWriter> clientWriters, StateHolder state) {
        this.state = state;
        this.socket = socket;
        this.clientWriters = clientWriters;
        protocol = new Protocol(this);

        synchronized(ServerThread.class){
            clientID = ++clientCounter;
        }
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            synchronized (ServerThread.class) {
                clientWriters.add(out);
                clientsConnected++;
                System.out.println("PLACE C / ID: " + clientID + " / clients connected: " + clientsConnected + " / State: " + state.getState());
                send2all(null);
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("QUIT")) {
                    break;
                } else {
                    send2all(inputLine);
                }
            }

            //Klienten har tryckt ctrl-c ELLER 'q'
            synchronized (ServerThread.class) {
                quit();
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Client: " + clientID + " disconnected");
        }
    }

    private void quit() {
        //Skriv endast till klienten som stänger connection
        out.println("CLEAR");
        out.println("Closing connection...");

        //Ta bort klienten ur listan
        clientWriters.remove(out);
        clientsConnected--;

        
        //Hämta meddelande att skicka till alla (kommer vara 1 klient)
        send2all("QUIT");
        System.out.println("PLACE E / ID: " + clientID + " / clients connected: " + clientsConnected + " / State: " + state.getState());
    }

    public void quitAll() {
        for (PrintWriter out : clientWriters) {
            out.println("CLEAR");
            out.println("Closing connection...");
        }
        for (PrintWriter out : clientWriters) {
            clientWriters.remove(out);
        }
        clientsConnected = 0;
    }

    public void send2all(String input) {
        System.out.println("PLACE F / ID: " + clientID + " / clients connected: " + clientsConnected + " / State: " + state.getState());
        //Baserat på input, state, antal connected, counter, clientID
        output = protocol.processInput(input);
        //Skriv till alla 
        synchronized (clientWriters) {
            for (PrintWriter out : clientWriters) {
                out.println("CLEAR"); //Be klient cleara terminal innan varje "riktig" output  
                out.println(output);    //För att kunna skriva flera rader
            }
        }
    }

}