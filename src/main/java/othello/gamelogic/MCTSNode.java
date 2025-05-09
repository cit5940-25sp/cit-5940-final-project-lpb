package othello.gamelogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    // simulates the terminal state of chosen child using random moves,
    // returns whether current child results in a win
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

    public BoardSpace[][] copyBoard(BoardSpace[][] board) {
        BoardSpace[][] newBoard = new BoardSpace[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                newBoard[i][j] = BoardSpace.getBoardSpace(i, j, board[i][j].getType());
            }
        }
        return newBoard;
    }

    public void executeMove(BoardSpace[][] board, BoardSpace move, List<BoardSpace> flips,
                     Player current, Player opponent) {
        board[move.getX()][move.getY()] = BoardSpace.getBoardSpace(
                move.getX(), move.getY(), current.getColor());
        for (BoardSpace dest : flips) {
            flipPiecesBetween(move, dest, board, current, opponent);
        }
    }

    private void flipPiecesBetween(BoardSpace start, BoardSpace end, BoardSpace[][] board,
                                   Player current, Player opponent) {
        int x1 = start.getX();
        int y1 = start.getY();
        int x2 = end.getX();
        int y2 = end.getY();

        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);

        int x = x1;
        int y = y1;

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

    public List<MCTSNode> getChildren() {
        return children;
    }

    public int getVisits() {
        return visits;
    }

    public int getWins() {
        return wins;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getOpponent() {
        return opponent;
    }

    public BoardSpace getMove() {
        return move;
    }

    public BoardSpace[][] getBoard() {
        return board;
    }

    public MCTSNode getParent() {
        return parent;
    }
} 