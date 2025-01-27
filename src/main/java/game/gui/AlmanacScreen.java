package game.gui;

import org.lwjgl.opengl.GL11;

import game.Localization;
import game.WindowManager;
import game.entity.EntityZombie;
import game.font.Color4f;
import game.font.FontManager;
import game.texture.TextureManager;

public class AlmanacScreen extends AbstractScreen {

	private EntityZombie zombie;
	
	public AlmanacScreen() {
		WindowManager.getSoundEngine().loadAndPlayBackgroundMusic("music/SelectCard.wav");
		
		TextureManager.loadTexture("Almanac_IndexBack", "textures/Almanac/Almanac_IndexBack.png");
		
		TextureManager.loadTexture("Almanac_CloseButton", "textures/Almanac/Almanac_CloseButton.png");
		TextureManager.loadTexture("Almanac_CloseButtonHighlight", "textures/Almanac/Almanac_CloseButtonHighlight.png");
		
		TextureManager.loadTexture("SeedChooser_Button", "textures/HotBar/SeedChooser_Button.png");
		TextureManager.loadTexture("SeedChooser_Button_Disabled", "textures/HotBar/SeedChooser_Button_Disabled.png");
		
		TextureManager.loadTexture("ButtonParam", "textures/setting/ButtonParam.png");
		TextureManager.loadTexture("ButtonParam1", "textures/setting/ButtonParam1.png");
		
		 zombie = new EntityZombie(1, 1);
	}
	
	@Override
	public void render() {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
		TextureManager.drawFullScreenTexture("Almanac_IndexBack");
		
		drawButtonWithText(0, "Almanac_CloseButton", "Almanac_CloseButtonHighlight", Localization.get("button.back"), "FBUSV8C5EI", ScaleToScreen.getRight(1615), ScaleToScreen.get(35), ScaleToScreen.get(220), ScaleToScreen.get(50), new Color4f("#FFFFFF"), 32);
		
		float[] scaledParams = ScaleToScreen.getScaledParams(980, 1020, 1, 30); 
		FontManager.getFont("BRIANNETOD").drawTextWithShadow(Localization.get("AlmanacScreen.name"), (int) ScaleToScreen.get(50), scaledParams[0], scaledParams[1], 0, new Color4f("#FFFFFF"), 0.1f, 0.1f, 0, true, 0.1f, 0.1f, new Color4f("#000000"));
        
        
        // Кнопка "Растения"
        float[] plantsButtonParams = ScaleToScreen.getScaledParams(300, 380, 340, 70);
        drawButtonWithText(1, "SeedChooser_Button", "SeedChooser_Button_Disabled", Localization.get("AlmanacScreen.plants"), "BRIANNETOD",
                plantsButtonParams[0], plantsButtonParams[1], plantsButtonParams[2], plantsButtonParams[3],
                new Color4f("#FFC600"), 32, 0, 9);

        // Кнопка "Зомби"
        float[] zombiesButtonParams = ScaleToScreen.getScaledParams(1240, 400, 370, 70);
        drawButtonWithText(2, "ButtonParam", "ButtonParam1", Localization.get("AlmanacScreen.zombies"), "BRIANNETOD",
                zombiesButtonParams[0], zombiesButtonParams[1], zombiesButtonParams[2], zombiesButtonParams[3],
                new Color4f("#26C400"), 32, 0, 9);

        float[] shadowParams = ScaleToScreen.getScaledParams(1362, 480, 130, 40);
        TextureManager.drawTexture("shadow", shadowParams[0], shadowParams[1], shadowParams[2], shadowParams[3]);
        // Параметры для текстуры зомби
        float[] zombieParams = ScaleToScreen.getScaledParams(1335, 480, 140, 205);
        TextureManager.drawTexture(zombie.getAnimation().getCurrentFrame(), zombieParams[0], zombieParams[1], zombieParams[2], zombieParams[3]);

        GL11.glPopMatrix();
	}


	@Override
	public void update(float aspect) {
		super.update(aspect);
		zombie.update(aspect);
	}
	
	@Override
	public void onButtonClick(int id) {
	       switch (id) {
           case 0:
        	   WindowManager.getSoundEngine().playSoundEffect("tap2");
              	WindowManager.CloseOverlayScreen();
               break;
           case 1:
        	   WindowManager.getSoundEngine().playSoundEffect("tap2");
             	 WindowManager.OpenOverlayScreen(new AlmanacScreenPlants());
              break;
           case 2:
        	   WindowManager.getSoundEngine().playSoundEffect("gravebutton");
             	 WindowManager.OpenOverlayScreen(new AlmanacScreenZombie());
              break;
           default:
               break;
       }
	}
	
	@Override
	public void cleanup() {
		
	}

	@Override
	public void input(boolean isKeyPressed) {
		
	}

}
