package pewpew;

import java.util.Random;

public class Enemy extends GameObject {
	private static final int ENEMY_SIZE = 25;
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private static final Random random = new Random();
	private Player player;

	public Enemy(Player player) {
		super(0, 0);
		this.player = player;
		if (random.nextBoolean()) {
			x = random.nextBoolean() ? -ENEMY_SIZE : WIDTH;
			y = random.nextInt(HEIGHT);
		} else {
			x = random.nextInt(WIDTH);
			y = random.nextBoolean() ? -ENEMY_SIZE : HEIGHT;
		}
	}

	@Override
	public void update() {
		// Target player's collision center instead of sprite position
		int targetX = player.getCollisionX() + Player.getCollisionWidth() / 2;
		int targetY = player.getCollisionY() + Player.getCollisionHeight() / 2;
		double angle = Math.atan2(targetY - (y + ENEMY_SIZE / 2), targetX - (x + ENEMY_SIZE / 2));
		x += 2 * Math.cos(angle);
		y += 2 * Math.sin(angle);
	}

	public static int getSize() {
		return ENEMY_SIZE;
	}

	// Add these for consistency
	public int getCenterX() {
		return x + ENEMY_SIZE / 2;
	}

	public int getCenterY() {
		return y + ENEMY_SIZE / 2;
	}
}
