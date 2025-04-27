package othello.gamelogic;

public interface AI {
    BoardSpace nextMove(BoardSpace[][] board, Player player);
}
