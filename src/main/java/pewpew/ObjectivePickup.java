package pewpew;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class ObjectivePickup extends GameObject {
	private static final float ROTATION_SPEED = 0.03f;
	private static final float FLOAT_SPEED = 0.05f;
	private static final float FLOAT_AMPLITUDE = 10;
	private static final float GLOW_SPEED = 0.03f;
	private static final int LIFETIME_MS = 8000;
	private static final int PICKUP_SIZE = 25;
	private static final int FADE_START_MS = 6000;
	private static final int SCORE_VALUE = 50;

	private boolean glowIncreasing = true;
	private float floatOffset = 0;
	private float fadeAlpha = 1.0f;
	private float maxGlowAlpha = 0.8f;
	private float glowAlpha = 0;
	private float rotation = 0;
	private long spawnTime;

	public ObjectivePickup(int x, int y) {
		super(x, y);
		this.spawnTime = System.currentTimeMillis();
	}

	@Override
	public void update() {
		rotation += ROTATION_SPEED;
		if (rotation > Math.PI * 2) {
			rotation -= Math.PI * 2;
		}

		floatOffset = (float) (Math.sin(System.currentTimeMillis() * FLOAT_SPEED) * FLOAT_AMPLITUDE);

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

		long aliveTime = System.currentTimeMillis() - spawnTime;
		if (aliveTime > FADE_START_MS) {
			fadeAlpha = 1.0f - ((float) (aliveTime - FADE_START_MS) / (LIFETIME_MS - FADE_START_MS));
			fadeAlpha = Math.max(0.2f, fadeAlpha);
		}
	}

	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		Composite oldComposite = g2d.getComposite();
		AffineTransform oldTransform = g2d.getTransform();

		// Draw glow effect
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha * fadeAlpha * 0.5f));
		g2d.setColor(Color.YELLOW);
		int glowSize = PICKUP_SIZE * 2;
		g2d.fillOval(x - glowSize / 2, (int) (y - glowSize / 2 + floatOffset), glowSize, glowSize);

		// Draw star shape
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
		g2d.translate(x, y + floatOffset);
		g2d.rotate(rotation);

		g2d.setColor(Color.ORANGE);
		drawStar(g2d, 0, 0, PICKUP_SIZE / 2, PICKUP_SIZE / 4, 5);

		g2d.setTransform(oldTransform);
		g2d.setComposite(oldComposite);
	}

	private void drawStar(Graphics2D g2d, int x, int y, int outerRadius, int innerRadius, int points) {
		int[] xPoints = new int[points * 2];
		int[] yPoints = new int[points * 2];

		for (int i = 0; i < points * 2; i++) {
			double angle = Math.PI * i / points - Math.PI / 2;
			int radius = (i % 2 == 0) ? outerRadius : innerRadius;
			xPoints[i] = x + (int) (Math.cos(angle) * radius);
			yPoints[i] = y + (int) (Math.sin(angle) * radius);
		}

		g2d.fillPolygon(xPoints, yPoints, points * 2);
	}

	public boolean shouldDespawn() {
		return System.currentTimeMillis() - spawnTime > LIFETIME_MS;
	}

	public boolean collidesWith(Player player) {
		float playerRadius = Player.getSize() / 2;
		float pickupCenterX = x;
		float pickupCenterY = y + floatOffset;
		float playerCenterX = player.getCollisionX() + playerRadius;
		float playerCenterY = player.getCollisionY() + playerRadius;

		return Math.hypot(pickupCenterX - playerCenterX, pickupCenterY - playerCenterY) < playerRadius
				+ PICKUP_SIZE / 2;
	}

	public int getScoreValue() {
		return SCORE_VALUE;
	}
}
