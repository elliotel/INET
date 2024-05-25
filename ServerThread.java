import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ServerThread extends Thread{

    static int portNumber = 4444;

    //Static så att värdet delas över alla threads
    private static int counter = 0;

    private Socket socket = null;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String inputLine, outputLine;
            out.println("Connected over port " + portNumber + "!");
            //Tar in input från klienten
            while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("")) {
                        out.println("Closing connection...");
                        break;
                    }
                    //Gör uppdatering av counter thread-safe
                    synchronized (ServerThread.class) {
                    counter++;
                    outputLine = "Received: " + inputLine + " -- Count: " + counter;
                    }
                    //Skickar tillbaka svaret till klienten
                    out.println(outputLine);
                    
            }
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
