package vista;

import controlador.InicioController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.Pedido;
import modelo.Usuario;
import vista.componentes.NeonIcon;
import vista.componentes.RoundedPanel;

public class InicioPanel extends JPanel {
    private transient final Usuario usuario;
    private transient final Consumer<String> navegar;
    private transient final InicioController controller = new InicioController();
    private static final DateTimeFormatter FECHA_CABECERA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_ACTIVIDAD = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public InicioPanel(Usuario usuario) {
        this(usuario, null);
    }

    public InicioPanel(Usuario usuario, Consumer<String> navegar) {
        this.usuario = usuario;
        this.navegar = navegar;
        setLayout(new BorderLayout(12, 12));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        construir();
    }

    private void construir() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        JLabel saludo = new JLabel("Bienvenido, " + usuario.getNombre() + "!");
        saludo.setFont(Estilos.fuenteTitulo());
        saludo.setForeground(Estilos.TEXTO);
        JLabel fecha = new JLabel(LocalDate.now().format(FECHA_CABECERA));
        fecha.setOpaque(true);
        fecha.setForeground(Estilos.ROSA_NEON);
        fecha.setBackground(Estilos.BLANCO);
        fecha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.ROSA_CLARO),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        cabecera.add(saludo, BorderLayout.WEST);
        cabecera.add(fecha, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(12, 12));
        centro.setOpaque(false);
        centro.add(crearResumen(), BorderLayout.NORTH);
        JPanel inferior = new JPanel(new BorderLayout(12, 12));
        inferior.setOpaque(false);
        inferior.add(crearAccesosRapidos(), BorderLayout.NORTH);
        inferior.add(crearActividad(), BorderLayout.CENTER);
        centro.add(inferior, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private JPanel crearResumen() {
        JPanel resumen = new JPanel(new GridLayout(1, 4, 14, 14));
        resumen.setOpaque(false);
        try {
            resumen.add(tarjeta("Ventas del dia", "S/ " + String.format("%.2f", controller.ventasHoy()), NeonIcon.CASH));
            resumen.add(tarjeta("Caja actual", "S/ " + String.format("%.2f", controller.cajaActual()), NeonIcon.EXCEL));
            resumen.add(tarjeta("Pedidos hoy", String.valueOf(controller.pedidosHoy()), NeonIcon.ORDER));
            resumen.add(tarjeta("Pendientes", String.valueOf(controller.pedidosPendientes()), NeonIcon.ORDER));
        } catch (Exception ex) {
            resumen.add(tarjeta("MySQL", "Sin conexion", NeonIcon.CASH));
            resumen.add(tarjeta("Revise", "README", NeonIcon.ORDER));
            resumen.add(tarjeta("Usuario", "admin", NeonIcon.USER));
            resumen.add(tarjeta("Clave", "admin", NeonIcon.LOCK));
        }
        return resumen;
    }

    private JPanel crearAccesosRapidos() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 8));
        contenedor.setOpaque(false);
        JLabel titulo = new JLabel("Acceso rapido");
        titulo.setFont(Estilos.fuenteSubtitulo());
        titulo.setForeground(Estilos.TEXTO);
        JPanel accesos = new JPanel(new GridLayout(1, 3, 14, 14));
        accesos.setOpaque(false);
        accesos.add(tarjetaAcceso("Mesa", "Administrar mesas", NeonIcon.TABLE, "Mesa"));
        accesos.add(tarjetaAcceso("Productos", "Gestionar productos", NeonIcon.PRODUCT, "Producto"));
        accesos.add(tarjetaAcceso("Pedido", "Gestionar pedidos", NeonIcon.ORDER, "Pedido"));
        contenedor.add(titulo, BorderLayout.NORTH);
        contenedor.add(accesos, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel tarjetaAcceso(String titulo, String descripcion, String icono, String destino) {
        RoundedPanel panel = new RoundedPanel(20, Estilos.FONDO_SUAVE, false);
        panel.setLayout(new BorderLayout(10, 2));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 151, 211)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel icon = new JLabel(new NeonIcon(icono, 28, Estilos.ROSA_NEON));
        JLabel nombre = new JLabel(titulo);
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nombre.setForeground(Estilos.TEXTO);
        JLabel desc = new JLabel(descripcion);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(Estilos.TEXTO_SUAVE);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(nombre);
        textos.add(desc);
        panel.add(icon, BorderLayout.WEST);
        panel.add(textos, BorderLayout.CENTER);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (navegar != null) {
                    navegar.accept(destino);
                }
            }
        });
        return panel;
    }

    private JPanel tarjeta(String titulo, String valor, String icono) {
        RoundedPanel panel = new RoundedPanel(24, Estilos.BLANCO, true);
        panel.setLayout(new BorderLayout(10, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panel.setPreferredSize(new Dimension(0, 95));
        JLabel icon = new JLabel(new NeonIcon(icono, 32, Estilos.ROSA_NEON));
        icon.setHorizontalAlignment(JLabel.CENTER);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilos.fuenteNormal());
        lblTitulo.setForeground(Estilos.TEXTO_SUAVE);
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(Estilos.TEXTO);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(lblTitulo);
        textos.add(lblValor);
        panel.add(icon, BorderLayout.WEST);
        panel.add(textos, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane crearActividad() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Pedido", "Mesa", "Fecha", "Total", "Estado"}, 0);
        try {
            List<Pedido> pedidos = controller.actividadReciente();
            for (Pedido p : pedidos) {
                String mesa = p.getMesaNumero() == 0 ? "Delivery" : "Mesa " + p.getMesaNumero();
                model.addRow(new Object[]{p.getCodigo(), mesa, formatearFecha(p.getFecha()), "S/ " + String.format("%.2f", p.getTotal()), p.getEstado()});
            }
        } catch (Exception ex) {
            model.addRow(new Object[]{"Sin conexion a MySQL", "-", "-", "-", ex.getMessage()});
        }
        JTable tabla = new JTable(model);
        Estilos.estilizarTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(Estilos.bordeRosa(), "Actividad Reciente"));
        scroll.getViewport().setBackground(Estilos.BLANCO);
        return scroll;
    }

    private String formatearFecha(java.util.Date fecha) {
        if (fecha == null) {
            return "-";
        }
        return Instant.ofEpochMilli(fecha.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(FECHA_ACTIVIDAD);
    }
}
