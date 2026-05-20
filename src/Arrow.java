import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Arrow {
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public double vx = 0;
    public double vy = 0;
    public double vz = 0;
    
    // Starting 3D position tracking
    private double startX = 0;
    private double startY = 0;
    private double startZ = 0;
    
    private static final double K = 450.0;       
    private static final double M = 0.02;       
    private static final double G = 9.8;        
    private static final double WIND_ACCEL_FACTOR = 0.5; 

    private double flightTime = 0;              
    private boolean isStuck = false;

    // Modified to take the physical world-space launch origin
    public void launch(double drawDistance, double targetX, double targetY, double launchOriginY) {
        reset();

        double eBow = 0.5 * K * (drawDistance * drawDistance);
        // Base velocity calculation
        double v0 = Math.sqrt((2.0 * eBow) / M);
        
        // Match starting positions
        this.startX = 0;
        this.startY = launchOriginY; 
        this.startZ = 0;

        this.x = startX;
        this.y = startY;
        this.z = startZ;
        
        // Calculate the vector from the bow base directly to the targeted crosshair spot
        double dx = targetX - startX;
        double dy = targetY - startY;
        double dz = Target.DISTANCE_Z - startZ;
        
        double totalDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // Assign proportional directional velocities based on the true muzzle vector
        this.vx = v0 * (dx / totalDistance);
        this.vy = v0 * (dy / totalDistance); 
        this.vz = v0 * (dz / totalDistance);
    }

    public void update(Wind wind) {
        if (isStuck) return;
        
        flightTime += 0.016; 
        double aWind = wind.getWindForce() * WIND_ACCEL_FACTOR;

        // Kinematic equations extending out from the visual launch origin point
        x = startX + (vx * flightTime) + (0.5 * aWind * flightTime * flightTime);
        y = startY + (vy * flightTime) + (0.5 * G * flightTime * flightTime); 
        z = startZ + (vz * flightTime);
        
        vx += aWind * 0.016;
        vy += G * 0.016;
    }
    
    public void setStuck(boolean stuck) {
        this.isStuck = stuck;
    }
    
    public boolean isStuck() {
        return isStuck;
    }
    
    public void reset() {
        x = 0; y = 0; z = 0;
        vx = 0; vy = 0; vz = 0;
        startX = 0; startY = 0; startZ = 0;
        flightTime = 0;
        isStuck = false;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        if (isStuck) return;

        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100;

        // Dynamic scale relative to depth positioning matrix
        double currentScale = 600.0 / (600.0 + z);

        int screenX = cx + (int)(x * currentScale);
        int screenY = cy + (int)(y * currentScale);

        double sizeFactor = Math.max(0.1, 1.0 - (z / (Target.DISTANCE_Z * 1.5)));
        int arrowLength = Math.max(6, (int)(80 * sizeFactor));
        int thick = Math.max(1, (int)(4 * sizeFactor));

        // Calculate travel heading angles
        double headingAngle = Math.atan2(vx, vz); 
        double pitchAngle = Math.atan2(vy, vz); 
        double totalRotation = headingAngle + pitchAngle + (Math.PI / 2);

        AffineTransform savedTransform = g.getTransform();

        g.translate(screenX, screenY);
        g.rotate(totalRotation);

        // Draw Arrow Shaft
        g.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(139, 69, 19)); 
        g.drawLine(0, 0, 0, arrowLength);

        // Draw Fletching / Feathers
        g.setStroke(new BasicStroke(thick + 1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE); 
        g.drawLine(-thick, arrowLength, -thick - 3, arrowLength - 6);
        g.drawLine(thick, arrowLength, thick + 3, arrowLength - 6);

        // Draw Arrow Tip
        g.setColor(Color.DARK_GRAY);
        g.fillOval(-thick, -thick, thick * 2, thick * 2);

        g.setTransform(savedTransform);
        g.setStroke(new BasicStroke(1));
    }
}
