import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class RaycastingEngine3D extends JPanel implements KeyListener, Runnable {

    final int screenWidth = 800, screenHeight = 600;
    final int mapWidth = 16, mapHeight = 16;
    final double FOV = Math.PI / 3;
    final double depth = 16;
    final int numRays = screenWidth;

    double playerX = 3.5, playerY = 3.5, playerAngle = 0;
    double moveSpeed = 0.08, rotSpeed = 0.04;
    boolean[] keys = new boolean[256];
    boolean shooting = false;

    boolean[][] world = new boolean[mapWidth][mapHeight];
    List<Enemy> enemies = new ArrayList<>();

    BufferedImage frame;
    Graphics2D buffer;

    // === Default constructor (for standalone use) ===
    public RaycastingEngine3D() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setFocusable(true);
        addKeyListener(this);
        frame = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        buffer = frame.createGraphics();
        generateWorld();
        new Thread(this).start();
    }

    // === Editor constructor ===
    public RaycastingEngine3D(boolean[][] map, List<double[]> enemyList, double px, double py) {
        this.world = map;
        this.playerX = px;
        this.playerY = py;
        this.playerAngle = 0;
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setFocusable(true);
        this.addKeyListener(this);
        this.frame = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
        this.buffer = frame.createGraphics();
        this.enemies = new ArrayList<>();
        for (double[] pos : enemyList) {
            enemies.add(new Enemy(pos[0], pos[1]));
        }
        new Thread(this).start();
    }

    void generateWorld() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (x == 0 || y == 0 || x == mapWidth - 1 || y == mapHeight - 1 || Math.random() < 0.1)
                    world[x][y] = true;
            }
        }
        enemies.add(new Enemy(6.5, 6.5));
        enemies.add(new Enemy(10.5, 3.5));
    }

    @Override
    public void run() {
        while (true) {
            updateLogic();
            render3D();
            repaint();
            try { Thread.sleep(16); } catch (InterruptedException e) {}
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

            double fx = playerX, fy = playerY;
            for (double d = 0; d < 5; d += 0.05) {
                fx = playerX + Math.cos(playerAngle) * d;
                fy = playerY + Math.sin(playerAngle) * d;
                for (Enemy e : enemies) {
                    if (e.health > 0 && Math.hypot(e.x - fx, e.y - fy) < 0.4) {
                        e.health -= 25;
                        System.out.println("Hit!");
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

                if (testX < 0 || testX >= mapWidth || testY < 0 || testY >= mapHeight) {
                    hitWall = true;
                    distanceToWall = depth;
                } else if (world[testX][testY]) {
                    hitWall = true;
                }
            }

            int ceiling = (int)((screenHeight / 2.0) - screenHeight / distanceToWall);
            int floor = screenHeight - ceiling;

            int shade = (int)(255 / (1 + distanceToWall * distanceToWall * 0.1));
            shade = Math.max(0, Math.min(255, shade));
            Color wallColor = new Color(shade, shade, shade);
            buffer.setColor(wallColor);
            buffer.drawLine(x, ceiling, x, floor);
        }

        // SPRITES (Enemies)
        enemies.sort((a, b) -> Double.compare(
                Math.hypot(b.x - playerX, b.y - playerY),
                Math.hypot(a.x - playerX, a.y - playerY)
        ));
        for (Enemy e : enemies) {
            if (e.health <= 0) continue;

            double dx = e.x - playerX;
            double dy = e.y - playerY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            double angleToSprite = Math.atan2(dy, dx) - playerAngle;

            if (dist < depth && Math.abs(angleToSprite) < FOV / 2.0) {
                int spriteSize = (int)(screenHeight / dist);
                int x = (int)(screenWidth / 2 + Math.tan(angleToSprite) * screenWidth / (2 * Math.tan(FOV / 2))) - spriteSize / 2;
                int y = screenHeight / 2 - spriteSize / 2;

                buffer.setColor(new Color(255, 0, 0, 200));
                buffer.fillRect(x, y, spriteSize, spriteSize);

                // Health bar
                buffer.setColor(Color.GREEN);
                int barWidth = (int)(spriteSize * (e.health / 100.0));
                buffer.fillRect(x, y - 5, barWidth, 4);
                buffer.setColor(Color.BLACK);
                buffer.drawRect(x, y - 5, spriteSize, 4);
            }
        }

        // HUD
        buffer.setColor(Color.WHITE);
        buffer.setFont(new Font("Monospaced", Font.BOLD, 16));
        buffer.drawString("W/A/S/D | LEFT/RIGHT = Move | SPACE = Shoot", 20, 30);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(frame, 0, 0, null);
    }

    @Override public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) shooting = true;
    }

    @Override public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Raycasting 3D");
        RaycastingEngine3D game = new RaycastingEngine3D();
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
