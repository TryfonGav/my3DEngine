public class Enemy {
    public double x, y;
    public int health = 100;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Enemy(Enemy other) {
        this.x = other.x;
        this.y = other.y;
        this.health = other.health;
    }
}
