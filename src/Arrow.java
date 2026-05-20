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

        // Your initial exact trajectory velocity mapping
        this.vx = v0 * Math.cos(phi) * Math.sin(theta);
        this.vy = v0 * Math.sin(phi); 
        this.vz = v0 * Math.cos(phi) * Math.cos(theta);
    }

    public void update(Wind wind) {
        if (isStuck) return;
        
        flightTime += 0.016; 
        double aWind = wind.getWindForce() * WIND_ACCEL_FACTOR;

        // Your initial exact position formulas
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
        // FIXED: Removed the early return statement that was blocking stuck arrows from drawing.
        // This now projects the final coordinates perfectly into screen space.
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100;
        
        int tipScreenX = cx + (int)(x * perspectiveScale);
        int tipScreenY = cy + (int)(y * perspectiveScale);
        
        int markerSize = Math.max(6, (int)(10 * perspectiveScale));
        
        // Dark background hole puncture
        g.setColor(new Color(20, 20, 20, 180));
        g.fillOval(tipScreenX - markerSize / 2, tipScreenY - markerSize / 2, markerSize, markerSize);
        
        // Bright red center impact marker
        g.setColor(new Color(255, 50, 50)); 
        g.fillOval(tipScreenX - markerSize / 4, tipScreenY - markerSize / 4, markerSize / 2, markerSize / 2);
    }
}
