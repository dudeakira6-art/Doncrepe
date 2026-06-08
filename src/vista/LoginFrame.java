package vista;

import controlador.LoginController;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import modelo.Usuario;
import vista.componentes.Recursos;

public class LoginFrame extends JFrame {
    private final PromptTextField txtUsuario = new PromptTextField("Usuario");
    private final PromptPasswordField txtPassword = new PromptPasswordField("Contraseña");
    private final LoginController controller = new LoginController();
    private final JButton btnVerPassword = new JButton();
    private char echoPassword;
    private boolean passwordVisible;

    public LoginFrame() {
        setTitle("Don Crepé - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 640);
        setMinimumSize(new Dimension(980, 580));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel izquierda = new LoginDecorPanel();
        izquierda.setPreferredSize(new Dimension(430, 0));
        izquierda.setLayout(new GridBagLayout());

        JLabel logo = new JLabel(Recursos.logo(300, 300));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        izquierda.add(logo);

        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setBackground(Color.WHITE);
        derecha.setBorder(BorderFactory.createEmptyBorder(34, 42, 34, 52));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        formulario.setPreferredSize(new Dimension(540, 510));
        formulario.setMinimumSize(new Dimension(420, 510));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel bienvenido = new JLabel("¡BIENVENIDO!");
        bienvenido.setHorizontalAlignment(SwingConstants.CENTER);
        bienvenido.setForeground(Color.BLACK);
        bienvenido.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 42, 0);
        formulario.add(bienvenido, gbc);

        JLabel titulo = new JLabel("INICIAR SESIÓN");
        titulo.setHorizontalAlignment(SwingConstants.LEFT);
        titulo.setForeground(Color.BLACK);
        titulo.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 36, 0);
        formulario.add(titulo, gbc);

        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        CampoLogin campoUsuario = new CampoLogin(txtUsuario, Recursos.icono("icon_userpink.png", 34), null);
        campoUsuario.setPreferredSize(new Dimension(520, 68));
        campoUsuario.setMinimumSize(new Dimension(420, 68));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 48, 0);
        formulario.add(campoUsuario, gbc);

        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        echoPassword = txtPassword.getEchoChar();
        configurarBotonPassword();
        CampoLogin campoPassword = new CampoLogin(txtPassword, null, btnVerPassword);
        campoPassword.setPreferredSize(new Dimension(520, 68));
        campoPassword.setMinimumSize(new Dimension(420, 68));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 88, 0);
        formulario.add(campoPassword, gbc);

        JButton ingresar = crearBotonIngresar();
        ingresar.addActionListener(e -> iniciarSesion());
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        formulario.add(ingresar, gbc);

        GridBagConstraints derechaGbc = new GridBagConstraints();
        derechaGbc.gridx = 0;
        derechaGbc.gridy = 0;
        derechaGbc.weightx = 1;
        derechaGbc.fill = GridBagConstraints.HORIZONTAL;
        derechaGbc.anchor = GridBagConstraints.CENTER;
        derecha.add(formulario, derechaGbc);
        add(izquierda, BorderLayout.WEST);
        add(derecha, BorderLayout.CENTER);
        getRootPane().setDefaultButton(ingresar);
    }

    private JButton crearBotonIngresar() {
        ImageIcon icono = imagenProporcional("boton_ingresar.png", 150, 100);
        JButton boton = new JButton(icono);
        boton.setText(icono == null ? "INGRESAR" : "");
        boton.setForeground(Estilos.ROSA_FUERTE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        Dimension tamano = icono == null
                ? new Dimension(170, 76)
                : new Dimension(icono.getIconWidth() + 20, icono.getIconHeight() + 16);
        boton.setPreferredSize(tamano);
        boton.setMinimumSize(tamano);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 45, 154), 1, true),
                        BorderFactory.createEmptyBorder(7, 7, 9, 9)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            }
        });
        return boton;
    }

    private ImageIcon imagenProporcional(String nombre, int anchoMaximo, int altoMaximo) {
        URL url = LoginFrame.class.getResource("/resources/img/" + nombre);
        if (url == null) {
            return null;
        }
        ImageIcon original = new ImageIcon(url);
        int anchoOriginal = original.getIconWidth();
        int altoOriginal = original.getIconHeight();
        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            return null;
        }
        double escala = Math.min((double) anchoMaximo / anchoOriginal, (double) altoMaximo / altoOriginal);
        int ancho = Math.max(1, (int) Math.round(anchoOriginal * escala));
        int alto = Math.max(1, (int) Math.round(altoOriginal * escala));
        Image imagen = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagen);
    }

    private void configurarBotonPassword() {
        btnVerPassword.setPreferredSize(new Dimension(46, 46));
        btnVerPassword.setBorder(BorderFactory.createEmptyBorder());
        btnVerPassword.setContentAreaFilled(false);
        btnVerPassword.setFocusPainted(false);
        btnVerPassword.setOpaque(false);
        btnVerPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnVerPassword.addActionListener(e -> alternarPassword());
        actualizarIconoPassword();
    }

    private void alternarPassword() {
        passwordVisible = !passwordVisible;
        txtPassword.setEchoChar(passwordVisible ? (char) 0 : echoPassword);
        actualizarIconoPassword();
    }

    private void actualizarIconoPassword() {
        String icono = passwordVisible ? "icon_eyepink.png" : "Icon_closedeyepink.png";
        ImageIcon imagen = Recursos.icono(icono, 34);
        btnVerPassword.setIcon(imagen);
        btnVerPassword.setToolTipText(passwordVisible ? "Ocultar contraseña" : "Mostrar contraseña");
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario y contraseña.");
            return;
        }

        try {
            Usuario autenticado = controller.autenticar(usuario, password);
            if (autenticado == null) {
                JOptionPane.showMessageDialog(this, "Credenciales inválidas.");
                return;
            }
            new MainFrame(autenticado).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con MySQL:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class LoginDecorPanel extends JPanel {
        LoginDecorPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ImageIcon fondo = Recursos.imagen("Fondo de Login.png", Math.max(1, getWidth()), Math.max(1, getHeight()));
            if (fondo != null) {
                g2.drawImage(fondo.getImage(), 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(Estilos.ROSA_CLARO);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            ImageIcon adorno = Recursos.imagen("Adorno de Inicio.png", 210, 124);
            if (adorno != null) {
                Image img = adorno.getImage();
                g2.drawImage(img, 18, getHeight() - 146, 210, 124, this);
            }
            g2.dispose();
        }
    }

    private static class CampoLogin extends JPanel {
        CampoLogin(JTextField campo, ImageIcon iconoIzquierda, JButton botonDerecha) {
            setOpaque(false);
            setLayout(new BorderLayout(12, 0));
            setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

            campo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            campo.setOpaque(false);
            campo.setForeground(Estilos.TEXTO);
            campo.setCaretColor(Estilos.ROSA_FUERTE);

            add(campo, BorderLayout.CENTER);
            if (iconoIzquierda != null) {
                JLabel icono = new JLabel(iconoIzquierda);
                icono.setPreferredSize(new Dimension(44, 44));
                icono.setHorizontalAlignment(SwingConstants.CENTER);
                add(icono, BorderLayout.EAST);
            } else if (botonDerecha != null) {
                add(botonDerecha, BorderLayout.EAST);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            g2.setColor(new Color(255, 0, 129));
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class PromptTextField extends JTextField {
        private final String prompt;

        PromptTextField(String prompt) {
            this.prompt = prompt;
            instalarRepintadoDeFoco(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                pintarPrompt(g, prompt, this);
            }
        }
    }

    private static class PromptPasswordField extends JPasswordField {
        private final String prompt;

        PromptPasswordField(String prompt) {
            this.prompt = prompt;
            instalarRepintadoDeFoco(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && !isFocusOwner()) {
                pintarPrompt(g, prompt, this);
            }
        }
    }

    private static void pintarPrompt(Graphics g, String prompt, JTextField campo) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(135, 135, 135));
        g2.setFont(campo.getFont());
        Insets insets = campo.getInsets();
        g2.drawString(prompt, insets.left, campo.getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 3);
        g2.dispose();
    }

    private static void instalarRepintadoDeFoco(final JTextField campo) {
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.repaint();
            }
        });
    }
}
