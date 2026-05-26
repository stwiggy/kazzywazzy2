import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Target {
    public double x = 0;
    public double y = 0;
    public static final double DISTANCE_Z = 80.0;
    public double radius = 120.0;
    private double vx = 0;
    private double vy = 0;
    private static final double SPEED = 1.5;
    private static final double BOUNDS = 150.0;
    private Random random = new Random();
    private boolean moving = false;

    private List<HitPoint> hits = new ArrayList<>();
    private static class HitPoint {
        double relX, relY; // relative to target center
        HitPoint(double relX, double relY) { this.relX = relX; this.relY = relY; }
    }

    public Target() { randomizeDirection(); }

    private void randomizeDirection() {
        double angle = random.nextDouble() * Math.PI * 2;
        vx = Math.cos(angle) * SPEED;
        vy = Math.sin(angle) * SPEED;
    }

    public void setMoving(boolean moving) { this.moving = moving; }

    public void update() {
        if (!moving) return;
        x += vx;
        y += vy;
        if (x > BOUNDS || x < -BOUNDS) { vx = -vx; randomizeDirection(); vx = Math.abs(vx) * (x > 0 ? -1 : 1); }
        if (y > BOUNDS || y < -BOUNDS) { vy = -vy; randomizeDirection(); vy = Math.abs(vy) * (y > 0 ? -1 : 1); }
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
        // store hit position relative to target center
        hits.add(new HitPoint(arrowX - x, arrowY - y));
    }

    public void clearHits() {
        hits.clear();
        x = 0;
        y = 0;
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2 + (int)(x * perspectiveScale);
        int cy = screenHeight / 2 - 50 + (int)(y * perspectiveScale);
        int r = (int)(radius * perspectiveScale);
        Color[] rings = {Color.YELLOW, Color.RED, Color.BLUE, Color.GREEN, Color.WHITE};
        for (int i = rings.length - 1; i >= 0; i--) {
            int currentRadius = r * (i + 1) / rings.length;
            g.setColor(rings[i]);
            g.fillOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
        }
        for (HitPoint hit : hits) {
            int hx = screenWidth / 2 + (int)((x + hit.relX) * perspectiveScale);
            int hy = screenHeight / 2 - 50 + (int)((y + hit.relY) * perspectiveScale);
            int s = 6;
            g.setColor(Color.BLACK);
            g.setStroke(new java.awt.BasicStroke(3, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.drawLine(hx - s, hy - s, hx + s, hy + s);
            g.drawLine(hx + s, hy - s, hx - s, hy + s);
            g.setStroke(new java.awt.BasicStroke(1));
        }
    }
}
