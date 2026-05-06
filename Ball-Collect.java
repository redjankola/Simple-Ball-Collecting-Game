import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class seminar7_loja extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(seminar7_loja::new);
    }

    public seminar7_loja() {
        setTitle("Koshi - Loja");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        add(new PanelLoje());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

class PanelLoje extends JPanel implements KeyListener, ActionListener {

    private static final int W = 600, H = 500;
    private static final int KOSHI_W = 80, KOSHI_H = 18;
    private static final int KOSHI_Y = H - 40;
    private static final int MAX_JETA = 5;

    private int koshiX = W / 2 - KOSHI_W / 2;
    private int score = 0, jeta = MAX_JETA;
    private boolean paused = false, gameOver = false;
    private boolean levajtas = false, levdjathtas = false;

    private final ArrayList<Rrath> rathet = new ArrayList<>();
    private final Timer timer;
    private final Random rand = new Random();
    private int spawnCounter = 0;

    private final Color[] NGJYRAT = {
            Color.RED, new Color(78, 205, 196), new Color(255, 230, 109),
            new Color(162, 155, 254), new Color(253, 121, 168)
    };

    public PanelLoje() {
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(10, 10, 26));
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(16, this); // ~60 fps
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused || gameOver) { repaint(); return; }

        // Leviz koshin
        if (levajtas)  koshiX = Math.max(0, koshiX - 5);
        if (levdjathtas) koshiX = Math.min(W - KOSHI_W, koshiX + 5);

        // Shpawn rrath te ri
        spawnCounter++;
        int rate = Math.max(40, 80 - score * 2);
        if (spawnCounter >= rate) {
            rathet.add(new Rrath(rand, W, NGJYRAT, score));
            spawnCounter = 0;
        }

        // Leviz dhe kontrollo rathet
        Iterator<Rrath> it = rathet.iterator();
        while (it.hasNext()) {
            Rrath r = it.next();
            r.y += r.vy;

            // Kapur nga koshi
            if (r.y + r.radius > KOSHI_Y &&
                    r.y - r.radius < KOSHI_Y + KOSHI_H &&
                    r.x > koshiX && r.x < koshiX + KOSHI_W) {
                score++;
                it.remove();
            }
            // Rrath ra ne toke
            else if (r.y - r.radius > H) {
                jeta--;
                if (jeta <= 0) gameOver = true;
                it.remove();
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vizato rathet
        for (Rrath r : rathet) r.draw(g2);

        // Vizato koshin
        drawKoshi(g2);

        // HUD — pike dhe jete
        drawHUD(g2);

        // Mesazh nese loja u ndal ose mbaroi
        if (gameOver) drawMesazh(g2, "Loja Mbaroi!", "Pike: " + score + "  |  Shtyp R per te rifilluar");
        else if (paused) drawMesazh(g2, "E Ndalur", "Shtyp R per te vazhduar");
    }

    private void drawKoshi(Graphics2D g2) {
        int[] xs = {koshiX, koshiX + KOSHI_W, koshiX + KOSHI_W - 8, koshiX + 8};
        int[] ys = {KOSHI_Y, KOSHI_Y, KOSHI_Y + KOSHI_H, KOSHI_Y + KOSHI_H};
        g2.setColor(new Color(45, 53, 97));
        g2.fillPolygon(xs, ys, 4);
        g2.setColor(new Color(123, 140, 222));
        g2.setStroke(new BasicStroke(2));
        g2.drawPolygon(xs, ys, 4);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(Color.WHITE);
        g2.drawString("Pike: " + score, 14, 24);
        g2.drawString("Jet:", W - 120, 24);
        for (int i = 0; i < MAX_JETA; i++) {
            g2.setColor(i < jeta ? new Color(255, 107, 107) : new Color(60, 60, 80));
            g2.fillOval(W - 75 + i * 16, 12, 10, 10);
        }
    }

    private void drawMesazh(Graphics2D g2, String titull, String nentitull) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, W, H);
        g2.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titull, (W - fm.stringWidth(titull)) / 2, H / 2 - 20);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.setColor(new Color(200, 200, 200));
        fm = g2.getFontMetrics();
        g2.drawString(nentitull, (W - fm.stringWidth(nentitull)) / 2, H / 2 + 16);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT  -> levajtas = true;
            case KeyEvent.VK_RIGHT -> levdjathtas = true;
            case KeyEvent.VK_N     -> paused = true;
            case KeyEvent.VK_R     -> { paused = false; if (gameOver) resetLoja(); }
            case KeyEvent.VK_S     -> System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  levajtas = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) levdjathtas = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    private void resetLoja() {
        score = 0; jeta = MAX_JETA;
        gameOver = false; paused = false;
        rathet.clear();
        koshiX = W / 2 - KOSHI_W / 2;
    }
}

class Rrath {
    int x, y, radius;
    double vy;
    Color color;

    public Rrath(Random rand, int W, Color[] ngjyrat, int score) {
        radius = 10 + rand.nextInt(8);
        x = radius + rand.nextInt(W - radius * 2);
        y = -radius;
        vy = 1.2 + rand.nextDouble() * 1.8 + score * 0.03;
        color = ngjyrat[rand.nextInt(ngjyrat.length)];
    }

    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g2.setColor(new Color(255, 255, 255, 80));
        int hr = radius / 3;
        g2.fillOval(x - radius/2, y - radius/2, hr, hr);
    }
}