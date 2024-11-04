package pewpew;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private Timer timer;
    private Random random;
    private int score;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        initializeGame();
    }

    private void initializeGame() {
        player = new Player(WIDTH / 2, HEIGHT / 2);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        random = new Random();
        score = 0;

        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw player
        g.setColor(Color.BLUE);
        g.fillRect(player.getX(), player.getY(), Player.getSize(), Player.getSize());

        // Draw enemies
        g.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            g.fillOval(enemy.getX(), enemy.getY(), Enemy.getSize(), Enemy.getSize());
        }

        // Draw bullets with their specific colors
        for (Bullet bullet : bullets) {
            g.setColor(bullet.getColor());
            g.fillOval(bullet.getX(), bullet.getY(), bullet.getSize(), bullet.getSize());
        }

        // Draw score and current weapon
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGameState();
        checkCollisions();
        spawnEnemies();
        repaint();
    }

    private void updateGameState() {
        player.update();

        // Update and remove out-of-bounds bullets
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update();
            if (bullet.getX() < 0 || bullet.getX() > WIDTH ||
                    bullet.getY() < 0 || bullet.getY() > HEIGHT) {
                bulletIt.remove();
            }
        }

        // Update enemies
        enemies.forEach(Enemy::update);
    }

    private void checkCollisions() {
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();

            // Check bullet collisions
            Iterator<Bullet> bulletIt = bullets.iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                if (Math.hypot(bullet.getX() - enemy.getX(),
                        bullet.getY() - enemy.getY()) < Enemy.getSize()) {
                    enemyIt.remove();
                    bulletIt.remove();
                    score += 10;
                    break;
                }
            }

            // Check player collision
            if (Math.hypot(player.getX() - enemy.getX(),
                    player.getY() - enemy.getY()) < (Player.getSize() + Enemy.getSize()) / 2) {
                gameOver();
            }
        }
    }

    private void spawnEnemies() {
        if (random.nextInt(50) == 0) {
            enemies.add(new Enemy(player));
        }
    }

    private void gameOver() {
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Score: " + score);
        System.exit(0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                player.setDY(-5);
                break;
            case KeyEvent.VK_S:
                player.setDY(5);
                break;
            case KeyEvent.VK_A:
                player.setDX(-5);
                break;
            case KeyEvent.VK_D:
                player.setDX(5);
                break;
            case KeyEvent.VK_1:
                player.switchWeapon(0);
                break; // Basic gun
            case KeyEvent.VK_2:
                player.switchWeapon(1);
                break; // Shotgun
            case KeyEvent.VK_3:
                player.switchWeapon(2);
                break; // Rapid fire
            case KeyEvent.VK_SPACE:
                Point mouse = getMousePosition();
                if (mouse != null) {
                    double angle = Math.atan2(mouse.y - player.getY(),
                            mouse.x - player.getX());
                    bullets.addAll(player.shoot(angle));
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_S:
                player.setDY(0);
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
                player.setDX(0);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}