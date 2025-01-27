package game.level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import game.Localization;
import game.ThreadPoolManager;
import game.WindowManager;
import game.entity.Entity;
import game.entity.EntityZombie;
import game.entity.LawnMower;
import game.entity.plants.Projectile;
import game.gui.ScaleToScreen;
import game.level.wave.WaveSystem;
import game.level.wave.ZombieLaneManager;
import game.texture.TextureManager;
import game.utils.TextAnimation;

public class Level {

    private boolean isStartingGame = false;
    private Camera camera;
    private String backgroundTexture;

    private int sunCount;
    private ArrayList<Sun> suns = new ArrayList<>();
    private float spawnTimer;
    private static final float SPAWN_TIME = 10.0f;

    private float waveDelay;
    private static final float WAVE_START_DELAY = 16.0f; // 16 секунд

    private ArrayList<LawnMower> lawnMowers = new ArrayList<>();

    private PlantManager plantManager = new PlantManager();
    private LawnGrid lawnGrid;
    private static List<Projectile> projectiles = new ArrayList<>();

    private float gridStartX = 480; // Начальная позиция поля по оси X
    private float gridStartY = 30; // Начальная позиция поля по оси Y
    private float cellWidth = 150; // Ширина клетки
    private float cellHeight = 180; // Высота клетки

    public String levelName;
    public WaveSystem waveSystem = new WaveSystem(this, 25, new ZombieLaneManager(5, 183f, 2000f, 70f));

    private TextAnimation textAnimation;

    public Level(String levelname, Camera camera, String backgroundTexture) {
        this.levelName = levelname;
        textAnimation = new TextAnimation();
        projectiles.clear();
        waveSystem.addEnemyType(EntityZombie.class, 0.95f);

        this.camera = camera;
        this.backgroundTexture = backgroundTexture;
        this.sunCount = 1000;
        this.spawnTimer = SPAWN_TIME;

        this.waveDelay = WAVE_START_DELAY;

        plantManager.initializeAvailablePlants();
        initializeLawnMowers();
        lawnGrid = new LawnGrid(gridStartX, gridStartY, cellWidth, cellHeight);
    }

    private void initializeLawnMowers() {
        float startY = 20;
        float endY = 740;
        int numberOfLawnMowers = 5;
        float stepY = (endY - startY) / (numberOfLawnMowers - 1);
        for (int i = 0; i < numberOfLawnMowers; i++) {
            float y = startY + i * stepY;
            lawnMowers.add(new LawnMower(345, y, 2000));
        }
    }

    public void update(float deltaTime) {
        // Обновление камеры
        camera.update(deltaTime);

        // Обновление сетки (тут не требуется)
        lawnGrid.update(deltaTime);

        // Параллельное обновление волны зомби (отедльно внутри обработка)
        updateWaveSystem(deltaTime);

        // Параллельное обновление солнц
        ThreadPoolManager.getInstance().submitTask(() -> updateSuns(deltaTime));

        // Параллельное обновление снарядов
        ThreadPoolManager.getInstance().submitTask(() -> updateProjectiles(deltaTime));

        // Параллельное обновление газонокосилок
        ThreadPoolManager.getInstance().submitTask(() -> updateLawnMowers(deltaTime));

        // Дополнительная логика, которая не требует многозадачности
        if (isStartingGame) {
            spawnTimer -= deltaTime;
            if (spawnTimer <= 0 && !(waveSystem.getLevelCompleted() == true)) {
                spawnSun();
                spawnTimer = SPAWN_TIME;
            }

            if (waveDelay > 0) {
                waveDelay -= deltaTime;
                return;
            }
        }
    }

    public void updateSuns(float deltaTime) {
        // Синхронизация на коллекции солнц, чтобы избежать ошибок с модификацией списка
        // во время итерации
        synchronized (suns) {
            Iterator<Sun> iterator = suns.iterator();
            while (iterator.hasNext()) {
                Sun sun = iterator.next();
                sun.update(deltaTime);
                if (sun.getTimeAlive() >= Sun.MAX_TIME_ALIVE) {
                    iterator.remove(); // Удаляем солнце, если оно слишком долго в игре
                }
            }
        }
    }

