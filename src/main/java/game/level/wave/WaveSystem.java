package game.level.wave;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import game.Localization;
import game.ThreadPoolManager;
import game.WindowManager;
import game.entity.Entity;
import game.entity.EntityZombie;
import game.level.Level;

public class WaveSystem {

    private boolean hasPlayedSound = false;
    private boolean hasPlayedWaveSound = false;

    private int totalWaves; // Количество волн
    private int currentWave; // Текущая волна
    private float progress; // Прогресс до следующей волны (0-1)
    private float enemySpawnTimer; // Таймер для фонового спавна
    private boolean isFinalWave; // Индикатор последней волны
    private boolean isWaveActive; // Идёт ли текущая волна
    private boolean levelCompleted; // Индикатор завершения уровня

    private ZombieLaneManager laneManager; // Линии для спавна
    private Random random;

    private Map<Class<? extends Entity>, Float> spawnChances; // Враги и их шансы
    public static List<Entity> entities; // Список всех врагов

    private int[] laneZombieCounts;
    private Level level;

    public WaveSystem(Level level, int totalWaves, ZombieLaneManager lanemanager) {
        this.level = level;
        entities = new ArrayList<>();

        this.totalWaves = Math.max(1, totalWaves); // Минимум 1 волна
        this.currentWave = 0;
        this.progress = 0f;
        this.enemySpawnTimer = 0f;
        this.isFinalWave = false;
        this.isWaveActive = false;

        this.laneManager = lanemanager;
        this.random = new Random();
        this.spawnChances = new HashMap<>();

        this.laneZombieCounts = new int[laneManager.getNumLanes()];
    }

    // Настройка врагов
    public void addEnemyType(Class<? extends Entity> entityType, float spawnChance) {
        spawnChances.put(entityType, spawnChance);
    }

    private boolean isWaveDelayActive;
    private float waveDelayTimer;
    private int enemiesSpawnedInWave;
    private int waveEnemiesToSpawn;

    public void update(float deltaTime) {
        ThreadPoolManager.getInstance().submitTask(() -> removeEntitiesWithZeroHP());
        int batchSize = 100; // Размер группы
        List<List<Entity>> batches = createBatches(entities, batchSize);

        // Обрабатываем каждую группу в отдельном потоке
        batches.forEach(batch -> ThreadPoolManager.getInstance()
                .submitTask(() -> batch.forEach(entity -> entity.update(deltaTime))));

        // Если финальная волна завершена и все враги уничтожены
        if (isFinalWave && entities.isEmpty() && !isWaveActive) {
            levelCompleted = true;
            currentWave = totalWaves;
            return;
        }

        // Если прогресс достиг 1.0, ждём уничтожения всех врагов перед запуском новой
        // волны
        if (progress >= 1f) {
            if (!entities.isEmpty() || isWaveActive) {
                return; // Ждём завершения текущей волны
            }

            // Запускаем задержку перед новой волной
            handleWaveStartDelay(deltaTime);
            return;
        }

        // Если волна активна, продолжаем спавнить врагов
        if (isWaveActive) {
            spawnWaveEnemies(deltaTime);
        } else {
            // Если волна неактивна, увеличиваем прогресс
            enemySpawnTimer += deltaTime;
            float spawnInterval = calculateSpawnInterval();

            if (enemySpawnTimer >= spawnInterval) {
                spawnEnemy();
                enemySpawnTimer = 0f;
            }

            if (!isWaveActive && !isFinalWave) {
                progress += deltaTime * (0.005 + (currentWave * 0.001));
            }
        }
    }

    // Обработка задержки перед запуском волны
    private void handleWaveStartDelay(float deltaTime) {
        hasPlayedWaveSound = false;
        if (!isWaveDelayActive) {
            isWaveDelayActive = true;
            waveDelayTimer = 0f;

            // Анимация текста
            level.getTextAnimation().send(
                    new String[] { Localization.get("level.text.zombiewave") },
                    new long[] { 5000 }, true, false);

            if (!hasPlayedWaveSound) {
                WindowManager.getSoundEngine().playSoundEffect("hugewave");
                hasPlayedWaveSound = true;
            }
        }

        waveDelayTimer += deltaTime;
        if (waveDelayTimer >= 5f + (float) Math.random() * 3f) { // Задержка 5–8 секунд
            startWave();
            isWaveDelayActive = false;
        }
    }

    // Запуск волны
    private void startWave() {
        if (currentWave >= totalWaves) {
            return;
        }

        currentWave++;
        System.out.println("Wave " + currentWave + " started!");

        isWaveActive = true;
        hasPlayedWaveSound = false;
        progress = 0f; // Сбрасываем прогресс
        enemiesSpawnedInWave = 0;
        waveEnemiesToSpawn = (laneManager.getNumLanes() * 2) + currentWave * 2;

        if (currentWave == totalWaves) {
            System.out.println("Final wave started!");
            level.getTextAnimation().send(
                    new String[] { Localization.get("level.text.zombiewavefinal") },
                    new long[] { 2500 }, true, false);
            WindowManager.getSoundEngine().playSoundEffect("finalwave");

            isFinalWave = true;
        }
    }

