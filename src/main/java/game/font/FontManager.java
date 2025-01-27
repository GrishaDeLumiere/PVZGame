package game.font;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private static final Map<String, FontTT> fonts = new HashMap<>();

    public static void loadFont(String fontName, String filePath, int fontSize) {
        File file = new File("assets/" + filePath); // Создаем объект File, указывая путь к шрифту
        if (!file.exists()) {
            System.err.println("Шрифт не найден по пути: " + filePath);
            return;
        }
        
        try (FileInputStream fontStream = new FileInputStream(file)) { // Используем FileInputStream для чтения файла
            FontTT font = new FontTT(Font.createFont(Font.TRUETYPE_FONT, fontStream), fontSize, 0);
            fonts.put(fontName, font);
            System.out.println("Шрифт '" + fontName + "' успешно загружен из: " + filePath);
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке шрифта '" + fontName + "' из файла: " + filePath);
            e.printStackTrace();
        }
    }

    public static FontTT getFont(String fontName) {
        return fonts.get(fontName);
    }

    public static void removeFont(String fontName) {
        fonts.remove(fontName);
        System.out.println("Шрифт '" + fontName + "' удален.");
    }

    public static boolean isFontLoaded(String fontName) {
        boolean isLoaded = fonts.containsKey(fontName);
        if (isLoaded) {
            System.out.println("Шрифт '" + fontName + "' найден и загружен.");
        } else {
            System.out.println("Шрифт '" + fontName + "' не найден.");
        }
        return isLoaded;
    }
    
    public static void LoadFonts() {
        FontManager.loadFont("BRIANNETOD", "fonts/BRIANNETOD.ttf", 32);
        FontManager.loadFont("FBUSV8C5EI", "fonts/FBUSV8C5EI.ttf", 32);
        FontManager.loadFont("ASHLEYSCRIPTMTSTD", "fonts/ASHLEYSCRIPTMTSTD.ttf", 32);
        FontManager.loadFont("PICO12", "fonts/PICO12.ttf", 32);
        //for waves
        FontManager.loadFont("FBUSV8C5EI-BIG", "fonts/FBUSV8C5EI.ttf", 125);
    }
}