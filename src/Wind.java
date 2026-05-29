import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

public class Wind {
    private double windSpeed;        
    private double windDirectionX;     
    private double windDirectionY;     
    
    private Random random;

    public Wind() {
        random = new Random();
        randomize();
    }

    public void randomize() {
        this.windSpeed = 6.0 + (random.nextDouble() * 10.0);
        int windType = random.nextInt(3);
        
        if (windType == 0) {
            this.windDirectionX = random.nextBoolean() ? 1.0 : -1.0;
            this.windDirectionY = 0.0;
        } else if (windType == 1) {
            this.windDirectionX = 0.0;
            this.windDirectionY = random.nextBoolean() ? 1.0 : -1.0;
        } else {
            this.windDirectionX = random.nextBoolean() ? 1.0 : -1.0;
            this.windDirectionY = random.nextBoolean() ? 1.0 : -1.0;
        }
    }
    
    public void setWindForce(double force) {
        if (force == 0) {
            this.windSpeed = 0.0;
            this.windDirectionX = 0.0;
            this.windDirectionY = 0.0;
        } else {
            this.windSpeed = Math.abs(force);
        }
    }

    public double getSpeed() { return windSpeed; }
    public double getDirSignX() { return windDirectionX; }
    public double getDirSignY() { return windDirectionY; }

    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        if (windSpeed == 0.0 || (windDirectionX == 0.0 && windDirectionY == 0.0)) {
            return;
        }

        int uiX = screenWidth - 140;
        int uiY = 45;

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(uiX - 10, uiY - 25, 135, 60, 15, 15);

        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Wind: " + String.format("%.1f", windSpeed) + " m/s", uiX, uiY - 5);

        int cx = uiX + 55;
        int cy = uiY + 18;
        int len = 12;

        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.CYAN);

        int endX = cx + (int)(windDirectionX * len);
        int endY = cy + (int)(windDirectionY * len);
        int startX = cx - (int)(windDirectionX * len);
        int startY = cy - (int)(windDirectionY * len);

        g.drawLine(startX, startY, endX, endY);

        double angle = Math.atan2(endY - startY, endX - startX);
        int arrowLength = 6;
        g.drawLine(endX, endY, (int)(endX - arrowLength * Math.cos(angle - 0.5)), (int)(endY - arrowLength * Math.sin(angle - 0.5)));
        g.drawLine(endX, endY, (int)(endX - arrowLength * Math.cos(angle + 0.5)), (int)(endY - arrowLength * Math.sin(angle + 0.5)));

        g.setStroke(new BasicStroke(1));
    }
}
