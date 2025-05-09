package othello;

/**
 * A utility class that holds global constants used throughout the Othello game.
 *
 * <p>This includes board evaluation weights for AI strategies and parameters for
 * decision-making algorithms such as MCTS (Monte Carlo Tree Search).</p>
 */
public class Constants {
    /**
     * Evaluation weights used to assess the strategic value of each board position.
     *
     * <p>These weights are typically used by AI players to favor advantageous
     * positions such as corners and discourage risky ones like adjacent to corners.</p>
     */
    public static final int[][] BOARD_WEIGHTS =
            {{200, -70, 30, 25, 25, 30, -70, 200},
                    {-70, -100, -10, -10, -10, -10, -100, -70},
                    {30, -10, 2, 2, 2, 2, -10, 30},
                    {25, -10, 2, 2, 2, 2, -10, 25},
                    {25, -10, 2, 2, 2, 2, -10, 25},
                    {30, -10, 2, 2, 2, 2, -10, 30},
                    {-70, -100, -10, -10, -10, -10, -100, -70},
                    {200, -70, 30, 25, 25, 30, -70, 200}};

    /**
     * The exploration parameter used in Monte Carlo Tree Search (MCTS) to balance
     * exploration and exploitation when selecting moves.
     *
     * <p>This is typically set to √2 to provide a good trade-off between trying new moves
     * and exploiting known strong ones.</p>
     */
    public static final double EXPLORATION_PARAM = Math.sqrt(2);
}
