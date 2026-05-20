import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Arrow {
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public double vx = 0;
    public double vy = 0;
    public double vz = 0;
    
    // Physics constants
    private static final double GRAVITY = 0.05; // Added subtle gravity drop for depth realism
    private static final double WIND_EFFECT = 0.005; // Balanced scaling for 60 FPS updates

    public void update(Wind wind) {
        // Apply physics velocities
        x += vx;
        y += vy;
        z += vz;
        
        // Apply constant gravity drop over flight duration
        vy += GRAVITY;
        
        // Smoothly apply wind push across time frames
        vx += wind.getForce() * WIND_EFFECT;
    }
    
    public void reset() {
        x = 0;
        y = 0;
        z = 0;
        vx = 0;
        vy = 0;
        vz = 0;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100;
        
        // Project tail (back) of the arrow to 2D screen
        int tailScreenX = cx + (int)(x * perspectiveScale);
        int tailScreenY = cy + (int)(y * perspectiveScale);
        
        // Calculate the position of the tip of the arrow in 3D
        double arrowLength3D = 60.0;
        double tipZ = z + arrowLength3D;
        double tipScale = 1000.0 / Math.max(1, tipZ);
        
        // Project tip to 2D screen
        int tipScreenX = cx + (int)(x * tipScale);
        int tipScreenY = cy + (int)(y * tipScale);
        
        // Draw the shaft
        g.setColor(new Color(200, 200, 200)); // Silver/carbon fiber arrow shaft
        g.setStroke(new BasicStroke(Math.max(2, (int)(5 * perspectiveScale)), BasicStroke.CAP_ROUND, BasicStroke.
