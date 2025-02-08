package game;

public class DebugManager {

    private static boolean debugMode = SettingsManager.isDebugMode();
    private static boolean collisionMode = false;

    // Включение или выключение общего режима дебага
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        SettingsManager.setDebugMode(enabled);
    }

    // Проверка, включён ли общий режим дебага
    public static boolean isDebugMode() {
        return debugMode;
    }

    // Проверка, включён ли режим дебага коллизий
    public static boolean isCollisionDebugMode() {
        return collisionMode;
    }

    // Переключение режима коллизии
    public static void toggleCollisionMode() {
        collisionMode = !collisionMode; // Переключаем состояние коллизии

    }

}