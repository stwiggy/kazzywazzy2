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
        // 🚫 DON'T SHOW DURING FLIGHT: If it hasn't hit yet, skip drawing completely
        if (!isStuck) {
            return;
        }

        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100;
        
        // Calculate the exact impact point scaled to the perspective
        int tipScreenX = cx + (int)(x * perspectiveScale);
        int tipScreenY = cy + (int)(y * perspectiveScale);
        
        // 🎯 DRAW IMPACT MARK: A bold, distinct cross/dot where the arrow pinned the target
        int markerSize = Math.max(4, (int)(8 * perspectiveScale));
        
        // Draw a small dark shadow backing
        g.setColor(new Color(0, 0, 0, 150));
        g.fillOval(tipScreenX - markerSize / 2, tipScreenY - markerSize / 2, markerSize, markerSize);
        
        // Draw a bright inner core entry hole indicator
        g.setColor(new Color(230, 30, 30)); 
        g.fillOval(tipScreenX - markerSize / 4, tipScreenY - markerSize / 4, markerSize / 2, markerSize / 2);
    }
}