    public void updateProjectiles(float deltaTime) {
        synchronized (projectiles) {
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile pj = projectiles.get(i);
                pj.update(deltaTime);
                if (!pj.isActive()) {
                    projectiles.remove(i); // Удаляем неактивные снаряды
                }
            }
        }
    }

    private void updateLawnMowers(float deltaTime) {
        // Обновление газонокосилок в отдельном потоке
        Iterator<LawnMower> mowerIterator = lawnMowers.iterator();
        while (mowerIterator.hasNext()) {
            LawnMower mower = mowerIterator.next();
            mower.update(deltaTime); // Обновление газонокосилки

            // Обработка столкновений газонокосилки с зомби
            for (Entity zombie : WaveSystem.getEntities()) {
                mower.checkCollisionWithEntity(zombie);
            }

            // Удаление газонокосилки, если она достигла цели
            if (mower.hasReachedTargetX()) {
                mowerIterator.remove();
            }
        }
    }

    public void updateWaveSystem(float deltaTime) {
        // Обновление волны зомби только если игра началась
        if (isStartingGame) {
            waveSystem.update(deltaTime);
        }
    }

    public void render() {
        float[] levelParams = ScaleToScreen.getScaledParams(0, 0, 2580, 1080);
        TextureManager.drawTexture(backgroundTexture,
                levelParams[0], levelParams[1],
                levelParams[2], levelParams[3]);
        // Рендер газонокосилок
        for (LawnMower mower : lawnMowers) {
            mower.render();
        }

        lawnGrid.render();

        synchronized (projectiles) {
            // Фильтруем null элементы, если они есть
            projectiles.removeIf(Objects::isNull);
    
            // Сортируем снаряды по координате Y
            projectiles.sort((a, b) -> {
                // Проверка значений getY()
                if (a == null || b == null) {
                    throw new IllegalArgumentException("Projectiles should not be null!");
                }
    
                float yA = a.getY();
                float yB = b.getY();
    
                // Проверка на NaN
                if (Float.isNaN(yA) || Float.isNaN(yB)) {
                    throw new IllegalArgumentException("getY() returned NaN for one or more projectiles");
                }
    
                return Float.compare(yA, yB);
            });
    
            // Рендерим отсортированные projectiles
            for (Projectile projectile : projectiles) {
                if (projectile != null) {
                    projectile.render();
                }
            }
        }

    }

    public static List<Projectile> getProjectiles() {
        return projectiles;
    }

    public static void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public void Starting() {
        WindowManager.getSoundEngine().playSoundEffect("readysetplant");
        WindowManager.getSoundEngine().loadAndPlayBackgroundMusic("music/Day.wav");

        String[] words = {
                Localization.get("level.text.start1"),
                Localization.get("level.text.start2"),
                Localization.get("level.text.start3")
        };

        long[] wordDelays = { 500, 520, 1250 };
        textAnimation.send(words, wordDelays, false, true);

        isStartingGame = true;
    }

    public void addSun(int amount) {
        sunCount += amount;
    }

    public void removeSun(int amount) {
        sunCount -= amount;
    }

    public int getSunCount() {
        return sunCount;
    }

    public void spawnSun() {
        float x = (float) (Math.random() * 1520);
        float y = 1000;
        float velocity = 175f;
        Sun sun = new Sun(x + 200, y, velocity, this, 25);
        suns.add(sun);
    }

    public void showSunRender() {
        for (Sun sun : suns) {
            sun.render();
        }
    }

    public void onMouseClick(float mouseX, float mouseY) {
        boolean clickedSun = false;

        for (Sun sun : suns) {
            if (sun.isClicked(mouseX, mouseY) && sun.isActive() && sun.GetisMovingToCorner() == false) {
                sun.setClicked(true);
                clickedSun = true;
                break;
            }
        }

        if (clickedSun) {
            WindowManager.getSoundEngine().playSoundEffect("points");
        }
    }

    public void setStartGame(boolean isStartingGame) {
        this.isStartingGame = isStartingGame;
    }

    public boolean getStartGame() {
        return isStartingGame;
    }

    public PlantManager getPlantManager() {
        return plantManager;
    }

    public LawnGrid getLawnGrid() {
        return lawnGrid;
    }

    public TextAnimation getTextAnimation() {
        return textAnimation;
    }

}
