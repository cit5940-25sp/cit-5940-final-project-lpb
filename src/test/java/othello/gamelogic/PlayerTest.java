package othello.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private Player testPlayerBlack;
    private Player testPlayerWhite;
    private BoardSpace[][] board;

    // Helper method to create an empty board
    private BoardSpace[][] createEmptyBoard() {
        BoardSpace[][] newBoard = new BoardSpace[OthelloGame.GAME_BOARD_SIZE][OthelloGame.GAME_BOARD_SIZE];
        for (int i = 0; i < OthelloGame.GAME_BOARD_SIZE; i++) {
            for (int j = 0; j < OthelloGame.GAME_BOARD_SIZE; j++) {
                newBoard[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
            }
        }
        return newBoard;
    }
    @BeforeEach
    void clearBoardSpaceCache() {
        // Clear the static cache in BoardSpace before each PlayerTest method
        try {
            Field cacheField = BoardSpace.class.getDeclaredField("boardSpaceCache");
            cacheField.setAccessible(true);
            Map<?, ?> cache = (Map<?, ?>) cacheField.get(null);
            cache.clear();
            System.out.println("--- BoardSpace cache cleared for PlayerTest ---"); // Optional: confirmation print
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Warning: Could not clear BoardSpace cache for PlayerTest isolation.");
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() {
        // Use HumanPlayer for testing Player's concrete methods
        testPlayerBlack = new HumanPlayer(BoardSpace.SpaceType.BLACK);
        testPlayerWhite = new HumanPlayer(BoardSpace.SpaceType.WHITE);
        board = createEmptyBoard();

        // Clear owned spaces before each test
        testPlayerBlack.getPlayerOwnedSpacesSpaces().clear();
        testPlayerWhite.getPlayerOwnedSpacesSpaces().clear();
    }

    @Test
    void testSetGetColor() {
        assertEquals(BoardSpace.SpaceType.BLACK, testPlayerBlack.getColor(), "Player color should be set correctly.");
        assertEquals(BoardSpace.SpaceType.WHITE, testPlayerWhite.getColor(), "Player color should be set correctly.");
    }

    @Test
    void testAddGetOwnedSpaces() {
        BoardSpace space1 = board[1][1];
        BoardSpace space2 = board[2][2];

        assertTrue(testPlayerBlack.getPlayerOwnedSpacesSpaces().isEmpty(), "Owned spaces should initially be empty.");

        testPlayerBlack.addOwnedSpace(space1);
        assertEquals(1, testPlayerBlack.getPlayerOwnedSpacesSpaces().size(), "Owned spaces size should be 1 after adding one.");
        assertTrue(testPlayerBlack.getPlayerOwnedSpacesSpaces().contains(space1), "Owned spaces should contain the added space.");

        testPlayerBlack.addOwnedSpace(space2);
        assertEquals(2, testPlayerBlack.getPlayerOwnedSpacesSpaces().size(), "Owned spaces size should be 2 after adding another.");
        assertTrue(testPlayerBlack.getPlayerOwnedSpacesSpaces().contains(space2), "Owned spaces should contain the second added space.");

        // Test adding duplicate
        testPlayerBlack.addOwnedSpace(space1);
        assertEquals(2, testPlayerBlack.getPlayerOwnedSpacesSpaces().size(), "Adding a duplicate space should not change the size.");
    }

    @Test
    void testRemoveOwnedSpace() {
        BoardSpace space1 = board[1][1];
        BoardSpace space2 = board[2][2];
        BoardSpace space3 = board[3][3]; // Not owned by player

        testPlayerBlack.addOwnedSpace(space1);
        testPlayerBlack.addOwnedSpace(space2);
        assertEquals(2, testPlayerBlack.getPlayerOwnedSpacesSpaces().size(), "Initial size before removal.");

        testPlayerBlack.removeOwnedSpace(space1);
        assertEquals(1, testPlayerBlack.getPlayerOwnedSpacesSpaces().size(), "Size should decrease after removal.");
        assertFalse(testPlayerBlack.getPlayerOwnedSpacesSpaces().contains(space1), "Removed space should no longer be present.");
        assertTrue(testPlayerBlack.getPlayerOwnedSpacesSpaces().contains(space2), "Other space should still be present.");

        // Test removing non-owned space
        testPlayerBlack.removeOwnedSpace(space3);
        assertEquals(1, testPlayerBlack.getPlayerOwnedSpacesSpaces().size(), "Removing a non-owned space should not change size.");

        // Test removing non-existent space (already removed)
        testPlayerBlack.removeOwnedSpace(space1);
        assertEquals(1, testPlayerBlack.getPlayerOwnedSpacesSpaces().size(), "Removing an already removed space should not change size.");
    }

    // --- Tests for getAvailableMoves ---

    @Test
    void testGetAvailableMovesInitialBoard() {
        // Setup initial Othello board configuration
        board[3][3].setType(BoardSpace.SpaceType.WHITE);
        board[4][4].setType(BoardSpace.SpaceType.WHITE);
        board[3][4].setType(BoardSpace.SpaceType.BLACK);
        board[4][3].setType(BoardSpace.SpaceType.BLACK);

        testPlayerBlack.addOwnedSpace(board[3][4]);
        testPlayerBlack.addOwnedSpace(board[4][3]);
        testPlayerWhite.addOwnedSpace(board[3][3]);
        testPlayerWhite.addOwnedSpace(board[4][4]);

        // Black player's turn (usually starts)
        Map<BoardSpace, List<BoardSpace>> blackMoves = testPlayerBlack.getAvailableMoves(board);

        // Expected moves for Black initially: (2,3), (3,2), (4,5), (5,4)
        assertEquals(4, blackMoves.size(), "Black should have 4 available moves initially.");
        assertTrue(blackMoves.containsKey(board[2][3]), "Should contain move to (2,3)");
        assertTrue(blackMoves.containsKey(board[3][2]), "Should contain move to (3,2)");
        assertTrue(blackMoves.containsKey(board[4][5]), "Should contain move to (4,5)");
        assertTrue(blackMoves.containsKey(board[5][4]), "Should contain move to (5,4)");

        // Check origins for a specific move, e.g., to (2,3) should originate from (4,3) via (3,3)
        assertTrue(blackMoves.get(board[2][3]).contains(board[4][3]), "Move to (2,3) should list origin (4,3).");
        assertEquals(1, blackMoves.get(board[2][3]).size(), "Move to (2,3) should have exactly one origin.");

        // White player's turn next
        Map<BoardSpace, List<BoardSpace>> whiteMoves = testPlayerWhite.getAvailableMoves(board);
        // Expected moves for White initially: (2,4), (4,2), (5,3), (3,5)
        assertEquals(4, whiteMoves.size(), "White should have 4 available moves initially.");
        assertTrue(whiteMoves.containsKey(board[2][4]), "Should contain move to (2,4)");
        assertTrue(whiteMoves.containsKey(board[4][2]), "Should contain move to (4,2)");
        assertTrue(whiteMoves.containsKey(board[5][3]), "Should contain move to (5,3)");
        assertTrue(whiteMoves.containsKey(board[3][5]), "Should contain move to (3,5)");
    }

    @Test
    void testGetAvailableMovesHorizontal() {
        // B W W _
        board[0][0].setType(BoardSpace.SpaceType.BLACK);
        board[0][1].setType(BoardSpace.SpaceType.WHITE);
        board[0][2].setType(BoardSpace.SpaceType.WHITE);
        // board[0][3] is EMPTY

        testPlayerBlack.addOwnedSpace(board[0][0]);
        testPlayerWhite.addOwnedSpace(board[0][1]);
        testPlayerWhite.addOwnedSpace(board[0][2]);

        Map<BoardSpace, List<BoardSpace>> blackMoves = testPlayerBlack.getAvailableMoves(board);

        assertEquals(1, blackMoves.size(), "Black should have 1 horizontal move.");
        assertTrue(blackMoves.containsKey(board[0][3]), "Move should be to (0,3).");
        assertTrue(blackMoves.get(board[0][3]).contains(board[0][0]), "Origin should be (0,0).");
    }

    @Test
    void testGetAvailableMovesVertical() {
        // B
        // W
        // W
        // _
        board[0][0].setType(BoardSpace.SpaceType.BLACK);
        board[1][0].setType(BoardSpace.SpaceType.WHITE);
        board[2][0].setType(BoardSpace.SpaceType.WHITE);
        // board[3][0] is EMPTY

        testPlayerBlack.addOwnedSpace(board[0][0]);
        testPlayerWhite.addOwnedSpace(board[1][0]);
        testPlayerWhite.addOwnedSpace(board[2][0]);

        Map<BoardSpace, List<BoardSpace>> blackMoves = testPlayerBlack.getAvailableMoves(board);

        assertEquals(1, blackMoves.size(), "Black should have 1 vertical move.");
        assertTrue(blackMoves.containsKey(board[3][0]), "Move should be to (3,0).");
        assertTrue(blackMoves.get(board[3][0]).contains(board[0][0]), "Origin should be (0,0).");
    }

    @Test
    void testGetAvailableMovesDiagonal() {
        // B 0 0 0
        // 0 W 0 0
        // 0 0 W 0
        // 0 0 0 _
        board[0][0].setType(BoardSpace.SpaceType.BLACK);
        board[1][1].setType(BoardSpace.SpaceType.WHITE);
        board[2][2].setType(BoardSpace.SpaceType.WHITE);
        // board[3][3] is EMPTY

        testPlayerBlack.addOwnedSpace(board[0][0]);
        testPlayerWhite.addOwnedSpace(board[1][1]);
        testPlayerWhite.addOwnedSpace(board[2][2]);

        Map<BoardSpace, List<BoardSpace>> blackMoves = testPlayerBlack.getAvailableMoves(board);

        assertEquals(1, blackMoves.size(), "Black should have 1 diagonal move.");
        assertTrue(blackMoves.containsKey(board[3][3]), "Move should be to (3,3).");
        assertTrue(blackMoves.get(board[3][3]).contains(board[0][0]), "Origin should be (0,0).");
    }


    @Test
    void testGetAvailableMovesMultipleOrigins() {
        // B W _ W B
        board[0][0].setType(BoardSpace.SpaceType.BLACK);
        board[0][1].setType(BoardSpace.SpaceType.WHITE);
        // board[0][2] is EMPTY
        board[0][3].setType(BoardSpace.SpaceType.WHITE);
        board[0][4].setType(BoardSpace.SpaceType.BLACK);

        testPlayerBlack.addOwnedSpace(board[0][0]);
        testPlayerWhite.addOwnedSpace(board[0][1]);
        testPlayerWhite.addOwnedSpace(board[0][3]);
        testPlayerBlack.addOwnedSpace(board[0][4]);

        Map<BoardSpace, List<BoardSpace>> blackMoves = testPlayerBlack.getAvailableMoves(board);

        assertEquals(1, blackMoves.size(), "Black should have 1 available move destination.");
        assertTrue(blackMoves.containsKey(board[0][2]), "The only move destination should be (0,2).");

        List<BoardSpace> origins = blackMoves.get(board[0][2]);
        assertEquals(2, origins.size(), "Move to (0,2) should have two origins.");
        assertTrue(origins.contains(board[0][0]), "Origin list should include (0,0).");
        assertTrue(origins.contains(board[0][4]), "Origin list should include (0,4).");
    }

    @Test
    void testGetAvailableMovesNoMoves() {
        // B B B
        // B B B
        // B B B
        for(int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                board[i][j].setType(BoardSpace.SpaceType.BLACK);
                testPlayerBlack.addOwnedSpace(board[i][j]);
            }
        }
        // White has no pieces and no valid moves
        Map<BoardSpace, List<BoardSpace>> whiteMoves = testPlayerWhite.getAvailableMoves(board);
        assertTrue(whiteMoves.isEmpty(), "White should have no available moves.");

        // Black also has no moves as there are no empty spaces adjacent to white (or no white pieces)
        Map<BoardSpace, List<BoardSpace>> blackMoves = testPlayerBlack.getAvailableMoves(board);
        assertTrue(blackMoves.isEmpty(), "Black should have no available moves in this scenario.");
    }

}