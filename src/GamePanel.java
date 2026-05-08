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
    private int dragStartX = 0;
    private int dragStartY = 0;
    private int currentDragX = 0;
    private int currentDragY = 0;
    private boolean isDragging = false;
    
    // Crosshair offset (where the user is aiming)
    private double aimOffsetX = 0;
    private double aimOffsetY = 0;
    
    // Zoom and charge mechanics
    private double zoomLevel = 1.0;
    private double chargeLevel = 0.0;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(135, 206, 235)); // Sky blue

        target = new Target();
        arrow = new Arrow();
        wind = new Wind();

        // Setup mouse listeners for aiming
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentState == GameState.START_SCREEN) {
                    currentState = GameState.AIMING;
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
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    currentDragX = e.getX();
                    currentDragY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentState == GameState.AIMING && isDragging) {
                    isDragging = false;
                    shootArrow();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentState == GameState.AIMING && isDragging) {
                    currentDragX = e.getX();
                    currentDragY = e.getY();
                    
                    // Update aim offset based on drag difference
                    aimOffsetX = (currentDragX - dragStartX) * 1.0;
                    aimOffsetY = (currentDragY - dragStartY) * 1.0;
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);

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
        // and use the drag offset to determine the initial angle/velocity
        arrow.vz = 10.0 + 20.0 * chargeLevel; // Speed depends on charge
        
        // Map the screen aim offset to initial velocities
        // We want to shoot "up" and "towards" the aim point.
        // Since camera pan is exactly 1.0 * aimOffset, and at full charge vz=30 (time=33.3),
        // vx * 33.3 should equal aimOffsetX. So vx = aimOffsetX / 33.3 ≈ 0.03
        arrow.vx = aimOffsetX * 0.03;
        
        // Pulling down (positive aimOffsetY) means shooting higher (negative velocity Y)
        // because Y=0 is top in Java Swing
        arrow.vy = aimOffsetY * 0.03; // Removed the base upward boost so it flies straight
        
        aimOffsetX = 0;
        aimOffsetY = 0;
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
        
        // Apply camera pan (aim offset) and zoom
        g2d.translate(WIDTH / 2.0, HEIGHT / 2.0);
        g2d.scale(zoomLevel, zoomLevel);
        if (currentState == GameState.AIMING) {
            g2d.translate(-aimOffsetX * 1.0, -aimOffsetY * 1.0); // Increased from 0.1 to let the shooter move MUCH more
        }
        g2d.translate(-WIDTH / 2.0, -HEIGHT / 2.0);

        // Draw Environment (Ground)
        g2d.setColor(new Color(34, 139, 34)); // Forest green
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
            
            // Draw crosshair
            g2d.setColor(Color.WHITE);
            g2d.drawLine(cx - 10, cy, cx + 10, cy);
            g2d.drawLine(cx, cy - 10, cx, cy + 10);
            
            // Draw pull-back indicator
            if (isDragging) {
                // Remove the red drag line, replace with charge circle
                g2d.setStroke(new java.awt.BasicStroke(4));
                g2d.setColor(new Color(255, 255, 255, 100)); // Faded white background
                g2d.drawOval(cx - 30, cy - 30, 60, 60);
                
                g2d.setColor(Color.YELLOW);
                int angle = (int)(360 * chargeLevel);
                g2d.drawArc(cx - 30, cy - 30, 60, 60, 90, -angle);
                g2d.setStroke(new java.awt.BasicStroke(1));
            }
        }

        // Draw UI Overlay
        drawUI(g2d);
    }

    private void drawBow(Graphics2D g2d) {
        int cx = WIDTH / 2;
        int bottomY = HEIGHT;
        
        g2d.setColor(new Color(139, 69, 19)); // Brown
        // Simulate a bow curving up from the bottom of the screen
        g2d.fillArc(cx - 300, bottomY - 100, 600, 200, 0, 180);
        
        // Empty out the middle
        g2d.setColor(getBackground());
        g2d.fillArc(cx - 280, bottomY - 80, 560, 180, 0, 180);
        
        // Bow string
        g2d.setColor(Color.WHITE);
        if (isDragging) {
            // String pulled back
            g2d.drawLine(cx - 280, bottomY - 80, cx, bottomY); // Left string to center
            g2d.drawLine(cx + 280, bottomY - 80, cx, bottomY); // Right string to center
            
            // Arrow nocked
            g2d.setColor(Color.GRAY);
            g2d.fillRect(cx - 2, bottomY - 150, 4, 150);
        } else {
            g2d.drawLine(cx - 280, bottomY - 80, cx + 280, bottomY - 80);
            
            // Arrow resting
            g2d.setColor(Color.GRAY);
            g2d.fillRect(cx - 2, bottomY - 100, 4, 100);
        }
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        
        g2d.drawString("Round: " + round + " / " + MAX_ROUNDS, 20, 30);
        g2d.drawString("Score: " + totalScore, 20, 60);

        if (currentState == GameState.START_SCREEN) {
            drawCenterText(g2d, "Click anywhere to start!");
        } else if (currentState == GameState.ROUND_END) {
            drawCenterText(g2d, "Score: " + lastScore + " - Click to continue");
        } else if (currentState == GameState.GAME_OVER) {
            drawCenterText(g2d, "Game Over! Total Score: " + totalScore + " - Click to restart");
        }
    }

    private void drawCenterText(Graphics2D g2d, String text) {
        int stringLen = (int) g2d.getFontMetrics().getStringBounds(text, g2d).getWidth();
        int start = WIDTH / 2 - stringLen / 2;
        
        // Draw outline
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, start - 1, HEIGHT / 2 - 1);
        g2d.drawString(text, start + 1, HEIGHT / 2 - 1);
        g2d.drawString(text, start - 1, HEIGHT / 2 + 1);
        g2d.drawString(text, start + 1, HEIGHT / 2 + 1);
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, start, HEIGHT / 2);
    }
}
