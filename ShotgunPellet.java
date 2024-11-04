package pewpew;

import java.awt.*;

public class ShotgunPellet extends Bullet {
    public ShotgunPellet(int x, int y, double angle) {
        super(x, y, angle, 8, 5, 3, Color.ORANGE);
    }
}
