package game.entity.plants;

import java.util.List;

import game.WindowManager;
import game.entity.Animation;
import game.entity.AnimationUtils;
import game.entity.Entity;
import game.level.Level;
import game.level.wave.WaveSystem;

public class PeaShooterSingle extends Plant {

	private Animation idleAnimation; 
	
    public PeaShooterSingle(float x, float y) {
        super(x, y,              // Позиция: x и y координаты
              161,               // Ширина объекта
              200,               // Высота объекта
              100,               // цена растения
              7.5f,           // cooldown
              300 ,               // Здоровье объекта
              20,                // Урон объекта
              2,                // Время перезарядки
              80,               // Ширина хитбокса
              120);              // Высота хитбокса
        List<String> idleFrames = AnimationUtils.createAnimationFrames("PeaShooterSingle", 1, 25);

        this.idleAnimation = new Animation(idleFrames, Animation.calculateFrameDuration(40));

        this.animation = idleAnimation; 
    }

	@Override
	public void shoot() {
	    Entity target = findEnemyInRange(2000);
	    if (target != null) {
	    	WindowManager.getAL().getSoundEffectManager().playSoundEffect("throw");
			Level.addProjectile(new Projectile(this.x + 60, this.y + 77, damage, 650, 10, 10));
	     }
	}
	
    private Entity findEnemyInRange(float range) {
        for (Entity entity : WaveSystem.getEntities()) {
            if (isLineIntersectingWithHitbox(entity, range)) {
                return entity; 
            }
        }
        return null;
    }

    private boolean isLineIntersectingWithHitbox(Entity entity, float range) {
        // Линия проходит от x до x + range (по оси X)
        float lineStartX = this.x;         // Начало линии
        float lineEndX = this.x + range;   // Конец линии

        // Получаем хитбокс сущности
        float entityX = entity.getX();          // Позиция сущности по X
        float entityY = entity.getY();          // Позиция сущности по Y
        float entityWidth = entity.getHitboxWidth();  // Ширина хитбокса сущности
        float entityHeight = entity.getHitboxHeight(); // Высота хитбокса сущности

        // Проверяем, пересекает ли линия с хитбоксом сущности по оси X и Y
        return (lineStartX < entityX + entityWidth && lineEndX > entityX &&
                entityY < this.y + this.hitboxHeight && entityY + entityHeight > this.y);
    }

	@Override
	public void render() {
		super.render();
	}
	
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

}
