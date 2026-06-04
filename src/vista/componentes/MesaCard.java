package vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import modelo.Mesa;
import vista.Estilos;

public class MesaCard extends RoundedPanel {
    public MesaCard(Mesa mesa) {
        super(24, Estilos.BLANCO, true);
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(135, 150));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel estado = new JLabel(mesa.getEstado());
        estado.setHorizontalAlignment(SwingConstants.CENTER);
        estado.setOpaque(true);
        boolean libre = "LIBRE".equalsIgnoreCase(mesa.getEstado());
        estado.setBackground(libre ? new Color(217, 255, 232) : new Color(255, 224, 234));
        estado.setForeground(libre ? Estilos.VERDE : Estilos.ROJO);
        estado.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        estado.setFont(new Font("Segoe UI", Font.BOLD, 12));

        ImageIcon mesaIcon = Recursos.imagen("mesa.png", 58, 58);
        JLabel mesaGrafica = new JLabel(mesaIcon != null ? mesaIcon : new NeonIcon(NeonIcon.TABLE, 50, libre ? Estilos.CELESTE : Estilos.ROSA_NEON));
        mesaGrafica.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel numero = new JLabel("Mesa " + mesa.getNumero());
        numero.setHorizontalAlignment(SwingConstants.CENTER);
        numero.setForeground(Estilos.TEXTO);
        numero.setFont(new Font("Segoe UI", Font.BOLD, 15));

        add(estado, BorderLayout.NORTH);
        add(mesaGrafica, BorderLayout.CENTER);
        add(numero, BorderLayout.SOUTH);
    }
}
