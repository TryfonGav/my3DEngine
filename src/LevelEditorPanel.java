import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class LevelEditorPanel extends JPanel {
    public TileType[][] map = new TileType[16][16];
    public List<double[]> enemies = new ArrayList<>();
    public double playerX = 1, playerY = 1;
    private EditorTool selectedTool = EditorTool.WALL;

    public LevelEditorPanel() {
        setPreferredSize(new Dimension(512, 512));
        resetMap();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int tileX = e.getX() / 32;
                int tileY = e.getY() / 32;
                if (tileX < 0 || tileX >= 16 || tileY < 0 || tileY >= 16) return;

                switch (selectedTool) {
                    case WALL -> map[tileX][tileY] = TileType.WALL;
                    case FLOOR -> map[tileX][tileY] = TileType.FLOOR;
                    case DOOR -> map[tileX][tileY] = TileType.DOOR;
                    case KEY -> map[tileX][tileY] = TileType.KEY;
                    case POTION -> map[tileX][tileY] = TileType.POTION;
                    case ENEMY -> enemies.add(new double[]{tileX + 0.5, tileY + 0.5});
                    case PLAYER -> {
                        playerX = tileX + 0.5;
                        playerY = tileY + 0.5;
                    }
                }

                repaint();
            }
        });
    }

    public void setTool(EditorTool tool) {
        selectedTool = tool;
    }

    private void resetMap() {
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                map[x][y] = TileType.FLOOR;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                switch (map[x][y]) {
                    case WALL -> g.setColor(Color.DARK_GRAY);
                    case FLOOR -> g.setColor(Color.LIGHT_GRAY);
                    case DOOR -> g.setColor(Color.BLUE);
                    case KEY -> g.setColor(Color.YELLOW);
                    case POTION -> g.setColor(Color.GREEN);
                }
                g.fillRect(x * 32, y * 32, 32, 32);
                g.setColor(Color.BLACK);
                g.drawRect(x * 32, y * 32, 32, 32);
            }
        }

        for (double[] e : enemies) {
            g.setColor(Color.RED);
            g.fillOval((int)((e[0] - 0.5) * 32), (int)((e[1] - 0.5) * 32), 32, 32);
        }

        g.setColor(Color.CYAN);
        g.fillOval((int)((playerX - 0.5) * 32), (int)((playerY - 0.5) * 32), 32, 32);
    }
}
