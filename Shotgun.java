package pewpew;

import java.util.ArrayList;
import java.util.Random;

public class Shotgun extends Weapon {
    private static final int PELLET_COUNT = 5;
    private static final double SPREAD = Math.PI / 8; // 22.5 degrees
    private static final Random random = new Random();

    public Shotgun() {
        super(2); // 2 shots per second
    }

    @Override
    public ArrayList<Bullet> shoot(int x, int y, double angle) {
        ArrayList<Bullet> bullets = new ArrayList<>();
        if (canShoot()) {
            for (int i = 0; i < PELLET_COUNT; i++) {
                double spread = angle + (random.nextDouble() - 0.5) * SPREAD;
                bullets.add(new ShotgunPellet(x, y, spread));
            }
            updateLastShotTime();
        }
        return bullets;
    }
}
