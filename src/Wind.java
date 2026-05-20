import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

public class Wind {
    private double speed;        
    private double dirSignX;     
    private double dirSignY;     
    
    private Random random;

    public Wind() {
        random = new Random();
        randomize();
    }

    public void randomize() {
        this.speed = 6.0 + (random.nextDouble() * 10.0);
        int windType = random.nextInt(3);
        
        if (windType == 0) {
            this.dirSignX = random.nextBoolean() ? 1.0 : -1.0;
            this.dirSignY = 0.0;
        } else if (windType == 1) {
            this.dirSignX = 0.0;
            this.dirSignY = random.nextBoolean() ? 1.0 : -1.0;
        } else {
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
            this.speed = Math.abs(force);
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

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(uiX - 10, uiY - 25, 135, 60, 15, 15);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Wind: " + String.format("%.1f", speed) + " m/s", uiX, uiY - 5);

        int cx = uiX + 55;
        int cy = uiY + 18;
        int len = 12;

        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.CYAN);

        int endX = cx + (int)(dirSignX * len);
        int endY = cy + (int)(dirSignY * len);
        int startX = cx - (int)(dirSignX * len);
        int startY = cy - (int)(dirSignY * len);

        g.drawLine(startX, startY, endX, endY);

        double angle = Math.atan2(endY - startY, endX - startX);
        int arrowLength = 6;
        g.drawLine(endX, endY, (int)(endX - arrowLength * Math.cos(angle - 0.5)), (int)(endY - arrowLength * Math.sin(angle - 0.5)));
        g.drawLine(endX, endY, (int)(endX - arrowLength * Math.cos(angle + 0.5)), (int)(endY - arrowLength * Math.sin(angle + 0.5)));

        g.setStroke(new BasicStroke(1));
    }
}
