import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

public class ServerThread extends Thread {
    public static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>()); // För att skriva till alla klienter samtidigt
    protected static int clientsConnected = 0;    // rör sig mellan 0 och 2
    protected static int clientCounter = 0;       //Static counter för att ge varje klient unikt ID, har ingen övre gräns
    private Socket socket = null;
    private PrintWriter out;
    public StateHolder state;
    protected int clientID;                       //unikt ID för varje klient
    private String output;
    private Protocol protocol;

    public ServerThread(Socket socket, StateHolder state) {
        this.state = state;
        this.socket = socket;
        protocol = new Protocol(this);

        // Varje gång tråd startar
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
                System.out.println("Increasing clients to " + clientsConnected);
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

            if(inputLine == null){
                System.out.println("Client: " + clientID + " disconnected");
            }

            //Klienten har tryckt ctrl-c ELLER 'q'
            synchronized (ServerThread.class) {
                if (clientsConnected > 0){
                    quit();
                }
                
            }

            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
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
    }

    public void quitAll() {
        synchronized (ServerThread.class) {
            clientsConnected = 0;
        }
        synchronized (clientWriters) {
            
            
            for (PrintWriter out : clientWriters) {
                System.out.println("Closing connection for ID: " + clientID);
                out.println("CLEAR");
                out.println("Closing connection...");
            }
            for (PrintWriter out : clientWriters) {
                clientWriters.remove(out);
            }
        }
    }

    public void send2all(String input) {
        //Baserat på input, state, antal connected, counter, clientID
        output = protocol.processInput(input);
        if(output.equals("quitall")){
            quitAll();
        } else {
            //Skriv till alla 
            synchronized (clientWriters) {
                for (PrintWriter out : clientWriters) {
                    out.println("CLEAR"); //Be klient cleara terminal innan varje "riktig" output  
                    out.println(output);    //För att kunna skriva flera rader
                }
            }
        }
    }

}