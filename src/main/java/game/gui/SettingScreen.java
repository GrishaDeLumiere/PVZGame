package game.gui;

import game.Localization;
import game.SettingsManager;
import game.WindowManager;
import game.font.Color4f;
import game.font.FontManager;
import game.texture.TextureManager;

public class SettingScreen extends AbstractScreen {
	
	public SettingScreen() {
		WindowManager.getAL().getMusicManager().loadAndPlayBackgroundMusic("music/SelectCard.ogg");
		
		TextureManager.loadTexture("setting_bg", "textures/setting/setting_bg.png");
		
		TextureManager.loadTexture("SeedChooser_Button2", "textures/setting/SeedChooser_Button2.png");
		TextureManager.loadTexture("SeedChooser_Button2_Glow", "textures/setting/SeedChooser_Button2_Glow.png");
		
		TextureManager.loadTexture("ButtonParam", "textures/setting/ButtonParam.png");
		TextureManager.loadTexture("ButtonParam1", "textures/setting/ButtonParam1.png");
	}
	
	@Override
	public void render() {
		TextureManager.drawFullScreenTexture("setting_bg");
		
		drawButtonWithText(0, "SeedChooser_Button2", "SeedChooser_Button2_Glow", Localization.get("button.back"), "FBUSV8C5EI", ScaleToScreen.getRight(1600), ScaleToScreen.get(35), ScaleToScreen.get(250), ScaleToScreen.get(50), new Color4f("#FFFFFF"), 32);
		
		String screenMode = SettingsManager.isFullscreen() ? Localization.get("SettingScreen.isFullscreen.on") : Localization.get("SettingScreen.isFullscreen.off");
		drawButtonWithText(1, "ButtonParam", "ButtonParam1", screenMode, "FBUSV8C5EI", ScaleToScreen.get(85), ScaleToScreen.get(800), ScaleToScreen.get(325), ScaleToScreen.get(75), new Color4f("#50c878"), 32);
		
		String autoSunSelectionMode = SettingsManager.isAutoSunSelectionEnabled() ? Localization.get("SettingScreen.isAutoSunSelectionEnabled") : Localization.get("SettingScreen.isAutoSunSelectionDisabled");
		drawButtonWithText(2, "ButtonParam", "ButtonParam1", autoSunSelectionMode, "FBUSV8C5EI", ScaleToScreen.get(85), ScaleToScreen.get(700), ScaleToScreen.get(325), ScaleToScreen.get(75), new Color4f("#50c878"), 32);
        
		// Масштабируем параметры (X, Y, ширина и высота)
		float[] scaledParams = ScaleToScreen.getScaledParams(980, 1020, 1, 30);

		// Теперь используем масштабированные параметры для отрисовки текста
		FontManager.getFont("BRIANNETOD").drawTextWithShadow(
			Localization.get("SettingScreen.name"),  // Текст
		    (int) ScaleToScreen.get(50),  // Масштабированный размер шрифта
		    scaledParams[0],  // Масштабированная X-координата
		    scaledParams[1],  // Масштабированная Y-координата
		    0,  // Угол наклона (если нужно)
		    new Color4f("#FFFFFF"),  // Цвет текста
		    0.1f,  // Масштабирование для тени (если нужно)
		    0.1f,  // Масштабирование для тени (если нужно)
		    0,  // Дополнительные параметры для тени (если нужно)
		    true,  // Отображать тень или нет
		    0.1f,  // Масштабирование тени по оси X
		    0.1f,  // Масштабирование тени по оси Y
		    new Color4f("#000000")  // Цвет тени
		);
        
	}
	
	@Override
	public void cleanup() {}

	@Override
	public void input(boolean isKeyPressed) {}

	@Override
	public void onButtonClick(int id) {
	       switch (id) {
           case 0:
        	   WindowManager.getAL().getSoundEffectManager().playSoundEffect("buttonclick");
               	WindowManager.SwitchToScreen(new MainMenuScreen());
               break;
               
           case 1:
        	   WindowManager.getAL().getSoundEffectManager().playSoundEffect("ceramic");
              	WindowManager.toggleFullscreen();
              break;
              
           case 2: // Логика переключения автоподбора
        	   WindowManager.getAL().getSoundEffectManager().playSoundEffect("ceramic");
               boolean hasExecuted = false;
               boolean currentAutoSunSelection = SettingsManager.isAutoSunSelectionEnabled();
               System.out.println("Текущее состояние AutoSunSelection: " + currentAutoSunSelection);
               if (!hasExecuted) {
                   SettingsManager.setAutoSunSelectionEnabled(!currentAutoSunSelection); 
                   System.out.println("Новое состояние AutoSunSelection: " + !currentAutoSunSelection);
                   hasExecuted = true; 
               }
               break;
           default:
               break;
       }
	}

	@Override
	public void update(float delta) {
         super.update(delta);
	}
	
}
