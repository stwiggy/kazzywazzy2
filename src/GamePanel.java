import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.GradientPaint;
import java.awt.geom.Path2D;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements ActionListener {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private enum GameState {
        START_SCREEN, AIMING, ARROW_FLYING, ROUND_END, GAME_OVER
    }

    private GameState currentState = GameState.START_SCREEN;
    private Timer timer;

    private Target target;
    private Arrow arrow;
    private Wind wind;

    private int round = 1;
    private final int MAX_ROUNDS = 3;
    private int totalScore = 0;
    private int lastScore = 0;

    // Aiming variables
    private int mouseX = WIDTH / 2;
    private int mouseY = HEIGHT / 2;
    private boolean isDragging = false;
    
    // Zoom and charge mechanics
    private double zoomLevel = 1.0;
    private double chargeLevel = 0.0;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        setBackground(new Color(135, 206, 235)); // Sky blue

        // Hide the default cursor
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);

        target = new Target();
        arrow = new Arrow();
        wind = new Wind();

        // Setup mouse listeners for aiming
        // Setup mouse listeners for aiming
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentState == GameState.START_SCREEN) {
                    currentState = GameState.AIMING;
                    // Initialize the drag immediately on start click!
                    isDragging = true;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    return;
                }
                if (currentState == GameState.ROUND_END) {
                    if (round < MAX_ROUNDS) {
                        round++;
                        wind.randomize();
                        currentState = GameState.AIMING;
                    } else {
                        currentState = GameState.GAME_OVER;
                    }
                    return;
                }
                if (currentState == GameState.GAME_OVER) {
                    round = 1;
                    totalScore = 0;
                    wind.randomize();
                    currentState = GameState.AIMING;
                    return;
                }

                if (currentState == GameState.AIMING) {
                    isDragging = true;
                    mouseX = e.getX();
                    mouseY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentState == GameState.AIMING && isDragging) {
                    isDragging = false;
                    shootArrow();
                }
            }
        };

        MouseMotionAdapter mma = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint(); // Force immediate updates to screen matrix
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint(); // Force immediate updates to screen matrix
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(mma);

        // 60 FPS
        timer = new Timer(1000 / 60, this);
    }

    public void startGame() {
        timer.start();
    }

    private void shootArrow() {
        currentState = GameState.ARROW_FLYING;
        arrow.reset();
        
        // The pull back distance could determine power, but we'll use fixed power for simplicity,
        arrow.vz = 10.0 + 20.0 * chargeLevel; // Speed depends on charge
        
        // Calculate world coordinates the user is aiming at, accounting for zoom
        double wx = (mouseX - WIDTH / 2.0) / zoomLevel + WIDTH / 2.0;
        double wy = (mouseY - HEIGHT / 2.0) / zoomLevel + HEIGHT / 2.0;
        
        // Center of the 3D world projection is WIDTH / 2, HEIGHT / 2 - 100
        double x_world = wx - (WIDTH / 2.0);
        double y_world = wy - (HEIGHT / 2.0 - 100.0);
        
        // Time to reach the target plane
        double t = Target.DISTANCE_Z / arrow.vz;
        
        // Calculate initial velocities to hit the target point
        arrow.vx = x_world / t;
        arrow.vy = y_world / t;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update zoom and charge
        if (currentState == GameState.AIMING && isDragging) {
            zoomLevel += (1.5 - zoomLevel) * 0.1;
            chargeLevel += 0.02;
            if (chargeLevel > 1.0) chargeLevel = 1.0;
        } else {
            zoomLevel += (1.0 - zoomLevel) * 0.1;
            if (currentState != GameState.ARROW_FLYING) {
                chargeLevel = 0.0;
            }
        }

        if (currentState == GameState.ARROW_FLYING) {
            arrow.update(wind);

            // Check if it reached the target's Z plane
            if (arrow.z >= Target.DISTANCE_Z) {
                // Calculate score
                lastScore = target.calculateScore(arrow.x, arrow.y);
                totalScore += lastScore;
                currentState = GameState.ROUND_END;
            }
            
            // Check if it fell to the ground (missed low)
            // Ground is let's say y = 500 in world coordinates
            if (arrow.y > 500) {
                lastScore = 0;
                currentState = GameState.ROUND_END;
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- WORLD RENDERING (Subject to zoom and camera pan) ---
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
        
        // Apply zoom
        g2d.translate(WIDTH / 2.0, HEIGHT / 2.0);
        g2d.scale(zoomLevel, zoomLevel);
        g2d.translate(-WIDTH / 2.0, -HEIGHT / 2.0);

        // Draw Sky Gradient
        GradientPaint skyPaint = new GradientPaint(
            0, -HEIGHT, new Color(30, 100, 200), // Deep sky
            0, HEIGHT / 2 + 100, new Color(200, 230, 255) // Light horizon
        );
        g2d.setPaint(skyPaint);
        g2d.fillRect(-WIDTH, -HEIGHT, WIDTH * 3, HEIGHT * 2 + 100);

        // Draw Environment (Ground)
        GradientPaint groundPaint = new GradientPaint(
            0, HEIGHT / 2 + 100, new Color(45, 160, 45), // Lighter green at horizon
            0, HEIGHT * 2, new Color(20, 80, 20) // Dark green closer
        );
        g2d.setPaint(groundPaint);
        g2d.fillRect(-WIDTH, HEIGHT / 2 + 100, WIDTH * 3, HEIGHT); // Make ground wider to handle pan/zoom

        // Perspective scale for the target
        double cameraZ = 0; // Camera is at z=0
        double targetZDist = Target.DISTANCE_Z - cameraZ;
        double targetScale = 1000.0 / targetZDist;

        // Draw Target
        target.draw(g2d, WIDTH, HEIGHT, targetScale);

        // Draw Wind
        wind.draw(g2d, WIDTH, HEIGHT);

        // Draw Arrow
        if (currentState == GameState.ARROW_FLYING) {
            double arrowDist = arrow.z - cameraZ;
            if (arrowDist < 1) arrowDist = 1;
            double arrowScale = 1000.0 / arrowDist;
            arrow.draw(g2d, WIDTH, HEIGHT, arrowScale);
        }

        // --- UI RENDERING (Not zoomed/panned) ---
        g2d.setTransform(oldTransform);
        
        int cx = WIDTH / 2;
        int cy = HEIGHT / 2;

        // Draw Bow (first person view) if aiming
        if (currentState == GameState.AIMING || currentState == GameState.START_SCREEN) {
            drawBow(g2d);
            
            // Draw crosshair at mouse cursor
            g2d.setColor(new Color(0, 255, 0, 180)); // Bright green reticle
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(mouseX - 15, mouseY - 15, 30, 30);
            g2d.drawLine(mouseX - 25, mouseY, mouseX - 5, mouseY);
            g2d.drawLine(mouseX + 5, mouseY, mouseX + 25, mouseY);
            g2d.drawLine(mouseX, mouseY - 25, mouseX, mouseY - 5);
            g2d.drawLine(mouseX, mouseY + 5, mouseX, mouseY + 25);
            g2d.fillOval(mouseX - 2, mouseY - 2, 4, 4); // Center dot
            g2d.setStroke(new BasicStroke(1));
            
            // Draw pull-back indicator
            if (isDragging) {
                g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(new Color(0, 0, 0, 100)); // shadow
                g2d.drawOval(mouseX - 40, mouseY - 40, 80, 80);
                
                // Color gradient based on charge level
                Color chargeColor = new Color(
                    (int)(255 * chargeLevel),
                    (int)(255 * (1 - chargeLevel)),
                    0
                );
                g2d.setColor(chargeColor);
                int angle = (int)(360 * chargeLevel);
                g2d.drawArc(mouseX - 40, mouseY - 40, 80, 80, 90, -angle);
                g2d.setStroke(new BasicStroke(1));
            }
        }

        // Draw UI Overlay
        drawUI(g2d);
    }

    private void drawBow(Graphics2D g2d) {
        int bottomY = HEIGHT;
        
        // --- FIX THE ZOOM MISMATCH ---
        // Reverse-engineer the zoom translation matrix so the bow coordinates 
        // perfectly align with the scaled visual crosshair space.
        double dynamicBowX = (mouseX - WIDTH / 2.0) / zoomLevel + WIDTH / 2.0;
        
        // --- 1. Draw Bow Structure ---
        Path2D bowPath = new Path2D.Double();
        bowPath.moveTo(dynamicBowX - 300, bottomY - 100);
        bowPath.quadTo(dynamicBowX, bottomY + 50, dynamicBowX + 300, bottomY - 100);
        bowPath.quadTo(dynamicBowX, bottomY + 100, dynamicBowX - 300, bottomY - 100);
        
        GradientPaint woodPaint = new GradientPaint(
            (float)(dynamicBowX - 300), bottomY - 100, new Color(101, 67, 33), 
            (float)(dynamicBowX + 300), bottomY - 100, new Color(139, 69, 19)
        );
        g2d.setPaint(woodPaint);
        g2d.fill(bowPath);
        
        // Bow grip
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRoundRect((int)dynamicBowX - 15, bottomY - 20, 30, 40, 10, 10);
        
        // --- 2. Draw Bow String & Nocked Arrow ---
        g2d.setColor(new Color(220, 220, 220, 200)); 
        g2d.setStroke(new BasicStroke(3)); 
        
        if (isDragging) {
            g2d.drawLine((int)dynamicBowX - 290, bottomY - 90, (int)dynamicBowX, bottomY); 
            g2d.drawLine((int)dynamicBowX + 290, bottomY - 90, (int)dynamicBowX, bottomY); 
            
            // Arrow body
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect((int)dynamicBowX - 3, bottomY - 150, 6, 150);
            
            // Fletching
            g2d.setColor(new Color(200, 50, 50));
            g2d.fillPolygon(new int[]{(int)dynamicBowX-3, (int)dynamicBowX-15, (int)dynamicBowX-3}, new int[]{bottomY-20, bottomY, bottomY}, 3);
            g2d.fillPolygon(new int[]{(int)dynamicBowX+3, (int)dynamicBowX+15, (int)dynamicBowX+3}, new int[]{bottomY-20, bottomY, bottomY}, 3);
            
            // Arrowhead
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(new int[]{(int)dynamicBowX-4, (int)dynamicBowX, (int)dynamicBowX+4}, new int[]{bottomY-150, bottomY-160, bottomY-150}, 3);
        } else {
            g2d.drawLine((int)dynamicBowX - 290, bottomY - 90, (int)dynamicBowX + 290, bottomY - 90);
            
            // Arrow resting
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect((int)dynamicBowX - 3, bottomY - 100, 6, 100);
            
            g2d.setColor(new Color(200, 50, 50));
            g2d.fillPolygon(new int[]{(int)dynamicBowX-3, (int)dynamicBowX-15, (int)dynamicBowX-3}, new int[]{bottomY-20, bottomY, bottomY}, 3);
            g2d.fillPolygon(new int[]{(int)dynamicBowX+3, (int)dynamicBowX+15, (int)dynamicBowX+3}, new int[]{bottomY-20, bottomY, bottomY}, 3);
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(new int[]{(int)dynamicBowX-4, (int)dynamicBowX, (int)dynamicBowX+4}, new int[]{bottomY-100, bottomY-110, bottomY-100}, 3);
        }
        g2d.setStroke(new BasicStroke(1));
    }

    private void drawUI(Graphics2D g2d) {
        // UI Backdrop
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(10, 10, 200, 70, 15, 15);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        // Text Shadow
        g2d.setColor(Color.BLACK);
        g2d.drawString("Round: " + round + " / " + MAX_ROUNDS, 22, 37);
        g2d.drawString("Score: " + totalScore, 22, 67);

        // Text Body
        g2d.setColor(Color.WHITE);
        g2d.drawString("Round: " + round + " / " + MAX_ROUNDS, 20, 35);
        g2d.drawString("Score: " + totalScore, 20, 65);

        if (currentState == GameState.START_SCREEN) {
            drawCenterText(g2d, "Click anywhere to start!");
        } else if (currentState == GameState.ROUND_END) {
            drawCenterText(g2d, "Score: " + lastScore + " - Click to continue");
        } else if (currentState == GameState.GAME_OVER) {
            drawCenterText(g2d, "Game Over! Total Score: " + totalScore + " - Click to restart");
        }
    }

    private void drawCenterText(Graphics2D g2d, String text) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
        int stringLen = (int) g2d.getFontMetrics().getStringBounds(text, g2d).getWidth();
        int start = WIDTH / 2 - stringLen / 2;
        
        // Text Shadow
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(text, start + 3, HEIGHT / 2 + 3);
        
        // Text Body
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, start, HEIGHT / 2);
    }
}
