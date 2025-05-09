package othello.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import othello.gamelogic.BoardSpace;
import othello.gamelogic.ComputerPlayer;
import othello.gamelogic.HumanPlayer;
import othello.gamelogic.OthelloGame;
import othello.gamelogic.Player;
import othello.gui.GUISpace;

import java.util.List;
import java.util.Map;

/**
 * Controls the main GUI of the Othello game, managing game flow,
 * user interaction, board rendering, and a per-turn countdown timer.
 *
 * This controller communicates between the game model ({@link OthelloGame})
 * and the JavaFX view components. It handles input from both human and computer players,
 * updates the GUI board, and enforces rules such as turn skipping and game-over detection.
 *
 * UI components are wired using FXML.
 */
public class GameController {
    /** Label showing the current turn and score. */
    @FXML private Label turnLabel;

    /** Pane container for the game board. */
    @FXML private Pane gameBoard;

    /** Colored circle indicating the current player's turn. */
    @FXML private Circle turnCircle;

    /** Button to trigger a computer move (visible only for AI turns). */
    @FXML private Button computerTurnBtn;

    /** Label displaying the remaining time for the current turn. */
    @FXML private Label timerLabel;

    /** The main Othello game logic instance. */
    private OthelloGame og;

    /** 2D array representing the GUI version of the board. */
    private GUISpace[][] guiBoard;

    /** The player whose turn it is. */
    private Player currentPlayer;

    /** Number of consecutive turns skipped due to no valid moves. */
    private int skippedTurns;

    /** JavaFX timeline used for countdown timer. */
    private Timeline turnTimer;

    /** Time remaining in seconds for the current turn. */
    private int timeRemaining;

    /**
     * Called by App.start(): initialize game and UI.
     */
    public void initGame(String arg1, String arg2) {
        Player p1;
        if ("human".equals(arg1)) {
            p1 = new HumanPlayer(BoardSpace.SpaceType.BLACK);
        } else {
            p1 = new ComputerPlayer(arg1);
        }
        p1.setColor(BoardSpace.SpaceType.BLACK);

        Player p2;
        if ("human".equals(arg2)) {
            p2 = new HumanPlayer(BoardSpace.SpaceType.WHITE);
        } else {
            p2 = new ComputerPlayer(arg2);
        }
        p2.setColor(BoardSpace.SpaceType.WHITE);

        og = new OthelloGame(p1, p2);
        guiBoard = new GUISpace[8][8];
        displayBoard();
        initSpaces();

        skippedTurns = 0;
        currentPlayer = p1;
        updateTurnText();
        startTurnTimer();
        takeTurn(currentPlayer);
    }

    /**
     * Displays the game board using the current state of {@link OthelloGame}.
     * Called at initialization and after each move.
     */
    @FXML
    protected void displayBoard() {
        gameBoard.getChildren().clear();
        for (BoardSpace[] row : og.getBoard()) {
            for (BoardSpace space : row) {
                GUISpace gs = new GUISpace(space.getX(), space.getY(), space.getType());
                gameBoard.getChildren().add(gs.getSquare());
                guiBoard[space.getX()][space.getY()] = gs;
            }
        }
    }

    /**
     * Clears all graphical board elements from the game pane.
     */
    @FXML
    protected void clearBoard() {
        gameBoard.getChildren().clear();
    }

    /**
     * Places the initial four discs on the board (2 black, 2 white) in the center.
     */
    @FXML
    protected void initSpaces() {
        guiBoard[3][3].addOrUpdateDisc(BoardSpace.SpaceType.WHITE);
        guiBoard[4][4].addOrUpdateDisc(BoardSpace.SpaceType.WHITE);
        guiBoard[3][4].addOrUpdateDisc(BoardSpace.SpaceType.BLACK);
        guiBoard[4][3].addOrUpdateDisc(BoardSpace.SpaceType.BLACK);
    }

    /**
     * Begins the current player's turn and handles timer setup and move display.
     *
     * @param player the player taking the turn
     */
    @FXML
    protected void takeTurn(Player player) {
        currentPlayer = player;
        updateTurnText();
        startTurnTimer();

        if (player instanceof HumanPlayer) {
            computerTurnBtn.setVisible(false);
            showMoves((HumanPlayer) player);
        } else {
            computerTurnBtn.setVisible(true);
            computerTurnBtn.setOnAction(e -> computerDecision((ComputerPlayer) player));
        }
    }

