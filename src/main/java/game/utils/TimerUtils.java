package game.utils;

public class TimerUtils {

    private static long lastTime = System.nanoTime(); // Время последнего кадра
    private static long lastRealTime = System.nanoTime(); // Реальное время последнего кадра (без timeScale)

    private static int frames = 0; // Количество кадров
    private static float elapsedTime = 0; // Время, прошедшее с последнего обновления FPS
    private static int currentFps = 0; // Текущее значение FPS

    // Коэффициент времени (1.0 - нормальная скорость, 0.5 - замедление, 2.0 - ускорение)
    private static float timeScale = 1.0f;

    /**
     * Получает дельту времени с учётом timeScale
     */
    public static float getDelta() {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTime) / 1_000_000_000f * timeScale; // Перевод в секунды с учётом timeScale
        lastTime = currentTime;
        return deltaTime;
    }

    /**
     * Получает реальную дельту времени (без учёта timeScale)
     */
    public static float getRealDelta() {
        long currentRealTime = System.nanoTime();
        float realDelta = (currentRealTime - lastRealTime) / 1_000_000_000f; // Перевод в секунды
        lastRealTime = currentRealTime;
        return realDelta;
    }

    public static void init() {
        lastTime = System.nanoTime(); // Инициализация времени для игрового цикла
        lastRealTime = System.nanoTime(); // Инициализация реального времени
    }

    /**
     * Обновление FPS (на основе реального времени)
     */
    public static void update() {
        frames++;
        elapsedTime += getRealDelta(); // Используем реальное время для подсчёта FPS

        // Обновляем FPS каждые 1 секунду
        if (elapsedTime >= 1.0f) {
            currentFps = frames; // Фиксируем количество кадров за секунду
            frames = 0; // Сбрасываем счётчик
            elapsedTime = 0; // Сбрасываем время
        }
    }

    public static String getFpsString() {
        return "FPS: " + currentFps;
    }

    /**
     * Устанавливает коэффициент времени
     */
    public static void setTimeScale(float scale) {
        timeScale = scale;
    }

    /**
     * Получает текущий коэффициент времени
     */
    public static float getTimeScale() {
        return timeScale;
    }
}
