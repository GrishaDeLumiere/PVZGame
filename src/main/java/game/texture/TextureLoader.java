package game.texture;

public class TextureLoader {

    public static void loadTextures() {
    	TextureManager.loadTexture("shadow", "textures/entity/shadow.png");
        TextureManager.loadTextureRange("Zombie", 1, 178, "textures/entity/zombie"); 
        TextureManager.loadTextureRange("Sun", 1, 13, "textures/entity/Sun"); 
        TextureManager.loadTextureRange("LawnMower", 1, 17, "textures/entity/LawnMower");
        TextureManager.loadTextureRange("PeaShooterSingle", 1, 25, "textures/Plant/PeaShooterSingle");
        
        TextureManager.loadTexture("Projectile_star", "textures/Projectile/Projectile_star.png");
        TextureManager.loadTexture("ProjectileCactus", "textures/Projectile/ProjectileCactus.png");
        TextureManager.loadTexture("ProjectilePea", "textures/Projectile/ProjectilePea.png");
        TextureManager.loadTexture("ProjectileSnowPea", "textures/Projectile/ProjectileSnowPea.png");
    }
	
}
