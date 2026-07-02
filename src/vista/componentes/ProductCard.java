package vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import modelo.Producto;
import vista.Estilos;

public class ProductCard extends RoundedPanel {
    private final transient Producto producto;

    public ProductCard(Producto producto) {
        super(22, Estilos.BLANCO, true);
        this.producto = producto;
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(150, 145));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel nombre = new JLabel("<html><center>" + producto.getNombre() + "</center></html>");
        nombre.setHorizontalAlignment(SwingConstants.CENTER);
        nombre.setForeground(Estilos.TEXTO);
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 12));

        ImageIcon imagen = Recursos.imagen(Recursos.imagenProducto(producto.getNombre(), producto.getImagen()), 78, 58);
        JLabel icono = new JLabel(imagen != null ? imagen : new NeonIcon(NeonIcon.PRODUCT, 42, Estilos.ROSA_NEON));
        icono.setHorizontalAlignment(SwingConstants.CENTER);
        icono.setOpaque(true);
        icono.setBackground(new Color(255, 232, 246));
        icono.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JLabel precio = new JLabel("S/ " + String.format("%.2f", producto.getPrecio()));
        precio.setHorizontalAlignment(SwingConstants.CENTER);
        precio.setOpaque(true);
        precio.setBackground(Estilos.ROSA_NEON);
        precio.setForeground(Color.WHITE);
        precio.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        precio.setFont(new Font("Segoe UI", Font.BOLD, 13));

        add(nombre, BorderLayout.NORTH);
        add(icono, BorderLayout.CENTER);
        add(precio, BorderLayout.SOUTH);
    }

    public Producto getProducto() {
        return producto;
    }
}