    // Спавн врагов для волны
    private void spawnWaveEnemies(float deltaTime) {
        if (!hasPlayedWaveSound) {
            WindowManager.getSoundEngine().playSoundEffect("siren");
            hasPlayedWaveSound = true;
        }

        // Увеличение сложности с каждой волной
        int spawnRate = Math.max(5, 5 + currentWave); // Увеличиваем количество врагов с каждой волной
        int enemiesToSpawnThisWave = waveEnemiesToSpawn + currentWave * 3; // Увеличиваем общее количество врагов на
                                                                           // волне

        for (int i = 0; i < spawnRate && enemiesSpawnedInWave < enemiesToSpawnThisWave; i++) {
            spawnEnemy();
            enemiesSpawnedInWave++;
        }

        // Если все враги заспавнены и уничтожены, завершаем волну
        if (enemiesSpawnedInWave >= enemiesToSpawnThisWave && entities.isEmpty()) {
            System.out.println("Wave " + currentWave + " completed!");
            isWaveActive = false;
            progress = 0f; // Сбрасываем прогресс
        }
    }

    // Метод для создания групп
    private List<List<Entity>> createBatches(List<Entity> entities, int batchSize) {
        List<List<Entity>> batches = new ArrayList<>();
        for (int i = 0; i < entities.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entities.size());
            batches.add(entities.subList(i, end));
        }
        return batches;
    }

    private float calculateSpawnInterval() {
        if (currentWave == 0) {
            if (progress < 0.2f) {
                // Начальный интервал от 20 секунд
                return Math.max(20f - progress * 50f, 10f); // Быстрое уменьшение от 20 до 10 секунд
            } else if (progress < 0.5f) {
                // Умеренный спавн от 15 до 10 секунд
                return Math.max(15f - (progress - 0.2f) * 20f, 10f); // Разгон
            } else if (progress < 0.8f) {
                // Быстрый спавн от 10 до 5 секунд
                return Math.max(10f - (progress - 0.5f) * 15f, 5f); // Очень быстрый спавн
            } else {
                // Почти максимальная частота
                return Math.max(5f - (progress - 0.8f) * 10f, 2f); // Очень частый спавн от 5 до 2 секунд
            }
        } else {
            // Для последующих волн: быстрое увеличение сложности с прогрессом
            // Базовый интервал
            float baseSpawnTime = Math.max(10f - (currentWave * 0.25f), 0.1f); // Интервал уменьшается с каждой волной,
                                                                               // но не ниже 0.1

            // Учитываем прогресс, который ускоряет спавн по мере продвижения
            // Прогресс влияет на ускорение спавна, делая его быстрее с каждой волной
            float progressFactor = 1f + (progress * 1.5f); // Умножаем прогресс на коэффициент, чтобы увеличить влияние

            // Увеличиваем сложность в зависимости от прогресса и текущей волны
            float finalSpawnTime = baseSpawnTime / progressFactor; // Уменьшаем интервал с учётом прогресса

            return (float) (finalSpawnTime - (Math.random() * 1.5f)); // Немного случайности для вариативности
        }
    }

    // Спавн врагов
    private void spawnEnemy() {
        Class<? extends Entity> selectedType = selectEnemyType();
        if (selectedType == null)
            return;

        // Случайная линия
        int chosenLane = getMinEntityLane();

        float laneX = laneManager.getLaneX(chosenLane);
        float laneY = laneManager.getLaneY(chosenLane);

        // Создаём врага
        EntitySpawner<? extends Entity> spawner = new EntitySpawner<>(selectedType);
        Entity entity = spawner.createEntity(laneX, laneY);

        if (entity != null) {
            synchronized (entities) {
                entities.add(entity);
            }
            laneZombieCounts[chosenLane]++;

            if (!hasPlayedSound) {
                WindowManager.getSoundEngine().playSoundEffect("awooga");
                hasPlayedSound = true;
            }
            if (entity instanceof EntityZombie) {
                EntityZombie zombie = (EntityZombie) entity;
                zombie.startWalking();
            }
        }
    }

    private int getMinEntityLane() {
        int minZombieCount = Integer.MAX_VALUE;
        List<Integer> minLanes = new ArrayList<>();

        // Находим минимальное количество зомби среди всех ланей
        for (int i = 0; i < laneZombieCounts.length; i++) {
            if (laneZombieCounts[i] < minZombieCount) {
                minZombieCount = laneZombieCounts[i];
                minLanes.clear();
                minLanes.add(i);
            } else if (laneZombieCounts[i] == minZombieCount) {
                minLanes.add(i);
            }
        }

        // Возвращаем случайно выбранную лань из минимальных
        Random random = new Random();
        return minLanes.get(random.nextInt(minLanes.size()));
    }

    // Выбор врага по шансам
    private Class<? extends Entity> selectEnemyType() {
        float totalChance = spawnChances.values().stream().reduce(0f, Float::sum);
        float roll = random.nextFloat() * totalChance;

        float cumulative = 0f;
        for (Map.Entry<Class<? extends Entity>, Float> entry : spawnChances.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void removeEntitiesWithZeroHP() {
        // Используем потокобезопасную коллекцию, чтобы избежать блокировок
        entities.removeIf(entity -> entity.hp <= 0);
    }

    public void render() {
        List<Entity> entitiesToRender = new ArrayList<>(entities);
        entitiesToRender.sort(Comparator.comparingDouble(Entity::getY).reversed());
        for (Entity entity : entitiesToRender) {
            entity.render();
        }
    }

    // Получение текущего списка врагов
    public static List<Entity> getEntities() {
        return entities;
    }

    public int getTotalWaves() {
        return totalWaves;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public float getProgress() {
        return progress;
    }

    public Map<Class<? extends Entity>, Float> getEnemyTypes() {
        return spawnChances;
    }

    public boolean getLevelCompleted() {
        return levelCompleted;
    }

}
