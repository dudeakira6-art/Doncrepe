package vista.componentes;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import modelo.Comprobante;
import modelo.DetallePedido;
import modelo.Pedido;
import servicio.ComprobanteService;
import vista.Estilos;

public final class ComprobanteDialog {
    private ComprobanteDialog() {
    }

    public static void mostrar(JPanel parent, Pedido pedido, java.util.List<DetallePedido> detalles,
            Comprobante comprobante, File archivoPdf) {
        ComprobanteService service = new ComprobanteService();
        JTextArea vista = new JTextArea(service.construirVista(pedido, detalles, comprobante));
        vista.setEditable(false);
        vista.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
        vista.setForeground(Estilos.TEXTO);
        vista.setBackground(Estilos.BLANCO);

        JPanel panel = new RoundedPanel(18, Estilos.BLANCO, true);
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scroll = new JScrollPane(vista);
        scroll.setPreferredSize(new Dimension(520, 390));
        panel.add(scroll, BorderLayout.CENTER);

        Object[] opciones = {"Imprimir", "Guardar PDF", "Cerrar"};
        int seleccion = JOptionPane.showOptionDialog(parent, panel, comprobante.getNombreVisible(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[2]);
        if (seleccion == 0) {
            imprimir(parent, archivoPdf);
        } else if (seleccion == 1) {
            guardarComo(parent, archivoPdf);
        }
    }

    private static void imprimir(JPanel parent, File archivoPdf) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.PRINT)) {
                Desktop.getDesktop().print(archivoPdf);
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivoPdf);
            } else {
                throw new IllegalStateException("La impresión del sistema no está disponible.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo imprimir o abrir el PDF:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void guardarComo(JPanel parent, File archivoPdf) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(archivoPdf.getName()));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.copy(archivoPdf.toPath(), chooser.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(parent, "PDF guardado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo guardar el PDF:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
