package game.entity;

import game.DebugManager;
import game.effects.GlowEffect;
import game.entity.plants.Plant;
import game.font.Color4f;
import game.gui.ScaleToScreen;
import game.shaders.ShaderManager;
import game.texture.TextureManager;
import game.utils.DebugUtils;

public abstract class Entity {

	public String name;
    protected float x, y; // Координаты
    protected float width, height; // Размеры
    public int hp; // Здоровье
    public int damage; // Урон
    public float speedDamage;
    protected float speed; // Скорость движения
    
    protected Animation animation; 

    protected float hitboxWidth, hitboxHeight;
    
    private GlowEffect glowEffect;

    public Entity(String name, float x, float y, float width, float height, int hp, int damage, float speed, float speedDamage, float hitboxWidth, float hitboxHeight) {
    	this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.damage = damage;
        this.speed = speed;
        this.speedDamage = speedDamage;

        // Инициализация параметров хитбокса
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        
        this.glowEffect = new GlowEffect(0.25f, 1.0f, 1.5f);
    }

    public void takeDamage(int amount) {
        this.hp -= amount;
        if (this.hp <= 0) {
            onDeath();
        }
        glowEffect.startGlow();
    }

    protected abstract void onDeath(); // Обработка смерти

    public void update(float deltaTime) {
        if (this.animation != null) {
            this.animation.update(deltaTime);
        }
        glowEffect.update(deltaTime);
    }

    public void render() {
        // Отрисовка тени
        TextureManager.drawTexture("shadow", ScaleToScreen.getStretchedWidth(x), ScaleToScreen.getStretchedHeight(y - 13), ScaleToScreen.getStretchedWidth(140), ScaleToScreen.getStretchedHeight(50));
        
        if(glowEffect.isGlowing() && glowEffect.getGlowIntensity() > 0.0f) {
            ShaderManager.useShader("glow");
            ShaderManager.setUniform("glowIntensity", glowEffect.getGlowIntensity());
         }
        
        // Отрисовка юнити
        TextureManager.drawTexture(animation.getCurrentFrame(), ScaleToScreen.getStretchedWidth(x - 28), ScaleToScreen.getStretchedHeight(y - 1), ScaleToScreen.getStretchedWidth(width), ScaleToScreen.getStretchedHeight(height));
        
        ShaderManager.resetShader();
        
        if (DebugManager.isDebugMode()) {
            // Добавляем хитбокс в список для отрисовки
            DebugUtils.addHitbox(x, y, hitboxWidth, hitboxHeight, 0, 0, new Color4f(1.0f, 0.0f, 0.0f, 1.0f));
            
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
    
    public boolean checkCollisionWithPlant(Plant plant) {
        return this.isColliding(plant.getX(), plant.getY(), plant.getHitboxWidth(), plant.getHitboxHeight());
    }
    
    public Animation getAnimation() {
        return animation;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public float getAttack() {
        return speedDamage;
    }
    
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
    
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

	public float getHitboxWidth() {
		return hitboxWidth;
	}

	public float getHitboxHeight() {
		return hitboxHeight;
	}

    public String getCurrentTextureName() {
        return animation != null ? animation.getCurrentFrame() : "default";
    }

}