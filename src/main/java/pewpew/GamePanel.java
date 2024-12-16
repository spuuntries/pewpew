package pewpew;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

	// Movement control variables
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean upPressed = false;
	private boolean downPressed = false;
	private boolean isFiring = false;

	private float weaponSwitchScale = 1.0f;
	private boolean isWeaponSwitching = false;
	
	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.GRAY);
		setFocusable(true);
		addKeyListener(this);

		initializeGame();
	}

	private void initializeGame() {
		player = new Player(WIDTH / 2, HEIGHT / 2);
		enemies = new ArrayList<>();
		bullets = new ArrayList<>();
		random = new Random();
		score = 0;

		timer = new Timer(20, this);
		timer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Draw player
		g.setColor(Color.RED);
		g.fillRect(player.getX(), player.getY(), Player.getSize(), Player.getSize());

		// Draw current weapon sprite
		Point mouse = getMousePosition();
		if (mouse != null) {
			Graphics2D g2d = (Graphics2D) g;
			int centerX = player.getX() + Player.getSize() / 2;
			int centerY = player.getY() + Player.getSize() / 2;

			double angle = Math.atan2(mouse.y - centerY, mouse.x - centerX);
			// player.updateGunAngle(angle);

			// Save the current transform
			AffineTransform old = g2d.getTransform();

			// Translate to player center
			g2d.translate(centerX, centerY);

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

		// Draw enemies
		g.setColor(Color.BLACK);
		for (Enemy enemy : enemies) {
			g.fillOval(enemy.getX(), enemy.getY(), Enemy.getSize(), Enemy.getSize());
		}

		// Draw bullets with their specific colors
		for (Bullet bullet : bullets) {
			bullet.render(g);
		}

		// Draw score and current weapon
		g.setColor(Color.WHITE);
		Font customFont = new Font("Arial", Font.BOLD, 30); // Font size 30, bold style
		g.setFont(customFont);
		g.drawString("Score: " + score, 10, 30);
	}

	private Point calculateGunTip() {
		Point mouse = getMousePosition();
		if (mouse != null) {
			int centerX = player.getX() + Player.getSize() / 2;
			int centerY = player.getY() + Player.getSize() / 2;

			double angle = Math.atan2(mouse.y - centerY, mouse.x - centerX);

			// Calculate gun tip position based on current weapon size
			int gunLength = player.getCurrentWeapon().getDisplayWidth();
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

		// Handle continuous firing
		if (isFiring) {
			Point mouse = getMousePosition();
			Point gunTip = calculateGunTip();
			if (mouse != null && gunTip != null) {
				double angle = Math.atan2(mouse.y - gunTip.y, mouse.x - gunTip.x);
				bullets.addAll(player.shoot(gunTip.x, gunTip.y, angle));
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
			if (Math.hypot(player.getX() - enemy.getX(),
					player.getY() - enemy.getY()) < (Player.getSize() + Enemy.getSize()) / 2) {
				gameOver();
			}
		}
	}

	private void spawnEnemies() {
		if (random.nextInt(50) == 0) {
			enemies.add(new Enemy(player));
		}
	}

	private void restartGame() {
	    player = new Player(WIDTH / 2, HEIGHT / 2);
	    enemies.clear();
	    bullets.clear();
	    score = 0;
	    timer.start();
	    
	    leftPressed = false;
	    rightPressed = false;
	    upPressed = false;
	    downPressed = false;
	    isFiring = false;
	}
	
	private void gameOver() {
		timer.stop();
		int choice = JOptionPane.showOptionDialog(this, 
		        "Game Over! Score: " + score + "\nWould you like to retry?", 
		        "Game Over", 
		        JOptionPane.YES_NO_OPTION, 
		        JOptionPane.INFORMATION_MESSAGE, 
		        null, 
		        new String[]{"Retry", "Exit"}, 
		        "Retry");

		    if (choice == JOptionPane.YES_OPTION) {
		        restartGame();
		    } else {
		        System.exit(0);
		    }
	}

	@Override
	public void keyPressed(KeyEvent e) {
	    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
	        isPaused = !isPaused;  
	        if (isPaused) {
	            timer.stop();  
	        } else {
	            timer.start(); 
	        }
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
	            case KeyEvent.VK_3:
	                switchWeapon(2);
	                break;
	            case KeyEvent.VK_SPACE:
	                Point mouse = getMousePosition();
	                Point gunTip = calculateGunTip();
	                isFiring = true;
	                if (mouse != null && gunTip != null) {
	                    double angle = Math.atan2(mouse.y - gunTip.y, mouse.x - gunTip.x);
	                    bullets.addAll(player.shoot(gunTip.x, gunTip.y, angle));
	                }
	                break;
	        }
	        updatePlayerVelocity();
	    }
	}


	@Override
	public void keyReleased(KeyEvent e) {
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
