package othello.gamelogic;

import java.util.List;

import static othello.Constants.BOARD_WEIGHTS;

public interface AI {
    BoardSpace nextMove(BoardSpace[][] board, Player player, Player opponent);
}
