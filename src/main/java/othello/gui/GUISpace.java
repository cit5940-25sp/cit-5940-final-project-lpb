package othello.gui;

import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import othello.gamelogic.BoardSpace;


/**
 * Represents a space on the GUI board.
 * Includes a Pane that hosts a Rectangle for a background and a Circle for the colored disc.
 */
public class GUISpace {
    public static final int SQUARE_SIZE = 70;

    private Pane squarePane;
    private Rectangle bg;
    private Circle disc;
    private BoardSpace.SpaceType type;
    private final int x;
    private final int y;

    private static final Color BOARD_COLOR = Color.web("#2E8B57");
    private static final Color HIGHLIGHT_COLOR = Color.web("#FFD700");
    private static final Color LEGAL_MOVE_COLOR = Color.web("#F5DEB3");

    public GUISpace(int x, int y, BoardSpace.SpaceType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        setSquare();
        addOrUpdateDisc(type);
    }

    /**
     * Sets the position of the space in the GUI.
     */
    public void setSquare() {
        Point2D location = new Point2D(x * SQUARE_SIZE, y * SQUARE_SIZE);
        squarePane = new Pane();
        squarePane.setPrefHeight(SQUARE_SIZE);
        squarePane.setPrefWidth(SQUARE_SIZE);
        squarePane.setLayoutX(location.getX());
        squarePane.setLayoutY(location.getY());

        bg = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
        bg.setStroke(Color.web("#1E3F1E", 0.8));
        bg.setFill(BOARD_COLOR);
        bg.setArcWidth(15);
        bg.setArcHeight(15);
        bg.setEffect(new DropShadow(5, Color.gray(0.4)));

        squarePane.getChildren().add(bg);
    }

    /**
     * @return the visual square Pane
     */
    public Pane getSquare() {

        return squarePane;
    }

    /**
     * Sets the fill of the background Pane
     * @param color color given to the background Pane
     */
    public void setBgColor(Color color) {
        bg.setFill(color);
    }

    public void highlightAsLegalMove() {
        setBgColor(LEGAL_MOVE_COLOR);
    }

    public void resetBackground() {
        setBgColor(BOARD_COLOR);
    }

    public BoardSpace.SpaceType getType() {
        return type;
    }

    public void setDiscGlow(boolean enable) {
        if (disc != null) {
            disc.setEffect(enable ? new Glow(0.8) : null);
        }
    }

    /**
     * Adds a visual disc to this Pane, replacing the current disc or adding it to an empty space
     * @param type type or color of disc to add
     */
    public void addOrUpdateDisc(BoardSpace.SpaceType type) {
        if (squarePane.getChildren().contains(disc)) {
            squarePane.getChildren().remove(disc);
        }
        this.type = type;
        if (this.type == BoardSpace.SpaceType.BLACK || this.type == BoardSpace.SpaceType.WHITE) {
            disc = new Circle();
            int squareCenter = SQUARE_SIZE / 2;
            disc.setRadius(squareCenter - 5);
            disc.setFill(this.type.fill());
            disc.setStroke(Color.BLACK);
            disc.setCenterX(squareCenter);
            disc.setCenterY(squareCenter);
            squarePane.getChildren().add(disc);
        }
    }

    public Circle getDisc() {
        return disc;
    }
}
