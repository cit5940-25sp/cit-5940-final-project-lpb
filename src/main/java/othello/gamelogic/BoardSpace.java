package othello.gamelogic;
import java.util.Objects;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a logical space on the Othello Board.
 * Keeps track of coordinates and the type of the current space.
 * Implements Flyweight pattern to share instances.
 */

public class BoardSpace {

    private final int x;
    private final int y;
    private SpaceType type;

    // Flyweight factory: stores shared instances of BoardSpace
    private static final Map<String, BoardSpace> boardSpaceCache = new HashMap<>();

    public BoardSpace(int x, int y, SpaceType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    /**
     * Flyweight factory method to get or create a BoardSpace instance.
     * @param x The x-coordinate of the space.
     * @param y The y-coordinate of the space.
     * @param type The type of the space.
     * @return The shared BoardSpace instance.
     */
    public static BoardSpace getBoardSpace(int x, int y, SpaceType type) {
        String key = x + "," + y + "," + type.name(); // Unique key for each combination
        return boardSpaceCache.computeIfAbsent(key, k -> new BoardSpace(x, y, type));
    }

    // Copy constructor (optional, if needed)
    public BoardSpace(BoardSpace other) {
        this.x = other.x;
        this.y = other.y;
        this.type = other.type;
    }

    /**
     * @return the x coordinate of this space
     */
    public int getX() {

        return x;
    }

    /**
     * @return the x coordinate of this space
     */
    public int getY() {

        return y;
    }

    /**
     * @return the Space of the current tile
     */
    public SpaceType getType() {

        return type;
    }

    /**
     * Sets the type of the tile, then adds an othello chip (circle) to the tile.
     * @param type Space to set this space to.
     */
    public void setType(SpaceType type) {

        this.type = type;
    }

    /**
     * Represents the type of the board space, used for filling in the color of the space in the GUI
     */
    public enum SpaceType {
        EMPTY(Color.GRAY),
        BLACK(Color.BLACK),
        WHITE(Color.WHITE);

        private final Color fill;

        SpaceType(Color fill) {

            this.fill = fill;
        }

        public Color fill() {

            return fill;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardSpace that = (BoardSpace) o;
        return x == that.x && y == that.y && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, type);
    }

}