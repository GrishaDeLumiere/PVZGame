package game;

import java.io.*;
import java.util.Properties;

public class SettingsManager {

    private static final String SETTINGS_FILE = "settings.properties";
    private static Properties properties = new Properties();

    static {
        loadSettings();
    }

    public static void loadSettings() {
        File settingsFile = new File(SETTINGS_FILE);
        if (settingsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                properties.load(fis);

                addDefaultIfMissing("fullscreen", "false");
                addDefaultIfMissing("width", "1280");
                addDefaultIfMissing("height", "800");
                addDefaultIfMissing("masterVolume", "0.25");
                addDefaultIfMissing("backgroundMusicVolume", "0.3");
                addDefaultIfMissing("debugMode", "false");
                addDefaultIfMissing("autoSunSelection", "false");

                saveSettings(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            properties.setProperty("fullscreen", "false");
            properties.setProperty("width", "1280");
            properties.setProperty("height", "800");
            properties.setProperty("masterVolume", "0.25");
            properties.setProperty("backgroundMusicVolume", "0.3");
            properties.setProperty("debugMode", "false");
            properties.setProperty("autoSunSelection", "false");
            saveSettings();
        }
    }

    private static void addDefaultIfMissing(String key, String defaultValue) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, defaultValue);
        }
    }

    public static void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFullscreen() {
        return Boolean.parseBoolean(properties.getProperty("fullscreen", "false"));
    }

    public static void setFullscreen(boolean fullscreen) {
        properties.setProperty("fullscreen", Boolean.toString(fullscreen));
        saveSettings();
    }

    public static int getWidth() {
        return Integer.parseInt(properties.getProperty("width", "1280"));
    }

    public static void setWidth(int width) {
        properties.setProperty("width", Integer.toString(width));
        saveSettings();
    }

    public static int getHeight() {
        return Integer.parseInt(properties.getProperty("height", "800"));
    }

    public static void setHeight(int height) {
        properties.setProperty("height", Integer.toString(height));
        saveSettings();
    }
    
    public static float getMasterVolume() {
        return Float.parseFloat(properties.getProperty("masterVolume", "0.25"));
    }

    public static void setMasterVolume(float volume) {
        properties.setProperty("masterVolume", Float.toString(volume));
        saveSettings();
    }

    public static float getBackgroundMusicVolume() {
        return Float.parseFloat(properties.getProperty("backgroundMusicVolume", "0.3"));
    }

    public static void setBackgroundMusicVolume(float volume) {
        properties.setProperty("backgroundMusicVolume", Float.toString(volume));
        saveSettings();
    }

    public static boolean isDebugMode() {
        return Boolean.parseBoolean(properties.getProperty("debugMode", "false"));
    }

    // Новый метод для изменения debugMode
    public static void setDebugMode(boolean debugMode) {
        properties.setProperty("debugMode", Boolean.toString(debugMode));
        saveSettings();
    }
    
    public static boolean isAutoSunSelectionEnabled() {
        return Boolean.parseBoolean(properties.getProperty("autoSunSelection", "false"));
    }

    // Новый метод: задать значение autoSunSelection
    public static void setAutoSunSelectionEnabled(boolean enabled) {
        properties.setProperty("autoSunSelection", Boolean.toString(enabled));
        saveSettings();
    }
    
    
}