package vista;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.border.Border;
import vista.componentes.RoundedButton;

public final class Estilos {
    public static final Color ROSA_FUERTE = new Color(255, 28, 154);
    public static final Color ROSA_NEON = new Color(255, 0, 174);
    public static final Color ROSA_CLARO = new Color(255, 205, 235);
    public static final Color FONDO = new Color(255, 245, 251);
    public static final Color FONDO_SUAVE = new Color(255, 232, 246);
    public static final Color TEXTO = new Color(42, 25, 38);
    public static final Color TEXTO_SUAVE = new Color(118, 78, 105);
    public static final Color VERDE = new Color(21, 153, 71);
    public static final Color ROJO = new Color(255, 47, 95);
    public static final Color CELESTE = new Color(67, 213, 230);
    public static final Color BLANCO = Color.WHITE;

    private Estilos() {
    }

    public static Font fuenteTitulo() {
        return new Font("Segoe UI", Font.BOLD, 26);
    }

    public static Font fuenteSubtitulo() {
        return new Font("Segoe UI", Font.BOLD, 17);
    }

    public static Font fuenteNormal() {
        return new Font("Segoe UI", Font.PLAIN, 13);
    }

    public static Border bordeRosa() {
        return BorderFactory.createLineBorder(ROSA_FUERTE, 1);
    }

    public static void panelTarjeta(JComponent component) {
        component.setBackground(BLANCO);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 129, 196), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
    }

    public static JButton botonPrimario(String texto) {
        JButton boton = new RoundedButton(texto, ROSA_NEON, new Color(255, 72, 190), Color.WHITE, 20);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        return boton;
    }

    public static JButton botonSecundario(String texto) {
        JButton boton = new RoundedButton(texto, Color.WHITE, FONDO_SUAVE, ROSA_NEON, 18);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 119, 197), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return boton;
    }

    public static void estilizarTabla(JTable tabla) {
        tabla.setFont(fuenteNormal());
        tabla.setRowHeight(32);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tabla.setSelectionBackground(ROSA_CLARO);
        tabla.setSelectionForeground(TEXTO);
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(ROSA_FUERTE);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBackground(ROSA_FUERTE);
        renderer.setForeground(Color.WHITE);
        renderer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        renderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        renderer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        header.setDefaultRenderer(renderer);
    }
}
