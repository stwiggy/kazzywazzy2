import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

public class Wind {
    private double speed;        // ALWAYS positive magnitude (0.0 to 10.0)
    private double dirSignX;     // Direction on X axis: -1.0 (Left), 0.0 (None), 1.0 (Right)
    private double dirSignY;     // Direction on Y axis: -1.0 (Up),   0.0 (None), 1.0 (Down)
    
    private Random random;

    public Wind() {
        random = new Random();
        randomize();
    }

    public void randomize() {
        // Base positive speed calculation
        this.speed = 3.0 + (random.nextDouble() * 6.0);
        
        // Pick a random style of wind: 0 = Horizontal, 1 = Vertical, 2 = Diagonal
        int windType = random.nextInt(3);
        
        if (windType == 0) {
            // Horizontal Only (Left or Right)
            this.dirSignX = random.nextBoolean() ? 1.0 : -1.0;
            this.dirSignY = 0.0;
        } else if (windType == 1) {
            // Vertical Only (Up or Down)
            this.dirSignX = 0.0;
            this.dirSignY = random.nextBoolean() ? 1.0 : -1.0;
        } else {
            // Diagonal (Combines both X and Y vectors)
            this.dirSignX = random.nextBoolean() ? 1.0 : -1.0;
            this.dirSignY = random.nextBoolean() ? 1.0 : -1.0;
        }
    }
    
    public void setWindForce(double force) {
        if (force == 0) {
            this.speed = 0.0;
            this.dirSignX = 0.0;
            this.dirSignY = 0.0;
        } else {
            this.speed = Math.abs(force); // Enforce positive speed rule
        }
    }

    public double getSpeed() {
        return speed;
    }

    public double getDirSignX() {
        return dirSignX;
    }

    public double getDirSignY() {
        return dirSignY;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        if (speed == 0.0 || (dirSignX == 0.0 && dirSignY == 0.0)) {
            return;
        }

        int uiX = screenWidth - 140;
        int uiY = 45;

        // Draw Background container card
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(uiX - 10, uiY - 25, 135, 60, 15, 15);

        // Display strictly positive text string
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString(String.format("Wind: %.1f m/s", speed), uiX, uiY - 5);

        // Draw the directional arrow pointer
        int cx = uiX + 55;
        int cy = uiY + 18;
        int len = 12;

        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.CYAN);

        // Calculate line endpoints using our structural position signs
        int endX = cx + (int)(dirSignX * len);
        int endY = cy + (int)(dirSignY * len);
        int startX = cx - (int)(dirSignX * len);
        int startY = cy - (int)(dirSignY * len);

        // Draw basic layout stem line
        g.drawLine(startX, startY, endX, endY);

        // Draw dynamic arrowhead pointers relative to target direction angles
        double angle = Math.atan2(endY - startY, endX - startX);
        int arrowLength = 6;
        g.drawLine(endX, endY, (int)(endX - arrowLength * Math.cos(angle - 0.5)), (int)(endY - arrowLength * Math.sin(angle - 0.5)));
        g.drawLine(endX, endY, (int)(endX - arrowLength * Math.cos(angle + 0.5)), (int)(endY - arrowLength * Math.sin(angle + 0.5)));

        g.setStroke(new BasicStroke(1));
    }
}
