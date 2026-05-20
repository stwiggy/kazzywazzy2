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

            if (arrow.z >= Target.DISTANCE_Z) {
                lastScore = target.calculateScore(arrow.x, arrow.y);
                totalScore += lastScore;
                arrow.setStuck(true);
                currentState = GameState.ROUND_END;
            }
            
            if (arrow.y > 800) {
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

        GradientPaint skyPaint = new GradientPaint(
            0, -HEIGHT, new Color(30, 100, 200), 
            0, HEIGHT / 2 + 100, new Color(200, 230, 255)
        );
        g2d.setPaint(skyPaint);
        g2d.fillRect(-WIDTH, -HEIGHT, WIDTH * 3, HEIGHT * 2 + 100);

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
        wind.draw(g2d, WIDTH, HEIGHT);

        if (currentState == GameState.ARROW_FLYING || currentState == GameState.ROUND_END) {
            // Note: Arrow handle draw request internally checks if it should skip rendering or show hit mark
            target.draw(g2d, WIDTH, HEIGHT, targetScale);
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
        double dynamicBowX = mouseX;
        int tensionY = (int)(chargeLevel * 15); 
        int dynamicBowY = mouseY + 250; 
        int bottomY = dynamicBowY + tensionY; 
        
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
        
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRoundRect((int)dynamicBowX - 15, bottomY - 20, 30, 40, 10, 10);
        
        g2d.setColor(new Color(220, 220, 220, 200)); 
        g2d.setStroke(new BasicStroke(3)); 
        
        if (isDragging) {
            g2d.drawLine((int)dynamicBowX - 290, bottomY - 90, (int)dynamicBowX, bottomY + tensionY); 
            g2d.drawLine((int)dynamicBowX + 290, bottomY - 90, (int)dynamicBowX, bottomY + tensionY); 
            
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect((int)dynamicBowX - 3, bottomY - 150 + tensionY, 6, 150);
            
            g2d.setColor(new Color(200, 50, 50));
            g2d.fillPolygon(new int[]{(int)dynamicBowX-3, (int)dynamicBowX-15, (int)dynamicBowX-3}, new int[]{bottomY-20 + tensionY, bottomY + tensionY, bottomY + tensionY}, 3);
            g2d.fillPolygon(new int[]{(int)dynamicBowX+3, (int)dynamicBowX+15, (int)dynamicBowX+3}, new int[]{bottomY-20 + tensionY, bottomY + tensionY, bottomY + tensionY}, 3);
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(new int[]{(int)dynamicBowX-4, (int)dynamicBowX, (int)dynamicBowX+4}, new int[]{bottomY-150 + tensionY, bottomY-160 + tensionY, bottomY-150 + tensionY}, 3);
        } else {
            g2d.drawLine((int)dynamicBowX - 290, bottomY - 90, (int)dynamicBowX + 290, bottomY - 90);
            
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
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(10, 10, 200, 70, 15, 15);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Round: " + round + " / " + MAX_ROUNDS, 22, 37);
        g2d.drawString("Score: " + totalScore, 22, 67);

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
        
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(text, start + 3, HEIGHT / 2 + 3);
        
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, start, HEIGHT / 2);
    }
}
