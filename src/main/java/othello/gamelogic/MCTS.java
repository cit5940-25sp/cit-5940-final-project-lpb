package othello.gamelogic;

public class MCTS implements AI {
    @Override
    public BoardSpace nextMove(BoardSpace[][] boardSpace, Player player) {
        // reinforcement learning, random sampling from population
        // auto prunes a state value
        // STEPS:
        // 1. Tree Traversal - UCB1(S_i) = average of S_i (score / n_i) + 2 *
        //                     sqrt(ln N (parent visits) / n_i (# of visits of the node))
        //    current = S_0
        //    current.isLeaf() ? (n_i = 0 (i.e. current never sampled before) ? Rollout :
        //                      for each available action from here, add a new state in tree,
        //                      current = first new child node, Rollout) :
        //                      current = child w. max UCB1(S_i)
        // 2. Node Expansion
        // 3. Rollout (Random Simulation)
        //    If S_i is terminal state (end of game): return value(S_i)
        //    Else: A_i = random(available actions(S_i))
        //          S_i = Simulate(A_i, S_i)
        //          until reaching terminal state
        // 4. Backpropagation
        //    Recursively go back until the root and update the score (sum of score of 2 children)
        //    and increment n_i of each node
        return null;
    }
}
