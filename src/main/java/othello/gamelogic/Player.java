package othello.gamelogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract Player class for representing a player within the game.
 * All types of Players have a color and a set of owned spaces on the game board.
 */
public abstract class Player {
    private final List<BoardSpace> playerOwnedSpaces = new ArrayList<>();
    public List<BoardSpace> getPlayerOwnedSpacesSpaces() {
        return playerOwnedSpaces;
    }

    private BoardSpace.SpaceType color;
    public void setColor(BoardSpace.SpaceType color) {

        this.color = color;
    }

    public BoardSpace.SpaceType getColor() {

        return color;
    }

    /**
     * PART 1
     * TODO: Implement this method
     * Gets the available moves for this player given a certain board state.
     * This method will find destinations, empty spaces that are valid moves,
     * and map them to a list of origins that can traverse to those destinations.
     * @param board the board that will be evaluated for possible moves for this player
     * @return a map with a destination BoardSpace mapped to a List of origin BoardSpaces.
     */
    public Map<BoardSpace, List<BoardSpace>> getAvailableMoves(BoardSpace[][] board) {

        Map<BoardSpace, List<BoardSpace>> availableMoves = new HashMap<>();

        // Check all 8 possible directions
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        // Find all pieces owned by this player
        for (BoardSpace space : playerOwnedSpaces) {
            int x = space.getX();
            int y = space.getY();
            System.out.println("Checking from origin: (" + x + "," + y + ")"); // 打印当前检查的起点

            // Check each direction from this piece
            for (int i = 0; i < 8; i++) {
                int newX = x + dx[i];
                int newY = y + dy[i];
                List<BoardSpace> flipped = new ArrayList<>();
                // Move in this direction until we find an empty space or edge
                while (newX >= 0 && newX < board.length &&
                        newY >= 0 && newY < board[0].length) {
                    BoardSpace current = board[newX][newY];
                    // If empty space, check if we have pieces to flip
                    if (current.getType() == BoardSpace.SpaceType.EMPTY) {
                        System.out.println("      Found EMPTY.");
                        if (!flipped.isEmpty()) {
                            // Valid move found - add to available moves
                            if (!availableMoves.containsKey(current)) {
                                availableMoves.put(current, new ArrayList<>());
                            }
                            availableMoves.get(current).add(space);
                        }
                        break;
                    }
                    // If opponent's piece, add to potential flips
                    else if (current.getType() != color) {
                        flipped.add(current);
                    }
                    // If our own piece, stop searching this direction
                    else {
                        break;
                    }

                    newX += dx[i];
                    newY += dy[i];
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
        if (!playerOwnedSpaces.contains(space)) {
            playerOwnedSpaces.add(space);
        }
    }

    /**
     * Removes a space from this player's owned spaces
     * @param space the space to remove
     */
    public void removeOwnedSpace(BoardSpace space) {
        playerOwnedSpaces.remove(space);
    }
}
