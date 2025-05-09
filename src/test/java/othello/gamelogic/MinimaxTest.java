package othello.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class MinimaxTest {
    private Minimax minimax;
    private Player blackPlayer;
    private Player whitePlayer;
    private BoardSpace[][] board;

    @BeforeEach
    void setUp() {
        minimax = new Minimax();
        blackPlayer = new HumanPlayer(BoardSpace.SpaceType.BLACK);
        whitePlayer = new HumanPlayer(BoardSpace.SpaceType.WHITE);
        board = new BoardSpace[8][8];
        
        // Initialize the board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
            }
        }
        
        // Set initial piece positions
        board[3][3] = BoardSpace.getBoardSpace(3, 3, BoardSpace.SpaceType.WHITE);
        board[3][4] = BoardSpace.getBoardSpace(3, 4, BoardSpace.SpaceType.BLACK);
        board[4][3] = BoardSpace.getBoardSpace(4, 3, BoardSpace.SpaceType.BLACK);
        board[4][4] = BoardSpace.getBoardSpace(4, 4, BoardSpace.SpaceType.WHITE);
        
        blackPlayer.addOwnedSpace(board[3][4]);
        blackPlayer.addOwnedSpace(board[4][3]);
        whitePlayer.addOwnedSpace(board[3][3]);
        whitePlayer.addOwnedSpace(board[4][4]);
    }

    @Test
    void testInitialMove() {
        BoardSpace move = minimax.nextMove(board, blackPlayer, whitePlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
    }

    @Test
    void testNoAvailableMoves() {
        // Create a board state where a corner move is available
        while (!blackPlayer.getPlayerOwnedSpacesSpaces().isEmpty()) {
            blackPlayer.removeOwnedSpace(blackPlayer.getPlayerOwnedSpacesSpaces().get(0));
        }
        while (!whitePlayer.getPlayerOwnedSpacesSpaces().isEmpty()) {
            whitePlayer.removeOwnedSpace(whitePlayer.getPlayerOwnedSpacesSpaces().get(0));
        }
        // Create a board state with no valid moves
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.WHITE);
            }
        }
        
        BoardSpace move = minimax.nextMove(board, blackPlayer, whitePlayer);
        assertNull(move);
    }

    @Test
    void testDepthSetting() {
        // Test setting the search depth
        minimax.setMaxDepth(4);
        assertEquals(4, minimax.getMaxDepth());
        
        // Test invalid depth values
        assertThrows(IllegalArgumentException.class, () -> minimax.setMaxDepth(0));
        assertThrows(IllegalArgumentException.class, () -> minimax.setMaxDepth(-1));
    }

    @Test
    void testMoveConsistency() {
        // Test calling nextMove multiple times
        BoardSpace move1 = minimax.nextMove(board, blackPlayer, whitePlayer);
        BoardSpace move2 = minimax.nextMove(board, blackPlayer, whitePlayer);
        
        // Since Minimax is deterministic, it should return the same move
        assertEquals(move1, move2);
        
        // Verify validity of the move
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move1));
    }

    @Test
    void testMoveExecution() {
        BoardSpace move = minimax.nextMove(board, blackPlayer, whitePlayer);
        assertNotNull(move);
        
        // Execute the move
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        
        // Verify the state after the move
        board[move.getX()][move.getY()] = BoardSpace.getBoardSpace(move.getX(), move.getY(), blackPlayer.getColor());
        blackPlayer.addOwnedSpace(board[move.getX()][move.getY()]);
        
        assertEquals(blackPlayer.getColor(), board[move.getX()][move.getY()].getType());
        assertTrue(blackPlayer.getPlayerOwnedSpacesSpaces().contains(board[move.getX()][move.getY()]));
    }

    @Test
    void testDifferentBoardStates() {
        // Let white make a move at (5,2)
        board[5][2] = BoardSpace.getBoardSpace(5, 2, BoardSpace.SpaceType.WHITE);
        whitePlayer.addOwnedSpace(board[5][2]);
        
        // Now white has a clear best move at (2,5) which can capture 2 black pieces
        BoardSpace move = minimax.nextMove(board, whitePlayer, blackPlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = whitePlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
        
        // The best move should be at (2,5) as it can capture 2 black pieces
        assertEquals(2, move.getX());
        assertEquals(5, move.getY());
        
        // Verify the number of pieces that will be flipped
        List<BoardSpace> flips = availableMoves.get(move);
        assertTrue(flips.contains(board[5][2]));
        
        // Verify that this move is indeed the best by checking other possible moves
        for (Map.Entry<BoardSpace, List<BoardSpace>> entry : availableMoves.entrySet()) {
            if (entry.getKey() != move) {
                assertTrue(entry.getValue().size() <= flips.size(), 
                    "The chosen move should capture at least as many pieces as any other move");
            }
        }
    }

    @Test
    void testCornerMovePreference() {
        // Create a board state where a corner move is available
        while (!blackPlayer.getPlayerOwnedSpacesSpaces().isEmpty()) {
            blackPlayer.removeOwnedSpace(blackPlayer.getPlayerOwnedSpacesSpaces().get(0));
        }
        while (!whitePlayer.getPlayerOwnedSpacesSpaces().isEmpty()) {
            whitePlayer.removeOwnedSpace(whitePlayer.getPlayerOwnedSpacesSpaces().get(0));
        }
        assertEquals(0, whitePlayer.getPlayerOwnedSpacesSpaces().size());
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0 && j == 1) {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.BLACK);
                    blackPlayer.addOwnedSpace(board[i][j]);
                } else if (i == 1 && j == 0) {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.BLACK);
                    blackPlayer.addOwnedSpace(board[i][j]);
                } else if (i == 2 && j == 0) {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.WHITE);
                    whitePlayer.addOwnedSpace(board[i][j]);
                } else if (i == 2 && j == 1) {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.BLACK);
                    blackPlayer.addOwnedSpace(board[i][j]);
                } else {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
                }
            }
        }
        
        BoardSpace move = minimax.nextMove(board, whitePlayer, blackPlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = whitePlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
        
        // The move should be the corner (0,0)
        assertEquals(0, move.getX());
        assertEquals(0, move.getY());
    }
}