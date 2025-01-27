package game.gui;

import org.lwjgl.opengl.GL11;

import game.WindowManager;
import game.font.Color4f;
import game.font.FontManager;
import game.font.FontTT;
import game.keys.MouseManager;
import game.texture.TextureManager;

public abstract class AbstractScreen implements Screen {

    public abstract void onButtonClick(int id);

    public AbstractScreen() {}

    @Override
    public void render() {

    }   
    
    public void updateDebugSegments() {

    }

    @Override
    public void update(float delta) {

    }

    public void drawButton(int id, String textureNormal, String textureHovered, float x, float y, float width, float height) {
        MouseManager mouseManager = WindowManager.getMouseManager();

        boolean isHovered = mouseManager.isHovered(x, y, width, height);
        boolean isPressed = mouseManager.isLeftMousePressed() && isHovered;

        int offsetX = isPressed ? 5 : 0;
        int offsetY = isPressed ? 5 : 0;

        String buttonImage = isPressed || isHovered ? textureHovered : textureNormal;

        TextureManager.drawTexture(buttonImage, x + offsetX, y + offsetY, width, height);
        if (mouseManager.isLeftClicked(x, y, width, height)) {
            onButtonClick(id);
            mouseManager.reset(); // Сбрасываем состояние после клика
        }
    }
    
    public void drawButtonWithText(int id, String textureNormal, String textureHovered, String text, String font, float x, float y, float width, float height, Color4f textColor, int FontSize) {
        FontTT fontname = FontManager.getFont(font);
        drawButton(id, textureNormal, textureHovered, x, y, width, height);

        if (fontname != null && text != null && !text.isEmpty()) {
            MouseManager mouseManager = WindowManager.getMouseManager();

            boolean isHovered = mouseManager.isHovered(x, y, width, height);
            boolean isPressed = mouseManager.isLeftMousePressed() && isHovered;

            int offsetX = isPressed ? 5 : 0;
            int offsetY = isPressed ? 5 : 0;

            // Центрирование текста по ширине кнопки
            float textX = x + width / 2;

            // Центрирование текста по высоте кнопки
            float textHeight = fontname.getHeight(text, FontSize) * ScaleToScreen.get(1.0f);
            float baselineOffset = fontname.getBaselineOffset(FontSize) * ScaleToScreen.get(1.0f); // Добавлено для учёта базовой линии шрифта
            float textY = y + (height - textHeight) / 2 + baselineOffset;

            // Рисуем текст с учётом всех корректировок
            fontname.drawTextWithShadow(
                    text,
                    (int) ScaleToScreen.get(FontSize),
                    textX + offsetX,
                    textY + offsetY,
                    0,
                    textColor,
                    0,
                    0,
                    0,
                    true,
                    1.0f,
                    1.0f,
                    new Color4f("#000000")
            );
            GL11.glColor4f(1, 1, 1, 1);
        }
    }
    
    public void drawButtonWithText(int id, String textureNormal, String textureHovered, String text, String font, float x, float y, float width, float height, Color4f textColor, int FontSize, float OffsetX) {
        FontTT fontname = FontManager.getFont(font);

        if (fontname != null && text != null && !text.isEmpty()) {
        	 MouseManager mouseManager = WindowManager.getMouseManager();
        	
            boolean isHovered = mouseManager.isHovered(x, y, width, height);
            boolean isPressed = mouseManager.isLeftMousePressed() && isHovered;

            int offsetX = isPressed ? 5 : 0;
            int offsetY = isPressed ? 5 : 0;
            
            String buttonImage = isPressed || isHovered ? textureHovered : textureNormal;

            TextureManager.drawTexture(buttonImage, x + offsetX + OffsetX, y + offsetY, width, height);
            if (mouseManager.isLeftClicked(x, y, width, height)) {
                onButtonClick(id);
                mouseManager.reset(); // Сбрасываем состояние после клика
            }
        	
            float textX = x + width / 2;
            float textHeight = fontname.getHeight(text, FontSize) * ScaleToScreen.get(1.0f);
            float textY = y + (height - textHeight) / 2;

            fontname.drawTextWithShadow(
                    text,
                    (int) ScaleToScreen.get(FontSize), 
                    textX + offsetX + OffsetX,
                    textY + ScaleToScreen.get(29) + offsetY,
                    0,
                    textColor,
                    0,
                    0,
                    0,
                    true,
                    1.0f,
                    1.0f,
                    new Color4f("#000000")
            );
            GL11.glColor4f(1, 1, 1, 1);
        }
    }
    
    public void drawButtonWithText(int id, String textureNormal, String textureHovered, String text, String font, float x, float y, float width, float height, Color4f textColor, int FontSize, float OffsetX, float OffsetY) {
        FontTT fontname = FontManager.getFont(font);

        if (fontname != null && text != null && !text.isEmpty()) {
        	 MouseManager mouseManager = WindowManager.getMouseManager();
        	
            boolean isHovered = mouseManager.isHovered(x, y, width, height);
            boolean isPressed = mouseManager.isLeftMousePressed() && isHovered;

            int offsetX = isPressed ? 5 : 0;
            int offsetY = isPressed ? 5 : 0;
            
            String buttonImage = isPressed || isHovered ? textureHovered : textureNormal;

            TextureManager.drawTexture(buttonImage, x + offsetX + OffsetX, y + offsetY, width, height);
            if (mouseManager.isLeftClicked(x, y, width, height)) {
                onButtonClick(id);
                mouseManager.reset(); // Сбрасываем состояние после клика
            }
        	
            float textX = x + width / 2;
            float textHeight = fontname.getHeight(text, FontSize) * ScaleToScreen.get(1.0f);
            float textY = y + (height - textHeight) / 2;

            fontname.drawTextWithShadow(
                    text,
                    (int) ScaleToScreen.get(FontSize), 
                    textX + offsetX + OffsetX,
                    textY + ScaleToScreen.get(29) + offsetY + OffsetY,
                    0,
                    textColor,
                    0,
                    0,
                    0,
                    true,
                    1.0f,
                    1.0f,
                    new Color4f("#000000")
            );
            GL11.glColor4f(1, 1, 1, 1);
        }
    }
    
}