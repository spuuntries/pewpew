package pewpew;

import java.awt.*;

public class BasicBullet extends Bullet {
    public BasicBullet(int x, int y, double angle) {
        super(x, y, angle, 10, 10, 5, Color.YELLOW);
    }
}