package othello.gamelogic;

import othello.Constants;
import java.util.List;
import java.util.Map;

public class MCTS implements AI {
    private int iterations;
    private static final double EXPLORATION_PARAM = Constants.EXPLORATION_PARAM;

    public MCTS() {
        this.iterations = 1000; // default iterations
    }

    public void setIterations(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("Iterations must be greater than 0");
        }
        this.iterations = iterations;
    }

    public int getIterations() {
        return iterations;
    }

    @Override
    public BoardSpace nextMove(BoardSpace[][] board, Player player, Player opponent) {
        Map<BoardSpace, List<BoardSpace>> moves = player.getAvailableMoves(board);
        if (moves.isEmpty()) {
            return null;
        }

        // create root node (copy board before passing it in)
        BoardSpace[][] copy = new BoardSpace[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                copy[i][j] = BoardSpace.getBoardSpace(i, j, board[i][j].getType());
            }
        }
        MCTSNode root = new MCTSNode(copy, player.copy(), opponent.copy(), null, null);
        root.expand();

        // MCTS iterations
        for (int i = 0; i < iterations; i++) {
            // 1. Selection
            MCTSNode selectedNode = root;
            while (!selectedNode.getChildren().isEmpty() && selectedNode.getVisits() > 0) {
                selectedNode = selectedNode.selectChild(EXPLORATION_PARAM);
            }

            // 2. Expansion
            // if no child, try to expand again
            if (selectedNode.getVisits() > 0) {
                selectedNode.expand();
                if (!selectedNode.getChildren().isEmpty()) {
                    selectedNode = selectedNode.selectChild(EXPLORATION_PARAM);
                }
            }

            // 3. Simulation
            boolean won = selectedNode.simulate();

            // 4. Backpropagation
            selectedNode.backPropagate(won);
        }

        // return best move
        return root.getBestMove();
    }
}
