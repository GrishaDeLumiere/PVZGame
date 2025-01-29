package game.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import game.Localization;
import game.WindowManager;
import game.entity.Entity;
import game.entity.EntityZombie;
import game.font.Color4f;
import game.font.FontManager;
import game.font.FontTT;
import game.texture.TextureManager;

public class AlmanacScreenZombie extends AbstractScreen {
	
	private List<Entity> entities = new ArrayList<>();
	private Entity selectedEntity = null; // Выбранный моб
	
	public AlmanacScreenZombie() {
		WindowManager.getAL().getMusicManager().loadAndPlayBackgroundMusic("music/SelectCard.wav");
		
		TextureManager.loadTexture("Almanac_ZombieBack", "textures/Almanac/Almanac_ZombieBack.png");
		TextureManager.loadTexture("Almanac_ZombieCard", "textures/Almanac/Almanac_ZombieCard.png");
		TextureManager.loadTexture("Almanac_ZombieWindow2", "textures/Almanac/Almanac_ZombieWindow2.png");
		TextureManager.loadTexture("Almanac_ZombieWindow", "textures/Almanac/Almanac_ZombieWindow.png");
		TextureManager.loadTexture("Almanac_GroundDay", "textures/Almanac/Almanac_GroundDay.png");
		
	    entities.add(new EntityZombie(0, 0));
	    
	}
	
	@Override
	public void render() {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
		TextureManager.drawFullScreenTexture("Almanac_ZombieBack");
		
		drawButtonWithText(0, "Almanac_CloseButton", "Almanac_CloseButtonHighlight", Localization.get("button.back"), "FBUSV8C5EI", ScaleToScreen.getRight(1615), ScaleToScreen.get(35), ScaleToScreen.get(220), ScaleToScreen.get(50), new Color4f("#FFFFFF"), 32);

		float[] scaledParams = ScaleToScreen.getScaledParams(980, 1030, 1, 30); 
		FontManager.getFont("BRIANNETOD").drawTextWithShadow(Localization.get("AlmanacScreenZombie.name"), (int) ScaleToScreen.get(50), scaledParams[0], scaledParams[1], 0, new Color4f("#26C400"), 0.1f, 0.1f, 0, true, 0.1f, 0.1f, new Color4f("#000000"));
        
        
        
        GL11.glPopMatrix();
        drawEntityList();
	}

