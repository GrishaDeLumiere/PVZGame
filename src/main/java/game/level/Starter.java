package game.level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import game.Localization;
import game.ThreadPoolManager;
import game.WindowManager;
import game.entity.Entity;
import game.font.Color4f;
import game.font.FontManager;
import game.gui.ScaleToScreen;
import game.level.wave.EntitySpawner;
import game.texture.TextureManager;
import java.awt.*;

public class Starter {

    private Level level;
    private Camera camera;

    private boolean isCameraMovingRight = false;
    private boolean isChoosingPlants = false;
    private boolean isEntitiesVisible = false;
    private boolean isReturningCamera = false;

    private List<Entity> entities = new ArrayList<>();

    private float timePassed = 0;
    private float delayDuration = 1.5f;

    public void init() {
        entities.clear();
        WindowManager.getSoundEngine().loadAndPlayBackgroundMusic("music/SelectCard.wav");
        TextureManager.loadTexture("background1", "textures/Level/background1.png");
        camera = new Camera(1920, 1080);
        level = new Level("level.name.test", camera, "background1");

        ThreadPoolManager.getInstance().submitTask(() -> spawnInfoEntities(1));
        isCameraMovingRight = true;
        timePassed = 0;
        camera.moveSmoothly(0, 0);
        textAlpha = 1;
    }

    public void update(float deltaTime) {
        level.update(deltaTime);

        for (Entity entity : entities) {
            entity.update(deltaTime);
        }

        timePassed += deltaTime;
        // camera.moveSmoothly(0.1f, deltaTime);
        if (timePassed >= delayDuration) {
            if (isCameraMovingRight) {
                if (camera.getX() < ScaleToScreen.getStretchedWidth(650)) {
                    camera.moveSmoothly(ScaleToScreen.getStretchedWidth(650), deltaTime);
                } else {
                    isCameraMovingRight = false;
                    isChoosingPlants = true;
                }
            }
        }

        if (isChoosingPlants && isEntitiesVisible) {
            if (!isReturningCamera) {

            }
        }

        if (isReturningCamera) {
            if (camera.getX() > 0.01f) {
                camera.moveSmoothly(0.01f, deltaTime);
            } else {
                if (level.getStartGame() == false) {
                    level.Starting();
                    entities.clear();
                }
                level.setStartGame(true);

            }
        }
    }

    public void endPlantSelection() {
        isReturningCamera = true;
    }

    private static final int GRID_SIZE = 100;

    public void spawnInfoEntities(float multiplier) {
        // Проверяем, чтобы множитель был хотя бы 1
        if (multiplier < 1) {
            multiplier = 1;
        }

        // Получаем карту типов врагов и их шансы
        Map<Class<? extends Entity>, Float> enemyTypes = level.waveSystem.getEnemyTypes();
        float spawnX = 1980;
        float spawnY = 20;
        float width = 420;
        float height = 750;

        // Грид для отслеживания коллизий
        Map<Point, List<Entity>> grid = new HashMap<>();
        List<Entity> spawnedEntities = new ArrayList<>();

        // Проходим по каждому типу врага в карте
        for (Map.Entry<Class<? extends Entity>, Float> entry : enemyTypes.entrySet()) {
            Class<? extends Entity> enemyType = entry.getKey();

            // Умножаем количество врагов на множитель
            int spawnCount = (int) Math.ceil(multiplier);

            for (int i = 0; i < spawnCount; i++) {
                // Генерируем случайные координаты для спавна врага
                float randomX = spawnX + (float) (Math.random() * width);
                float randomY = spawnY + (float) (Math.random() * height);

                boolean collision = false;

                // Проверка на коллизии только при малых множителях
                if (multiplier <= 50 && enemyTypes.size() <= 50) {
                    int gridX = (int) (randomX / GRID_SIZE);
                    int gridY = (int) (randomY / GRID_SIZE);
                    // Проверяем текущую ячейку и соседние
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            Point neighborPoint = new Point(gridX + dx, gridY + dy);
                            List<Entity> nearbyEntities = grid.getOrDefault(neighborPoint, new ArrayList<>());

                            for (Entity entity : nearbyEntities) {
                                if (entity.getClass().equals(enemyType)) {
                                    float distX = Math.abs(entity.getX() - randomX);
                                    float distY = Math.abs(entity.getY() - randomY);
                                    if (distX < 50 && distY < 100) {
                                        collision = true;
                                        break;
                                    }
                                }
                            }

                            if (collision)
                                break;
                        }
                        if (collision)
                            break;
                    }
                }

                // Если нет коллизий или проверки отключены, создаём врага
                if (!collision || multiplier > 50) {
                    // Создаём спавнера для текущего типа врага
                    EntitySpawner<? extends Entity> spawner = new EntitySpawner<>(enemyType);
                    Entity entity = spawner.createEntity(randomX, randomY); // Создаем врага с помощью спавнера

                    // Обновляем грид для этого врага
                    if (multiplier <= 50 && enemyTypes.size() <= 50) {
                        int gridX = (int) (randomX / GRID_SIZE);
                        int gridY = (int) (randomY / GRID_SIZE);
                        Point gridPoint = new Point(gridX, gridY);
                        grid.computeIfAbsent(gridPoint, k -> new ArrayList<>()).add(entity);
                    }

                    spawnedEntities.add(entity);
                }
            }
        }

        // Добавляем всех спауненных врагов в основной список
        entities.addAll(spawnedEntities);
        isEntitiesVisible = true;

        // Логируем количество заспавненных врагов
        System.out.println("Spawned " + spawnedEntities.size() + " entities.");
    }

    private static float textAlpha = 1.0f;

    public void render() {
        camera.apply();
        level.render();
        if (isEntitiesVisible) {
            entities.sort(Comparator.comparingDouble(Entity::getY).reversed());
            for (Entity entity : entities) {
                entity.render();
            }
        }

        // Если игра не началась и текст еще не скрыт
        if (level.getStartGame() == false && textAlpha > 0) {

            if (timePassed >= delayDuration) {
                if (textAlpha > 0) {
                    textAlpha -= 0.001f;
                    if (textAlpha < 0) {
                        textAlpha = 0;

                    }
                }
            }

            // Создаем цвета с учетом текущей альфы
            Color4f textColor = new Color4f("#FFFFFF");
            textColor.setAlpha(textAlpha);

            Color4f shadowColor = new Color4f("#000000");
            shadowColor.setAlpha(textAlpha);

            // Отображаем текст
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            FontManager.getFont("FBUSV8C5EI").drawTextWithShadow(
                    Localization.get(level.levelName),
                    ScaleToScreen.get(50),
                    ScaleToScreen.getCenterX(955),
                    ScaleToScreen.getTop(100),
                    0,
                    textColor,
                    0.1f,
                    0.1f,
                    0,
                    true,
                    0.1f,
                    0.1f,
                    shadowColor);
            GL11.glPopMatrix();
        }
    }

    public Level getLevel() {
        return level;
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean getStartLevel() {
        return isReturningCamera;
    }

}