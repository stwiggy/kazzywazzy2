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
        
        // Increased time step per frame (from 0.016 to 0.026) to make the arrow travel faster
        flightTime += 0.026; 
        
        double horizontalWindForce = wind.getWindForce(); 
        double verticalWindForce = wind.getWindForceY();

        // FIXED: Preserving direction signs explicitly by multiplying the direction sign *after* the squaring operation
        double windSignX = Math.signum(horizontalWindForce);
        double windSignY = Math.signum(verticalWindForce);
        
        double absWindX = Math.abs(horizontalWindForce) * WIND_ACCEL_FACTOR;
        double absWindY = Math.abs(verticalWindForce) * WIND_ACCEL_FACTOR;

        x = (vx * flightTime) + (0.5 * absWindX * flightTime * flightTime * windSignX);
        y = (vy * flightTime) + (0.5 * G * flightTime * flightTime) + (0.5 * absWindY * flightTime * flightTime * windSignY); 
        z = (vz * flightTime);
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

        int screenX = cx + (int)(x * perspectiveScale);
        int screenY = cy + (int)(y * perspectiveScale);

        int arrowLength = Math.max(10, (int)(80 * (1.0 - (z / Target.DISTANCE_Z))));
        int thick = Math.max(1, (int)(4 * (1.0 - (z / Target.DISTANCE_Z))));

        g.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(139, 69, 19)); 
        g.drawLine(screenX, screenY, screenX, screenY + arrowLength);

        g.setStroke(new BasicStroke(thick + 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE); 
        g.drawLine(screenX - thick, screenY + arrowLength, screenX - thick - 4, screenY + arrowLength - 8);
        g.drawLine(screenX + thick, screenY + arrowLength, screenX + thick + 4, screenY + arrowLength - 8);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(screenX - thick, screenY - thick, thick * 2, thick * 2);

        g.setStroke(new BasicStroke(1));
    }
}
