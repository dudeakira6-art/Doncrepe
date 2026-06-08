package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import modelo.Mesa;
import modelo.Usuario;
import vista.componentes.MenuLateral;
import vista.componentes.NeonIcon;
import vista.componentes.Recursos;

public class MainFrame extends JFrame {
    private final Usuario usuario;
    private final JPanel contenido = new JPanel(new CardLayout());
    private final MenuLateral menu = new MenuLateral();
    private MesasPanel mesasPanel;
    private PedidosPanel pedidosPanel;

    public MainFrame(Usuario usuario) {
        this.usuario = usuario;
        setTitle("Don Crepe - Sistema de Gestion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(menu, BorderLayout.WEST);
        add(crearHeader(), BorderLayout.NORTH);
        contenido.setBackground(Estilos.FONDO);
        add(contenido, BorderLayout.CENTER);

        cargarPaneles();
        configurarMenu();
        mostrar("Inicio");
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Estilos.BLANCO);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilos.ROSA_CLARO),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        marca.setOpaque(false);
        ImageIcon logo = Recursos.logo(42, 42);
        JLabel logoLabel = new JLabel(logo);
        JLabel titulo = new JLabel("Don Crepe POS");
        titulo.setFont(Estilos.fuenteSubtitulo());
        titulo.setForeground(Estilos.TEXTO);
        marca.add(logoLabel);
        marca.add(titulo);

        JLabel empleado = new JLabel(usuario.getNombre() + "    " + usuario.getRol());
        empleado.setIcon(new NeonIcon(NeonIcon.USER, 20, Estilos.ROSA_NEON));
        empleado.setIconTextGap(8);
        empleado.setHorizontalAlignment(SwingConstants.RIGHT);
        empleado.setForeground(Estilos.TEXTO_SUAVE);
        header.add(marca, BorderLayout.WEST);
        header.add(empleado, BorderLayout.EAST);
        return header;
    }

    private void cargarPaneles() {
        contenido.add(new InicioPanel(usuario, new java.util.function.Consumer<String>() {
            @Override
            public void accept(String destino) {
                mostrar(destino);
            }
        }), "Inicio");
        mesasPanel = new MesasPanel(new java.util.function.Consumer<Mesa>() {
            @Override
            public void accept(Mesa mesa) {
                mostrar("Pedido");
                pedidosPanel.abrirNuevoPedido(mesa);
            }
        });
        contenido.add(mesasPanel, "Mesa");
        pedidosPanel = new PedidosPanel(usuario, new Runnable() {
            @Override
            public void run() {
                refrescarDatos();
            }
        });
        contenido.add(pedidosPanel, "Pedido");
        contenido.add(new ProductosPanel(), "Producto");
        contenido.add(new HistorialCajaPanel(), "Historial Caja");
    }

    private void refrescarDatos() {
        if (mesasPanel != null) {
            mesasPanel.refrescar();
        }
        if (pedidosPanel != null) {
            pedidosPanel.refrescar();
        }
    }

    private void configurarMenu() {
        String[] opciones = {"Inicio", "Mesa", "Pedido", "Producto", "Historial Caja"};
        for (String opcion : opciones) {
            menu.getBoton(opcion).addActionListener(e -> mostrar(e.getActionCommand()));
            menu.getBoton(opcion).setActionCommand(opcion);
        }
        menu.getBoton("Cerrar Sesion").addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void mostrar(String nombre) {
        menu.marcarActivo(nombre);
        ((CardLayout) contenido.getLayout()).show(contenido, nombre);
    }
}
