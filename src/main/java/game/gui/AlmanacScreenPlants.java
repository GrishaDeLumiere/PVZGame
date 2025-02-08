package game.gui;

import org.lwjgl.opengl.GL11;

import game.Localization;
import game.WindowManager;
import game.font.Color4f;
import game.font.FontManager;
import game.texture.TextureManager;

public class AlmanacScreenPlants extends AbstractScreen  {
	
	public AlmanacScreenPlants() {
		WindowManager.getAL().getMusicManager().loadAndPlayBackgroundMusic("music/SelectCard.ogg");
		
		TextureManager.loadTexture("Almanac_PlantBack", "textures/Almanac/plant.png");
	}
	
	@Override
	public void render() {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
		TextureManager.drawFullScreenTexture("Almanac_PlantBack");
		
		drawButtonWithText(0, "Almanac_CloseButton", "Almanac_CloseButtonHighlight", "Назад", "FBUSV8C5EI", ScaleToScreen.getRight(1615), ScaleToScreen.get(35), ScaleToScreen.get(220), ScaleToScreen.get(50), new Color4f("#FFFFFF"), 32);
        
    	float[] scaledParams = ScaleToScreen.getScaledParams(980, 1040, 0, 0); 
		FontManager.getFont("BRIANNETOD").drawTextWithShadow(Localization.get("AlmanacScreenPlants.name"), (int) ScaleToScreen.get(50), scaledParams[0], scaledParams[1], 0, new Color4f("#E3C24F"), 0.1f, 0.1f, 0, true, 0.1f, 0.1f, new Color4f("#000000"));
        
        GL11.glPopMatrix();
	}

	@Override
	public void update(float aspect) {
		super.update(aspect);
	}
	
	@Override
	public void onButtonClick(int id) {
	       switch (id) {
           case 0:
        	   WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");
              	WindowManager.CloseOverlayScreen();
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
