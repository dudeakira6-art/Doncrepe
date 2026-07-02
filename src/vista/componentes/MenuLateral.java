package vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import vista.Estilos;

public class MenuLateral extends JPanel {
    private static final String MENU_INICIO = "Inicio";
    private static final String MENU_MESA = "Mesa";
    private static final String MENU_PEDIDO = "Pedido";
    private static final String MENU_PRODUCTO = "Producto";
    private static final String MENU_HISTORIAL = "Historial Caja";
    private static final String MENU_CERRAR = "Cerrar Sesion";
    private final Map<String, JButton> botones = new LinkedHashMap<String, JButton>();
    private String activo = MENU_INICIO;

    public MenuLateral() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(new Color(255, 202, 235));
        setPreferredSize(new Dimension(210, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(255, 136, 207)));

        JLabel logo = new JLabel(Recursos.logo(150, 150));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        logo.setBorder(BorderFactory.createEmptyBorder(16, 10, 8, 10));
        add(logo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(5, 1, 0, 8));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        agregarBoton(centro, MENU_INICIO, NeonIcon.HOME);
        agregarBoton(centro, MENU_MESA, NeonIcon.TABLE);
        agregarBoton(centro, MENU_PEDIDO, NeonIcon.ORDER);
        agregarBoton(centro, MENU_PRODUCTO, NeonIcon.PRODUCT);
        agregarBoton(centro, MENU_HISTORIAL, NeonIcon.CASH);
        add(centro, BorderLayout.CENTER);

        JButton cerrar = crearBoton(MENU_CERRAR, NeonIcon.LOGOUT);
        cerrar.setActionCommand(MENU_CERRAR);
        botones.put(MENU_CERRAR, cerrar);
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.setBorder(BorderFactory.createEmptyBorder(10, 12, 18, 12));
        sur.add(cerrar, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        marcarActivo(MENU_INICIO);
    }

    private void agregarBoton(JPanel panel, String texto, String icono) {
        JButton boton = crearBoton(texto, icono);
        botones.put(texto, boton);
        panel.add(boton);
    }

    private JButton crearBoton(String texto, String icono) {
        JButton boton = new JButton(texto);
        boton.setIcon(iconoMenu(icono, Estilos.TEXTO));
        boton.setIconTextGap(10);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        boton.setBackground(new Color(255, 202, 235));
        boton.setForeground(Estilos.TEXTO);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!texto.equals(activo)) {
                    boton.setBackground(new Color(255, 238, 248));
                }
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, texto.equals(activo) ? 5 : 1, 0, 0, Estilos.ROSA_FUERTE),
                        BorderFactory.createEmptyBorder(9, 13, 9, 13)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!texto.equals(activo)) {
                    boton.setBackground(new Color(255, 202, 235));
                }
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, texto.equals(activo) ? 5 : 0, 0, 0, Estilos.ROSA_FUERTE),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
        });
        return boton;
    }

    public JButton getBoton(String nombre) {
        return botones.get(nombre);
    }

    public void marcarActivo(String nombre) {
        activo = nombre;
        for (Map.Entry<String, JButton> entry : botones.entrySet()) {
            boolean seleccionado = entry.getKey().equals(activo);
            entry.getValue().setBackground(seleccionado ? Estilos.BLANCO : new Color(255, 202, 235));
            entry.getValue().setForeground(seleccionado ? Estilos.ROSA_FUERTE : Estilos.TEXTO);
            String tipo = iconoPorNombre(entry.getKey());
            entry.getValue().setIcon(iconoMenu(tipo, seleccionado));
            entry.getValue().setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, seleccionado ? 5 : 0, 0, 0, Estilos.ROSA_FUERTE),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        }
    }

    private javax.swing.Icon iconoMenu(String tipo, boolean rosa) {
        String archivo = iconoArchivo(tipo, rosa);
        ImageIcon icon = Recursos.icono(archivo, 20);
        if (icon != null) {
            return icon;
        }
        return new NeonIcon(tipo, 20, rosa ? Estilos.ROSA_NEON : Estilos.TEXTO);
    }

    private javax.swing.Icon iconoMenu(String tipo, Color color) {
        return iconoMenu(tipo, Estilos.ROSA_NEON.equals(color));
    }

    private String iconoArchivo(String tipo, boolean rosa) {
        if (NeonIcon.HOME.equals(tipo)) {
            return rosa ? "icon_homepink.png" : "icon_home.png";
        }
        if (NeonIcon.TABLE.equals(tipo)) {
            return rosa ? "icon_tablepink.png" : "icon_table.png";
        }
        if (NeonIcon.ORDER.equals(tipo)) {
            return rosa ? "icon_orderpink.png" : "icon_order.png";
        }
        if (NeonIcon.PRODUCT.equals(tipo)) {
            return rosa ? "icon_productpink.png" : "icon_product.png";
        }
        if (NeonIcon.CASH.equals(tipo)) {
            return rosa ? "icon_cashpink.png" : "icon_cash.png";
        }
        return "icon_logout.png";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        ImageIcon fondo = Recursos.imagen("Fondo de Botones.png", getWidth(), getHeight());
        if (fondo != null) {
            g2.drawImage(fondo.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
        super.paintComponent(g);
    }

    private String iconoPorNombre(String nombre) {
        if (MENU_INICIO.equals(nombre)) {
            return NeonIcon.HOME;
        }
        if (MENU_MESA.equals(nombre)) {
            return NeonIcon.TABLE;
        }
        if (MENU_PEDIDO.equals(nombre)) {
            return NeonIcon.ORDER;
        }
        if (MENU_PRODUCTO.equals(nombre)) {
            return NeonIcon.PRODUCT;
        }
        if (MENU_HISTORIAL.equals(nombre)) {
            return NeonIcon.CASH;
        }
        return NeonIcon.LOGOUT;
    }
}
