package game.gui;

import static org.lwjgl.glfw.GLFW.*;

import game.WindowManager;
import game.font.Color4f;
import game.font.FontManager;
import game.font.FontTT;
import game.texture.TextureManager;

public class LoadingScreen implements Screen {

    private int progress = 0;
    private boolean isFinished = false; 
    
    public LoadingScreen() {
    	TextureManager.loadTexture("titlescreen", "textures/loading/titlescreen.png");
        TextureManager.loadTexture("titlescreen1", "textures/loading/titlescreen1.png");
        TextureManager.loadTexture("loadBarDirt", "textures/loading/LoadBar_dirt.png");
        TextureManager.loadTexture("loadBarGrass", "textures/loading/LoadBar_grass.png");
    }

    @Override
    public void render() {
        TextureManager.drawFullScreenTexture("titlescreen1");

        // Отступ от нижнего края
        int offsetY = 45; // Отступ от нижнего края экрана
        int barY = offsetY; // Y-координата: нижний край + отступ

        // Рисуем dirt бар
        TextureManager.drawTexture("loadBarDirt",
            WindowManager.displayWidth / 2 - 205, // Центровка по горизонтали
            barY,                                // Позиция снизу
            420, 55);

        // Рисуем grass бар поверх, в зависимости от progress
        TextureManager.drawTexture("loadBarGrass",
            WindowManager.displayWidth / 2 - 210, // Центровка по горизонтали
            barY + 37,                                // Позиция снизу
            (int) (420 * (progress / 100.0)), 25);
        
        FontTT font = FontManager.getFont("FBUSV8C5EI");
        FontTT font1 = FontManager.getFont("BRIANNETOD");
        if (font != null) {
            font.drawTextWithShadow("Загрузка: " + progress + "%", 34, 
                                    WindowManager.displayWidth / 2, barY + 100, 0, 
                                    Color4f.WHITE, 0, 0, 0, true, 
                                    2.0f, 2.0f, new Color4f("#000000"));
            if(isFinished)
            font1.drawTextWithShadow("Нажмите любую кнопку для продолжения", 24, 
                    WindowManager.displayWidth / 2, barY + 45, 0, 
                    new Color4f("#ddbc19"), 0, 0, 0, true, 
                    2.0f, 2.0f, new Color4f("#000000"));
        }
        
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (progress == 100) {
            isFinished = true; // Загрузка завершена
        }
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public void cleanup() {}

    @Override
    public void update(float delta) {}

	@Override
	public void input(boolean isKeyPressed) {
		if(isFinished) {
		    for (int key = GLFW_KEY_SPACE; key <= GLFW_KEY_LAST; key++) {
		        if (glfwGetKey(WindowManager.getWindowHandle(), key) == GLFW_PRESS && !isKeyPressed) {
		        	WindowManager.SwitchToScreen(new MainMenuScreen());
		            isKeyPressed = true;
		        } else if (glfwGetKey(WindowManager.getWindowHandle(), key) == GLFW_RELEASE) {
		            isKeyPressed = false; 
		        }
		    }
		}
	}
}