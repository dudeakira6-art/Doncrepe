package vista;

import controlador.PedidosController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import modelo.DetallePedido;
import modelo.Mesa;
import modelo.Pedido;
import modelo.Producto;
import modelo.Usuario;
import vista.componentes.NeonIcon;
import vista.componentes.RoundedPanel;

public class PedidosPanel extends JPanel {
    private final Usuario usuario;
    private final Runnable alGuardar;
    private final PedidosController controller = new PedidosController();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Pedido", "Cliente", "Mesa", "Total", "Metodo de Pago", "Estado"}, 0);
    private JTable tablaPedidos;

    public PedidosPanel(Usuario usuario, Runnable alGuardar) {
        this.usuario = usuario;
        this.alGuardar = alGuardar;
        setLayout(new BorderLayout(10, 10));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(crearBarra(), BorderLayout.NORTH);
        tablaPedidos = new JTable(model);
        tablaPedidos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        Estilos.estilizarTabla(tablaPedidos);
        JScrollPane scroll = new JScrollPane(tablaPedidos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Estilos.BLANCO);
        add(scroll, BorderLayout.CENTER);
        cargar();
    }

    private JPanel crearBarra() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JLabel titulo = new JLabel("Pedido");
        titulo.setFont(Estilos.fuenteTitulo());
        titulo.setForeground(Estilos.TEXTO);
        JButton nuevo = Estilos.botonSecundario("+ Nuevo Pedido");
        nuevo.setIcon(new NeonIcon(NeonIcon.ADD, 18, Estilos.ROSA_NEON));
        nuevo.addActionListener(e -> nuevoPedido());
        JButton eliminar = Estilos.botonSecundario("Eliminar Pedido");
        eliminar.addActionListener(e -> eliminarPedido());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(nuevo);
        acciones.add(eliminar);
        barra.add(titulo, BorderLayout.WEST);
        barra.add(acciones, BorderLayout.EAST);
        return barra;
    }

    private void cargar() {
        model.setRowCount(0);
        try {
            List<Pedido> pedidos = controller.listarPedidos();
            for (Pedido p : pedidos) {
                String mesa = p.getMesaNumero() == 0 ? "Delivery" : "Mesa " + p.getMesaNumero();
                model.addRow(new Object[]{p.getCodigo(), p.getCliente(), mesa, "S/ " + String.format("%.2f", p.getTotal()), p.getMetodoPago(), p.getEstado()});
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Sin conexion", ex.getMessage(), "", "", "", ""});
        }
    }

    private void nuevoPedido() {
        try {
            List<Mesa> mesas = controller.listarMesasLibres();
            List<Producto> productos = controller.listarProductos();
            if (productos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe tener productos registrados.");
                return;
            }

            JTextField cliente = new JTextField("Carlos Alberto");
            JComboBox<MesaItem> cmbMesa = new JComboBox<MesaItem>();
            for (Mesa mesa : mesas) {
                cmbMesa.addItem(new MesaItem(mesa));
            }
            JComboBox<Producto> cmbProducto = new JComboBox<Producto>();
            for (Producto producto : productos) {
                cmbProducto.addItem(producto);
            }
            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
            JComboBox<String> metodo = new JComboBox<String>(new String[]{"Efectivo", "Tarjeta", "Yape", "Transferencia"});
            JCheckBox delivery = new JCheckBox("Pedido para Delivery");
            delivery.setOpaque(false);
            delivery.setForeground(Estilos.ROSA_NEON);
            delivery.setFont(new Font("Segoe UI", Font.BOLD, 13));
            if (mesas.isEmpty()) {
                delivery.setSelected(true);
                cmbMesa.setEnabled(false);
                delivery.setEnabled(false);
            }
            delivery.addActionListener(e -> cmbMesa.setEnabled(!delivery.isSelected()));

            DefaultTableModel detalleModel = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Subtotal"}, 0);
            List<DetallePedido> detalles = new ArrayList<DetallePedido>();
            JTable tablaDetalle = new JTable(detalleModel);
            JLabel total = new JLabel("Total: S/ 0.00");
            total.setFont(new Font("Segoe UI", Font.BOLD, 16));
            total.setForeground(Estilos.ROSA_NEON);

            JButton agregar = Estilos.botonSecundario("Agregar item");
            agregar.setIcon(new NeonIcon(NeonIcon.ADD, 16, Estilos.ROSA_NEON));
            agregar.addActionListener(e -> {
                Producto producto = (Producto) cmbProducto.getSelectedItem();
                int cant = ((Integer) cantidad.getValue()).intValue();
                DetallePedido detalle = new DetallePedido(producto, cant);
                detalles.add(detalle);
                detalleModel.addRow(new Object[]{producto.getNombre(), cant, "S/ " + String.format("%.2f", detalle.getSubtotal())});
                total.setText("Total: S/ " + String.format("%.2f", controller.calcularTotal(detalles)));
            });

            RoundedPanel panel = new RoundedPanel(18, Estilos.BLANCO, false);
            panel.setLayout(new BorderLayout(8, 8));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JPanel campos = new JPanel(new GridLayout(0, 2, 6, 6));
            campos.setOpaque(false);
            campos.add(labelFormulario("Cliente"));
            campos.add(cliente);
            campos.add(labelFormulario("Mesa"));
            campos.add(cmbMesa);
            campos.add(labelFormulario("Producto"));
            campos.add(cmbProducto);
            campos.add(labelFormulario("Cantidad"));
            campos.add(cantidad);
            campos.add(labelFormulario("Metodo de pago"));
            campos.add(metodo);
            campos.add(labelFormulario("Entrega"));
            campos.add(delivery);
            campos.add(agregar);
            campos.add(total);
            panel.add(campos, BorderLayout.NORTH);
            Estilos.estilizarTabla(tablaDetalle);
            panel.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);

            int ok = JOptionPane.showConfirmDialog(this, panel, "Nuevo Pedido", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }
            if (detalles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Agregue al menos un producto.");
                return;
            }
            MesaItem mesa = (MesaItem) cmbMesa.getSelectedItem();
            if (!delivery.isSelected() && mesa == null) {
                JOptionPane.showMessageDialog(this, "No hay mesas libres disponibles. Marque Delivery o libere una mesa.");
                return;
            }
            controller.crearPedido(usuario, delivery.isSelected() ? null : mesa.getMesa(), cliente.getText().trim(), metodo.getSelectedItem().toString(), detalles, delivery.isSelected());
            JOptionPane.showMessageDialog(this, "Pedido guardado correctamente.");
            cargar();
            if (alGuardar != null) {
                alGuardar.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo crear pedido:\n" + ex.getMessage());
        }
    }

    private JLabel labelFormulario(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(Estilos.ROSA_NEON);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private void eliminarPedido() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para eliminar.");
            return;
        }
        String codigo = tablaPedidos.getValueAt(fila, 0).toString();
        int ok = JOptionPane.showConfirmDialog(this, "Eliminar el pedido " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.eliminarPedido(codigo);
            cargar();
            if (alGuardar != null) {
                alGuardar.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el pedido:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class MesaItem {
        private final Mesa mesa;

        MesaItem(Mesa mesa) {
            this.mesa = mesa;
        }

        Mesa getMesa() {
            return mesa;
        }

        @Override
        public String toString() {
            return "Mesa " + mesa.getNumero() + " (" + mesa.getEstado() + ")";
        }
    }
}
