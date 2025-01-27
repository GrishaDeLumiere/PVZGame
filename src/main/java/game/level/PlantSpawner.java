package game.level;

import game.entity.plants.Plant;

public class PlantSpawner<T extends Plant> {

    private Class<T> plantType;

    public PlantSpawner(Class<T> plantType) {
        this.plantType = plantType;
    }

    public T createPlant(float x, float y) {
        try {
            return plantType.getConstructor(float.class, float.class).newInstance(x, y);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}