package pewpew;

import java.util.Random;

public class Enemy extends GameObject {
    private static final int ENEMY_SIZE = 20;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final Random random = new Random();
    private Player player;

    public Enemy(Player player) {
        super(0, 0);
        this.player = player;
        if (random.nextBoolean()) {
            x = random.nextBoolean() ? 0 : WIDTH;
            y = random.nextInt(HEIGHT);
        } else {
            x = random.nextInt(WIDTH);
            y = random.nextBoolean() ? 0 : HEIGHT;
        }
    }

    @Override
    public void update() {
        double angle = Math.atan2(player.getY() - y, player.getX() - x);
        x += 2 * Math.cos(angle);
        y += 2 * Math.sin(angle);
    }

    public static int getSize() {
        return ENEMY_SIZE;
    }
}
