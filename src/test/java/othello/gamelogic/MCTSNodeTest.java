package othello.gamelogic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class MCTSNodeTest {
    private MCTSNode rootNode;
    private Player blackPlayer;
    private Player whitePlayer;
    private BoardSpace[][] board;

    @BeforeEach
    void setUp() {
        blackPlayer = new HumanPlayer(BoardSpace.SpaceType.BLACK);
        whitePlayer = new HumanPlayer(BoardSpace.SpaceType.WHITE);
        board = new BoardSpace[8][8];
        
        // 初始化棋盘
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = BoardSpace.getBoardSpace(i, j, BoardSpace.SpaceType.EMPTY);
            }
        }
        
        // 设置初始棋子位置
        board[3][3] = BoardSpace.getBoardSpace(3, 3, BoardSpace.SpaceType.WHITE);
        board[3][4] = BoardSpace.getBoardSpace(3, 4, BoardSpace.SpaceType.BLACK);
        board[4][3] = BoardSpace.getBoardSpace(4, 3, BoardSpace.SpaceType.BLACK);
        board[4][4] = BoardSpace.getBoardSpace(4, 4, BoardSpace.SpaceType.WHITE);
        
        blackPlayer.addOwnedSpace(board[3][4]);
        blackPlayer.addOwnedSpace(board[4][3]);
        whitePlayer.addOwnedSpace(board[3][3]);
        whitePlayer.addOwnedSpace(board[4][4]);
        
        rootNode = new MCTSNode(board, blackPlayer, whitePlayer, null, null);
    }

    @Test
    void testNodeInitialization() {
        assertNotNull(rootNode);
        assertEquals(0, rootNode.getVisits());
        assertEquals(0, rootNode.getWins());
        assertTrue(rootNode.getChildren().isEmpty());
        assertEquals(blackPlayer.getColor(), rootNode.getCurrentPlayer().getColor());
        assertEquals(whitePlayer.getColor(), rootNode.getOpponent().getColor());
    }

    @Test
    void testNodeExpansion() {
        rootNode.expand();
        List<MCTSNode> children = rootNode.getChildren();
        
        // 黑方初始应该有4个合法移动
        assertEquals(4, children.size());
        
        // 验证每个子节点
        for (MCTSNode child : children) {
            assertNotNull(child.getMove());
            assertEquals(whitePlayer.getColor(), child.getCurrentPlayer().getColor());
            assertEquals(blackPlayer.getColor(), child.getOpponent().getColor());
            assertEquals(0, child.getVisits());
            assertEquals(0, child.getWins());
        }
    }

    @Test
    void testNodeSelection() {
        rootNode.expand();
        MCTSNode selected = rootNode.selectChild(1.4);
        assertNotNull(selected);
        assertTrue(rootNode.getChildren().contains(selected));
    }

    @Test
    void testBackpropagation() {
        rootNode.expand();
        MCTSNode child = rootNode.getChildren().get(0);
        
        // 模拟并反向传播
        boolean won = child.simulate();
        child.backPropagate(won);
        
        assertEquals(1, child.getVisits());
        assertEquals(won ? 1 : 0, child.getWins());
        assertEquals(1, rootNode.getVisits());
        assertEquals(won ? 1 : 0, rootNode.getWins());
    }

    @Test
    void testBestMoveSelection() {
        rootNode.expand();
        MCTSNode child = rootNode.getChildren().get(0);
        
        // 多次模拟并反向传播
        for (int i = 0; i < 10; i++) {
            boolean won = child.simulate();
            child.backPropagate(won);
        }
        
        BoardSpace bestMove = rootNode.getBestMove();
        assertNotNull(bestMove);
        assertSame(bestMove.getType(), BoardSpace.SpaceType.EMPTY);
    }

    @Test
    void testBoardCopy() {
        BoardSpace[][] originalBoard = rootNode.getBoard();
        BoardSpace[][] copiedBoard = rootNode.copyBoard(originalBoard);
        
        assertNotSame(originalBoard, copiedBoard);
        assertEquals(originalBoard.length, copiedBoard.length);
        assertEquals(originalBoard[0].length, copiedBoard[0].length);
        
        for (int i = 0; i < originalBoard.length; i++) {
            for (int j = 0; j < originalBoard[i].length; j++) {
                assertEquals(originalBoard[i][j].getType(), copiedBoard[i][j].getType());
                assertEquals(originalBoard[i][j].getX(), copiedBoard[i][j].getX());
                assertEquals(originalBoard[i][j].getY(), copiedBoard[i][j].getY());
            }
        }
    }

    @Test
    void testExecuteMove() {
        Map<BoardSpace, List<BoardSpace>> moves = blackPlayer.getAvailableMoves(board);
        BoardSpace move = moves.keySet().iterator().next();
        List<BoardSpace> flips = moves.get(move);
        
        rootNode.executeMove(board, move, flips, blackPlayer, whitePlayer);
        
        assertEquals(blackPlayer.getColor(), board[move.getX()][move.getY()].getType());
        assertTrue(blackPlayer.getPlayerOwnedSpacesSpaces().contains(board[move.getX()][move.getY()]));
    }

    @Test
    void testGetters() {
        // Test getBoard
        BoardSpace[][] nodeBoard = rootNode.getBoard();
        assertNotNull(nodeBoard);
        assertEquals(8, nodeBoard.length);
        assertEquals(8, nodeBoard[0].length);
        assertEquals(BoardSpace.SpaceType.WHITE, nodeBoard[3][3].getType());
        assertEquals(BoardSpace.SpaceType.BLACK, nodeBoard[3][4].getType());
        
        // Test getCurrentPlayer
        Player currentPlayer = rootNode.getCurrentPlayer();
        assertNotNull(currentPlayer);
        assertEquals(BoardSpace.SpaceType.BLACK, currentPlayer.getColor());
        
        // Test getOpponent
        Player opponent = rootNode.getOpponent();
        assertNotNull(opponent);
        assertEquals(BoardSpace.SpaceType.WHITE, opponent.getColor());
        
        // Test getMove
        assertNull(rootNode.getMove()); // Root node should have no move
        
        // Test getChildren
        List<MCTSNode> children = rootNode.getChildren();
        assertNotNull(children);
        assertTrue(children.isEmpty()); // Root node should have no children initially
        
        // Test getVisits
        assertEquals(0, rootNode.getVisits());
        
        // Test getWins
        assertEquals(0, rootNode.getWins());
        
        // Test getParent
        assertNull(rootNode.getParent()); // Root node should have no parent
        
        // Test getters after expansion
        rootNode.expand();
        children = rootNode.getChildren();
        assertFalse(children.isEmpty());
        
        // Test getters on a child node
        MCTSNode childNode = children.get(0);
        assertNotNull(childNode.getMove());
        assertEquals(BoardSpace.SpaceType.EMPTY, childNode.getMove().getType());
        assertEquals(BoardSpace.SpaceType.WHITE, childNode.getCurrentPlayer().getColor());
        assertEquals(BoardSpace.SpaceType.BLACK, childNode.getOpponent().getColor());
        assertEquals(rootNode, childNode.getParent());
        assertEquals(0, childNode.getVisits());
        assertEquals(0, childNode.getWins());
    }
} 