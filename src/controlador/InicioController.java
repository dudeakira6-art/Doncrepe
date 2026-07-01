package controlador;

import dao.CajaDAO;
import dao.ICajaDAO;
import dao.IPedidoDAO;
import dao.IProductoDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.List;
import modelo.Pedido;

public class InicioController {
    private final IPedidoDAO pedidoDAO;
    private final ICajaDAO cajaDAO;
    private final IProductoDAO productoDAO;

    public InicioController() {
        this(new PedidoDAO(), new CajaDAO(), new ProductoDAO());
    }

    public InicioController(IPedidoDAO pedidoDAO, ICajaDAO cajaDAO, IProductoDAO productoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.cajaDAO = cajaDAO;
        this.productoDAO = productoDAO;
    }

    public double ventasHoy() throws SQLException {
        return pedidoDAO.ventasHoy();
    }

    public double cajaActual() throws SQLException {
        return cajaDAO.totalDelDia();
    }

    public int pedidosHoy() throws SQLException {
        return pedidoDAO.pedidosHoy();
    }

    public int pedidosPendientes() throws SQLException {
        return pedidoDAO.pedidosPendientes();
    }

    public int productosActivos() throws SQLException {
        return productoDAO.contarActivos();
    }

    public List<Pedido> actividadReciente() throws SQLException {
        return pedidoDAO.listarRecientes();
    }
}
