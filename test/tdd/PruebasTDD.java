package tdd;

import controlador.HistorialCajaController;
import controlador.MesasController;
import controlador.PedidosController;
import controlador.ProductosController;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.security.Provider;
import java.security.Security;
import dao.IMesaDAO;
import dao.IPedidoDAO;
import modelo.Caja;
import modelo.Comprobante;
import modelo.DetallePedido;
import modelo.Pedido;
import modelo.Mesa;
import modelo.Producto;
import modelo.ResultadoComprobante;
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
        pruebas.debeCubrirModelosBasicos();
        pruebas.debeCubrirVistasDeComprobante();
        pruebas.debeCubrirErroresDeComprobanteService();
        pruebas.debeCubrirSeguridadPasswordCompleta();
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
        Pedido pedido = new Pedido(1, "P-TDD", "Cliente TDD", 24.00, "Efectivo", "COMPLETADO", LocalDateTime.now());
        pedido.setMesaNumero(1);
        File carpeta = new File("build/test/comprobantes");
        Comprobante comprobante = new Comprobante(0, 1, Comprobante.BOLETA_DNI, "B001-TDD", "B001-TDD.pdf", LocalDateTime.now());
        comprobante.setClienteNombre("Cliente TDD");
        comprobante.setDni("12345678");
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

    private void debeCubrirModelosBasicos() {
        LocalDateTime fecha = LocalDateTime.of(2026, Month.JULY, 2, 13, 45);
        Producto producto = new Producto(7, "Crepe de Fresa", "Crepe dulce", 12.50, "fresa.png", true);
        assertTrue(producto.getIdProducto() == 7, "El producto debe exponer su id.");
        assertTrue("Crepe de Fresa".equals(producto.getNombre()), "El producto debe exponer su nombre.");
        assertTrue("Crepe dulce".equals(producto.getCategoria()), "El producto debe exponer su categoria.");
        assertDouble(12.50, producto.getPrecio(), "El producto debe exponer su precio.");
        assertTrue("fresa.png".equals(producto.getImagen()), "El producto debe exponer su imagen.");
        assertTrue(producto.isActivo(), "El producto debe exponer su estado activo.");
        assertTrue("Crepe de Fresa - S/ 12.50".equals(producto.toString()), "El producto debe formatearse correctamente.");

        Mesa mesa = new Mesa(3, 12, "OCUPADO");
        assertTrue(mesa.getIdMesa() == 3, "La mesa debe exponer su id.");
        assertTrue(mesa.getNumero() == 12, "La mesa debe exponer su numero.");
        assertTrue("OCUPADO".equals(mesa.getEstado()), "La mesa debe exponer su estado.");

        Caja caja = new Caja(8, "P-123", 25.00, "Yape", fecha, "VENTA");
        assertTrue(caja.getIdCaja() == 8, "La caja debe exponer su id.");
        assertTrue("P-123".equals(caja.getCodigoPedido()), "La caja debe exponer su codigo de pedido.");
        assertDouble(25.00, caja.getMonto(), "La caja debe exponer su monto.");
        assertTrue("Yape".equals(caja.getMetodoPago()), "La caja debe exponer su metodo de pago.");
        assertTrue(fecha.equals(caja.getFecha()), "La caja debe exponer su fecha.");
        assertTrue("VENTA".equals(caja.getTipoMovimiento()), "La caja debe exponer su tipo de movimiento.");

        Usuario usuario = new Usuario(9, "Milo Perez", "milo", "Gerente");
        assertTrue(usuario.getIdUsuario() == 9, "El usuario debe exponer su id.");
        assertTrue("Milo Perez".equals(usuario.getNombre()), "El usuario debe exponer su nombre.");
        assertTrue("milo".equals(usuario.getUsuario()), "El usuario debe exponer su login.");
        assertTrue("Gerente".equals(usuario.getRol()), "El usuario debe exponer su rol.");

        Pedido pedido = new Pedido(10, "P-001", "Cliente", 34.50, "Efectivo", "PENDIENTE", fecha);
        assertTrue(pedido.getIdPedido() == 10, "El pedido debe exponer su id.");
        assertTrue("P-001".equals(pedido.getCodigo()), "El pedido debe exponer su codigo.");
        assertTrue("Cliente".equals(pedido.getCliente()), "El pedido debe exponer su cliente.");
        assertDouble(34.50, pedido.getTotal(), "El pedido debe exponer su total.");
        assertTrue("Efectivo".equals(pedido.getMetodoPago()), "El pedido debe exponer su metodo de pago.");
        assertTrue("PENDIENTE".equals(pedido.getEstado()), "El pedido debe exponer su estado.");
        assertTrue(fecha.equals(pedido.getFecha()), "El pedido debe exponer su fecha.");
        assertTrue(pedido.getMesaNumero() == 0, "La mesa del pedido debe iniciar en cero.");
        pedido.setMesaNumero(7);
        assertTrue(pedido.getMesaNumero() == 7, "La mesa del pedido debe poder cambiarse.");

        Comprobante factura = new Comprobante(1, 10, Comprobante.FACTURA, "F001-0001", "F001-0001.pdf", fecha);
        factura.setClienteNombre("Empresa SA");
        factura.setDni("12345678");
        factura.setRuc("20123456789");
        factura.setRazonSocial("Empresa SA");
        factura.setDireccion("Av. Peru 123");
        factura.setArchivoPdf("factura-final.pdf");
        factura.setFecha(fecha.plusHours(1));
        assertTrue(factura.getIdComprobante() == 1, "El comprobante debe exponer su id.");
        assertTrue(factura.getIdPedido() == 10, "El comprobante debe exponer su id de pedido.");
        assertTrue(Comprobante.FACTURA.equals(factura.getTipo()), "El comprobante debe exponer su tipo.");
        assertTrue("F001-0001".equals(factura.getNumero()), "El comprobante debe exponer su numero.");
        assertTrue("Empresa SA".equals(factura.getClienteNombre()), "El comprobante debe exponer el cliente.");
        assertTrue("12345678".equals(factura.getDni()), "El comprobante debe exponer el DNI.");
        assertTrue("20123456789".equals(factura.getRuc()), "El comprobante debe exponer el RUC.");
        assertTrue("Empresa SA".equals(factura.getRazonSocial()), "El comprobante debe exponer la razon social.");
        assertTrue("Av. Peru 123".equals(factura.getDireccion()), "El comprobante debe exponer la direccion.");
        assertTrue("factura-final.pdf".equals(factura.getArchivoPdf()), "El comprobante debe exponer el archivo PDF.");
        assertTrue(fecha.plusHours(1).equals(factura.getFecha()), "El comprobante debe exponer la fecha actualizada.");
        assertTrue("Factura".equals(factura.getNombreVisible()), "La factura debe mostrarse con nombre visible correcto.");

        Comprobante boletaDni = new Comprobante(2, 11, Comprobante.BOLETA_DNI, "B001-0002", "B001-0002.pdf", fecha);
        assertTrue("Boleta".equals(boletaDni.getNombreVisible()), "La boleta con DNI debe mostrarse como Boleta.");

        Comprobante boletaSimple = new Comprobante(3, 12, Comprobante.BOLETA_SIMPLE, "B001-0003", "B001-0003.pdf", fecha);
        assertTrue("Boleta simple".equals(boletaSimple.getNombreVisible()), "La boleta simple debe mostrarse correctamente.");

        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        detalles.add(new DetallePedido(producto, 2));
        ResultadoComprobante resultado = new ResultadoComprobante(pedido, detalles, factura, new File("archivo.pdf"));
        assertTrue(resultado.getPedido() == pedido, "El resultado debe exponer el pedido.");
        assertTrue(resultado.getDetalles() == detalles, "El resultado debe exponer los detalles.");
        assertTrue(resultado.getComprobante() == factura, "El resultado debe exponer el comprobante.");
        assertTrue("archivo.pdf".equals(resultado.getArchivoPdf().getName()), "El resultado debe exponer el PDF.");
    }

    private void debeCubrirVistasDeComprobante() {
        ComprobanteService service = new ComprobanteService();
        LocalDateTime fecha = LocalDateTime.of(2026, Month.JULY, 2, 14, 10);
        Producto productoLargo = new Producto(20, "Crepe de Nutella con Fresas, Platanos y Salsa Especial", "Crepe dulce", 18.00, "crepe.png", true);
        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        detalles.add(new DetallePedido(productoLargo, 2));

        Pedido pedidoDelivery = new Pedido(1, "P-DEL", "Cliente Delivery", 36.00, "Yape", "PENDIENTE", fecha);
        pedidoDelivery.setMesaNumero(0);
        Comprobante boletaSimple = new Comprobante(1, 1, Comprobante.BOLETA_SIMPLE, "B001-0004", "B001-0004.pdf", fecha);
        String vistaSimple = service.construirVista(pedidoDelivery, detalles, boletaSimple);
        assertTrue(vistaSimple.contains("BOLETA SIMPLE"), "La vista debe mostrar la boleta simple.");
        assertTrue(vistaSimple.contains("Atencion: Delivery"), "La vista debe mostrar delivery.");
        assertTrue(vistaSimple.contains("Cliente: Consumidor final"), "La vista debe mostrar consumidor final.");

        Pedido pedidoMesa = new Pedido(2, "P-MESA", "Cliente Mesa", 36.00, "Efectivo", "PENDIENTE", fecha);
        pedidoMesa.setMesaNumero(5);
        Comprobante boletaDni = new Comprobante(2, 2, Comprobante.BOLETA_DNI, "B001-0005", "B001-0005.pdf", fecha);
        boletaDni.setClienteNombre("Carlos Alberto");
        boletaDni.setDni("12345678");
        String vistaDni = service.construirVista(pedidoMesa, detalles, boletaDni);
        assertTrue(vistaDni.contains("Atencion: Mesa 5"), "La vista debe mostrar la mesa.");
        assertTrue(vistaDni.contains("Cliente: Carlos Alberto"), "La vista debe mostrar el cliente de boleta con DNI.");
        assertTrue(vistaDni.contains("DNI: 12345678"), "La vista debe mostrar el DNI.");

        Pedido pedidoFactura = new Pedido(3, "P-FAC", "Cliente Factura", 36.00, "Tarjeta", "PENDIENTE", fecha);
        pedidoFactura.setMesaNumero(3);
        Comprobante factura = new Comprobante(3, 3, Comprobante.FACTURA, "F001-0006", "F001-0006.pdf", fecha);
        factura.setClienteNombre("Empresa SA");
        factura.setRuc("20123456789");
        factura.setRazonSocial("Empresa SA");
        factura.setDireccion("Av. Peru 123");
        String vistaFactura = service.construirVista(pedidoFactura, detalles, factura);
        assertTrue(vistaFactura.contains("Cliente: Empresa SA"), "La vista debe mostrar el cliente de factura.");
        assertTrue(vistaFactura.contains("RUC: 20123456789"), "La vista debe mostrar el RUC.");
        assertTrue(vistaFactura.contains("Razon social: Empresa SA"), "La vista debe mostrar la razon social.");
        assertTrue(vistaFactura.contains("Direccion: Av. Peru 123"), "La vista debe mostrar la direccion.");

        try {
            String limpio = invocarPrivado(service, "sanitizar", new Class<?>[] { String.class }, new Object[] { "Atención Ñandú" });
            assertTrue("Atencion Nandu".equals(limpio), "La sanitizacion debe quitar tildes y enies.");
            String vacio = invocarPrivado(service, "sanitizar", new Class<?>[] { String.class }, new Object[] { null });
            assertTrue("".equals(vacio), "La sanitizacion debe manejar nulos.");

            String recortado = invocarPrivado(service, "recortar", new Class<?>[] { String.class, int.class }, new Object[] { "abcdefghijklmnop", 10 });
            assertTrue(recortado.endsWith("..."), "El recorte debe agregar puntos suspensivos.");
            String recorteVacio = invocarPrivado(service, "recortar", new Class<?>[] { String.class, int.class }, new Object[] { null, 10 });
            assertTrue("".equals(recorteVacio), "El recorte debe manejar nulos.");
        } catch (Exception ex) {
            throw new AssertionError("No se pudieron invocar los metodos privados: " + ex.getMessage());
        }
    }

    private void debeCubrirErroresDeComprobanteService() {
        ComprobanteService service = new ComprobanteService();
        LocalDateTime fecha = LocalDateTime.of(2026, Month.JULY, 2, 15, 0);
        Pedido pedido = new Pedido(4, "P-ERR", "Cliente", 10.00, "Efectivo", "PENDIENTE", fecha);
        pedido.setMesaNumero(1);
        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        detalles.add(new DetallePedido(new Producto(1, "Producto", "Bebida", 10.00, "img.png", true), 1));

        Path archivoConflictoCarpeta = Path.of("build", "test", "conflicto-carpeta");
        Path archivoConflictoComprobante = Path.of("build", "test", "conflicto-comprobante");
        try {
            Files.createDirectories(archivoConflictoCarpeta.getParent());
            Files.deleteIfExists(archivoConflictoCarpeta);
            Files.writeString(archivoConflictoCarpeta, "bloqueo");
            Comprobante comprobanteCarpeta = new Comprobante(5, 4, Comprobante.BOLETA_SIMPLE, "B001-ERR1", "B001-ERR1.pdf", fecha);
            try {
                service.generarPdf(pedido, detalles, comprobanteCarpeta, archivoConflictoCarpeta.resolve("subdir").toFile());
                throw new AssertionError("Se esperaba error al crear la carpeta destino.");
            } catch (IOException ex) {
                assertTrue(ex.getMessage().contains("carpeta de comprobantes"), "Debe fallar al crear la carpeta destino.");
            }

            Files.deleteIfExists(archivoConflictoComprobante);
            Files.writeString(archivoConflictoComprobante, "bloqueo");
            Comprobante comprobanteArchivo = new Comprobante(6, 4, Comprobante.BOLETA_SIMPLE, "B001-ERR2", archivoConflictoComprobante.resolve("subdir").resolve("B001-ERR2.pdf").toString(), fecha);
            try {
                service.generarPdf(pedido, detalles, comprobanteArchivo, new File("build/test/comprobantes-ok"));
                throw new AssertionError("Se esperaba error al crear la carpeta del comprobante.");
            } catch (IOException ex) {
                assertTrue(ex.getMessage().contains("carpeta del comprobante"), "Debe fallar al crear la carpeta del comprobante.");
            }
        } catch (IOException ex) {
            throw new AssertionError("No se pudo preparar el entorno de prueba: " + ex.getMessage());
        } finally {
            try {
                Files.deleteIfExists(archivoConflictoCarpeta);
                Files.deleteIfExists(archivoConflictoComprobante);
            } catch (IOException ex) {
                throw new AssertionError("No se pudo limpiar el entorno de prueba: " + ex.getMessage());
            }
        }
    }

    private void debeCubrirSeguridadPasswordCompleta() {
        String hashAdmin = SeguridadPassword.encriptar("ADMIN", "1234");
        String hashAdminMinusculas = SeguridadPassword.encriptar("admin", "1234");
        assertTrue(hashAdmin.equals(hashAdminMinusculas), "El usuario debe normalizarse antes de encriptar.");
        assertTrue(SeguridadPassword.coincide("admin", "1234", hashAdmin), "La contrasena debe coincidir con el hash.");
        assertTrue(SeguridadPassword.coincide("admin", "1234", "1234"), "Debe aceptar contrasenas heredadas en texto plano.");
        assertTrue(!SeguridadPassword.coincide("admin", "1234", null), "Un hash nulo no debe coincidir.");
        assertTrue(!SeguridadPassword.coincide("admin", "1234", "otro"), "Un hash distinto no debe coincidir.");
        assertTrue("Gerente".equals(SeguridadPassword.normalizarRol("admin", "Empleado")), "Admin debe mapear a Gerente.");
        assertTrue("Empleado".equals(SeguridadPassword.normalizarRol("empleado", "Gerente")), "Empleado debe mapear a Empleado.");
        assertTrue("Supervisor".equals(SeguridadPassword.normalizarRol("otro", "Supervisor")), "Los roles distintos deben conservarse.");
        String hashUsuarioNulo = SeguridadPassword.encriptar(null, "1234");
        assertTrue(hashUsuarioNulo != null && !hashUsuarioNulo.isEmpty(), "Debe aceptar usuario nulo al encriptar.");

        Provider[] proveedores = Security.getProviders();
        try {
            for (Provider proveedor : proveedores) {
                Security.removeProvider(proveedor.getName());
            }
            try {
                SeguridadPassword.encriptar("admin", "1234");
                throw new AssertionError("Se esperaba error al no disponer de SHA-256.");
            } catch (IllegalStateException ex) {
                assertTrue(ex.getMessage().contains("SHA-256"), "El error debe mencionar SHA-256.");
            }
        } finally {
            for (Provider proveedor : proveedores) {
                Security.addProvider(proveedor);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T invocarPrivado(Object objetivo, String nombre, Class<?>[] tipos, Object[] argumentos) throws Exception {
        Method metodo = objetivo.getClass().getDeclaredMethod(nombre, tipos);
        metodo.setAccessible(true);
        return (T) metodo.invoke(objetivo, argumentos);
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


