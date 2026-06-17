package vista;

import controlador.PedidosController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import modelo.Comprobante;
import modelo.DetallePedido;
import modelo.Mesa;
import modelo.Pedido;
import modelo.Producto;
import modelo.ResultadoComprobante;
import modelo.Usuario;
import vista.componentes.ComprobanteDialog;
import vista.componentes.NeonIcon;
import vista.componentes.ProductCard;
import vista.componentes.Recursos;
import vista.componentes.RoundedPanel;
import vista.componentes.WrapLayout;

public class PedidosPanel extends JPanel {
    private final Usuario usuario;
    private final Runnable alGuardar;
    private final PedidosController controller = new PedidosController();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Pedido", "Cliente", "Mesa", "Total", "MÃ©todo de Pago", "Estado"}, 0);
    private JTable tablaPedidos;
    private JButton btnComprobante;

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
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonComprobante();
            }
        });
        JScrollPane scroll = new JScrollPane(tablaPedidos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Estilos.BLANCO);
        add(scroll, BorderLayout.CENTER);
        cargar();
    }

    public void refrescar() {
        cargar();
    }

    public void abrirNuevoPedido(Mesa mesa) {
        nuevoPedido(mesa);
    }

    private JPanel crearBarra() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JLabel titulo = new JLabel("Pedido");
        titulo.setFont(Estilos.fuenteTitulo());
        titulo.setForeground(Estilos.TEXTO);
        JButton nuevo = Estilos.botonSecundario("+ Nuevo Pedido");
        nuevo.setIcon(new NeonIcon(NeonIcon.ADD, 18, Estilos.ROSA_NEON));
        nuevo.addActionListener(e -> nuevoPedido(null));
        JButton pagar = Estilos.botonSecundario("Proceder con pago");
        pagar.setIcon(new NeonIcon(NeonIcon.CASH, 18, Estilos.ROSA_NEON));
        pagar.addActionListener(e -> procederPago());
        btnComprobante = Estilos.botonSecundario("Comprobante");
        btnComprobante.setEnabled(false);
        btnComprobante.addActionListener(e -> verComprobanteSeleccionado());
        JButton eliminar = Estilos.botonSecundario("Eliminar Pedido");
        eliminar.addActionListener(e -> eliminarPedido());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(nuevo);
        acciones.add(pagar);
        acciones.add(btnComprobante);
        acciones.add(eliminar);
        barra.add(titulo, BorderLayout.WEST);
        barra.add(acciones, BorderLayout.EAST);
        return barra;
    }

    private void cargar() {
        model.setRowCount(0);
        try {
            for (Pedido p : controller.listarPedidos()) {
                String mesa = p.getMesaNumero() == 0 ? "Delivery" : "Mesa " + p.getMesaNumero();
                model.addRow(new Object[]{p.getCodigo(), p.getCliente(), mesa, "S/ " + String.format("%.2f", p.getTotal()), p.getMetodoPago(), p.getEstado()});
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Sin conexiÃ³n", ex.getMessage(), "", "", "", ""});
        }
        actualizarBotonComprobante();
    }

    private void nuevoPedido(Mesa mesaPreseleccionada) {
        try {
            List<Mesa> mesas = controller.listarMesasLibres();
            if (mesaPreseleccionada != null) {
                mesas.clear();
                mesas.add(0, mesaPreseleccionada);
            }
            List<Producto> productos = controller.listarProductos();
            if (productos.isEmpty()) {
                mensaje("Debe tener productos registrados.");
                return;
            }

            JTextField cliente = new JTextField("Carlos Alberto");
            JComboBox<MesaItem> cmbMesa = new JComboBox<MesaItem>();
            for (Mesa mesa : mesas) {
                cmbMesa.addItem(new MesaItem(mesa));
            }
            JCheckBox delivery = new JCheckBox("Pedido para Delivery");
            delivery.setOpaque(false);
            delivery.setForeground(Estilos.ROSA_NEON);
            delivery.setFont(new Font("Segoe UI", Font.BOLD, 13));
            if (mesaPreseleccionada != null) {
                delivery.setSelected(false);
                cmbMesa.setEnabled(false);
            } else if (mesas.isEmpty()) {
                delivery.setSelected(true);
                cmbMesa.setEnabled(false);
            }
            delivery.addActionListener(e -> cmbMesa.setEnabled(!delivery.isSelected() && mesaPreseleccionada == null));

            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
            DefaultTableModel detalleModel = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Subtotal"}, 0);
            List<DetallePedido> detalles = new ArrayList<DetallePedido>();
            JTable tablaDetalle = new JTable(detalleModel);
            Estilos.estilizarTabla(tablaDetalle);
            JLabel total = new JLabel("Total: S/ 0.00");
            total.setFont(new Font("Segoe UI", Font.BOLD, 16));
            total.setForeground(Estilos.ROSA_NEON);

            JTabbedPane tabs = new JTabbedPane();
            tabs.add("CrepÃ©s dulces", crearTabProductos(productos, "dulce", cantidad, detalles, detalleModel, total));
            tabs.add("CrepÃ©s salados", crearTabProductos(productos, "salado", cantidad, detalles, detalleModel, total));
            tabs.add("Bebidas", crearTabProductos(productos, "bebida", cantidad, detalles, detalleModel, total));

            RoundedPanel panel = new RoundedPanel(18, Estilos.BLANCO, false);
            panel.setLayout(new BorderLayout(8, 8));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JPanel campos = new JPanel(new GridLayout(0, 2, 6, 6));
            campos.setOpaque(false);
            campos.add(labelFormulario("Cliente"));
            campos.add(cliente);
            campos.add(labelFormulario("Mesa"));
            campos.add(cmbMesa);
            campos.add(labelFormulario("Cantidad"));
            campos.add(cantidad);
            campos.add(labelFormulario("Entrega"));
            campos.add(delivery);
            campos.add(labelFormulario("Resumen"));
            campos.add(total);

            panel.add(campos, BorderLayout.NORTH);
            panel.add(tabs, BorderLayout.CENTER);
            JScrollPane detalleScroll = new JScrollPane(tablaDetalle);
            detalleScroll.setBorder(BorderFactory.createTitledBorder(Estilos.bordeRosa(), "Productos agregados"));
            panel.add(detalleScroll, BorderLayout.SOUTH);

            int ok = JOptionPane.showConfirmDialog(this, panel, "Nuevo Pedido", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }
            if (detalles.isEmpty()) {
                mensaje("Agregue al menos un producto.");
                return;
            }
            MesaItem mesa = (MesaItem) cmbMesa.getSelectedItem();
            if (!delivery.isSelected() && mesa == null) {
                mensaje("No hay mesas libres disponibles. Marque Delivery o libere una mesa.");
                return;
            }
            controller.crearPedido(usuario, delivery.isSelected() ? null : mesa.getMesa(), cliente.getText().trim(), "Pendiente", detalles, delivery.isSelected());
            mensaje("Pedido agregado correctamente.");
            cargar();
            notificarCambios();
        } catch (Exception ex) {
            mensajeError("No se pudo crear pedido:\n" + ex.getMessage());
        }
    }

    private JScrollPane crearTabProductos(List<Producto> productos, String categoria, JSpinner cantidad,
            List<DetallePedido> detalles, DefaultTableModel detalleModel, JLabel total) {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 12));
        panel.setOpaque(false);
        for (Producto producto : productos) {
            if (!categoriaProducto(producto).equals(categoria)) {
                continue;
            }
            ProductCard card = new ProductCard(producto);
            card.setToolTipText("Click para agregar");
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    int cant = ((Integer) cantidad.getValue()).intValue();
                    DetallePedido detalle = new DetallePedido(producto, cant);
                    detalles.add(detalle);
                    detalleModel.addRow(new Object[]{producto.getNombre(), cant, "S/ " + String.format("%.2f", detalle.getSubtotal())});
                    total.setText("Total: S/ " + String.format("%.2f", controller.calcularTotal(detalles)));
                }
            });
            panel.add(card);
        }
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new java.awt.Dimension(620, 260));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(Estilos.FONDO);
        return scroll;
    }

    private void procederPago() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            mensaje("Seleccione un pedido para pagar.");
            return;
        }
        String codigo = tablaPedidos.getValueAt(fila, 0).toString();
        String estado = tablaPedidos.getValueAt(fila, 5).toString();
        if ("COMPLETADO".equalsIgnoreCase(estado)) {
            mensaje("El pedido seleccionado ya fue pagado.");
            return;
        }
        try {
            Pedido pedido = controller.buscarPedido(codigo);
            List<DetallePedido> detalles = controller.listarDetalles(codigo);
            ResultadoComprobante resultado = mostrarDialogoPago(pedido, detalles);
            if (resultado == null) {
                return;
            }
            mensaje("Pago registrado correctamente.");
            cargar();
            notificarCambios();
            ComprobanteDialog.mostrar(this, resultado.getPedido(), resultado.getDetalles(), resultado.getComprobante(), resultado.getArchivoPdf());
        } catch (Exception ex) {
            mensajeError("No se pudo procesar el pago:\n" + ex.getMessage());
        }
    }

    private ResultadoComprobante mostrarDialogoPago(Pedido pedido, List<DetallePedido> detalles) throws Exception {
        JComboBox<String> metodo = new JComboBox<String>(new String[]{"Efectivo", "Tarjeta", "Yape"});
        JComboBox<String> tipo = new JComboBox<String>(new String[]{"Boleta simple", "Boleta con DNI", "Factura"});
        JCheckBox confirmado = new JCheckBox("Confirmo que el pago fue recibido/aprobado");
        confirmado.setOpaque(false);
        JLabel qr = new JLabel(Recursos.imagen("Código_QR.jpg", 160, 160));
        qr.setVisible(false);

        JTextField nombreBoleta = new JTextField(pedido.getCliente());
        JTextField dniBoleta = new JTextField();
        JTextField nombreFactura = new JTextField(pedido.getCliente());
        JTextField rucFactura = new JTextField();
        JTextField razonFactura = new JTextField();
        JTextField direccionFactura = new JTextField();

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel superior = new JPanel(new GridLayout(0, 2, 8, 8));
        superior.add(labelFormulario("Método de pago"));
        superior.add(metodo);
        superior.add(labelFormulario("Comprobante"));
        superior.add(tipo);

        JLabel ayuda = new JLabel("Boleta simple: no requiere datos del cliente.");
        ayuda.setForeground(Estilos.TEXTO_SUAVE);
        ayuda.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        CardLayout tarjetasLayout = new CardLayout();
        JPanel tarjetas = new JPanel(tarjetasLayout);
        tarjetas.setOpaque(false);
        tarjetas.add(crearTarjetaBoletaSimple(ayuda), Comprobante.BOLETA_SIMPLE);
        tarjetas.add(crearTarjetaBoletaDni(nombreBoleta, dniBoleta), Comprobante.BOLETA_DNI);
        tarjetas.add(crearTarjetaFactura(nombreFactura, rucFactura, razonFactura, direccionFactura), Comprobante.FACTURA);
        tarjetasLayout.show(tarjetas, Comprobante.BOLETA_SIMPLE);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(superior, BorderLayout.NORTH);
        centro.add(tarjetas, BorderLayout.CENTER);
        centro.add(confirmado, BorderLayout.SOUTH);
        panel.add(centro, BorderLayout.CENTER);
        panel.add(qr, BorderLayout.EAST);
        metodo.addActionListener(e -> {
            qr.setVisible("Yape".equals(metodo.getSelectedItem()));
            panel.revalidate();
            panel.repaint();
        });
        tipo.addActionListener(e -> {
            tarjetasLayout.show(tarjetas, tipoComprobante(tipo.getSelectedItem().toString()));
            panel.revalidate();
            panel.repaint();
        });

        int ok = JOptionPane.showConfirmDialog(this, panel, "Pago y comprobante", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return null;
        }
        if (!confirmado.isSelected()) {
            throw new IllegalArgumentException("Debe confirmar que el pago fue recibido o aprobado.");
        }
        String tipoValor = tipoComprobante(tipo.getSelectedItem().toString());
        if (Comprobante.BOLETA_SIMPLE.equals(tipoValor)) {
            return controller.procesarPagoYGenerarComprobante(pedido.getCodigo(), metodo.getSelectedItem().toString(), tipoValor,
                    "", "", "", "", "", new File("reportes/comprobantes"));
        }
        if (Comprobante.BOLETA_DNI.equals(tipoValor)) {
            return controller.procesarPagoYGenerarComprobante(pedido.getCodigo(), metodo.getSelectedItem().toString(), tipoValor,
                    nombreBoleta.getText(), dniBoleta.getText(), "", "", "", new File("reportes/comprobantes"));
        }
        return controller.procesarPagoYGenerarComprobante(pedido.getCodigo(), metodo.getSelectedItem().toString(), tipoValor,
                nombreFactura.getText(), "", rucFactura.getText(), razonFactura.getText(), direccionFactura.getText(),
                new File("reportes/comprobantes"));
    }

    private JPanel crearTarjetaBoletaSimple(JLabel ayuda) {
        RoundedPanel panel = new RoundedPanel(18, Estilos.FONDO_SUAVE, true);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setLayout(new BorderLayout());
        JLabel titulo = new JLabel("Boleta simple");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titulo.setForeground(Estilos.ROSA_NEON);
        panel.add(titulo, BorderLayout.NORTH);
        panel.add(ayuda, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearTarjetaBoletaDni(JTextField nombre, JTextField dni) {
        RoundedPanel panel = new RoundedPanel(18, Estilos.FONDO_SUAVE, true);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setLayout(new GridLayout(0, 2, 8, 8));
        panel.add(labelFormulario("Nombre"));
        panel.add(nombre);
        panel.add(labelFormulario("DNI"));
        panel.add(dni);
        return panel;
    }

    private JPanel crearTarjetaFactura(JTextField nombre, JTextField ruc, JTextField razon, JTextField direccion) {
        RoundedPanel panel = new RoundedPanel(18, Estilos.FONDO_SUAVE, true);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setLayout(new GridLayout(0, 2, 8, 8));
        panel.add(labelFormulario("Nombre"));
        panel.add(nombre);
        panel.add(labelFormulario("RUC"));
        panel.add(ruc);
        panel.add(labelFormulario("Razón social"));
        panel.add(razon);
        panel.add(labelFormulario("Dirección"));
        panel.add(direccion);
        return panel;
    }

    private void verComprobanteSeleccionado() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            mensaje("Seleccione un pedido completado.");
            return;
        }
        String codigo = tablaPedidos.getValueAt(fila, 0).toString();
        String estado = tablaPedidos.getValueAt(fila, 5).toString();
        if (!"COMPLETADO".equalsIgnoreCase(estado)) {
            mensaje("El pedido todavÃ­a no tiene comprobante porque estÃ¡ pendiente.");
            return;
        }
        try {
            ResultadoComprobante resultado = controller.obtenerComprobante(codigo);
            ComprobanteDialog.mostrar(this, resultado.getPedido(), resultado.getDetalles(), resultado.getComprobante(), resultado.getArchivoPdf());
        } catch (Exception ex) {
            mensajeError("No se pudo abrir el comprobante:\n" + ex.getMessage());
        }
    }

    private void actualizarBotonComprobante() {
        if (btnComprobante == null) {
            return;
        }
        int fila = tablaPedidos == null ? -1 : tablaPedidos.getSelectedRow();
        if (fila < 0) {
            btnComprobante.setText("Comprobante");
            btnComprobante.setEnabled(false);
            return;
        }
        String estado = tablaPedidos.getValueAt(fila, 5).toString();
        if (!"COMPLETADO".equalsIgnoreCase(estado)) {
            btnComprobante.setText("Comprobante");
            btnComprobante.setEnabled(false);
            return;
        }
        String codigo = tablaPedidos.getValueAt(fila, 0).toString();
        try {
            Comprobante comprobante = controller.buscarComprobante(codigo);
            if (comprobante == null) {
                btnComprobante.setText("Comprobante");
                btnComprobante.setEnabled(false);
                return;
            }
            btnComprobante.setText(Comprobante.FACTURA.equals(comprobante.getTipo()) ? "Factura" : "Boleta");
            btnComprobante.setEnabled(true);
        } catch (Exception ex) {
            btnComprobante.setText("Comprobante");
            btnComprobante.setEnabled(false);
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
            mensaje("Seleccione un pedido para eliminar.");
            return;
        }
        String codigo = tablaPedidos.getValueAt(fila, 0).toString();
        int ok = JOptionPane.showConfirmDialog(this, "Â¿Eliminar el pedido " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.eliminarPedido(codigo);
            mensaje("Pedido eliminado correctamente.");
            cargar();
            notificarCambios();
        } catch (Exception ex) {
            mensajeError("No se pudo eliminar el pedido:\n" + ex.getMessage());
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

    private String tipoComprobante(String visible) {
        if ("Factura".equals(visible)) {
            return Comprobante.FACTURA;
        }
        if ("Boleta con DNI".equals(visible)) {
            return Comprobante.BOLETA_DNI;
        }
        return Comprobante.BOLETA_SIMPLE;
    }

    private void mensaje(String texto) {
        JOptionPane.showMessageDialog(this, panelMensaje(texto, false), "Don CrepÃ©", JOptionPane.PLAIN_MESSAGE);
    }

    private void mensajeError(String texto) {
        JOptionPane.showMessageDialog(this, panelMensaje(texto, true), "Error", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel panelMensaje(String texto, boolean error) {
        RoundedPanel panel = new RoundedPanel(20, Estilos.BLANCO, true);
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JLabel icono = new JLabel(new NeonIcon(error ? NeonIcon.EXCEL : NeonIcon.CASH, 28, error ? Estilos.ROJO : Estilos.ROSA_NEON));
        JLabel mensaje = new JLabel("<html><body style='width:320px'>" + texto.replace("\n", "<br>") + "</body></html>");
        mensaje.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mensaje.setForeground(error ? Estilos.ROJO : Estilos.TEXTO);
        panel.add(icono, BorderLayout.WEST);
        panel.add(mensaje, BorderLayout.CENTER);
        return panel;
    }

    private void notificarCambios() {
        if (alGuardar != null) {
            alGuardar.run();
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



