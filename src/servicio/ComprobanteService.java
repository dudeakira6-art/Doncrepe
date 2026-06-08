package servicio;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import modelo.Comprobante;
import modelo.DetallePedido;
import modelo.Pedido;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class ComprobanteService {
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public File generarPdf(Pedido pedido, List<DetallePedido> detalles, Comprobante comprobante, File carpetaDestino) throws IOException {
        if (!carpetaDestino.exists() && !carpetaDestino.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de comprobantes.");
        }
        File archivo = new File(comprobante.getArchivoPdf());
        if (archivo.getParentFile() == null) {
            archivo = new File(carpetaDestino, comprobante.getArchivoPdf());
        }
        File carpetaArchivo = archivo.getParentFile();
        if (carpetaArchivo != null && !carpetaArchivo.exists() && !carpetaArchivo.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta del comprobante.");
        }
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                escribirLineas(contenido, construirLineas(pedido, detalles, comprobante));
            }
            documento.save(archivo);
        }
        return archivo;
    }

    public String construirVista(Pedido pedido, List<DetallePedido> detalles, Comprobante comprobante) {
        StringBuilder sb = new StringBuilder();
        for (String linea : construirLineas(pedido, detalles, comprobante)) {
            sb.append(linea).append(System.lineSeparator());
        }
        return sb.toString();
    }

    private List<String> construirLineas(Pedido pedido, List<DetallePedido> detalles, Comprobante comprobante) {
        List<String> lineas = new ArrayList<String>();
        lineas.add("DON CREPÉ");
        lineas.add(comprobante.getNombreVisible().toUpperCase() + "  " + comprobante.getNumero());
        lineas.add("Pedido: " + pedido.getCodigo());
        lineas.add("Fecha: " + formatoFecha.format(pedido.getFecha()));
        lineas.add("Atención: " + (pedido.getMesaNumero() == 0 ? "Delivery" : "Mesa " + pedido.getMesaNumero()));
        lineas.add("Método de pago: " + pedido.getMetodoPago());
        lineas.add("");
        if (Comprobante.FACTURA.equals(comprobante.getTipo())) {
            lineas.add("RUC: " + comprobante.getRuc());
            lineas.add("Razón social: " + comprobante.getRazonSocial());
            lineas.add("Dirección: " + comprobante.getDireccion());
        } else {
            if (comprobante.getClienteNombre() != null && !comprobante.getClienteNombre().trim().isEmpty()) {
                lineas.add("Cliente: " + comprobante.getClienteNombre());
            }
            if (Comprobante.BOLETA_DNI.equals(comprobante.getTipo()) && comprobante.getDni() != null && !comprobante.getDni().trim().isEmpty()) {
                lineas.add("DNI: " + comprobante.getDni());
            }
        }
        lineas.add("");
        lineas.add(String.format("%-24s %5s %10s", "Producto", "Cant.", "Subtotal"));
        lineas.add("------------------------------------------");
        for (DetallePedido detalle : detalles) {
            lineas.add(String.format("%-24s %5d S/ %7.2f",
                    recortar(detalle.getProducto().getNombre(), 24),
                    detalle.getCantidad(),
                    detalle.getSubtotal()));
        }
        lineas.add("------------------------------------------");
        lineas.add(String.format("TOTAL: S/ %.2f", pedido.getTotal()));
        lineas.add("");
        lineas.add("Gracias por su compra.");
        return lineas;
    }

    private void escribirLineas(PDPageContentStream contenido, List<String> lineas) throws IOException {
        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA_BOLD, 18);
        contenido.newLineAtOffset(60, 780);
        contenido.showText(sanitizar(lineas.get(0)));
        contenido.setFont(PDType1Font.HELVETICA, 11);
        contenido.setLeading(16);
        contenido.newLine();
        for (int i = 1; i < lineas.size(); i++) {
            contenido.showText(sanitizar(lineas.get(i)));
            contenido.newLine();
        }
        contenido.endText();
    }

    private String sanitizar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("é", "e")
                .replace("É", "E")
                .replace("á", "a")
                .replace("Á", "A")
                .replace("í", "i")
                .replace("Í", "I")
                .replace("ó", "o")
                .replace("Ó", "O")
                .replace("ú", "u")
                .replace("Ú", "U")
                .replace("ñ", "n")
                .replace("Ñ", "N");
    }

    private String recortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= maximo) {
            return texto;
        }
        return texto.substring(0, maximo - 3) + "...";
    }
}
