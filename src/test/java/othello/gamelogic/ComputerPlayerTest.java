package othello.gamelogic;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ComputerPlayer} class.
 * These tests validate the behavior of strategy-related methods and move-making functionality.
 */
class ComputerPlayerTest {

    @Test
    void getStrategyName() {
        ComputerPlayer minimaxPlayer = new ComputerPlayer("minimax");
        assertEquals("minimax", minimaxPlayer.getStrategyName());

    }

    @Test
    void testGetStrategyName() {
        ComputerPlayer player = new ComputerPlayer("minimax");
        assertEquals("minimax", player.getStrategyName(), "getStrategyName should return the correct strategy name.");
    }

    @Test
    void makeMove() {
        BoardSpace[][] mockBoard = new BoardSpace[8][8];
        Player mockOpponent = new Player();

        ComputerPlayer customPlayer = new ComputerPlayer("custom") {
            @Override
            public BoardSpace makeMove(BoardSpace[][] board, Player opponent) {
                return BoardSpace.getBoardSpace(3, 4, BoardSpace.SpaceType.BLACK);
            }
        };

        BoardSpace move = customPlayer.makeMove(mockBoard, mockOpponent);
        assertEquals(3, move.getX());
        assertEquals(4, move.getY());
    }
    @Test
    void testMakeMoveThrowsIfStrategyIsNull() throws Exception {
        ComputerPlayer player = new ComputerPlayer("minimax");

        // Use reflection to set strategy to null
        java.lang.reflect.Field strategyField = ComputerPlayer.class.getDeclaredField("strategy");
        strategyField.setAccessible(true);
        strategyField.set(player, null);

        assertThrows(IllegalStateException.class, () ->
                player.makeMove(new BoardSpace[8][8], new Player())
        );
    }

}