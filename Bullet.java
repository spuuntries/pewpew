package pewpew;

import java.awt.*;

public abstract class Bullet extends GameObject {
    protected double dx, dy;
    protected int damage;
    protected int speed;
    protected int size;
    protected Color color;

    public Bullet(int x, int y, double angle, int speed, int damage, int size, Color color) {
        super(x, y);
        this.speed = speed;
        this.damage = damage;
        this.size = size;
        this.color = color;
        dx = speed * Math.cos(angle);
        dy = speed * Math.sin(angle);
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
    }

    public int getDamage() {
        return damage;
    }

    public int getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }
}
