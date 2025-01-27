package game.entity;

import java.util.List;

import game.WindowManager;
import game.entity.plants.Plant;
import game.level.LawnGrid;

public class EntityZombie extends Entity {

    private Animation walkAnimation;
    private Animation idleAnimation; 
    private Animation attackAnimation; 
    private boolean isWalking = false; // Состояние зомби: двигается ли
    private boolean isAttacking = false;
    
    private float stepTimer = 0.0f; // Таймер для отслеживания времени между шагами
    private float attackTimer = 0.0f;
    private boolean isStepping = false; // Флаг для отслеживания, идет ли шаг
    private float distanceToMove = 0.0f; // Расстояние, которое должно пройти зомби в одном шаге

    public EntityZombie(float x, float y) {
        super("zombie", x, y,   // имя, Позиция: x и y координаты
              150,     // Ширина объекта
              200,     // Высота объекта
              190,      // Здоровье объекта
              50,      // Урон объекта
              getRandomSpeed(),// Скорость объекта
              65,      // Скорость нанесения урона
              100,     // Ширина хитбокса
              160);    // Высота хитбокса

        List<String> walkFrames = AnimationUtils.createAnimationFrames("Zombie", 45, 135);
        List<String> idleFrames = AnimationUtils.createAnimationFrames("Zombie", 1, 44);
        List<String> attackFrames = AnimationUtils.createAnimationFrames("Zombie", 139, 178);

        this.walkAnimation = new Animation(walkFrames, Animation.calculateFrameDuration(getSpeed()));
        this.idleAnimation = new Animation(idleFrames, Animation.calculateFrameDuration(getSpeed()));
        this.attackAnimation = new Animation(attackFrames, Animation.calculateFrameDuration(getAttack()));

        this.animation = idleAnimation; 
    }
    
    private static float getRandomSpeed() {
        return 34f + (float) Math.random() * (44f - 34f);
    }

	@Override
    protected void onDeath() {
		this.hp = 0;
    }

	@Override
	public void update(float deltaTime) {
	    super.update(deltaTime);
	    // Если зомби атакует
	    if (isAttacking) {
	        attackTimer += deltaTime;  // Увеличиваем таймер атаки

	        // Проверяем, прошло ли время для атаки (зависит от скорости)
	        if (attackTimer >= Animation.calculateFrameDuration(getAttack() / 20)) {
	            attackTimer = 0.0f;  // Сбрасываем таймер
	            boolean plantFoundAndDamaged = false;
	            if (!plantFoundAndDamaged) {
	                isAttacking = false; // Прекращаем атаку
	                startWalking();      // Возвращаемся к ходьбе
	            }
	            // Проверяем столкновение с растениями
	            for (Plant plant : LawnGrid.getPlants()) {
	                if (checkCollisionWithPlant(plant) && plant.getHealth() > 0) {
	                    // Если зомби по-прежнему сталкивается с растением
	                	WindowManager.getSoundEngine().playSoundEffect("chomp");
	                    attackPlant(plant);
	                    plantFoundAndDamaged = true;
	                    break; // Останавливаемся на первом найденном растении
	                }
	            }
	            if (!plantFoundAndDamaged) {
	                isAttacking = false; // Прекращаем атаку
	                startWalking();      // Возвращаемся к ходьбе
	            }
	        }

	        this.animation = attackAnimation;  // Анимация атаки
	    } else if (isWalking) {
	        if (isStepping) {
	            move(deltaTime);
	            this.animation = walkAnimation;

	            Plant targetPlant = null;

	            // Проверяем столкновение с растениями
	            for (Plant plant : LawnGrid.getPlants()) {
	                if (checkCollisionWithPlant(plant) && plant.getHealth() > 0) {
	                    targetPlant = plant;
	                    break; // Останавливаемся на первом найденном растении
	                }
	            }

	            if (targetPlant != null) {
	                stopWalking();
	                startAttacking(targetPlant);
	            }
	        } else {
	            stepTimer += deltaTime;  // Увеличиваем таймер шага
	            if (stepTimer >= Animation.calculateFrameDuration(getSpeed())) {
	                isStepping = true;
	                stepTimer = 0.0f;  // Сбрасываем таймер
	            }
	        }
	    } else {
	        this.animation = idleAnimation;
	    }
	}

	private void attackPlant(Plant plant) {
	    if (plant.getHealth() > 0) {
	        plant.takeDamage(this.damage);
	    }

	    if (plant.getHealth() <= 0) {
	    	WindowManager.getSoundEngine().playSoundEffect("gulp"); 
	        
	        plant.onDeath();  // Убираем растение с поля
	        isAttacking = false;  // Прекращаем атаку
	    } else {
	        isAttacking = false;
	        startWalking();  // Возвращаемся к ходьбе, если растения уже нет
	    }
	}

	private void startAttacking(Plant plant) {
	    // Начинаем атаку, когда зомби находит растение для атаки
	    isAttacking = true;
	    this.animation = attackAnimation;
	}

	private void move(float deltaTime) {
	    if (distanceToMove < getSpeed()) {
	        float moveAmount = getSpeed() * deltaTime;
	        this.x -= moveAmount;
	        distanceToMove += moveAmount;
	    } else {
	        distanceToMove = 0;
	        isStepping = false;
	    }
	}

	public void startWalking() {
	    this.isWalking = true;
	    isStepping = true;
	    this.animation = walkAnimation;
	}

	public void stopWalking() {
	    this.isWalking = false;
	    this.animation = idleAnimation;
	}
}