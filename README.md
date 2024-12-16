# **Final Project Object-Oriented Programming (N)**

<div style="text-align: center; margin: 0 auto; width: fit-content;">
  <img src="./assets/wmcb.webp" style="width: 25vw"><br>
  <b style="font-size: 2.5vw">When Mother Nature Claps Back (WMNCB)</b>
</div>


| Name | Student ID |
|--|--|
| Faiz Muhammad Kautsar | 5054231013 |
| Shalahuddin Ahmad Cahyoga | 5054231014 |


To visualize the class diagram of our project, here's an ELK-rendered mermaid graph of our class diagram:  

![Class Diagram Rendered](./assets/rendered.png)

Here's the source:  

```mermaid
classDiagram
    %% Core Game Structure
    Game *-- GamePanel
    GamePanel *-- Player
    GamePanel o-- Enemy
    GamePanel o-- Bullet
    GamePanel o-- HealthPickup
    GamePanel o-- ObjectivePickup
    GamePanel o-- WeaponPickup

    %% GameObject Inheritance
    GameObject <|-- Player
    GameObject <|-- Enemy
    GameObject <|-- Bullet
    GameObject <|-- WeaponPickup
    GameObject <|-- HealthPickup
    GameObject <|-- ObjectivePickup

    %% Weapon System
    Player *-- Weapon
    Player o-- Bullet
    Weapon <|-- BasicGun
    Weapon <|-- Shotgun
    Weapon <|-- RapidFire
    Weapon ..> Bullet
    WeaponPickup *-- Weapon

    %% Bullet System
    BasicBullet --|> Bullet
    ShotgunPellet --|> Bullet
    LightBullet --|> Bullet

    %% Enemy Relationship
    Enemy o-- Player

    class GameObject {
        <<abstract>>
        #int x
        #int y
        +GameObject(int x, int y)
        +abstract update()
        +getX()
        +getY()
    }

    class Game {
        +main(String[] args)
    }
    
    class GamePanel {
        -Player player
        -ArrayList<Enemy> enemies
        -ArrayList<Bullet> bullets
        -ArrayList<HealthPickup> healthPickups
        -ArrayList<ObjectivePickup> objectivePickups
        -ArrayList<WeaponPickup> weaponPickups
        -Timer timer
        -int score
        +actionPerformed(ActionEvent e)
        +paintComponent(Graphics g)
        -updateGameState()
        -checkCollisions()
        -spawnEnemies()
    }

    class Player {
        -int dx, dy
        -int currentWeaponIndex
        -int health
        -boolean isInvulnerable
        -ArrayList<Weapon> inventory
        -Weapon currentWeapon
        -AnimationState currentState
        -BufferedImage[] sprites
        +Player(int x, int y)
        +takeDamage(int amount)
        +heal(int amount)
        +pickupWeapon(Weapon newWeapon)
        +switchWeapon(int index)
        +shoot(int gunTipX, int gunTipY, double angle)
        +update()
        +render(Graphics2D g)
    }

    class Enemy {
        -static int ENEMY_SIZE
        -static Random random
        -Player player
        +Enemy(Player player)
        +update()
        +static int getSize()
        +int getCenterX()
        +int getCenterY()
    }

    class Weapon {
        <<abstract>>
        #int fireRate
        #long lastShotTime
        #BufferedImage sprite
        #int displayWidth
        #int displayHeight
        +Weapon(int fireRate, String spritePath, int w, int h)
        +boolean canShoot()
        +BufferedImage getSprite()
        +int getDisplayWidth()
        +int getDisplayHeight()
        +abstract ArrayList<Bullet> shoot(int x, int y, double angle)
    }

    class BasicGun {
        +BasicGun()
        +ArrayList<Bullet> shoot(int x, int y, double angle)
    }

    class RapidFire {
        +RapidFire()
        +ArrayList<Bullet> shoot(int x, int y, double angle)
    }

    class Shotgun {
        -static int PELLET_COUNT
        -static double SPREAD
        +Shotgun()
        +ArrayList<Bullet> shoot(int x, int y, double angle)
    }

    class Bullet {
        <<abstract>>
        #double dx, dy
        #int damage
        #int speed
        #int size
        #Color color
        #BufferedImage sprite
        +Bullet(int x, int y, double angle, int speed, int damage, int size, String spritePath)
        +update()
        +render(Graphics g)
        +getDamage()
        +getSize()
    }

    class BasicBullet {
        +BasicBullet(int x, int y, double angle)
    }

    class ShotgunPellet {
        +ShotgunPellet(int x, int y, double angle)
    }

    class LightBullet {
        +LightBullet(int x, int y, double angle)
    }

    class WeaponPickup {
        -float rotation
        -float floatOffset
        -float fadeAlpha
        -float glowAlpha
        -Weapon weapon
        -long spawnTime
        +WeaponPickup(int x, int y, Weapon weapon)
        +update()
        +render(Graphics g)
        +boolean shouldDespawn()
        +boolean collidesWith(Player player)
        +Weapon getWeapon()
    }

    class HealthPickup {
        -float rotation
        -float floatOffset
        -float fadeAlpha
        -float glowAlpha
        -long spawnTime
        +HealthPickup(int x, int y)
        +update()
        +render(Graphics g)
        +boolean shouldDespawn()
        +boolean collidesWith(Player player)
        +int getHealAmount()
    }

    class ObjectivePickup {
        -float rotation
        -float floatOffset
        -float fadeAlpha
        -float glowAlpha
        -long spawnTime
        +ObjectivePickup(int x, int y)
        +update()
        +render(Graphics g)
        +boolean shouldDespawn()
        +boolean collidesWith(Player player)
        +int getScoreValue()
    }

    class AnimationState {
        <<enumeration>>
        IDLE
        WALKING_LEFT
        WALKING_RIGHT
        WALKING_UP
        WALKING_DOWN
    }

    Player -- AnimationState
```

