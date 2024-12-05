package pewpew;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ShotgunPellet extends Bullet {
    public ShotgunPellet(int x, int y, double angle) {
        super(x, y, angle, 17, 5, 25, /*Color.ORANGE*/ "/Small_Bullet.png");
    }
}
