package game.level;

import java.util.List;

import org.lwjgl.opengl.GL11;

import game.DebugManager;
import game.SettingsManager;
import game.WindowManager;
import game.entity.Animation;
import game.entity.AnimationUtils;
import game.font.Color4f;
import game.gui.ScaleToScreen;
import game.texture.TextureManager;
import game.utils.DebugUtils;

public class Sun {

    private float x, y; 
    private float velocity; 
    private float cornerVelocity;
    private boolean clicked; 

    private static final float SUN_SIZE = 175; 
    private Animation animation; 

    private boolean isFalling; 
    private boolean isMovingToCorner;
    private float cornerX, cornerY;
    
    private float targetY; 
    private float timeAlive; 
    public static final float MAX_TIME_ALIVE = 20.0f;
    
    private float hitboxOffsetX = 50;
    private float hitboxOffsetY = 45;
    private float hitboxWidth = 75;
    private float hitboxHeight = 75;
    
    private Level level;
    private int sunAmount;
    
    public Sun(float x, float y, float velocity, Level level, int sunAmount) {
        this.x = x;
        this.y = y;
        this.velocity = velocity;
        this.cornerVelocity = 1000f;
        this.clicked = false;
        this.isFalling = true;
        this.isMovingToCorner = false; 
        this.level = level;
        this.sunAmount = sunAmount;
        
        this.cornerX = 320;
        this.cornerY = 945; 
        
        this.timeAlive = 0;

        this.targetY = 100 + (float) (Math.random() * 500);

        List<String> frames = AnimationUtils.createAnimationFrames("Sun", 1, 13);
        this.animation = new Animation(frames, 0.05f);
    }

    public void update(float deltaTime) {
        if (SettingsManager.isAutoSunSelectionEnabled() && !clicked && !isMovingToCorner) {
            setClicked(true);
        }
        if (isMovingToCorner) {
            float speed = cornerVelocity * deltaTime; 
            float dx = ScaleToScreen.get(cornerX - x);
            float dy = ScaleToScreen.getBot(cornerY - y);
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < speed) {
                x = cornerX;
                y = cornerY;
                isMovingToCorner = false;
                clicked = true;
                level.addSun(sunAmount);
                clicked = true;
            } else {
                x += speed * (dx / distance);
                y += speed * (dy / distance);
            }
        } else if (isFalling) {
            y -= velocity * deltaTime;
            if (y <= targetY) {
                y = targetY; 
                isFalling = false; 
            }
        }

        animation.update(deltaTime);

        if (!clicked) {
            timeAlive += deltaTime;
            if (timeAlive >= MAX_TIME_ALIVE) {
                clicked = true; 
            }
        }
    }

    public void render() {
        if (clicked && !isMovingToCorner) return; 
        
        String currentFrame = animation.getCurrentFrame();

        float timeRemaining = MAX_TIME_ALIVE - timeAlive;
        boolean isFlashing = timeRemaining <= 5.0f; 
        float alpha = 1.0f;

        if (isFlashing) {
            float pulse = (float) Math.sin(timeAlive * 10); 
            alpha = 0.3f + 0.7f * (pulse * 0.5f + 0.5f); 
        }
        TextureManager.drawTexture(currentFrame, ScaleToScreen.getStretchedWidth(x), ScaleToScreen.getStretchedHeight(y), ScaleToScreen.getStretchedWidth(SUN_SIZE), ScaleToScreen.getStretchedHeight(SUN_SIZE), alpha);
        if (DebugManager.isDebugMode() && isMovingToCorner == false) {
            if (DebugManager.isCollisionDebugMode())
            DebugUtils.addHitbox(x, y, hitboxWidth, hitboxHeight, 0, 0, new Color4f(0.0f, 1.0f, 0.0f, 1.0f));
        }
    }

    public boolean isActive() {
        return !clicked || isMovingToCorner; 
    }
    
    public boolean isClicked(float mouseX, float mouseY) {
        return mouseX >= ScaleToScreen.getStretchedWidth(x + hitboxOffsetX) && mouseX <= ScaleToScreen.getStretchedWidth(x + hitboxOffsetX + hitboxWidth) &&
               mouseY >= ScaleToScreen.getStretchedHeight(y + hitboxOffsetY) && mouseY <= ScaleToScreen.getStretchedHeight(y + hitboxOffsetY + hitboxHeight);
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
        if (clicked) {
        	WindowManager.getAL().getSoundEffectManager().playSoundEffect("points"); 
            isMovingToCorner = true;
        }
    }

    public boolean GetisMovingToCorner() {
        return isMovingToCorner;
    }
    
    public float getY() {
        return y;
    }

    public boolean isFalling() {
        return isFalling;
    }

    public float getTimeAlive() {
        return timeAlive;
    }
    
}
