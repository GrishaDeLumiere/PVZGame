package game.entity;

import java.util.List;

public class Animation {
    
    private final List<String> frames; // Имена текстур в анимации
    private final float frameTime; // Время отображения одного кадра
    private int currentFrameIndex; // Текущий кадр
    private float timer; // Таймер

    public Animation(List<String> frames, float frameTime) {
        this.frames = frames;
        this.frameTime = frameTime;
        this.currentFrameIndex = 0;
        this.timer = 0;
    }

    public void update(float deltaTime) {
        timer += deltaTime; 
        while (timer >= frameTime) { // Плавно продвигаем таймер
            timer -= frameTime; // Снимаем полные кадры
            currentFrameIndex = (currentFrameIndex + 1) % frames.size(); // Переключение на следующий кадр
        }
    }

    public String getCurrentFrame() {
        return frames.get(currentFrameIndex);
    }

    public String getFrameByIndex(int index) {
        return frames.get(index);
    }

    public static float calculateFrameDuration(float speed) {
        return 0.1f / (speed / 20.0f); 
    }

}