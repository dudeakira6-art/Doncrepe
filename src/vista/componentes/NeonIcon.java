package vista.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public class NeonIcon implements Icon {
    public static final String HOME = "home";
    public static final String TABLE = "table";
    public static final String ORDER = "order";
    public static final String PRODUCT = "product";
    public static final String CASH = "cash";
    public static final String LOGOUT = "logout";
    public static final String USER = "user";
    public static final String LOCK = "lock";
    public static final String EXCEL = "excel";
    public static final String ADD = "add";

    private final String type;
    private final int size;
    private final Color color;

    public NeonIcon(String type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return getIconWidth();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(Math.max(2f, size / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        int s = size;
        int cx = x + s / 2;
        int cy = y + s / 2;

        if (HOME.equals(type)) {
            g2.drawLine(x + 4, y + s / 2, cx, y + 4);
            g2.drawLine(cx, y + 4, x + s - 4, y + s / 2);
            g2.drawRoundRect(x + 7, y + s / 2, s - 14, s - 10, 4, 4);
        } else if (TABLE.equals(type)) {
            g2.drawOval(x + 5, y + 5, s - 10, s / 3);
            g2.drawLine(cx, y + s / 3 + 6, cx, y + s - 5);
            g2.drawLine(x + 8, y + s - 5, x + s - 8, y + s - 5);
        } else if (ORDER.equals(type)) {
            g2.drawRoundRect(x + 5, y + 4, s - 10, s - 8, 5, 5);
            g2.drawLine(x + 9, y + 11, x + s - 9, y + 11);
            g2.drawLine(x + 9, y + 17, x + s - 10, y + 17);
        } else if (PRODUCT.equals(type)) {
            g2.drawRoundRect(x + 5, y + 7, s - 10, s - 10, 5, 5);
            g2.drawLine(x + 8, y + 8, cx, y + 3);
            g2.drawLine(cx, y + 3, x + s - 8, y + 8);
        } else if (CASH.equals(type)) {
            g2.drawRoundRect(x + 4, y + 7, s - 8, s - 14, 4, 4);
            g2.drawOval(cx - 4, cy - 4, 8, 8);
            g2.drawLine(x + 8, y + 11, x + 11, y + 11);
            g2.drawLine(x + s - 11, y + s - 11, x + s - 8, y + s - 11);
        } else if (LOGOUT.equals(type)) {
            g2.drawLine(x + 4, y + 5, x + 4, y + s - 5);
            g2.drawLine(x + 4, y + 5, x + s / 2, y + 5);
            g2.drawLine(x + 4, y + s - 5, x + s / 2, y + s - 5);
            g2.drawLine(x + s / 2, cy, x + s - 4, cy);
            g2.drawLine(x + s - 8, cy - 5, x + s - 4, cy);
            g2.drawLine(x + s - 8, cy + 5, x + s - 4, cy);
        } else if (USER.equals(type)) {
            g2.drawOval(cx - 5, y + 4, 10, 10);
            g2.drawArc(x + 5, y + 13, s - 10, s - 8, 20, 140);
        } else if (LOCK.equals(type)) {
            g2.drawRoundRect(x + 5, y + 11, s - 10, s - 13, 4, 4);
            g2.drawArc(x + 8, y + 3, s - 16, s - 8, 0, 180);
        } else if (EXCEL.equals(type)) {
            g2.drawRoundRect(x + 4, y + 4, s - 8, s - 8, 4, 4);
            g2.drawLine(x + 9, y + 9, x + s - 9, y + s - 9);
            g2.drawLine(x + s - 9, y + 9, x + 9, y + s - 9);
        } else if (ADD.equals(type)) {
            g2.drawOval(x + 4, y + 4, s - 8, s - 8);
            g2.drawLine(cx, y + 8, cx, y + s - 8);
            g2.drawLine(x + 8, cy, x + s - 8, cy);
        }
        g2.dispose();
    }
}
