package vista.componentes;

import java.awt.Image;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
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
        for (Map.Entry<String, String> entrada : imagenesPorClave().entrySet()) {
            if (contieneAlgunaClave(nombre, entrada.getKey())) {
                return entrada.getValue();
            }
        }
        return "";
    }

    public static boolean existe(String nombre) {
        return StringUtils.isNotBlank(nombre) && Recursos.class.getResource("/resources/img/" + nombre) != null;
    }

    private static Map<String, String> imagenesPorClave() {
        Map<String, String> imagenes = new LinkedHashMap<String, String>();
        imagenes.put("fresa", "Crepé de Fresa.png");
        imagenes.put("nutella", "Crepe de Nutella.png");
        imagenes.put("platano|banana", "Crepe platano.png");
        imagenes.put("dulce|leche", "Crepe Dulce de Leche.png");
        imagenes.put("arandano", "Crepé de Arándanos.png");
        imagenes.put("jamon|queso", "Crepe Jamón y Queso.png");
        imagenes.put("pollo", "Crepé de Pollo.png");
        imagenes.put("champinon", "Crepé de Champiñones.png");
        imagenes.put("vegetar", "Crep+e Vegetariano.png");
        imagenes.put("huevo", "Crepé de Huevo.png");
        imagenes.put("cafe", "cafe.png");
        imagenes.put("frappe", "Frapuccino.png");
        imagenes.put("naranja|jugo", "Jugo de Naranja.png");
        imagenes.put("batido|smoothie|fruta", "Jugo de Frutas.png");
        imagenes.put("coca|cola", "Coca Cola.png");
        return imagenes;
    }

    private static boolean contieneAlgunaClave(String texto, String claves) {
        for (String clave : claves.split("\\|")) {
            if (texto.contains(clave)) {
                return true;
            }
        }
        return false;
    }
}
