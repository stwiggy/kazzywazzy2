import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Arrow {
    // Current positions: x=x_world, y=y_world, z=z_world
    public double x = 0;
    public double y = 0;
    public double z = 0;

    // Initial velocity components calculated at release
    private double vx = 0;
    private double vy = 0;
    private double vz = 0;
    
    // Physics Constants derived from your formulas
    private static final double K = 12.0;       // Bow string constant (k)
    private static final double M = 0.05;       // Weight of arrow in kg (m)
    private static final double G = 9.8;        // Gravity constant (g)
    private static final double WIND_EFFECT = 0.08;

    private double flightTime = 0;              // Elapsed time 't' since release
    private boolean isStuck = false;

    public void launch(double drawDistance, double targetX, double targetY) {
        reset();

        // 1. Calculate energy: E_bow = 0.5 * k * x^2
        double eBow = 0.5 * K * (drawDistance * drawDistance);

        // 2. Calculate initial velocity magnitude: vi = sqrt(2 * E / m)
        double v0 = Math.sqrt((2.0 * eBow) / M);

        // 3. Determine launch angles phi and theta based on target aiming point from center
        // In our viewport, depth to target is Target.DISTANCE_Z
        double distanceToTarget3D = Math.sqrt(targetX * targetX + targetY * targetY + Target.DISTANCE_Z * Target.DISTANCE_Z);
        
        // phi: vertical elevation angle
        double phi = Math.asin(targetY / distanceToTarget3D);
        // theta: horizontal panning angle
        double theta = Math.atan2(targetX, Target.DISTANCE_Z);

        // 4. Deconstruct into vector trajectories based on your spherical coordinate formulas
        // Adjusted axes to fit project canvas orientation (Z is deep, Y is altitude)
        this.vx = v0 * Math.cos(phi) * Math.sin(theta);
        this.vy = v0 * Math.sin(phi); 
        this.vz = v0 * Math.cos(phi) * Math.cos(theta);
    }

    public void update(Wind wind) {
        if (isStuck) return;
        
        // Advance flight time 't' (scaled for frame-rate step conversion)
        flightTime += 0.016; 

        // Apply your kinematic equations:
        // x = vx * t
        // y = vy * t - 0.5 * g * t^2  (Note: we add wind acceleration directly to X trajectory)
        x = (vx * flightTime) + (wind.getForce() * WIND_EFFECT * flightTime);
        y = (vy * flightTime) + (0.5 * G * flightTime * flightTime); // '+' because down is positive in Java 2D layouts
        z = (vz * flightTime);
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
