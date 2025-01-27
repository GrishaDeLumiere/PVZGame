package game.shaders;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.lwjgl.opengl.GL20;

public class ShaderManager {
	
    private static final Map<String, ShaderProgram> shaders = new HashMap<>();
    private static ShaderProgram currentShader;
    private static final String SHADER_PATH = "shaders/";

    // Загрузка всех шейдеров
    public static void loadAllShaders() {
        try {
            loadShader("glow", "glow.vert", "glow.frag");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Загрузка одного шейдера
    private static void loadShader(String name, String vertexFileName, String fragmentFileName) throws IOException {
        String vertexCode = readFile(SHADER_PATH + vertexFileName);
        String fragmentCode = readFile(SHADER_PATH + fragmentFileName);
        
        // Создаём новый шейдер
        ShaderProgram shader = new ShaderProgram(vertexCode, fragmentCode);
        
        // Проверка успешности линковки шейдера
        if (GL20.glGetProgrami(shader.getProgramId(), GL20.GL_LINK_STATUS) == 0) {
            System.err.println("Ошибка линковки шейдера: " + name);
            System.err.println(GL20.glGetProgramInfoLog(shader.getProgramId()));
        } else {
            shaders.put(name, shader);
            System.out.println("Шейдер успешно загружен: " + name);
        }
    }

    // Применение шейдера по имени
    public static void useShader(String name) {
        ShaderProgram shader = shaders.get(name);
        if (shader != null) {
            currentShader = shader;
            currentShader.use();
        }
    }

    // Сброс текущего шейдера
    public static void resetShader() {
        currentShader = null;
        GL20.glUseProgram(0);
    }

    // Установка униформы в текущий шейдер
    public static void setUniform(String name, float value) {
        if (currentShader != null) {
            currentShader.setUniform(name, value);
        }
    }
    
    // Получение шейдера по имени
    public static ShaderProgram getShader(String name) {
        return shaders.get(name);
    }

    // Очистка ресурсов
    public static void cleanUp() {
        shaders.values().forEach(ShaderProgram::delete);
    }
    
    // Чтение файла с использованием BufferedReader и правильного пути
    private static String readFile(String filePath) throws IOException {
        // Конкатенация пути для правильного доступа к файлу
        File file = new File("assets/" + filePath);
        
        // Проверка существования файла
        if (!file.exists()) {
            throw new IOException("Файл не найден: " + file.getPath());
        }

        // Чтение файла с правильным относительным путем
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

}
