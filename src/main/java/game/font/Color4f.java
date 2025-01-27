package game.font;


/**
	@author saucecode
	A floating-point Color container written for use with FonTT.java and Texture.java.
	This file is public domain. Use it for whatever.
*/
public class Color4f {
    public static final Color4f WHITE = new Color4f(1.0f, 1.0f, 1.0f, 1.0f);
    public static final Color4f RED = new Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    public static final Color4f GREEN = new Color4f(0.0f, 1.0f, 0.0f, 1.0f);
    public static final Color4f BLUE = new Color4f(0.0f, 0.0f, 1.0f, 1.0f);
    public static final Color4f YELLOW = new Color4f(1.0f, 1.0f, 0.0f, 1.0f);

    private float red, green, blue, alpha;

    public Color4f(float red, float green, float blue, float alpha) {
        setRed(red);
        setBlue(blue);
        setGreen(green);
        setAlpha(alpha);
    }

    public Color4f(float red, float green, float blue) {
        this(red, green, blue, 1.0f);
    }

    // New constructor to support hex codes like #RRGGBB or #RRGGBBAA
    public Color4f(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1); // Remove the '#' character
        }
        
        if (hex.length() == 6) {
            this.red = Integer.parseInt(hex.substring(0, 2), 16) / 255.0f;
            this.green = Integer.parseInt(hex.substring(2, 4), 16) / 255.0f;
            this.blue = Integer.parseInt(hex.substring(4, 6), 16) / 255.0f;
            this.alpha = 1.0f; // Default to 100% opacity
        } else if (hex.length() == 8) {
            this.red = Integer.parseInt(hex.substring(0, 2), 16) / 255.0f;
            this.green = Integer.parseInt(hex.substring(2, 4), 16) / 255.0f;
            this.blue = Integer.parseInt(hex.substring(4, 6), 16) / 255.0f;
            this.alpha = Integer.parseInt(hex.substring(6, 8), 16) / 255.0f;
        } else {
            throw new IllegalArgumentException("Invalid hex color code: " + hex);
        }
    }

    // Getters and setters...
    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}