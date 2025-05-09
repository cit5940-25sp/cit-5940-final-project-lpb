package othello.gamelogic;

import othello.Constants;

import java.util.List;
import java.util.Map;

/**
 * Custom AI implementation for Othello game.
 * Uses Negamax algorithm with Alpha-Beta pruning for move selection.
 * Implements a weighted board evaluation strategy based on position values.
 */
public class Custom implements AI {
    private int depth = 3;

    /**
     * Sets the search depth for the Negamax algorithm.
     * @param depth The desired search depth, must be greater than 0
     * @throws IllegalArgumentException if depth is less than 1
     */
    public void setDepth(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("depth must be greater than 0");
        }
        this.depth = depth;
    }

    /**
     * PART 1
     * Determines the next move for the current player using the Negamax algorithm.
     * Evaluates all possible moves and selects the one with the highest score.
     * @param board The current game board state
     * @param player The current player making the move
     * @param opponent The opposing player
     * @return The selected BoardSpace to move to, or null if no valid moves exist
     */
    @Override
    public BoardSpace nextMove(BoardSpace[][] board, Player player, Player opponent) {
        Map<BoardSpace, List<BoardSpace>> moves = player.getAvailableMoves(board);
        if (moves.isEmpty()) return null;

        BoardSpace bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Map.Entry<BoardSpace, List<BoardSpace>> move : moves.entrySet()) {
            BoardSpace[][] newBoard = copyBoard(board);
            Player current = player.copy();
            Player opp = opponent.copy();
            executeMove(newBoard, move.getKey(), move.getValue(), current, opp);

            int score = -negamax(newBoard, opp, current, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, -1);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move.getKey();
            }
        }

        return bestMove;
    }

    /**
     * PART 2
     * Implements the Negamax algorithm with Alpha-Beta pruning.
     * Recursively evaluates board positions to find the best move.
     * @param board The current game board state
     * @param currentPlayer The player whose turn it is
     * @param opponent The opposing player
     * @param depth The remaining search depth
     * @param alpha The alpha value for pruning
     * @param beta The beta value for pruning
     * @param color The color multiplier (1 for maximizing player, -1 for minimizing)
     * @return The evaluation score for the current position
     */
    private int negamax(BoardSpace[][] board, Player currentPlayer, Player opponent, int depth, int alpha, int beta, int color) {
        if (depth == 0) {
            return color * evaluate(board, currentPlayer);
        }

        Map<BoardSpace, List<BoardSpace>> moves = currentPlayer.getAvailableMoves(board);
        if (moves.isEmpty()) {
            if (opponent.getAvailableMoves(board).isEmpty()) {
                return color * evaluate(board, currentPlayer);
            }
            return -negamax(board, opponent, currentPlayer, depth, -beta, -alpha, -color);
        }

        int maxScore = Integer.MIN_VALUE;
        for (Map.Entry<BoardSpace, List<BoardSpace>> move : moves.entrySet()) {
            BoardSpace[][] newBoard = copyBoard(board);
            Player current = currentPlayer.copy();
            Player opp = opponent.copy();
            executeMove(newBoard, move.getKey(), move.getValue(), current, opp);
            int score = -negamax(newBoard, opp, current, depth - 1, -beta, -alpha, -color);
            maxScore = Math.max(maxScore, score);
            alpha = Math.max(alpha, score);
            if (alpha >= beta) break;
        }

        return maxScore;
    }

    /**
     * PART 3
     * Evaluates the current board state using a weighted position strategy.
     * Uses predefined weights from Constants.BOARD_WEIGHTS to value different positions.
     * @param board The current game board state
     * @param player The player whose position is being evaluated
     * @return The evaluation score for the current position
     */
    private int evaluate(BoardSpace[][] board, Player player) {
        int score = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                BoardSpace space = board[x][y];
                if (space.getType() == player.getColor()) {
                    score += Constants.BOARD_WEIGHTS[x][y];
                } else if (space.getType() != BoardSpace.SpaceType.EMPTY) {
                    score -= Constants.BOARD_WEIGHTS[x][y];
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
