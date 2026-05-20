import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

public class Wind {
    private double speed;      // Wind speed magnitude
    private double directionX; // -1 for Left, 1 for Right

    private Random random;

    public Wind() {
        random = new Random();
        randomize();
    }

    public void randomize() {
        // Generates a random speed between 0.0 and 4.0
        this.speed = random.nextDouble() * 4.0;
        // Randomly assigns direction: either -1.0 (Left) or 1.0 (Right)
        this.directionX = random.nextBoolean() ? 1.0 : -1.0;
    }

    public double getWindForce() {
        return speed * directionX;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(screenWidth - 180, 10, 160, 70, 15, 15);

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Wind Indicator", screenWidth - 160, 32);

        // Draw wind arrow baseline tracker frame
        int arrowCenterX = screenWidth - 100;
        int arrowCenterY = 60;
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawLine(arrowCenterX - 40, arrowCenterY, arrowCenterX + 40, arrowCenterY);

        // Draw context direction alert indicators
        if (speed > 0.1) {
            g.setColor(Color.RED);
            int arrowLength = (int)(speed * 10);
            if (directionX > 0) {
                // Pointing Right
                g.drawLine(arrowCenterX, arrowCenterY, arrowCenterX + arrowLength, arrowCenterY);
                g.fillPolygon(
                    new int[]{arrowCenterX + arrowLength, arrowCenterX + arrowLength - 6, arrowCenterX + arrowLength - 6},
                    new int[]{arrowCenterY, arrowCenterY - 5, arrowCenterY + 5}, 
                    3
                );
            } else {
                // Pointing Left
                g.drawLine(arrowCenterX, arrowCenterY, arrowCenterX - arrowLength, arrowCenterY);
                g.fillPolygon(
                    new int[]{arrowCenterX - arrowLength, arrowCenterX - arrowLength + 6, arrowCenterX - arrowLength + 6},
                    new int[]{arrowCenterY, arrowCenterY - 5, arrowCenterY + 5}, 
                    3
                );
            }
        } else {
            g.drawString("Calm", arrowCenterX - 18, arrowCenterY + 5);
        }
        g.setStroke(new java.awt.BasicStroke(1));
    }
}
