package game;

public class DebugManager {

    private static boolean debugMode = SettingsManager.isDebugMode();

    // Включение или выключение режима
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        SettingsManager.setDebugMode(enabled);
    }

    // Проверка, включён ли режим
    public static boolean isDebugMode() {
        return debugMode;
    }

    // Логирование сообщений
    public static void log(String message) {
        if (debugMode) {
            System.out.println("[DEBUG] " + message);
        }
    }

    // Логирование с объектом
    public static void log(String tag, Object value) {
        if (debugMode) {
            System.out.println("[DEBUG] " + tag + ": " + value);
        }
    }
}