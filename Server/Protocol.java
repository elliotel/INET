

public class Protocol {
    String state;
    int clientsConnected;
    int clientCounter;
    int clientID;
    String output;
    String in;

    public String processInput(String in, String state, int clientsConnected, int clientCounter, int clientID){
        this.state = state;
        this.clientsConnected = clientsConnected;
        this.clientCounter = clientCounter;
        this.clientID = clientID;
        this.in = in;


        switch (state){
            case "WAITING":
                return waiting();
            case "READY":
                if(in == "UP"){
                    return "HEJ";
                    //return running();
                    //VARFÖR FUNKAR DETTA INTE?!?!?!?!?
                } if(in == null) {
                    return ready();
                }
                break;
            case "RUNNING":
                if(in == "q"){
                    state = "WAITING";
                    return waiting();
                } else {
                    return running();   
                }
            default:
                return "detta ska inte ske";
        }
    }

    private String waiting() {
        output = "Client counter: " + clientCounter + "\n" 
                + "Clients connected = " + clientsConnected + "\n\n\n"
                + "Waiting for another player to connect... "
                + in + " state: " + state;
        return output;
    }

    private String ready() {
        output = clientsConnected + " Clients connected \n\n\n" 
                + "Press [enter] to play!" + " in: " + in + " state: " + state;
        return output;
    }

    private String running() {
        output = "NU ÄR SPELET IGÅNG! input: " + in; 
        return output;
    }
    
}
