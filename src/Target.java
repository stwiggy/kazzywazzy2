import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Target {
    // 3D position parameters
    public double x = 0; // Centers around 0 in simulation space
    public double y = 0;
    public static final double DISTANCE_Z = 50.0; 
    public double radius = 120.0; 

    // Dynamic movement state variables
    private double destX = 0;
    private double destY = 0;
    private double speed = 3.5; // Constant movement speed in pixels/frame
    private boolean isMovingEnabled = false;
    private Random random = new Random();

    // Hit-detection storage structures
    private List<HitPoint> hits = new ArrayList<>();

    private static class HitPoint {
        double relX, relY; // Coordinates stored relative to the center of the moving target
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
        if (enabled) {
            pickNewDestination();
        }
    }

    private void pickNewDestination() {
        // Generates reasonable bounding limits so it stays well within the visible field
        this.destX = -180.0 + (random.nextDouble() * 360.0);
        this.destY = -120.0 + (random.nextDouble() * 200.0);
    }

    public void update() {
        if (!isMovingEnabled) return;

        // Calculate vector distance components to target destination
        double dx = destX - x;
        double dy = destY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            // Snap to destination and pick a new point
            x = destX;
            y = destY;
            pickNewDestination();
        } else {
            // Move smoothly along the vector path at a fixed constant speed
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
        // Store coordinates relative to target's center so hits move with it
        hits.add(new HitPoint(arrowX - x, arrowY - y));
    }

    public void clearHits() {
        hits.clear();
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2 + (int)(x * perspectiveScale);
        int cy = screenHeight / 2 - 100 + (int)(y * perspectiveScale);

        int r = (int)(radius * perspectiveScale);

        // Render target ring layers
        Color[] rings = {Color.WHITE, Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW};
        for (int i = rings.length - 1; i >= 0; i--) {
            int currentRadius = r * (i + 1) / rings.length;
            g.setColor(rings[i]);
            g.fillOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
        }

        // Render relative hit marker indicators
        g.setColor(new Color(50, 255, 50)); // Reverted Green impact markers
        for (HitPoint hit : hits) {
            int hx = cx + (int)(hit.relX * perspectiveScale);
            int hy = cy + (int)(hit.relY * perspectiveScale);
            g.fillOval(hx - 4, hy - 4, 8, 8);
            g.setColor(Color.BLACK);
            g.drawOval(hx - 4, hy - 4, 8, 8);
        }
    }
}
