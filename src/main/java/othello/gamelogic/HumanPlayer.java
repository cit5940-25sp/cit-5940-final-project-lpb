package othello.gamelogic;

import java.util.List;
import java.util.Map;

/**
 * Represents a human player that can make decisions through GUI input.
 * Mostly blank to differentiate it from a ComputerPlayer within the GUI.
 * No need to edit this class!
 */
public class HumanPlayer extends Player {
    /**
     * Creates a new human player with the specified color
     * @param color The color (BLACK or WHITE) this player will use
     */
    public HumanPlayer(BoardSpace.SpaceType color) {
        this.setColor(color);
    }

    /**
     * Human players don't need to calculate available moves themselves
     * as this is handled by the base Player class and used by the GUI.
     */
    @Override
    public Map<BoardSpace, List<BoardSpace>> getAvailableMoves(BoardSpace[][] board) {
        // Delegate to the parent class implementation
        return super.getAvailableMoves(board);
    }
}