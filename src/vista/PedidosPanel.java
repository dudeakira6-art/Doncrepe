package vista;

import controlador.PedidosController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
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
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Pedido", "Cliente", "Mesa", "Total", "Método de Pago", "Estado"}, 0);
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
            model.addRow(new Object[]{"Sin conexión", ex.getMessage(), "", "", "", ""});
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
            tabs.add("Crepés dulces", crearTabProductos(productos, "dulce", cantidad, detalles, detalleModel, total));
            tabs.add("Crepés salados", crearTabProductos(productos, "salado", cantidad, detalles, detalleModel, total));
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
        JTextField nombre = new JTextField(pedido.getCliente());
        JTextField dni = new JTextField();
        JTextField ruc = new JTextField();
        JTextField razon = new JTextField();
        JTextField direccion = new JTextField();
        JTextField email = new JTextField();
        JTextField telefono = new JTextField();
        JCheckBox confirmado = new JCheckBox("Confirmo que el pago fue recibido/aprobado");
        confirmado.setOpaque(false);
        JLabel qr = new JLabel(Recursos.imagen("Código_QR.jpg", 160, 160));
        qr.setVisible(false);

        // Labels de error inline
        javax.swing.JLabel nombreErr = new javax.swing.JLabel("");
        nombreErr.setForeground(Color.RED);
        nombreErr.setVisible(false);
        javax.swing.JLabel dniErr = new javax.swing.JLabel("");
        dniErr.setForeground(Color.RED);
        dniErr.setVisible(false);
        javax.swing.JLabel rucErr = new javax.swing.JLabel("");
        rucErr.setForeground(Color.RED);
        rucErr.setVisible(false);
        javax.swing.JLabel razonErr = new javax.swing.JLabel("");
        razonErr.setForeground(Color.RED);
        razonErr.setVisible(false);
        javax.swing.JLabel direccionErr = new javax.swing.JLabel("");
        direccionErr.setForeground(Color.RED);
        direccionErr.setVisible(false);
        javax.swing.JLabel emailErr = new javax.swing.JLabel("");
        emailErr.setForeground(Color.RED);
        emailErr.setVisible(false);
        javax.swing.JLabel telefonoErr = new javax.swing.JLabel("");
        telefonoErr.setForeground(Color.RED);
        telefonoErr.setVisible(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel campos = new JPanel();
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));
        campos.setOpaque(false);

        // fila método
        JPanel filaMetodo = new JPanel(new BorderLayout(6, 6));
        filaMetodo.setOpaque(false);
        filaMetodo.add(labelFormulario("Método de pago"), BorderLayout.WEST);
        filaMetodo.add(metodo, BorderLayout.CENTER);
        campos.add(filaMetodo);

        // fila tipo
        JPanel filaTipo = new JPanel(new BorderLayout(6, 6));
        filaTipo.setOpaque(false);
        filaTipo.add(labelFormulario("Comprobante"), BorderLayout.WEST);
        filaTipo.add(tipo, BorderLayout.CENTER);
        campos.add(filaTipo);

        // fila nombre
        JPanel filaNombre = new JPanel(new BorderLayout(6, 6));
        filaNombre.setOpaque(false);
        filaNombre.add(labelFormulario("Nombre"), BorderLayout.WEST);
        filaNombre.add(nombre, BorderLayout.CENTER);
        filaNombre.add(nombreErr, BorderLayout.EAST);
        campos.add(filaNombre);

        // fila dni
        JPanel filaDni = new JPanel(new BorderLayout(6, 6));
        filaDni.setOpaque(false);
        filaDni.add(labelFormulario("DNI"), BorderLayout.WEST);
        filaDni.add(dni, BorderLayout.CENTER);
        filaDni.add(dniErr, BorderLayout.EAST);
        campos.add(filaDni);

        // fila ruc
        JPanel filaRuc = new JPanel(new BorderLayout(6, 6));
        filaRuc.setOpaque(false);
        filaRuc.add(labelFormulario("RUC"), BorderLayout.WEST);
        filaRuc.add(ruc, BorderLayout.CENTER);
        filaRuc.add(rucErr, BorderLayout.EAST);
        campos.add(filaRuc);

        // fila razon
        JPanel filaRazon = new JPanel(new BorderLayout(6, 6));
        filaRazon.setOpaque(false);
        filaRazon.add(labelFormulario("Razón social"), BorderLayout.WEST);
        filaRazon.add(razon, BorderLayout.CENTER);
        filaRazon.add(razonErr, BorderLayout.EAST);
        campos.add(filaRazon);

        // fila direccion
        JPanel filaDireccion = new JPanel(new BorderLayout(6, 6));
        filaDireccion.setOpaque(false);
        filaDireccion.add(labelFormulario("Dirección"), BorderLayout.WEST);
        filaDireccion.add(direccion, BorderLayout.CENTER);
        filaDireccion.add(direccionErr, BorderLayout.EAST);
        campos.add(filaDireccion);

        // fila email
        JPanel filaEmail = new JPanel(new BorderLayout(6, 6));
        filaEmail.setOpaque(false);
        filaEmail.add(labelFormulario("Email"), BorderLayout.WEST);
        filaEmail.add(email, BorderLayout.CENTER);
        filaEmail.add(emailErr, BorderLayout.EAST);
        campos.add(filaEmail);

        // fila telefono
        JPanel filaTelefono = new JPanel(new BorderLayout(6, 6));
        filaTelefono.setOpaque(false);
        filaTelefono.add(labelFormulario("Teléfono"), BorderLayout.WEST);
        filaTelefono.add(telefono, BorderLayout.CENTER);
        filaTelefono.add(telefonoErr, BorderLayout.EAST);
        campos.add(filaTelefono);

        // fila confirmado
        JPanel filaConfirmado = new JPanel(new BorderLayout());
        filaConfirmado.setOpaque(false);
        filaConfirmado.add(new JLabel(""), BorderLayout.WEST);
        filaConfirmado.add(confirmado, BorderLayout.CENTER);
        campos.add(filaConfirmado);

        panel.add(campos, BorderLayout.CENTER);
        panel.add(qr, BorderLayout.EAST);

        // helper para actualizar visibilidad según tipo
        java.util.function.Consumer<String> ajustarSegunTipo = (seleccion) -> {
            boolean esBoletaSimple = "Boleta simple".equals(seleccion);
            boolean esBoletaDni = "Boleta con DNI".equals(seleccion);
            boolean esFactura = "Factura".equals(seleccion);

            // Nombre: visible en boleta con dni, oculto en factura y boleta simple
            filaNombre.setVisible(esBoletaDni);
            nombreErr.setVisible(false);

            // DNI: visible solo en boleta con dni
            filaDni.setVisible(esBoletaDni);
            dniErr.setVisible(false);

            // RUC / Razon / Direccion: visibles solo en Factura
            filaRuc.setVisible(esFactura);
            filaRazon.setVisible(esFactura);
            filaDireccion.setVisible(esFactura);
            filaEmail.setVisible(esFactura);
            filaTelefono.setVisible(esFactura);
            rucErr.setVisible(false);
            razonErr.setVisible(false);
            direccionErr.setVisible(false);
            emailErr.setVisible(false);
            telefonoErr.setVisible(false);

            // Si boleta simple ocultamos todos los campos de cliente
            if (esBoletaSimple) {
                filaNombre.setVisible(false);
                filaDni.setVisible(false);
                filaRuc.setVisible(false);
                filaRazon.setVisible(false);
                filaDireccion.setVisible(false);
            }
            panel.revalidate();
            panel.repaint();
        };

        tipo.addActionListener(e -> ajustarSegunTipo.accept(tipo.getSelectedItem().toString()));
        // inicializar visibilidad
        ajustarSegunTipo.accept(tipo.getSelectedItem().toString());

        metodo.addActionListener(e -> {
            qr.setVisible("Yape".equals(metodo.getSelectedItem()));
            panel.revalidate();
            panel.repaint();
        });

        // Mostrar diálogo en loop para permitir validación inline sin cerrar inmediatamente
        while (true) {
            int ok = JOptionPane.showConfirmDialog(this, panel, "Pago y comprobante", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return null;
            }
            if (!confirmado.isSelected()) {
                JOptionPane.showMessageDialog(this, "Debe confirmar que el pago fue recibido o aprobado.", "Atención", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // limpiar errores
            nombreErr.setText(""); nombreErr.setVisible(false);
            dniErr.setText(""); dniErr.setVisible(false);
            rucErr.setText(""); rucErr.setVisible(false);
            razonErr.setText(""); razonErr.setVisible(false);
            direccionErr.setText(""); direccionErr.setVisible(false);

            String tipoVisible = tipo.getSelectedItem().toString();
            String tipoValor = tipoComprobante(tipoVisible);
            boolean valido = true;

            if ("Boleta con DNI".equals(tipoVisible)) {
                if (nombre.getText().trim().isEmpty()) {
                    nombreErr.setText("Nombre obligatorio");
                    nombreErr.setVisible(true);
                    if (valido) { nombre.requestFocus(); }
                    valido = false;
                }
                if (!dni.getText().trim().matches("\\d{8}")) {
                    dniErr.setText("DNI inválido (8 dígitos)");
                    dniErr.setVisible(true);
                    if (valido) { dni.requestFocus(); }
                    valido = false;
                }
            } else if ("Factura".equals(tipoVisible)) {
                if (!ruc.getText().trim().matches("\\d{11}")) {
                    rucErr.setText("RUC inválido (11 dígitos)");
                    rucErr.setVisible(true);
                    if (valido) { ruc.requestFocus(); }
                    valido = false;
                }
                if (razon.getText().trim().isEmpty()) {
                    razonErr.setText("Razón social obligatoria");
                    razonErr.setVisible(true);
                    if (valido) { razon.requestFocus(); }
                    valido = false;
                }
                if (direccion.getText().trim().isEmpty()) {
                    direccionErr.setText("Dirección obligatoria");
                    direccionErr.setVisible(true);
                    if (valido) { direccion.requestFocus(); }
                    valido = false;
                }
                if (email.getText().trim().isEmpty()) {
                    emailErr.setText("Email obligatorio");
                    emailErr.setVisible(true);
                    if (valido) { email.requestFocus(); }
                    valido = false;
                } else if (!email.getText().trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                    emailErr.setText("Email inválido");
                    emailErr.setVisible(true);
                    if (valido) { email.requestFocus(); }
                    valido = false;
                }
                if (!telefono.getText().trim().matches("\\d{6,15}")) {
                    telefonoErr.setText("Teléfono inválido (6-15 dígitos)");
                    telefonoErr.setVisible(true);
                    if (valido) { telefono.requestFocus(); }
                    valido = false;
                }
            }

            if (!valido) {
                // volver a mostrar el diálogo con errores inline
                continue;
            }

                // enviar al controlador
                return controller.procesarPagoYGenerarComprobante(pedido.getCodigo(), metodo.getSelectedItem().toString(), tipoValor,
                    // Para Boleta simple dejamos vacío (sin datos de cliente)
                    "Boleta simple".equals(tipoVisible) ? "" : nombre.getText(),
                    dni.getText(), ruc.getText(), razon.getText(), direccion.getText(), email.getText(), telefono.getText(),
                    new File("reportes/comprobantes"));
        }
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
            mensaje("El pedido todavía no tiene comprobante porque está pendiente.");
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
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar el pedido " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
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
        JOptionPane.showMessageDialog(this, panelMensaje(texto, false), "Don Crepé", JOptionPane.PLAIN_MESSAGE);
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
