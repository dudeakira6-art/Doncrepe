package vista.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class RoundedButton extends JButton {
    private final Color normal;
    private final Color hover;
    private final int radius;
    private boolean hovered;

    public RoundedButton(String text, Color normal, Color hover, Color foreground, int radius) {
        super(text);
        this.normal = normal;
        this.hover = hover;
        this.radius = radius;
        setForeground(foreground);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (hovered) {
            g2.setColor(new Color(255, 0, 174, 55));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, radius, radius);
        }
        g2.setColor(hovered ? hover : normal);
        int y = getModel().isPressed() ? 2 : 0;
        g2.fillRoundRect(0, y, getWidth(), getHeight() - 3, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
