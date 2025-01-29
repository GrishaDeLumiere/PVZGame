package game.entity;

import java.util.List;

import game.DebugManager;
import game.WindowManager;
import game.font.Color4f;
import game.gui.ScaleToScreen;
import game.texture.TextureManager;
import game.utils.DebugUtils;

public class LawnMower {
	
    private float x, y;
    private float width = 120, height = 100;
    private boolean isActive = false;
    private float speed = 400;
    private float targetX;
    
    protected Animation animation;
    private Animation movenimation;
    private Animation idleAnimation; 
    
    private float hitboxOffsetX = 50;
    private float hitboxOffsetY = 45;
    private float hitboxWidth = 75;
    private float hitboxHeight = 75;

    public LawnMower(float x, float y, float targetX) {
        this.x = x;
        this.y = y;
        this.targetX = targetX;
        
        List<String> moveFrames = AnimationUtils.createAnimationFrames("LawnMower", 1, 17);
        List<String> idleFrames = AnimationUtils.createAnimationFrames("LawnMower", 1, 1);
        this.movenimation = new Animation(moveFrames, Animation.calculateFrameDuration(speed));
        this.idleAnimation = new Animation(idleFrames, Animation.calculateFrameDuration(speed));

        this.animation = idleAnimation; 
    }

    public void activate() {
        this.isActive = true;
        this.animation = movenimation;
    }

    public void update(float deltaTime) {
        if (this.animation != null) {
            this.animation.update(deltaTime);
        }

        if (isActive) {
            moveToTargetX(deltaTime);
        }
    }

    // Метод для движения к targetX
    private void moveToTargetX(float deltaTime) {
        if (!hasReachedTargetX()) {
            if (x < targetX) {
                x += speed * deltaTime;
                if (x > targetX) { 
                    x = targetX;
                }
            } else {
                x -= speed * deltaTime;
                if (x < targetX) {
                    x = targetX;
                }
            }
        } else {
            deactivate(); 
        }
    }

    public boolean hasReachedTargetX() {
        float epsilon = 0.1f;
        return Math.abs(x - targetX) <= epsilon;
    }


	public boolean isActive() {
		return isActive;
	}
    
    private void deactivate() {
        this.isActive = false;
    }


    public void checkCollisionWithEntity(Entity entity) {
        // Если газонокосилка не активна и есть столкновение, активируем её
        if (!isActive && isCollidingWithEntity(entity)) {
        	WindowManager.getAL().getSoundEffectManager().playSoundEffect("lawnmower");
            activate();
        }
        
        // Если газонокосилка активна и есть столкновение, убиваем юнит
        if (isActive && isCollidingWithEntity(entity)) {
            entity.onDeath();  // Уничтожаем юнита
        }
    }
    
    public boolean isCollidingWithEntity(Entity entity) {
        float hitboxX = x + hitboxOffsetX;
        float hitboxY = y + hitboxOffsetY;
        return entity.isColliding(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    public void render() {
        TextureManager.drawTexture("shadow", ScaleToScreen.getStretchedWidth(x + 29), ScaleToScreen.getStretchedHeight(y + 32), ScaleToScreen.getStretchedWidth(140), ScaleToScreen.getStretchedHeight(85));
        
        TextureManager.drawTexture(animation.getCurrentFrame(), ScaleToScreen.getStretchedWidth(x + 28), ScaleToScreen.getStretchedHeight(y + 47), ScaleToScreen.getStretchedWidth(width), ScaleToScreen.getStretchedHeight(height));
        if (DebugManager.isDebugMode()) {
            // Добавляем хитбокс в список для отрисовки
            DebugUtils.addHitbox(x, y, hitboxWidth, hitboxHeight, hitboxOffsetX, hitboxOffsetY, new Color4f(0.0f, 0.0f, 1.0f, 1.0f));
        }
    }

}