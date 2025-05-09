package othello.gamelogic;

import othello.Constants;
import java.util.List;
import java.util.Map;

/**
 * Monte Carlo Tree Search (MCTS) implementation for Othello game.
 * Uses the UCT (Upper Confidence Bound for Trees) formula for node selection.
 * Implements a simulation-based approach to find the best move.
 */
public class MCTS implements AI {
    private int iterations;
    private static final double EXPLORATION_PARAM = Constants.EXPLORATION_PARAM;

    /**
     * Creates a new MCTS instance with default iteration count.
     * Default iterations are set to 1000 for balanced performance.
     */
    public MCTS() {
        this.iterations = 1000; // default iterations
    }

    /**
     * Sets the number of iterations for the MCTS algorithm.
     * @param iterations The desired number of iterations, must be greater than 0
     * @throws IllegalArgumentException if iterations is less than 1
     */
    public void setIterations(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("Iterations must be greater than 0");
        }
        this.iterations = iterations;
    }

    /**
     * Gets the current number of iterations.
     * @return The number of iterations used in the MCTS algorithm
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * PART 1
     * Determines the next move using Monte Carlo Tree Search.
     * Builds a search tree through iterations of selection, expansion,
     * simulation, and backpropagation.
     * @param board The current game board state
     * @param player The current player making the move
     * @param opponent The opposing player
     * @return The selected BoardSpace to move to, or null if no valid moves exist
     */
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
