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
        START_SCREEN, AIMING, ARROW_FLYING, ROUND_END, LEVEL_COMPLETE, GAME_OVER
    }

    private GameState currentState = GameState.START_SCREEN;
    private Timer timer;

    private Target target;
    private Arrow arrow;
    private Wind wind;

    // Progression variables
    private int shotInLevel = 1;
    private final int MAX_SHOTS = 3;
    private int currentLevel = 1;
    private final int MAX_LEVELS = 3;
    
    private int totalScore = 0;
    private int lastScore = 0;

    private int mouseX = WIDTH / 2;
    private int mouseY = HEIGHT / 2;
    private boolean isDragging = false;
    
    private double zoomLevel = 1.0;
    private double chargeLevel = 0.0; 
    private static final double A_CONSTANT = 2.5; 

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        setBackground(new Color(135, 206, 235));

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);

        target = new Target();
        arrow = new Arrow();
        wind = new Wind();
        
        // Initialize Level 1 with absolutely no wind
        configureWindForLevel();

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentState == GameState.START_SCREEN) {
                    currentState = GameState.AIMING;
                    isDragging = true;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    return;
                }
                if (currentState == GameState.ROUND_END) {
                    if (shotInLevel < MAX_SHOTS) {
                        shotInLevel++;
                        configureWindForLevel();
                        currentState = GameState.AIMING;
                    } else {
                        // 3 shots completed in this level
                        if (currentLevel < MAX_LEVELS) {
                            currentState = GameState.LEVEL_COMPLETE;
                        } else {
                            currentState = GameState.GAME_OVER;
                        }
                    }
                    return;
                }
                if (currentState == GameState.LEVEL_COMPLETE) {
                    currentLevel++;
                    shotInLevel = 1;
                    configureWindForLevel();
                    currentState = GameState.AIMING;
                    return;
                }
                if (currentState == GameState.GAME_OVER) {
                    currentLevel = 1;
                    shotInLevel = 1;
                    totalScore = 0;
                    target.clearHits();
                    configureWindForLevel();
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
                repaint(); 
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint(); 
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(mma);

        timer = new Timer(1000 / 60, this);
    }

    public void startGame() {
        timer.start();
    }

    /**
     * Configures wind patterns depending entirely on current level settings.
     * Restricts forces to spatial directions avoiding forward/backward vector pushes.
     */
    private void configureWindForLevel() {
        if (currentLevel == 1) {
            // Level 1: Perfectly calm. No drift.
            wind.setWindForce(0); 
        } else if (currentLevel == 2) {
            // Level 2: Moderate lateral side-winds (Left or Right drift only)
            wind.randomize(); 
            // If your Wind class randomizes verticality natively, you can clamp it here if needed, 
            // though standard horizontal drift works beautifully out of the box.
        } else {
            // Level 3: Strong multi-directional crosswinds (Alters X-drift and subtle Y lift/drop)
            wind.randomize();
            // Up the stakes slightly for the final level challenge
            wind.setWindForce(wind.getWindForce() * 1.5); 
        }
    }

    private void shootArrow() {
        currentState = GameState.ARROW_FLYING;
        
        double wx = (mouseX - WIDTH / 2.0) / zoomLevel + WIDTH / 2.0;
        double wy = (mouseY - HEIGHT / 2.0) / zoomLevel + HEIGHT / 2.0;
        
        double targetAimX = wx - (WIDTH / 2.0);
        double targetAimY = wy - (HEIGHT / 2.0 - 100.0);
        
        arrow.launch(chargeLevel, targetAimX, targetAimY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.AIMING && isDragging) {
            zoomLevel += (1.4 - zoomLevel) * 0.05;
            chargeLevel += A_CONSTANT * 0.016; 
            if (chargeLevel > 3.5) chargeLevel = 3.5; 
        } else {
            zoomLevel += (1.0 - zoomLevel) * 0.1;
            if (currentState != GameState.ARROW_FLYING && currentState != GameState.ROUND_END) {
                chargeLevel = 0.0;
            }
        }

        if (currentState == GameState.ARROW_FLYING) {
            arrow.update(wind);

            if (arrow.z >= Target.DISTANCE_Z && !arrow.isStuck()) {
                double dx = arrow.x - target.x;
                double dy = arrow.y - target.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= target.radius) {
                    lastScore = target.calculateScore(arrow.x, arrow.y);
                    totalScore += lastScore;
                    target.addHit(arrow.x, arrow.y);
                    arrow.setStuck(true);
                    currentState = GameState.ROUND_END;
                }
            }
            
            if (arrow.z > Target.DISTANCE_Z * 1.5 || arrow.y > 800) {
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
        
        g2d.translate(WIDTH / 2.0, HEIGHT / 2.0);
        g2d.scale(zoomLevel, zoomLevel);
        g2d.translate(-WIDTH / 2.0, -HEIGHT / 2.0);

        // Sky
        GradientPaint skyPaint = new GradientPaint(
            0, -HEIGHT, new Color(30, 100, 200), 
            0, HEIGHT / 2 + 100, new Color(200, 230, 255)
        );
        g2d.setPaint(skyPaint);
        g2d.fillRect(-WIDTH, -HEIGHT, WIDTH * 3, HEIGHT * 2 + 100);

        // Ground
        GradientPaint groundPaint = new GradientPaint(
            0, HEIGHT / 2 + 100, new Color(45, 160, 45), 
            0, HEIGHT * 2, new Color(20, 80, 20)
        );
        g2d.setPaint(groundPaint);
        g2d.fillRect(-WIDTH, HEIGHT / 2 + 100, WIDTH * 3, HEIGHT); 

        double cameraZ = 0; 
        double targetZDist = Target.DISTANCE_Z - cameraZ;
        double targetScale = 600.0 / targetZDist;

        target.draw(g2d, WIDTH, HEIGHT, targetScale);
        
        // Only draw the wind indicator if there is active wind (Levels 2 and 3)
        if (currentLevel > 1) {
            wind.draw(g2d, WIDTH, HEIGHT);
        }

        if (currentState == GameState.ARROW_FLYING || currentState == GameState.ROUND_END) {
            arrow.draw(g2d, WIDTH, HEIGHT, targetScale);
        }

        g2d.setTransform(oldTransform);

        if (currentState == GameState.AIMING || currentState == GameState.START_SCREEN) {
            drawBow(g2d);
            
            g2d.setColor(new Color(0, 255, 0, 180)); 
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(mouseX - 15, mouseY - 15, 30, 30);
            g2d.drawLine(mouseX - 25, mouseY, mouseX - 5, mouseY);
            g2d.drawLine(mouseX + 5, mouseY, mouseX + 25, mouseY);
            g2d.fillOval(mouseX - 2, mouseY - 2, 4, 4); 
            g2d.setStroke(new BasicStroke(1));
            
            if (isDragging) {
                g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setColor(new Color(0, 0, 0, 100)); 
                g2d.drawOval(mouseX - 40, mouseY - 40, 80, 80);
                
                double maxCap = 3.5;
                double ratio = chargeLevel / maxCap;
                Color chargeColor = new Color((int)(255 * ratio), (int)(255 * (1.0 - ratio)), 0);
                g2d.setColor(chargeColor);
                int angle = (int)(360 * ratio);
                g2d.drawArc(mouseX - 40, mouseY - 40, 80, 80, 90, -angle);
                g2d.setStroke(new BasicStroke(1));
            }
        }

        drawUI(g2d);
    }

    private void drawBow(Graphics2D g2d) {
        double bowBaseX = WIDTH / 2.0;
        double bowBaseY = HEIGHT - 50.0;
        
        double angle = Math.atan2(mouseY - bowBaseY, mouseX - bowBaseX) - Math.PI / 2;
        
        java.awt.geom.AffineTransform initialTransform = g2d.getTransform();
        g2d.translate(bowBaseX, bowBaseY);
        g2d.rotate(angle);
        
        int tensionY = (int)(chargeLevel * 15); 
        int bottomY = tensionY; 
        
        Path2D bowPath = new Path2D.Double();
        bowPath.moveTo(-300, bottomY - 100);
        bowPath.quadTo(0, bottomY + 50, 300, bottomY - 100);
        bowPath.quadTo(0, bottomY + 100, -300, bottomY - 100);
        
        GradientPaint woodPaint = new GradientPaint(
            -300, bottomY - 100, new Color(101, 67, 33), 
            300, bottomY - 100, new Color(139, 69, 19)
        );
        g2d.setPaint(woodPaint);
        g2d.fill(bowPath);
        
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRoundRect(-15, bottomY - 20, 30, 40, 10, 10);
        
        g2d.setColor(new Color(220, 220, 220, 200)); 
        g2d.setStroke(new BasicStroke(3)); 
        
        if (isDragging) {
            g2d.drawLine(-290, bottomY - 90, 0, bottomY + tensionY); 
            g2d.drawLine(290, bottomY - 90, 0, bottomY + tensionY); 
            
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(-3, bottomY - 150 + tensionY, 6, 150);
            
            g2d.setColor(new Color(200, 50, 50));
            g2d.fillPolygon(new int[]{-3, -15, -3}, new int[]{bottomY - 20 + tensionY, bottomY + tensionY, bottomY + tensionY}, 3);
            g2d.fillPolygon(new int[]{3, 15, 3}, new int[]{bottomY - 20 + tensionY, bottomY + tensionY, bottomY + tensionY}, 3);
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(new int[]{-4, 0, 4}, new int[]{bottomY - 150 + tensionY, bottomY - 160 + tensionY, bottomY - 150 + tensionY}, 3);
        } else {
            g2d.drawLine(-290, bottomY - 90, 290, bottomY - 90);
            
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(-3, bottomY - 100, 6, 100);
            
            g2d.setColor(new Color(200, 50, 50));
            g2d.fillPolygon(new int[]{-3, -15, -3}, new int[]{bottomY - 20, bottomY, bottomY}, 3);
            g2d.fillPolygon(new int[]{3, 15, 3}, new int[]{bottomY - 20, bottomY, bottomY}, 3);
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(new int[]{-4, 0, 4}, new int[]{bottomY - 100, bottomY - 110, bottomY - 100}, 3);
        }
        g2d.setTransform(initialTransform);
        g2d.setStroke(new BasicStroke(1));
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(10, 10, 240, 105, 15, 15);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Level: " + currentLevel + " / " + MAX_LEVELS, 22, 37);
        g2d.drawString("Shot: " + shotInLevel + " / " + MAX_SHOTS, 22, 67);
        g2d.drawString("Total Score: " + totalScore, 22, 97);

        g2d.setColor(Color.WHITE);
        g2d.drawString("Level: " + currentLevel + " / " + MAX_LEVELS, 20, 35);
        g2d.drawString("Shot: " + shotInLevel + " / " + MAX_SHOTS, 20, 65);
        g2d.drawString("Total Score: " + totalScore, 20, 95);

        if (currentState == GameState.START_SCREEN) {
            drawCenterText(g2d, "Level 1: Calm Breezes - Click to start!");
        } else if (currentState == GameState.ROUND_END) {
            drawCenterText(g2d, "Hit Score: " + lastScore + " - Click to continue");
        } else if (currentState == GameState.LEVEL_COMPLETE) {
            drawCenterText(g2d, "Level " + currentLevel + " Cleared! Click for Level " + (currentLevel + 1));
        } else if (currentState == GameState.GAME_OVER) {
            drawCenterText(g2d, "Victory! Grand Score: " + totalScore + " - Click to restart");
        }
    }

    private void drawCenterText(Graphics2D g2d, String text) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
        int stringLen = (int) g2d.getFontMetrics().getStringBounds(text, g2d).getWidth();
        int start = WIDTH / 2 - stringLen / 2;
        
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(text, start + 3, HEIGHT / 2 + 3);
        
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, start, HEIGHT / 2);
    }
}
