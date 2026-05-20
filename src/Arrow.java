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
    
    private static final double K = 150.0;       
    private static final double M = 0.05;       
    private static final double G = 9.8;        
    private static final double WIND_ACCEL_FACTOR = 0.5; 

    private double flightTime = 0;              
    private boolean isStuck = false;

    public void launch(double drawDistance, double targetX, double targetY) {
        reset();

        double eBow = 0.5 * K * (drawDistance * drawDistance);
        double v0 = Math.sqrt((2.0 * eBow) / M);
        double distanceToTarget3D = Math.sqrt(targetX * targetX + targetY * targetY + Target.DISTANCE_Z * Target.DISTANCE_Z);
        
        double phi = Math.asin(targetY / distanceToTarget3D);
        double theta = Math.atan2(targetX, Target.DISTANCE_Z);

        this.vx = v0 * Math.cos(phi) * Math.sin(theta);
        this.vy = v0 * Math.sin(phi); 
        this.vz = v0 * Math.cos(phi) * Math.cos(theta);
    }

    public void update(Wind wind) {
        if (isStuck) return;
        
        flightTime += 0.016; 
        double aWind = wind.getWindForce() * WIND_ACCEL_FACTOR;

        x = (vx * flightTime) + (0.5 * aWind * flightTime * flightTime);
        y = (vy * flightTime) + (0.5 * G * flightTime * flightTime); 
        z = (vz * flightTime);
    }
    
    public void setStuck(boolean stuck) {
        this.isStuck = stuck;
    }
    
    public void reset() {
        x = 0; y = 0; z = 0;
        vx = 0; vy = 0; vz = 0;
        flightTime = 0;
        isStuck = false;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100;
        
        int tailScreenX = cx + (int)(x * perspectiveScale);
        int tailScreenY = cy + (int)(y * perspectiveScale);
        
        double arrowLength3D = isStuck ? 15.0 : 50.0;
        double tipZ = z + arrowLength3D;
        double tipScale = 600.0 / Math.max(1, tipZ);
        
        int tipScreenX = cx + (int)(x * tipScale);
        int tipScreenY = cy + (int)(y * tipScale);
        
        g.setColor(new Color(200, 200, 200)); 
        g.setStroke(new BasicStroke(Math.max(2, (int)(4 * perspectiveScale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(tailScreenX, tailScreenY, tipScreenX, tipScreenY);
        
        int fletchSize = Math.max(3, (int)(10 * perspectiveScale));
        g.setColor(new Color(220, 50, 50, 200)); 
        
        double dx = tipScreenX - tailScreenX;
        double dy = tipScreenY - tailScreenY;
        double len = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        
        double nx = -dy / len;
        double ny = dx / len;
        
        g.fillPolygon(
            new int[]{tailScreenX, (int)(tailScreenX + nx * fletchSize), (int)(tailScreenX + (dx / len) * fletchSize)},
            new int[]{tailScreenY, (int)(tailScreenY + ny * fletchSize), (int)(tailScreenY + (dy / len) * fletchSize)},
            3
        );
        g.fillPolygon(
            new int[]{tailScreenX, (int)(tailScreenX - nx * fletchSize), (int)(tailScreenX + (dx / len) * fletchSize)},
            new int[]{tailScreenY, (int)(tailScreenY - ny * fletchSize), (int)(tailScreenY + (dy / len) * fletchSize)},
            3
        );
        
        if (isStuck) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillOval(tipScreenX - 3, tipScreenY - 3, 6, 6);
        }
        g.setStroke(new BasicStroke(1));
    }
}
