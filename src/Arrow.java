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
    
    private static final double K = 450.0;       
    private static final double M = 0.02;       
    private static final double G = 9.8;        
    private static final double WIND_ACCEL_FACTOR = 0.5; 

    private double flightTime = 0;              
    private boolean isStuck = false;

    public void launch(double drawDistance, double targetX, double targetY) {
        reset();

        double eBow = 0.5 * K * (drawDistance * drawDistance);
        double v0 = Math.sqrt((2.0 * eBow) / M);
        
        double distanceToTarget3D = Math.sqrt(targetX * targetX + targetY * targetY + Target.DISTANCE_Z * Target.DISTANCE_Z);
        
        this.vx = v0 * (targetX / distanceToTarget3D);
        this.vy = v0 * (targetY / distanceToTarget3D); 
        this.vz = v0 * (Target.DISTANCE_Z / distanceToTarget3D);
    }

    public void update(Wind wind) {
        if (isStuck) return;
        
        flightTime += 0.016; 
        double aWind = wind.getWindForce() * WIND_ACCEL_FACTOR;

        x = (vx * flightTime) + (0.5 * aWind * flightTime * flightTime);
        y = (vy * flightTime) + (0.5 * G * flightTime * flightTime); 
        z = (vz * flightTime);
        
        // Dynamic simulation updates to current instantaneous velocities
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
        flightTime = 0;
        isStuck = false;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        if (isStuck) return;

        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100;

        double currentScale = 600.0 / (600.0 + z);

        int screenX = cx + (int)(x * currentScale);
        int screenY = cy + (int)(y * currentScale);

        double sizeFactor = Math.max(0.1, 1.0 - (z / (Target.DISTANCE_Z * 1.5)));
        int arrowLength = Math.max(6, (int)(80 * sizeFactor));
        int thick = Math.max(1, (int)(4 * sizeFactor));

        // 🎯 TILT CALCULATION:
        // Calculate horizontal deviation angle (yaw) and vertical arc angle (pitch)
        double headingAngle = Math.atan2(vx, vz); 
        double pitchAngle = Math.atan2(vy, vz); 
        
        // Combine into a singular composite rotation angle for screen coordinates
        // We add Math.PI / 2 because our raw arrow line asset points vertically down (0 degrees)
        double totalRotation = headingAngle + pitchAngle + (Math.PI / 2);

        // Save initial graphic context transformations
        AffineTransform savedTransform = g.getTransform();

        // Translate drawing context directly over the tip point, then rotate along heading vector
        g.translate(screenX, screenY);
        g.rotate(totalRotation);

        // 1. Draw Arrow Shaft (drawn relative to the local translated origin 0,0)
        g.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(139, 69, 19)); 
        g.drawLine(0, 0, 0, arrowLength);

        // 2. Draw Fletching / Feathers
        g.setStroke(new BasicStroke(thick + 1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE); 
        g.drawLine(-thick, arrowLength, -thick - 3, arrowLength - 6);
        g.drawLine(thick, arrowLength, thick + 3, arrowLength - 6);

        // 3. Draw Arrow Tip Nock
        g.setColor(Color.DARK_GRAY);
        g.fillOval(-thick, -thick, thick * 2, thick * 2);

        // Restore context state
        g.setTransform(savedTransform);
        g.setStroke(new BasicStroke(1));
    }
}
