package pewpew;

import java.util.ArrayList;

public abstract class Weapon {
    protected int fireRate; // shots per second
    protected long lastShotTime;

    public Weapon(int fireRate) {
        this.fireRate = fireRate;
        this.lastShotTime = 0;
    }

    public boolean canShoot() {
        return System.currentTimeMillis() - lastShotTime >= 1000.0 / fireRate;
    }

    public abstract ArrayList<Bullet> shoot(int x, int y, double angle);

    protected void updateLastShotTime() {
        lastShotTime = System.currentTimeMillis();
    }
}
