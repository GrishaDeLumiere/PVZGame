package game.utils;

public class AnimatedObject {
	
    public int currentY;
    public int targetY;
    public int startY;
    public int speed;
    public boolean movingDown; 
    public boolean animationFinished; 

    public AnimatedObject(int startY, int targetY, int speed, boolean movingDown) {
        this.startY = startY;
        this.currentY = startY;
        this.targetY = targetY;
        this.speed = speed;
        this.movingDown = movingDown;
        this.animationFinished = false; 
    }

    public boolean animate() {
        if (animationFinished) {
            return false;
        }

        if (movingDown) {
            if (currentY < targetY) {
                currentY += Math.max(1, (targetY - currentY) / speed);
                return true;
            }
        } else { 
            if (currentY > targetY) {
                currentY -= Math.max(1, (currentY - targetY) / speed);
                return true; 
            }
        }
        currentY = targetY;
        animationFinished = true;
        return false;
    }
}