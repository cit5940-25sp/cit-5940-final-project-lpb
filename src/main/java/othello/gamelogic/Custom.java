package othello.gamelogic;

public class Custom implements AI {
    @Override
    public BoardSpace nextMove(BoardSpace[][] boardSpace, Player player) {
        // TODO: Algorithm TBD
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
        return null;
    }
}
