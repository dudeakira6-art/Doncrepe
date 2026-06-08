package vista;

import controlador.ProductosController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import modelo.Producto;
import vista.componentes.NeonIcon;
import vista.componentes.ProductCard;
import vista.componentes.WrapLayout;

public class ProductosPanel extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane();
    private final ProductosController controller = new ProductosController();

    public ProductosPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(crearBarra(), BorderLayout.NORTH);
        tabs.setBackground(Estilos.FONDO);
        add(tabs, BorderLayout.CENTER);
        cargar();
    }

    private JPanel crearBarra() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JLabel titulo = new JLabel("Producto");
        titulo.setFont(Estilos.fuenteTitulo());
        titulo.setForeground(Estilos.TEXTO);
        JButton agregar = Estilos.botonSecundario("Agregar producto");
        agregar.setIcon(new NeonIcon(NeonIcon.ADD, 18, Estilos.ROSA_NEON));
        agregar.addActionListener(e -> mostrarFormulario(null));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(agregar);
        barra.add(titulo, BorderLayout.WEST);
        barra.add(acciones, BorderLayout.EAST);
        return barra;
    }

    private void cargar() {
        tabs.removeAll();
        try {
            List<Producto> productos = controller.listarProductos();
            tabs.add("Crepés dulces", crearScrollCategoria(productos, "dulce"));
            tabs.add("Crepés salados", crearScrollCategoria(productos, "salado"));
            tabs.add("Bebidas", crearScrollCategoria(productos, "bebida"));
        } catch (Exception ex) {
            JPanel error = new JPanel();
            error.setOpaque(false);
            error.add(new JLabel("No se pudieron cargar productos: " + ex.getMessage()));
            tabs.add("Error", error);
        }
        revalidate();
        repaint();
    }

    private JScrollPane crearScrollCategoria(List<Producto> productos, String categoria) {
        JPanel grilla = new JPanel(new WrapLayout(FlowLayout.LEFT, 14, 14));
        grilla.setOpaque(false);
        for (Producto producto : productos) {
            if (!categoriaProducto(producto).equals(categoria)) {
                continue;
            }
            ProductCard card = new ProductCard(producto);
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    mostrarOpciones(producto);
                }
            });
            grilla.add(card);
        }
        JScrollPane scroll = new JScrollPane(grilla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Estilos.FONDO);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void mostrarOpciones(Producto producto) {
        Object[] opciones = {"Editar", "Eliminar", "Cancelar"};
        int seleccion = JOptionPane.showOptionDialog(this, producto.getNombre(), "Producto",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (seleccion == 0) {
            mostrarFormulario(producto);
        } else if (seleccion == 1) {
            eliminar(producto);
        }
    }

    private void mostrarFormulario(Producto producto) {
        JTextField nombre = new JTextField(producto == null ? "" : producto.getNombre());
        JTextField categoria = new JTextField(producto == null ? "Crepe" : producto.getCategoria());
        JTextField precio = new JTextField(producto == null ? "" : String.valueOf(producto.getPrecio()));
        JTextField imagen = new JTextField(producto == null ? "" : producto.getImagen());

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Nombre"));
        panel.add(nombre);
        panel.add(new JLabel("Categoría"));
        panel.add(categoria);
        panel.add(new JLabel("Precio"));
        panel.add(precio);
        panel.add(new JLabel("Imagen (opcional)"));
        panel.add(imagen);

        int ok = JOptionPane.showConfirmDialog(this, panel, producto == null ? "Agregar producto" : "Editar producto", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double valor = Double.parseDouble(precio.getText().trim());
            if (producto == null) {
                controller.guardarProducto(new Producto(0, nombre.getText().trim(), categoria.getText().trim(), valor, imagen.getText().trim(), true));
            } else {
                Producto actualizado = new Producto(producto.getIdProducto(), nombre.getText().trim(), categoria.getText().trim(), valor, imagen.getText().trim(), true);
                controller.guardarProducto(actualizado);
            }
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar producto:\n" + ex.getMessage());
        }
    }

    private void eliminar(Producto producto) {
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar " + producto.getNombre() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.eliminarProducto(producto);
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar:\n" + ex.getMessage());
        }
    }

    private String categoriaProducto(Producto producto) {
        String nombre = producto.getNombre().toLowerCase();
        if (nombre.contains("cafe") || nombre.contains("frappe") || nombre.contains("jugo") || nombre.contains("batido") || nombre.contains("coca")) {
            return "bebida";
        }
        if (nombre.contains("jamon") || nombre.contains("queso") || nombre.contains("pollo") || nombre.contains("champi") || nombre.contains("veget") || nombre.contains("huevo")) {
            return "salado";
        }
        return "dulce";
    }
}
