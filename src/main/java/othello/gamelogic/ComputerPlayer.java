package othello.gamelogic;

/**
 * Represents a computer player that will make decisions autonomously during their turns.
 * Employs a specific computer strategy passed in through program arguments.
 */
public class ComputerPlayer extends Player{
    public ComputerPlayer(String strategyName) {
        // PART 2
        // TODO: Use the strategyName input to create a specific strategy class for this computer
        // This input should match the following:
        // "human", "minimax", "expectimax", "mcts", "custom" (greedy)
        // alternative: 1. Heuristic-Based Weighted Evaluation
        //
        // A slightly fancier greedy approach! Basically:
        //	•	Assign weights to different strategic factors:
        //	•	Corner control
        //	•	Edge stability
        //	•	Mobility (number of legal moves)
        //	•	Potential mobility (number of squares next to opponent’s discs)
        //	•	Disc count difference (early game = ignore, late game = important)
        //
        // Implementation:
        // Write an evaluation function like:
        // score = (corner_weight * my_corners) - (corner_weight * opp_corners) +
        //         (mobility_weight * my_moves) - (mobility_weight * opp_moves) +
        //         (edge_weight * my_edges) - (edge_weight * opp_edges)
        // Then just evaluate all legal moves and choose the one with the highest score.
        //
        // Pro: Super fast, no recursion!
        // Con: Not as “deep” as tree search ones.
    }

    // PART 2
    // TODO: implement a method that returns a BoardSpace that a strategy selects
    private BoardSpace minimax() {
        return null;
    }

    private BoardSpace expectimax() {
        return null;
    }

    private BoardSpace mcts() {
        return null;
    }

    private BoardSpace custom() {
        // TODO: Algorithm TBD
        return null;
    }
}