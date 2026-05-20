import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;
import java.awt.Stroke;

public class Target {
    // Distance from the player (Z-axis)
    public static final double DISTANCE_Z = 1400.0;
    
    // Position of the target in the 3D world (relative to player view center)
    public double x = 0;
    public double y = 0;
    
    // 🔴 EDITED SECTION: Reduced radius from 150.0 to 95.0 to force a smaller, distant look
    public double radius = 95.0;
    
    // Colors for the rings (from outside to inside)
    private static final Color[] RING_COLORS = {
        Color.WHITE, Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW
    };

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        // Calculate screen center
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100; // Shifted up slightly for better perspective
        
        // Target screen position
        int screenX = cx + (int)(x * perspectiveScale);
        int screenY = cy + (int)(y * perspectiveScale);
        
        // Target screen radius
        int screenRadius = (int)(radius * perspectiveScale);
        
        // Target stand (tripod)
        g.setColor(new Color(80, 50, 20)); // Dark Wood
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke((float)Math.max(2, 10 * perspectiveScale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Center leg
        g.drawLine(screenX, screenY + screenRadius / 2, screenX, screenHeight);
        // Left leg
        g.drawLine(screenX, screenY + screenRadius / 2, screenX - screenRadius, screenHeight);
        // Right leg
        g.drawLine(screenX, screenY + screenRadius / 2, screenX + screenRadius, screenHeight);
        g.setStroke(oldStroke);

        // Number of rings is 5 main colors, but yellow has an inner 10-point ring
        int numRings = RING_COLORS.length;
        
        // Draw rings from outside in
        for (int i = 0; i < numRings; i++) {
            double ringRadiusScale = 1.0 - (i * 0.2); // 1.0, 0.8, 0.6, 0.4, 0.2
            int currentRadius = (int)(screenRadius * ringRadiusScale);
            if (currentRadius <= 0) continue;
            
            Point2D center = new Point2D.Float(screenX - currentRadius*0.3f, screenY - currentRadius*0.3f);
            float rad = currentRadius * 1.5f;
            Color baseColor = RING_COLORS[i];
            Color highlight = i == 0 ? Color.WHITE : baseColor.brighter();
            Color shadow = baseColor.darker();
            
            if (rad > 0) {
                float[] dist = {0.0f, 1.0f};
                Color[] colors = {highlight, shadow};
                RadialGradientPaint p = new RadialGradientPaint(center, rad, dist, colors);
                g.setPaint(p);
            } else {
                g.setColor(baseColor);
            }
            
            g.fillOval(
                screenX - currentRadius,
                screenY - currentRadius,
                currentRadius * 2,
                currentRadius * 2
            );
            
            // Outline for visibility
            if (i == 0 || i == numRings - 1) {
                g.setColor(new Color(0, 0, 0, 80));
                g.drawOval(
                    screenX - currentRadius,
                    screenY - currentRadius,
                    currentRadius * 2,
                    currentRadius * 2
                );
            }
        }
        
        // Draw inner 10-point ring line
        g.setColor(new Color(0, 0, 0, 100));
        int innerRadius = (int)(screenRadius * 0.1);
        if (innerRadius > 0) {
            g.drawOval(
                screenX - innerRadius,
                screenY - innerRadius,
                innerRadius * 2,
                innerRadius * 2
            );
            // Center cross
            g.drawLine(screenX - 5, screenY, screenX + 5, screenY);
            g.drawLine(screenX, screenY - 5, screenX, screenY + 5);
        }
    }
    
    /**
     * Calculates the score based on the intersection point (impactX, impactY)
     * relative to the target's center.
     */
    public int calculateScore(double impactX, double impactY) {
        double dx = impactX - x;
        double dy = impactY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > radius) {
            return 0; // Missed the target completely
        }
        
        double normalizedDist = distance / radius; // 0.0 at center, 1.0 at edge
        
        if (normalizedDist <= 0.1) return 10;
        if (normalizedDist <= 0.2) return 9;
        if (normalizedDist <= 0.4) return 7; // Red
        if (normalizedDist <= 0.6) return 5; // Blue
        if (normalizedDist <= 0.8) return 3; // Black
        return 1; // White
    }
}
