package othello.gamelogic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ComputerPlayer} class.
 * These tests validate the behavior of strategy-related methods and move-making functionality.
 */
class ComputerPlayerTest {

    @Test
    void testGetStrategyName() {
        ComputerPlayer actual = new ComputerPlayer("minimax");
        assertEquals("minimax", actual.getStrategyName(), "getStrategyName should return the correct strategy name.");
        actual = new ComputerPlayer("custom");
        assertEquals("custom", actual.getStrategyName(), "getStrategyName should return the correct strategy name.");
        actual = new ComputerPlayer("MCTS");
        assertEquals("mcts", actual.getStrategyName(), "getStrategyName should return the correct strategy name.");
        assertThrows(IllegalArgumentException.class, () -> new ComputerPlayer("expectimax"));
    }

    @Test
    void makeMove() {
        BoardSpace[][] mockBoard = new BoardSpace[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                mockBoard[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
            }
        }
        Player mockOpponent = new HumanPlayer(BoardSpace.SpaceType.WHITE);
        ComputerPlayer player = new ComputerPlayer("custom");
        player.setColor(BoardSpace.SpaceType.BLACK);
        mockBoard[3][3] = BoardSpace.getBoardSpace(3, 3, BoardSpace.SpaceType.BLACK);
        player.addOwnedSpace(mockBoard[3][3]);
        mockBoard[3][4] = BoardSpace.getBoardSpace(3, 4, BoardSpace.SpaceType.WHITE);
        mockOpponent.addOwnedSpace(mockBoard[3][4]);

        BoardSpace move = player.makeMove(mockBoard, mockOpponent);
        assertNotNull(move, "Move should not return null.");
        assertEquals(3, move.getX());
        assertEquals(5, move.getY());
    }
}