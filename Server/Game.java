

public class Game {

    public static final char NULLCHAR = '\u0000';

    private static char[][] board;
    private Player player;
    private static int playerCount = 0;
    private char droppedItem;
    private Boolean button;
    private static int buttonsPressed = 0;
    private Boolean escaped;
    private static int escapedPlayers = 0;

    public Game() {
        droppedItem = NULLCHAR;
        button = false;
        escaped = false;
        if (board == null) {
            setupBoard();
        }
        player = setupPlayer();
    }

    private void setupBoard() {
        board =  new char[20][40];
        spawnWalls();
        spawnPlayers();
        spawnKeys();
        spawnDoors();
        spawnButtons();
        spawnExit();
    }
    private void spawnWalls() {
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

    private void spawnPlayers() {
        board[board.length - 7][6] = '1';
        board[board.length - 7][board[0].length-7] = '2';
    }

    private void spawnKeys() {
        board[board.length - 11][14] = 'ô';
        board[board.length - 11][board[0].length-15] = 'â';
    }

    private void spawnDoors() {
        board[6][6] = 'ã';
        board[6][board[0].length - 7] = 'õ';
        for (int col = 15; col <= board[0].length - 16; col++) {
            board[6][col] = '*';
        }
    }

    private void spawnButtons() {
        board[2][3] = '©';
        board[2][board[0].length - 4] = '©';
    }

    private void spawnExit() {
        for (int col = 18; col <= board[0].length - 19; col++) {
            board[0][col] = '^';
        }
    }

    private void openDoor() {
        for (int col = 15; col <= board[0].length - 16; col++) {
            board[6][col] = ' ';
        }
    }

    public void resetGame() {
        playerCount = 0;
        board = null;
    }

    private Player setupPlayer() {
        int playerID = ++playerCount;
        int y = board.length - 7;
        int x = 6;
        if (playerID == 2) {
            x = board[0].length - 7;
        }
        return new Player(playerID, x, y);
    }

    public void dropItem() {
        if (escaped) return;
        if (button || player.getKey() == NULLCHAR) {
            return;
        }
        droppedItem = player.dropKey();
    }

    public Boolean movePlayer(String Direction) {
        if (escaped) return false;
        int nextX = player.getX();
        int nextY = player.getY();
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

        Boolean pickedUpKey = false;
        //Key pickup
        if (board[nextY][nextX] == 'ô' || board[nextY][nextX] == 'â') {
            if (player.getKey() != NULLCHAR) return false;
            else {
                player.pickupKey(board[nextY][nextX]);
                pickedUpKey = true;
            }
        }

        Boolean openedDoor = false;
        //Door opening
        if (board[nextY][nextX] == 'õ' || board[nextY][nextX] == 'ã') {
            switch (player.getKey()) {
                case NULLCHAR:
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

        if (board[nextY][nextX] == '^') {
            board[player.getY()][player.getX()] = ' ';
            escaped = true;
            escapedPlayers++;
            if (escapedPlayers == 2) {
                return true;
            }
        }

        Boolean nextButton = false;
        //Button
        if (board[nextY][nextX] == '©') {
            nextButton = true;
        }

        //Movement
        if (board[nextY][nextX] == ' ' || pickedUpKey || openedDoor || nextButton) {
            board[nextY][nextX] = (char) (player.getID() + '0');

            //Leave behind dropped key
            if (button) {
                board[player.getY()][player.getX()] = '©';
                buttonsPressed--;
                button = false;
            }
            else if (droppedItem == NULLCHAR) {
                board[player.getY()][player.getX()] = ' ';
            }
            else {
                board[player.getY()][player.getX()] = droppedItem;
                droppedItem = NULLCHAR;
            }

            if (nextButton) {
                button = true;
                buttonsPressed++;
                if (buttonsPressed == 2) {
                    openDoor();
                }
            }

            player.setX(nextX);
            player.setY(nextY);
        }
        return false;
    }

    public String printBoard() {
        StringBuilder sb = new StringBuilder(board.length * board[0].length);
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                sb.append(board[row][col]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

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

    public char dropKey() {
        char drop = getKey();
        key = Game.NULLCHAR;
        return drop;
    }
}