import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class Target {
    public double x = 0; 
    public double y = 0;
    public static final double DISTANCE_Z = 50.0; 
    public double radius = 8.0; 

    private List<HitPoint> hits = new ArrayList<>();

    private static class HitPoint {
        double x, y;
        HitPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public Target() {}

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
        hits.add(new HitPoint(arrowX, arrowY));
    }

    public void clearHits() {
        hits.clear();
    }

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2 + (int)(x * perspectiveScale);
        int cy = screenHeight / 2 - 100 + (int)(y * perspectiveScale);
        int r = (int)(radius * perspectiveScale);

        Color[] rings = {Color.WHITE, Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW};
        for (int i = rings.length - 1; i >= 0; i--) {
            int currentRadius = r * (i + 1) / rings.length;
            g.setColor(rings[i]);
            g.fillOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);
        }

        g.setColor(new Color(50, 255, 50)); 
        for (HitPoint hit : hits) {
            int hx = screenWidth / 2 + (int)(hit.x * perspectiveScale);
            int hy = screenHeight / 2 - 100 + (int)(hit.y * perspectiveScale);
            g.fillOval(hx - 4, hy - 4, 8, 8);
            g.setColor(Color.BLACK);
            g.drawOval(hx - 4, hy - 4, 8, 8);
        }
    }
}
