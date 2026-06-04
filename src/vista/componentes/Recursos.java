package vista.componentes;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import org.apache.commons.lang3.StringUtils;

public final class Recursos {
    private Recursos() {
    }

    public static ImageIcon logo(int ancho, int alto) {
        return imagen("logo.png", ancho, alto);
    }

    public static ImageIcon imagen(String nombre, int ancho, int alto) {
        if (StringUtils.isBlank(nombre)) {
            return null;
        }
        URL url = Recursos.class.getResource("/resources/img/" + nombre);
        if (url == null) {
            return null;
        }
        ImageIcon original = new ImageIcon(url);
        Image escalada = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }

    public static ImageIcon icono(String nombre, int size) {
        return imagen(nombre, size, size);
    }

    public static String imagenProducto(String nombreProducto, String imagenGuardada) {
        if (StringUtils.isNotBlank(imagenGuardada) && existe(imagenGuardada.trim())) {
            return imagenGuardada.trim();
        }
        String nombre = StringUtils.stripAccents(StringUtils.lowerCase(StringUtils.trimToEmpty(nombreProducto)));
        if (nombre.contains("fresa")) {
            return "Crepé de Fresa.png";
        }
        if (nombre.contains("nutella")) {
            return "Crepe de Nutella.png";
        }
        if (nombre.contains("platano") || nombre.contains("banana")) {
            return "Crepe platano.png";
        }
        if (nombre.contains("dulce") || nombre.contains("leche")) {
            return "Crepe Dulce de Leche.png";
        }
        if (nombre.contains("arandano")) {
            return "Crepé de Arándanos.png";
        }
        if (nombre.contains("jamon") || nombre.contains("queso")) {
            return "Crepe Jamón y Queso.png";
        }
        if (nombre.contains("pollo")) {
            return "Crepé de Pollo.png";
        }
        if (nombre.contains("champinon")) {
            return "Crepé de Champiñones.png";
        }
        if (nombre.contains("vegetar")) {
            return "Crep+e Vegetariano.png";
        }
        if (nombre.contains("huevo")) {
            return "Crepé de Huevo.png";
        }
        if (nombre.contains("cafe")) {
            return "cafe.png";
        }
        if (nombre.contains("frappe")) {
            return "Frapuccino.png";
        }
        if (nombre.contains("naranja") || nombre.contains("jugo")) {
            return "Jugo de Naranja.png";
        }
        if (nombre.contains("batido") || nombre.contains("smoothie") || nombre.contains("fruta")) {
            return "Jugo de Frutas.png";
        }
        if (nombre.contains("coca") || nombre.contains("cola")) {
            return "Coca Cola.png";
        }
        return "";
    }

    public static boolean existe(String nombre) {
        return StringUtils.isNotBlank(nombre) && Recursos.class.getResource("/resources/img/" + nombre) != null;
    }
}
