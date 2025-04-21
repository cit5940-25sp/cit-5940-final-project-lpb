package othello.gamelogic;

import java.util.*;

/**
 * Models a board of Othello.
 * Includes methods to get available moves and take spaces.
 */
public class OthelloGame {
    public static final int GAME_BOARD_SIZE = 8;

    private BoardSpace[][] board;
    private final Player playerOne;
    private final Player playerTwo;

    public OthelloGame(Player playerOne, Player playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        initBoard();
        // Set up initial board configuration
        board[3][3].setType(BoardSpace.SpaceType.WHITE);
        board[3][4].setType(BoardSpace.SpaceType.BLACK);
        board[4][3].setType(BoardSpace.SpaceType.BLACK);
        board[4][4].setType(BoardSpace.SpaceType.WHITE);

        // Initialize player owned spaces
        playerOne.addOwnedSpace(board[3][4]);
        playerOne.addOwnedSpace(board[4][3]);
        playerTwo.addOwnedSpace(board[3][3]);
        playerTwo.addOwnedSpace(board[4][4]);
    }

    public BoardSpace[][] getBoard() {

        return board;
    }

    public Player getPlayerOne() {

        return playerOne;
    }

    public Player getPlayerTwo() {

        return  playerTwo;
    }

    /**
     * Returns the available moves for a player.
     * Used by the GUI to get available moves each turn.
     * @param player player to get moves for
     * @return the map of available moves,that maps destination to list of origins
     */
    public Map<BoardSpace, List<BoardSpace>> getAvailableMoves(Player player) {
        return player.getAvailableMoves(board);
    }

    /**
     * Initializes the board at the start of the game with all EMPTY spaces.
     */
    public void initBoard() {
        board = new BoardSpace[GAME_BOARD_SIZE][GAME_BOARD_SIZE];
        for (int i = 0; i < GAME_BOARD_SIZE; i++) {
            for (int j = 0; j < GAME_BOARD_SIZE; j++) {
                board[i][j] = new BoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
            }
        }
    }

    /**
     * PART 1
     * TODO: Implement this method
     * Claims the specified space for the acting player.
     * Should also check if the space being taken is already owned by the acting player,
     * should not claim anything if acting player already owns space at (x,y)
     * @param actingPlayer the player that will claim the space at (x,y)
     * @param opponent the opposing player, will lose a space if their space is at (x,y)
     * @param x the x-coordinate of the space to claim
     * @param y the y-coordinate of the space to claim
     */
    public void takeSpace(Player actingPlayer, Player opponent, int x, int y) {
        BoardSpace space = board[x][y];

        // Only process if the space is empty or belongs to opponent
        if (space.getType() != actingPlayer.getColor()) {
            // Remove from opponent if it was their space
            if (space.getType() == opponent.getColor()) {
                opponent.removeOwnedSpace(space);
            }

            // Set new owner and add to player's owned spaces
            space.setType(actingPlayer.getColor());
            actingPlayer.addOwnedSpace(space);
        }
    }

    /**
     * PART 1
     * TODO: Implement this method
     * Claims spaces from all origins that lead to a specified destination.
     * This is called when a player, human or computer, selects a valid destination.
     * @param actingPlayer the player that will claim spaces
     * @param opponent the opposing player, that may lose spaces
     * @param availableMoves map of the available moves, that maps destination to list of origins
     * @param selectedDestination the specific destination that a HUMAN player selected
     */
    public void takeSpaces(Player actingPlayer, Player opponent,
                           Map<BoardSpace, List<BoardSpace>> availableMoves, BoardSpace selectedDestination) {
        // First take the destination space
        takeSpace(actingPlayer, opponent, selectedDestination.getX(), selectedDestination.getY());

        // Get all origin spaces that lead to this destination
        List<BoardSpace> origins = availableMoves.get(selectedDestination);
        if (origins != null) {
            for (BoardSpace origin : origins) {
                // Flip all pieces between origin and destination
                flipPiecesBetween(origin, selectedDestination, actingPlayer, opponent);
            }
        }
    }
    /**
     * Helper method to flip all pieces between two spaces
     */
    private void flipPiecesBetween(BoardSpace start, BoardSpace end,
                                   Player actingPlayer, Player opponent) {
        int x1 = start.getX();
        int y1 = start.getY();
        int x2 = end.getX();
        int y2 = end.getY();

        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);

        int x = x1 + dx;
        int y = y1 + dy;

        // Move along the line between start and end
        while (x != x2 || y != y2) {
            BoardSpace current = board[x][y];

            // Flip the piece
            current.setType(actingPlayer.getColor());
            actingPlayer.addOwnedSpace(current);
            opponent.removeOwnedSpace(current);

            x += dx;
            y += dy;
        }
    }

    /**
     * PART 2
     * TODO: Implement this method
     * Gets the computer decision for its turn.
     * Should call a method within the ComputerPlayer class that returns a BoardSpace using a specific strategy.
     * @param computer computer player that is deciding their move for their turn
     * @return the BoardSpace that was decided upon
     */
    public BoardSpace computerDecision(ComputerPlayer computer) {

        return computer.makeMove(board);
    }

}