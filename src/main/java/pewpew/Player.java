package pewpew;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player extends GameObject {
	private int dx, dy;
	private static final int PLAYER_SIZE = 30;
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private Weapon currentWeapon;
	private ArrayList<Weapon> weapons;

	public Player(int x, int y) {
		super(x, y);
		dx = dy = 0;
		weapons = new ArrayList<>();
		weapons.add(new BasicGun());
		weapons.add(new Shotgun());
		weapons.add(new RapidFire());
		currentWeapon = weapons.get(0);
	}

	public Weapon getCurrentWeapon() {
		return currentWeapon;
	}

	public BufferedImage getCurrentWeaponSprite() {
		return currentWeapon.getSprite();
	}

	public ArrayList<Bullet> shoot(int gunTipX, int gunTipY, double angle) {
		if (currentWeapon.canShoot()) {
			return currentWeapon.shoot(gunTipX, gunTipY, angle);
		}
		return new ArrayList<>();
	}

	public void switchWeapon(int index) {
		if (index >= 0 && index < weapons.size()) {
			currentWeapon = weapons.get(index);
		}
	}

	@Override
	public void update() {
		x += dx;
		y += dy;
		x = Math.max(0, Math.min(x, WIDTH - PLAYER_SIZE));
		y = Math.max(0, Math.min(y, HEIGHT - PLAYER_SIZE));
	}

	public void setDX(int dx) {
		this.dx = dx;
	}

	public void setDY(int dy) {
		this.dy = dy;
	}

	public static int getSize() {
		return PLAYER_SIZE;
	}
}
