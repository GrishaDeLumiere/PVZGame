package game.gui;

public interface Screen {
	
    void render();
    void cleanup(); 
    void update(float delta);  
    void input(boolean isKeyPressed);
    
}