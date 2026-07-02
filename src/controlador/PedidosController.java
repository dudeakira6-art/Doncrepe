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
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
    private static final String ESTADO_COMPLETADO = "COMPLETADO";
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
        List<Mesa> libres = new ArrayList<Mesa>();
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

    public void crearPedido(PedidoSolicitud solicitud) throws SQLException {
        if (solicitud == null) {
            throw new IllegalArgumentException("Debe completar la solicitud del pedido.");
        }
        Usuario usuario = solicitud.getUsuario();
        Mesa mesa = solicitud.getMesa();
        String cliente = solicitud.getCliente();
        String metodoPago = solicitud.getMetodoPago();
        List<DetallePedido> detalles = solicitud.getDetalles();
        boolean delivery = solicitud.isDelivery();

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
        LOGGER.info("Creando pedido para {} en {} con productos: {}", clienteNormalizado,
                delivery ? "Delivery" : "mesa " + mesa.getNumero(), resumenProductos(detalles));
        pedidoDAO.crearPedido(usuario.getIdUsuario(), delivery ? 0 : mesa.getIdMesa(), clienteNormalizado, metodoPago, detalles);
    }

    public ResultadoComprobante procesarPagoYGenerarComprobante(PagoComprobanteSolicitud solicitud)
            throws SQLException, IOException {
        if (solicitud == null) {
            throw new IllegalArgumentException("Debe completar la solicitud del pago.");
        }
        String codigo = solicitud.getCodigo();
        String metodoPago = solicitud.getMetodoPago();
        String tipoComprobante = solicitud.getTipoComprobante();
        String clienteNombre = solicitud.getClienteNombre();
        String dni = solicitud.getDni();
        String ruc = solicitud.getRuc();
        String razonSocial = solicitud.getRazonSocial();
        String direccion = solicitud.getDireccion();
        File carpetaComprobantes = solicitud.getCarpetaComprobantes();

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
        if (ESTADO_COMPLETADO.equalsIgnoreCase(pedidoActual.getEstado())) {
            throw new IllegalArgumentException("El pedido ya fue pagado.");
        }
        Comprobante comprobante = construirComprobante(pedidoActual, solicitud);
        List<DetallePedido> detalles = pedidoDAO.listarDetalles(codigo);
        Pedido pedidoParaComprobante = new Pedido(
                pedidoActual.getIdPedido(),
                pedidoActual.getCodigo(),
                pedidoActual.getCliente(),
                pedidoActual.getTotal(),
                metodoPago,
                ESTADO_COMPLETADO,
                pedidoActual.getFecha(),
                pedidoActual.getMesaNumero());
        File pdf = comprobanteService.generarPdf(pedidoParaComprobante, detalles, comprobante, carpetaComprobantes);
        try {
            pedidoDAO.registrarPago(codigo, metodoPago, comprobante);
        } catch (SQLException ex) {
            if (pdf.exists()) {
                try {
                    Files.deleteIfExists(pdf.toPath());
                } catch (IOException ioEx) {
                    LOGGER.warn("No se pudo eliminar el PDF huérfano {}", pdf.getAbsolutePath(), ioEx);
                }
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
        List<String> nombres = new ArrayList<String>();
        for (DetallePedido detalle : detalles) {
            nombres.add(detalle.getCantidad() + "x " + detalle.getProducto().getNombre());
        }
        return Joiner.on(", ").join(nombres);
    }

    private Comprobante construirComprobante(Pedido pedido, PagoComprobanteSolicitud solicitud) {
        String numero = (Comprobante.FACTURA.equals(solicitud.getTipoComprobante()) ? "F001-" : "B001-") + System.currentTimeMillis();
        File carpeta = solicitud.getCarpetaComprobantes();
        String archivo = new File(carpeta, numero + ".pdf").getPath();
        String nombre = StringUtils.defaultIfBlank(solicitud.getClienteNombre(), pedido.getCliente());
        return new Comprobante(0, pedido.getIdPedido(), solicitud.getTipoComprobante(), numero, nombre.trim(),
                StringUtils.trimToEmpty(solicitud.getDni()), StringUtils.trimToEmpty(solicitud.getRuc()),
                StringUtils.trimToEmpty(solicitud.getRazonSocial()), StringUtils.trimToEmpty(solicitud.getDireccion()),
                archivo, Date.from(LocalDateTime.now(ZoneId.systemDefault()).atZone(ZoneId.systemDefault()).toInstant()));
    }

    public static final class PedidoSolicitud {
        private Usuario usuario;
        private Mesa mesa;
        private String cliente;
        private String metodoPago;
        private List<DetallePedido> detalles;
        private boolean delivery;

        public Usuario getUsuario() {
            return usuario;
        }

        public void setUsuario(Usuario usuario) {
            this.usuario = usuario;
        }

        public Mesa getMesa() {
            return mesa;
        }

        public void setMesa(Mesa mesa) {
            this.mesa = mesa;
        }

        public String getCliente() {
            return cliente;
        }

        public void setCliente(String cliente) {
            this.cliente = cliente;
        }

        public String getMetodoPago() {
            return metodoPago;
        }

        public void setMetodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
        }

        public List<DetallePedido> getDetalles() {
            return detalles;
        }

        public void setDetalles(List<DetallePedido> detalles) {
            this.detalles = detalles;
        }

        public boolean isDelivery() {
            return delivery;
        }

        public void setDelivery(boolean delivery) {
            this.delivery = delivery;
        }
    }

    public static final class PagoComprobanteSolicitud {
        private String codigo;
        private String metodoPago;
        private String tipoComprobante;
        private String clienteNombre;
        private String dni;
        private String ruc;
        private String razonSocial;
        private String direccion;
        private File carpetaComprobantes;

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getMetodoPago() {
            return metodoPago;
        }

        public void setMetodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
        }

        public String getTipoComprobante() {
            return tipoComprobante;
        }

        public void setTipoComprobante(String tipoComprobante) {
            this.tipoComprobante = tipoComprobante;
        }

        public String getClienteNombre() {
            return clienteNombre;
        }

        public void setClienteNombre(String clienteNombre) {
            this.clienteNombre = clienteNombre;
        }

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getRuc() {
            return ruc;
        }

        public void setRuc(String ruc) {
            this.ruc = ruc;
        }

        public String getRazonSocial() {
            return razonSocial;
        }

        public void setRazonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public File getCarpetaComprobantes() {
            return carpetaComprobantes;
        }

        public void setCarpetaComprobantes(File carpetaComprobantes) {
            this.carpetaComprobantes = carpetaComprobantes;
        }
    }
}
