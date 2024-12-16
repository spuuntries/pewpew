package pewpew;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player extends GameObject {
	private int dx, dy;
	private int currentWeaponIndex = 0;

	// Size and display constants
	private static final int SPRITE_WIDTH = 48; // Original sprite width
	private static final int SPRITE_HEIGHT = 64; // Original sprite height
	private static final float SCALE = 1.5f; // Adjust this to change overall size
	private static final int DISPLAY_WIDTH = (int) (SPRITE_WIDTH * SCALE);
	private static final int DISPLAY_HEIGHT = (int) (SPRITE_HEIGHT * SCALE);
	private static final int COLLISION_WIDTH = (int) (SPRITE_WIDTH * SCALE * 0.4f);
	private static final int COLLISION_HEIGHT = (int) (SPRITE_HEIGHT * SCALE * 0.4f);

	private static final int MAX_HEALTH = 100;
	private int health;
	private boolean isInvulnerable = false;
	private long invulnerabilityTimer = 0;
	private static final long INVULNERABILITY_DURATION = 1000; // 1 second of invulnerability after taking damage

	private boolean isHit = false;
	private long hitEffectTimer = 0;
	private static final long HIT_EFFECT_DURATION = 100; // milliseconds
	private Color hitTint = new Color(255, 0, 0, 100);

	private static final int MAX_WEAPONS = 2;
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;

	// Animation timing constants
	private static final int IDLE_SPEED = 150; // Slower for idle
	private static final int WALKING_SPEED = 100; // Faster for walking
	private long lastAnimationUpdate;

	private enum AnimationState {
		IDLE, WALKING_LEFT, WALKING_RIGHT, WALKING_UP, WALKING_DOWN
	}

	private AnimationState currentState = AnimationState.IDLE;
	private int currentFrame = 0;

	// Sprite sheets for different states
	private BufferedImage[] idleSprites;
	private BufferedImage[] walkLeftSprites;
	private BufferedImage[] walkRightSprites;
	private BufferedImage[] walkUpSprites;
	private BufferedImage[] walkDownSprites;

	private ArrayList<Weapon> inventory;
	private Weapon currentWeapon;

	public Player(int x, int y) {
		super(x, y);
		loadSprites();
		health = MAX_HEALTH;

		dx = dy = 0;
		inventory = new ArrayList<>();
		inventory.add(new BasicGun());
		currentWeapon = inventory.get(0);
	}

	public static int getCollisionWidth() {
		return COLLISION_WIDTH;
	}

	public static int getCollisionHeight() {
		return COLLISION_HEIGHT;
	}

	public void takeDamage(int amount) {
		if (!isInvulnerable) {
			health = Math.max(0, health - amount);
			isInvulnerable = true;
			invulnerabilityTimer = System.currentTimeMillis();
			isHit = true;
			hitEffectTimer = System.currentTimeMillis();

			// Trigger screen shake and damage effect in GamePanel
			GamePanel.triggerHitEffects();
		}
	}

	public int getHealth() {
		return health;
	}

	public void heal(int amount) {
		health = Math.min(MAX_HEALTH, health + amount);
	}

	private void loadSprites() {
		try {
			// Load each animation sequence from its own file
			SpriteSheet idleSheet = new SpriteSheet("/idle.png", SPRITE_WIDTH, SPRITE_HEIGHT);
			SpriteSheet walkLeftSheet = new SpriteSheet("/walk_left.png", SPRITE_WIDTH, SPRITE_HEIGHT);
			SpriteSheet walkRightSheet = new SpriteSheet("/walk_right.png", SPRITE_WIDTH, SPRITE_HEIGHT);
			SpriteSheet walkUpSheet = new SpriteSheet("/walk_up.png", SPRITE_WIDTH, SPRITE_HEIGHT);
			SpriteSheet walkDownSheet = new SpriteSheet("/walk_down.png", SPRITE_WIDTH, SPRITE_HEIGHT);

			// Get all frames from each sheet
			idleSprites = idleSheet.getSprites(0, 0, 8);
			walkLeftSprites = walkLeftSheet.getSprites(0, 0, 8);
			walkRightSprites = walkRightSheet.getSprites(0, 0, 8);
			walkUpSprites = walkUpSheet.getSprites(0, 0, 8);
			walkDownSprites = walkDownSheet.getSprites(0, 0, 8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getCurrentAnimationSpeed() {
		return currentState == AnimationState.IDLE ? IDLE_SPEED : WALKING_SPEED;
	}

	private int findEmptySlot() {
		for (int i = 0; i < MAX_WEAPONS; i++) {
			if (i >= inventory.size()) {
				return i;
			}
		}
		return -1;
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
			inventory.add(newWeapon);
			currentWeapon = newWeapon;
			currentWeaponIndex = inventory.size() - 1;
			return null;
		} else {
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
		// Update position
		x += dx;
		y += dy;

		if (isInvulnerable && System.currentTimeMillis() - invulnerabilityTimer > INVULNERABILITY_DURATION) {
			isInvulnerable = false;
		}

		// Constrain movement based on collision box
		int collisionX = getCollisionX();
		int collisionY = getCollisionY();

		if (collisionX < 0)
			x = -(DISPLAY_WIDTH - COLLISION_WIDTH) / 2;
		if (collisionX + COLLISION_WIDTH > WIDTH)
			x = WIDTH - COLLISION_WIDTH - (DISPLAY_WIDTH - COLLISION_WIDTH) / 2;
		if (collisionY < 0)
			y = -(DISPLAY_HEIGHT - COLLISION_HEIGHT) / 2;
		if (collisionY + COLLISION_HEIGHT > HEIGHT)
			y = HEIGHT - COLLISION_HEIGHT - (DISPLAY_HEIGHT - COLLISION_HEIGHT) / 2;

		// Update animation state
		updateAnimationState();

		// Update animation frame
		if (System.currentTimeMillis() - lastAnimationUpdate > getCurrentAnimationSpeed()) {
			BufferedImage[] currentAnim = getCurrentAnimation();
			if (currentAnim != null && currentAnim.length > 0) {
				currentFrame = (currentFrame + 1) % currentAnim.length;
			}
			lastAnimationUpdate = System.currentTimeMillis();
		}
	}

	private BufferedImage[] getCurrentAnimation() {
		switch (currentState) {
		case WALKING_LEFT:
			return walkLeftSprites;
		case WALKING_RIGHT:
			return walkRightSprites;
		case WALKING_UP:
			return walkUpSprites;
		case WALKING_DOWN:
			return walkDownSprites;
		case IDLE:
		default:
			return idleSprites;
		}
	}

	private void updateAnimationState() {
		if (dx == 0 && dy == 0) {
			currentState = AnimationState.IDLE;
		} else if (Math.abs(dx) > Math.abs(dy)) {
			// Horizontal movement takes precedence
			currentState = dx > 0 ? AnimationState.WALKING_RIGHT : AnimationState.WALKING_LEFT;
		} else {
			// Vertical movement
			currentState = dy > 0 ? AnimationState.WALKING_DOWN : AnimationState.WALKING_UP;
		}
	}

	public void render(Graphics2D g) {
		BufferedImage[] currentAnim = getCurrentAnimation();
		if (currentAnim != null && currentAnim.length > 0) {
			// Draw sprite
			g.drawImage(currentAnim[currentFrame], x, y, DISPLAY_WIDTH, DISPLAY_HEIGHT, null);

			// Debug: draw collision box
//			g.setColor(new Color(255, 0, 0, 50));
//			g.fillRect(getCollisionX(), getCollisionY(), COLLISION_WIDTH, COLLISION_HEIGHT);
		}
	}

	public void setDX(int dx) {
		this.dx = dx;
	}

	public void setDY(int dy) {
		this.dy = dy;
	}

	public static int getSize() {
		return Math.max(COLLISION_WIDTH, COLLISION_HEIGHT);
	}

	public int getCollisionX() {
		return x + (DISPLAY_WIDTH - COLLISION_WIDTH) / 2;
	}

	public int getCollisionY() {
		return y + (DISPLAY_HEIGHT - COLLISION_HEIGHT) / 2;
	}
}
