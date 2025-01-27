package game.utils;

import game.font.FontManager;
import game.font.FontTT;
import game.font.Color4f;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadingCircle {
    
    private List<PerformanceSegment> segments;
    private float totalLoad;
    private Map<String, Float> smoothedLoads;
    private Map<String, Float> targetLoads;
    private long lastUpdateTime;
    private static final long UPDATE_INTERVAL = 500; // Обновлять каждые 500 мс
    private static final float SMOOTHING_FACTOR = 0.1f;

    public LoadingCircle() {
        segments = new ArrayList<>();
        totalLoad = 0;
        smoothedLoads = new HashMap<>();
        targetLoads = new HashMap<>();
        lastUpdateTime = System.currentTimeMillis();
    }

    public void addSegment(String name, float load, Color4f color) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
            lastUpdateTime = currentTime;
            targetLoads.put(name, load);
        }

        if (!smoothedLoads.containsKey(name)) {
            smoothedLoads.put(name, load);
            segments.add(new PerformanceSegment(name, load, color));
        }
    }

    public void update(float deltaTime) {
        totalLoad = 0;
        boolean changed = false;

        for (Map.Entry<String, Float> entry : targetLoads.entrySet()) {
            String name = entry.getKey();
            float targetLoad = entry.getValue();
            float currentLoad = smoothedLoads.getOrDefault(name, 0f);
            
            float newLoad = currentLoad + (targetLoad - currentLoad) * SMOOTHING_FACTOR;
            if (Math.abs(newLoad - currentLoad) > 0.01f) {
                changed = true;
            }
            
            smoothedLoads.put(name, newLoad);
            totalLoad += newLoad;
        }

        if (changed) {
            updateSegments();
        }
    }

    private void updateSegments() {
        segments.clear();
        for (Map.Entry<String, Float> entry : smoothedLoads.entrySet()) {
            String name = entry.getKey();
            float load = entry.getValue();
            Color4f color = getColorForSegment(name);
            segments.add(new PerformanceSegment(name, load, color));
        }
    }

    private Color4f getColorForSegment(String name) {
        return new Color4f(1, 1, 1, 1); // Белый цвет по умолчанию
    }

    public void clearSegments() {
        segments.clear();
        smoothedLoads.clear();
        targetLoads.clear();
        totalLoad = 0;
    }

    public void render(float x, float y, float radius) {
        glPushMatrix();

        float centerX = x;
        float centerY = y;

        // Рисуем сегменты
        float startAngle = 0;
        for (PerformanceSegment segment : segments) {
            float sweepAngle = (segment.load / totalLoad) * 360;
            drawSegment(centerX, centerY, radius, startAngle, sweepAngle, segment.color);
            startAngle += sweepAngle;
        }

        // Рисуем текст для каждого сегмента
        FontTT font = FontManager.getFont("BRIANNETOD");
        if (font != null) {
            float textAngle = 0;
            for (PerformanceSegment segment : segments) {
                float sweepAngle = (segment.load / totalLoad) * 360;
                float midAngle = textAngle + sweepAngle / 2;
                float textX = centerX + (float) Math.cos(Math.toRadians(midAngle)) * (radius * 0.7f);
                float textY = centerY + (float) Math.sin(Math.toRadians(midAngle)) * (radius * 0.7f);
                
                String text = String.format("%s: %.1f%%", segment.name, (segment.load / totalLoad) * 100);
                font.drawTextWithShadow(text, 12, textX, textY, 0, Color4f.WHITE, 0, 0, 0, true, 1.0f, 1.0f, new Color4f("#000000"));
                
                textAngle += sweepAngle;
            }
        }

        glPopMatrix();
    }
    
    private void drawSegment(float centerX, float centerY, float radius, float startAngle, float sweepAngle, Color4f color) {
        glBegin(GL_TRIANGLE_FAN);
        glColor4f(color.getRed(), color.getGreen(), color.getBlue(), 0.8f);
        glVertex2f(centerX, centerY);
        for (float angle = startAngle; angle <= startAngle + sweepAngle; angle += 0.5f) {
            double rad = Math.toRadians(angle);
            glVertex2f(centerX + (float) Math.cos(rad) * radius, centerY + (float) Math.sin(rad) * radius);
        }
        glEnd();
    }

    private class PerformanceSegment {
        String name;
        float load;
        Color4f color;

        PerformanceSegment(String name, float load, Color4f color) {
            this.name = name;
            this.load = load;
            this.color = color;
        }
    }
}