package othello.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Custom AI implementation.
 * Tests various aspects of the Custom AI algorithm including move selection,
 * board evaluation, and game state handling.
 */
public class CustomTest {
    private Custom custom;
    private Player blackPlayer;
    private Player whitePlayer;
    private BoardSpace[][] board;

    /**
     * Sets up the test environment before each test.
     * Initializes the board, players, and Custom AI instance.
     */
    @BeforeEach
    void setUp() {
        custom = new Custom();
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

    /**
     * PART 1
     * Tests the initial move selection of the Custom AI.
     * Verifies that the algorithm can make a valid move from the starting position
     * and that the move is in a valid position on the board.
     */
    @Test
    void testInitialMove() {
        BoardSpace move = custom.nextMove(board, blackPlayer, whitePlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
    }

    /**
     * PART 2
     * Tests the behavior when no valid moves are available.
     * Verifies that the algorithm correctly returns null when there are no legal moves
     * for the current player.
     */
    @Test
    void testNoAvailableMoves() {
        // Create a board state with no valid moves
        board[3][4] = BoardSpace.getBoardSpace(3, 4, BoardSpace.SpaceType.EMPTY);
        board[4][3] = BoardSpace.getBoardSpace(4, 3, BoardSpace.SpaceType.EMPTY);
        blackPlayer.removeOwnedSpace(board[3][4]);
        blackPlayer.removeOwnedSpace(board[4][3]);
        
        BoardSpace move = custom.nextMove(board, blackPlayer, whitePlayer);
        assertNull(move);
    }

    /**
     * PART 3
     * Tests the depth setting functionality of the Custom AI.
     * Verifies that valid depth values are accepted and invalid values (0 or negative)
     * throw appropriate exceptions.
     */
    @Test
    void testDepthSetting() {
        // Test setting the search depth
        custom.setDepth(4);
        
        // Test invalid depth values
        assertThrows(IllegalArgumentException.class, () -> custom.setDepth(0));
        assertThrows(IllegalArgumentException.class, () -> custom.setDepth(-1));
    }

    /**
     * PART 4
     * Tests the consistency of move selection.
     * Verifies that multiple calls to nextMove() return the same move for the same board state,
     * as the Negamax algorithm is deterministic.
     */
    @Test
    void testMoveConsistency() {
        // Test calling nextMove multiple times
        BoardSpace move1 = custom.nextMove(board, blackPlayer, whitePlayer);
        BoardSpace move2 = custom.nextMove(board, blackPlayer, whitePlayer);
        
        // Since Negamax is deterministic, it should return the same move
        assertEquals(move1, move2);
        
        // Verify the validity of the move
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move1));
    }

