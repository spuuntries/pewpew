package pewpew;

import java.util.ArrayList;

public class RapidFire extends Weapon {
    public RapidFire() {
        super(15); // 15 shots per second
    }

    @Override
    public ArrayList<Bullet> shoot(int x, int y, double angle) {
        ArrayList<Bullet> bullets = new ArrayList<>();
        if (canShoot()) {
            bullets.add(new LightBullet(x, y, angle));
            updateLastShotTime();
        }
        return bullets;
    }
}
