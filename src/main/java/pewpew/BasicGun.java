package pewpew;

import java.util.ArrayList;

public class BasicGun extends Weapon {
	public BasicGun() {
		super(5, "/Pistol.png", 32, 16); // 5 shots per second
	}

	@Override
	public ArrayList<Bullet> shoot(int x, int y, double angle) {
		ArrayList<Bullet> bullets = new ArrayList<>();
		if (canShoot()) {
			bullets.add(new BasicBullet(x, y, angle));
			updateLastShotTime();
		}
		return bullets;
	}
}
