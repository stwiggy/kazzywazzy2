import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;
import java.awt.Stroke;

public class Target {
    // Distance from the player (Z-axis) - Increased from 1000 to 1400 to make it further
    public static final double DISTANCE_Z = 1400.0;
    
    public double x = 0;
    public double y = 0;
    public double radius = 150.0;
    
    private static final Color[] RING_COLORS = {
        Color.WHITE, Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW
    };
    
    private static final int[] RING_POINTS = { 1, 3, 5, 7, 9, 10 };

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100; 
        
        int screenX = cx + (int)(x * perspectiveScale);
        int screenY = cy + (int)(y * perspectiveScale);
        int screenRadius = (int)(radius * perspectiveScale);
        
        // Target stand (tripod)
        g.setColor(new Color(80, 50, 20)); 
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke((float)Math.max(2, 10 * perspectiveScale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        g.drawLine(screenX, screenY + screenRadius / 2, screenX, screenHeight);
        g.drawLine(screenX, screenY + screenRadius / 2, screenX - screenRadius, screenHeight);
        g.drawLine(screenX, screenY + screenRadius / 2, screenX + screenRadius, screenHeight);
        g.setStroke(oldStroke);

        int numRings = RING_COLORS.length;
        
        for (int i = 0; i < numRings; i++) {
            double ringRadiusScale = 1.0 - (i * 0.2); 
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
            
            g
