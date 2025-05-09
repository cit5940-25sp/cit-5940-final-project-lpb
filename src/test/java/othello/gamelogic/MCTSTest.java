package othello.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class MCTSTest {
    private MCTS mcts;
    private Player blackPlayer;
    private Player whitePlayer;
    private BoardSpace[][] board;

    @BeforeEach
    void setUp() {
        mcts = new MCTS();
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
        BoardSpace move = mcts.nextMove(board, blackPlayer, whitePlayer);
        assertNotNull(move);
        assertTrue(move.getType() == BoardSpace.SpaceType.EMPTY);
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
    }

    @Test
    void testNoAvailableMoves() {
        // Create a board state with no valid moves
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.WHITE);
                whitePlayer.addOwnedSpace(board[i][j]);
            }
        }
        blackPlayer.removeOwnedSpace(board[3][4]);
        blackPlayer.removeOwnedSpace(board[4][3]);
        
        BoardSpace move = mcts.nextMove(board, blackPlayer, whitePlayer);
        assertNull(move);
    }

    @Test
    void testIterationsSetting() {
        // Test setting the number of iterations
        mcts.setIterations(500);
        assertEquals(500, mcts.getIterations());
        
        // Test invalid iteration counts
        assertThrows(IllegalArgumentException.class, () -> mcts.setIterations(0));
        assertThrows(IllegalArgumentException.class, () -> mcts.setIterations(-1));
    }

    @Test
    void testMoveConsistency() {
        // Test calling nextMove multiple times
        BoardSpace move1 = mcts.nextMove(board, blackPlayer, whitePlayer);
        BoardSpace move2 = mcts.nextMove(board, blackPlayer, whitePlayer);
        
        // Verify validity of 2 moves
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move1));
        assertTrue(availableMoves.containsKey(move2));
    }

    @Test
    void testMoveExecution() {
        BoardSpace move = mcts.nextMove(board, blackPlayer, whitePlayer);
        assertNotNull(move);
        
        // Execute the move
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        List<BoardSpace> flips = availableMoves.get(move);
        
        // Verify the state after the move
        board[move.getX()][move.getY()] = BoardSpace.getBoardSpace(move.getX(), move.getY(), blackPlayer.getColor());
        blackPlayer.addOwnedSpace(board[move.getX()][move.getY()]);
        
        assertEquals(blackPlayer.getColor(), board[move.getX()][move.getY()].getType());
        assertTrue(blackPlayer.getPlayerOwnedSpacesSpaces().contains(board[move.getX()][move.getY()]));
    }

    @Test
    void testDifferentBoardStates() {
        // Create a board state with a black advantage
        
        // Let the black player make a move first, occupying (2,3)
        board[2][3] = BoardSpace.getBoardSpace(2, 3, BoardSpace.SpaceType.BLACK);
        blackPlayer.addOwnedSpace(board[2][3]);
        // Flip the white piece at (3,3)
        board[3][3] = BoardSpace.getBoardSpace(3, 3, BoardSpace.SpaceType.BLACK);
        blackPlayer.addOwnedSpace(board[3][3]);
        whitePlayer.removeOwnedSpace(board[3][3]);
        
        BoardSpace move = mcts.nextMove(board, whitePlayer, blackPlayer);
        assertNotNull(move);
        assertEquals(move.getType(), BoardSpace.SpaceType.EMPTY);
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = whitePlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
        assertTrue((move.getX() == 2 && move.getY() == 2) || 
                  (move.getX() == 2 && move.getY() == 4) || (move.getX() == 4 && move.getY() == 2));
        
        // Verify the rationality of the move
        List<BoardSpace> flips = availableMoves.get(move);
        assertFalse(flips.isEmpty());
        assertTrue(flips.contains(board[4][4]));
    }
} 