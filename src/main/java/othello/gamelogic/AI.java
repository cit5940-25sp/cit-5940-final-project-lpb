package othello.gamelogic;

import java.util.List;

import static othello.Constants.BOARD_WEIGHTS;

/**
 * Interface for AI strategies in the Othello game.
 * Defines the contract for different AI implementations to determine
 * the next move based on the current game state.
 * 
 * Implementations include:
 * - Minimax: Uses minimax algorithm with alpha-beta pruning
 * - MCTS: Uses Monte Carlo Tree Search
 * - Custom: Uses negamax algorithm with custom evaluation
 */
public interface AI {
    /**
     * Determines the next move for the current player.
     * Each AI implementation will use its own strategy to evaluate
     * the board state and select the best move.
     * 
     * @param board The current game board state
     * @param player The current player making the move
     * @param opponent The opposing player
     * @return The selected BoardSpace to move to, or null if no valid moves exist
     */
    BoardSpace nextMove(BoardSpace[][] board, Player player, Player opponent);
}
