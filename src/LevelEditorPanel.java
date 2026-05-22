import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LevelEditorPanel extends JPanel implements MouseListener {

    final int tileSize = 32;
    final int cols = 16, rows = 16;

    private boolean[][] walls = new boolean[cols][rows];
    private java.util.List<Enemy> enemies = new java.util.ArrayList<>();
    private double playerX = -1, playerY = -1;

    EditorTool currentTool = EditorTool.WALL;

    public LevelEditorPanel() {
        setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        addMouseListener(this);
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Grid
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (walls[x][y]) {
                    g.setColor(Color.GRAY);
                    g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }

        // Enemies
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            g.fillOval((int)(e.x * tileSize), (int)(e.y * tileSize), tileSize, tileSize);
        }

        // Player
        if (playerX >= 0) {
            g.setColor(Color.CYAN);
            g.fillOval((int)(playerX * tileSize), (int)(playerY * tileSize), tileSize, tileSize);
        }
    }

    public void setTool(EditorTool tool) {
        currentTool = tool;
    }

    // Accessors for external use (defensive copies where appropriate)
    public boolean[][] getWalls() {
        boolean[][] copy = new boolean[cols][rows];
        for (int i = 0; i < cols; i++) System.arraycopy(walls[i], 0, copy[i], 0, rows);
        return copy;
    }

    public java.util.List<Enemy> getEnemies() {
        return new java.util.ArrayList<>(enemies);
    }

    public void saveToFile(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LevelData data = new LevelData(cols, rows, walls, enemies, playerX, playerY);
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        }
    }

    public void loadFromFile(File file) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            LevelData data = gson.fromJson(reader, LevelData.class);
            if (data == null) throw new IOException("Invalid level file");
            // Resize walls if needed
            if (data.cols != cols || data.rows != rows) {
                walls = new boolean[data.cols][data.rows];
            }
            // Copy walls safely
            for (int i = 0; i < Math.min(cols, data.cols); i++) {
                for (int j = 0; j < Math.min(rows, data.rows); j++) {
                    walls[i][j] = data.walls[i][j];
                }
            }
            enemies.clear();
            if (data.enemies != null) {
                for (Enemy e : data.enemies) {
                    enemies.add(new Enemy(e.x, e.y));
                }
            }
            playerX = data.playerX;
            playerY = data.playerY;
            repaint();
        }
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public void mousePressed(MouseEvent e) {
        int x = e.getX() / tileSize;
        int y = e.getY() / tileSize;
        if (x < 0 || y < 0 || x >= cols || y >= rows) return;

        switch (currentTool) {
            case WALL -> walls[x][y] = !walls[x][y];
            case ENEMY -> enemies.add(new Enemy(x + 0.5, y + 0.5));
            case PLAYER -> {
                playerX = x + 0.5;
                playerY = y + 0.5;
            }
            case EMPTY -> {
                walls[x][y] = false;
                enemies.removeIf(pos -> (int) pos.x == x && (int) pos.y == y);
            }
        }
        repaint();
    }

    // unused
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
