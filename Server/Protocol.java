

public class Protocol {
    private ServerThread thread;
    private String output;
    private Game game;

    public Protocol(ServerThread thread) {
        this.thread = thread;
        game = null;
    }
    

    public String processInput(String input){
        System.out.println("Client " + thread.clientID + ": " + input);
        switch (thread.state){
            case "WAITING":
                if (game != null && input != "q") {
                    System.out.println("ID: " + thread.clientID + " RESETTING");
                    game.resetGame();
                    game = null;
                    thread.clientsConnected++;
                    System.out.println("PLACE A" + thread.clientID + ": " + thread.clientsConnected + " - " + thread.state);
                }
                if (thread.clientsConnected < 2) {
                return waiting();
                }
                else {
                    thread.state = "READY";
                    return ready();
                }
            case "READY":
                if (input != null && input.equals("ENTER")) {
                    thread.state = "RUNNING";
                    return running(input);
                }
                return ready();
            case "RUNNING":
                    return running(input);   
            case "RESTART":
            if (game != null && input != "q") {
                game.resetGame();
                game = null;
                thread.clientsConnected++;
                System.out.println("PLACE B" + thread.clientID + ": " + thread.clientsConnected + " - " + thread.state);
            }
            if (input == null || !input.equals("ENTER")) {
                return restart();
            }
            else {
                thread.state = "WAITING";
                return waiting();
            }
            default:    
                throw new RuntimeException();
        }
    }

    private String waiting() {
        if (game != null) {
            game.resetGame();
            game = null;
        }
        output = "Client counter: " + thread.clientCounter + "\n" 
                + "Clients connected = " + thread.clientsConnected + "\n\n\n"
                + "Waiting for another player to connect... ";
        return output;
    }

    private String ready() {
        output = thread.clientsConnected + " Clients connected \n\n\n" 
               + "Press [enter] to play!";
        return output;
    }

    private String running(String input) {
        if (game == null) {
            System.out.println("NULL - Launching new game for clientID: " + thread.clientID);
            game = new Game();
        }
        if (input != null && !input.equals("ENTER")) {
            game.movePlayer(input);
        }
        output = game.printBoard();
        return output;
    }

    private String restart() {
        output = "Other client disconnected \n\n\n" 
               + "Press [enter] to return to menu!";
        return output;

    }
    
}
