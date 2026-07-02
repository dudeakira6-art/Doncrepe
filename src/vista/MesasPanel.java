package vista;

import controlador.MesasController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import modelo.Mesa;
import vista.componentes.MesaCard;
import vista.componentes.WrapLayout;

public class MesasPanel extends JPanel {
    private final JPanel grilla = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
    private final MesasController controller = new MesasController();
    private final Consumer<Mesa> alOcuparMesa;

    public MesasPanel() {
        this(null);
    }

    public MesasPanel(Consumer<Mesa> alOcuparMesa) {
        this.alOcuparMesa = alOcuparMesa;
        setLayout(new BorderLayout(10, 10));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel titulo = new JLabel("Mesas");
        titulo.setFont(Estilos.fuenteTitulo());
        titulo.setForeground(Estilos.TEXTO);
        add(titulo, BorderLayout.NORTH);
        grilla.setOpaque(false);
        cargar();
        JScrollPane scroll = new JScrollPane(grilla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Estilos.FONDO);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void refrescar() {
        cargar();
    }

    private void cargar() {
        grilla.removeAll();
        try {
            List<Mesa> mesas = controller.listarMesas();
            for (Mesa mesa : mesas) {
                MesaCard card = new MesaCard(mesa);
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        cambiarEstado(mesa);
                    }
                });
                grilla.add(card);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar mesas:\n" + ex.getMessage());
        }
    }

    private void cambiarEstado(Mesa mesa) {
        String nuevo = "LIBRE".equalsIgnoreCase(mesa.getEstado()) ? "OCUPADO" : "LIBRE";
        if ("OCUPADO".equals(nuevo) && alOcuparMesa != null) {
            int abrir = JOptionPane.showConfirmDialog(this,
                    "¿Crear un pedido para la Mesa " + mesa.getNumero() + "?", "Mesas", JOptionPane.YES_NO_OPTION);
            if (abrir == JOptionPane.YES_OPTION) {
                alOcuparMesa.accept(mesa);
            }
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Cambiar Mesa " + mesa.getNumero() + " a " + nuevo + "?", "Mesas", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.alternarEstado(mesa);
            cargar();
            revalidate();
            repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cambiar estado:\n" + ex.getMessage());
        }
    }
}
