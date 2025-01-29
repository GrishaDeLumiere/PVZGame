package game.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import game.WindowManager;
import game.level.LevelScreen;
import game.texture.TextureManager;
import game.utils.AnimatedObject;

public class MainMenuScreen extends AbstractScreen {

    private Timer animationTimer;
    private List<AnimatedObject> animatedObjects = new ArrayList<>();
    private boolean AnimsFinished = false; 
	
	public MainMenuScreen() {
	if(AnimsFinished == false) {
		WindowManager.getAL().getMusicManager().loadAndPlayBackgroundMusic("music/MainMenu.wav");
		WindowManager.getAL().getSoundEffectManager().playSoundEffect("roll_in");
	}
	
        // Загружаем текстуры через TextureManager
        TextureManager.loadTexture("SelectorScreen_BG", "textures/mainmenu/SelectorScreen_BG.png");
        TextureManager.loadTexture("SelectorScreen_BG_Right", "textures/mainmenu/SelectorScreen_BG_Right.png");
        TextureManager.loadTexture("SelectorScreen_BG_Center", "textures/mainmenu/SelectorScreen_BG_Center.png");
        TextureManager.loadTexture("SelectorScreen_BG_Left", "textures/mainmenu/SelectorScreen_BG_Left.png");
        
        TextureManager.loadTexture("SelectorScreen_WoodSign1", "textures/mainmenu/SelectorScreen_WoodSign1.png");
        TextureManager.loadTexture("SelectorScreen_WoodSign2", "textures/mainmenu/SelectorScreen_WoodSign2.png");
        TextureManager.loadTexture("SelectorScreen_WoodSign3", "textures/mainmenu/SelectorScreen_WoodSign3.png");
        
        //облака
        TextureManager.loadTexture("SelectorScreen_Cloud1", "textures/mainmenu/SelectorScreen_Cloud1.png");
        TextureManager.loadTexture("SelectorScreen_Cloud2", "textures/mainmenu/SelectorScreen_Cloud2.png");
        TextureManager.loadTexture("SelectorScreen_Cloud4", "textures/mainmenu/SelectorScreen_Cloud4.png");
        TextureManager.loadTexture("SelectorScreen_Cloud5", "textures/mainmenu/SelectorScreen_Cloud5.png");
        TextureManager.loadTexture("SelectorScreen_Cloud6", "textures/mainmenu/SelectorScreen_Cloud6.png");
        TextureManager.loadTexture("SelectorScreen_Cloud7", "textures/mainmenu/SelectorScreen_Cloud7.png");
        
        TextureManager.loadTexture("SelectorScreen_Leaves", "textures/mainmenu/SelectorScreen_Leaves.png");
        
        //Травка
       // TextureManager.loadTexture("SelectorScreen_Leaf1", "textures/mainmenu/SelectorScreen_Leaf1.png");
       // TextureManager.loadTexture("SelectorScreen_Leaf2", "textures/mainmenu/SelectorScreen_Leaf2.png");
       // TextureManager.loadTexture("SelectorScreen_Leaf3", "textures/mainmenu/SelectorScreen_Leaf3.png");
       // TextureManager.loadTexture("SelectorScreen_Leaf4", "textures/mainmenu/SelectorScreen_Leaf4.png");
       // TextureManager.loadTexture("SelectorScreen_Leaf5", "textures/mainmenu/SelectorScreen_Leaf5.png");
        
        //Кнопки
        TextureManager.loadTexture("SelectorScreen_Adventure_button", "textures/mainmenu/SelectorScreen_Adventure_button.png");
        TextureManager.loadTexture("SelectorScreen_Adventure_highlight", "textures/mainmenu/SelectorScreen_Adventure_highlight.png");
        
        TextureManager.loadTexture("SelectorScreen_Vasebreaker_button", "textures/mainmenu/SelectorScreen_Vasebreaker_button.png");
        TextureManager.loadTexture("SelectorScreen_vasebreaker_highlight", "textures/mainmenu/SelectorScreen_vasebreaker_highlight.png");
        
        TextureManager.loadTexture("SelectorScreen_Almanac", "textures/mainmenu/SelectorScreen_Almanac.png");
        TextureManager.loadTexture("SelectorScreen_AlmanacHighlight", "textures/mainmenu/SelectorScreen_AlmanacHighlight.png");
        
        TextureManager.loadTexture("SelectorScreen_Quit1", "textures/mainmenu/SelectorScreen_Quit1.png");
        TextureManager.loadTexture("SelectorScreen_Quit2", "textures/mainmenu/SelectorScreen_Quit2.png");
        
        TextureManager.loadTexture("SelectorScreen_Options1", "textures/mainmenu/SelectorScreen_Options1.png");
        TextureManager.loadTexture("SelectorScreen_Options2", "textures/mainmenu/SelectorScreen_Options2.png");
		
        SwingUtilities.invokeLater(() -> {
            animatedObjects.add(new AnimatedObject(-600, 0, 5, true)); // Гроб
            animatedObjects.add(new AnimatedObject(1, -100, 4, false)); //дерево
            
            animatedObjects.add(new AnimatedObject(200, -10, 4, false)); // SelectorScreen_WoodSign1
            animatedObjects.add(new AnimatedObject(200, -10, 4, false)); // SelectorScreen_WoodSign2
            animatedObjects.add(new AnimatedObject(200, -10, 4, false)); // SelectorScreen_WoodSign13
            
            startAnimations();
        });
        
        AnimsFinished = true;
        
	}
	
