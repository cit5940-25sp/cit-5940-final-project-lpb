package othello.gamelogic;

/**
 * Represents a computer player that will make decisions autonomously during their turns.
 * Employs a specific computer strategy passed in through program arguments.
 */
public class ComputerPlayer extends Player{
    private ComputerStrategy strategy;
    public ComputerPlayer(String strategyName) {
        // PART 2
        // TODO: Use the strategyName input to create a specific strategy class for this computer
        // This input should match the ones specified in App.java!
        // Create the appropriate strategy based on the input name
        this.strategy = createStrategy(strategyName);
    }
    /**
     * Creates a specific strategy instance based on the strategy name
     * @param strategyName name of the strategy from program arguments
     * @return the created strategy instance
     */
    private ComputerStrategy createStrategy(String strategyName) {
        switch (strategyName.toLowerCase()) {
            case "minimax":
                return new MinimaxStrategy();
            case "expectimax":
                return new ExpectimaxStrategy();
            case "mcts":
                return new MCTSStrategy();
            case "custom":
                return new CustomStrategy();
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategyName);
        }
    }

    /**
     * Makes a move decision using the assigned strategy
     * @param board current game board state
     * @return the selected BoardSpace to move to
     */
    public BoardSpace makeMove(BoardSpace[][] board) {
        if (strategy == null) {
            throw new IllegalStateException("No strategy assigned to computer player");
        }
        return strategy.makeMove(board, this);
    }

    /**
     * Interface for all computer strategies
     */
    public interface ComputerStrategy {
        BoardSpace makeMove(BoardSpace[][] board, Player computerPlayer);
    }

    /**
     * Minimax strategy implementation
     */
    public static class MinimaxStrategy implements ComputerStrategy {
        @Override
        public BoardSpace makeMove(BoardSpace[][] board, Player computerPlayer) {
            // TODO: Implement minimax algorithm
            // This should evaluate possible moves using minimax
            // and return the best move found
            return null; // placeholder
        }
    }

    /**
     * Expectimax strategy implementation
     */
    public static class ExpectimaxStrategy implements ComputerStrategy {
        @Override
        public BoardSpace makeMove(BoardSpace[][] board, Player computerPlayer) {
            // TODO: Implement expectimax algorithm
            // Similar to minimax but with chance nodes
            return null; // placeholder
        }
    }

    /**
     * Monte Carlo Tree Search strategy implementation
     */
    public static class MCTSStrategy implements ComputerStrategy {
        @Override
        public BoardSpace makeMove(BoardSpace[][] board, Player computerPlayer) {
            // TODO: Implement MCTS algorithm
            // Should use the 4-step MCTS process
            return null; // placeholder
        }
    }

    /**
     * Custom strategy implementation
     */
    public static class CustomStrategy implements ComputerStrategy {
        @Override
        public BoardSpace makeMove(BoardSpace[][] board, Player computerPlayer) {
            // TODO: Implement your custom strategy
            // This could be any algorithm you design
            return null; // placeholder
        }
    }

    // PART 2
    // TODO: implement a method that returns a BoardSpace that a strategy selects
}