package controlador;

import com.google.common.base.Joiner;
import dao.IMesaDAO;
import dao.IPedidoDAO;
import dao.IProductoDAO;
import dao.MesaDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import modelo.Comprobante;
import modelo.DetallePedido;
import modelo.Mesa;
import modelo.Pedido;
import modelo.Producto;
import modelo.ResultadoComprobante;
import modelo.Usuario;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicio.CalculadoraPedido;
import servicio.ComprobanteService;

public class PedidosController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PedidosController.class);
    private final IPedidoDAO pedidoDAO;
    private final IMesaDAO mesaDAO;
    private final IProductoDAO productoDAO;
    private final CalculadoraPedido calculadora;
    private final ComprobanteService comprobanteService = new ComprobanteService();

    public PedidosController() {
        this(new PedidoDAO(), new MesaDAO(), new ProductoDAO(), new CalculadoraPedido());
    }

    public PedidosController(IPedidoDAO pedidoDAO, IMesaDAO mesaDAO, IProductoDAO productoDAO, CalculadoraPedido calculadora) {
        this.pedidoDAO = pedidoDAO;
        this.mesaDAO = mesaDAO;
        this.productoDAO = productoDAO;
        this.calculadora = calculadora;
    }

    public List<Pedido> listarPedidos() throws SQLException {
        return pedidoDAO.listarRecientes();
    }

    public Pedido buscarPedido(String codigo) throws SQLException {
        if (StringUtils.isBlank(codigo)) {
            throw new IllegalArgumentException("Seleccione un pedido.");
        }
        return pedidoDAO.buscarPorCodigo(codigo);
    }

    public List<DetallePedido> listarDetalles(String codigo) throws SQLException {
        if (StringUtils.isBlank(codigo)) {
            throw new IllegalArgumentException("Seleccione un pedido.");
        }
        return pedidoDAO.listarDetalles(codigo);
    }

    public Comprobante buscarComprobante(String codigo) throws SQLException {
        if (StringUtils.isBlank(codigo)) {
            throw new IllegalArgumentException("Seleccione un pedido.");
        }
        return pedidoDAO.buscarComprobantePorPedido(codigo);
    }

    public ResultadoComprobante obtenerComprobante(String codigo) throws SQLException {
        Pedido pedido = buscarPedido(codigo);
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido seleccionado ya no existe.");
        }
        Comprobante comprobante = buscarComprobante(codigo);
        if (comprobante == null) {
            throw new IllegalArgumentException("El pedido no tiene comprobante registrado.");
        }
        return new ResultadoComprobante(pedido, listarDetalles(codigo), comprobante, new File(comprobante.getArchivoPdf()));
    }

    public List<Mesa> listarMesas() throws SQLException {
        return mesaDAO.listar();
    }

    public List<Mesa> listarMesasLibres() throws SQLException {
        java.util.List<Mesa> libres = new java.util.ArrayList<Mesa>();
        for (Mesa mesa : mesaDAO.listar()) {
            if ("LIBRE".equalsIgnoreCase(mesa.getEstado())) {
                libres.add(mesa);
            }
        }
        return libres;
    }

    public List<Producto> listarProductos() throws SQLException {
        return productoDAO.listarActivos();
    }

    public double calcularTotal(List<DetallePedido> detalles) {
        return calculadora.calcularTotal(detalles);
    }

    public void crearPedido(Usuario usuario, Mesa mesa, String cliente, String metodoPago, List<DetallePedido> detalles) throws SQLException {
        crearPedido(usuario, mesa, cliente, metodoPago, detalles, false);
    }

    public void crearPedido(Usuario usuario, Mesa mesa, String cliente, String metodoPago, List<DetallePedido> detalles, boolean delivery) throws SQLException {
        if (usuario == null) {
            throw new IllegalArgumentException("Debe seleccionar empleado.");
        }
        if (!delivery && mesa == null) {
            throw new IllegalArgumentException("Debe seleccionar una mesa.");
        }
        String clienteNormalizado = normalizarCliente(cliente);
        if (StringUtils.isBlank(clienteNormalizado)) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto.");
        }
        if (!delivery && !"LIBRE".equalsIgnoreCase(mesa.getEstado())) {
            throw new IllegalArgumentException("La mesa seleccionada está ocupada.");
        }
        LOGGER.info("Creando pedido para {} en {} con productos: {}", clienteNormalizado, delivery ? "Delivery" : "mesa " + mesa.getNumero(), resumenProductos(detalles));
        pedidoDAO.crearPedido(usuario.getIdUsuario(), delivery ? 0 : mesa.getIdMesa(), clienteNormalizado, metodoPago, detalles);
    }

    public ResultadoComprobante procesarPagoYGenerarComprobante(String codigo, String metodoPago, String tipoComprobante,
            String clienteNombre, String dni, String ruc, String razonSocial, String direccion, File carpetaComprobantes)
            throws SQLException, IOException {
        if (StringUtils.isBlank(codigo)) {
            throw new IllegalArgumentException("Seleccione un pedido para pagar.");
        }
        if (!"Efectivo".equals(metodoPago) && !"Tarjeta".equals(metodoPago) && !"Yape".equals(metodoPago)) {
            throw new IllegalArgumentException("Seleccione un método de pago válido.");
        }
        validarDatosComprobante(tipoComprobante, clienteNombre, dni, ruc, razonSocial, direccion);
        Pedido pedidoActual = pedidoDAO.buscarPorCodigo(codigo);
        if (pedidoActual == null) {
            throw new IllegalArgumentException("El pedido seleccionado ya no existe.");
        }
        if ("COMPLETADO".equalsIgnoreCase(pedidoActual.getEstado())) {
            throw new IllegalArgumentException("El pedido ya fue pagado.");
        }
        Comprobante comprobante = construirComprobante(pedidoActual, tipoComprobante, clienteNombre, dni, ruc, razonSocial, direccion, carpetaComprobantes);
        List<DetallePedido> detalles = pedidoDAO.listarDetalles(codigo);
        Pedido pedidoParaComprobante = new Pedido(
                pedidoActual.getIdPedido(),
                pedidoActual.getCodigo(),
                pedidoActual.getCliente(),
                pedidoActual.getTotal(),
                metodoPago,
                "COMPLETADO",
                pedidoActual.getFecha(),
                pedidoActual.getMesaNumero());
        File pdf = comprobanteService.generarPdf(pedidoParaComprobante, detalles, comprobante, carpetaComprobantes);
        try {
            pedidoDAO.registrarPago(codigo, metodoPago, comprobante);
        } catch (SQLException ex) {
            if (pdf.exists() && !pdf.delete()) {
                LOGGER.warn("No se pudo eliminar el PDF huérfano {}", pdf.getAbsolutePath());
            }
            throw ex;
        }
        Pedido pedidoPagado = pedidoDAO.buscarPorCodigo(codigo);
        return new ResultadoComprobante(pedidoPagado, detalles, comprobante, pdf);
    }

    public void validarDatosComprobante(String tipo, String clienteNombre, String dni, String ruc, String razonSocial, String direccion) {
        if (StringUtils.isBlank(tipo)) {
            throw new IllegalArgumentException("Seleccione el tipo de comprobante.");
        }
        if (Comprobante.BOLETA_DNI.equals(tipo)) {
            if (!StringUtils.defaultString(dni).matches("\\d{8}")) {
                throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
            }
            if (StringUtils.isBlank(clienteNombre)) {
                throw new IllegalArgumentException("Ingrese el nombre para la boleta.");
            }
        } else if (Comprobante.FACTURA.equals(tipo)) {
            if (!StringUtils.defaultString(ruc).matches("\\d{11}")) {
                throw new IllegalArgumentException("El RUC debe tener 11 dígitos.");
            }
            if (StringUtils.isBlank(razonSocial) || StringUtils.isBlank(direccion)) {
                throw new IllegalArgumentException("Ingrese razón social y dirección para la factura.");
            }
        } else if (!Comprobante.BOLETA_SIMPLE.equals(tipo)) {
            throw new IllegalArgumentException("Tipo de comprobante no válido.");
        }
    }

    public void eliminarPedido(String codigo) throws SQLException {
        if (StringUtils.isBlank(codigo)) {
            throw new IllegalArgumentException("Seleccione un pedido para eliminar.");
        }
        pedidoDAO.eliminarPorCodigo(codigo);
    }

    public String normalizarCliente(String cliente) {
        String limpio = StringUtils.trimToEmpty(cliente);
        String[] palabras = StringUtils.split(limpio.toLowerCase());
        if (palabras == null || palabras.length == 0) {
            return "";
        }
        for (int i = 0; i < palabras.length; i++) {
            palabras[i] = StringUtils.capitalize(palabras[i]);
        }
        return StringUtils.join(palabras, " ");
    }

    public String resumenProductos(List<DetallePedido> detalles) {
        java.util.List<String> nombres = new java.util.ArrayList<String>();
        for (DetallePedido detalle : detalles) {
            nombres.add(detalle.getCantidad() + "x " + detalle.getProducto().getNombre());
        }
        return Joiner.on(", ").join(nombres);
    }

    private Comprobante construirComprobante(Pedido pedido, String tipo, String clienteNombre, String dni,
            String ruc, String razonSocial, String direccion, File carpetaComprobantes) {
        String numero = (Comprobante.FACTURA.equals(tipo) ? "F001-" : "B001-") + System.currentTimeMillis();
        String archivo = new File(carpetaComprobantes, numero + ".pdf").getPath();
        String nombre = StringUtils.defaultIfBlank(clienteNombre, pedido.getCliente());
        // Si es boleta simple, dejamos el nombre vacío (no pedir datos de cliente)
        if (Comprobante.BOLETA_SIMPLE.equals(tipo)) {
            nombre = "";
        }
        return new Comprobante(0, pedido.getIdPedido(), tipo, numero, nombre.trim(), StringUtils.trimToEmpty(dni),
                StringUtils.trimToEmpty(ruc), StringUtils.trimToEmpty(razonSocial), StringUtils.trimToEmpty(direccion),
                archivo, new java.util.Date());
    }
}
