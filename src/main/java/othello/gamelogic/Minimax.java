package othello.gamelogic;

import java.util.*;

public class Minimax implements AI {

    @Override
    public BoardSpace nextMove(BoardSpace[][] board, Player player) {
        Map<BoardSpace, List<BoardSpace>> moves = player.getAvailableMoves(board);
        if (moves.isEmpty()) {
            return null;
        }

        Deque<BoardSpace> queue = new ArrayDeque<>();
        BoardSpace bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        // use BFS
        // reverse strategy at each level (minimize opponent gain/maximize self gain)
        return null;
    }
}
