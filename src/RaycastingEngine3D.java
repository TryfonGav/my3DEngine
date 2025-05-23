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

    boolean[][] world = new boolean[mapWidth][mapHeight];
    List<Enemy> enemies = new ArrayList<>();

    BufferedImage frame;
    Graphics2D buffer;

    // === Default constructor ===
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

    // === Editor-based constructor ===
    public RaycastingEngine3D(boolean[][] map, List<double[]> enemyList, double px, double py) {
        this(); // Call default constructor
        this.world = map;
        this.playerX = px;
        this.playerY = py;
        this.enemies.clear();
        for (double[] pos : enemyList) {
            this.enemies.add(new Enemy(pos[0], pos[1]));
        }
    }

    void generateWorld() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                world[x][y] = x == 0 || y == 0 || x == mapWidth - 1 || y == mapHeight - 1 || Math.random() < 0.1;
            }
        }
        enemies.add(new Enemy(6.5, 6.5));
        enemies.add(new Enemy(10.5, 3.5));
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
        if (!world[mx][my]) {
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
            double distanceToWall = 0;
            boolean hitWall = false;

            while (!hitWall && distanceToWall < depth) {
                distanceToWall += 0.01;
                int testX = (int)(playerX + eyeX * distanceToWall);
                int testY = (int)(playerY + eyeY * distanceToWall);
                if (testX < 0 || testY < 0 || testX >= mapWidth || testY >= mapHeight) break;
                if (world[testX][testY]) hitWall = true;
            }

            int ceiling = (int)((screenHeight / 2.0) - screenHeight / distanceToWall);
            int floor = screenHeight - ceiling;
            int shade = (int)(255 / (1 + distanceToWall * distanceToWall * 0.1));
            shade = Math.max(0, Math.min(255, shade));
            buffer.setColor(new Color(shade, shade, shade));
            buffer.drawLine(x, ceiling, x, floor);
        }

        // === Enemies with occlusion check ===
        for (Enemy e : enemies) {
            if (e.health <= 0) continue;
            double dx = e.x - playerX, dy = e.y - playerY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            double angleToSprite = Math.atan2(dy, dx) - playerAngle;

            if (dist < depth && Math.abs(angleToSprite) < FOV / 2.0) {
                double rayX = Math.cos(playerAngle + angleToSprite);
                double rayY = Math.sin(playerAngle + angleToSprite);
                double wallDist = 0;
                while (wallDist < depth) {
                    int testX = (int)(playerX + rayX * wallDist);
                    int testY = (int)(playerY + rayY * wallDist);
                    if (testX < 0 || testY < 0 || testX >= mapWidth || testY >= mapHeight) break;
                    if (world[testX][testY]) break;
                    wallDist += 0.05;
                }

                if (dist < wallDist - 0.2) {
                    int spriteSize = (int)(screenHeight / dist);
                    int x = (int)(screenWidth / 2 + Math.tan(angleToSprite) * screenWidth / (2 * Math.tan(FOV / 2))) - spriteSize / 2;
                    int y = screenHeight / 2 - spriteSize / 2;

                    buffer.setColor(new Color(255, 0, 0, 200));
                    buffer.fillRect(x, y, spriteSize, spriteSize);
                    buffer.setColor(Color.GREEN);
                    int barWidth = (int)(spriteSize * (e.health / 100.0));
                    buffer.fillRect(x, y - 5, barWidth, 4);
                    buffer.setColor(Color.BLACK);
                    buffer.drawRect(x, y - 5, spriteSize, 4);
                }
            }
        }

        // === Weapon (raised)
        int gunHeight = screenHeight - 160;
        buffer.setColor(weaponFrame > 0 ? Color.ORANGE : Color.GRAY);
        buffer.fillRect((screenWidth - 100) / 2, gunHeight, 100, 100);
        if (weaponFrame > 0) weaponFrame--;

        // === Minimap (top right)
        int miniTile = 8;
        int miniX = screenWidth - (mapWidth * miniTile) - 20;
        int miniY = 20;
        for (int mx = 0; mx < mapWidth; mx++) {
            for (int my = 0; my < mapHeight; my++) {
                if (world[mx][my]) {
                    buffer.setColor(Color.DARK_GRAY);
                    buffer.fillRect(miniX + mx * miniTile, miniY + my * miniTile, miniTile, miniTile);
                }
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

        buffer.setColor(Color.WHITE);
        buffer.setFont(new Font("Monospaced", Font.BOLD, 16));
        buffer.drawString("WASD | ←/→ = Turn | SPACE = Shoot | ESC = Menu", 20, 30);

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
