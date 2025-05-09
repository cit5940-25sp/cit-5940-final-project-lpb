package othello.gamelogic;

/**
 * Represents a computer player that will make decisions autonomously during their turns.
 * Employs a specific computer strategy passed in through program arguments.
 */
public class ComputerPlayer extends Player{
    private AI strategy;
    private String strategyName;

    /**
     * Constructs a ComputerPlayer using a given strategy name.
     * Initializes the AI strategy accordingly.
     *
     * @param strategyName the name of the strategy to use ("minimax", "mcts", or "custom")
     * @throws IllegalArgumentException if the strategy name is not recognized
     */
    public ComputerPlayer(String strategyName) {
        this.strategyName = strategyName;
        this.strategy = switch (strategyName.toLowerCase()) {
            case "minimax" -> new Minimax();
            case "mcts" -> new MCTS();
            case "custom" -> new Custom();
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategyName);
        };
    }

    /**
     * Returns the name of the strategy used by this computer player.
     *
     * @return the strategy name
     */
    public String getStrategyName() {
        return strategyName;
    }

    /**
     * Makes a move decision using the assigned strategy
     * @param board current game board state
     * @return the selected BoardSpace to move to
     */
    public BoardSpace makeMove(BoardSpace[][] board, Player opponent) {
        if (strategy == null) {
            throw new IllegalStateException("No strategy assigned to computer player");
        }
        return strategy.nextMove(board, this, opponent);
    }
}