package othello.gamelogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a node in the Monte Carlo Tree Search.
 * Each node stores the game state, player information, and statistics
 * for the MCTS algorithm.
 */
public class MCTSNode {
    private BoardSpace[][] board;
    private Player currentPlayer;
    private Player opponent;
    private BoardSpace move; // the move to reach this node
    private MCTSNode parent;
    private List<MCTSNode> children;
    private int visits;
    private int wins;
    private static final Random random = new Random();

    /**
     * Creates a new MCTS node with the specified game state and player information.
     * @param board The current game board state
     * @param currentPlayer The player whose turn it is
     * @param opponent The opposing player
     * @param move The move that led to this node
     * @param parent The parent node in the search tree
     */
    public MCTSNode(BoardSpace[][] board, Player currentPlayer, Player opponent,
                    BoardSpace move, MCTSNode parent) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.opponent = opponent;
        this.move = move;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0;
    }

    /**
     * PART 1
     * Expands the current node by creating child nodes for all possible moves.
     * Each child node represents a possible next game state.
     */
    public void expand() {
        Map<BoardSpace, List<BoardSpace>> moves = currentPlayer.getAvailableMoves(board);
        for (Map.Entry<BoardSpace, List<BoardSpace>> entry : moves.entrySet()) {
            BoardSpace[][] newBoard = copyBoard(board);
            Player current = currentPlayer.copy();
            Player opponent = getOpponent().copy();
            executeMove(newBoard, entry.getKey(), entry.getValue(), current, opponent);
            MCTSNode child = new MCTSNode(newBoard, opponent, current,
                                         entry.getKey(), this);
            children.add(child);
        }
    }

    /**
     * PART 2
     * Selects the best child node using the UCT formula.
     * Balances exploration and exploitation in the search tree.
     * @return The selected child node
     */
    public MCTSNode selectChild(double explorationParam) {
        MCTSNode selected = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        List<MCTSNode> bestChildren = new ArrayList<>();

        for (MCTSNode child : children) {
            double uctValue;
            // incentivize exploring unexplored nodes
            if (child.visits == 0) {
                uctValue = Double.POSITIVE_INFINITY;
            } else {
                double exploitation = (double) child.wins / child.visits;
                double exploration = Math.sqrt(Math.log(this.visits) / child.visits);
                uctValue = exploitation + explorationParam * exploration;
            }

            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestChildren.clear();
                bestChildren.add(child);
            } else if (uctValue == bestValue) {
                bestChildren.add(child);
            }
        }

        // if there are multiple best values, randomly select one
        if (!bestChildren.isEmpty()) {
            selected = bestChildren.get(random.nextInt(bestChildren.size()));
        }

        return selected;    // null if no child
    }

    /**
     * PART 3
     * Simulates a random game from the current node until the end.
     * @return The result of the simulation (1 for win, 0 for loss)
     */
    public boolean simulate() {
        BoardSpace[][] simBoard = copyBoard(board);
        Player simCurrentPlayer = currentPlayer.copy();
        Player simOpponent = opponent.copy();
        boolean gameOver = false;

        // simulate terminal state (end of game) of the current child
        while (!gameOver) {
            Map<BoardSpace, List<BoardSpace>> moves = simCurrentPlayer.getAvailableMoves(simBoard);
            if (moves.isEmpty()) {
                Map<BoardSpace, List<BoardSpace>> opponentMoves = simOpponent.getAvailableMoves(simBoard);
                if (opponentMoves.isEmpty()) {
                    gameOver = true;
                } else {
                    // opponent move
                    List<BoardSpace> possibleMoves = new ArrayList<>(opponentMoves.keySet());
                    BoardSpace randomMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
                    executeMove(simBoard, randomMove, opponentMoves.get(randomMove), simOpponent, simCurrentPlayer);
                }
            } else {
                // current player move
                List<BoardSpace> possibleMoves = new ArrayList<>(moves.keySet());
                BoardSpace randomMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
                executeMove(simBoard, randomMove, moves.get(randomMove), simCurrentPlayer, simOpponent);
            }
            
            // swap players
            Player temp = simCurrentPlayer;
            simCurrentPlayer = simOpponent;
            simOpponent = temp;
        }

        // calculate win/lose
        int currentPlayerCount = 0;
        int opponentCount = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (simBoard[i][j].getType() == currentPlayer.getColor()) {
                    currentPlayerCount++;
                } else if (simBoard[i][j].getType() == opponent.getColor()) {
                    opponentCount++;
                }
            }
        }

        return currentPlayerCount > opponentCount;
    }

    /**
     * PART 4
     * Updates the node's statistics after a simulation.
     * Propagates the result up the tree.
     * @param won The result of the simulation (1 for win, 0 for loss)
     */
    public void backPropagate(boolean won) {
        MCTSNode node = this;
        while (node != null) {
            node.visits++;
            if (won) {
                node.wins++;
            }
            node = node.parent;
        }
    }

    /**
     * PART 5
     * Determines the best move based on the win rate of child nodes.
     * Selects the child node with the highest win rate after all simulations.
     * @return The BoardSpace representing the best move, or null if no children exist
     */
    public BoardSpace getBestMove() {
        MCTSNode bestChild = null;
        double bestWinRate = -1;

        for (MCTSNode child : children) {
            double winRate = (double) child.wins / child.visits;
            if (winRate > bestWinRate) {
                bestWinRate = winRate;
                bestChild = child;
            }
        }

        return bestChild != null ? bestChild.move : null;
    }

    /**
     * PART 6
     * Creates a deep copy of the game board.
     * Uses the BoardSpace flyweight pattern to maintain memory efficiency.
     * @param board The board to copy
     * @return A new board with the same state as the input board
     */
    public BoardSpace[][] copyBoard(BoardSpace[][] board) {
        BoardSpace[][] newBoard = new BoardSpace[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                newBoard[i][j] = BoardSpace.getBoardSpace(i, j, board[i][j].getType());
            }
        }
        return newBoard;
    }

    /**
     * PART 7
     * Executes a move on the board and updates player ownership.
     * Places a piece at the specified location and flips all captured pieces.
     * @param board The game board to modify
     * @param move The move to execute
     * @param flips The list of pieces to flip
     * @param current The player making the move
     * @param opponent The opposing player
     */
    public void executeMove(BoardSpace[][] board, BoardSpace move, List<BoardSpace> flips,
                     Player current, Player opponent) {
        board[move.getX()][move.getY()] = BoardSpace.getBoardSpace(
                move.getX(), move.getY(), current.getColor());
        current.addOwnedSpace(board[move.getX()][move.getY()]);
        for (BoardSpace dest : flips) {
            flipPiecesBetween(move, dest, board, current, opponent);
        }
    }

    /**
     * PART 8
     * Flips all pieces between two positions on the board.
     * Updates player ownership of the flipped pieces.
     * Uses the BoardSpace flyweight pattern for piece updates.
     * @param start The starting position
     * @param end The ending position
     * @param board The game board to modify
     * @param current The player gaining ownership
     * @param opponent The player losing ownership
     */
    private void flipPiecesBetween(BoardSpace start, BoardSpace end, BoardSpace[][] board,
                                   Player current, Player opponent) {
        int x1 = start.getX();
        int y1 = start.getY();
        int x2 = end.getX();
        int y2 = end.getY();

        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);

        int x = x1 + dx;
        int y = y1 + dy;

        // Move along the line between start and end
        while (x != x2 || y != y2) {
            BoardSpace cur = board[x][y];
            // Update the board reference
            // Get the flyweight instance for the new state
            BoardSpace curNew = BoardSpace.getBoardSpace(x, y, current.getColor());
            // Update the board reference
            board[x][y] = curNew;

            x += dx;
            y += dy;

            // Update ownership lists
            opponent.removeOwnedSpace(cur);
            current.addOwnedSpace(curNew);
        }
    }

    /**
     * Gets the list of child nodes.
     * @return The children of this node
     */
    public List<MCTSNode> getChildren() {
        return children;
    }

    /**
     * Gets the number of times this node has been visited.
     * @return The visit count
     */
    public int getVisits() {
        return visits;
    }

    /**
     * Gets the number of wins recorded for this node.
     * @return The win count
     */
    public int getWins() {
        return wins;
    }

    /**
     * Gets the current player.
     * @return The player whose turn it is
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the opposing player.
     * @return The opponent player
     */
    public Player getOpponent() {
        return opponent;
    }

    /**
     * Gets the move that led to this node.
     * @return The move that created this node
     */
    public BoardSpace getMove() {
        return move;
    }

    /**
     * Gets the current game board state.
     * @return The board state at this node
     */
    public BoardSpace[][] getBoard() {
        return board;
    }

    /**
     * Gets the parent node.
     * @return The parent of this node
     */
    public MCTSNode getParent() {
        return parent;
    }
} 