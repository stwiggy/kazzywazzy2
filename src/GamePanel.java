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

    private int shotInLevel = 1;
    private final int MAX_SHOTS = 3;
    private int currentLevel = 1;
    private final int MAX_LEVELS = 3;
    private int totalScore = 0;
    private int lastScore = 0;
    private int mouseX = WIDTH / 2;
    private int mouseY = HEIGHT / 2;
    private boolean isDragging = false;
    private double cameraZoom = 1.0;
    private double bowCharge = 0.0;
    private static final double CHARGE_RATE = 2.5;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        setBackground(new Color(135, 206, 235));
        setOpaque(true);

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);

        target = new Target();
        arrow = new Arrow();
        wind = new Wind();
        configureWindForLevel();
            target.setMoving(currentLevel == 3);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentState == GameState.START_SCREEN) {
                    target.showScores();
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
            target.setMoving(currentLevel == 3);
                        currentState = GameState.AIMING;
                        target.clearHits();
                        cameraZoom = 1.0;
                        bowCharge = 0.0;
                    } else {
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
                    target.clearHits();
                    configureWindForLevel();
            target.setMoving(currentLevel == 3);
                    currentState = GameState.AIMING;
                    cameraZoom = 1.0;
                    bowCharge = 0.0;
                    return;
                }
                if (currentState == GameState.GAME_OVER) {
                    currentLevel = 1;
                    shotInLevel = 1;
                    totalScore = 0;
                    target.clearHits();
                    configureWindForLevel();
            target.setMoving(currentLevel == 3);
                    currentState = GameState.AIMING;
                    cameraZoom = 1.0;
                    bowCharge = 0.0;
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

    public void startGame() { timer.start(); }

    private void configureWindForLevel() {
        if (currentLevel == 1) {
            wind.setWindForce(0);
        } else if (currentLevel == 2) {
            wind.randomize();
            wind.setWindForce(10.0 + Math.random() * 10.0);
        } else {
            wind.randomize();
            wind.setWindForce(20.0 + Math.random() * 10.0);
        }
    }

    private void shootArrow() {
        target.hideScores();
        currentState = GameState.ARROW_FLYING;
        double wx = (mouseX - WIDTH / 2.0) / cameraZoom + WIDTH / 2.0;
        double wy = (mouseY - HEIGHT / 2.0) / cameraZoom + HEIGHT / 2.0;
        double targetAimX = wx - (WIDTH / 2.0);
        double targetAimY = wy - (HEIGHT / 2.0 - 100.0);
        arrow.launch(bowCharge, targetAimX, targetAimY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.AIMING && isDragging) {
            cameraZoom += (1.2 - cameraZoom) * 0.08;
            bowCharge += CHARGE_RATE * 0.024;
            if (bowCharge > 2.0) bowCharge = 2.0;
        } else {
            cameraZoom += (1.0 - cameraZoom) * 0.15;
            if (currentState != GameState.ARROW_FLYING && currentState != GameState.ROUND_END) {
                bowCharge = 0.0;
            }
        }

        target.update();
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
        g2d.scale(cameraZoom, cameraZoom);
        g2d.translate(-WIDTH / 2.0, -HEIGHT / 2.0);

        g2d.setColor(new Color(30, 100, 200));
        g2d.fillRect(-WIDTH, -HEIGHT, WIDTH * 3, HEIGHT * 2 + 100);

        g2d.setColor(new Color(45, 160, 45));
        g2d.fillRect(-WIDTH, HEIGHT / 2 + 100, WIDTH * 3, HEIGHT);

        double targetScale = 1.0;

        if (currentLevel > 1) {
            drawWindSquiggles(g2d);
        }

        target.draw(g2d, WIDTH, HEIGHT, targetScale);

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
                double maxCap = 2.0;
                double ratio = bowCharge / maxCap;
                Color chargeColor = new Color((int)(255 * ratio), (int)(255 * (1.0 - ratio)), 0);
                g2d.setColor(chargeColor);
                int angle = (int)(360 * ratio);
                g2d.drawArc(mouseX - 40, mouseY - 40, 80, 80, 90, -angle);
                g2d.setStroke(new BasicStroke(1));
            }
        }
        drawUI(g2d);
    }

    private void drawWindSquiggles(Graphics2D g2d) {
        double speed = wind.getSpeed();
        double sx = wind.getDirSignX();
        double sy = wind.getDirSignY();
        if (speed == 0 || (sx == 0 && sy == 0)) return;

        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        java.awt.Shape oldClip = g2d.getClip();
        g2d.setClip(0, 0, WIDTH, HEIGHT / 2 + 100);
        java.util.Random rng = new java.util.Random(77);
        int numSquiggles = 6 + (int)(speed / 5);
        double perpX = -sy;
        double perpY = sx;

        for (int i = 0; i < numSquiggles; i++) {
            float thickness = 2.0f + rng.nextFloat() * 2.5f;
            g2d.setStroke(new java.awt.BasicStroke(thickness, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            int alpha = 200 + rng.nextInt(55);
            g2d.setColor(new java.awt.Color(255, 255, 255, alpha));

            double startX = rng.nextInt(WIDTH);
            double startY = rng.nextInt(HEIGHT);
            double len = 100 + rng.nextDouble() * 120;
            double wave = 20 + rng.nextDouble() * 25;

            double p0x = startX;
            double p0y = startY;
            
            double p1x = startX + sx * len * 0.5;
            double p1y = startY + sy * len * 0.5;
            
            double p2x = startX + sx * len;
            double p2y = startY + sy * len;

            double c1x = p0x + sx * len * 0.15 + perpX * wave;
            double c1y = p0y + sy * len * 0.15 + perpY * wave;
            double c2x = p1x - sx * len * 0.15 + perpX * wave * 0.5;
            double c2y = p1y - sy * len * 0.15 + perpY * wave * 0.5;
            double c3x = p1x + sx * len * 0.15 - perpX * wave * 0.5;
            double c3y = p1y + sy * len * 0.15 - perpY * wave * 0.5;
            double c4x = p2x - sx * len * 0.15 - perpX * wave * 0.3;
            double c4y = p2y - sy * len * 0.15 - perpY * wave * 0.3;

            java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
            path.moveTo(p0x, p0y);
            path.curveTo(c1x, c1y, c2x, c2y, p1x, p1y);
            path.curveTo(c3x, c3y, c4x, c4y, p2x, p2y);

            int curlType = rng.nextInt(3);
            if (curlType < 2) {
                double curlDir = (curlType == 0) ? 1.0 : -1.0;
                double curlR = 18 + rng.nextDouble() * 14;
                
                double tanX = p2x - c4x;
                double tanY = p2y - c4y;
                double tanLen = Math.sqrt(tanX * tanX + tanY * tanY);
                if (tanLen > 0) { tanX /= tanLen; tanY /= tanLen; }
                double cp1x = p2x + tanX * curlR;
                double cp1y = p2y + tanY * curlR;
                double cp2x = p2x + tanX * curlR + (-tanY) * curlDir * curlR;
                double cp2y = p2y + tanY * curlR + tanX * curlDir * curlR;
                double endCx = p2x + (-tanY) * curlDir * curlR * 0.8;
                double endCy = p2y + tanX * curlDir * curlR * 0.8;
                path.curveTo(cp1x, cp1y, cp2x, cp2y, endCx, endCy);
            }

            g2d.draw(path);
        }
        g2d.setStroke(new java.awt.BasicStroke(1));
        g2d.setClip(oldClip);
    }

    private void drawBow(Graphics2D g2d) {
        double bowBaseX = WIDTH / 2.0;
        double bowBaseY = HEIGHT - 50.0;
        double angle = Math.atan2(mouseY - bowBaseY, mouseX - bowBaseX) + Math.PI / 2;

        java.awt.geom.AffineTransform initialTransform = g2d.getTransform();
        g2d.translate(bowBaseX, bowBaseY);
        g2d.rotate(angle);

        int tensionY = (int)(bowCharge * 15);
        int bottomY = tensionY;

        Path2D bowPath = new Path2D.Double();
        bowPath.moveTo(-300, bottomY - 100);
        bowPath.quadTo(0, bottomY - 250, 300, bottomY - 100);
        bowPath.quadTo(0, bottomY - 300, -300, bottomY - 100);

        g2d.setColor(new Color(120, 80, 30));
        g2d.fill(bowPath);

        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRoundRect(-15, bottomY - 20, 30, 40, 10, 10);
        g2d.setColor(new Color(220, 220, 220, 200));
        g2d.setStroke(new BasicStroke(3));

        if (isDragging) {
            g2d.drawLine(-290, bottomY - 100, 0, bottomY + tensionY);
            g2d.drawLine(290, bottomY - 100, 0, bottomY + tensionY);
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(-3, bottomY - 150 + tensionY, 6, 150);
            g2d.setColor(new Color(200, 50, 50));
            g2d.fillPolygon(new int[]{-3, -15, -3}, new int[]{bottomY - 20 + tensionY, bottomY + tensionY, bottomY + tensionY}, 3);
            g2d.fillPolygon(new int[]{3, 15, 3}, new int[]{bottomY - 20 + tensionY, bottomY + tensionY, bottomY + tensionY}, 3);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillPolygon(new int[]{-4, 0, 4}, new int[]{bottomY - 150 + tensionY, bottomY - 160 + tensionY, bottomY - 150 + tensionY}, 3);
        } else {
            g2d.drawLine(-290, bottomY - 100, 290, bottomY - 100);
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
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString("Level: " + currentLevel + " / " + MAX_LEVELS, 22, 37);
        g2d.drawString("Shot: " + shotInLevel + " / " + MAX_SHOTS, 22, 67);
        g2d.drawString("Total Score: " + totalScore, 22, 97);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Level: " + currentLevel + " / " + MAX_LEVELS, 20, 35);
        g2d.drawString("Shot: " + shotInLevel + " / " + MAX_SHOTS, 20, 65);
        g2d.drawString("Total Score: " + totalScore, 20, 95);
        if (currentState == GameState.START_SCREEN) {
            drawCenterText(g2d, "Click to start Level 1!", HEIGHT / 2 + 150);
        } else if (currentState == GameState.ROUND_END) {
            drawCenterText(g2d, "Hit Score: " + lastScore + " - Click to continue", HEIGHT / 2 + 150);
        } else if (currentState == GameState.LEVEL_COMPLETE) {
            drawCenterText(g2d, "Level " + currentLevel + " Cleared! Click for Level " + (currentLevel + 1), HEIGHT / 2 + 150);
        } else if (currentState == GameState.GAME_OVER) {
            drawCenterText(g2d, "Grand Score: " + totalScore + " - Click to play again", HEIGHT / 2 + 150);
        }
    }

    private void drawCenterText(Graphics2D g2d, String text) {
        drawCenterText(g2d, text, HEIGHT / 2);
    }

    private void drawCenterText(Graphics2D g2d, String text, int y) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
        int stringLen = (int) g2d.getFontMetrics().getStringBounds(text, g2d).getWidth();
        int start = WIDTH / 2 - stringLen / 2;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(text, start + 3, y + 3);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, start, y);
    }
}
