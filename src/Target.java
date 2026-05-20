import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;
import java.awt.Stroke;

public class Target {
    // 🎯 RESTORED BASELINE: Depth reset to 1400.0 units down the Z-axis
    public static final double DISTANCE_Z = 1400.0;
    public double x = 0;
    public double y = 0;
    
    // 📏 RESTORED SIZE: Physical width scale reset to 115.0 to match long-range perspective
    public double radius = 115.0;
    
    private static final Color[] RING_COLORS = {
        Color.WHITE, Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW
    };

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100; // Aligned with the original elevated horizon offset
        
        int screenX = cx + (int)(x * perspectiveScale);
        int screenY = cy + (int)(y * perspectiveScale);
        int screenRadius = (int)(radius * perspectiveScale);
        
        // Render target stand structure legs
        g.setColor(new Color(80, 50, 20)); 
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke((float)Math.max(2, 10 * perspectiveScale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        g.drawLine(screenX, screenY + screenRadius / 2, screenX, screenHeight);
        g.drawLine(screenX, screenY + screenRadius / 2, screenX - screenRadius, screenHeight);
        g.drawLine(screenX, screenY + screenRadius / 2, screenX + screenRadius, screenHeight);
        g.setStroke(oldStroke);

        int numRings = RING_COLORS.length;
        
        // Render scoring concentric rings
        for (int i = 0; i < numRings; i++) {
            double ringRadiusScale = 1.0 - (i * 0.2); 
            int currentRadius = (int)(screenRadius * ringRadiusScale);
            if (currentRadius <= 0) continue;
            
            Point2D center = new Point2D.Float(screenX - currentRadius * 0.3f, screenY - currentRadius * 0.3f);
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
            
            g.fillOval(screenX - currentRadius, screenY - currentRadius, currentRadius * 2, currentRadius * 2);
            
            if (i == 0 || i == numRings - 1) {
                g.setColor(new Color(0, 0, 0, 80));
                g.drawOval(screenX - currentRadius, screenY - currentRadius, currentRadius * 2, currentRadius * 2);
            }
        }
        
        // Render target bullseye inner crosshair
        g.setColor(new Color(0, 0, 0, 100));
        int innerRadius = (int)(screenRadius * 0.1);
        if (innerRadius > 0) {
            g.drawOval(screenX - innerRadius, screenY - innerRadius, innerRadius * 2, innerRadius * 2);
            g.drawLine(screenX - 5, screenY, screenX + 5, screenY);
            g.drawLine(screenX, screenY - 5, screenX, screenY + 5);
        }
    }
    
    public int calculateScore(double impactX, double impactY) {
        double dx = impactX - x;
        double dy = impactY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > radius) {
            return 0; 
        }
        
        double normalizedDist = distance / radius; 
        
        if (normalizedDist <= 0.1) return 10;
        if (normalizedDist <= 0.2) return 9;
        if (normalizedDist <= 0.4) return 7; 
        if (normalizedDist <= 0.6) return 5; 
        if (normalizedDist <= 0.8) return 3; 
        return 1; 
    }
}
