package servicio;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import modelo.DetallePedido;
import modelo.Pedido;

public class BoletaService {
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public File generar(Pedido pedido, List<DetallePedido> detalles, File carpetaDestino) throws IOException {
        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdirs();
        }
        File archivo = new File(carpetaDestino, "boleta_" + pedido.getCodigo() + ".txt");
        List<String> lineas = new ArrayList<String>();
        lineas.add("========================================");
        lineas.add("              DON CREPÉ");
        lineas.add("              BOLETA DE VENTA");
        lineas.add("========================================");
        lineas.add("Pedido: " + pedido.getCodigo());
        lineas.add("Cliente: " + pedido.getCliente());
        lineas.add("Fecha: " + formatoFecha.format(pedido.getFecha()));
        lineas.add("Atención: " + (pedido.getMesaNumero() == 0 ? "Delivery" : "Mesa " + pedido.getMesaNumero()));
        lineas.add("Método de pago: " + pedido.getMetodoPago());
        lineas.add("----------------------------------------");
        lineas.add(String.format("%-22s %5s %9s", "Producto", "Cant.", "Subtotal"));
        lineas.add("----------------------------------------");
        for (DetallePedido detalle : detalles) {
            lineas.add(String.format("%-22s %5d S/ %6.2f",
                    recortar(detalle.getProducto().getNombre(), 22),
                    detalle.getCantidad(),
                    detalle.getSubtotal()));
        }
        lineas.add("----------------------------------------");
        lineas.add(String.format("TOTAL: S/ %.2f", pedido.getTotal()));
        lineas.add("Estado: " + pedido.getEstado());
        lineas.add("========================================");
        lineas.add("Gracias por su compra.");
        Files.write(archivo.toPath(), lineas, StandardCharsets.UTF_8);
        return archivo;
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
