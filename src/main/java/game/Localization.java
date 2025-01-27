package game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Localization {
	
    private static final Properties properties = new Properties();
    private static final String DEFAULT_PATH = "assets/localization/lang.properties"; // Путь к файлу

    public static void load() {
        System.out.println("Загрузка файла локализации...");

        File file = new File(DEFAULT_PATH);
        if (!file.exists()) {
            System.out.println("Файл локализации не найден: " + file.getPath());
            return; // Продолжаем выполнение, даже если файл отсутствует
        }

        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) { // Явно указываем UTF-8
            properties.load(reader);
            System.out.println("Файл локализации успешно загружен: " + DEFAULT_PATH);
        } catch (IOException e) {
            System.out.println("Ошибка загрузки файла локализации: " + DEFAULT_PATH);
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key, key); // Если ключ не найден, возвращаем сам ключ
    }
    
}