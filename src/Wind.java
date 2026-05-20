import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

public class Wind {
    private double speed;      // Wind speed magnitude (0.0 to 10.0)
    private double directionX;  // Horizontal factor: -1.0 (Left) to 1.0 (Right)
    private double directionY;  // Vertical factor: -1.0 (Up) to 1.0 (Down) - For Level 3 use
    
    private Random random;

    public Wind() {
        random = new Random();
        randomize();
    }

    /**
     * Randomizes the wind directions and speeds cleanly for active levels.
     */
    public void randomize() {
        // Generates a speed between 2.0 and 8.0 m/s
        this.speed = 2.0 + (random.nextDouble() * 6.0);
        
        // Randomly assign direction: left (-1) or right (1)
        this.directionX = random.nextBoolean() ? 1.0 : -1.0;
        
        // Minor random vertical lift or drag modifier (-0.3 to 0.3) for crosswinds
        this.directionY = -0.3 + (random.nextDouble() * 0.6);
    }
    
    /**
     * Explicitly overrides wind values (crucial for Level 1 reset).
     */
    public void setWindForce(double force) {
        if (force == 0) {
            this.speed = 0.0;
            this.directionX = 0.0;
            this.directionY = 0.0;
        } else {
            this.speed = force;
        }
    }

    /**
     * Returns the horizontal wind acceleration force handled by Arrow.java
     */
    public double getWindForce() {
        return speed * directionX;
    }
    
    /**
     * Returns the vertical wind acceleration force (useful if you want vertical drift in Level 3)
     */
    public double getWindForceY() {
        return speed * directionY;
    }

    public double getSpeed() {
        return speed;
    }

    /**
     * Draws a clean, intuitive wind visual guide at the top right of the screen.
     */
    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        // If there is no wind active, skip rendering entirely
        if (speed == 0.0 || directionX == 0.0) {
            return;
        }

        int uiX = screenWidth - 130;
        int uiY = 45;

        // 1. Draw Background Container Badge
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(uiX - 15, uiY - 25, 125, 60, 15, 15);

        // 2. Draw Text Data
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        String windText = String.format("Wind: %.1f m/s", speed);
        g.drawString(windText, uiX - 5, uiY - 5);

        // 3. Draw Directional Navigation Arrow
        int arrowCenterX = uiX + 45;
        int arrowCenterY = uiY + 18;
        int arrowLength = 25;

        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.CYAN);

        if (directionX > 0) {
            // Blowing Right: Draw line pointing right (--->)
            g.drawLine(arrowCenterX - arrowLength / 2, arrowCenterY, arrowCenterX + arrowLength / 2, arrowCenterY);
            g.drawLine(arrowCenterX + arrowLength / 2 - 5, arrowCenterY - 5, arrowCenterX + arrowLength / 2, arrowCenterY);
            g.drawLine(arrowCenterX + arrowLength / 2 - 5, arrowCenterY + 5, arrowCenterX + arrowLength / 2, arrowCenterY);
        } else {
            // Blowing Left: Draw line pointing left (<---)
            g.drawLine(arrowCenterX + arrowLength / 2, arrowCenterY, arrowCenterX - arrowLength / 2, arrowCenterY);
            g.drawLine(arrowCenterX - arrowLength / 2 + 5, arrowCenterY - 5, arrowCenterX - arrowLength / 2, arrowCenterY);
            g.drawLine(arrowCenterX - arrowLength / 2 + 5, arrowCenterY + 5, arrowCenterX - arrowLength / 2, arrowCenterY);
        }

        // Reset system stroke defaults
        g.setStroke(new BasicStroke(1));
    }
}