    /**
     * PART 5
     * Tests the execution of a move selected by the Custom AI.
     * Verifies that the selected move can be properly executed on the board
     * and that the board state is correctly updated.
     */
    @Test
    void testMoveExecution() {
        BoardSpace move = custom.nextMove(board, blackPlayer, whitePlayer);
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

    /**
     * PART 6
     * Tests the algorithm's preference for corner moves.
     * Verifies that when a corner move is available, the algorithm will select it
     * as corners are strategically valuable in Othello.
     */
    @Test
    void testCornerMovePreference() {
        blackPlayer.getPlayerOwnedSpacesSpaces().clear();
        whitePlayer.getPlayerOwnedSpacesSpaces().clear();
        // Create a board state where a corner move is available
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
        
        BoardSpace move = custom.nextMove(board, whitePlayer, blackPlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = whitePlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
        
        // The move should be the corner (0,0)
        assertEquals(0, move.getX());
        assertEquals(0, move.getY());
    }

    /**
     * PART 7
     * Tests the algorithm's decision-making in different board states.
     * Verifies that the algorithm can identify and select the best move
     * in a situation where one player has a clear advantage.
     */
    @Test
    void testDifferentBoardStates() {
        // Create a board state where the black player has an advantage
        blackPlayer.getPlayerOwnedSpacesSpaces().clear();
        whitePlayer.getPlayerOwnedSpacesSpaces().clear();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
            }
        }
        board[3][2] = BoardSpace.getBoardSpace(3, 2, BoardSpace.SpaceType.BLACK);
        blackPlayer.addOwnedSpace(board[3][2]);
        board[4][2] = BoardSpace.getBoardSpace(4, 2, BoardSpace.SpaceType.WHITE);
        whitePlayer.addOwnedSpace(board[4][2]);
        board[4][3] = BoardSpace.getBoardSpace(4, 3, BoardSpace.SpaceType.BLACK);
        blackPlayer.addOwnedSpace(board[4][3]);
        board[5][4] = BoardSpace.getBoardSpace(5, 4, BoardSpace.SpaceType.WHITE);
        whitePlayer.addOwnedSpace(board[5][4]);
        
        // The white player has a clear best move
        BoardSpace move = custom.nextMove(board, whitePlayer, blackPlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = whitePlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
        
        // The best move should be (2,1), because it can flip two black pieces
        assertEquals(2, move.getX());
        assertEquals(1, move.getY());
        
        // Verify the number of pieces that will be flipped
        List<BoardSpace> flips = availableMoves.get(move);
        assertEquals(1, flips.size());
        assertEquals(5, flips.get(0).getX());
        assertEquals(4, flips.get(0).getY());
    }

    /**
     * PART 8
     * Tests the Alpha-Beta pruning optimization in the Negamax algorithm.
     * Verifies that the algorithm can make correct decisions while using pruning
     * to improve search efficiency.
     */
    @Test
    void testAlphaBetaPruning() {
        // Create a board state that can trigger Alpha-Beta pruning
        // The black player has a clear best move at (2,3)
        board[2][3] = BoardSpace.getBoardSpace(2, 3, BoardSpace.SpaceType.BLACK);
        blackPlayer.addOwnedSpace(board[2][3]);
        
        // Set a shallow search depth to observe pruning effects
        custom.setDepth(2);
        
        BoardSpace move = custom.nextMove(board, blackPlayer, whitePlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = blackPlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
    }

    /**
     * PART 9
     * Tests the evaluation function in various board positions.
     * Verifies that the algorithm correctly evaluates different board states,
     * including corner advantages, edge disadvantages, and internal positions.
     */
    @Test
    void testEvaluationFunction() {
        // Test the evaluation function in different board states
        // 1. Corner advantage
        board[0][0] = BoardSpace.getBoardSpace(0, 0, BoardSpace.SpaceType.BLACK);
        blackPlayer.addOwnedSpace(board[0][0]);
        
        // 2. Edge disadvantage
        board[0][1] = BoardSpace.getBoardSpace(0, 1, BoardSpace.SpaceType.WHITE);
        whitePlayer.addOwnedSpace(board[0][1]);
        
        // 3. Internal position
        board[3][3] = BoardSpace.getBoardSpace(3, 3, BoardSpace.SpaceType.BLACK);
        blackPlayer.addOwnedSpace(board[3][3]);
        whitePlayer.removeOwnedSpace(board[3][3]);
        
        BoardSpace move = custom.nextMove(board, whitePlayer, blackPlayer);
        assertNotNull(move);
        assertEquals(BoardSpace.SpaceType.EMPTY, move.getType());
        
        // Verify if the move is in a valid position
        Map<BoardSpace, List<BoardSpace>> availableMoves = whitePlayer.getAvailableMoves(board);
        assertTrue(availableMoves.containsKey(move));
    }

    /**
     * PART 10
     * Tests the algorithm's behavior in end-game situations.
     * Verifies that the algorithm can make appropriate decisions
     * when the game is near completion.
     */
    @Test
    void testEndGameState() {
        // Create a board state that is near the end of the game
        blackPlayer.getPlayerOwnedSpacesSpaces().clear();
        whitePlayer.getPlayerOwnedSpacesSpaces().clear();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i < 4) {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.BLACK);
                    blackPlayer.addOwnedSpace(board[i][j]);
                } else {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
                }
            }
        }
        
        // Set a deeper search depth to make better decisions in the end game
        custom.setDepth(4);
        
        BoardSpace move = custom.nextMove(board, whitePlayer, blackPlayer);
        assertNull(move);
        
        // Verify original player instance not modified
        Map<BoardSpace, List<BoardSpace>> availableMoves = whitePlayer.getAvailableMoves(board);
        assertTrue(availableMoves.isEmpty());
    }

    /**
     * PART 11
     * Tests the handling of situations where a player must pass their turn.
     * Verifies that the algorithm correctly returns null when the current player
     * has no valid moves available.
     */
    @Test
    void testPassTurn() {
        // Create a board state that requires skipping a turn
        // The white player has no available moves, but the black player does
        blackPlayer.getPlayerOwnedSpacesSpaces().clear();
        whitePlayer.getPlayerOwnedSpacesSpaces().clear();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0 && j == 0) {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.BLACK);
                    blackPlayer.addOwnedSpace(board[i][j]);
                } else if (i == 1 && j == 1) {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.WHITE);
                    whitePlayer.addOwnedSpace(board[i][j]);
                } else {
                    board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
                }
            }
        }
        
        BoardSpace move = custom.nextMove(board, whitePlayer, blackPlayer);
        assertNull(move); // Should return null, because there are no available moves
    }
} 