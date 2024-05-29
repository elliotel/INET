import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;

public class ServerThread extends Thread {
    private static int clientsConnected = 0; 
    private static int clientCounter = 0;       //Static counter för att ge varje klient unikt ID
    private Socket socket = null;
    private PrintWriter out;
    private Set<PrintWriter> clientWriters;
    private int clientID;                       //unikt ID för varje klient
    private static String state = "WAITING";
    private String output;
    private Protocol protocol;

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
            protocol = new Protocol();

            synchronized (ServerThread.class) {
                clientWriters.add(out);
                clientsConnected++;
                if(clientsConnected == 2){
                    state = "READY";
                }
                send2all(null);
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("q")) {
                    //Skriv endast till klienten som stränger connection
                    out.println("Closing connection...");
                    send2all(inputLine);
                    break;
                } else {
                    send2all(inputLine);
                }
            }

            //Klienten har tryckt ctrl-c
            synchronized (ServerThread.class) {
                quit();
            }

            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Client: " + clientID + " disconnected");
        }
    }

    private void quit() {
        //Ta bort klienten ur listan
        clientWriters.remove(out);
        clientsConnected--;
        //Ändra state till waiting
        if(clientsConnected == 1){
            state = "WAITING";
        }
        //Hämta meddelande att skicka till alla (kommer vara 1 klient)
        send2all(null);
    }

    public void send2all(String in) {
        //Baserat på input, state, antal connected, counter, clientID
        output = protocol.processInput(in, state, clientsConnected, clientCounter, clientID);
        //Skriv till alla 
        synchronized (clientWriters) {
            for (PrintWriter out : clientWriters) {
                out.println("CLEAR"); //Be klient cleara terminal innan varje "riktig" output  
                out.println(output);    //För att kunna skriva flera rader
            }
        }
    }

}