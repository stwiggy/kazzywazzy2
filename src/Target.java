import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    private static final double MOVE_SPEED = 1.5;
    private static final double MOVEMENT_BOUNDS = 150.0;
    private Random random = new Random();
    private boolean moving = false;
    private boolean showScores = true;

    private List<HitPoint> hits = new ArrayList<>();
    private static class HitPoint {
        double relX, relY;
        HitPoint(double relX, double relY) { this.relX = relX; this.relY = relY; }
    }

    public Target() { randomizeDirection(); }

    private void randomizeDirection() {
        double angle = random.nextDouble() * Math.PI * 2;
        vx = Math.cos(angle) * MOVE_SPEED;
        vy = Math.sin(angle) * MOVE_SPEED;
    }

    public void setMoving(boolean moving) { this.moving = moving; }
    public void hideScores() { this.showScores = false; }
    public void showScores() { this.showScores = true; }

    public void update() {
        if (!moving) return;
        x += vx;
        y += vy;
        if (x > MOVEMENT_BOUNDS || x < -MOVEMENT_BOUNDS) { vx = -vx; randomizeDirection(); vx = Math.abs(vx) * (x > 0 ? -1 : 1); }
        if (y > MOVEMENT_BOUNDS || y < -MOVEMENT_BOUNDS) { vy = -vy; randomizeDirection(); vy = Math.abs(vy) * (y > 0 ? -1 : 1); }
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

        // Stand legs - only when not moving
        if (!moving) {
            g.setColor(new Color(180, 140, 90));
            g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - r / 3, cy + r, cx - r / 2, cy + r + 60);
            g.drawLine(cx + r / 3, cy + r, cx + r / 2, cy + r + 60);
            g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - r / 2 + 5, cy + r + 40, cx + r / 2 - 5, cy + r + 40);
            g.setStroke(new BasicStroke(1));
        }

        // Rings from outside in: white, black(green), blue, red, yellow(gold)
        Color[] rings = {new Color(255, 215, 80), new Color(210, 55, 55), new Color(50, 120, 200), new Color(80, 80, 80), Color.WHITE};
        int[] scores = {10, 8, 6, 4, 2};

        for (int i = rings.length - 1; i >= 0; i--) {
            int currentRadius = r * (i + 1) / rings.length;
            g.setColor(rings[i]);
            g.fillOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
            g.setColor(new Color(80, 80, 80, 120));
            g.setStroke(new BasicStroke(1));
            g.drawOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
        }

        // Score labels - one per ring, displayed vertically top to bottom
        if (showScores) {
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(8, r / 7)));
            int totalRings = rings.length;
            for (int i = totalRings - 1; i >= 0; i--) {
                int currentRadius = r * (i + 1) / totalRings;
                int prevRadius = i > 0 ? r * i / totalRings : 0;
                int bandMid = (currentRadius + prevRadius) / 2;
                String label = String.valueOf(scores[i]);
                int sw = g.getFontMetrics().stringWidth(label);
                int sh = g.getFontMetrics().getAscent();
                Color textColor = Color.WHITE;
                if (i == totalRings - 1) textColor = new Color(80, 80, 80);
                if (i == 0) textColor = new Color(120, 80, 0);
                g.setColor(textColor);
                // draw single label at top of each band
                g.drawString(label, cx - sw / 2, cy - bandMid + sh / 2);
            }
        }

        // Hit markers
        for (HitPoint hit : hits) {
            int hx = screenWidth / 2 + (int)((x + hit.relX) * perspectiveScale);
            int hy = screenHeight / 2 - 50 + (int)((y + hit.relY) * perspectiveScale);
            int s = 6;
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(hx - s, hy - s, hx + s, hy + s);
            g.drawLine(hx + s, hy - s, hx - s, hy + s);
            g.setStroke(new BasicStroke(1));
        }
    }
}
