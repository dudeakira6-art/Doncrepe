package vista.componentes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private final int radius;
    private final Color background;
    private final boolean shadow;

    public RoundedPanel(int radius, Color background) {
        this(radius, background, false);
    }

    public RoundedPanel(int radius, Color background, boolean shadow) {
        this.radius = radius;
        this.background = background;
        this.shadow = shadow;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int inset = shadow ? 4 : 0;
        if (shadow) {
            g2.setColor(new Color(255, 79, 163, 45));
            g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 8, radius, radius);
        }
        g2.setColor(background);
        g2.fillRoundRect(0, 0, getWidth() - inset, getHeight() - inset, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
