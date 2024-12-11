package pewpew;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public abstract class Weapon {
    protected int fireRate; // shots per second
    protected long lastShotTime;
    protected BufferedImage sprite;
    protected int displayWidth; // width to display the weapon
    protected int displayHeight; // height to display the weapon

    public Weapon(int fireRate, String spritePath, int displayWidth, int displayHeight) {
        this.fireRate = fireRate;
        this.lastShotTime = 0;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        try {
            sprite = ImageIO.read(getClass().getResource(spritePath));
        } catch (IOException e) {
            System.err.println("Error loading weapon sprite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public boolean canShoot() {
        return System.currentTimeMillis() - lastShotTime >= 1000.0 / fireRate;
    }

    public abstract ArrayList<Bullet> shoot(int gunTipX, int gunTipY, double angle);

    protected void updateLastShotTime() {
        lastShotTime = System.currentTimeMillis();
    }

    public BufferedImage getSprite() {
        return sprite;
    }
}
