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

    /**
     * Constructs a new OthelloGame with two players and sets up the initial board state.
     * Initializes the four central pieces according to Othello rules and assigns
     * ownership to each player.
     *
     * @param playerOne the first player (usually black)
     * @param playerTwo the second player (usually white)
     */
    public OthelloGame(Player playerOne, Player playerTwo) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        initBoard();
        // Set up initial board configuration
        board[3][3] = BoardSpace.getBoardSpace(3, 3, BoardSpace.SpaceType.WHITE);
        board[3][4] = BoardSpace.getBoardSpace(3, 4, BoardSpace.SpaceType.BLACK);
        board[4][3] = BoardSpace.getBoardSpace(4, 3, BoardSpace.SpaceType.BLACK);
        board[4][4] = BoardSpace.getBoardSpace(4, 4, BoardSpace.SpaceType.WHITE);

        // Initialize player owned spaces
        playerOne.addOwnedSpace(board[3][4]);
        playerOne.addOwnedSpace(board[4][3]);
        playerTwo.addOwnedSpace(board[3][3]);
        playerTwo.addOwnedSpace(board[4][4]);
    }

    /**
     * Returns the current game board as a 2D array of BoardSpace.
     *
     * @return the game board
     */
    public BoardSpace[][] getBoard() {

        return board;
    }

    /**
     * Returns the first player.
     *
     * @return player one
     */
    public Player getPlayerOne() {

        return playerOne;
    }

    /**
     * Returns the second player.
     *
     * @return player two
     */
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
                // Use Flyweight factory to get shared instances
                board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
            }
        }
    }

    /**
     * PART 1
     * Claims the specified space for the acting player.
     * Should also check if the space being taken is already owned by the acting player,
     * should not claim anything if acting player already owns space at (x,y)
     * @param actingPlayer the player that will claim the space at (x,y)
     * @param opponent the opposing player, will lose a space if their space is at (x,y)
     * @param x the x-coordinate of the space to claim
     * @param y the y-coordinate of the space to claim
     */
    public void takeSpace(Player actingPlayer, Player opponent, int x, int y) {

        BoardSpace space = board[x][y]; // Get current state of the space

        // Safety checks
        if (actingPlayer == null || opponent == null || space == null) {
            System.err.println("ERROR in takeSpace: Null player or space object provided for (" + x + "," + y + ")");
            return;
        }

        // --- Original DEBUG ---
        // System.out.println("--- Starting takeSpace(" + x + "," + y + ") for " + actingPlayer.getColor() + " ---");
        // int initialActingPlayerSize = actingPlayer.getPlayerOwnedSpacesSpaces().size(); // Record initial size

        // Only process if the space is empty or belongs to opponent
        if (space.getType() != actingPlayer.getColor()) {
            // Remove from opponent if it was their space
            if (space.getType() == opponent.getColor()) {
                opponent.removeOwnedSpace(space);
            }

            // Get the flyweight instance for the new state (acting player's color)
            BoardSpace newSpace = BoardSpace.getBoardSpace(x, y, actingPlayer.getColor());
            // Update the board to point to this instance
            board[x][y] = newSpace;

            // Add the new instance to the acting player's owned list
            actingPlayer.addOwnedSpace(newSpace); // This calls the method in Player.java

        } else {
            // Code here executes if the space *already* belongs to the acting player
            System.out.println(">>> takeSpace: Condition FALSE (space type " + space.getType() + " == player color " + actingPlayer.getColor() + "). Skipping add/remove."); // DEBUG
        }
    }


    /**
     * PART 1
     * Claims spaces from all origins that lead to a specified destination.
     * This is called when a player, human or computer, selects a valid destination.
     * @param actingPlayer the player that will claim spaces
     * @param opponent the opposing player, that may lose spaces
     * @param availableMoves map of the available moves, that maps destination to list of origins
     * @param selectedDestination the specific destination that a HUMAN player selected
     */
    public void takeSpaces(Player actingPlayer, Player opponent,
                           Map<BoardSpace, List<BoardSpace>> availableMoves, BoardSpace selectedDestination) {

        // --- DEBUG: Log info just before calling takeSpace ---
        if (selectedDestination != null) {
            int destX = selectedDestination.getX();
            int destY = selectedDestination.getY();
            if (destX >= 0 && destX < GAME_BOARD_SIZE && destY >= 0 && destY < GAME_BOARD_SIZE && board[destX][destY] != null) {
            } else {
                System.err.println("!!! takeSpaces: Invalid coordinates or null board space for selectedDestination (" + destX + "," + destY + ")");
            }
        } else {
            System.err.println("!!! takeSpaces: selectedDestination is NULL!");
            return; // Cannot proceed if destination is null
        }
        // --- End DEBUG ---

        // First take the destination space
        takeSpace(actingPlayer, opponent, selectedDestination.getX(), selectedDestination.getY());

        // Get all origin spaces that lead to this destination
        List<BoardSpace> origins = availableMoves.get(selectedDestination);
        if (origins != null) {
            for (BoardSpace origin : origins) {
                if (origin == null) { // Safety check
                    System.err.println("!!! takeSpaces: Found null origin in list for destination (" + selectedDestination.getX() + "," + selectedDestination.getY() + ")");
                    continue;
                }
                // Flip all pieces between origin and destination
                flipPiecesBetween(origin, selectedDestination, actingPlayer, opponent);
            }
        } else {
            // This might happen if availableMoves map is inconsistent, log it.
            System.err.println("!!! takeSpaces: No origins found in availableMoves map for selectedDestination (" + selectedDestination.getX() + "," + selectedDestination.getY() + ")");
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

            if (current.getType() == opponent.getColor()) {
                // Get the flyweight instance for the new state
                BoardSpace currentNew = BoardSpace.getBoardSpace(x, y, actingPlayer.getColor());
                // Update the board reference
                board[x][y] = currentNew;

                // Update ownership lists
                opponent.removeOwnedSpace(current);
                actingPlayer.addOwnedSpace(currentNew);
            } else {
                System.err.println("Warning: Unexpected piece type at (" + x + "," + y + ") in flipPiecesBetween.");
                break;
            }

            x += dx;
            y += dy;
        }
    }

    /**
     * PART 2
     * Gets the computer decision for its turn.
     * Should call a method within the ComputerPlayer class that returns a BoardSpace using a specific strategy.
     * @param computer computer player that is deciding their move for their turn
     * @return the BoardSpace that was decided upon
     */
    public BoardSpace computerDecision(ComputerPlayer computer) {

        return computer.makeMove(board, playerOne == computer ? playerTwo : playerOne);
    }
}