A breakdown of some of the OOP principles we applied (non-exhaustive, too many to list lmao):

1. **Inheritance (IS-A Relationship)**
- GameObject <- Player, Enemy, Bullet, WeaponPickup, HealthPickup, ObjectivePickup
- Weapon <- BasicGun, Shotgun, RapidFire
- Bullet <- BasicBullet, ShotgunPellet, LightBullet

2. **Composition (HAS-A Relationship with strong coupling)**
- Game *-- GamePanel (Game has a GamePanel)
- GamePanel *-- Player (GamePanel has a Player)
- Player *-- Weapon (Player has Weapons)
- WeaponPickup *-- Weapon (WeaponPickup has a Weapon)

3. **Aggregation (HAS-A Relationship with loose coupling)**
- GamePanel o-- Enemy, Bullet, HealthPickup, ObjectivePickup, WeaponPickup
- Player o-- Bullet
- Enemy o-- Player

4. **Encapsulation (Information Hiding)**
- Private fields (-):
  - GamePanel: timer, score
  - Player: dx, dy, health, isInvulnerable
  - WeaponPickup: rotation, floatOffset, fadeAlpha
  
- Protected fields (#):
  - GameObject: x, y
  - Weapon: fireRate, lastShotTime
  - Bullet: dx, dy, damage, speed
  
- Public methods (+):
  - GameObject: getX(), getY()
  - Player: takeDamage(), heal(), pickupWeapon()
  - Weapon: canShoot(), getSprite()

5. **Abstraction**

- Abstract classes:
  - GameObject (abstract update())
  - Weapon (abstract shoot())
  - Bullet

- Interfaces/Common behaviors:
  - All pickups have: update(), render(), shouldDespawn(), collidesWith()

6. **Polymorphism**

- Method Overriding:
  - Different shoot() implementations in BasicGun, Shotgun, RapidFire
  - Different update() implementations in Player, Enemy, Bullet
  
- Interface Implementation:
  - All GameObjects implement update()
  - All Weapons implement shoot()
