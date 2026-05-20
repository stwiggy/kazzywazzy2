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
        
        // Normalized vector approach guarantees the arrow travels directly towards the crosshair coordinates
        double distanceToTarget3D = Math.sqrt(targetX * targetX + targetY * targetY + Target.DISTANCE_Z * Target.DISTANCE_Z);
        
        this.vx = v0 * (targetX / distanceToTarget3D);
        this.vy = v0 * (targetY / distanceToTarget3D); 
        this.vz = v0 * (Target.DISTANCE_Z / distanceToTarget3D);
    }

    public void update(Wind wind) {
        if (isStuck) return;
        
        flightTime += 0.016; 
        double aWind = wind.getWindForce() * WIND_ACCEL_FACTOR;

        // Your initial exact cumulative trajectory position calculations
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
        // Handled cleanly inside Target rendering pipeline now to completely stop sliding artifacts
    }
}
