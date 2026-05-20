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
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentState == GameState.START_SCREEN) {
                    currentState = GameState.AIMING;
                    isDragging = true;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    return;
