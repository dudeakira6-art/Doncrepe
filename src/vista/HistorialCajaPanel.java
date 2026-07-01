package vista;

import controlador.HistorialCajaController;
import controlador.PedidosController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import modelo.Caja;
import modelo.ResultadoComprobante;
import vista.componentes.ComprobanteDialog;
import vista.componentes.NeonIcon;
import vista.componentes.RoundedPanel;

public class HistorialCajaPanel extends JPanel {
    private final JSpinner fechaDesde = crearSpinnerFecha();
    private final JSpinner fechaHasta = crearSpinnerFecha();
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Pedido", "Monto", "Método", "Fecha", "Tipo"}, 0);
    private final JLabel total = new JLabel("Total: S/ 0.00");
    private final HistorialCajaController controller = new HistorialCajaController();
    private final PedidosController pedidosController = new PedidosController();
    private JTable tabla;

    public HistorialCajaPanel() {
        ponerFechaActual(fechaDesde);
        ponerFechaActual(fechaHasta);
        setLayout(new BorderLayout(10, 10));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(crearBarra(), BorderLayout.NORTH);
        tabla = new JTable(model);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        Estilos.estilizarTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Estilos.BLANCO);
        add(scroll, BorderLayout.CENTER);
        add(crearTotalPanel(), BorderLayout.SOUTH);
        cargar();
    }

    private JPanel crearBarra() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JLabel titulo = new JLabel("Historial Caja");
        titulo.setFont(Estilos.fuenteTitulo());
        titulo.setForeground(Estilos.TEXTO);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtros.setOpaque(false);

        JButton buscar = Estilos.botonSecundario("Buscar");
        buscar.addActionListener(e -> cargar());

        JButton comprobante = Estilos.botonSecundario("Ver comprobante");
        comprobante.addActionListener(e -> verComprobante());

        JButton exportar = Estilos.botonSecundario("Exportar Excel");
        exportar.setIcon(new NeonIcon(NeonIcon.EXCEL, 18, Estilos.ROSA_NEON));
        exportar.addActionListener(e -> exportarExcel());

        filtros.add(new JLabel("Desde"));
        filtros.add(fechaDesde);
        filtros.add(new JLabel("Hasta"));
        filtros.add(fechaHasta);
        filtros.add(buscar);
        filtros.add(comprobante);
        filtros.add(exportar);
        barra.add(titulo, BorderLayout.WEST);
        barra.add(filtros, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearTotalPanel() {
        RoundedPanel panel = new RoundedPanel(18, Estilos.BLANCO, true);
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        total.setFont(new Font("Segoe UI", Font.BOLD, 17));
        total.setForeground(Estilos.ROSA_NEON);
        panel.add(total);
        return panel;
    }

    private void cargar() {
        model.setRowCount(0);
        double suma = 0;
        try {
            String desde = formatoSql(fechaDesde.getValue());
            String hasta = formatoSql(fechaHasta.getValue());
            List<Caja> movimientos = controller.listarPorRango(desde, hasta);
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Caja c : movimientos) {
                model.addRow(new Object[]{
                    c.getCodigoPedido(),
                    "S/ " + String.format("%.2f", c.getMonto()),
                    c.getMetodoPago(),
                    fmt.format(c.getFecha()),
                    c.getTipoMovimiento()
                });
            }
            suma = controller.sumarMovimientos(movimientos);
            total.setText("Total: S/ " + String.format("%.2f", suma));
        } catch (Exception ex) {
            model.addRow(new Object[]{"Sin conexión", ex.getMessage(), "", "", ""});
        }
    }

    private void verComprobante() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un movimiento de caja.");
            return;
        }
        String codigo = tabla.getValueAt(fila, 0).toString();
        try {
            ResultadoComprobante resultado = pedidosController.obtenerComprobante(codigo);
            ComprobanteDialog.mostrar(this, resultado.getPedido(), resultado.getDetalles(), resultado.getComprobante(), resultado.getArchivoPdf());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el comprobante:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportarExcel() {
        try {
            String desde = formatoSql(fechaDesde.getValue());
            String hasta = formatoSql(fechaHasta.getValue());
            File archivo = controller.exportarExcel(desde, hasta, new File("reportes"));
            JOptionPane.showMessageDialog(this, "Reporte exportado correctamente:\n" + archivo.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar el reporte:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JSpinner crearSpinnerFecha() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(120, 30));
        return spinner;
    }

    private void ponerFechaActual(JSpinner spinner) {
        spinner.setValue(new Date());
    }

    private String formatoSql(Object valor) {
        Date fecha = valor instanceof Date ? (Date) valor : new Date();
        return new SimpleDateFormat("yyyy-MM-dd").format(fecha);
    }
}
