package othello.gamelogic;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardSpaceTest {

    @BeforeEach
    void clearFlyweightCache() {
        try {
            java.lang.reflect.Field cacheField = BoardSpace.class.getDeclaredField("boardSpaceCache");
            cacheField.setAccessible(true);
            java.util.Map<?, ?> cache = (java.util.Map<?, ?>) cacheField.get(null);
            cache.clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Warning: Could not clear BoardSpace cache for test isolation.");
            e.printStackTrace();
        }
    }

    @Test
    void testGetters() {
        BoardSpace space = BoardSpace.getBoardSpace(2, 3, BoardSpace.SpaceType.BLACK);
        assertEquals(2, space.getX(), "getX should return the correct x-coordinate.");
        assertEquals(3, space.getY(), "getY should return the correct y-coordinate.");
        assertEquals(BoardSpace.SpaceType.BLACK, space.getType(), "getType should return the correct initial type.");
    }

    @Test
    void testSetType() {
        BoardSpace space = BoardSpace.getBoardSpace(4, 4, BoardSpace.SpaceType.EMPTY);
        assertEquals(BoardSpace.SpaceType.EMPTY, space.getType(), "Initial type should be EMPTY.");

        space.setType(BoardSpace.SpaceType.WHITE);
        assertEquals(BoardSpace.SpaceType.WHITE, space.getType(), "setType should update the space type to WHITE.");

        space.setType(BoardSpace.SpaceType.BLACK);
        assertEquals(BoardSpace.SpaceType.BLACK, space.getType(), "setType should update the space type to BLACK.");
    }

    @Test
    void testFlyweightPattern() {
        BoardSpace space1 = BoardSpace.getBoardSpace(1, 1, BoardSpace.SpaceType.EMPTY);
        BoardSpace space2 = BoardSpace.getBoardSpace(1, 1, BoardSpace.SpaceType.EMPTY);
        BoardSpace space3 = BoardSpace.getBoardSpace(1, 2, BoardSpace.SpaceType.EMPTY);
        BoardSpace space4 = BoardSpace.getBoardSpace(1, 1, BoardSpace.SpaceType.BLACK); // Different type

        assertSame(space1, space2, "getBoardSpace should return the same instance for identical parameters (Flyweight).");
        assertNotSame(space1, space3, "getBoardSpace should return different instances for different coordinates.");
        assertNotSame(space1, space4, "getBoardSpace should return different instances for different types.");
    }

    @Test
    void testCopyConstructor() {
        BoardSpace original = BoardSpace.getBoardSpace(5, 5, BoardSpace.SpaceType.WHITE);
        BoardSpace copy = new BoardSpace(original);

        assertEquals(original.getX(), copy.getX(), "Copy should have the same x-coordinate.");
        assertEquals(original.getY(), copy.getY(), "Copy should have the same y-coordinate.");
        assertEquals(original.getType(), copy.getType(), "Copy should have the same type.");
        assertNotSame(original, copy, "Copy constructor should create a new object instance.");

        // Modify original, copy should not change (if type was mutable, this would be important)
        // Since type is an enum, this test mainly ensures it's a separate object.
        // If BoardSpace had mutable fields, we'd test their independence here.
    }

    @Test
    void testSpaceTypeEnum() {
        assertEquals(Color.GRAY, BoardSpace.SpaceType.EMPTY.fill(), "EMPTY fill color should be GRAY.");
        assertEquals(Color.BLACK, BoardSpace.SpaceType.BLACK.fill(), "BLACK fill color should be BLACK.");
        assertEquals(Color.WHITE, BoardSpace.SpaceType.WHITE.fill(), "WHITE fill color should be WHITE.");
    }
}