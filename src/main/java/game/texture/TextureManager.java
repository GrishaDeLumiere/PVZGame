package game.texture;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL46;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.gui.ScaleToScreen;

public class TextureManager {

    // Хранение текстур по имени
    private static final Map<String, Integer> textureMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(TextureManager.class);

    public static void loadTexture(String textureName, String filePath) {
        int textureId;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            File file = new File("assets/" + filePath); 
            if (!file.exists()) {
                throw new RuntimeException("Texture not found: " + file.getPath());
            }

            InputStream inputStream = new FileInputStream(file);
            byte[] bytes = IOUtils.toByteArray(inputStream);
            ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bytes.length);
            imageBuffer.put(bytes);
            imageBuffer.flip();

            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture file: " + STBImage.stbi_failure_reason());
            }

            int width = widthBuffer.get(0);
            int height = heightBuffer.get(0);

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            GL30.glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            float maxAnisotropy = GL11.glGetFloat(GL46.GL_MAX_TEXTURE_MAX_ANISOTROPY);
            if (maxAnisotropy > 0.0f) {
                GL11.glTexParameterf(GL_TEXTURE_2D, GL46.GL_TEXTURE_MAX_ANISOTROPY, Math.min(16.0f, maxAnisotropy));
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            STBImage.stbi_image_free(image);

            textureMap.put(textureName, textureId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading texture: " + e.getMessage());
        }
    }



    // Метод для получения текстуры по имени
    public static int getTexture(String textureName) {
        return textureMap.getOrDefault(textureName, -1); // Возвращаем -1, если текстуры нет
    }

    public static void drawFullScreenTexture(String textureName) {
        int textureId = getTexture(textureName);
        if (textureId == -1) {
            System.out.println("Texture not found: " + textureName);
            return;
        }
        
        // Получаем масштабирование и размеры экрана
        float scaledWidth = ScaleToScreen.getStretchedWidth(ScaleToScreen.BASE_WIDTH);
        float scaledHeight = ScaleToScreen.getStretchedHeight(ScaleToScreen.BASE_HEIGHT);

        // Открываем матрицу для работы с OpenGL
        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, 1); // Устанавливаем цвет (по умолчанию белый)

        // Привязка текстуры
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Рисуем текстуру, учитывая масштабирование
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1); glVertex2f(0, 0);             // Нижний левый угол
        glTexCoord2f(1, 1); glVertex2f(scaledWidth, 0);   // Нижний правый угол
        glTexCoord2f(1, 0); glVertex2f(scaledWidth, scaledHeight); // Верхний правый угол
        glTexCoord2f(0, 0); glVertex2f(0, scaledHeight);  // Верхний левый угол
        glEnd();

        // Отвязываем текстуру
        glBindTexture(GL_TEXTURE_2D, 0);
        
        GL11.glPopMatrix(); // Закрываем матрицу
    }


    // Метод для удаления текстуры
    public static void deleteTexture(String textureName) {
        Integer textureId = textureMap.remove(textureName);
        if (textureId != null) {
            glDeleteTextures(textureId);
        }
    }
    
    
    public static GLFWImage.Buffer loadIcon(String filePath) {
        GLFWImage.Buffer iconBuffer = null;
    
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);
    
            // Проверка существования файла
            File file = new File(filePath); // Указываем полный путь
            if (!file.exists()) {
                logger.error("Icon file not found: {}", file.getPath());
                return null;
            }
    
            // Чтение файла в ByteBuffer
            ByteBuffer imageBuffer = null;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] bytes = new byte[(int) file.length()];
                inputStream.read(bytes);
                imageBuffer = ByteBuffer.allocateDirect(bytes.length);
                imageBuffer.put(bytes);
                imageBuffer.flip();
            }
    
            // Загрузка изображения через STBImage
            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (image == null) {
                logger.error("Failed to load icon file: {}", STBImage.stbi_failure_reason());
                return null;
            }
    
            // Создаем GLFWImage.Buffer
            int width = widthBuffer.get(0);
            int height = heightBuffer.get(0);
    
            iconBuffer = GLFWImage.malloc(1);
            GLFWImage icon = iconBuffer.get(0);
            icon.set(width, height, image);
    
            logger.info("Icon loaded successfully: {}x{}", width, height);
    
            // Освобождаем изображение
            STBImage.stbi_image_free(image);
            return iconBuffer;
        } catch (Exception e) {
            logger.error("Error loading icon: ", e);
            if (iconBuffer != null) {
                iconBuffer.free();
            }
            return null;
        }
    }

    
    public static void drawTexture(String textureName, float x, float y, float width, float height) {
        int textureId = getTexture(textureName);
        if (textureId == -1) {
            System.out.println("Texture not found: " + textureName);
            return;
        }

        glEnable(GL_BLEND);
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        // Рисование текстуры с использованием сглаживания
        glBegin(GL_QUADS);
            glTexCoord2f(0, 1); glVertex2f(x, y);                
            glTexCoord2f(1, 1); glVertex2f(x + width, y);     
            glTexCoord2f(1, 0); glVertex2f(x + width, y + height); 
            glTexCoord2f(0, 0); glVertex2f(x, y + height);   
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0); // Освобождаем текстуру

        glDisable(GL_BLEND); // Отключаем альфа-смешивание
    }
    
    public static void drawTexture(String textureName, float x, float y, float width, float height, float alpha) {
        int textureId = getTexture(textureName);
        if (textureId == -1) {
            System.out.println("Texture not found: " + textureName);
            return;
        }

        // Устанавливаем прозрачность (alpha)
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // Указываем, как смешивать цвета
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha); // RGBA: Белый цвет с заданной прозрачностью

        glBindTexture(GL_TEXTURE_2D, textureId);

        glBegin(GL_QUADS);

        glTexCoord2f(0, 1); glVertex2f(x, y);
        glTexCoord2f(1, 1); glVertex2f(x + width, y);
        glTexCoord2f(1, 0); glVertex2f(x + width, y + height);
        glTexCoord2f(0, 0); glVertex2f(x, y + height);

        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);

        // Сбрасываем цвет обратно на стандартный (без прозрачности)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glDisable(GL_BLEND);
    }

    public static void loadTextureRange(String prefix, int start, int end, String folderPath) {
        for (int i = start; i <= end; i++) {
            String textureName = String.format("%s%04d", prefix, i); // Генерация имени вида Zombie0001
            String filePath = folderPath + "/" + textureName + ".png"; // Путь к файлу
            loadTexture(textureName, filePath); // Загрузка текстуры
        }
    }


    public static void drawSubTexture(String textureName, float x, float y, float width, float height, float texX, float texY, float texWidth, float texHeight) {
        int textureId = getTexture(textureName);
        if (textureId == -1) {
            System.out.println("Texture not found: " + textureName);
            return;
        }

        glEnable(GL_BLEND);
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Вычисление текстурных координат (нормализация)
        float texCoordX1 = texX;
        float texCoordY1 = texY;
        float texCoordX2 = texX + texWidth;
        float texCoordY2 = texY + texHeight;

        glBegin(GL_QUADS);
            glTexCoord2f(texCoordX1, texCoordY2); glVertex2f(x, y);                  // Верхний левый угол
            glTexCoord2f(texCoordX2, texCoordY2); glVertex2f(x + width, y);         // Верхний правый угол
            glTexCoord2f(texCoordX2, texCoordY1); glVertex2f(x + width, y + height);// Нижний правый угол
            glTexCoord2f(texCoordX1, texCoordY1); glVertex2f(x, y + height);        // Нижний левый угол
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_BLEND);
    }
    
}