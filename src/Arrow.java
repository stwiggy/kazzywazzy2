import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Arrow {
    // 🔒 LOCKED BOW POSITION: Arrow world origin points start firmly locked at 0,0,0
    public double x = 0;
    public double y = 0;
    public double z = 0;

    // Initial velocity vector components calculated at release
    private double vx = 0;
    private double vy = 0;
    private double vz = 0;
    
    // ⚡ SPEED CONSTANTS: Heavy bow spring tension constant (k) for rapid arrow speeds
    private static final double K = 850.0;       // Bow string constant (k)
    private static final double M = 0.05;       // Weight of arrow in kg (m)
    private static final double G = 9.8;        // Gravity acceleration constant (g)
    
    // 💨 HIGH WIND DRIFT: Boosted to 5.0 to force major horizontal drifting on fast-flying arrows
    private static final double WIND_ACCEL_FACTOR = 5.0; 

    private double flightTime = 0;              // Elapsed time 't' since release
    private boolean isStuck = false;

    public void launch(double drawDistance, double targetX, double targetY) {
        reset(); // Forces x=0, y=0, z=0 immediately on pull-back release

        // 1. Kinetic energy transition: E_bow = 0.5 * k * x^2
        double eBow = 0.5 * K * (drawDistance * drawDistance);

        // 2. Initial velocity magnitude calculation: vi = sqrt(2 * E / m)
        double v0 = Math.sqrt((2.0 * eBow) / M);

        // 3. Vector Path Aiming: Create a direct line of sight vector from origin toward the cursor location
        double distance3D = Math.sqrt(targetX * targetX + targetY * targetY + Target.DISTANCE_Z * Target.DISTANCE_Z);
        
        // 4. Deconstruct normalized aiming vectors scaled up by total velocity (v0)
        // This locks the bow base to 0,0,0 but pivots its launch trajectory perfectly to your crosshair
        this.vx = v0 * (targetX / distance3D);
        this.vy = v0 * (targetY / distance3D); 
        this.vz = v0 * (Target.DISTANCE_Z / distance3D);
    }

    public void update(Wind wind) {
        if (isStuck) return;
        
        // Advance continuous flight step timer
        flightTime += 0.016; 

        // Crosswind horizontal acceleration calculations
        double aWind = wind.getForce() * WIND_ACCEL_FACTOR;

        // Kinematic trajectory algorithms matching locked coordinates
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
        int cy = screenHeight / 2; // CAMERA REALISM: Center aligned horizon perspective 
        
        int tailScreenX = cx + (int)(x * perspectiveScale);
        int tailScreenY = cy + (int)(y * perspectiveScale);
        
        double arrowLength3D = isStuck ? 12.0 : 45.0;
        double tipZ = z + arrowLength3D;
        double tipScale = 600.0 / Math.max(1, tipZ);
        
        int tipScreenX = cx + (int)(x * tipScale);
        int tipScreenY = cy + (int)(y * tipScale);
        
        g.setColor(new Color(230, 230, 230)); 
        g.setStroke(new BasicStroke(Math.max(2, (int)(3 * perspectiveScale)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(tailScreenX, tailScreenY, tipScreenX, tipScreenY);
        
        int fletchSize = Math.max(2, (int)(8 * perspectiveScale));
        g.setColor(new Color(235, 60, 60, 220)); 
        
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
            g.setColor(new Color(20, 20, 20, 180));
            g.fillOval(tipScreenX - 3, tipScreenY - 3, 6, 6);
        }
        g.setStroke(new BasicStroke(1));
    }
}
