package pewpew;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class Bullet extends GameObject {
    protected double dx, dy;
    protected int damage;
    protected int speed;
    protected int size;
    protected Color color;
    protected BufferedImage sprite;

    public Bullet(int x, int y, double angle, int speed, int damage, int size, String spritePath /*Color color*/) {  
        super(x, y);
        this.speed = speed;
        this.damage = damage;
        this.size = size;
        //this.color = color;
        this.sprite = loadImage(spritePath);
        dx = speed * Math.cos(angle);
        dy = speed * Math.sin(angle);
    }
    
 // Load an image from the specified path
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null; // return null if the image fails to load
        }
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
    }
    
    
    public void render(Graphics g) {
        if (sprite != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            // Bullet's center
            g2d.translate((int) x, (int) y);
            // Rotate by the bullet's angle (in radians)
            double angle = Math.atan2(dx, dy); // Calculate angle from dx, dy
            g2d.rotate(angle);
        	// Draw the sprite, centered on the bullet's x and y coordinates
            g2d.drawImage(sprite, (int) - size / 2, (int) - size / 2, size, size, null);
            
            // g2d.dispose();
        } else {
            g.setColor(color); // Optional color if no sprite
            g.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        }
    }


    public int getDamage() {
        return damage;
    }

    public int getSize() {
        return size;
    }

    /*public Color getColor() {
        return color;
    }*/
}
