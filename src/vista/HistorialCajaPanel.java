package vista;

import controlador.HistorialCajaController;
import java.awt.BorderLayout;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Caja;
import vista.componentes.NeonIcon;
import vista.componentes.RoundedPanel;

public class HistorialCajaPanel extends JPanel {
    private final JTextField fecha = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Pedido", "Monto", "Metodo", "Fecha", "Tipo"}, 0);
    private final JLabel total = new JLabel("Total: S/ 0.00");
    private final HistorialCajaController controller = new HistorialCajaController();

    public HistorialCajaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(crearBarra(), BorderLayout.NORTH);
        JTable tabla = new JTable(model);
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
        JButton exportar = Estilos.botonSecundario("Exportar Excel");
        exportar.setIcon(new NeonIcon(NeonIcon.EXCEL, 18, Estilos.ROSA_NEON));
        exportar.addActionListener(e -> exportarExcel());
        filtros.add(new JLabel("Fecha"));
        filtros.add(fecha);
        filtros.add(buscar);
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
            List<Caja> movimientos = controller.listarPorFecha(fecha.getText().trim());
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Caja c : movimientos) {
                model.addRow(new Object[]{c.getCodigoPedido(), "S/ " + String.format("%.2f", c.getMonto()), c.getMetodoPago(), fmt.format(c.getFecha()), c.getTipoMovimiento()});
            }
            suma = controller.sumarMovimientos(movimientos);
            total.setText("Total: S/ " + String.format("%.2f", suma));
        } catch (Exception ex) {
            model.addRow(new Object[]{"Sin conexion", ex.getMessage(), "", "", ""});
        }
    }

    private void exportarExcel() {
        try {
            File archivo = controller.exportarExcel(fecha.getText().trim(), new File("reportes"));
            JOptionPane.showMessageDialog(this, "Reporte exportado correctamente:\n" + archivo.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar el reporte:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