    private void startAnimations() {
        animationTimer = new Timer(16, e -> {
            boolean needsUpdate = false;

            for (AnimatedObject obj : animatedObjects) {
                needsUpdate |= obj.animate(); // Если хотя бы один элемент еще движется
            }

            if (!needsUpdate) {
                animationTimer.stop(); // Останавливаем таймер, если всё завершилось
            }
        });
        animationTimer.start();
    }
	
	@Override
	public void render() {
		TextureManager.drawFullScreenTexture("SelectorScreen_BG");
		
	    if (animatedObjects.isEmpty()) {
	        return; // Если список пуст, просто ничего не рисуем
	    }
	    
        
        TextureManager.drawTexture("SelectorScreen_BG_Center",
        		ScaleToScreen.get(150), 
                0,
                ScaleToScreen.get(1500), ScaleToScreen.get(580));
	    
        //ГРОБ
        TextureManager.drawTexture("SelectorScreen_BG_Right",
        		ScaleToScreen.getRight(500), 
        		ScaleToScreen.get(animatedObjects.get(0).currentY),
        		ScaleToScreen.get(1280),  ScaleToScreen.get(1000));
        
        TextureManager.drawTexture("SelectorScreen_BG_Left",
                0, 
                ScaleToScreen.get(animatedObjects.get(1).currentY),
                ScaleToScreen.get(800), ScaleToScreen.getStretchedHeight(1200));
        
        //Табличка
        
        TextureManager.drawTexture("SelectorScreen_WoodSign1",
                30,  // Правый край экрана
                ScaleToScreen.getBot(animatedObjects.get(2).currentY + 885),  // Верх экрана
                ScaleToScreen.get(500), ScaleToScreen.get(220));  // Размеры
        
        TextureManager.drawTexture("SelectorScreen_WoodSign2",
                30,  // Правый край экрана
                ScaleToScreen.getBot(animatedObjects.get(3).currentY + 805),  // Верх экрана
                ScaleToScreen.get(500), ScaleToScreen.get(100));  // Размеры
        
        TextureManager.drawTexture("SelectorScreen_WoodSign3",
                45,  // Правый край экрана
                ScaleToScreen.getBot(animatedObjects.get(3).currentY + 755),  // Верх экрана
                ScaleToScreen.get(120), ScaleToScreen.get(75));  // Размеры
        
        //Трава
        TextureManager.drawTexture("SelectorScreen_Leaves",
        		ScaleToScreen.get(460), 
        		ScaleToScreen.get(0),
                ScaleToScreen.get(500), ScaleToScreen.get(120));
        
        //Облачки
        TextureManager.drawTexture("SelectorScreen_Cloud5",
        		ScaleToScreen.getRight(1350), 
        		ScaleToScreen.get(900),
                ScaleToScreen.get(550), ScaleToScreen.get(150));
        
        TextureManager.drawTexture("SelectorScreen_Cloud4",
        		ScaleToScreen.getRight(1550), 
        		ScaleToScreen.get(810),
                ScaleToScreen.get(300), ScaleToScreen.get(50));
        
        
        drawButton(0, "SelectorScreen_Adventure_button", "SelectorScreen_Adventure_highlight", ScaleToScreen.getRight(1030), ScaleToScreen.get(animatedObjects.get(0).currentY + 680), ScaleToScreen.get(570), ScaleToScreen.get(170));
        
        drawButton(7, "SelectorScreen_Almanac", "SelectorScreen_AlmanacHighlight", ScaleToScreen.getRight(1120), ScaleToScreen.get(animatedObjects.get(0).currentY + 75), ScaleToScreen.get(175), ScaleToScreen.get(175));
        
        drawButton(9, "SelectorScreen_Options1", "SelectorScreen_Options2", ScaleToScreen.getRight(1277), ScaleToScreen.get(animatedObjects.get(0).currentY + 135), ScaleToScreen.get(172), ScaleToScreen.get(50));
        
        drawButton(10, "SelectorScreen_Quit1", "SelectorScreen_Quit2", ScaleToScreen.getRight(1525), ScaleToScreen.get(animatedObjects.get(0).currentY + 115), ScaleToScreen.get(130), ScaleToScreen.get(50));
        
	}

	@Override
	public void cleanup() {}

	@Override
	public void update(float delta) {
         super.update(delta);
	}

	@Override
	public void onButtonClick(int id) {
        switch (id) {
            case 0:
            	WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap");
            	WindowManager.SwitchToScreen(new LevelScreen());
                break;
            case 1:

                break;
            case 2:

                break;
                
            case 7:
            	WindowManager.getAL().getSoundEffectManager().playSoundEffect("bleep");
            	WindowManager.SwitchToScreen(new AlmanacScreen());
                break;
                
            case 9:
            	WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");
            	WindowManager.SwitchToScreen(new SettingScreen());
                break;
            case 10:
            	WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");
            	WindowManager.exitGame();
                break;
            default:
                break;
        }
	}
	
	@Override
	public void input(boolean isKeyPressed) {}
	
}
