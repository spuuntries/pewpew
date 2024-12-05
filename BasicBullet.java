package pewpew;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BasicBullet extends Bullet {
    public BasicBullet(int x, int y, double angle) {
        super(x, y, angle, 15, 10, 30, /*Color.YELLOW*/ "/Small_Bullet.png");
    }
}