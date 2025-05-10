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

import java.util.List;
import java.util.Map;

/**
 * Manages the interaction between model and view of the game, with a 30s per-turn timer.
 */
public class GameController {

    @FXML private Label turnLabel;
    @FXML private Pane gameBoard;
    @FXML private Circle turnCircle;
    @FXML private Button computerTurnBtn;
    @FXML private Label timerLabel;

    private OthelloGame og;
    private GUISpace[][] guiBoard;
    private Player currentPlayer;
    private int skippedTurns;

    // Timer
    private Timeline turnTimer;
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

    @FXML
    protected void clearBoard() {
        gameBoard.getChildren().clear();
    }

    @FXML
    protected void initSpaces() {
        guiBoard[3][3].addOrUpdateDisc(BoardSpace.SpaceType.WHITE);
        guiBoard[4][4].addOrUpdateDisc(BoardSpace.SpaceType.WHITE);
        guiBoard[3][4].addOrUpdateDisc(BoardSpace.SpaceType.BLACK);
        guiBoard[4][3].addOrUpdateDisc(BoardSpace.SpaceType.BLACK);
    }

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

    @FXML
    protected void selectSpace(Player player,
                               Map<BoardSpace, List<BoardSpace>> moves,
                               BoardSpace dest) {
        processMove(player, dest, moves);
    }

    private void processMove(Player player,
                             BoardSpace dest,
                             Map<BoardSpace, List<BoardSpace>> moves) {
        stopTurnTimer();
        og.takeSpaces(player, otherPlayer(), moves, dest);
        displayBoard();
        takeTurn(otherPlayer());
    }

    private void updateTurnText() {
        int p1 = og.getPlayerOne().getPlayerOwnedSpacesSpaces().size();
        int p2 = og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size();
        turnCircle.setFill(currentPlayer.getColor().fill());
        turnLabel.setText(currentPlayer.getColor() + "'s Turn (" +
                (currentPlayer instanceof HumanPlayer ? "Human" : "Computer") + ")\n" +
                "B: " + p1 + " - W: " + p2);
    }

    @FXML
    protected void gameOver() {
        int p1 = og.getPlayerOne().getPlayerOwnedSpacesSpaces().size();
        int p2 = og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size();
        String result = p1 == p2 ? "Tie!" : (p1 > p2 ? "Black wins!" : "White wins!");
        turnLabel.setText("GAME OVER: " + result + " (" + p1 + "-" + p2 + ")");
        stopTurnTimer();
    }

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

    private void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
            turnTimer = null;
        }
    }

    private void onTurnTimeout() {
        turnLabel.setText("Time up for " + currentPlayer.getColor());
        handleNoMoves();
    }

    private int totalPieces() {
        return og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() +
                og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size();
    }

    private Player otherPlayer() {
        return currentPlayer == og.getPlayerOne() ? og.getPlayerTwo() : og.getPlayerOne();
    }
}