package controlador;

import com.google.common.base.Joiner;
import dao.IMesaDAO;
import dao.IPedidoDAO;
import dao.IProductoDAO;
import dao.MesaDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
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
import servicio.CalculadoraPedido;

public class PedidosController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PedidosController.class);
    private final IPedidoDAO pedidoDAO;
    private final IMesaDAO mesaDAO;
    private final IProductoDAO productoDAO;
    private final CalculadoraPedido calculadora;

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
