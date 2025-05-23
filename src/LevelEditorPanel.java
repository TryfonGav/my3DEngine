import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class LevelEditorPanel extends JPanel implements MouseListener {

    final int tileSize = 32;
    final int cols = 16, rows = 16;

    boolean[][] walls = new boolean[cols][rows];
    ArrayList<double[]> enemies = new ArrayList<>();
    double playerX = -1, playerY = -1;

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
        for (double[] e : enemies) {
            g.fillOval((int)(e[0] * tileSize), (int)(e[1] * tileSize), tileSize, tileSize);
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

    public void mousePressed(MouseEvent e) {
        int x = e.getX() / tileSize;
        int y = e.getY() / tileSize;
        if (x < 0 || y < 0 || x >= cols || y >= rows) return;

        switch (currentTool) {
            case WALL -> walls[x][y] = !walls[x][y];
            case ENEMY -> enemies.add(new double[]{x + 0.5, y + 0.5});
            case PLAYER -> {
                playerX = x + 0.5;
                playerY = y + 0.5;
            }
            case EMPTY -> {
                walls[x][y] = false;
                enemies.removeIf(pos -> (int) pos[0] == x && (int) pos[1] == y);
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
