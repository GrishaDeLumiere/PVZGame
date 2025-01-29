package game.level;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import game.WindowManager;
import game.entity.plants.Plant;
import game.font.Color4f;
import game.font.FontManager;
import game.gui.ScaleToScreen;
import game.keys.MouseManager;
import game.texture.TextureManager;
import game.utils.AnimatedCards;

public class CardsManager {

    private static final int PLANT_BUTTON_ID_OFFSET = 1000;
    private Starter starter;
    private List<AnimatedCards> animations = new ArrayList<>();

    public CardsManager(Starter starter) {
        this.starter = starter;
    }

    // Текущий список растений в хотбаре
    public void renderSelectedPlants() {
        float startX = ScaleToScreen.getCenterX(448);
        float startY = ScaleToScreen.getBot(952);

        List<Plant> selectedPlants = starter.getLevel().getPlantManager().getSelectedPlants();
        for (int i = 0; i < selectedPlants.size(); i++) {
            Plant plant = selectedPlants.get(i);

            float targetX = startX + i * ScaleToScreen.get(90);
            float targetY = startY;

            // Анимация для перемещения карточки
            AnimatedCards animation = findAnimationForPlant(plant);
            if (animation != null && !animation.isFinished()) {
                targetX = animation.getCurrentX();
                targetY = animation.getCurrentY();
            }

            // ID кнопки для карточки
            int buttonId = PLANT_BUTTON_ID_OFFSET + 100 + i;

            // Рисуем саму карточку растения
            int plantPrice = plant.getPrice();
            if (starter.getLevel().getSunCount() >= plantPrice) {
                drawPlantButton(buttonId, plant, targetX, targetY, ScaleToScreen.get(85), ScaleToScreen.get(117),
                        false);
            } else {
                drawPlantButton(buttonId, plant, targetX, targetY, ScaleToScreen.get(85), ScaleToScreen.get(117), true);
            }

            // Визуальный кулдаун
            renderCooldown(plant, targetX, targetY);
        }
    }

    public void renderCooldown(Plant plant, float targetX, float targetY) {
        PlantManager plantManager = starter.getLevel().getPlantManager();
        float cooldownTime = plantManager.getCooldownTime(plant); // Оставшееся время кулдауна
        float maxCooldown = plant.getCooldown(); // Максимальный кулдаун

        if (cooldownTime > 0) {
            // Рассчитываем процент времени, которое осталось
            float cooldownHeight = (cooldownTime / maxCooldown) * ScaleToScreen.get(117); // Высота полоски кулдауна

            GL11.glPushMatrix(); // Сохраняем текущее состояние матрицы
            GL11.glLoadIdentity(); // Сбрасываем матрицу в исходное состояние
            // Включаем смешивание
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // Рисуем полоску кулдауна (полупрозрачную)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);

            TextureManager.drawTexture("cooldown", targetX, targetY, ScaleToScreen.get(85),
                    ScaleToScreen.get(cooldownHeight));

            // Отключаем смешивание
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glPopMatrix();
        }
    }

    public void renderPlantList(float y) {
        float startX = ScaleToScreen.getCenterX(350);
        float startY = ScaleToScreen.getBot(745) + y;

        List<Plant> availablePlants = starter.getLevel().getPlantManager().getAvailablePlants();
        List<Plant> selectedPlants = starter.getLevel().getPlantManager().getSelectedPlants();
        int plantsPerRow = 9;

        for (int i = 0; i < availablePlants.size(); i++) {
            Plant plant = availablePlants.get(i);

            if (plant == null)
                continue;

            // Вычисляем целевые позиции для растений
            float targetX = startX + (i % plantsPerRow) * ScaleToScreen.get(90);
            float targetY = startY - (i / plantsPerRow) * ScaleToScreen.get(120);

            // Находим анимацию для растения
            AnimatedCards animation = findAnimationForPlant(plant);
            if (animation != null) {
                if (animation.isFinished()) {
                    animations.remove(animation);
                }
            }

            float posX = (animation != null && !animation.isFinished()) ? animation.getCurrentX() : targetX;
            float posY = (animation != null && !animation.isFinished()) ? animation.getCurrentY() : targetY;

            int buttonId = PLANT_BUTTON_ID_OFFSET + i;
            boolean isPlantSelected = selectedPlants.contains(plant);

            // Рисуем растения которые в выборе
            drawPlantButton(buttonId, plant, posX, posY, ScaleToScreen.get(85), ScaleToScreen.get(117), true);

            // рисуем растения во время анимации перемещеиня
            if (animation != null && !animation.isFinished()) {
                drawPlantButton(buttonId, plant, targetX, targetY, ScaleToScreen.get(85), ScaleToScreen.get(117), true);
            }
            // рисуем растения которые можно выбрать
            if (!isPlantSelected)
                drawPlantButton(buttonId, plant, posX, posY, ScaleToScreen.get(85), ScaleToScreen.get(117), false);

            // рисуем растения во время анимации перемещеиня
            if (isPlantSelected && animation != null && !animation.isFinished()) {
                drawPlantButton(buttonId, plant, posX, posY, ScaleToScreen.get(85), ScaleToScreen.get(117), false);
            }
        }
    }

