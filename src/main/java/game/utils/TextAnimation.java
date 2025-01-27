package game.utils;

import game.font.Color4f;
import game.font.FontManager;
import game.gui.ScaleToScreen;

import java.util.LinkedList;
import java.util.Queue;

public class TextAnimation {

    private int currentWordIndex = 0;
    private float textAlpha = 1.0f; // Прозрачность текста
    private boolean useFadeOut = true; // Использовать ли исчезновение
    private boolean isIncreasing = true; // Увеличивать или уменьшать текст
    private Queue<String> wordsQueue = new LinkedList<>();
    private boolean animationFinished = false;
    private long wordStartTime = System.currentTimeMillis();
    private long[] wordDelays;

    public TextAnimation() {}

    public void send(String[] words, long[] delays, boolean useFadeOut, boolean isIncreasing) {
        wordsQueue.clear();
        for (String word : words) {
            wordsQueue.add(word);
        }
        wordDelays = delays;
        this.useFadeOut = useFadeOut; // Установка флага исчезновения
        this.isIncreasing = isIncreasing; // Установка типа изменения размера текста
        reset();
    }

    private float currentFontSize = ScaleToScreen.get(150); // Начальный размер текста

    private float standartFontSize = ScaleToScreen.get(120);
    private float maxFontSize = ScaleToScreen.get(200); // Максимальный размер для увеличения

    private float minFontSize = ScaleToScreen.get(65); // Минимальный размер для уменьшения
    private float shrinkMaxFontSize = ScaleToScreen.get(150); // Максимальный размер для уменьшения

    private void reset() {
        currentWordIndex = 0;
        currentFontSize = isIncreasing ? standartFontSize : shrinkMaxFontSize; // Установка начального размера
        textAlpha = 1.0f; // Сброс прозрачности
        animationFinished = false;
        System.currentTimeMillis();
        wordStartTime = System.currentTimeMillis();
    }

    public void updateAnimation(float delta) {
        if (animationFinished)
            return;

        long currentTime = System.currentTimeMillis();
        if (currentWordIndex >= wordsQueue.size()) {
            animationFinished = true;
            return;
        }

        if (isIncreasing) {
            // Увеличение текста
            if (currentFontSize < maxFontSize) {
                currentFontSize += delta * 25;
                if (currentFontSize >= maxFontSize) {
                    currentFontSize = maxFontSize;
                    wordStartTime = currentTime;
                }
            }
        } else {
            // Уменьшение текста
            if (currentFontSize > minFontSize) {
                currentFontSize -= delta * 250;
                if (currentFontSize <= minFontSize) {
                    currentFontSize = minFontSize;
                    wordStartTime = currentTime;
                }
            }
        }

        long elapsedTime = currentTime - wordStartTime;
        if (elapsedTime >= wordDelays[currentWordIndex]) {
            if (useFadeOut && textAlpha > 0) {
                // Эффект исчезновения
                textAlpha -= delta * 5f;
                if (textAlpha < 0) {
                    textAlpha = 0;
                }
            } else {
                currentWordIndex++;
                if (currentWordIndex < wordsQueue.size()) {
                    currentFontSize = isIncreasing ? standartFontSize : shrinkMaxFontSize; // Сброс размера для
                                                                                                 // следующего слова
                    textAlpha = 1.0f; // Восстановление прозрачности
                    wordStartTime = currentTime;
                }
            }
        }
    }

    public void render() {
        if (animationFinished || currentWordIndex >= wordsQueue.size())
            return;

        String word = wordsQueue.toArray(new String[0])[currentWordIndex];
        if (isIncreasing) {
            FontManager.getFont("FBUSV8C5EI-BIG").drawTextWithOutline(
                    word,
                    (int) currentFontSize,
                    ScaleToScreen.getStretchedWidth(980),
                    ScaleToScreen.get(650),
                    0,
                    new Color4f(1.0f, 0.0f, 0.0f, textAlpha), // Установка прозрачности
                    0.1f,
                    0.1f,
                    0,
                    true,
                    5.0f,
                    new Color4f(0.0f, 0.0f, 0.0f, textAlpha) // Прозрачность обводки
            );
        } else {
            FontManager.getFont("FBUSV8C5EI-BIG").drawTextWithOutline(
                    word,
                    (int) currentFontSize,
                    ScaleToScreen.getStretchedWidth(980),
                    ScaleToScreen.get(580),
                    0,
                    new Color4f(1.0f, 0.0f, 0.0f, textAlpha), // Установка прозрачности
                    0.1f,
                    0.1f,
                    0,
                    true,
                    5.0f,
                    new Color4f(0.0f, 0.0f, 0.0f, textAlpha) // Прозрачность обводки
            );
        }
    }

    public boolean isAnimationFinished() {
        return animationFinished;
    }
}