package game.gui;

import java.util.Arrays;
import java.util.Comparator;

import game.WindowManager;

public class ScaleToScreen {

    // Перечисление для стандартных соотношений сторон
    public enum AspectRatio {
        FULL_HD(16 / 9f, 1920, 1080),
        SXGA(4 / 3f, 1280, 1024),
        WIDESCREEN(21 / 9f, 2560, 1080),
        ULTRAWIDE(32 / 9f, 3840, 1080),
        STANDARD(16 / 10f, 1920, 1200);

        public final float ratio;
        public final int width;
        public final int height;

        AspectRatio(float ratio, int width, int height) {
            this.ratio = ratio;
            this.width = width;
            this.height = height;
        }
    }

    // Параметры экрана
    public static float screenCenterX, screenCenterY;
    public static int screenWidth, screenHeight;
    public static float scaleValue;
    public static AspectRatio currentAspect;

    // Базовые размеры (по умолчанию Full HD)
    public static final float BASE_WIDTH = 1920f;
    public static final float BASE_HEIGHT = 1080f;

    // Метод обновления с динамическим выбором формата
    public static void update() {
        screenWidth = WindowManager.displayWidth;
        screenHeight = WindowManager.displayHeight;

        float currentRatio = screenWidth / (float) screenHeight;

        // Выбор ближайшего подходящего соотношения
        currentAspect = Arrays.stream(AspectRatio.values())
                .min(Comparator.comparingDouble(ar -> Math.abs(ar.ratio - currentRatio)))
                .orElse(AspectRatio.FULL_HD);

        // Обновляем масштабирование
        refresh(currentAspect.ratio);
    }

    // Обновляем параметры экрана
    private static void refresh(float aspectRatio) {
        screenWidth = WindowManager.displayWidth;
        screenHeight = WindowManager.displayHeight;
        screenCenterX = screenWidth / 2f;
        screenCenterY = screenHeight / 2f;
        float ratio = screenWidth / (float) screenHeight;

        // Расчёт масштаба
        scaleValue = ratio < aspectRatio
                ? screenHeight / (1.0f + (aspectRatio - ratio))
                : screenHeight;
    }

    // Возвращает масштабированное значение
    public static float get(float value) {
        return scaleValue / (BASE_HEIGHT / value);
    }

    // Возвращает центр по оси X
    public static float getCenterX(float value) {
        return screenCenterX + scaleValue / (BASE_HEIGHT / (value - BASE_WIDTH / 2f));
    }

    // Возвращает центр по оси Y
    public static float getCenterY(float value) {
        return screenCenterY + scaleValue / (BASE_HEIGHT / (value - BASE_HEIGHT / 2f));
    }

    // Возвращает правую границу экрана с учётом масштаба
    public static float getRight(float value) {
        return screenWidth + scaleValue / (BASE_HEIGHT / (value - BASE_WIDTH));
    }

    // Возвращает нижнюю границу экрана с учётом масштаба
    public static float getBot(float value) {
        return screenHeight - scaleValue / (BASE_HEIGHT / (BASE_HEIGHT - value));
    }

    // Возвращает верхнюю границу экрана с учётом масштаба
    public static float getTop(float value) {
        return scaleValue / (BASE_HEIGHT / value);
    }

    // Возвращает растянутую высоту
    public static float getStretchedHeight(float originalHeight) {
        return screenHeight / (BASE_HEIGHT / originalHeight);
    }

    // Учитывает ширину экрана при растягивании
    public static float getStretchedWidth(float originalWidth) {
        return screenWidth / (BASE_WIDTH / originalWidth);
    }
    
	public static float calculateRelativeX(float baseX, float baseWidth) {
	    return getStretchedWidth(baseX / baseWidth * ScaleToScreen.BASE_WIDTH);
	}

	public static float calculateRelativeY(float baseY, float baseHeight) {
	    return getStretchedHeight(baseY / baseHeight * ScaleToScreen.BASE_HEIGHT);
	}
	
	public static float calculateRelativeWidth(float baseWidth, float originalWidth) {
	    return originalWidth / baseWidth * getStretchedWidth(ScaleToScreen.BASE_WIDTH);
	}

	public static float calculateRelativeHeight(float baseHeight, float originalHeight) {
	    return originalHeight / baseHeight * getStretchedHeight(ScaleToScreen.BASE_HEIGHT);
	}

    public static float[] getScaledParams(float x, float y, float width, float height) {
        return new float[] {
            getStretchedWidth(x),  // Масштабируем X
            getStretchedHeight(y), // Масштабируем Y
            getStretchedWidth(width),  // Масштабируем ширину
            getStretchedHeight(height) // Масштабируем высоту
        };
    }
    
}
