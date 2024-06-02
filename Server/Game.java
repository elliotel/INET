

public class Game {

    public static final char NULLCHAR = '\u0000';

    private static char[][] board;
    private static int playerCount = 0;
    private static int buttonsPressed = 0;
    private static int escapedPlayers = 0;
    private Player player;
    private char droppedItem;
    private Boolean button;
    private Boolean escaped;

    public Game() {
        droppedItem = NULLCHAR;
        button = false;
        escaped = false;
        synchronized(ServerThread.class) {
            if (board == null) {
                setupBoard();
            }
            player = setupPlayer();
        }
    }

    //Ritar upp startläget genom att sätta respektive tecken i matrisen
    private void setupBoard() {
        spawnWalls();
        spawnPlayers();
        spawnKeys();
        spawnDoors();
        spawnButtons();
        spawnExit();
    }
    private void spawnWalls() {
        board =  new char[20][40];
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (row == 0 || row == board.length - 1 || col == 0 || col == board[0].length - 1 || row == 6 || (col == 11 || col == board[0].length - 12) && row < 6) {
                    board[row][col] = '#';
                }
                else {
                    board[row][col] = ' ';
                }
            }
        }
    }

    //Ritar upp spelarnas startposition
    private void spawnPlayers() {
        board[board.length - 7][6] = '1';
        board[board.length - 7][board[0].length-7] = '2';
    }

    //Ritar upp nycklarnas startposition
    private void spawnKeys() {
        board[board.length - 11][14] = 'ô';
        board[board.length - 11][board[0].length-15] = 'â';
    }

    //Ritar upp dörrarna
    private void spawnDoors() {
        board[6][6] = 'ã';
        board[6][board[0].length - 7] = 'õ';
        for (int col = 15; col <= board[0].length - 16; col++) {
            board[6][col] = '*';
            //board[6][col] = ' ';
        }
    }

    //Ritar upp knapparna
    private void spawnButtons() {
        board[2][3] = '©';
        board[2][board[0].length - 4] = '©';
    }

    //Ritar upp nycklarna
    private void spawnExit() {
        for (int col = 18; col <= board[0].length - 19; col++) {
            board[0][col] = '^';
        }
    }

    //Öppnar den stora dörren i mitten (körs då båda knapparna tryckts in samtidigt)
    private void openDoor() {
        for (int col = 15; col <= board[0].length - 16; col++) {
            board[6][col] = ' ';
        }
    }

    //Resettar static variabler, samt ritar upp brädet till ursprungsläge igen
    public void resetGame() {
        playerCount = 0;
        escapedPlayers = 0;
        setupBoard();
    }

    //Konfigurerar själva spelaren
    private Player setupPlayer() {
        //Sätter det tecken som spelaren ska visas som, 1 för första som connecter, 2 för nästa
        int playerID = ++playerCount;
        //Sätter koordinaterna
        int y = board.length - 7;
        int x = 6;
        //Spelare 2 börjar på annan x-koordinat än spelare 1
        if (playerID == 2) {
            x = board[0].length - 7;
        }
        return new Player(playerID, x, y);
    }

    //Droppar den nyckel spelaren håller i.
    //Själva droppet sker först då spelaren flyttat sig ifrån den rutan den klickar drop på (nyckeln lämnas då kvar där)
    //droppedItem används i movePlayer för att hålla koll på om något ska lämnas när en rörelse utförts
    public void dropItem() {
        //Om vi redan gått ur spelplanen med denna spelare händer inget
        if (escaped) return;
        //Om vi står på en knapp, eller inte har något item, så händer inget
        if (button || player.getKey() == NULLCHAR) {
            return;
        }
        //Annars håller vi koll i den (lokalt instantierade) variabeln droppedItem vad som ska droppas nästa drag
        //dropKey() tar även bort föremålet från spelaren
        droppedItem = player.dropKey();
    }

    //Flyttar spelaren, och hanterar det som spelaren "landar" på.
    public Boolean movePlayer(String Direction) {
        //Om vi redan gått ur spelplanen med denna spelare händer inget
        if (escaped) return false;
        int nextX = player.getX();
        int nextY = player.getY();
        //Beräknar var de nya koordinaterna för den planerade flytten blir
        switch (Direction) {
            case "UP":
                nextY--;
                break;
            case "LEFT":
                nextX--;
                break;
            case "DOWN":
                nextY++;
                break;
            case "RIGHT":
                nextX++;
                break;
        }

        synchronized (ServerThread.class) {
            Boolean pickedUpKey = false;
            // Sätter pickedUpKey till true om den planerade flytten skulle landa spelaren på en nyckel, 
            // och spelaren inte redan bär på en nyckel (om spelaren redan bär på en nyckel ska ingen flytt 
            // utföras, och pickedUpKey lämnas därför som false)
            if (board[nextY][nextX] == 'ô' || board[nextY][nextX] == 'â') {
                if (player.getKey() != NULLCHAR) return false;                 // Har redan nyckel
                else {
                    player.pickupKey(board[nextY][nextX]);
                    pickedUpKey = true;
                }
            }

            Boolean openedDoor = false;
            //Sätter openedDoor till true om den planerade flytten skulle landa spelaren på en dörr 
            // (de som öppnas med nycklar, inte den stora i mitten), och spelaren har matchande nyckel. 
            // Om spelaren inte har matchande nyckel lämnas den som false, se ovan.
            if (board[nextY][nextX] == 'õ' || board[nextY][nextX] == 'ã') {             // Om vi försöker stå på dörr
                switch (player.getKey()) {
                    case NULLCHAR:
                        //Om vi försöker gå in i en dörr vi inte har nyckeln till ska ingen rörelse ske. Samma effekt fås av bara en break, då openedDoor är false och rörelsedelen nedan däremed inte kommer exekveras
                        return false;
                    case 'ô':
                        if (board[nextY][nextX] == 'õ') {
                            openedDoor = true;
                            player.dropKey();
                        }
                        break;
                    case 'â':
                        if (board[nextY][nextX] == 'ã') {
                            openedDoor = true;
                            player.dropKey();
                        }
                        break;
                    default:
                        break;
                
                }
            }

            //Sätter spelaren som "escaped" om den gått till kartans utgång. Detta gör att den inte kommer kunna röra sig eller droppa items mer, samt att den försvinner från kartan.
            if (board[nextY][nextX] == '^') {
                board[player.getY()][player.getX()] = ' ';
                escaped = true;
                escapedPlayers++;
                //Om båda spelarna har gått till kartans utgång så returnerar vi true, vilket säger till Protocol att spelet har vunnits.
                if (escapedPlayers == 2) {
                    return true;
                }
            }

            Boolean nextButton = false;
            //Om rutan som den planerade flytten landar på är en knapp, så sätts nextButton till true, för att hantera det fallet.
            if (board[nextY][nextX] == '©') {
                nextButton = true;
            }

            //Utför rörelsen om nästa ruta är en tom ruta, ALTERNATIVT om det är en nyckel vi kan plocka upp, dörr vi kan öppna, eller knapp, enligt beskrivningarna ovan.
            if (board[nextY][nextX] == ' ' || pickedUpKey || openedDoor || nextButton) {
                //Sätter nya positionen till spelarens ID (1 för player 1...), "+ '0'" behövs då player.getID() returnerar en int (1 för player 1), och vi vill ha karaktären '1'.
                //char's lagras som int-värden, och då '0' har värdet 48, och de andra siffrorna efterföljer, blir tex '2' == 2 + '0' == 2 + 48 == 50. "+ '0'" kändes mest läsbart.
                board[nextY][nextX] = (char) (player.getID() + '0');

                //Om vi redan stod på en knapp, och nu lämnar den, så minskar vi totala antalet intryckta knappar
                if (button) {
                    board[player.getY()][player.getX()] = '©';
                    buttonsPressed--;
                    button = false;
                }
                //Om vi INTE har valt att droppa ett item, (och vi inte just kom från en knapp), så lämnar vi efter oss en tom ruta
                else if (droppedItem == NULLCHAR) {
                    board[player.getY()][player.getX()] = ' ';
                }
                //Och annars, så betyder det att vi droppat ett item (och inte kom från en knapp), och då lämnar vi efter oss det item:et.
                else {
                    board[player.getY()][player.getX()] = droppedItem;
                    droppedItem = NULLCHAR;
                }

                //Om den pågående rörelsen kommer att ställa oss på en knapp (beräknat ovan), så ökar vi buttonsPressed med 1, samt sparar att spelaren står på en knapp.
                if (nextButton) {
                    button = true;
                    buttonsPressed++;
                    //Om båda knapparna nu är intryckta så öppnar vi den stora dörren
                    if (buttonsPressed == 2) {
                        openDoor();
                    }
                }
                //Sist men inte minst uppdaterar vi spelarens koordinater (i objektet, inte på visualiseringen)
                player.setX(nextX);
                player.setY(nextY);
            }
        }
        //Returnerar false eftersom spelet inte vunnits än
        return false;
    }

    //Bygger upp en stor string av hela matrisen via en stringbuilder, för att skicka till klienten. Går igenom tecken för tecken och lägger till newlines i slutet på varje rad.
    public String printBoard() {
        synchronized (ServerThread.class) {
            StringBuilder sb = new StringBuilder(board.length * board[0].length);
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[0].length; col++) {
                    sb.append(board[row][col]);
                }
                sb.append('\n'); // för varje ny row - gör ny rad
            }
            return sb.toString();
        }
    }
}

//Spelarklassen
class Player {
    private int playerID;
    private int x;
    private int y;
    
    private char key;

    public Player(int playerID, int x, int y) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
        key = Game.NULLCHAR;
    }

    public int getID() {
        return playerID;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public char getKey() {
        return key;
    }

    public void pickupKey(char pickup) {
        key = pickup;
    }

    //Tar bort nyckeln spelaren håller i och returnerar den
    public char dropKey() {
        char drop = getKey();
        key = Game.NULLCHAR;
        return drop;
    }
}