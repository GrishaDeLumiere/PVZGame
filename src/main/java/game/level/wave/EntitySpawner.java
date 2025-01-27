package game.level.wave;

import game.entity.Entity;

public class EntitySpawner<T extends Entity> {
	
    private Class<T> entityType;

    public EntitySpawner(Class<T> entityType) {
        this.entityType = entityType;
    }

    //метод для создания врага по типу
    public Entity createEntity(float x, float y) {
        try {
            return entityType.getConstructor(float.class, float.class).newInstance(x, y);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}