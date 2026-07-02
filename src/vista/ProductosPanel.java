package vista;

import controlador.ProductosController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
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
    private static final String CATEGORIA_DULCE = "Crepe dulce";
    private static final String CATEGORIA_SALADO = "Crepe salado";
    private static final String CATEGORIA_BEBIDA = "Bebida";
    private static final String CLAVE_DULCE = "crepe dulce";
    private static final String CLAVE_SALADO = "crepe salado";
    private static final String CLAVE_BEBIDA = "bebida";
    private final JTabbedPane tabs = new JTabbedPane();
    private final transient ProductosController controller = new ProductosController();

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
            tabs.add("Crepés dulces", crearScrollCategoria(productos, "crepe dulce"));
            tabs.add("Crepés salados", crearScrollCategoria(productos, "crepe salado"));
            tabs.add("Bebidas", crearScrollCategoria(productos, "bebida"));
        } catch (SQLException ex) {
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
            if (!categoriaClave(producto.getCategoria()).equals(categoria)) {
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
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
        javax.swing.JComboBox<String> categoria = new javax.swing.JComboBox<String>(
                new String[]{"Crepe dulce", "Crepe salado", "Bebida"});
        JTextField precio = new JTextField(producto == null ? "" : String.valueOf(producto.getPrecio()));
        JTextField imagen = new JTextField(producto == null ? "" : producto.getImagen());
        categoria.setSelectedItem(categoriaNormalizada(producto == null ? "Crepe dulce" : producto.getCategoria()));

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
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
            Producto nuevo = new Producto(
                    producto == null ? 0 : producto.getIdProducto(),
                    nombre.getText().trim(),
                    categoria.getSelectedItem().toString().trim(),
                    valor,
                    imagen.getText().trim(),
                    true);
            controller.guardarProducto(nuevo);
            cargar();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un precio válido.");
        } catch (SQLException ex) {
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
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar:\n" + ex.getMessage());
        }
    }

    private String categoriaNormalizada(String categoria) {
        String valor = categoria == null ? "" : categoria.trim().toLowerCase();
        if (valor.contains("bebida")) {
            return "Bebida";
        }
        if (valor.contains("salado")) {
            return "Crepe salado";
        }
        return "Crepe dulce";
    }

    private String categoriaClave(String categoria) {
        String valor = categoria == null ? "" : categoria.trim().toLowerCase();
        if (valor.contains("bebida")) {
            return "bebida";
        }
        if (valor.contains("salado")) {
            return "crepe salado";
        }
        return "crepe dulce";
    }
}
