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
    
    private boolean isStuck = false;
    private static final double GRAVITY = 0.05; 
    private static final double WIND_EFFECT = 0.005; 

    public void update(Wind wind) {
        if (isStuck) return;
        
        x += vx;
        y += vy;
        z += vz;
        vy += GRAVITY;
        vx += wind.getForce() * WIND_EFFECT;
    }
    
    public void setStuck(boolean stuck) {
        this.isStuck = stuck;
    }
    
    public void reset() {
        x = 0;
        y = 0;
        z = 0;
        vx = 0;
        vy = 0;
        vz = 0;
        isStuck = false;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100;
        
        int tailScreenX = cx + (int)(x * perspectiveScale);
        int tailScreenY = cy + (int)(y * perspectiveScale);
        
        double arrowLength3D = isStuck ? 20.0 : 60.0;
        double tipZ = z + arrowLength3D;
        double tipScale = 600.0 / Math.max(1, tipZ);
        
        int tipScreenX = cx + (int)(x * tipScale);
        int tipScreenY = cy + (int)(y * tipScale);
        
        g.setColor(new Color(200, 200, 200)); 
        g.setStroke(new BasicStroke(Math.max(2, (int)(5 * perspectiveScale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(tailScreenX, tailScreenY, tipScreenX, tipScreenY);
        
        int fletchSize = Math.max(3, (int)(12 * perspectiveScale));
        g.setColor(new Color(220, 50, 50, 200)); 
        
        double dx = tipScreenX - tailScreenX;
        double dy = tipScreenY - tailScreenY;
        double len = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        
        double nx = -dy / len;
        double ny = dx / len;
        
        int fletchLeftX = (int)(tailScreenX + nx * fletchSize);
        int fletchLeftY = (int)(tailScreenY + ny * fletchSize);
        int fletchRightX = (int)(tailScreenX - nx * fletchSize);
        int fletchRightY = (int)(tailScreenY - ny * fletchSize);
        
        g.fillPolygon(
            new int[]{tailScreenX, fletchLeftX, (int)(tailScreenX + (dx / len) * fletchSize)},
            new int[]{tailScreenY, fletchLeftY, (int)(tailScreenY + (dy / len) * fletchSize)},
            3
        );
        g.fillPolygon(
            new int[]{tailScreenX, fletchRightX, (int)(tailScreenX + (dx / len) * fletchSize)},
            new int[]{tailScreenY, fletchRightY, (int)(tailScreenY + (dy / len) * fletchSize)},
            3
        );
        
        if (isStuck) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillOval(tipScreenX - 3, tipScreenY - 3, 6, 6);
        }
        
        g.setStroke(new BasicStroke(1));
    }
}
