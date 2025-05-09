package othello.gamelogic;

import java.util.*;
import static othello.Constants.BOARD_WEIGHTS;

public class Minimax implements AI {
    private int maxDepth; // search depth

    public Minimax() {
        this.maxDepth = 2; // default depth
    }

    public void setMaxDepth(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("invalid search depth");
        }
        this.maxDepth = depth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

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

    private BoardSpace[][] copyBoard(BoardSpace[][] board) {
        BoardSpace[][] copy = new BoardSpace[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                copy[i][j] = BoardSpace.getBoardSpace(i, j, board[i][j].getType());
            }
        }
        return copy;
    }

    private void executeMove(BoardSpace[][] board, BoardSpace move, List<BoardSpace> moves,
                     Player player, Player opponent) {
        board[move.getX()][move.getY()] = BoardSpace.getBoardSpace(
                move.getX(), move.getY(), player.getColor());
        player.addOwnedSpace(board[move.getX()][move.getY()]);
        for (BoardSpace dest : moves) {
            flipPiecesBetween(move, dest, board, player, opponent);
        }
    }

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