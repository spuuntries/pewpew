package pewpew;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player extends GameObject {
	private int dx, dy;
	private int currentWeaponIndex = 0;

	private static final int PLAYER_SIZE = 30;
	private static final int MAX_WEAPONS = 2;
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;

	private ArrayList<Weapon> inventory;
	private Weapon currentWeapon;

	public Player(int x, int y) {
		super(x, y);
		dx = dy = 0;
		inventory = new ArrayList<>();
		// Maybe start with just the basic gun
		inventory.add(new BasicGun());
		currentWeapon = inventory.get(0);
	}

	private int findEmptySlot() {
		for (int i = 0; i < MAX_WEAPONS; i++) {
			if (i >= inventory.size()) {
				return i;
			}
		}
		return -1; // No empty slots
	}

	public boolean hasWeaponType(Class<?> weaponType) {
		for (Weapon weapon : inventory) {
			if (weapon.getClass().equals(weaponType)) {
				return true;
			}
		}
		return false;
	}

	public Weapon pickupWeapon(Weapon newWeapon) {
		int emptySlot = findEmptySlot();

		if (emptySlot != -1) {
			// Fill empty slot
			inventory.add(newWeapon);
			currentWeapon = newWeapon;
			currentWeaponIndex = inventory.size() - 1;
			return null;
		} else {
			// Swap weapons instead of replace
			Weapon oldWeapon = inventory.get(currentWeaponIndex);
			inventory.set(currentWeaponIndex, newWeapon);
			currentWeapon = newWeapon;
			return oldWeapon;
		}
	}

	public void switchWeapon(int index) {
		if (index >= 0 && index < inventory.size()) {
			currentWeaponIndex = index;
			currentWeapon = inventory.get(currentWeaponIndex);
		}
	}

	public ArrayList<Weapon> getInventory() {
		return inventory;
	}

	public Weapon getCurrentWeapon() {
		return currentWeapon;
	}

	public int getCurrentWeaponIndex() {
		return currentWeaponIndex;
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
