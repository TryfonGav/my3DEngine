import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class RaycastingEngine3D extends JPanel implements KeyListener, Runnable {

    final int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    final int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    final int mapWidth = 16, mapHeight = 16;
    final double FOV = Math.PI / 3;
    final double depth = 16;
    final int numRays = screenWidth;

    double playerX = 3.5, playerY = 3.5, playerAngle = 0;
    double moveSpeed = 0.08, rotSpeed = 0.04;
    boolean[] keys = new boolean[256];
    boolean shooting = false;
    int weaponFrame = 0;
    boolean showMenu = false;
    boolean hasKey = false;

    TileType[][] map = new TileType[mapWidth][mapHeight];
    List<Enemy> enemies = new ArrayList<>();

    BufferedImage frame;
    Graphics2D buffer;

    public RaycastingEngine3D() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (showMenu) {
                    int mx = e.getX(), my = e.getY();
                    if (mx >= screenWidth / 2 - 75 && mx <= screenWidth / 2 + 75 &&
                            my >= screenHeight / 2 && my <= screenHeight / 2 + 40) {
                        System.exit(0);
                    }
                }
            }
        });

        frame = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        buffer = frame.createGraphics();
        generateWorld();
        new Thread(this).start();
    }

    void generateWorld() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = (x == 0 || y == 0 || x == mapWidth - 1 || y == mapHeight - 1) ? TileType.WALL : TileType.FLOOR;
            }
        }
        map[5][5] = TileType.KEY;
        map[6][6] = TileType.POTION;
        map[7][7] = TileType.DOOR;
        enemies.add(new Enemy(10.5, 5.5));
    }

    public void run() {
        while (true) {
            if (!showMenu) updateLogic();
            render3D();
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
        }
    }

    void updateLogic() {
        if (keys[KeyEvent.VK_LEFT]) playerAngle -= rotSpeed;
        if (keys[KeyEvent.VK_RIGHT]) playerAngle += rotSpeed;

        double dx = Math.cos(playerAngle) * moveSpeed;
        double dy = Math.sin(playerAngle) * moveSpeed;

        if (keys[KeyEvent.VK_W]) tryMove(playerX + dx, playerY + dy);
        if (keys[KeyEvent.VK_S]) tryMove(playerX - dx, playerY - dy);

        TileType tile = map[(int)playerX][(int)playerY];
        if (tile == TileType.KEY) {
            hasKey = true;
            map[(int)playerX][(int)playerY] = TileType.FLOOR;
        }
        if (tile == TileType.POTION) {
            map[(int)playerX][(int)playerY] = TileType.FLOOR;
        }
        if (tile == TileType.DOOR && hasKey) {
            map[(int)playerX][(int)playerY] = TileType.FLOOR;
        }

        if (shooting) {
            shooting = false;
            weaponFrame = 5;
            for (double d = 0; d < 5; d += 0.05) {
                double fx = playerX + Math.cos(playerAngle) * d;
                double fy = playerY + Math.sin(playerAngle) * d;
                for (Enemy e : enemies) {
                    if (e.health > 0 && Math.hypot(e.x - fx, e.y - fy) < 0.4) {
                        e.health -= 25;
                        d = 5;
                        break;
                    }
                }
            }
        }
    }

    void tryMove(double nx, double ny) {
        int mx = (int) nx, my = (int) ny;
        TileType t = map[mx][my];
        if (t != TileType.WALL && !(t == TileType.DOOR && !hasKey)) {
            playerX = nx;
            playerY = ny;
        }
    }

    void render3D() {
        buffer.setColor(Color.BLACK);
        buffer.fillRect(0, 0, screenWidth, screenHeight);

        for (int x = 0; x < numRays; x++) {
            double rayAngle = (playerAngle - FOV / 2.0) + (x * FOV / numRays);
            double eyeX = Math.cos(rayAngle), eyeY = Math.sin(rayAngle);
            double distance = 0;
            boolean hit = false;

            while (!hit && distance < depth) {
                distance += 0.05;
                int testX = (int)(playerX + eyeX * distance);
                int testY = (int)(playerY + eyeY * distance);
                if (testX < 0 || testY < 0 || testX >= mapWidth || testY >= mapHeight) break;
                TileType tile = map[testX][testY];
                if (tile == TileType.WALL || tile == TileType.DOOR) {
                    hit = true;
                }
            }

            int ceiling = (int)((screenHeight / 2.0) - screenHeight / distance);
            int floor = screenHeight - ceiling;
            int shade = Math.max(0, 255 - (int)(distance * 32));
            buffer.setColor(new Color(shade, shade, shade));
            buffer.drawLine(x, ceiling, x, floor);

            buffer.setColor(new Color(40, 40, 40));
            buffer.drawLine(x, floor, x, screenHeight);
        }

        // Weapon
        buffer.setColor(weaponFrame > 0 ? Color.ORANGE : Color.GRAY);
        buffer.fillRect((screenWidth - 100) / 2, screenHeight - 160, 100, 100);
        if (weaponFrame > 0) weaponFrame--;

        // Minimap
        int miniTile = 8;
        int miniX = screenWidth - (mapWidth * miniTile) - 20;
        int miniY = 20;
        for (int mx = 0; mx < mapWidth; mx++) {
            for (int my = 0; my < mapHeight; my++) {
                switch (map[mx][my]) {
                    case WALL -> buffer.setColor(Color.DARK_GRAY);
                    case FLOOR -> buffer.setColor(Color.BLACK);
                    case DOOR -> buffer.setColor(Color.BLUE);
                    case POTION -> buffer.setColor(Color.GREEN);
                    case KEY -> buffer.setColor(Color.YELLOW);
                }
                buffer.fillRect(miniX + mx * miniTile, miniY + my * miniTile, miniTile, miniTile);
            }
        }
        buffer.setColor(Color.CYAN);
        buffer.fillRect((int)(miniX + playerX * miniTile), (int)(miniY + playerY * miniTile), miniTile, miniTile);
        buffer.setColor(Color.RED);
        for (Enemy e : enemies) {
            if (e.health > 0) {
                buffer.fillRect((int)(miniX + e.x * miniTile), (int)(miniY + e.y * miniTile), miniTile, miniTile);
            }
        }

        // HUD
        buffer.setColor(Color.WHITE);
        buffer.setFont(new Font("Monospaced", Font.BOLD, 16));
        buffer.drawString("WASD | ←/→ to Turn | SPACE = Shoot | ESC = Menu", 20, 30);

        if (showMenu) {
            buffer.setColor(new Color(0, 0, 0, 180));
            buffer.fillRect(screenWidth / 2 - 150, screenHeight / 2 - 100, 300, 200);
            buffer.setColor(Color.WHITE);
            buffer.setFont(new Font("Monospaced", Font.BOLD, 24));
            buffer.drawString("PAUSED", screenWidth / 2 - 60, screenHeight / 2 - 40);
            buffer.drawRect(screenWidth / 2 - 75, screenHeight / 2, 150, 40);
            buffer.drawString("EXIT GAME", screenWidth / 2 - 65, screenHeight / 2 + 28);
        }
    }

    public void paintComponent(Graphics g) {
        g.drawImage(frame, 0, 0, null);
    }

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !showMenu) {
            shooting = true;
            weaponFrame = 5;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            showMenu = !showMenu;
        }
    }

    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Raycasting 3D");
        RaycastingEngine3D game = new RaycastingEngine3D();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(game);
        frame.pack();
        frame.setVisible(true);
    }

    static class Enemy {
        double x, y;
        int health = 100;
        Enemy(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
