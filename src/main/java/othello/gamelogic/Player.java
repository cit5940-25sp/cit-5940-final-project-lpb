package othello.gamelogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Ensure Objects is imported for hashCode

/**
 * Abstract Player class for representing a player within the game.
 * All types of Players have a color and a set of owned spaces on the game board.
 */
public class Player {
    /** List of board spaces currently owned by this player. */
    private final List<BoardSpace> playerOwnedSpaces = new ArrayList<>();
    /** The color associated with this player. */
    private BoardSpace.SpaceType color;

    /**
     * Returns the list of spaces currently owned by the player.
     *
     * @return a list of {@link BoardSpace} instances owned by the player.
     */
    public List<BoardSpace> getPlayerOwnedSpacesSpaces() {
        return playerOwnedSpaces;
    }

    /**
     * Sets the color for this player.
     *
     * @param color the {@link BoardSpace.SpaceType} representing this player's color.
     */
    public void setColor(BoardSpace.SpaceType color) {
        this.color = color;
    }

    /**
     * Returns the color associated with this player.
     *
     * @return this player's {@link BoardSpace.SpaceType}.
     */
    public BoardSpace.SpaceType getColor() {
        return color;
    }



    /**
     * VALIDATION METHOD: Counts pieces of this player's color directly from the board.
     * @param board The current game board state.
     * @return The actual count of pieces on the board for this player.
     */
    private int countPiecesOnBoard(BoardSpace[][] board) {
        int count = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != null && board[i][j].getType() == this.color) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * PART 1
     * Gets the available moves for this player given a certain board state.
     * This method will find destinations, empty spaces that are valid moves,
     * and map them to a list of origins that can traverse to those destinations.
     * @param board the board that will be evaluated for possible moves for this player
     * @return a map with a destination BoardSpace mapped to a List of origin BoardSpaces.
     */
    public Map<BoardSpace, List<BoardSpace>> getAvailableMoves(BoardSpace[][] board) {
        // --- START VALIDATION ---
        int actualBoardCount = countPiecesOnBoard(board);
        int listCount = playerOwnedSpaces.size();
        if (actualBoardCount != listCount) {
            System.err.println("STATE INCONSISTENCY DETECTED for Player " + this.color + " at start of getAvailableMoves!");
            System.err.println("  Count from playerOwnedSpaces list: " + listCount);
            System.err.println("  Actual count on board: " + actualBoardCount);
            // Optionally print list contents vs board contents for deeper debugging
            System.err.println("  List contains coords:");
            for(BoardSpace bs : playerOwnedSpaces) System.err.print(" ("+bs.getX()+","+bs.getY()+")");
            System.err.println();
            System.err.println("  Board contains coords:");
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] != null && board[i][j].getType() == this.color) {
                        System.err.print(" ("+i+","+j+")");
                    }
                }
            }
            System.err.println();
            // Depending on severity, you might want to throw an exception or try to recover
            // For now, we just log the error and continue
        }
        // --- END VALIDATION ---

        Map<BoardSpace, List<BoardSpace>> availableMoves = new HashMap<>();

        // Check all 8 possible directions
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        if (playerOwnedSpaces.isEmpty()) {
            // Check if board count is also 0 for consistency
            if (actualBoardCount > 0) {
                System.err.println("Warning: playerOwnedSpaces is empty but board count for " + this.color + " is " + actualBoardCount);
            }
            System.out.println("No owned spaces found in list for player " + this.getColor() + ". Cannot calculate moves.");
            return availableMoves; // Return empty map if no pieces owned in the list
        }

        // (The rest of the getAvailableMoves method remains the same...)
        for (BoardSpace space : playerOwnedSpaces) {
            // Check if space is null - safety check
            if (space == null) {
                System.err.println("ERROR: Found null BoardSpace in playerOwnedSpaces for player " + this.getColor());
                continue; // Skip this iteration
            }
            int x = space.getX();
            int y = space.getY();
            // --- DEBUG ---
            // System.out.println("Checking from origin: (" + x + "," + y + ")"); // Reduced verbosity

            // Check each direction from this piece
            for (int i = 0; i < 8; i++) {
                int currentDx = dx[i];
                int currentDy = dy[i];
                int newX = x + currentDx;
                int newY = y + currentDy;
                List<BoardSpace> flipped = new ArrayList<>(); // Stores opponent pieces in this line

                // Move in this direction
                while (newX >= 0 && newX < board.length &&
                        newY >= 0 && newY < board[0].length) {

                    // Safety check for board content
                    if (board[newX] == null || board[newX][newY] == null) {
                        System.err.println("ERROR: Board position ("+newX+","+newY+") contains null!");
                        break; // Stop searching this direction if board state is invalid
                    }

                    BoardSpace current = board[newX][newY];

                    // If empty space
                    if (current.getType() == BoardSpace.SpaceType.EMPTY) {
                        // Only valid if we jumped over opponent pieces
                        if (!flipped.isEmpty()) {
                            // --- DEBUG ---
                            // System.out.println("      VALID MOVE FOUND: Dest(" + current.getX() + "," + current.getY() + ") from Origin(" + x + "," + y + "). Flipped count: " + flipped.size());
                            // Valid move found - add to available moves
                            if (!availableMoves.containsKey(current)) {
                                availableMoves.put(current, new ArrayList<>());
                            }
                            // Add the origin piece to the list for this destination
                            // Check contains using coordinates explicitly for clarity, though equals should work
                            boolean originAlreadyAdded = false;
                            for(BoardSpace existingOrigin : availableMoves.get(current)) {
                                if (existingOrigin.getX() == space.getX() && existingOrigin.getY() == space.getY()) {
                                    originAlreadyAdded = true;
                                    break;
                                }
                            }
                            if (!originAlreadyAdded) {
                                availableMoves.get(current).add(space);
                            }
                        }
                        // Stop searching in this direction once an empty space is found
                        break;
                    }
                    // If opponent's piece, add to potential flips
                    else if (current.getType() != color) {
                        flipped.add(current);
                    }
                    // If our own piece, this line is blocked
                    else {
                        break;
                    }

                    // Move to the next space in this direction
                    newX += currentDx;
                    newY += currentDy;
                }
            }
        }

        return availableMoves;
    }


    /**
     * Adds a space to this player's owned spaces
     * @param space the space to add
     */
    public void addOwnedSpace(BoardSpace space) {
        if (space == null) {
            System.err.println("ERROR: Attempted to add null space for player " + this.color);
            return;
        }
        // Explicitly check using coordinates before adding
        boolean alreadyPresent = false;
        for (BoardSpace existingSpace : playerOwnedSpaces) {
            if (existingSpace.getX() == space.getX() && existingSpace.getY() == space.getY()) {
                alreadyPresent = true;
                break;
            }
        }

        if (!alreadyPresent) {
            playerOwnedSpaces.add(space);
        }
    }

    /**
     * Removes a space from this player's owned spaces using coordinates.
     * @param spaceToRemove the space indicating which coordinates to remove
     */
    public void removeOwnedSpace(BoardSpace spaceToRemove) {
        if (spaceToRemove == null) {
            System.err.println("ERROR: Attempted to remove null space for player " + this.color);
            return;
        }
        int indexToRemove = -1;
        // Find the index of the space with matching coordinates
        for (int i = 0; i < playerOwnedSpaces.size(); i++) {
            BoardSpace currentSpace = playerOwnedSpaces.get(i);
            if (currentSpace == null) { // Safety check inside loop
                System.err.println("ERROR: Found null space in list at index " + i + " for player " + this.color);
                continue;
            }
            // Explicitly use coordinates for matching
            if (currentSpace.getX() == spaceToRemove.getX() &&
                    currentSpace.getY() == spaceToRemove.getY()) {
                indexToRemove = i;
                break; // Found the first match
            }
        }

        // If found, remove by index
        if (indexToRemove != -1) {
            playerOwnedSpaces.remove(indexToRemove);
        }
    }

    /**
     * Creates a deep copy of this Player object.
     * @return A new Player object with the same color and owned spaces.
     */
    public Player copy() {
        Player copy;
        if (this instanceof HumanPlayer) {
            copy = new HumanPlayer(this.color);
        } else if (this instanceof ComputerPlayer) {
            copy = new ComputerPlayer(((ComputerPlayer) this).getStrategyName());
            copy.setColor(this.color);
        } else {
            throw new IllegalStateException("Unknown player type");
        }
        
        // Copy owned spaces
        for (BoardSpace space : this.playerOwnedSpaces) {
            copy.addOwnedSpace(BoardSpace.getBoardSpace(space.getX(), space.getY(), space.getType()));
        }
        
        return copy;
    }
}