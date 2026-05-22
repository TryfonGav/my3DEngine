import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SmokeTest {
    @Test
    void enemyModel() {
        Enemy e = new Enemy(1.0, 2.0);
        assertEquals(1.0, e.x);
        assertEquals(2.0, e.y);
        assertEquals(100, e.health);
    }

    @Test
    void tileTypeExists() {
        assertNotNull(TileType.FLOOR);
    }
}
