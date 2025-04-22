package othello.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.util.Duration;
import othello.gamelogic.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.util.Duration;

/**
 * Manages the interaction between model and view of the game.
 */
public class GameController  {

    // FXML Variables to manipulate UI components
    @FXML
    private Label turnLabel;

    @FXML
    private Pane gameBoard;

    @FXML
    private Circle turnCircle;

    @FXML
    private Button computerTurnBtn;

    @FXML
    private Label scoreLabel;
    @FXML
    private Label timeLabel;
    private OthelloGame og;

    private Timeline gameTimer;
    private int timeRemaining = 30;

    // Private variables
    @FXML
    private Button startButton;

    private int skippedTurns;
    private GUISpace[][] guiBoard;



    @FXML
    public void initialize() {
        System.out.println("Initializing components...");
        System.out.println("computerTurnBtn: " + (computerTurnBtn != null));

        // 只初始化计时器但不启动
        gameTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    if (og != null) { // 确保og已初始化
                        timeRemaining--;
                        updateTimeDisplay();
                        if (timeRemaining <= 0) {
                            timeOut();
                        }
                    }
                })
        );
        gameTimer.setCycleCount(Timeline.INDEFINITE);
    }

    private void initGameTimer() {
        if (timeLabel == null) return;

        // 添加计时器样式类
        timeLabel.getStyleClass().add("dynamic-timer");

        gameTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    timeRemaining--;
                    updateTimeDisplay();
                    if (timeRemaining <= 0) timeOut();
                })
        );
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        resetTimer();
    }

    private void updateTimeDisplay() {
        if (timeLabel != null) {
            Platform.runLater(() -> {
                // 使用更生动的显示格式
                String timerText = String.format("⏱ %02d", timeRemaining);
                timeLabel.setText(timerText);

                if (timeRemaining <= 10) {
                    timeLabel.getStyleClass().add("urgent");
                    flashTimerWarning();
                } else {
                    timeLabel.getStyleClass().remove("urgent");
                }
            });
        }
    }
    private void flashTimerWarning() {
        if (timeLabel != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(300), timeLabel);
            ft.setFromValue(1.0);
            ft.setToValue(0.3);
            ft.setCycleCount(4);
            ft.setAutoReverse(true);
            ft.play();
        }
    }

    private void timeOut() {
        System.out.println("Time out for " + og.getCurrentPlayer().getColor() + " player!");

        // 1. 停止当前计时器
        if (gameTimer != null) {
            gameTimer.stop();
        }

        // 2. 显示超时信息
        Platform.runLater(() -> {
            turnLabel.setText("TIME OUT! " + og.getCurrentPlayer().getColor() + " skipped!");
        });

        // 3. 保存当前玩家引用
        Player timedOutPlayer = og.getCurrentPlayer();

        // 4. 直接切换到对手回合（无论当前是Human还是AI）
        og.switchPlayer(); // 确保OthelloGame中已实现switchPlayer()
        resetTimer();      // 重置对手的计时器

        // 5. 处理游戏流程
        if (timedOutPlayer instanceof HumanPlayer) {
            // 如果是人类玩家超时，立即开始对手回合
            takeTurn(og.getCurrentPlayer());
        } else {
            // 如果是AI超时（虽然不太可能），也切换回合
            takeTurn(og.getCurrentPlayer());
        }

        // 6. 检查是否游戏结束
        checkGameEndCondition();
    }

    private void checkGameEndCondition() {
        // 获取双方玩家的合法移动
        Map<BoardSpace, List<BoardSpace>> p1Moves = og.getAvailableMoves(og.getPlayerOne());
        Map<BoardSpace, List<BoardSpace>> p2Moves = og.getAvailableMoves(og.getPlayerTwo());

        // 检查游戏结束条件
        boolean boardFull = og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() +
                og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size() == 64;
        boolean noValidMoves = p1Moves.isEmpty() && p2Moves.isEmpty();

        if (boardFull || noValidMoves) {
            gameOver();
        }
    }

    private void resetTimer() {
        if (og == null) return;

        timeRemaining = 30; // 重置为30秒
        updateTimeDisplay();

        // 只有当前是人类玩家时才启动计时器
        if (og.getCurrentPlayer() instanceof HumanPlayer) {
            if (gameTimer != null) {
                gameTimer.playFromStart();
            }
        } else {
            // 如果是AI回合，停止计时器（AI不需要计时）
            if (gameTimer != null) {
                gameTimer.stop();
            }
        }
    }
    /**
     * Starts the game, called after controller initialization  in start method of App.
     * Sets the 2 players, their colors, and creates an OthelloGame for logic handling.
     * Then, shows the first move, beginning the game "loop".
     * @param arg1 type of player for player 1, either "human" or some computer strategy
     * @param arg2 type of player for player 2, either "human" or some computer strategy
     */
    public void initGame(String arg1, String arg2) {
        // 1. UI组件空检查（新增startButton检查）
        if (gameBoard == null || turnLabel == null || turnCircle == null ||
                timeLabel == null || startButton == null) {
            System.err.println("Error: Some UI components are not initialized!");
            Platform.exit();
            return;
        }

        // 2. 初始化玩家
        Player playerOne = arg1.equals("human") ?
                new HumanPlayer(BoardSpace.SpaceType.BLACK) :
                new ComputerPlayer(arg1);
        Player playerTwo = arg2.equals("human") ?
                new HumanPlayer(BoardSpace.SpaceType.WHITE) :
                new ComputerPlayer(arg2);

        // 3. 设置颜色
        playerOne.setColor(BoardSpace.SpaceType.BLACK);
        playerTwo.setColor(BoardSpace.SpaceType.WHITE);

        // 4. 初始化游戏逻辑
        og = new OthelloGame(playerOne, playerTwo);
        guiBoard = new GUISpace[8][8];
        displayBoard();
        initSpaces();

        // 5. 初始化但不启动计时器
        timeRemaining = 30;
        updateTimeDisplay();
        gameTimer.stop();

        // 6. 准备阶段UI设置
        Platform.runLater(() -> {
            turnText(playerOne); // 显示当前玩家信息
            startButton.setVisible(true); // 显示准备按钮
            startButton.setText("READY TO START");
            computerTurnBtn.setVisible(false); // 隐藏电脑回合按钮

            // 禁用棋盘交互直到准备完成
            gameBoard.setDisable(true);
        });
    }

    /**
     * Displays the board initially, adding the GUI squares into the window.
     * Also adds the initial state of the board with black and white taking spaces at the center.
     */
    @FXML
    protected void displayBoard() {
        BoardSpace[][] board = og.getBoard();
        double boardWidth = board.length * GUISpace.SQUARE_SIZE;
        double boardHeight = board[0].length * GUISpace.SQUARE_SIZE;
        gameBoard.setPrefSize(boardWidth, boardHeight);

        for (BoardSpace[] spaces : board) {
            for (BoardSpace space : spaces) {
                GUISpace guiSpace = new GUISpace(space.getX(), space.getY(), space.getType());
                Pane square = guiSpace.getSquare();
                gameBoard.getChildren().add(square);
                guiBoard[space.getX()][space.getY()] = guiSpace;
            }
        }
    }

    /**
     * Clears the board visually, called every time the board is redisplayed after the first time
     */
    @FXML
    protected void clearBoard() {
        BoardSpace[][] board = og.getBoard();
        for (BoardSpace[] spaces : board) {
            for (BoardSpace space : spaces) {
                GUISpace guiSpace = guiBoard[space.getX()][space.getY()];
                Pane square = guiSpace.getSquare();
                gameBoard.getChildren().remove(square);
            }
        }
    }

    /**
     * Sets the initial state of the Othello board
     */
    @FXML
    protected void initSpaces(){
        // Initial spaces
        guiBoard[3][3].addOrUpdateDisc(BoardSpace.SpaceType.WHITE);
        guiBoard[4][4].addOrUpdateDisc(BoardSpace.SpaceType.WHITE);
        guiBoard[3][4].addOrUpdateDisc(BoardSpace.SpaceType.BLACK);
        guiBoard[4][3].addOrUpdateDisc(BoardSpace.SpaceType.BLACK);
    }

    /**
     * Displays the score of the board and the current turn.
     */
    @FXML
    protected void turnText(Player player) {
        // 1. 设置棋子颜色（已移到独立显示区域）
        turnCircle.setFill(player.getColor().fill());

        // 2. 生成状态文本（移除了emoji，因为棋子已独立显示）
        String playerType = player instanceof HumanPlayer ? "Human" : "AI";
        String statusText = String.format("%s's Turn\n(%s)",
                player.getColor(),
                playerType);

        // 3. 更新状态标签
        turnLabel.setText(statusText);

        // 4. 更新得分显示（优化格式）
        if (scoreLabel != null && og != null) {
            int blackScore = og.getPlayerOne().getPlayerOwnedSpacesSpaces().size();
            int whiteScore = og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size();

            // 更简洁的得分格式（去掉了重复的emoji）
            scoreLabel.setText(String.format("Black: %02d   White: %02d",
                    blackScore,
                    whiteScore));

            // 5. 动态调整得分文字颜色
            Platform.runLater(() -> {
                if (blackScore > whiteScore) {
                    scoreLabel.setStyle("-fx-text-fill: #000000;");
                } else if (whiteScore > blackScore) {
                    scoreLabel.setStyle("-fx-text-fill: #ffffff;");
                } else {
                    scoreLabel.setStyle("-fx-text-fill: #cccccc;");
                }
            });
        }

        // 6. 添加进度条支持（需在FXML中添加ProgressBar组件）
        // if (progressBar != null) {
        //     double progress = (blackScore + whiteScore) / 64.0;
        //     progressBar.setProgress(progress);
        // }
    }

    /**
     * Displays the score of the board.
     */
    @FXML
    protected void skipTurnText(Player player) {
        turnLabel.setText(
                "Skipped " + player.getColor() + " due to no moves available! " + otherPlayer(player).getColor() + "'s Turn\n" +
                        og.getPlayerOne().getColor() + ": " + og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() + " - " +
                        og.getPlayerTwo().getColor() + ": " + og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size());
    }

    /**
     * Either shows moves for humans or makes a decision for a computer.
     * @param player player to take a turn for, whether its human or computer
     */
    @FXML
    protected void takeTurn(Player player) {
        if (computerTurnBtn == null) {
            System.err.println("Warning: computerTurnBtn is null!");
            return;
        }

        Objects.requireNonNull(computerTurnBtn, "Computer button not initialized");

        if (player instanceof HumanPlayer human) {
            computerTurnBtn.setVisible(false);
            showMoves(human);
        } else if (player instanceof ComputerPlayer computer) {
            computerTurnBtn.setVisible(true);
            computerTurnBtn.setOnAction(actionEvent -> {
                computerDecision(computer);
            });
        }
    }

    /**
     * Shows the current moves possible for the current board configuration.
     * If availableMoves is null, the getAvailableMoves method is likely not implemented yet.
     * If availableMoves is empty (no moves found, or full board), the game ends.
     * @param player player to show moves for
     */
    @FXML
    protected void showMoves(HumanPlayer player) {
        Map<BoardSpace, List<BoardSpace>> availableMoves = og.getAvailableMoves(player);
        if (availableMoves == null) {
            turnLabel.setText("Null move found for \n" + player.getColor() + "! \n Please implement \ngetAvailableMoves()!");
        } else if (availableMoves.size() == 0) {
            if (og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() + og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size() != 64 && skippedTurns != 2) {
                skipTurnText(player);
                takeTurn(otherPlayer(player));
                skippedTurns++;
            } else if (skippedTurns == 2 || og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() + og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size() == 64){
                gameOver();
            }

        } else {
            skippedTurns = 0;
            for (BoardSpace destination : availableMoves.keySet()) {
                // Attach hover listener (Enter, Exit) to each Pane
                GUISpace guiSpace = guiBoard[destination.getX()][destination.getY()];
                Pane currPane = guiSpace.getSquare();
                guiSpace.setBgColor(Color.LIGHTYELLOW);
                EventHandler<MouseEvent> enter = event -> guiSpace.setBgColor(Color.LIME);
                EventHandler<MouseEvent> exit = event -> guiSpace.setBgColor(Color.LIGHTYELLOW);
                currPane.addEventHandler(MouseEvent.MOUSE_ENTERED, enter);
                currPane.addEventHandler(MouseEvent.MOUSE_EXITED, exit);
                // Click removes hovers
                currPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    currPane.removeEventHandler(MouseEvent.MOUSE_ENTERED, enter);
                    currPane.removeEventHandler(MouseEvent.MOUSE_EXITED, exit);
                    selectSpace(player, availableMoves, destination);
                });
            }
        }

    }

    /**
     * Gets the computer decision, then selects the space.
     * @paramplayer a reference to the current computer player (could be player 1 or 2)
     */
    @FXML
    protected void computerDecision(ComputerPlayer computer) {
        Map<BoardSpace, List<BoardSpace>> availableMoves = og.getAvailableMoves(computer);
        if (availableMoves == null || availableMoves.isEmpty()) {
            if (og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() +
                    og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size() != 64 && skippedTurns != 2) {
                skipTurnText(computer);
                takeTurn(otherPlayer(computer));
                skippedTurns++;
            } else {
                gameOver();
            }
            return;
        }

        try {
            BoardSpace selectedDestination = og.computerDecision(computer);
            og.takeSpaces(computer, otherPlayer(computer), availableMoves, selectedDestination);
            updateGUIBoardWithAnimation(computer, availableMoves, selectedDestination);

            clearBoard();
            displayBoard();
            turnText(otherPlayer(computer));
            takeTurn(otherPlayer(computer));

            resetTimer();
        } catch (IllegalStateException e) {
            // 处理AI返回null的情况
            skipTurnText(computer);
            takeTurn(otherPlayer(computer));
        }

        checkGameEndCondition();

    }

    /**
     * Handles what happens when a player chooses to select a certain space during their turn.
     * @param player current turn's player
     * @param availableMoves the available moves gotten from showMoves earlier
     * @param selectedDestination the selected destination space that was clicked on
     */
    @FXML
    protected void selectSpace(Player player, Map<BoardSpace, List<BoardSpace>> availableMoves, BoardSpace selectedDestination) {
        // Remove other handlers by reinitializing empty spaces where they are
        for (BoardSpace destination : availableMoves.keySet()) {
            GUISpace guiSpace = guiBoard[destination.getX()][destination.getY()];
            if (destination != selectedDestination) {
                // Reinit unselected spaces, to remove event handlers
                og.getBoard()[destination.getX()][destination.getY()] =
                        new BoardSpace(destination.getX(), destination.getY(), BoardSpace.SpaceType.EMPTY);
                gameBoard.getChildren().remove(guiSpace.getSquare());
                GUISpace newGuiSpace = new GUISpace(destination.getX(), destination.getY(), BoardSpace.SpaceType.EMPTY);
                Pane newSquare = newGuiSpace.getSquare();
                gameBoard.getChildren().add(newSquare);
                guiBoard[destination.getX()][destination.getY()] = guiSpace;
            } else {
                og.getBoard()[destination.getX()][destination.getY()] =
                        new BoardSpace(destination.getX(), destination.getY(), player.getColor());
                gameBoard.getChildren().remove(guiSpace.getSquare());
                GUISpace newGuiSpace = new GUISpace(destination.getX(), destination.getY(), player.getColor());
                Pane newSquare = newGuiSpace.getSquare();
                gameBoard.getChildren().add(newSquare);
                guiBoard[destination.getX()][destination.getY()] = guiSpace;
            }
        }

        // Recolor the bg of the destination
        GUISpace guiSpace = guiBoard[selectedDestination.getX()][selectedDestination.getY()];
        guiSpace.setBgColor(Color.LIMEGREEN);

        // From all origins, path to the destination and take spaces
        og.takeSpaces(player, otherPlayer(player), availableMoves, selectedDestination);
        updateGUIBoardWithAnimation(player, availableMoves, selectedDestination);

        // Redisplay the new board
        clearBoard();
        displayBoard();

        // Next opponent turn
        turnText(otherPlayer(player));
        takeTurn(otherPlayer(player));

        resetTimer();
    }

    /**
     * Updates the GUI Board by adding or updating discs from all origins to a given destination
     * @param player player that is taking a turn
     * @param availableMoves the list of all available destinations and origins
     * @param selectedDestination the selected destination from either user input or computer decision
     */
    @FXML
    protected void updateGUIBoardWithAnimation(Player player,
                                               Map<BoardSpace, List<BoardSpace>> availableMoves,
                                               BoardSpace selectedDestination) {
        // 1. 放置新棋子动画
        GUISpace destinationSpace = guiBoard[selectedDestination.getX()][selectedDestination.getY()];
        Circle disc = destinationSpace.getDisc();

        if (disc != null) {
            ScaleTransition placeAnim = new ScaleTransition(Duration.millis(200), disc);
            placeAnim.setFromX(0.1);
            placeAnim.setFromY(0.1);
            placeAnim.setToX(1.0);
            placeAnim.setToY(1.0);
            placeAnim.play();
        }

        // 2. 翻转动画
        Timeline flipTimeline = new Timeline();
        int delay = 0;

        for (BoardSpace origin : availableMoves.get(selectedDestination)) {
            List<BoardSpace> path = calculatePath(origin, selectedDestination);

            for (BoardSpace space : path) {
                GUISpace guiSpace = guiBoard[space.getX()][space.getY()];
                Circle flipDisc = guiSpace.getDisc();

                if (flipDisc != null) {
                    KeyFrame flipFrame = new KeyFrame(
                            Duration.millis(delay),
                            e -> {
                                RotateTransition rt = new RotateTransition(Duration.millis(150), flipDisc);
                                rt.setFromAngle(0.0);
                                rt.setToAngle(180.0);
                                rt.setOnFinished(event -> guiSpace.addOrUpdateDisc(player.getColor()));
                                rt.play();
                            }
                    );
                    flipTimeline.getKeyFrames().add(flipFrame);
                    delay += 100;
                }
            }
        }

        flipTimeline.setOnFinished(e -> {
            clearBoard();
            displayBoard();
            resetTimer();
        });
        flipTimeline.play();
    }

    private List<BoardSpace> calculatePath(BoardSpace start, BoardSpace end) {
        List<BoardSpace> path = new ArrayList<>();
        int x1 = start.getX(), y1 = start.getY();
        int x2 = end.getX(), y2 = end.getY();

        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);

        int x = x1 + dx;
        int y = y1 + dy;

        while (x != x2 || y != y2) {
            path.add(og.getBoard()[x][y]);
            x += dx;
            y += dy;
        }

        return path;
    }

    private void addFlipAnimation(Timeline timeline, int x, int y, BoardSpace.SpaceType type, int delay) {
        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(delay),
                e -> guiBoard[x][y].addOrUpdateDisc(type)
        );
        timeline.getKeyFrames().add(keyFrame);
    }

    /**
     * Returns the other player given one of the player fields
     */
    @FXML
    protected Player otherPlayer(Player player) {
        if (player == og.getPlayerOne()) {
            return og.getPlayerTwo();
        } else {
            return og.getPlayerOne();
        }
    }

    /**
     * Ends the game.
     */
    @FXML
    protected void gameOver() {
        boolean p1Victory = false;
        boolean tie = false;
        if (og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() > og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size()) {
            p1Victory = true;
        } else if (og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() == og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size()) {
            tie = true;
        }
        if (tie) {
            turnLabel.setText("GAME OVER! Game Tied with scores: \n " +
                    og.getPlayerOne().getColor() + ": " + og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() + " - " +
                    og.getPlayerTwo().getColor() + ": " + og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size());
        } else if (p1Victory) {
            turnLabel.setText("GAME OVER! BLACK wins with scores: \n " +
                    og.getPlayerOne().getColor() + ": " + og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() + " - " +
                    og.getPlayerTwo().getColor() + ": " + og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size());
        } else {
            turnLabel.setText("GAME OVER! WHITE wins with scores: \n " +
                    og.getPlayerOne().getColor() + ": " + og.getPlayerOne().getPlayerOwnedSpacesSpaces().size() + " - " +
                    og.getPlayerTwo().getColor() + ": " + og.getPlayerTwo().getPlayerOwnedSpacesSpaces().size());
        }
    }

    @FXML
    protected void computerDecision(ActionEvent event) {
        if (og != null && og.getCurrentPlayer() instanceof ComputerPlayer) {
            computerDecision((ComputerPlayer) og.getCurrentPlayer());
        }
    }

    @FXML
    protected void startGame(ActionEvent event) {
        // 1. 隐藏准备按钮
        startButton.setVisible(false);

        // 2. 启用棋盘交互
        gameBoard.setDisable(false);

        // 3. 显示开始提示
        turnLabel.setText("GAME STARTED!");

        // 4. 如果是人类玩家先手，启动计时器
        if (og.getCurrentPlayer() instanceof HumanPlayer) {
            gameTimer.playFromStart();
        }

        // 5. 开始第一个回合
        takeTurn(og.getCurrentPlayer());

        // 6. 添加视觉反馈
        FadeTransition ft = new FadeTransition(Duration.seconds(1), turnLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0.3);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }
}