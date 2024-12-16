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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
	// Drawing shiz
	private BufferedImage backgroundImage;
	private static float SCALE_FACTOR = 1.0f;
	private static int BASE_currentWidth = 1280;
	private static int BASE_currentHeight = 720;
	private int currentWidth = BASE_currentWidth;
	private int currentHeight = BASE_currentHeight;

	// Entities
	private Player player;
	private ArrayList<Enemy> enemies;
	private ArrayList<Bullet> bullets;
	private ArrayList<HealthPickup> healthPickups;
	private ArrayList<ObjectivePickup> objectivePickups;
	private ArrayList<WeaponPickup> weaponPickups;

	private Timer timer;
	private Random random;
	private int score;

	// Flags
	private boolean isWeaponSwitching = false;
	private boolean isSpaceFiring = false;
	private boolean isMouseFiring = false;
	private boolean rightPressed = false;
	private boolean downPressed = false;
	private boolean leftPressed = false;
	private boolean upPressed = false;
	private boolean isPaused = false;

	// Weapon switch const
	private float weaponSwitchScale = 1.0f;

	// Damage effect consts
	private float damageEffect = 0.0f;
	private float criticalPulse = 0.0f;
	private float criticalEffect = 0.0f;
	private static final float DAMAGE_EFFECT_DECAY = 0.05f;
	private static final float CRITICAL_PULSE_SPEED = 0.05f;
	private static final float CRITICAL_HEALTH_THRESHOLD = 0.3f; // 30% health

	private double lastValidAngle = 0;

	// Game state vars
	private GameState currentState = GameState.MENU;
	private BufferedImage menuBackgroundImage;
	private Rectangle playButton;
	private Rectangle exitButton;

	// Shake effect vars
	private int screenShakeX = 0;
	private int screenShakeY = 0;
	private int screenShakeIntensity = 0;
	private Random screenShakeRandom = new Random();

	private static GamePanel instance;

	public GamePanel() {
		instance = this;
		setPreferredSize(new Dimension(currentWidth, currentHeight));
		setBackground(Color.GRAY);
		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);

		try {
			backgroundImage = ImageIO.read(getClass().getResource("/Background.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				handleResize();
			}
		});

		int buttonWidth = 200;
		int buttonHeight = 50;
		playButton = new Rectangle(BASE_currentWidth / 2 - buttonWidth / 2, BASE_currentHeight / 2 - buttonHeight,
				buttonWidth, buttonHeight);
		exitButton = new Rectangle(BASE_currentWidth / 2 - buttonWidth / 2, BASE_currentHeight / 2 + buttonHeight,
				buttonWidth, buttonHeight);

		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				handleResize();
			}
		});
	}

	private void drawMenu(Graphics2D g2d) {
		// Draw menu background
		if (menuBackgroundImage != null) {
			g2d.drawImage(menuBackgroundImage, 0, 0, BASE_currentWidth, BASE_currentHeight, null);
		} else {
			g2d.setColor(new Color(0, 0, 0, 150));
			g2d.fillRect(0, 0, BASE_currentWidth, BASE_currentHeight);
		}

		// Draw game title
		g2d.setFont(new Font("Arial", Font.BOLD, 72));
		g2d.setColor(Color.WHITE);
		String title = "WMNCB";
		FontMetrics fm = g2d.getFontMetrics();
		int titleX = BASE_currentWidth / 2 - fm.stringWidth(title) / 2;
		g2d.drawString(title, titleX, BASE_currentHeight / 3);

		// Draw buttons
		g2d.setFont(new Font("Arial", Font.BOLD, 32));

		// Play button
		g2d.setColor(Color.GREEN);
		g2d.fill(playButton);
		g2d.setColor(Color.WHITE);
		g2d.draw(playButton);
		String playText = "PLAY";
		fm = g2d.getFontMetrics();
		int textX = playButton.x + (playButton.width - fm.stringWidth(playText)) / 2;
		int textY = playButton.y + (playButton.height + fm.getAscent()) / 2 - 5;
		g2d.drawString(playText, textX, textY);

		// Exit button
		g2d.setColor(Color.RED);
		g2d.fill(exitButton);
		g2d.setColor(Color.WHITE);
		g2d.draw(exitButton);
		String exitText = "EXIT";
		textX = exitButton.x + (exitButton.width - fm.stringWidth(exitText)) / 2;
		textY = exitButton.y + (exitButton.height + fm.getAscent()) / 2 - 5;
		g2d.drawString(exitText, textX, textY);
	}

	private void initializeGame() {
		player = new Player(currentWidth / 2, currentHeight / 2);
		objectivePickups = new ArrayList<>();
		weaponPickups = new ArrayList<>();
		healthPickups = new ArrayList<>();
		enemies = new ArrayList<>();
		bullets = new ArrayList<>();
		random = new Random();
		score = 0;

		timer = new Timer(20, this);
		timer.start();
	}

	private void handleResize() {
		Dimension screenSize = getSize();
		currentWidth = screenSize.width;
		currentHeight = screenSize.height;

		// Calculate scale factor to fill the window while maintaining aspect ratio
		float scaleX = (float) currentWidth / BASE_currentWidth;
		float scaleY = (float) currentHeight / BASE_currentHeight;
		SCALE_FACTOR = Math.max(scaleX, scaleY);

		// Center the game area
		int gameWidth = (int) (BASE_currentWidth * SCALE_FACTOR);
		int gameHeight = (int) (BASE_currentHeight * SCALE_FACTOR);
		int offsetX = (currentWidth - gameWidth) / 2;
		int offsetY = (currentHeight - gameHeight) / 2;

		AffineTransform transform = new AffineTransform();
		transform.translate(offsetX, offsetY);
		transform.scale(SCALE_FACTOR, SCALE_FACTOR);
	}

	public static void triggerHitEffects() {
		if (instance != null) {
			instance.damageEffect = 1.0f;
			instance.triggerScreenShake(15);
		}
	}

	private Class<? extends Weapon> getRandomAvailableWeaponType() {
		ArrayList<Class<? extends Weapon>> availableTypes = new ArrayList<>();

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
		if (random.nextInt(200) == 0) {
			Class<? extends Weapon> weaponType = getRandomAvailableWeaponType();
			if (weaponType != null) {
				int x = random.nextInt(currentWidth - 20);
				int y = random.nextInt(currentHeight - 20);

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

	private void spawnHealthPickup() {
		if (random.nextInt(300) == 0) {
			int x = random.nextInt(currentWidth - 20);
			int y = random.nextInt(currentHeight - 20);
			healthPickups.add(new HealthPickup(x, y));
		}
	}

	private void spawnObjectivePickup() {
		if (random.nextInt(200) == 0) {
			int x = random.nextInt(currentWidth - 20);
			int y = random.nextInt(currentHeight - 20);
			objectivePickups.add(new ObjectivePickup(x, y));
		}
	}

	private void drawDamageEffect(Graphics2D g2d) {
		// Calculate the strongest effect between damage hit and critical pulse
		float effectStrength = Math.max(damageEffect, criticalEffect);

		if (effectStrength > 0) {
			int centerX = currentWidth / 2;
			int centerY = currentHeight / 2;
			int radius = (int) (Math.max(currentWidth, currentHeight) * 0.7);

			// Create a radial gradient paint
			RadialGradientPaint paint = new RadialGradientPaint(centerX, centerY, radius,
					new float[] { 0.0f, 0.7f, 1.0f },
					new Color[] { new Color(0, 0, 0, 0), new Color(255, 0, 0, (int) (effectStrength * 100)),
							new Color(255, 0, 0, (int) (effectStrength * 150)) });

			Composite originalComposite = g2d.getComposite();
			Paint originalPaint = g2d.getPaint();

			g2d.setPaint(paint);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, effectStrength));
			g2d.fillRect(0, 0, currentWidth, currentHeight);

			g2d.setPaint(originalPaint);
			g2d.setComposite(originalComposite);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// Save original transform
		AffineTransform originalTransform = g2d.getTransform();

		// Scale everything
		g2d.scale(SCALE_FACTOR, SCALE_FACTOR);

		// Draw background scaled
		if (backgroundImage != null) {
			g.drawImage(backgroundImage, 0, 0, BASE_currentWidth, BASE_currentHeight, null);
		}

		switch (currentState) {
		case MENU:
			drawMenu(g2d);
			break;
		case PLAYING:
			// Shake
			g2d.translate(screenShakeX, screenShakeY);

			// Draw player
//    		g.setColor(Color.RED);
//    		g.fillRect(player.getX(), player.getY(), Player.getSize(), Player.getSize());
			player.render((Graphics2D) g);

			// Draw current weapon sprite
			Point mouse = getScaledMousePosition();
			if (mouse != null) {
				int centerX = player.getCollisionX() + Player.getCollisionWidth() / 2;
				int centerY = player.getCollisionY() + Player.getCollisionHeight() / 2;

				double angle = calculateAimAngle();

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

			// Draw pickups
			for (WeaponPickup pickup : weaponPickups) {
				pickup.render(g);
			}
			for (HealthPickup pickup : healthPickups) {
				pickup.render(g);
			}
			for (ObjectivePickup pickup : objectivePickups) {
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
//    		Point gunTip = calculateGunTip();
//    		if (gunTip != null) {
//    			g2d.setColor(Color.RED);
//    			g2d.fillOval(gunTip.x - 2, gunTip.y - 2, 4, 4);
//    		}

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
			Font customFont = new Font("Arial", Font.BOLD, 30);
			g.setFont(customFont);
			g.drawString("Score: " + score, 10, 30);

			drawHealthBar(g);
			drawInventoryUI(g);

			g2d.setTransform(originalTransform);
			break;
		case PAUSED:
			player.render((Graphics2D) g);

			for (WeaponPickup pickup : weaponPickups) {
				pickup.render(g);
			}
			for (HealthPickup pickup : healthPickups) {
				pickup.render(g);
			}
			for (ObjectivePickup pickup : objectivePickups) {
				pickup.render(g);
			}

			g.setColor(Color.BLACK);
			for (Enemy enemy : enemies) {
				g.fillOval(enemy.getX(), enemy.getY(), Enemy.getSize(), Enemy.getSize());
			}

			for (Bullet bullet : bullets) {
				bullet.render(g);
			}

			g2d.setTransform(originalTransform);

			// Draw UI elements
			drawHealthBar(g);
			drawInventoryUI(g);

			g2d.setColor(new Color(0, 0, 0, 150));
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.scale(SCALE_FACTOR, SCALE_FACTOR);

			// Draw pause text
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Arial", Font.BOLD, 50));
			String pauseText = "PAUSED";
			FontMetrics fm = g2d.getFontMetrics();
			int textWidth = fm.stringWidth(pauseText);
			g2d.drawString(pauseText, BASE_currentWidth / 2 - textWidth / 2, BASE_currentHeight / 2);

			// Draw instructions
			g2d.setFont(new Font("Arial", Font.PLAIN, 20));
			String instructionText = "Press ESC to resume";
			FontMetrics fm2 = g2d.getFontMetrics();
			textWidth = fm2.stringWidth(instructionText);
			g2d.drawString(instructionText, BASE_currentWidth / 2 - textWidth / 2, BASE_currentHeight / 2 + 40);
			g2d.setTransform(originalTransform);
			break;
		}

		g2d.setTransform(originalTransform);
	}

	private void drawHealthBar(Graphics g) {
		int barWidth = 200;
		int barHeight = 20;
		int x = currentWidth - barWidth - 10;
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
		int textX = x + 5;
		int textY = y + ((barHeight - fm.getHeight()) / 2) + fm.getAscent() - 2;
		Font originalFont = g.getFont();
		Font customFont = new Font("Arial", Font.BOLD | Font.ITALIC, 20);
		g.setFont(customFont);
		g.drawString(healthText, textX, textY);
		g.setFont(originalFont);
	}

	private void drawInventoryUI(Graphics g) {
		ArrayList<Weapon> inventory = player.getInventory();
		int slotWidth = 64;
		int slotHeight = 64;
		int padding = 2; // Padding between slots
		int startX = 10; // Starting X position
		int startY = currentHeight - slotHeight - 10; // Position from bottom of screen

		// Draw the inventory slots
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

				// Calculate scaling to fit weapon
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

	private Point getScaledMousePosition() {
		Point mousePos = getMousePosition();
		if (mousePos != null) {
			int offsetX = (currentWidth - (int) (BASE_currentWidth * SCALE_FACTOR)) / 2;
			int offsetY = (currentHeight - (int) (BASE_currentHeight * SCALE_FACTOR)) / 2;
			return new Point((int) ((mousePos.x - offsetX) / SCALE_FACTOR),
					(int) ((mousePos.y - offsetY) / SCALE_FACTOR));
		}
		return null;
	}

	private double calculateAimAngle() {
		Point mouse = getScaledMousePosition();
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
		Point mouse = getScaledMousePosition();
		if (mouse != null) {
			// Use the same center point as weapon rendering
			int centerX = player.getCollisionX() + Player.getCollisionWidth() / 2;
			int centerY = player.getCollisionY() + Player.getCollisionHeight() / 2;

			double angle = calculateAimAngle();

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
		if (isMouseFiring || isSpaceFiring) {
			Point mouse = getScaledMousePosition();
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
			if (bullet.getX() < 0 || bullet.getX() > currentWidth || bullet.getY() < 0
					|| bullet.getY() > currentHeight) {
				bulletIt.remove();
			}
		}

		// Update enemies
		enemies.forEach(Enemy::update);

		spawnObjectivePickup();
		spawnWeaponPickup();
		spawnHealthPickup();

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
				player.pickupWeapon(pickup.getWeapon());
				pickupIt.remove();
			}
		}

		// Update health pickups
		Iterator<HealthPickup> healthIt = healthPickups.iterator();
		while (healthIt.hasNext()) {
			HealthPickup pickup = healthIt.next();
			pickup.update();
			if (pickup.shouldDespawn()) {
				healthIt.remove();
				continue;
			}
			if (pickup.collidesWith(player)) {
				player.heal(pickup.getHealAmount());
				healthIt.remove();
			}
		}

		// Update objective pickups
		Iterator<ObjectivePickup> objectiveIt = objectivePickups.iterator();
		while (objectiveIt.hasNext()) {
			ObjectivePickup pickup = objectiveIt.next();
			pickup.update();
			if (pickup.shouldDespawn()) {
				objectiveIt.remove();
				continue;
			}
			if (pickup.collidesWith(player)) {
				score += pickup.getScoreValue();
				objectiveIt.remove();
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
			criticalEffect = (float) (0.75f + 0.2f * Math.sin(criticalPulse));
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
			currentState = GameState.PAUSED;
			isMouseFiring = false;
			isSpaceFiring = false;
			timer.stop();
		} else {
			currentState = GameState.PLAYING;
			timer.start();
		}
		repaint();
	}

	private void restartGame() {
		// Stop the timer first
		timer.stop();

		// Clear all game objects
		player = new Player(currentWidth / 2, currentHeight / 2);
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
		isSpaceFiring = false;
		isMouseFiring = false;
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
		if ((currentState == GameState.PLAYING || currentState == GameState.PAUSED)
				&& e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			togglePause();
			return;
		}

		if (currentState == GameState.PLAYING && !isPaused) {
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
				Point mouse = getScaledMousePosition();
				Point gunTip = calculateGunTip();
				isSpaceFiring = true;
				if (mouse != null && gunTip != null) {
					double angle = calculateAimAngle();
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
	public void mousePressed(MouseEvent e) {
		Point scaledPoint = new Point(
				(int) ((e.getX() - (currentWidth - BASE_currentWidth * SCALE_FACTOR) / 2) / SCALE_FACTOR),
				(int) ((e.getY() - (currentHeight - BASE_currentHeight * SCALE_FACTOR) / 2) / SCALE_FACTOR));

		if (currentState == GameState.MENU) {
			if (playButton.contains(scaledPoint)) {
				currentState = GameState.PLAYING;
				initializeGame();
			} else if (exitButton.contains(scaledPoint)) {
				System.exit(0);
			}
		} else if (currentState == GameState.PLAYING && !isPaused) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				isMouseFiring = true;
				Point gunTip = calculateGunTip();
				if (gunTip != null) {
					double angle = calculateAimAngle();
					ArrayList<Bullet> newBullets = player.shoot(gunTip.x, gunTip.y, angle);
					if (!newBullets.isEmpty()) {
						triggerScreenShake(5);
					}
					bullets.addAll(newBullets);
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			isMouseFiring = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (currentState == GameState.PLAYING) {
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
				isSpaceFiring = false;
				break;
			}
			updatePlayerVelocity();
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
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