    private Plant selectedPlant = null;

    public void update(float delta) {
        MouseManager mouseManager = WindowManager.getMouseManager();
        mouseManager.processClick(); // Обрабатываем клик

        PlantManager plantManager = starter.getLevel().getPlantManager();
        plantManager.updateCooldowns(delta);

        if (mouseManager.isLeftMousePressed()) {
            float cursorX = (float) mouseManager.getMouseX(); // Получаем позицию мыши по оси X
            float cursorY = (float) mouseManager.getMouseY(); // Получаем позицию мыши по оси Y
            onFieldClick(cursorX, cursorY); // Садим растение на поле
        }

        // Если нажата правая кнопка, сбрасываем выбранное растение
        if (mouseManager.isRightMousePressed()) {
            selectedPlant = null; // Отмена выбора растения
            WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");
            mouseManager.reset();
        }
    }

    public void onFieldClick(float x, float y) {
        if (selectedPlant != null) {
            LawnGrid lawnGrid = starter.getLevel().getLawnGrid();
            PlantManager plantManager = starter.getLevel().getPlantManager();

            // Получаем цену растения и проверяем, достаточно ли солнца
            int plantPrice = selectedPlant.getPrice();
            if (lawnGrid.canPlacePlantAt(x, y) && starter.getLevel().getSunCount() >= plantPrice
                    && plantManager.isCooldownReady(selectedPlant)) {
                // Сажаем растение
                if (lawnGrid.plantAt(x, y, selectedPlant)) {
                    plantManager.resetCooldown(selectedPlant); // Сбрасываем кулдаун
                    starter.getLevel().removeSun(plantPrice); // Уменьшаем солнце
                    selectedPlant = null; // Сбрасываем выбранное растение
                    WindowManager.getAL().getSoundEffectManager().playSoundEffect("plant");
                }
            } else if (!plantManager.isCooldownReady(selectedPlant)) {
                WindowManager.getAL().getSoundEffectManager().playSoundEffect("buzzer"); // Игровой звук ошибки, если кулдаун не
                                                                          // завершен
            }
        }
    }

