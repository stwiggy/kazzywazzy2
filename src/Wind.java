package src;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.util.Random;

public class Wind {
    private double force; 
    private Random random = new Random();

    public Wind() {
        randomize();
    }

    public void randomize() {
        force = -3.0 + random.nextDouble() * 6.0;
    }

    public double getForce() {
        return force;
    }

    public void draw(Graphics2D g2d, int width, int height) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        
        String windText = "";
        if (force < -0.5) {
            windText = "Wind: <<< Left (" + String.format("%.1f", Math.abs(force)) + " m/s)";
        } else if (force > 0.5) {
            windText = "Wind: Right >>> (" + String.format("%.1f", force) + " m/s)";
        } else {
            windText = "Wind: Calm (0.0 m/s)";
        }
        
        g2d.drawString(windText, width - 260, 40);
    }
}
