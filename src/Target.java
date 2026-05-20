import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Target {
    // 3D positional properties
    public double x = 0; 
    public double y = 0;
    public static final double DISTANCE_Z = 50.0; 
    public double radius = 120.0; 

    private double destX = 0;
    private double destY = 0;
    private double speed = 3.5; 
    private boolean isMovingEnabled = false;
    private Random random = new Random();

    private List<HitPoint> hits = new ArrayList<>();

    private static class HitPoint {
        double relX, relY; 
        HitPoint(double relX, double relY) {
            this.relX = relX;
            this.relY = relY;
        }
    }

    public Target() {
        pickNewDestination();
    }

    public void setMovementEnabled(boolean enabled) {
        this.isMovingEnabled = enabled;
        if (enabled) pickNewDestination();
    }

    public void resetPosition() {
        this.x = 0;
        this.y = 0;
    }

    private void pickNewDestination() {
        // Keeps destinations completely aligned within the environment's boundary limits
        this.destX = -180.0 + (random.nextDouble() * 360.0);
        this.destY = -120.0 + (random.nextDouble() * 200.0);
    }

    public void update() {
        if (!isMovingEnabled) return;
        double dx = destX - x;
        double dy = destY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            x = destX;
            y = destY;
            pickNewDestination();
        } else {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }

    public int calculateScore(double arrowX, double arrowY) {
        double dx = arrowX - x;
        double dy = arrowY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist <= radius * 0.2) return 10;
        if (dist <= radius * 0.4) return 8;
        if (dist <= radius * 0.6) return 6;
        if (dist <= radius * 0.8) return 4;
        if (dist <= radius) return 2;
        return 0;
    }

    public void addHit(double arrowX, double arrowY) {
        // Keeps the green arrow impact dots locked relative to the moving board center
        hits.add(new HitPoint(arrowX - x, arrowY - y));
    }

    public void clearHits() {
        hits.clear();
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        // Safely converts simulation units to window space coordinates
        int cx = screenWidth / 2 + (int)(x * perspectiveScale);
        int cy = screenHeight / 2 - 100 + (int)(y * perspectiveScale);
        int r = (int)(radius * perspectiveScale);

        // Draw Target Ring Layers with exact color matching rules
        Color[] rings = {Color.WHITE, Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW};
        for (int i = rings.length - 1; i >= 0; i--) {
            int currentRadius = r * (i + 1) / rings.length;
            g.setColor(rings[i]);
            g.fillOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
        }

        // Draw green impact points relative to target center position
        g.setColor(new Color(50, 255, 50)); 
        for (HitPoint hit : hits) {
            int hx = cx + (int)(hit.relX * perspectiveScale);
            int hy = cy + (int)(hit.relY * perspectiveScale);
            g.fillOval(hx - 4, hy - 4, 8, 8);
            g.setColor(Color.BLACK);
            g.drawOval(hx - 4, hy - 4, 8, 8);
        }
    }
}
