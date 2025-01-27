package game.keys;

import static org.lwjgl.glfw.GLFW.*;

import game.WindowManager;

public class MouseManager {

    private double mouseX = 0;
    private double mouseY = 0;
    private boolean isLeftMousePressed = false;
    private boolean wasLeftMousePressed = false;
    private boolean leftClicked = false;

    private boolean isRightMousePressed = false;
    private boolean wasRightMousePressed = false;
    private boolean rightClicked = false;

    public MouseManager(long windowHandle) {
        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = WindowManager.displayHeight - ypos;
        });

        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                wasLeftMousePressed = isLeftMousePressed;
                isLeftMousePressed = (action == GLFW_PRESS);
            }
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                wasRightMousePressed = isRightMousePressed;
                isRightMousePressed = (action == GLFW_PRESS);
            }
        });
    }
    
    // Обработка нажатия на кнопки
    public void processClick() {
        // Левый клик
        if (!isLeftMousePressed && wasLeftMousePressed) {
            leftClicked = true; 
        } else {
            leftClicked = false;
        }

        // Правый клик
        if (!isRightMousePressed && wasRightMousePressed) {
            rightClicked = true;
        } else {
            rightClicked = false;
        }

        // Обновляем состояние
        wasLeftMousePressed = isLeftMousePressed;
        wasRightMousePressed = isRightMousePressed;
    }

    // Проверка, наведена ли мышь на объект
    public boolean isHovered(float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height);
    }

    // Проверка клика левой кнопки
    public boolean isLeftClicked(float x, float y, float width, float height) {
        return leftClicked && isHovered(x, y, width, height);
    }

    // Проверка клика правой кнопки
    public boolean isRightClicked(float x, float y, float width, float height) {
        return rightClicked && isHovered(x, y, width, height);
    }

    // Получение координат мыши
    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    // Состояние нажатия левой кнопки
    public boolean isLeftMousePressed() {
        return isLeftMousePressed;
    }

    // Состояние нажатия правой кнопки
    public boolean isRightMousePressed() {
        return isRightMousePressed;
    }

    // Сброс состояния мыши
    public void reset() {
        isLeftMousePressed = false;
        wasLeftMousePressed = false;
        leftClicked = false;

        isRightMousePressed = false;
        wasRightMousePressed = false;
        rightClicked = false;
    }
}