package othello.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for the {@link OthelloGame} class.
 * These tests validate the behavior of game-related methods such as board initialization, move-taking, and decision-making.
 */

class OthelloGameTest {
    private Player blackPlayer;
    private Player whitePlayer;
    private OthelloGame game;

    @BeforeEach
    void setUp() {
        blackPlayer = new Player();
        blackPlayer.setColor(BoardSpace.SpaceType.BLACK);

        whitePlayer = new Player();
        whitePlayer.setColor(BoardSpace.SpaceType.WHITE);
        game = new OthelloGame(blackPlayer, whitePlayer);
    }

    @Test
    void testGetBoard() {
        BoardSpace[][] board = game.getBoard();
        assertEquals(BoardSpace.SpaceType.WHITE, board[3][3].getType());
        assertEquals(BoardSpace.SpaceType.BLACK, board[3][4].getType());
    }

    @Test
    void testGetPlayerOne() {
        assertSame(blackPlayer, game.getPlayerOne());
    }

    @Test
    void testGetPlayerTwo() {
        assertSame(whitePlayer, game.getPlayerTwo());
    }

    @Test
    void testGetAvailableMoves() {
        Map<BoardSpace, List<BoardSpace>> moves = game.getAvailableMoves(blackPlayer);

        assertEquals(4, moves.size());

        assertTrue(moves.containsKey(BoardSpace.getBoardSpace(3, 2, BoardSpace.SpaceType.EMPTY)));
        assertTrue(moves.containsKey(BoardSpace.getBoardSpace(2, 3, BoardSpace.SpaceType.EMPTY)));
        assertTrue(moves.containsKey(BoardSpace.getBoardSpace(5, 4, BoardSpace.SpaceType.EMPTY)));
        assertTrue(moves.containsKey(BoardSpace.getBoardSpace(4, 5, BoardSpace.SpaceType.EMPTY)));

    }

    @Test
    void initBoard() {
        BoardSpace[][] board = game.getBoard();

        for (int i = 0; i < OthelloGame.GAME_BOARD_SIZE; i++) {
            for (int j = 0; j < OthelloGame.GAME_BOARD_SIZE; j++) {
                if ((i == 3 && j == 3) || (i == 4 && j == 4)) {
                    assertEquals(BoardSpace.SpaceType.WHITE, board[i][j].getType());
                } else if ((i == 3 && j == 4) || (i == 4 && j == 3)) {
                    assertEquals(BoardSpace.SpaceType.BLACK, board[i][j].getType());
                } else {
                    assertEquals(BoardSpace.SpaceType.EMPTY, board[i][j].getType());
                }
            }
        }
    }

    @Test
    void testTakeSpace() {
        game.takeSpace(blackPlayer, whitePlayer, 0, 0);
        BoardSpace space = game.getBoard()[0][0];
        assertEquals(BoardSpace.SpaceType.BLACK, space.getType());
    }

    @Test
    void testTakeSpaces() {
        Map<BoardSpace, List<BoardSpace>> moves = new HashMap<>();
        BoardSpace dest = BoardSpace.getBoardSpace(3, 2, BoardSpace.SpaceType.EMPTY);
        List<BoardSpace> origins = List.of(BoardSpace.getBoardSpace(3, 4, BoardSpace.SpaceType.BLACK));
        moves.put(dest, origins);

        game.takeSpaces(blackPlayer, whitePlayer, moves, dest);

        // Verify flipped piece
        assertEquals(BoardSpace.SpaceType.BLACK, game.getBoard()[3][3].getType());
    }

    @Test
    void testComputerDecision() {
        ComputerPlayer computer = new ComputerPlayer("minimax") {
            {
                setColor(BoardSpace.SpaceType.BLACK);
            }

            @Override
            public BoardSpace makeMove(BoardSpace[][] board, Player opponent) {
                Map<BoardSpace, List<BoardSpace>> moves = getAvailableMoves(board);
                return moves.keySet().stream().findFirst().orElse(null);
            }
        };

        OthelloGame testGame = new OthelloGame(computer, whitePlayer);
        BoardSpace decision = testGame.computerDecision(computer);

        Map<BoardSpace, List<BoardSpace>> validMoves = computer.getAvailableMoves(testGame.getBoard());
        if (!validMoves.isEmpty()) {
            assertTrue(validMoves.containsKey(decision), "move " + decision + " is not a valid move");
        } else {
            assertNull(decision);
        }
    }
}