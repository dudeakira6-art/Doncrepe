package servicio;

import java.util.List;
import modelo.DetallePedido;

public class CalculadoraPedido {
    public double calcularTotal(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (DetallePedido detalle : detalles) {
            total += detalle.getSubtotal();
        }
        return total;
    }
}
