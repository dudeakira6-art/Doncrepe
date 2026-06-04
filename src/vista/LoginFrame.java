package vista;

import controlador.LoginController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import modelo.Usuario;
import vista.componentes.NeonIcon;
import vista.componentes.Recursos;
import vista.componentes.RoundedPanel;

public class LoginFrame extends JFrame {
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final LoginController controller = new LoginController();

    public LoginFrame() {
        setTitle("Don Crepe - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel izquierda = new GradientPanel();
        izquierda.setPreferredSize(new Dimension(450, 0));
        izquierda.setLayout(new GridBagLayout());
        JLabel logo = new JLabel(Recursos.logo(330, 330));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        izquierda.add(logo);

        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setBackground(Estilos.FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        RoundedPanel tarjeta = new RoundedPanel(28, Estilos.BLANCO, true);
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        GridBagConstraints form = new GridBagConstraints();
        form.gridx = 0;
        form.fill = GridBagConstraints.HORIZONTAL;
        form.insets = new Insets(7, 5, 7, 5);

        JLabel bienvenido = new JLabel("Bienvenido");
        bienvenido.setForeground(Estilos.TEXTO);
        bienvenido.setFont(new Font("Segoe UI", Font.BOLD, 30));
        tarjeta.add(bienvenido, form);

        form.gridy = 1;
        JLabel subtitulo = new JLabel("Inicia sesion para gestionar ventas");
        subtitulo.setForeground(Estilos.TEXTO_SUAVE);
        subtitulo.setFont(Estilos.fuenteNormal());
        tarjeta.add(subtitulo, form);

        form.gridy = 2;
        txtUsuario.setPreferredSize(new Dimension(250, 34));
        txtUsuario.setBorder(BorderFactory.createTitledBorder(Estilos.bordeRosa(), "Usuario"));
        txtUsuario.setFont(Estilos.fuenteNormal());
        txtUsuario.setToolTipText("Usuario");
        tarjeta.add(txtUsuario, form);

        form.gridy = 3;
        txtPassword.setPreferredSize(new Dimension(250, 34));
        txtPassword.setBorder(BorderFactory.createTitledBorder(Estilos.bordeRosa(), "Contrasena"));
        txtPassword.setFont(Estilos.fuenteNormal());
        txtPassword.setToolTipText("Contrasena");
        tarjeta.add(txtPassword, form);

        form.gridy = 4;
        JButton ingresar = Estilos.botonPrimario("Ingresar");
        ingresar.setIcon(new NeonIcon(NeonIcon.USER, 18, Color.WHITE));
        ingresar.addActionListener(e -> iniciarSesion());
        tarjeta.add(ingresar, form);

        form.gridy = 5;
        JLabel ayuda = new JLabel("Usuario demo: admin / admin");
        ayuda.setHorizontalAlignment(SwingConstants.CENTER);
        ayuda.setForeground(Estilos.TEXTO_SUAVE);
        ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tarjeta.add(ayuda, form);

        derecha.add(tarjeta, gbc);

        add(izquierda, BorderLayout.WEST);
        add(derecha, BorderLayout.CENTER);
        getRootPane().setDefaultButton(ingresar);
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario y contrasena.");
            return;
        }

        try {
            Usuario autenticado = controller.autenticar(usuario, password);
            if (autenticado == null) {
                JOptionPane.showMessageDialog(this, "Credenciales invalidas.");
                return;
            }
            new MainFrame(autenticado).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con MySQL:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class GradientPanel extends JPanel {
        GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            javax.swing.ImageIcon fondo = Recursos.imagen("Fondo de Login.png", getWidth(), getHeight());
            if (fondo != null) {
                g2.drawImage(fondo.getImage(), 0, 0, getWidth(), getHeight(), this);
            } else {
                java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, Estilos.ROSA_CLARO, getWidth(), getHeight(), new Color(255, 105, 203));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            javax.swing.ImageIcon adorno = Recursos.imagen("Adorno de Inicio.png", 170, 100);
            if (adorno != null) {
                Image img = adorno.getImage();
                g2.drawImage(img, 12, getHeight() - 116, 170, 100, this);
            }
            g2.dispose();
        }
    }
}
