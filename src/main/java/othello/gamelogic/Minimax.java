package othello.gamelogic;

import java.util.*;

public class Minimax implements AI {

    @Override
    public BoardSpace nextMove(BoardSpace[][] board, Player player) {
        Map<BoardSpace, List<BoardSpace>> moves = player.getAvailableMoves(board);
        Deque<BoardSpace> queue = new ArrayDeque<>();

        // use BFS
        // reverse strategy at each level (minimize opponent gain/maximize self gain)
        return null;
    }
}
