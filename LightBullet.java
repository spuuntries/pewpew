package pewpew;

import java.awt.Color;

public class LightBullet extends Bullet {
    public LightBullet(int x, int y, double angle) {
        super(x, y, angle, 20, 5, 20, /*Color.GREEN*/ "resources/Small_Bullet.png");
    }
}
