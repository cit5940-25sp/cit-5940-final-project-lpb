package othello.gamelogic;

import java.util.*;

public class Minimax implements AI {

    @Override
    public BoardSpace nextMove(BoardSpace[][] board, Player player) {

        Map<BoardSpace, List<BoardSpace>> moves = player.getAvailableMoves(board);
        Deque<BoardSpace> queue = new ArrayDeque<>();
        if (moves.isEmpty()) {
            return null;
        }
        BoardSpace bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        // use BFS to find the best move
        for (BoardSpace move : moves.keySet()) {
            BoardSpace[][] nextState = board.clone();

        }
        // reverse strategy at each level (minimize opponent gain/maximize self gain)
        return null;
    }

    private void flipColors(Player actingPlayer,
                            Player opponent,
                            Map<BoardSpace, List<BoardSpace>> availableMoves,
                            BoardSpace selectedDestination) {

    }
}