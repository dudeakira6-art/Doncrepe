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
import modelo.DetallePedido;
import modelo.Mesa;
import modelo.Pedido;
import modelo.Producto;
import modelo.Usuario;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicio.BoletaService;
import servicio.CalculadoraPedido;

public class PedidosController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PedidosController.class);
    private final IPedidoDAO pedidoDAO;
    private final IMesaDAO mesaDAO;
    private final IProductoDAO productoDAO;
    private final CalculadoraPedido calculadora;
    private final BoletaService boletaService = new BoletaService();

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
        if (usuario == null || mesa == null) {
            if (!delivery) {
                throw new IllegalArgumentException("Debe seleccionar empleado y mesa.");
            }
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Debe seleccionar empleado y mesa.");
        }
        String clienteNormalizado = normalizarCliente(cliente);
        if (StringUtils.isBlank(clienteNormalizado)) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Agregue al menos un producto.");
        }
        if (!delivery && !"LIBRE".equalsIgnoreCase(mesa.getEstado())) {
            throw new IllegalArgumentException("La mesa seleccionada esta ocupada.");
        }
        LOGGER.info("Creando pedido para {} en {} con productos: {}", clienteNormalizado, delivery ? "Delivery" : "mesa " + mesa.getNumero(), resumenProductos(detalles));
        pedidoDAO.crearPedido(usuario.getIdUsuario(), delivery ? 0 : mesa.getIdMesa(), clienteNormalizado, metodoPago, detalles);
    }

    public File procesarPagoYGenerarBoleta(String codigo, String metodoPago, File carpetaBoletas) throws SQLException, IOException {
        if (StringUtils.isBlank(codigo)) {
            throw new IllegalArgumentException("Seleccione un pedido para pagar.");
        }
        if (StringUtils.isBlank(metodoPago)) {
            throw new IllegalArgumentException("Seleccione un método de pago.");
        }
        Pedido pedidoActual = pedidoDAO.buscarPorCodigo(codigo);
        if (pedidoActual == null) {
            throw new IllegalArgumentException("El pedido seleccionado ya no existe.");
        }
        if ("COMPLETADO".equalsIgnoreCase(pedidoActual.getEstado())) {
            throw new IllegalArgumentException("El pedido ya fue pagado.");
        }
        pedidoDAO.registrarPago(codigo, metodoPago);
        Pedido pedidoPagado = pedidoDAO.buscarPorCodigo(codigo);
        List<DetallePedido> detalles = pedidoDAO.listarDetalles(codigo);
        return boletaService.generar(pedidoPagado, detalles, carpetaBoletas);
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
}
