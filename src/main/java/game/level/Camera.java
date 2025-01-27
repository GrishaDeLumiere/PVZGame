package game.level;

import org.lwjgl.opengl.GL11;

public class Camera {
    
    private float x, y;
    private float width;
    private float speed = 550;
    private boolean isMoving = true;

    public Camera(float width, float height) {
        this.x = 0;
        this.y = 0;
        this.width = width;
    }

    public void update(float deltaTime) {}

    public void moveSmoothly(float targetX, float deltaTime) {
        // Двигаем камеру в сторону targetX
        if (x < targetX) {
            x += speed * deltaTime;
            if (x > targetX) {
                x = targetX;
                isMoving = false;
            }
        }
        if (x > targetX) {
            x -= speed * deltaTime;
            if (x < targetX) {
                x = targetX;
                isMoving = false;
            }
        }
    }

    public boolean isCameraStopped() {
        return !isMoving;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void apply() {
        GL11.glTranslatef(-x, 0, 0);
    }

    public float getWidth() {
        return width;
    }

    public float getPositionX() {
        return x;
    }
}