    /**
     * Highlights legal moves for a human player and enables click handlers.
     *
     * @param player the human player taking a turn
     */
    @FXML
    protected void showMoves(HumanPlayer player) {
        displayBoard();
        Map<BoardSpace, List<BoardSpace>> moves = og.getAvailableMoves(player);
        if (moves == null) {
            turnLabel.setText("Error: getAvailableMoves() returned null");
            return;
        }
        if (moves.isEmpty()) {
            handleNoMoves();
            return;
        }
        skippedTurns = 0;
        moves.forEach((dest, origins) -> {
            GUISpace gs = guiBoard[dest.getX()][dest.getY()];
            Pane sq = gs.getSquare();
            gs.setBgColor(Color.LIGHTYELLOW);
            EventHandler<MouseEvent> enter = e -> gs.setBgColor(Color.LIME);
            EventHandler<MouseEvent> exit  = e -> gs.setBgColor(Color.LIGHTYELLOW);
            sq.addEventHandler(MouseEvent.MOUSE_ENTERED, enter);
            sq.addEventHandler(MouseEvent.MOUSE_EXITED, exit);
            sq.setOnMouseClicked(e -> selectSpace(player, moves, dest));
        });
    }


    /**
     * Executes a move for a computer player using its decision algorithm.
     *
     * @param player the computer-controlled player
     */
    @FXML
    protected void computerDecision(ComputerPlayer player) {
        Map<BoardSpace, List<BoardSpace>> moves = og.getAvailableMoves(player);
        if (moves == null || moves.isEmpty()) {
            handleNoMoves();
            return;
        }
        skippedTurns = 0;
        BoardSpace dest = og.computerDecision(player);
        processMove(player, dest, moves);
    }

    private void handleNoMoves() {
        skippedTurns++;
        if (skippedTurns >= 2 || totalPieces() == 64) {
            gameOver();
        } else {
            turnLabel.setText("Skipped " + currentPlayer.getColor() + "!");
            takeTurn(otherPlayer());
        }
    }

    /**
     * Handles user selection of a space to move.
     *
     * @param player the player making the move
     * @param moves map of valid destinations and corresponding flipped discs
     * @param dest the chosen move location
     */
    @FXML
    protected void selectSpace(Player player,
                               Map<BoardSpace, List<BoardSpace>> moves,
                               BoardSpace dest) {
        processMove(player, dest, moves);
    }

    /**
     * Processes the logic of capturing spaces and updating the game state after a move.
     *
     * @param player the player making the move
     * @param dest the selected destination space
     * @param moves available moves and affected directions
     */
    private void processMove(Player player,
                             BoardSpace dest,
                             Map<BoardSpace, List<BoardSpace>> moves) {
        stopTurnTimer();
        og.takeSpaces(player, otherPlayer(), moves, dest);
        displayBoard();
        initSpaces();
        takeTurn(otherPlayer());
    }

    /**
     * Updates the UI to reflect the current turn, including score and player type.
     */
    private void updateTurnText() {
        int p1 = og.getPlayerOne().getPlayerOwnedSpacesSpaces().size();
        int p2 = og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size();
        turnCircle.setFill(currentPlayer.getColor().fill());
        turnLabel.setText(currentPlayer.getColor() + "'s Turn (" +
                (currentPlayer instanceof HumanPlayer ? "Human" : "Computer") + ")\n" +
                "B: " + p1 + " - W: " + p2);
    }

    /**
     * Displays the final score and winner at game end.
     */
    @FXML
    protected void gameOver() {
        int p1 = og.getPlayerOne().getPlayerOwnedSpacesSpaces().size();
        int p2 = og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size();
        String result = p1 == p2 ? "Tie!" : (p1 > p2 ? "Black wins!" : "White wins!");
        turnLabel.setText("GAME OVER: " + result + " (" + p1 + "-" + p2 + ")");
        stopTurnTimer();
    }

    /**
     * Starts or resets the turn timer to 30 seconds and updates the timer label.
     */
    private void startTurnTimer() {
        stopTurnTimer();
        timeRemaining = 30;
        timerLabel.setText(String.valueOf(timeRemaining));
        turnTimer = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
            timeRemaining--;
            timerLabel.setText(String.valueOf(timeRemaining));
            if (timeRemaining <= 0) onTurnTimeout();
        }));
        turnTimer.setCycleCount(timeRemaining);
        turnTimer.play();
    }

    /**
     * Stops and clears the current turn timer if one is running.
     */
    private void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
            turnTimer = null;
        }
    }

    /**
     * Called when the turn timer reaches zero; automatically skips the player's turn.
     */
    private void onTurnTimeout() {
        turnLabel.setText("Time up for " + currentPlayer.getColor());
        handleNoMoves();
    }

    /**
     * Calculates the total number of discs currently on the board.
     *
     * @return the total number of placed pieces
     */
    private int totalPieces() {
        return og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() +
                og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size();
    }

    /**
     * Returns the player who is not currently taking a turn.
     *
     * @return the opposing player
     */
    private Player otherPlayer() {
        return currentPlayer == og.getPlayerOne() ? og.getPlayerTwo() : og.getPlayerOne();
    }
}