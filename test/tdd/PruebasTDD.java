package tdd;

import controlador.HistorialCajaController;
import controlador.MesasController;
import controlador.PedidosController;
import controlador.ProductosController;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.time.LocalDateTime;
import dao.IMesaDAO;
import dao.IPedidoDAO;
import modelo.Caja;
import modelo.Comprobante;
import modelo.DetallePedido;
import modelo.Pedido;
import modelo.Mesa;
import modelo.Producto;
import modelo.Usuario;
import servicio.CalculadoraPedido;
import servicio.ComprobanteService;
import servicio.ReporteCajaExcelService;
import util.SeguridadPassword;

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
        pruebas.debeGenerarComprobantePdf();
        pruebas.debeValidarDatosDeFactura();
        pruebas.debePermitirBoletaSimpleSinDatos();
        pruebas.debeRequerirNombreYDniParaBoletaDni();
        pruebas.debeUsarSeguridadPassword();
        pruebas.debeAlternarEstadoDeMesa();
        pruebas.debeExponerUsuario();
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
            throw new AssertionError("Se esperaba error por precio inválido.");
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
        assertTrue("Crepe Dulce".equals(normalizado.getCategoria()), "Apache Commons debe limpiar y capitalizar la categoría.");
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
        movimientos.add(new Caja(1, "P-001", 34.00, "Efectivo", LocalDateTime.now(), "VENTA"));
        movimientos.add(new Caja(2, "P-002", 20.00, "Tarjeta", LocalDateTime.now(), "VENTA"));

        double total = new HistorialCajaController(null).sumarMovimientos(movimientos);

        assertDouble(54.00, total, "La caja debe sumar los movimientos.");
    }

    private void debeExportarCajaConApachePOI() {
        List<Caja> movimientos = new ArrayList<Caja>();
        movimientos.add(new Caja(1, "P-001", 34.00, "Efectivo", LocalDateTime.now(), "VENTA"));
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

    private void debeGenerarComprobantePdf() {
        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        detalles.add(new DetallePedido(new Producto(1, "Crepe de Fresa", "Crepe", 12.00, "", true), 2));
        Pedido pedido = new Pedido(1, "P-TDD", "Cliente TDD", 24.00, "Efectivo", "COMPLETADO", LocalDateTime.now(), 1);
        File carpeta = new File("build/test/comprobantes");
        Comprobante comprobante = new Comprobante(0, 1, Comprobante.BOLETA_DNI, "B001-TDD", "Cliente TDD", "12345678", "", "", "", "B001-TDD.pdf", LocalDateTime.now());
        File archivo = new File(carpeta, "B001-TDD.pdf");

        try {
            new ComprobanteService().generarPdf(pedido, detalles, comprobante, carpeta);
            assertTrue(archivo.exists() && archivo.length() > 0, "PDFBox debe generar el comprobante PDF.");
        } catch (Exception ex) {
            throw new AssertionError("No se pudo generar el comprobante PDF: " + ex.getMessage());
        }
    }

    private void debeValidarDatosDeFactura() {
        PedidosController controller = new PedidosController(null, null, null, new CalculadoraPedido());
        try {
            controller.validarDatosComprobante(Comprobante.FACTURA, "", "", "123", "", "");
            throw new AssertionError("Se esperaba error por RUC inválido.");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("RUC"), "La factura debe validar RUC.");
        } catch (Exception ex) {
            throw new AssertionError("No se esperaba otro tipo de error: " + ex.getMessage());
        }
    }

    private void debePermitirBoletaSimpleSinDatos() {
        PedidosController controller = new PedidosController(null, null, null, new CalculadoraPedido());
        try {
            controller.validarDatosComprobante(Comprobante.BOLETA_SIMPLE, "", "", "", "", "");
            assertTrue(true, "La boleta simple no debe exigir datos del cliente.");
        } catch (Exception ex) {
            throw new AssertionError("La boleta simple no debía exigir datos: " + ex.getMessage());
        }
    }

    private void debeRequerirNombreYDniParaBoletaDni() {
        PedidosController controller = new PedidosController(null, null, null, new CalculadoraPedido());
        try {
            controller.validarDatosComprobante(Comprobante.BOLETA_DNI, "", "12345678", "", "", "");
            throw new AssertionError("Se esperaba error por nombre faltante en boleta con DNI.");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("nombre"), "La boleta con DNI debe exigir nombre.");
        }
        try {
            controller.validarDatosComprobante(Comprobante.BOLETA_DNI, "Cliente TDD", "123", "", "", "");
            throw new AssertionError("Se esperaba error por DNI inválido en boleta con DNI.");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("DNI"), "La boleta con DNI debe exigir DNI de 8 digitos.");
        }
    }

    private void debeUsarSeguridadPassword() {
        String hash = SeguridadPassword.encriptar("admin", "1234");
        assertTrue(SeguridadPassword.coincide("admin", "1234", hash), "La contraseña debe coincidir con su hash.");
        assertTrue("Gerente".equals(SeguridadPassword.normalizarRol("admin", "Empleado")), "Admin debe mapear a Gerente.");
        assertTrue("Empleado".equals(SeguridadPassword.normalizarRol("empleado", "Gerente")), "Empleado debe mapear a Empleado.");
    }

    private void debeAlternarEstadoDeMesa() {
        IMesaDAO mesaDAO = new IMesaDAO() {
            @Override
            public List<Mesa> listar() throws SQLException {
                return new ArrayList<Mesa>();
            }

            @Override
            public void cambiarEstado(int idMesa, String estado) throws SQLException {
                // Stub de prueba: el cambio de estado se valida por el valor retornado por el controlador.
            }
        };
        IPedidoDAO pedidoDAO = new IPedidoDAO() {
            @Override public List<Pedido> listarRecientes() { return new ArrayList<Pedido>(); }
            @Override public Pedido buscarPorCodigo(String codigo) { return null; }
            @Override public List<DetallePedido> listarDetalles(String codigo) { return new ArrayList<DetallePedido>(); }
            @Override public boolean existePedidoPendienteMesa(int idMesa) { return false; }
            @Override public int pedidosHoy() { return 0; }
            @Override public int pedidosPendientes() { return 0; }
            @Override public double ventasHoy() { return 0; }
            @Override public void crearPedido(int idUsuario, int idMesa, String cliente, String metodoPago, List<DetallePedido> detalles) {
                // Stub de prueba.
            }
            @Override public void registrarPago(String codigo, String metodoPago, Comprobante comprobante) {
                // Stub de prueba.
            }
            @Override public Comprobante buscarComprobantePorPedido(String codigo) { return null; }
            @Override public void eliminarPorCodigo(String codigo) {
                // Stub de prueba.
            }
        };
        MesasController controller = new MesasController(mesaDAO, pedidoDAO);
        try {
            String estado = controller.alternarEstado(new Mesa(1, 1, "LIBRE"));
            assertTrue("OCUPADO".equals(estado), "Una mesa libre debe pasar a ocupada.");
        } catch (Exception ex) {
            throw new AssertionError("No se esperaba error al alternar estado de mesa: " + ex.getMessage());
        }

        MesasController controllerConPendiente = new MesasController(mesaDAO, new IPedidoDAO() {
            @Override public List<Pedido> listarRecientes() { return new ArrayList<Pedido>(); }
            @Override public Pedido buscarPorCodigo(String codigo) { return null; }
            @Override public List<DetallePedido> listarDetalles(String codigo) { return new ArrayList<DetallePedido>(); }
            @Override public boolean existePedidoPendienteMesa(int idMesa) { return true; }
            @Override public int pedidosHoy() { return 0; }
            @Override public int pedidosPendientes() { return 0; }
            @Override public double ventasHoy() { return 0; }
            @Override public void crearPedido(int idUsuario, int idMesa, String cliente, String metodoPago, List<DetallePedido> detalles) {
                // Stub de prueba.
            }
            @Override public void registrarPago(String codigo, String metodoPago, Comprobante comprobante) {
                // Stub de prueba.
            }
            @Override public Comprobante buscarComprobantePorPedido(String codigo) { return null; }
            @Override public void eliminarPorCodigo(String codigo) {
                // Stub de prueba.
            }
        });
        try {
            controllerConPendiente.alternarEstado(new Mesa(1, 1, "OCUPADO"));
            throw new AssertionError("Se esperaba error si la mesa tiene pedido pendiente.");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("pedido pendiente"), "La mesa no debe liberarse con pedido pendiente.");
        } catch (Exception ex) {
            throw new AssertionError("No se esperaba otro tipo de error: " + ex.getMessage());
        }
    }

    private void debeExponerUsuario() {
        Usuario usuario = new Usuario(1, "Milo Perez", "milo", "Empleado");
        assertTrue("milo".equals(usuario.getUsuario()), "El usuario debe exponer su nombre de usuario.");
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


