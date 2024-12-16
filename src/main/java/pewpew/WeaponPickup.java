package pewpew;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class WeaponPickup extends GameObject {
	private static final float ROTATION_SPEED = 0.03f;
	private static final float FLOAT_SPEED = 0.05f;
	private static final float FLOAT_AMPLITUDE = 10;
	private static final float GLOW_SPEED = 0.03f;
	private static final int LIFETIME_MS = 5000;
	private static final int PICKUP_SIZE = 20;
	private static final int FADE_START_MS = 3000;

	private boolean glowIncreasing = true;
	private boolean isFading = false;
	private float floatOffset = 0.8f;
	private float fadeAlpha = 1.0f;
	private float maxGlowAlpha = 0.8f;
	private float glowAlpha = 0;
	private float rotation = 0;
	private long spawnTime;
	private Weapon weapon;

	public WeaponPickup(int x, int y, Weapon weapon) {
		super(x, y);
		this.weapon = weapon;
		this.spawnTime = System.currentTimeMillis();
	}

	@Override
	public void update() {
		// Update rotation
		rotation += ROTATION_SPEED;
		if (rotation > Math.PI * 2) {
			rotation -= Math.PI * 2;
		}

		// Update floating motion
		floatOffset = (float) (Math.sin(System.currentTimeMillis() * FLOAT_SPEED) * FLOAT_AMPLITUDE);

		// Update glow effect
		if (glowIncreasing) {
			glowAlpha += GLOW_SPEED;
			if (glowAlpha >= maxGlowAlpha) {
				glowAlpha = maxGlowAlpha;
				glowIncreasing = false;
			}
		} else {
			glowAlpha -= GLOW_SPEED;
			if (glowAlpha <= 0.0f) {
				glowAlpha = 0.0f;
				glowIncreasing = true;
			}
		}

		// Update fade
		long aliveTime = System.currentTimeMillis() - spawnTime;
		if (aliveTime > FADE_START_MS) {
			fadeAlpha = 1.0f - ((float) (aliveTime - FADE_START_MS) / (LIFETIME_MS - FADE_START_MS));
			fadeAlpha = Math.max(0.2f, fadeAlpha);
		}
	}

	public boolean shouldDespawn() {
		return System.currentTimeMillis() - spawnTime > LIFETIME_MS;
	}

	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		BufferedImage sprite = weapon.getSprite();

		// Save original composite
		Composite oldComposite = g2d.getComposite();
		AffineTransform oldTransform = g2d.getTransform();

		if (sprite != null) {
			// Calculate scales
			double scaleX = (double) weapon.getDisplayWidth() / sprite.getWidth();
			double scaleY = (double) weapon.getDisplayHeight() / sprite.getHeight();

			// Draw glow effect first
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha * fadeAlpha * 0.5f));
			g2d.setColor(Color.YELLOW);
			int glowSize = (int) (Math.max(weapon.getDisplayWidth(), weapon.getDisplayHeight()) * 1.5);
			g2d.fillOval(x - glowSize / 2, (int) (y - glowSize / 2 + floatOffset), glowSize, glowSize);

			// Draw sprite with fade
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
			g2d.translate(x, y + floatOffset);
			g2d.rotate(rotation);
			g2d.scale(scaleX, scaleY);

			g2d.drawImage(sprite, -sprite.getWidth() / 2, -sprite.getHeight() / 2, null);
		} else {
			// Fallback with fade
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
			g2d.translate(x, y + floatOffset);
			g2d.rotate(rotation);

			g2d.setColor(Color.YELLOW);
			g2d.fillRect(-PICKUP_SIZE / 2, -PICKUP_SIZE / 2, PICKUP_SIZE, PICKUP_SIZE);
			g2d.setColor(Color.BLACK);
			g2d.drawString(weapon.getClass().getSimpleName().charAt(0) + "", -PICKUP_SIZE / 4, PICKUP_SIZE / 4);
		}

		// Restore original state
		g2d.setTransform(oldTransform);
		g2d.setComposite(oldComposite);
	}

	public boolean collidesWith(Player player) {
		// Use the larger of width or height for circular collision
		float pickupRadius = Math.max(weapon.getDisplayWidth(), weapon.getDisplayHeight()) / 2;
		float playerRadius = Player.getSize() / 2;

		// Get centers
		float pickupCenterX = x;
		float pickupCenterY = y + floatOffset; // Include the float offset for accurate collision
		float playerCenterX = player.getX() + playerRadius;
		float playerCenterY = player.getY() + playerRadius;

		// Add a bit of padding to make pickup easier (multiply by 1.5 or whatever feels
		// good)
		float collisionDistance = (pickupRadius + playerRadius) * 1.5f;

		return Math.hypot(pickupCenterX - playerCenterX, pickupCenterY - playerCenterY) < collisionDistance;
	}

	public Weapon getWeapon() {
		return weapon;
	}

	public boolean isFading() {
		return isFading;
	}

	public void setFading(boolean isFading) {
		this.isFading = isFading;
	}
}
