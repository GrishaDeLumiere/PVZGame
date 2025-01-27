package game.level;

import java.util.*;

import game.entity.plants.PeaShooterSingle;
import game.entity.plants.Plant;

public class PlantManager {

    private List<Plant> availablePlants = new ArrayList<>(); // Все доступные растения
    private List<Plant> selectedPlants = new ArrayList<>();  // Растения в хотбаре
    private Map<Plant, Integer> plantOriginalPositions = new HashMap<>();
    private Map<Plant, Float> plantCooldowns = new HashMap<>(); // Карта для отслеживания кулдаунов

    // Инициализация доступных растений
    public void initializeAvailablePlants() {
        availablePlants.add(new PeaShooterSingle(1, 1));
        for (int i = 0; i < availablePlants.size(); i++) {
            plantOriginalPositions.put(availablePlants.get(i), i);
        }
    }

    public void addSelectedPlant(Plant plant) {
        if (selectedPlants.size() < 14 && availablePlants.contains(plant) && !plant.isSelected()) {
            selectedPlants.add(plant);

            // Сохраняем индекс и помечаем растение как выбранное
            int originalIndex = availablePlants.indexOf(plant);
            plantOriginalPositions.put(plant, originalIndex);
            plant.setSelected(true);

            // Инициализируем кулдаун для нового растения
            plantCooldowns.put(plant, 0f);  // Таймер кулдауна = 0
        }
    }

    public void removeSelectedPlant(int index) {
        if (index >= 0 && index < selectedPlants.size()) {
            Plant plantToRemove = selectedPlants.get(index);
            selectedPlants.remove(index);

            plantToRemove.setSelected(false);
            plantCooldowns.remove(plantToRemove);  // Убираем кулдаун для удаленного растения
        }
    }

    // Обновление кулдаунов для всех выбранных растений
    public void updateCooldowns(float deltaTime) {
        for (Map.Entry<Plant, Float> entry : plantCooldowns.entrySet()) {
            Plant plant = entry.getKey();
            float currentCooldown = entry.getValue();
            
            // Если кулдаун еще не истек, уменьшаем его на прошедшее время
            if (currentCooldown > 0) {
                plantCooldowns.put(plant, currentCooldown - deltaTime);
            }
        }
    }

    
    // Проверка, готово ли растение к действию (кулдаун завершен)
    public boolean isCooldownReady(Plant plant) {
        return plantCooldowns.getOrDefault(plant, 0f) <= 0f;
    }

    public float getCooldownTime(Plant plant) {
        return plantCooldowns.getOrDefault(plant, 0f);  // Возвращаем оставшееся время кулдауна для растения
    }

    // Сброс кулдауна для растения
    public void resetCooldown(Plant plant) {
        plantCooldowns.put(plant, plant.getCooldown());
    }

    public List<Plant> getAvailablePlants() {
        return availablePlants;
    }

    public List<Plant> getSelectedPlants() {
        return selectedPlants;
    }

    public Plant getSelectedPlant(int index) {
        if (index >= 0 && index < selectedPlants.size()) { // Проверяем, что индекс валиден
            return selectedPlants.get(index);
        }
        return null;
    }
}