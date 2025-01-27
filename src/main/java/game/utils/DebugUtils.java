package game.utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.*;

import game.font.Color4f;
import game.font.FontManager;
import game.gui.ScaleToScreen;

public class DebugUtils {
    
    private static int vaoID;
    private static int vboID;
    private static final int MAX_HITBOXES = 1000; // Максимальное количество хитбоксов для батчинга
    private static FloatBuffer vertexBuffer;
    private static List<HitboxData> hitboxes = new ArrayList<>();

    public static void initVAO() {
        vaoID = GL30.glGenVertexArrays();
        vboID = GL15.glGenBuffers();
        
        vertexBuffer = BufferUtils.createFloatBuffer(MAX_HITBOXES * 8); // 4 вершины * 2 координаты на хитбокс

        GL30.glBindVertexArray(vaoID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
        
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public static void addHitbox(float x, float y, float width, float height, float offsetX, float offsetY, Color4f color) {
        if (hitboxes.size() < MAX_HITBOXES) {
            hitboxes.add(new HitboxData(x, y, width, height, offsetX, offsetY, color));
        }
    }

    public static void renderHitboxes() {
        if (hitboxes.isEmpty()) return;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL30.glBindVertexArray(vaoID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);

        vertexBuffer.clear();
        for (HitboxData hitbox : hitboxes) {
            addRectToBuffer(hitbox.x, hitbox.y, hitbox.width, hitbox.height, hitbox.offsetX, hitbox.offsetY);
        }
        vertexBuffer.flip();

        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuffer);

        GL11.glLineWidth(2.0f);
        for (int i = 0; i < hitboxes.size(); i++) {
            Color4f color = hitboxes.get(i).color;
            GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            GL11.glDrawArrays(GL11.GL_LINE_LOOP, i * 4, 4);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        hitboxes.clear();
    }

    private static void addRectToBuffer(float x, float y, float width, float height, float offsetX, float offsetY) {
        float x1 = ScaleToScreen.getStretchedWidth(x + offsetX);
        float y1 = ScaleToScreen.getStretchedHeight(y + offsetY);
        float x2 = ScaleToScreen.getStretchedWidth(x + offsetX + width);
        float y2 = ScaleToScreen.getStretchedHeight(y + offsetY + height);

        vertexBuffer.put(x1).put(y1);
        vertexBuffer.put(x2).put(y1);
        vertexBuffer.put(x2).put(y2);
        vertexBuffer.put(x1).put(y2);
    }

    private static class HitboxData {
        float x, y, width, height, offsetX, offsetY;
        Color4f color;

        HitboxData(float x, float y, float width, float height, float offsetX, float offsetY, Color4f color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.color = color;
        }
    }

    // Метод для отрисовки текста остается без изменений
    public static void renderText(float x, float y, String text, float textXOffset, float textYOffset, Color4f textColor) {
        FontManager.getFont("FBUSV8C5EI").drawTextWithShadow(
                text,
                ScaleToScreen.get(25),
                ScaleToScreen.getStretchedWidth(x + textXOffset),
                ScaleToScreen.getStretchedHeight(y + textYOffset),
                0,
                textColor,
                0.1f, 0.1f, 0,
                true,
                0.1f, 0.1f,
                new Color4f("#000000") // Цвет тени текста
        );
    }

}