	public void drawEntityList() {
	    float startX = 120; // Начальная позиция по X
	    float startY = 700; // Начальная позиция по Y
	    float buttonWidth = 150; // Ширина кнопки
	    float buttonHeight = 150; // Высота кнопки
	    float spacingX = 25; // Отступ между кнопками по X
	    float spacingY = 25; // Отступ между кнопками по Y

	    int screenWidth = ScaleToScreen.screenWidth; // Текущая ширина экрана
	    //int screenHeight = ScaleToScreen.screenHeight; // Текущая высота экрана
	    int panelWidth = (int) ScaleToScreen.get(1200); // Ширина панели, которую не должны превышать кнопки
	    int baseScreenWidth = 1920; // Базовая ширина экрана для 6 кнопок
	    int defaultButtonsPerRow = 6; // Количество кнопок на Full HD экране

	    // Динамическое вычисление количества кнопок в ряду с учетом масштаба
	    int buttonsPerRow = (int)(defaultButtonsPerRow * ((float)screenWidth / baseScreenWidth));
	    buttonsPerRow = Math.max(1, buttonsPerRow); // Гарантия хотя бы одной кнопки в ряду

	    // Учитываем максимальное количество кнопок по ширине экрана
	    int maxButtonsPerRow = (int)((screenWidth - startX) / (buttonWidth + spacingX));
	    buttonsPerRow = Math.min(buttonsPerRow, maxButtonsPerRow);

	    int buttonId = 1; // ID кнопок начинается с 1 (0 для "Назад")

	    float x = startX;
	    float y = startY;

	    for (Entity entity : entities) {
	        // Проверяем, не выходит ли кнопка за правую границу панели
	        if (x + buttonWidth > startX + panelWidth) {
	            // Если выходит за пределы панели, переносим на следующую строку
	            x = startX; // Сброс по X
	            y -= buttonHeight + spacingY; // Смещение по Y

	            // Проверяем, не выходит ли кнопка за нижнюю границу экрана
	            if (y - buttonHeight < 0) {
	                break; // Прерываем цикл, если кнопки не помещаются по Y
	            }
	        }

	        // Рисуем фон кнопки с учетом масштаба
	        TextureManager.drawTexture("Almanac_ZombieWindow", 
	            ScaleToScreen.get(x), ScaleToScreen.get(y), 
	            ScaleToScreen.get(buttonWidth), ScaleToScreen.get(buttonHeight));

	        GL11.glEnable(GL11.GL_SCISSOR_TEST); // Включаем обрезку

	        // Устанавливаем область обрезки с учетом масштаба
	        GL11.glScissor(
	            (int)ScaleToScreen.get(x - 5), // Начальная координата X (сдвиг немного)
	            (int)ScaleToScreen.get(y + 20), // Начальная координата Y (сдвиг немного)
	            (int)ScaleToScreen.get(200), // Ширина обрезаемой области
	            (int)ScaleToScreen.get(260)  // Высота обрезаемой области
	        );

	        TextureManager.drawTexture(entity.getAnimation().getFrameByIndex(0), 
	            ScaleToScreen.get(x + 27), ScaleToScreen.get(y - 18), 
	            ScaleToScreen.get(100), ScaleToScreen.get(150));

	        GL11.glDisable(GL11.GL_SCISSOR_TEST); // Выключаем обрезку

	        // Рисуем кнопку с учетом масштаба
	        drawButton(buttonId, "Almanac_ZombieWindow2", "Almanac_ZombieWindow2", 
	            ScaleToScreen.get(x), ScaleToScreen.get(y), 
	            ScaleToScreen.get(buttonWidth), ScaleToScreen.get(buttonHeight));

	        // Смещаем по X с учетом масштаба
	        x += buttonWidth + spacingX;

	        buttonId++;
	    }

	    // Если выбран моб, отрисовываем его справа
	    if (selectedEntity != null) {
	        // Отрисовка тени
	    	TextureManager.drawTexture("Almanac_GroundDay", ScaleToScreen.getRight(1349), ScaleToScreen.get(530), ScaleToScreen.get(300), ScaleToScreen.get(300));
	        TextureManager.drawTexture("shadow", ScaleToScreen.getRight(1440), ScaleToScreen.get(550), ScaleToScreen.get(140), ScaleToScreen.get(50));
	    	TextureManager.drawTexture("Almanac_ZombieCard", ScaleToScreen.getRight(1250), ScaleToScreen.get(120), ScaleToScreen.get(500), ScaleToScreen.get(775));
	    	TextureManager.drawTexture(selectedEntity.getAnimation().getCurrentFrame(), ScaleToScreen.getRight(1411), ScaleToScreen.get(560), ScaleToScreen.get(selectedEntity.getWidth()), ScaleToScreen.get(selectedEntity.getHeight()));

	    	drawWrappedText(Localization.get(selectedEntity.name + ".types"), FontManager.getFont("BRIANNETOD"), ScaleToScreen.get(24), ScaleToScreen.get(390), ScaleToScreen.get(425), new Color4f("#002E7E"));
	    	
	    	FontManager.getFont("BRIANNETOD").drawTextWithShadow(Localization.get(selectedEntity.name + ".name"), ScaleToScreen.get(30), ScaleToScreen.getRight(1485), ScaleToScreen.get(485), 0, new Color4f("#0DBF00"), 0.2f, 0.2f, 0, true, 0.1f, 0.1f, new Color4f("#000000"));
	    	FontManager.getFont("BRIANNETOD").drawText(Localization.get("entity.hp")+ ": " + selectedEntity.hp , ScaleToScreen.get(24), ScaleToScreen.getRight(1300), ScaleToScreen.get(395), 0, new Color4f("#000000"), 0.2f, 0.2f, 0, false);
	    	FontManager.getFont("BRIANNETOD").drawText(Localization.get("entity.damage")+ ": " + selectedEntity.damage, ScaleToScreen.get(24), ScaleToScreen.getRight(1300), ScaleToScreen.get(375), 0, new Color4f("#000000"), 0.2f, 0.2f, 0, false);
	    	
	    	drawWrappedText(Localization.get(selectedEntity.name + ".desc"), FontManager.getFont("BRIANNETOD"), ScaleToScreen.get(24), ScaleToScreen.get(390), ScaleToScreen.get(340), new Color4f("#000000"));

	    }
	}
	
	private void drawWrappedText(String description, FontTT font, float fontSize, float maxWidth, float yPosition, Color4f color) {
	    float lineHeight = ScaleToScreen.get(font.getHeight(description, (int) fontSize) * 0.5f); // Коэффициент уменьшения

	    for (String line : wrapText(description, maxWidth, font, fontSize)) {
	        font.drawText(line, fontSize, ScaleToScreen.getRight(1300), yPosition, 0, color, 0, 0, 0, false);
	        yPosition -= lineHeight; // Уменьшаем Y для следующей строки
	    }
	}
	
	public static List<String> wrapText(String text, float maxWidth, FontTT font, float fontSize) {
	    List<String> lines = new ArrayList<>();
	    String[] words = text.split(" ");
	    StringBuilder currentLine = new StringBuilder();

	    for (String word : words) {
	        String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;

	        // Вычисляем ширину строки через FontTT
	        float lineWidth = font.getWidth(testLine, (int) fontSize, false);

	        if (lineWidth > maxWidth) {
	            lines.add(currentLine.toString()); // Сохраняем текущую строку
	            currentLine.setLength(0); // Очищаем строку
	            currentLine.append(word);
	        } else {
	            if (currentLine.length() > 0) {
	                currentLine.append(" ");
	            }
	            currentLine.append(word);
	        }
	    }

	    if (currentLine.length() > 0) {
	        lines.add(currentLine.toString()); // Добавляем последнюю строку
	    }

	    return lines;
	}
	
	@Override
	public void update(float aspect) {
		super.update(aspect);
		 if (selectedEntity != null) {
			 selectedEntity.update(aspect);
		 }
	}
	
	@Override
	public void onButtonClick(int id) {
	       switch (id) {
           case 0:
        	   WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2");
              	WindowManager.CloseOverlayScreen();
               break;
           default: // Выбор моба
               if (id > 0 && id <= entities.size()) {
                   selectedEntity = entities.get(id - 1); // Выбираем моба из списка
                   WindowManager.getAL().getSoundEffectManager().playSoundEffect("tap2"); // Звук выбора
               }
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
