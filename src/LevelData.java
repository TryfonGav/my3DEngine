import java.util.List;

public class LevelData {
    public int cols;
    public int rows;
    public boolean[][] walls;
    public List<Enemy> enemies;
    public double playerX;
    public double playerY;

    public LevelData() {}

    public LevelData(int cols, int rows, boolean[][] walls, List<Enemy> enemies, double playerX, double playerY) {
        this.cols = cols;
        this.rows = rows;
        this.walls = walls;
        this.enemies = enemies;
        this.playerX = playerX;
        this.playerY = playerY;
    }
}
