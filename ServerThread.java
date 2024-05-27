import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;

public class ServerThread extends Thread {
    private static int clientCount = 0; // Static så att värdet delas över alla threads
    private Socket socket = null;
    private PrintWriter out;
    private Set<PrintWriter> clientWriters;

    public ServerThread(Socket socket, Set<PrintWriter> clientWriters) {
        this.socket = socket;
        this.clientWriters = clientWriters;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            synchronized (ST.class) {
                clientWriters.add(out);
                clientCount++;
                sendClientCountMessage();
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("q")) {
                    out.println("Closing connection...");
                    break;
                } if(inputLine.equals("t")){
                    send2all("Skickar till alla");
                } else {
                    //out.println("Received: " + inputLine);
                    sendClientCountMessage();
                }
            }

            synchronized (ST.class) {
                clientWriters.remove(out);
                clientCount--;
                sendClientCountMessage();
            }

            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Spelare Disconnected");
        }
    }

    public void send2all(String s) {
        synchronized (clientWriters) {
            for (PrintWriter client : clientWriters) {
                client.println(s);
            }
        }
    }

    private void sendClientCountMessage() {
        synchronized (clientWriters) {
            for (PrintWriter client : clientWriters) {
                client.println("Client count: " + clientCount);
                client.println(" Waiting for another player to connect...");
                if (clientCount == 2){
                    client.println("2 Players connected");
                    client.println("Press [enter] to play!");
                }
            }
        }
    }
}