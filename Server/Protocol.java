

public class Protocol {
    private ServerThread thread;
    private String output;
    private Game game;

    public Protocol(ServerThread thread) {
        this.thread = thread;
        game = null;
    }
    

    public String processInput(String input){
        System.out.println("Client " + thread.clientID + ": " + input + " STATE: " + thread.state.getState());
        switch (thread.state.getState()){
            case "WAITING":
            /*
                if (game != null && input != "QUIT") {
                    System.out.println("ID: " + thread.clientID + " RESETTING");
                    game.resetGame();
                    game = null;
                    ServerThread.clientsConnected++;
                    System.out.println("PLACE A / ID: " + thread.clientID + " / clients connected: " + thread.clientsConnected + " / State: " + thread.state.getState());
                }
            */
                if (ServerThread.clientsConnected == 1) { //Vet att den kommer vara 1 annars ska den inte funka
                    return waiting();
                }
                else {                              //Nu är 2 connected
                    synchronized (Server.class) {
                        thread.state.setState("READY");
                    }
                    return ready();
                }
            case "READY":
                if (input != null && input.equals("ENTER")) {
                    synchronized (thread.state) {
                        thread.state.setState("RUNNING");
                    }
                    return running(input);                              //Någon tryckte Enter
                } else if(input != null && input.equals("QUIT")){ 
                    synchronized (thread.state) {                       
                        thread.state.setState("WAITING");
                    }                                                   //Oavsett om man är 1 eller 2 connected
                    return waiting();                                   //Gå direkt till Waiting från Ready
                }
                return ready();
            case "RUNNING":
                    if(input != null && input.equals("QUIT")){
                        game.resetGame();
                        //game = null;
                        synchronized (thread.state) {                   
                            thread.state.setState("RESTART");
                        }
                        return restart();
                    }else {
                        return running(input); 
                    }
            case "VICTORY":
                    if (game != null) {
                        game.resetGame();
                        game = null;
                    }
                    if (input == null || !input.equals("ENTER")) {
                        return victory();
                    }
                    else {
                        thread.state.setState("WAITING");  
                        thread.quitAll();    
                    }
            case "RESTART":
            if (game != null) {
                game.resetGame();
                game = null;
                //thread.clientsConnected++;
                System.out.println("PLACE B / ID: " + thread.clientID + " / clients connected: " + thread.clientsConnected + " / State: " + thread.state.getState());
            }
            if (input == null || !input.equals("ENTER")) {
                return restart();
            }
            else {
                synchronized (thread.state) {
                    thread.state.setState("WAITING");
                }
                //thread.state.setState("WAITING");
                return waiting();
            }
            default:    
                throw new RuntimeException();
        }
    }

    private String waiting() {
        /* 
        if (game != null) {
            game.resetGame();
            game = null;
        }
        }*/
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
                    thread.state.setState("VICTORY");
                    return victory();
                }
            }
        }
        output = game.printBoard();
        return output;
    }

    private String victory() {
        output = "The game was won! \n\n\n" 
               + "Press [enter] to close game!";
        return output;

    }

    private String restart() {
        output = "Other client disconnected \n\n\n" 
               + "Press [enter] to return to menu!";
        return output;

    }
    
}
