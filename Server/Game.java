

public class Game {

    private static char[][] board;
    private Player player;
    private static int playerCount = 0;

    public Game() {

        if (board == null) {
            setupBoard();
        }
        player = spawnPlayer();
    }

    private void setupBoard() {
        board =  new char[20][40];
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (row == 0 || row == board.length - 1 || col == 0 || col == board[0].length - 1) {
                    board[row][col] = '#';
                }
                else {
                    board[row][col] = ' ';
                }
            }
        }
    }

    public void resetGame() {
        playerCount = 0;
        board = null;
    }

    private Player spawnPlayer() {
        int playerID = ++playerCount;
        int y = board.length - 6;
        int x = 6;
        if (playerID == 2) {
            x = board[0].length - 6;
        }
        if (board[y][x] != ' ') {
            throw new RuntimeException();
        }
        board[y][x] = (char) (playerID + '0');
        return new Player(playerID, x, y);
    }

    public void movePlayer(String Direction) {
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
        if (board[nextY][nextX] == ' ') {
            board[nextY][nextX] = (char) (player.getID() + '0');
            board[player.getY()][player.getX()] = ' ';
            player.setX(nextX);
            player.setY(nextY);
        }
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

    public Player(int playerID, int x, int y) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
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

}