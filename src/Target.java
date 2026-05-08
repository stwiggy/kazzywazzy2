import java.awt.Color;
import java.awt.Graphics2D;

public class Target {
    // Distance from the player (Z-axis)
    public static final double DISTANCE_Z = 1000.0;
    
    // Position of the target in the 3D world (relative to player view center)
    public double x = 0;
    public double y = 0;
    
    // Target radius in world units
    public double radius = 150.0;
    
    // Colors for the rings (from outside to inside)
    private static final Color[] RING_COLORS = {
        Color.WHITE, Color.BLACK, Color.BLUE, Color.RED, Color.YELLOW
    };
    
    // Points for the rings (from outside to inside)
    private static final int[] RING_POINTS = { 1, 3, 5, 7, 9, 10 };

    public void draw(Graphics2D g, int screenWidth, int screenHeight, double perspectiveScale) {
        // Calculate screen center
        int cx = screenWidth / 2;
        int cy = screenHeight / 2 - 100; // Shifted up slightly for better perspective
        
        // Target screen position
        int screenX = cx + (int)(x * perspectiveScale);
        int screenY = cy + (int)(y * perspectiveScale);
        
        // Target screen radius
        int screenRadius = (int)(radius * perspectiveScale);
        
        // Number of rings is 5 main colors, but yellow has an inner 10-point ring
        int numRings = RING_COLORS.length;
        
        // Draw rings from outside in
        for (int i = 0; i < numRings; i++) {
            g.setColor(RING_COLORS[i]);
            double ringRadiusScale = 1.0 - (i * 0.2); // 1.0, 0.8, 0.6, 0.4, 0.2
            int currentRadius = (int)(screenRadius * ringRadiusScale);
            
            g.fillOval(
                screenX - currentRadius,
                screenY - currentRadius,
                currentRadius * 2,
                currentRadius * 2
            );
            
            // Outline for visibility
            if (i == 0) {
                g.setColor(Color.BLACK);
                g.drawOval(
                    screenX - currentRadius,
                    screenY - currentRadius,
                    currentRadius * 2,
                    currentRadius * 2
                );
            }
        }
        
        // Draw inner 10-point ring line
        g.setColor(Color.BLACK);
        int innerRadius = (int)(screenRadius * 0.1);
        g.drawOval(
            screenX - innerRadius,
            screenY - innerRadius,
            innerRadius * 2,
            innerRadius * 2
        );
        
        // Target stand (simple brown line)
        g.setColor(new Color(139, 69, 19)); // Saddle Brown
        g.fillRect(screenX - 5, screenY + screenRadius, 10, screenHeight - (screenY + screenRadius));
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
