package pewpew;

import java.util.ArrayList;

public class Player extends GameObject {
    private int dx, dy;
    private static final int PLAYER_SIZE = 30;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private Weapon currentWeapon;
    private ArrayList<Weapon> weapons;

    public Player(int x, int y) {
        super(x, y);
        dx = dy = 0;
        weapons = new ArrayList<>();
        weapons.add(new BasicGun());
        weapons.add(new Shotgun());
        weapons.add(new RapidFire());
        currentWeapon = weapons.get(0);
    }

    public ArrayList<Bullet> shoot(double angle) {
        return currentWeapon.shoot(x + PLAYER_SIZE / 2, y + PLAYER_SIZE / 2, angle);
    }

    public void switchWeapon(int index) {
        if (index >= 0 && index < weapons.size()) {
            currentWeapon = weapons.get(index);
        }
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
        x = Math.max(0, Math.min(x, WIDTH - PLAYER_SIZE));
        y = Math.max(0, Math.min(y, HEIGHT - PLAYER_SIZE));
    }

    public void setDX(int dx) {
        this.dx = dx;
    }

    public void setDY(int dy) {
        this.dy = dy;
    }

    public static int getSize() {
        return PLAYER_SIZE;
    }
}
