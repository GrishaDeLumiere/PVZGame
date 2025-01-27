package game.level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import game.DebugManager;
import game.entity.plants.Plant;
import game.gui.ScaleToScreen;

public class LawnGrid {

    public static final int GRID_WIDTH = 9;
    public static final int GRID_HEIGHT = 5;
    public float startX, startY;
    public float cellWidth, cellHeight;
    public static List<Plant> plants;  // Список для хранения всех растений

    public LawnGrid(float startX, float startY, float cellWidth, float cellHeight) {
        this.startX = startX;
        this.startY = startY;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        LawnGrid.plants = new ArrayList<>();  // Инициализация списка растений
    }

    public void renderGrid() {
        GL11.glPushMatrix();
        GL11.glColor4f(0.0f, 0.5f, 0.5f, 1.0f); 
        GL11.glLineWidth(2);  
        GL11.glBegin(GL11.GL_LINES); 

        // Рисуем вертикальные линии
        for (int i = 0; i <= GRID_WIDTH; i++) {
            float x = startX + i * cellWidth;
            GL11.glVertex2f(ScaleToScreen.getStretchedWidth(x), ScaleToScreen.getStretchedHeight(startY)); 
            GL11.glVertex2f(ScaleToScreen.getStretchedWidth(x), ScaleToScreen.getStretchedHeight(startY + GRID_HEIGHT * cellHeight)); 
        }

        // Рисуем горизонтальные линии
        for (int j = 0; j <= GRID_HEIGHT; j++) {
            float y = startY + j * cellHeight;
            GL11.glVertex2f(ScaleToScreen.getStretchedWidth(startX), ScaleToScreen.getStretchedHeight(y)); 
            GL11.glVertex2f(ScaleToScreen.getStretchedWidth(startX + GRID_WIDTH * cellWidth), ScaleToScreen.getStretchedHeight(y)); 
        }

        GL11.glEnd();  
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); 
        GL11.glPopMatrix();
    }

    // Метод для рендеринга всех растений из списка
    public void renderPlants() {
        for (Plant plant : plants) {
                plant.render(); 
        }
    }

    public void update(float deltaTime) {
        // Удаляем сущности с HP <= 0
        removeEntitiesWithZeroHP();
        for (Plant plant : plants) {
            if (plant != null) {
                plant.update(deltaTime);
            }
        }
    }

    public void render() {
        if (DebugManager.isDebugMode()) {
        	//Временно выключено, потом разберусь
            //renderGrid();
        }
        renderPlants();
    }

    public boolean plantAt(float x, float y, Plant selectedPlant) {
        if (selectedPlant == null) {
            return false; // Если нет выбранного растения, ничего не сажаем
        }

        float offsetX = -50.0f; // Смещение по X (можно задать нужное значение)
        float offsetY = -55.0f; // Смещение по Y (можно задать нужное значение)


        // Определяем клетку, в которой хотим посадить
        int gridX = (int) ((x - startX) / cellWidth);
        int gridY = (int) ((y - startY) / cellHeight);

        // Проверяем, можно ли посадить растение в данную клетку
        if (canPlantBePlaced(gridX, gridY)) {
            Plant newPlant = null;
            try {
                // Создаем новое растение через конструктор
                newPlant = selectedPlant.getClass()
                        .getDeclaredConstructor(float.class, float.class)
                        .newInstance(
                            startX + gridX * cellWidth + cellWidth / 2 + offsetX,
                            startY + gridY * cellHeight + cellHeight / 2 + offsetY
                        );
            } catch (Exception e) {
                System.err.println("Ошибка создания растения: " + e.getMessage());
                e.printStackTrace();
            }

            if (newPlant != null) {
                plants.add(newPlant); // Добавляем растение в список
                return true;
            }
        }
        return false;
    }

    // Проверяет, есть ли уже растение в данной клетке
    public boolean isPlantAt(int gridX, int gridY) {
        for (Plant plant : plants) {
            if (plant != null) {
                int plantGridX = (int) ((plant.getX() - startX) / cellWidth);
                int plantGridY = (int) ((plant.getY() - startY) / cellHeight);
                if (plantGridX == gridX && plantGridY == gridY) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isValidCell(int gridX, int gridY) {
        return gridX >= 0 && gridX < GRID_WIDTH && gridY >= 0 && gridY < GRID_HEIGHT;
    }

    // Проверяет, можно ли посадить растение в клетку (клетка пуста)
    public boolean canPlantBePlaced(int gridX, int gridY) {
        return isValidCell(gridX, gridY) && !isPlantAt(gridX, gridY);
    }
    
    // Получение индексов сетки по координатам
    public int[] getGridPosition(float x, float y) {
        // Преобразуем Y с учетом ScaleToScreen
        float adjustedY = ScaleToScreen.get(y);

        int gridX = (int) ((x - startX) / cellWidth);
        int gridY = (int) ((adjustedY - startY) / cellHeight);
        return new int[]{gridX, gridY};
    }

    // Проверка, можно ли посадить растение по экранным координатам
    public boolean canPlacePlantAt(float x, float y) {
        int[] gridPos = getGridPosition(x, y);
        int gridX = gridPos[0];
        int gridY = gridPos[1];
        return canPlantBePlaced(gridX, gridY);
    }

    // Получение центра клетки по индексу (для визуализации)
    public float[] getCellCenter(int gridX, int gridY) {
        float centerX = startX + gridX * cellWidth + cellWidth / 2;
        float centerY = startY + gridY * cellHeight + cellHeight / 2;
        return new float[]{centerX, centerY};
    }
    
    public static List<Plant> getPlants() {
        return plants;
    }
    
    private void removeEntitiesWithZeroHP() {
        // Используем обобщённый Iterator<Plant>
    	
        Iterator<Plant> iterator = plants.iterator();
        while (iterator.hasNext()) {
            Plant entity = iterator.next(); // Здесь мы извлекаем объект типа Plant
            if (entity.hp <= 0) {
                iterator.remove(); // Удаляем сущность из списка
                System.out.println("Entity with x: " + entity.getX() + " and y: " + entity.getY() + " has died.");
            }
        }
    }
    
}