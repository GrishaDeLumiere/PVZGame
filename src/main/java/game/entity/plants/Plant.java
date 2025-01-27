package game.entity.plants;

import game.DebugManager;
import game.effects.GlowEffect;
import game.entity.Animation;
import game.font.Color4f;
import game.gui.ScaleToScreen;
import game.shaders.ShaderManager;
import game.texture.TextureManager;
import game.utils.DebugUtils;

public abstract class Plant {

    protected float x, y;
    protected float width, height; // Размеры
    
    protected int price;
    public int hp;
    protected int damage;
    protected float reloadTime;  // Время задержки
    protected float timeSinceLastShot;  // Время, прошедшее с последнего выстрела
    protected boolean isReloading = false;  // Статус перезарядки
    private final float plantCooldown;

    protected float hitboxWidth, hitboxHeight;
    
    protected Animation animation; 
    
    private boolean isSelected;
    private GlowEffect glowEffect;

    public Plant(float x, float y, float width, float height, int price, float cooldown, int health, int damage, float reloadTime, float hitboxWidth, float hitboxHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = health;
        this.damage = damage;
        this.reloadTime = reloadTime;
        this.timeSinceLastShot = 0.0f; 
        this.price = price; 
        this.plantCooldown = cooldown;
        // Инициализация параметров хитбокса
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        
        this.glowEffect = new GlowEffect(0.25f, 1.0f, 1.5f);
    }
    
    public void update(float deltaTime) {
        if (this.animation != null) {
            this.animation.update(deltaTime);
        }
        if (isReloading) {
            timeSinceLastShot += deltaTime; 
            if (timeSinceLastShot >= reloadTime) {
                isReloading = false;
                timeSinceLastShot = 0.0f; 
                shoot(); 
            }
        } else {
            timeSinceLastShot += deltaTime;  
            if (timeSinceLastShot >= reloadTime) {  
                shoot(); 
                timeSinceLastShot = 0.0f;  
            }
        }

        glowEffect.update(deltaTime);
    }

    // Абстрактный метод для выстрела, который будет реализован в подклассах
    public abstract void shoot();

    // Метод получения урона
    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            onDeath();
        }
        
        // Включаем эффект свечения при получении урона
        glowEffect.startGlow();
    }

    public void onDeath() {}

	public void render() {
        TextureManager.drawTexture("shadow", ScaleToScreen.getStretchedWidth(x - 20), ScaleToScreen.getStretchedHeight(y - 3), ScaleToScreen.getStretchedWidth(140), ScaleToScreen.getStretchedHeight(50));
        
        if(glowEffect.isGlowing() && glowEffect.getGlowIntensity() > 0.0f) {
            ShaderManager.useShader("glow");
            ShaderManager.setUniform("glowIntensity", glowEffect.getGlowIntensity());
         }

        TextureManager.drawTexture(animation.getCurrentFrame(), ScaleToScreen.getStretchedWidth(x - 16), ScaleToScreen.getStretchedHeight(y - 57), ScaleToScreen.getStretchedWidth(width), ScaleToScreen.getStretchedHeight(height));
        ShaderManager.resetShader();
        
        if (DebugManager.isDebugMode()) {
            // Добавляем хитбокс в список для отрисовки
            DebugUtils.addHitbox(x, y, hitboxWidth, hitboxHeight, 0, 0, new Color4f(1.0f, 0.75f, 0.0f, 1.0f));
            
            // Отрисовываем текст
            DebugUtils.renderText(x, y, "ХП: " + hp, 40, 120, new Color4f("#ffa500"));
            DebugUtils.renderText(x, y, "DM: " + damage, 37, 95, new Color4f("#ffa500"));
        }

    }
    
    public boolean isColliding(float otherX, float otherY, float otherWidth, float otherHeight) {
        return x < otherX + otherWidth && 
               x + hitboxWidth > otherX && 
               y < otherY + otherHeight && 
               y + hitboxHeight > otherY;
    }

    public Animation getAnimation() {
        return animation;
    }
    
    // Геттеры и сеттеры для параметров
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getHealth() {
        return hp;
    }
    
    public int getPrice() {
        return price;
    }

    public int getDamage() {
        return damage;
    }

    public float getReloadTime() {
        return reloadTime;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

	public float getHitboxWidth() {
		return hitboxWidth;
	}

	public float getHitboxHeight() {
		return hitboxHeight;
	}

    public Float getCooldown() {
        return plantCooldown;
    }
    
}
