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

    private static final double K = 200.0;
    private static final double M = 0.02;
    private static final double G = 25.0;
    private static final double WIND_ACCEL_FACTOR = 12.0;
    private double flightTime = 0;
    private boolean isStuck = false;

    // store initial velocity for direction
    private double initVx = 0;
    private double initVy = 0;
    private double initVz = 0;
    private double initSpeed = 0;

    public void launch(double drawDistance, double targetX, double targetY) {
        reset();
        double eBow = 0.5 * K * (drawDistance * drawDistance);
        double v0 = Math.sqrt((2.0 * eBow) / M);

        double distanceToTarget3D = Math.sqrt(targetX * targetX + targetY * targetY + Target.DISTANCE_Z * Target.DISTANCE_Z);

        vx = v0 * (targetX / distanceToTarget3D);
        vy = v0 * (targetY / distanceToTarget3D);
        vz = v0 * (Target.DISTANCE_Z / distanceToTarget3D);

        initVx = vx;
        initVy = vy;
        initVz = vz;
        initSpeed = v0;
    }

    public void update(Wind wind) {
        if (isStuck) return;

        flightTime += 0.026;
        double windSpeed = wind.getSpeed();
        double signX = wind.getDirSignX();
        double signY = wind.getDirSignY();
        double totalWindAcceleration = windSpeed * WIND_ACCEL_FACTOR;

        double driftX = 0.5 * totalWindAcceleration * flightTime * flightTime * signX;
        double driftY = 0.5 * totalWindAcceleration * flightTime * flightTime * signY;
        x = (vx * flightTime) + driftX;
        y = (vy * flightTime) + (0.5 * G * flightTime * flightTime) + driftY;
        z = (vz * flightTime);
    }

    public void setStuck(boolean stuck) { this.isStuck = stuck; }
    public boolean isStuck() { return isStuck; }

    public void reset() {
        x = 0; y = 0; z = 0;
        vx = 0; vy = 0; vz = 0;
        initVx = 0; initVy = 0; initVz = 0; initSpeed = 0;
        flightTime = 0;
        isStuck = false;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        if (isStuck) return;

        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 50;
        int screenX = cx + (int)(x * perspectiveScale);
        int screenY = cy + (int)(y * perspectiveScale);

        // compute current velocity direction including gravity and wind for arrow angle
        double curVx = initVx;
        double curVy = initVy + G * flightTime;
        double curVz = initVz;

        // project 3D velocity onto 2D screen
        double len = Math.sqrt(curVx * curVx + curVy * curVy + curVz * curVz);
        if (len == 0) len = 1;

        int arrowLength = Math.max(10, (int)(80 * (1.0 - (z / Target.DISTANCE_Z))));
        int thick = Math.max(1, (int)(4 * (1.0 - (z / Target.DISTANCE_Z))));

        // direction of arrow on screen
        double screenDirX = curVx / len;
        double screenDirY = curVy / len;

        int tailX = screenX - (int)(screenDirX * arrowLength);
        int tailY = screenY - (int)(screenDirY * arrowLength);

        g.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(139, 69, 19));
        g.drawLine(tailX, tailY, screenX, screenY);

        // fletching at tail
        g.setStroke(new BasicStroke(thick + 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.drawLine(tailX - thick, tailY, tailX - thick - 4, tailY - 8);
        g.drawLine(tailX + thick, tailY, tailX + thick + 4, tailY - 8);

        // tip
        g.setColor(Color.DARK_GRAY);
        g.fillOval(screenX - thick, screenY - thick, thick * 2, thick * 2);

        g.setStroke(new BasicStroke(1));
    }
}
