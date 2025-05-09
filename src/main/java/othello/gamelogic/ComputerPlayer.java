package othello.gamelogic;

/**
 * Represents a computer player that will make decisions autonomously during their turns.
 * Employs a specific computer strategy passed in through program arguments.
 */
public class ComputerPlayer extends Player{
    private AI strategy;
    private String strategyName;

    public ComputerPlayer(String strategyName) {
        this.strategyName = strategyName;
        this.strategy = createStrategy(strategyName);
    }

    public String getStrategyName() {
        return strategyName;
    }

    /**
     * Creates a specific strategy instance based on the strategy name
     * @param strategyName name of the strategy from program arguments
     * @return the created strategy instance
     */
    private AI createStrategy(String strategyName) {
        switch (strategyName.toLowerCase()) {
            case "minimax":
                return new Minimax();
            case "mcts":
                return new MCTS();
            case "custom":
                return new Custom();
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategyName);
        }
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