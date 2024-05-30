

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
        switch (ServerThread.state){
            case "WAITING":
                if (game != null && input != "q") {
                    System.out.println("ID: " + thread.clientID + " RESETTING");
                    game.resetGame();
                    game = null;
                    ServerThread.clientsConnected++;
                    System.out.println("PLACE A" + thread.clientID + ": " + ServerThread.clientsConnected + " - " + ServerThread.state);
                }
                if (ServerThread.clientsConnected < 2) {
                return waiting();
                }
                else {
                    ServerThread.state = "READY";
                    return ready();
                }
            case "READY":
                if (input != null && input.equals("ENTER")) {
                    ServerThread.state = "RUNNING";
                    return running(input);
                }
                return ready();
            case "RUNNING":
                    return running(input);   
            case "VICTORY":
            if (game != null) {
                game.resetGame();
                game = null;
            }
            if (input == null || !input.equals("ENTER")) {
                return victory();
            }
            else {
                ServerThread.state = "WAITING";
                return waiting();
            }
            case "RESTART":
            if (game != null && input != "q") {
                game.resetGame();
                game = null;
                ServerThread.clientsConnected++;
                System.out.println("PLACE B" + thread.clientID + ": " + ServerThread.clientsConnected + " - " + ServerThread.state);
            }
            if (input == null || !input.equals("ENTER")) {
                return restart();
            }
            else {
                ServerThread.state = "WAITING";
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
        output = "Client counter: " + ServerThread.clientCounter + "\n" 
                + "Clients connected = " + ServerThread.clientsConnected + "\n\n\n"
                + "Waiting for another player to connect... ";
        return output;
    }

    private String ready() {
        output = ServerThread.clientsConnected + " Clients connected \n\n\n" 
               + "Press [enter] to play!";
        return output;
    }

    private String running(String input) {
        if (game == null) {
            System.out.println("NULL - Launching new game for clientID: " + thread.clientID);
            game = new Game();
        }
        if (input != null && !input.equals("ENTER")) {
            if (input.equals("DROP")) {
                game.dropItem();
            }
            else {
                //Returns true if game was won
                if (game.movePlayer(input)) {
                    ServerThread.state = "VICTORY";  
                    return victory();
                }
            }
        }
        output = game.printBoard();
        return output;
    }

    private String victory() {
        output = "The game was won! \n\n\n" 
               + "Press [enter] to return to menu!";
        return output;

    }

    private String restart() {
        output = "Other client disconnected \n\n\n" 
               + "Press [enter] to return to menu!";
        return output;

    }
    
}
