package pewpew;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;

	private Player player;
	private ArrayList<Enemy> enemies;
	private ArrayList<Bullet> bullets;
	private Timer timer;
	private Random random;
	private int score;
	private boolean isPaused = false;
	private BufferedImage backgroundImage;

	// Movement control variables
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean upPressed = false;
	private boolean downPressed = false;
	private boolean isFiring = false;

	private float weaponSwitchScale = 1.0f;
	private boolean isWeaponSwitching = false;

	private float damageEffect = 0.0f;
	private float criticalEffect = 0.0f;
	private float criticalPulse = 0.0f;
	private static final float DAMAGE_EFFECT_DECAY = 0.05f;
	private static final float CRITICAL_HEALTH_THRESHOLD = 0.3f; // 30% health
	private static final float CRITICAL_PULSE_SPEED = 0.05f;

	private double lastValidAngle = 0;

	private int screenShakeX = 0;
	private int screenShakeY = 0;
	private int screenShakeIntensity = 0;
	private Random screenShakeRandom = new Random();
	private ArrayList<WeaponPickup> weaponPickups;

	private static GamePanel instance;

	public GamePanel() {
		instance = this;
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.GRAY);
		setFocusable(true);
		addKeyListener(this);

		try {
			backgroundImage = ImageIO.read(getClass().getResource("/Background_Lab.png"));
		} catch (Exception e) {
		    e.printStackTrace();
		}

		initializeGame();
	}

	private void initializeGame() {
		player = new Player(WIDTH / 2, HEIGHT / 2);
		weaponPickups = new ArrayList<>();
		enemies = new ArrayList<>();
		bullets = new ArrayList<>();
		random = new Random();
		score = 0;

		timer = new Timer(20, this);
		timer.start();
	}

	public static void triggerHitEffects() {
		if (instance != null) {
			instance.damageEffect = 1.0f;
			instance.triggerScreenShake(15);
		}
	}

	private Class<? extends Weapon> getRandomAvailableWeaponType() {
		ArrayList<Class<? extends Weapon>> availableTypes = new ArrayList<>();

		// Add weapon types the player doesn't have
		if (!player.hasWeaponType(Shotgun.class))
			availableTypes.add(Shotgun.class);
		if (!player.hasWeaponType(RapidFire.class))
			availableTypes.add(RapidFire.class);
		if (!player.hasWeaponType(BasicGun.class))
			availableTypes.add(BasicGun.class);

		if (availableTypes.isEmpty())
			return null;
		return availableTypes.get(random.nextInt(availableTypes.size()));
	}

	private void spawnWeaponPickup() {
		if (random.nextInt(200) == 0) { // adjust probability as needed
			Class<? extends Weapon> weaponType = getRandomAvailableWeaponType();
			if (weaponType != null) {
				int x = random.nextInt(WIDTH - 20);
				int y = random.nextInt(HEIGHT - 20);

				// Create new weapon instance
				Weapon weaponToSpawn = null;
				try {
					weaponToSpawn = weaponType.getDeclaredConstructor().newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				weaponPickups.add(new WeaponPickup(x, y, weaponToSpawn));
			}
		}
	}

	private void drawDamageEffect(Graphics2D g2d) {
		// Calculate the strongest effect between damage hit and critical pulse
		float effectStrength = Math.max(damageEffect, criticalEffect);

		if (effectStrength > 0) {
			int centerX = WIDTH / 2;
			int centerY = HEIGHT / 2;
			int radius = (int) (Math.max(WIDTH, HEIGHT) * 0.7);

			// Create a radial gradient paint
			RadialGradientPaint paint = new RadialGradientPaint(centerX, centerY, radius,
					new float[] { 0.0f, 0.7f, 1.0f },
					new Color[] { new Color(0, 0, 0, 0), new Color(255, 0, 0, (int) (effectStrength * 100)),
							new Color(255, 0, 0, (int) (effectStrength * 150)) });

			Composite originalComposite = g2d.getComposite();
			Paint originalPaint = g2d.getPaint();

			g2d.setPaint(paint);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, effectStrength));
			g2d.fillRect(0, 0, WIDTH, HEIGHT);

			g2d.setPaint(originalPaint);
			g2d.setComposite(originalComposite);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// Draw background
		if (backgroundImage != null) {
	        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
	    }

		// Draw pause overlay if paused
		if (isPaused) {
			// Semi-transparent black overlay
			g2d.setColor(new Color(0, 0, 0, 150));
			g2d.fillRect(0, 0, WIDTH, HEIGHT);

			// Draw pause text
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Arial", Font.BOLD, 50));
			String pauseText = "PAUSED";
			int textWidth = g2d.getFontMetrics().stringWidth(pauseText);
			g2d.drawString(pauseText, WIDTH / 2 - textWidth / 2, HEIGHT / 2);

			// Draw instructions
			g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			String instructionText = "Press ESC to resume";
			textWidth = g2d.getFontMetrics().stringWidth(instructionText);
			g2d.drawString(instructionText, WIDTH / 2 - textWidth / 2, HEIGHT / 2 + 40);
		}

		// Transform w/o shake
		AffineTransform originalTransform = g2d.getTransform();

		// Shake
		g2d.translate(screenShakeX, screenShakeY);

		// Draw player
//		g.setColor(Color.RED);
//		g.fillRect(player.getX(), player.getY(), Player.getSize(), Player.getSize());
		player.render((Graphics2D) g);

		// Draw current weapon sprite
		Point mouse = getMousePosition();
		if (mouse != null) {
			int centerX = player.getCollisionX() + Player.getCollisionWidth() / 2;
			int centerY = player.getCollisionY() + Player.getCollisionHeight() / 2;

			double angle = Math.atan2(mouse.y - centerY, mouse.x - centerX);

			// Save the current transform
			AffineTransform old = g2d.getTransform();

			// Translate to player center
			g2d.translate(centerX, centerY);
			// player.updateGunAngle(angle);

			// Determine if we need to flip the sprite
			boolean facingLeft = Math.abs(angle) > Math.PI / 2;

			// Rotate based on mouse position
			if (facingLeft) {
				// If facing left, flip vertically and adjust angle
				g2d.scale(1, -1);
				g2d.rotate(-angle);
			} else {
				g2d.rotate(angle);
			}

			// Draw the current weapon's sprite with scaling
			BufferedImage weaponSprite = player.getCurrentWeaponSprite();
			if (weaponSprite != null) {
				double scaleX = (double) player.getCurrentWeapon().getDisplayWidth() / weaponSprite.getWidth();
				double scaleY = (double) player.getCurrentWeapon().getDisplayHeight() / weaponSprite.getHeight();

				// Apply weapon switch animation scale only to the weapon
				if (isWeaponSwitching) {
					scaleX *= weaponSwitchScale;
					scaleY *= weaponSwitchScale;
					weaponSwitchScale += 0.1f;
					if (weaponSwitchScale >= 1.2f) {
						isWeaponSwitching = false;
						weaponSwitchScale = 1.0f;
					}
				}

				g2d.scale(scaleX, scaleY);

				g2d.drawImage(weaponSprite, 0, -weaponSprite.getHeight() / 2, weaponSprite.getWidth(),
						weaponSprite.getHeight(), null);
			}

			// Restore the original transform
			g2d.setTransform(old);
		}

		// Draw weapon pickups
		for (WeaponPickup pickup : weaponPickups) {
			pickup.render(g);
		}

		// Draw enemies
		g.setColor(Color.BLACK);
		for (Enemy enemy : enemies) {
			g.fillOval(enemy.getX(), enemy.getY(), Enemy.getSize(), Enemy.getSize());
		}

		// Draw bullets with their specific colors
		for (Bullet bullet : bullets) {
			bullet.render(g);
		}

		// Debugging tip calc
//		Point gunTip = calculateGunTip();
//		if (gunTip != null) {
//			g2d.setColor(Color.RED);
//			g2d.fillOval(gunTip.x - 2, gunTip.y - 2, 4, 4);
//		}

		// Update screen shake
		if (screenShakeIntensity > 0) {
			screenShakeX = screenShakeRandom.nextInt(screenShakeIntensity * 2) - screenShakeIntensity;
			screenShakeY = screenShakeRandom.nextInt(screenShakeIntensity * 2) - screenShakeIntensity;
			screenShakeIntensity--;
		} else {
			screenShakeX = 0;
			screenShakeY = 0;
		}

		// Apply screen shake
		g2d.setTransform(originalTransform);
		drawDamageEffect(g2d);

		// Draw score and current weapon
		g.setColor(Color.WHITE);
		Font customFont = new Font("Arial", Font.BOLD, 30); // Font size 30, bold style
		g.setFont(customFont);
		g.drawString("Score: " + score, 10, 30);

		drawHealthBar(g);
		drawInventoryUI(g);
	}

	private void drawHealthBar(Graphics g) {
		int barWidth = 200;
		int barHeight = 20;
		int x = WIDTH - barWidth - 10;
		int y = 10;

		// Draw background
		g.setColor(Color.GRAY);
		g.fillRect(x, y, barWidth, barHeight);

		// Draw health
		float healthPercent = (float) player.getHealth() / 100;
		int healthWidth = (int) (barWidth * healthPercent);

		// Change color based on health level
		if (healthPercent > 0.6f) {
			g.setColor(Color.GREEN);
		} else if (healthPercent > 0.3f) {
			g.setColor(Color.YELLOW);
		} else {
			g.setColor(Color.RED);
		}
		g.fillRect(x, y, healthWidth, barHeight);

		// Draw border
		g.setColor(Color.WHITE);
		g.drawRect(x, y, barWidth, barHeight);

		// Draw health text
		String healthText = player.getHealth() + "/100";
		g.setColor(Color.WHITE);
		FontMetrics fm = g.getFontMetrics();
		int textX = x + (barWidth - fm.stringWidth(healthText)) / 2;
		int textY = y + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
		g.drawString(healthText, textX, textY);
	}

	private void drawInventoryUI(Graphics g) {
		ArrayList<Weapon> inventory = player.getInventory();
		int slotWidth = 64; // Increased slot size to match Minecraft-style
		int slotHeight = 64; // Square slots
		int padding = 2; // Reduced padding between slots
		int startX = 10; // Starting X position
		int startY = HEIGHT - slotHeight - 10; // Position from bottom of screen

		// Draw the inventory slots horizontally
		for (int i = 0; i < inventory.size(); i++) {
			int x = startX + (i * (slotWidth + padding));

			// Draw slot background
			g.setColor(
					i == player.getCurrentWeaponIndex() ? new Color(255, 255, 0, 100) : new Color(128, 128, 128, 100));
			g.fillRect(x, startY, slotWidth, slotHeight);

			// Draw slot border
			g.setColor(i == player.getCurrentWeaponIndex() ? Color.YELLOW : Color.WHITE);
			g.drawRect(x, startY, slotWidth, slotHeight);

			// Draw weapon sprite
			Weapon weapon = inventory.get(i);
			if (weapon != null && weapon.getSprite() != null) {
				BufferedImage sprite = weapon.getSprite();

				// Calculate scaling to fit weapon while maintaining aspect ratio
				double scale = Math.min((double) (slotWidth - 16) / sprite.getWidth(),
						(double) (slotHeight - 16) / sprite.getHeight());

				int scaledWidth = (int) (sprite.getWidth() * scale);
				int scaledHeight = (int) (sprite.getHeight() * scale);

				// Center the weapon in the slot
				int weaponX = x + (slotWidth - scaledWidth) / 2;
				int weaponY = startY + (slotHeight - scaledHeight) / 2;

				g.drawImage(sprite, weaponX, weaponY, scaledWidth, scaledHeight, null);
			}

			// Draw slot number
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, 16));
			g.drawString(String.valueOf(i + 1), x + 5, startY + 20);
		}
	}

	private void triggerScreenShake(int intensity) {
		screenShakeIntensity = intensity;
	}

	private double calculateAimAngle() {
		Point mouse = getMousePosition();
		if (mouse != null) {
			int centerX = player.getX() + Player.getSize() / 2;
			int centerY = player.getY() + Player.getSize() / 2;

			double dx = mouse.x - centerX;
			double dy = mouse.y - centerY;
			double angle = Math.atan2(dy, dx);

			// Only update last valid angle if far enough from center
			double distance = Math.hypot(dx, dy);
			if (distance > 10) {
				lastValidAngle = angle;
			}

			return lastValidAngle;
		}
		return lastValidAngle;
	}

	private Point calculateGunTip() {
		Point mouse = getMousePosition();
		if (mouse != null) {
			// Use the same center point as weapon rendering
			int centerX = player.getCollisionX() + Player.getCollisionWidth() / 2;
			int centerY = player.getCollisionY() + Player.getCollisionHeight() / 2;

			double angle = Math.atan2(mouse.y - centerY, mouse.x - centerX);

			// Use a smaller fixed offset first to test
			int gunLength = 20; // Start with a small value and adjust

			int tipX = centerX + (int) (Math.cos(angle) * gunLength);
			int tipY = centerY + (int) (Math.sin(angle) * gunLength);

			return new Point(tipX, tipY);
		}
		return null;
	}

	private void switchWeapon(int index) {
		player.switchWeapon(index);
		isWeaponSwitching = true;
		weaponSwitchScale = 0.8f; // Start slightly smaller
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateGameState();
		checkCollisions();
		spawnEnemies();
		repaint();
	}

	private void updateGameState() {
		player.update();

		for (WeaponPickup pickup : weaponPickups) {
			pickup.update(); // Add this line to update all pickups
		}

		// Handle continuous firing
		if (isFiring) {
			Point mouse = getMousePosition();
			Point gunTip = calculateGunTip();
			if (mouse != null && gunTip != null) {
				double angle = calculateAimAngle(); // Use the same angle calculation
				ArrayList<Bullet> newBullets = player.shoot(gunTip.x, gunTip.y, angle);
				if (!newBullets.isEmpty()) {
					triggerScreenShake(5);
				}
				bullets.addAll(newBullets);
			}
		}

		// Update and remove out-of-bounds bullets
		Iterator<Bullet> bulletIt = bullets.iterator();
		while (bulletIt.hasNext()) {
			Bullet bullet = bulletIt.next();
			bullet.update();
			if (bullet.getX() < 0 || bullet.getX() > WIDTH || bullet.getY() < 0 || bullet.getY() > HEIGHT) {
				bulletIt.remove();
			}
		}

		// Update enemies
		enemies.forEach(Enemy::update);

		spawnWeaponPickup();

		ArrayList<WeaponPickup> newPickups = new ArrayList<>();
		Iterator<WeaponPickup> pickupIt = weaponPickups.iterator();
		while (pickupIt.hasNext()) {
			WeaponPickup pickup = pickupIt.next();

			// Check for despawn
			if (pickup.shouldDespawn()) {
				pickupIt.remove();
				continue;
			}

			if (pickup.collidesWith(player)) {
				Weapon oldWeapon = player.pickupWeapon(pickup.getWeapon());
				pickupIt.remove();
			}
		}

		if (damageEffect > 0) {
			damageEffect -= DAMAGE_EFFECT_DECAY;
			if (damageEffect < 0)
				damageEffect = 0;
		}

		float healthPercent = (float) player.getHealth() / 100;
		if (healthPercent <= CRITICAL_HEALTH_THRESHOLD) {
			// Update pulse animation
			criticalPulse += CRITICAL_PULSE_SPEED;
			if (criticalPulse > Math.PI * 2) {
				criticalPulse = 0;
			}

			// Calculate pulse intensity (creates a smooth sine wave)
			criticalEffect = (float) (0.3f + 0.2f * Math.sin(criticalPulse));
		} else {
			criticalEffect = 0;
			criticalPulse = 0;
		}

		weaponPickups.addAll(newPickups);
	}

	private void checkCollisions() {
		Iterator<Enemy> enemyIt = enemies.iterator();
		while (enemyIt.hasNext()) {
			Enemy enemy = enemyIt.next();

			// Check bullet collisions
			Iterator<Bullet> bulletIt = bullets.iterator();
			while (bulletIt.hasNext()) {
				Bullet bullet = bulletIt.next();
				if (Math.hypot(bullet.getX() - enemy.getX(), bullet.getY() - enemy.getY()) < Enemy.getSize()) {
					enemyIt.remove();
					bulletIt.remove();
					score += 10;
					break;
				}
			}

			// Check player collision
			int playerCenterX = player.getCollisionX() + Player.getCollisionWidth() / 2;
			int playerCenterY = player.getCollisionY() + Player.getCollisionHeight() / 2;
			int enemyCenterX = enemy.getX() + Enemy.getSize() / 2;
			int enemyCenterY = enemy.getY() + Enemy.getSize() / 2;

			double collisionDistance = (Player.getCollisionWidth() + Enemy.getSize()) / 2;
			if (Math.hypot(playerCenterX - enemyCenterX, playerCenterY - enemyCenterY) < collisionDistance) {
				player.takeDamage(20);
				if (player.getHealth() <= 0) {
					gameOver();
				}
			}
		}
	}

	private void spawnEnemies() {
		if (random.nextInt(50) == 0) {
			enemies.add(new Enemy(player));
		}
	}

	private void togglePause() {
		isPaused = !isPaused;
		if (isPaused) {
			isFiring = false;
			timer.stop();
		} else {
			timer.start();
		}
		repaint(); // Force a repaint to show/hide pause overlay
	}

	private void restartGame() {
		// Stop the timer first
		timer.stop();

		// Clear all game objects
		player = new Player(WIDTH / 2, HEIGHT / 2);
		enemies = new ArrayList<>();
		bullets = new ArrayList<>();
		weaponPickups = new ArrayList<>();
		score = 0;

		// Reset all game states
		isPaused = false;
		leftPressed = false;
		rightPressed = false;
		upPressed = false;
		downPressed = false;
		isFiring = false;
		isWeaponSwitching = false;
		weaponSwitchScale = 1.0f;
		screenShakeIntensity = 0;

		// Start the timer after everything is reset
		timer.start();
	}

	private void gameOver() {
		timer.stop();

		// Show dialog after timer is stopped
		int choice = JOptionPane.showOptionDialog(this, "Game Over! Score: " + score + "\nWould you like to retry?",
				"Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
				new String[] { "Retry", "Exit" }, "Retry");

		if (choice == JOptionPane.YES_OPTION) {
			// Make sure we're in a safe state before restarting
			SwingUtilities.invokeLater(() -> {
				restartGame();
			});
		} else {
			System.exit(0);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			togglePause();
			return;
		}

		if (!isPaused) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				upPressed = true;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				downPressed = true;
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				leftPressed = true;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				rightPressed = true;
				break;
			case KeyEvent.VK_1:
				switchWeapon(0);
				break;
			case KeyEvent.VK_2:
				switchWeapon(1);
				break;
			case KeyEvent.VK_SPACE:
				Point mouse = getMousePosition();
				Point gunTip = calculateGunTip();
				isFiring = true;
				if (mouse != null && gunTip != null) {
					double angle = calculateAimAngle(); // Use the same angle calculation
					ArrayList<Bullet> newBullets = player.shoot(gunTip.x, gunTip.y, angle);
					if (!newBullets.isEmpty()) {
						triggerScreenShake(5);
					}
					bullets.addAll(newBullets);
				}
				break;
			}
			updatePlayerVelocity();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!isPaused) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				upPressed = false;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				downPressed = false;
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				leftPressed = false;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				rightPressed = false;
				break;
			case KeyEvent.VK_SPACE:
				isFiring = false;
				break;
			}
			updatePlayerVelocity();
		}
	}

	private void updatePlayerVelocity() {
		int dx = 0;
		int dy = 0;

		if (leftPressed)
			dx -= 15;
		if (rightPressed)
			dx += 15;
		if (upPressed)
			dy -= 15;
		if (downPressed)
			dy += 15;

		player.setDX(dx);
		player.setDY(dy);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Not used but required by KeyListener interface
	}
}
