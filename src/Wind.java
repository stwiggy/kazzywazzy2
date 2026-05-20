import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

public class Wind {
    private double speed; // e.g., 0.0 to 10.0
    private double directionX; // -1.0 to 1.0 (Left or Right)
    
    private Random random;

    public Wind() {
        random = new Random();
        randomize();
    }

    public void randomize() {
        // Wind has been removed
        speed = 0.0;
        directionX = 0.0;
    }
    
    public double getWindForce() {
        return speed * directionX;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        // Wind UI is hidden
    }
}
