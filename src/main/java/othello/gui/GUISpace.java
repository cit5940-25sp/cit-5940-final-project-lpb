package othello.gui;

import javafx.geometry.Point2D;
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
    /** The width and height of each board square in pixels. */
    public static final int SQUARE_SIZE = 60;

    /** The main Pane representing this space on the board. */
    private Pane squarePane;

    /** The green background rectangle inside the Pane. */
    private Rectangle bg;

    /** The black or white disc displayed inside the Pane, if present. */
    private Circle disc;

    /** The current logical type (black, white, or empty) associated with this space. */
    private BoardSpace.SpaceType type;

    /** The x-coordinate (column) of this board space. */
    private final int x;

    /** The y-coordinate (row) of this board space. */
    private final int y;

    /**
     * Constructs a new GUISpace at the given coordinates and initializes it with a space type.
     *
     * @param x the x (column) coordinate on the board
     * @param y the y (row) coordinate on the board
     * @param type the type of space to initialize (BLACK, WHITE, or EMPTY)
     */
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
        bg.setStroke(Color.BLACK);
        bg.setFill(Color.SEAGREEN);
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
}