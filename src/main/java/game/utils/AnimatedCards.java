package game.utils;

public class AnimatedCards {
    private Object object;
    private float startX, startY, endX, endY;
    private long duration, startTime;

    public AnimatedCards(Object object, float startX, float startY, float endX, float endY, long duration) {
        this.object = object;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public Object getObject() {
        return object;
    }
    public boolean isFinished() {
        return System.currentTimeMillis() > startTime + duration;
    }

    public float getCurrentX() {
        float progress = Math.min(1.0f, (System.currentTimeMillis() - startTime) / (float) duration);
        return startX + progress * (endX - startX);
    }

    public float getCurrentY() {
        float progress = Math.min(1.0f, (System.currentTimeMillis() - startTime) / (float) duration);
        return startY + progress * (endY - startY);
    }
}
