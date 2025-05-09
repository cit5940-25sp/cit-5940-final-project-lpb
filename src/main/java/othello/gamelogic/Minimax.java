package othello.gamelogic;

import java.util.*;
import static othello.Constants.BOARD_WEIGHTS;

/**
 * Minimax algorithm implementation for Othello game.
 * Uses Alpha-Beta pruning to optimize the search process.
 * Implements a weighted board evaluation strategy based on position values.
 */
public class Minimax implements AI {
    private int maxDepth; // search depth

    /**
     * Creates a new Minimax instance with default search depth.
     * Default depth is set to 2 for balanced performance.
     */
    public Minimax() {
        this.maxDepth = 2; // default depth
    }

    /**
     * Sets the maximum search depth for the Minimax algorithm.
     * @param depth The desired search depth, must be greater than 0
     * @throws IllegalArgumentException if depth is less than 1
     */
    public void setMaxDepth(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("invalid search depth");
        }
        this.maxDepth = depth;
    }

    /**
     * Gets the current maximum search depth.
     * @return The maximum search depth used in the Minimax algorithm
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * PART 1
     * Determines the next move using the Minimax algorithm with Alpha-Beta pruning.
     * Evaluates all possible moves and selects the one with the highest score.
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

        BoardSpace bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Map.Entry<BoardSpace, List<BoardSpace>> entry : moves.entrySet()) {
            BoardSpace[][] copy = copyBoard(board);
            Player playerCopy = player.copy();
            Player opponentCopy = opponent.copy();
            executeMove(copy, entry.getKey(), entry.getValue(), playerCopy, opponentCopy);
            int score = minimax(copy, playerCopy, opponentCopy, maxDepth - 1, alpha, beta, false);

            if (score > bestScore) {
                bestScore = score;
                bestMove = entry.getKey();
            }
            alpha = bestScore;
        }

        return bestMove;
    }

    /**
     * PART 2
     * Implements the Minimax algorithm with Alpha-Beta pruning.
     * Recursively evaluates board positions to find the best move.
     * @param board The current game board state
     * @param player The current player
     * @param opponent The opposing player
     * @param depth The remaining search depth
     * @param alpha The alpha value for pruning
     * @param beta The beta value for pruning
     * @param isMaximizing Whether this is a maximizing player's turn
     * @return The evaluation score for the current position
     */
    private int minimax(BoardSpace[][] board, Player player, Player opponent,
                        int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0) {
            return evaluateBoard(board, player, opponent);
        }

        Map<BoardSpace, List<BoardSpace>> moves = isMaximizing ? 
            player.getAvailableMoves(board) : opponent.getAvailableMoves(board);

        if (moves.isEmpty()) {
            // Check if opponent also has no moves — if yes, game over
            if (opponent.getAvailableMoves(board).isEmpty()) {
                return evaluateBoard(board, player, opponent);
            }

            // Otherwise, pass the turn to the opponent
            return minimax(board, opponent, player, depth, alpha, beta, !isMaximizing);
        }

        int bestScore;
        // current player
        if (isMaximizing) {
            bestScore = Integer.MIN_VALUE;
            for (Map.Entry<BoardSpace, List<BoardSpace>> entry : moves.entrySet()) {
                BoardSpace[][] copy = copyBoard(board);
                Player playerCopy = player.copy();
                Player opponentCopy = opponent.copy();
                executeMove(copy, entry.getKey(), entry.getValue(), playerCopy, opponentCopy);

                int score = minimax(copy, playerCopy, opponentCopy, depth - 1, alpha, beta, false);
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, bestScore);

                if (beta <= alpha) {
                    break; // Beta Pruning
                }
            }
        } else {
            // opponent
            bestScore = Integer.MAX_VALUE;
            for (Map.Entry<BoardSpace, List<BoardSpace>> entry : moves.entrySet()) {
                BoardSpace[][] copy = copyBoard(board);
                Player playerCopy = player.copy();
                Player opponentCopy = opponent.copy();
                executeMove(copy, entry.getKey(), entry.getValue(), playerCopy, opponentCopy);

                int score = minimax(copy, playerCopy, opponentCopy, depth - 1, alpha, beta, true);
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, bestScore);

                if (beta <= alpha) {
                    break; // Alpha Pruning
                }
            }
        }
        return bestScore;
    }

    /**
     * PART 3
     * Evaluates the current board state using a weighted position strategy.
     * Uses predefined weights from Constants.BOARD_WEIGHTS to value different positions.
     * @param board The current game board state
     * @param player The player whose position is being evaluated
     * @return The evaluation score for the current position
     */
    private int evaluateBoard(BoardSpace[][] board, Player player, Player opponent) {
        int score = 0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].getType() == player.getColor()) {
                    score += BOARD_WEIGHTS[i][j];
                } else if (board[i][j].getType() == opponent.getColor()) {
                    score -= BOARD_WEIGHTS[i][j];
                }
            }
        }

        return score;
    }

    /**
     * PART 4
     * Creates a deep copy of the game board.
     * @param board The board to copy
     * @return A new board with the same state as the input board
     */
    private BoardSpace[][] copyBoard(BoardSpace[][] board) {
        BoardSpace[][] copy = new BoardSpace[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                copy[i][j] = BoardSpace.getBoardSpace(i, j, board[i][j].getType());
            }
        }
        return copy;
    }

    /**
     * PART 5
     * Executes a move on the board and updates player ownership.
     * @param board The game board to modify
     * @param move The move to execute
     * @param moves The list of pieces to flip
     * @param player The player making the move
     * @param opponent The opposing player
     */
    private void executeMove(BoardSpace[][] board, BoardSpace move, List<BoardSpace> moves,
                     Player player, Player opponent) {
        board[move.getX()][move.getY()] = BoardSpace.getBoardSpace(
                move.getX(), move.getY(), player.getColor());
        player.addOwnedSpace(board[move.getX()][move.getY()]);
        for (BoardSpace dest : moves) {
            flipPiecesBetween(move, dest, board, player, opponent);
        }
    }

    /**
     * PART 6
     * Flips all pieces between two positions on the board.
     * Updates player ownership of the flipped pieces.
     * @param start The starting position
     * @param end The ending position
     * @param board The game board to modify
     * @param player The player gaining ownership
     * @param opponent The player losing ownership
     */
    private void flipPiecesBetween(BoardSpace start, BoardSpace end, BoardSpace[][] board,
                                   Player player, Player opponent) {
        int x1 = start.getX();
        int y1 = start.getY();
        int x2 = end.getX();
        int y2 = end.getY();

        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);

        int x = x1 + dx;
        int y = y1 + dy;

        // Move along the line between start and end
        while (x != x2 || y != y2) {
            BoardSpace current = board[x][y];
            // Update the board reference
            // Get the flyweight instance for the new state
            BoardSpace currentNew = BoardSpace.getBoardSpace(x, y, player.getColor());
            // Update the board reference
            board[x][y] = currentNew;

            x += dx;
            y += dy;

            // Update ownership lists
            opponent.removeOwnedSpace(current);
            player.addOwnedSpace(currentNew);
        }
    }
}