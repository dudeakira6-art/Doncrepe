package tdd;

import controlador.HistorialCajaController;
import controlador.PedidosController;
import controlador.ProductosController;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import modelo.Caja;
import modelo.DetallePedido;
import modelo.Pedido;
import modelo.Producto;
import servicio.BoletaService;
import servicio.CalculadoraPedido;
import servicio.ReporteCajaExcelService;

public class PruebasTDD {
    private int pruebasEjecutadas = 0;

    public static void main(String[] args) {
        PruebasTDD pruebas = new PruebasTDD();
        pruebas.debeCalcularSubtotalDeDetalle();
        pruebas.debeCalcularTotalDePedido();
        pruebas.debeRetornarCeroSiPedidoNoTieneDetalles();
        pruebas.debeValidarPrecioMayorACero();
        pruebas.debeNormalizarProductoConApacheCommons();
        pruebas.debeNormalizarClienteYResumirPedidoConGuava();
        pruebas.debeSumarMovimientosDeCaja();
        pruebas.debeExportarCajaConApachePOI();
        pruebas.debeGenerarBoletaDePedido();
        System.out.println("TDD OK - pruebas ejecutadas: " + pruebas.pruebasEjecutadas);
    }

    private void debeCalcularSubtotalDeDetalle() {
        Producto producto = new Producto(1, "Crepe de Nutella", "Crepe", 11.00, "", true);
        DetallePedido detalle = new DetallePedido(producto, 2);

        assertDouble(22.00, detalle.getSubtotal(), "El subtotal debe ser precio por cantidad.");
    }

    private void debeCalcularTotalDePedido() {
        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        detalles.add(new DetallePedido(new Producto(1, "Crepe de Fresa", "Crepe", 12.00, "", true), 1));
        detalles.add(new DetallePedido(new Producto(2, "Cafe", "Bebida", 5.00, "", true), 2));

        double total = new CalculadoraPedido().calcularTotal(detalles);

        assertDouble(22.00, total, "El total debe sumar todos los subtotales.");
    }

    private void debeRetornarCeroSiPedidoNoTieneDetalles() {
        double total = new CalculadoraPedido().calcularTotal(new ArrayList<DetallePedido>());

        assertDouble(0.00, total, "Un pedido sin detalles debe valer cero.");
    }

    private void debeValidarPrecioMayorACero() {
        ProductosController controller = new ProductosController(null);
        Producto producto = new Producto(0, "Crepe Test", "Crepe", 0.00, "", true);

        try {
            controller.guardarProducto(producto);
            throw new AssertionError("Se esperaba error por precio invalido.");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("precio"), "El mensaje debe mencionar el precio.");
        } catch (Exception ex) {
            throw new AssertionError("No se esperaba otro tipo de error: " + ex.getMessage());
        }
    }

    private void debeNormalizarProductoConApacheCommons() {
        ProductosController controller = new ProductosController(null);
        Producto producto = new Producto(0, "  crepe de nutella  ", "  crepe dulce ", 11.00, "  img.png  ", true);
        Producto normalizado = controller.normalizarProducto(producto);

        assertTrue("Crepe De Nutella".equals(normalizado.getNombre()), "Apache Commons debe limpiar y capitalizar el producto.");
        assertTrue("Crepe Dulce".equals(normalizado.getCategoria()), "Apache Commons debe limpiar y capitalizar la categoria.");
        assertTrue("img.png".equals(normalizado.getImagen()), "Apache Commons debe limpiar la ruta de imagen.");
    }

    private void debeNormalizarClienteYResumirPedidoConGuava() {
        PedidosController controller = new PedidosController(null, null, null, new CalculadoraPedido());
        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        detalles.add(new DetallePedido(new Producto(1, "Crepe de Fresa", "Crepe", 12.00, "", true), 1));
        detalles.add(new DetallePedido(new Producto(2, "Cafe", "Bebida", 5.00, "", true), 2));

        assertTrue("Carlos Alberto".equals(controller.normalizarCliente("  carlos alberto ")), "Apache Commons debe normalizar el cliente.");
        assertTrue("1x Crepe de Fresa, 2x Cafe".equals(controller.resumenProductos(detalles)), "Guava debe unir el resumen de productos.");
    }

    private void debeSumarMovimientosDeCaja() {
        List<Caja> movimientos = new ArrayList<Caja>();
        movimientos.add(new Caja(1, "P-001", 34.00, "Efectivo", new Date(), "VENTA"));
        movimientos.add(new Caja(2, "P-002", 20.00, "Tarjeta", new Date(), "VENTA"));

        double total = new HistorialCajaController(null).sumarMovimientos(movimientos);

        assertDouble(54.00, total, "La caja debe sumar los movimientos.");
    }

    private void debeExportarCajaConApachePOI() {
        List<Caja> movimientos = new ArrayList<Caja>();
        movimientos.add(new Caja(1, "P-001", 34.00, "Efectivo", new Date(), "VENTA"));
        File carpeta = new File("build/test/reportes");
        File archivo = new File(carpeta, "caja_tdd.xlsx");

        try {
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }
            new ReporteCajaExcelService().exportar(movimientos, "2026-06-03", archivo);
            assertTrue(archivo.exists() && archivo.length() > 0, "Apache POI debe crear el reporte Excel de caja.");
        } catch (Exception ex) {
            throw new AssertionError("No se pudo exportar Excel con Apache POI: " + ex.getMessage());
        }
    }

    private void debeGenerarBoletaDePedido() {
        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        detalles.add(new DetallePedido(new Producto(1, "Crepe de Fresa", "Crepe", 12.00, "", true), 2));
        Pedido pedido = new Pedido(1, "P-TDD", "Cliente TDD", 24.00, "Efectivo", "COMPLETADO", new Date(), 1);
        File carpeta = new File("build/test/boletas");
        File archivo = new File(carpeta, "boleta_P-TDD.txt");

        try {
            new BoletaService().generar(pedido, detalles, carpeta);
            assertTrue(archivo.exists() && archivo.length() > 0, "La boleta debe generarse como archivo de texto.");
        } catch (Exception ex) {
            throw new AssertionError("No se pudo generar la boleta: " + ex.getMessage());
        }
    }

    private void assertDouble(double esperado, double actual, String mensaje) {
        pruebasEjecutadas++;
        if (Math.abs(esperado - actual) > 0.01) {
            throw new AssertionError(mensaje + " Esperado: " + esperado + " Actual: " + actual);
        }
    }

    private void assertTrue(boolean condicion, String mensaje) {
        pruebasEjecutadas++;
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
