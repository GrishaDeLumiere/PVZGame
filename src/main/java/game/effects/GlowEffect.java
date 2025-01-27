package game.effects;

public class GlowEffect {
	
    private boolean isGlowing;
    private float glowDuration;
    private float glowTimer;
    private float minColor;
    private float maxColor;

    public GlowEffect(float glowDuration, float minColor, float maxColor) {
        this.glowDuration = glowDuration;
        this.glowTimer = 0.0f;
        this.isGlowing = false;
        this.minColor = minColor;
        this.maxColor = maxColor;
    }

    public void startGlow() {
        this.isGlowing = true;
        this.glowTimer = 0.0f;
    }

    public void update(float deltaTime) {
        if (isGlowing) {
            glowTimer += deltaTime;
            if (glowTimer >= glowDuration) {
                isGlowing = false;
                glowTimer = 0.0f;
            }
        }
    }

    public float getGlowIntensity() {
        float glow = minColor;
        if (isGlowing) {
            glow = minColor + (float) (Math.sin(glowTimer * Math.PI / glowDuration) * (maxColor - minColor));
        }
        return glow;
    }

    public boolean isGlowing() {
        return isGlowing;
    }
}