    public void render() {
        if (selectedPlant != null) {
            MouseManager mouseManager = WindowManager.getMouseManager();
            float cursorX = (float) mouseManager.getMouseX(); // Получаем позицию мыши по оси X
            float cursorY = (float) mouseManager.getMouseY(); // Получаем позицию мыши по оси Y

            // Рисуем растение, следящее за курсором
            TextureManager.drawTexture(
                    selectedPlant.getAnimation().getFrameByIndex(0),
                    cursorX - 65, cursorY - 110,
                    ScaleToScreen.get(165), ScaleToScreen.get(200));

            // Получаем координаты клетки
            LawnGrid lawnGrid = starter.getLevel().getLawnGrid();
            int gridX = (int) ((cursorX - lawnGrid.startX) / lawnGrid.cellWidth);
            int gridY = (int) ((cursorY - lawnGrid.startY) / lawnGrid.cellHeight);

            // Если клетка внутри поля
            if (gridX >= 0 && gridX < LawnGrid.GRID_WIDTH && gridY >= 0 && gridY < LawnGrid.GRID_HEIGHT) {
                // Рисуем растение в клетке, полупрозрачное
                float plantPosX = lawnGrid.startX + gridX * lawnGrid.cellWidth + (lawnGrid.cellWidth / 2) - 145 / 2;
                float plantPosY = lawnGrid.startY + gridY * lawnGrid.cellHeight + (lawnGrid.cellHeight / 2) - 260 / 2;

                GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f); // Полупрозрачный
                TextureManager.drawTexture(
                        selectedPlant.getAnimation().getFrameByIndex(0),
                        ScaleToScreen.getStretchedWidth(plantPosX), ScaleToScreen.getStretchedHeight(plantPosY),
                        ScaleToScreen.getStretchedWidth(165), ScaleToScreen.getStretchedHeight(200));
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); // Возвращаем цвет в обычный
            }
        }
    }

    public void onButtonClick(int id) {
        PlantManager plantManager = starter.getLevel().getPlantManager();

        // Система посадки из хотбара
        if (starter.getLevel().getStartGame()) {
            if (id >= PLANT_BUTTON_ID_OFFSET) {
                int plantIndex = id - PLANT_BUTTON_ID_OFFSET - 100;
                Plant plant = plantManager.getSelectedPlant(plantIndex);

                if (plant != null) {
                    int plantPrice = plant.getPrice();
                    if (starter.getLevel().getSunCount() >= plantPrice) {
                        // Проверяем, находится ли растение в кулдауне
                        if (selectedPlant == plant) {
                            return;
                        }

                        if (!plantManager.isCooldownReady(plant)) {
                            WindowManager.getAL().getSoundEffectManager().playSoundEffect("buzzer"); // Звук кулдауна
                            return; // Выход, чтобы не выделить растение
                        }

                        // Если растение не в кулдауне, продолжаем логику выбора
                        if (selectedPlant == plant) {
                            selectedPlant = null; // Снимаем выбор, если растение уже выбрано
                        } else {
                            WindowManager.getAL().getSoundEffectManager().playSoundEffect("seedlift"); // Звук выбора растения
                            selectedPlant = plant; // Выбираем новое растение
                        }
                    } else {
                        WindowManager.getAL().getSoundEffectManager().playSoundEffect("buzzer");
                    }
                }
            }
        }

        // МЕНЮ ВЫБОРА РАСТЕНИЙ ДО НАЧАЛА ИГРЫ.
        if (starter.getLevel().getStartGame() == false && starter.getStartLevel() == false) {
            if (id >= PLANT_BUTTON_ID_OFFSET && id < PLANT_BUTTON_ID_OFFSET + 100) {
                int plantIndex = id - PLANT_BUTTON_ID_OFFSET;
                List<Plant> availablePlants = plantManager.getAvailablePlants();

                if (plantIndex < availablePlants.size()) {
                    Plant selectedPlant = availablePlants.get(plantIndex);

                    if (selectedPlant != null && !selectedPlant.isSelected()) {
                        WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap");

                        float startX = ScaleToScreen.getCenterX(350) + (plantIndex % 9) * ScaleToScreen.get(90);
                        float startY = ScaleToScreen.getBot(745) - (plantIndex / 9) * ScaleToScreen.get(120);

                        float endX = ScaleToScreen.getCenterX(448)
                                + plantManager.getSelectedPlants().size() * ScaleToScreen.get(90);
                        float endY = ScaleToScreen.getBot(952);

                        addAnimation(selectedPlant, startX, startY, endX, endY);
                        plantManager.addSelectedPlant(selectedPlant);
                        selectedPlant.setSelected(true);
                    }
                }
            } else if (id >= PLANT_BUTTON_ID_OFFSET + 100) {
                WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");

                int plantIndex = id - (PLANT_BUTTON_ID_OFFSET + 100);
                List<Plant> selectedPlants = plantManager.getSelectedPlants();

                if (plantIndex >= 0 && plantIndex < selectedPlants.size()) {
                    Plant removedPlant = selectedPlants.get(plantIndex);

                    // Начальные координаты для анимации удаления
                    float startX = ScaleToScreen.getCenterX(448) + plantIndex * ScaleToScreen.get(90);
                    float startY = ScaleToScreen.getBot(952);

                    // Конечные координаты для восстановления растения в списке
                    int originalIndex = plantManager.getAvailablePlants().indexOf(removedPlant);
                    float endX = ScaleToScreen.getCenterX(350) + (originalIndex % 9) * ScaleToScreen.get(90);
                    float endY = ScaleToScreen.getBot(745) - (originalIndex / 9) * ScaleToScreen.get(120);

                    addAnimation(removedPlant, startX, startY, endX, endY);

                    // Для всех остальных растений сдвигаем их влево
                    for (int i = plantIndex + 1; i < selectedPlants.size(); i++) {
                        Plant nextPlant = selectedPlants.get(i);

                        // Сдвигаем координаты на оси X
                        float targetX = ScaleToScreen.getCenterX(448) + (i - 1) * ScaleToScreen.get(90);
                        float targetY = ScaleToScreen.getBot(952); // Y остаётся неизменным

                        // Добавляем анимацию сдвига
                        addAnimation(nextPlant, ScaleToScreen.getCenterX(448) + i * ScaleToScreen.get(90),
                                ScaleToScreen.getBot(952), targetX, targetY);
                    }

                    plantManager.removeSelectedPlant(plantIndex);
                    removedPlant.setSelected(false);
                }
            }
        }
    }

    private void addAnimation(Plant plant, float startX, float startY, float endX, float endY) {
        animations.add(new AnimatedCards(plant, startX, startY, endX, endY, 100)); // 100ms анимация
    }

    private AnimatedCards findAnimationForPlant(Plant plant) {
        return animations.stream().filter(a -> a.getObject() == plant).findFirst().orElse(null);
    }

    public void drawPlantButton(int id, Plant plant, float x, float y, float width, float height,
            boolean isPlantSelected) {
        MouseManager mouseManager = WindowManager.getMouseManager();

        // Логика определения состояния кнопки (нормальная, наведённая, нажатая)
        boolean isHovered = mouseManager.isHovered(x, y, width, height);
        boolean isPressed = mouseManager.isLeftMousePressed() && isHovered;

        // Если растение выбрано, делаем его неактивным
        if (isPlantSelected) {
            if (isPressed && starter.getStartLevel() == true) {
                GL11.glColor4f(1.0f, 0.25f, 0.25f, 1.0f); // Серый цвет при нажатии
            } else {
                GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f); // Темный цвет, чтобы показать, что оно уже выбрано
            }
        } else {
            // Выбор цвета кнопки в зависимости от её состояния
            if (isPressed) {
                GL11.glColor4f(0.8f, 0.8f, 0.8f, 1.0f); // Серый цвет при нажатии
            } else if (isHovered) {
                GL11.glColor4f(1.0f, 1.0f, 0.8f, 1.0f); // Светло-жёлтый цвет при наведении
            } else {
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); // Белый цвет по умолчанию
            }
        }

        // Рисуем фон кнопки
        TextureManager.drawTexture("SeedPacket_Larger", x, y, width, height);

        // Рисуем изображение растения на кнопке
        TextureManager.drawTexture(
                plant.getAnimation().getFrameByIndex(0),
                x + ScaleToScreen.get(5), y + ScaleToScreen.get(5), ScaleToScreen.get(90), ScaleToScreen.get(100));

        // Если растение выбрано, добавляем затемнение
        if (isPlantSelected) {
            GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f); // Полупрозрачный черный фильтр
            TextureManager.drawTexture("SeedPacket_Larger", x, y, width, height);
        }

        // Сбрасываем цвет к белому, чтобы не повлиять на другие элементы
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // Рисуем цену растения на кнопке
        FontManager.getFont("FBUSV8C5EI").drawText(
                "" + plant.getPrice(),
                (int) ScaleToScreen.get(20),
                x + ScaleToScreen.get(36), y + ScaleToScreen.get(26),
                0,
                new Color4f("#000000"),
                0.1f,
                0.1f,
                0,
                true);

        // Проверяем нажатие на кнопку
        if (mouseManager.isLeftClicked(x, y, width, height)) {
            onButtonClick(id); // Обрабатываем клик по кнопке
            mouseManager.reset(); // Сбрасываем состояние после клика
        }
    }

    private boolean[] isKeyPressedArray = new boolean[9]; // Флаги для клавиш 1-9

    public void input(boolean isKeyPressed) {
        if (starter.getStartLevel()) {
            for (int i = 0; i < 9; i++) {
                int key = GLFW_KEY_1 + i; // Клавиши 1-9
                if (glfwGetKey(WindowManager.getWindowHandle(), key) == GLFW_PRESS && !isKeyPressedArray[i]) {
                    // Вызываем метод выбора растения
                    onButtonClick(PLANT_BUTTON_ID_OFFSET + i + 100);
                    isKeyPressedArray[i] = true; // Устанавливаем флаг для текущей клавиши
                } else if (glfwGetKey(WindowManager.getWindowHandle(), key) == GLFW_RELEASE) {
                    isKeyPressedArray[i] = false; // Сбрасываем флаг для текущей клавиши
                }
            }
        }
    }

}