package game.entity.plants;

import java.util.Arrays;

import game.DebugManager;
import game.WindowManager;
import game.entity.Entity;
import game.entity.EntityZombie;
import game.font.Color4f;
import game.gui.ScaleToScreen;
import game.level.wave.WaveSystem;
import game.texture.TextureManager;
import game.utils.DebugUtils;

public class Projectile {

    private float x, y; // Позиция снаряда
    private float speed;
    private float damage; // Урон снаряда
    private boolean isActive = true; // Статус снаряда

    protected float hitboxWidth, hitboxHeight;

    public Projectile(float x, float y, float damage, float speed, float hitboxWidth, float hitboxHeight) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.speed = speed;

        // Инициализация параметров хитбокса
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
    }

    public void update(float deltaTime) {
        if (isActive) {
            x += speed * deltaTime; // Двигаем снаряд по оси X

            // Проверяем столкновение с каждой сущностью в списке
            for (Entity entity : WaveSystem.getEntities()) {
                if (isColliding(entity.getX(), entity.getY(), entity.getHitboxWidth(), entity.getHitboxHeight())) {
                    handleCollision(entity);
                    if (entity instanceof EntityZombie) {
                        WindowManager.getAL().getSoundEffectManager()
                                .playRandomSoundEffect(Arrays.asList("splat", "splat2", "splat3"));
                    }

                }
            }

            // Делаем снаряд неактивным, если он вылетел за пределы
            if (x > 2000) {
                isActive = false;
            }
        }
    }

    public void render() {
        TextureManager.drawTexture("shadow", ScaleToScreen.getStretchedWidth(x - 5),
                ScaleToScreen.getStretchedHeight(y - 80), ScaleToScreen.getStretchedWidth(50),
                ScaleToScreen.getStretchedHeight(32));

        TextureManager.drawTexture("ProjectilePea", ScaleToScreen.getStretchedWidth(x),
                ScaleToScreen.getStretchedHeight(y), ScaleToScreen.getStretchedWidth(40),
                ScaleToScreen.getStretchedHeight(40));
        if (DebugManager.isDebugMode()) {
            DebugUtils.addHitbox(
                    x, y, hitboxWidth, hitboxHeight,
                    0, 0,
                    new Color4f(0.1f, 0.75f, 0.75f, 1.0f));
        }
    }

    public boolean isColliding(float otherX, float otherY, float otherWidth, float otherHeight) {
        return x < otherX + otherWidth &&
                x + hitboxWidth > otherX &&
                y < otherY + otherHeight &&
                y + hitboxHeight > otherY;
    }

    public void handleCollision(Entity entity) {
        if (entity.isColliding(entity.getX(), entity.getY(), entity.getHitboxWidth(), entity.getHitboxHeight())) {
            entity.takeDamage((int) damage);
            isActive = false;
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public float getY() {
        return x;
    }
}
