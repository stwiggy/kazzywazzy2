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
    private static final double GRAVITY = 0.0; // Set to 0 so the arrow flies straight
    private static final double WIND_EFFECT = 0.1;

    public void update(Wind wind) {
        // Apply velocities
        x += vx;
        y += vy;
        z += vz;
        
        // Apply gravity
        vy += GRAVITY;
        
        // Apply wind (pushes horizontally)
        vx += wind.getWindForce() * WIND_EFFECT;
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
        g.setStroke(new BasicStroke(Math.max(2, (int)(5 * perspectiveScale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(tailScreenX, tailScreenY, tipScreenX, tipScreenY);
        
        // Draw fletchings (more realistic polygons)
        int fletchSize = Math.max(3, (int)(15 * perspectiveScale));
        g.setColor(new Color(200, 50, 50, 200)); // Translucent red
        
        // Offset a bit from the very back
        int fletchStartX = tailScreenX + (tipScreenX - tailScreenX) / 10;
        int fletchStartY = tailScreenY + (tipScreenY - tailScreenY) / 10;
        
        g.fillPolygon(new int[]{fletchStartX, tailScreenX, tailScreenX - fletchSize}, 
                      new int[]{fletchStartY, tailScreenY, tailScreenY - fletchSize}, 3);
        g.fillPolygon(new int[]{fletchStartX, tailScreenX, tailScreenX + fletchSize}, 
                      new int[]{fletchStartY, tailScreenY, tailScreenY - fletchSize}, 3);
        g.fillPolygon(new int[]{fletchStartX, tailScreenX, tailScreenX}, 
                      new int[]{fletchStartY, tailScreenY, tailScreenY + fletchSize}, 3);

        // Arrowhead (if tip is visible, draw a small point)
        int headSize = Math.max(2, (int)(8 * tipScale));
        g.setColor(Color.DARK_GRAY);
        g.fillPolygon(new int[]{tipScreenX, tipScreenX - headSize, tipScreenX + headSize}, 
                      new int[]{tipScreenY + headSize, tipScreenY - headSize, tipScreenY - headSize}, 3);

        // Nock
        g.setColor(Color.WHITE);
        int nockSize = Math.max(3, (int)(6 * perspectiveScale));
        g.fillOval(tailScreenX - nockSize/2, tailScreenY - nockSize/2, nockSize, nockSize);
        
        g.setStroke(new BasicStroke(1));
    }
}
