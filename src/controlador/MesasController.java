package controlador;

import dao.IMesaDAO;
import dao.IPedidoDAO;
import dao.MesaDAO;
import dao.PedidoDAO;
import java.sql.SQLException;
import java.util.List;
import modelo.Mesa;

public class MesasController {
    private final IMesaDAO mesaDAO;
    private final IPedidoDAO pedidoDAO;

    public MesasController() {
        this(new MesaDAO(), new PedidoDAO());
    }

    public MesasController(IMesaDAO mesaDAO) {
        this(mesaDAO, new PedidoDAO());
    }

    public MesasController(IMesaDAO mesaDAO, IPedidoDAO pedidoDAO) {
        this.mesaDAO = mesaDAO;
        this.pedidoDAO = pedidoDAO;
    }

    public List<Mesa> listarMesas() throws SQLException {
        return mesaDAO.listar();
    }

    public String alternarEstado(Mesa mesa) throws SQLException {
        String nuevo = "LIBRE".equalsIgnoreCase(mesa.getEstado()) ? "OCUPADO" : "LIBRE";
        if ("LIBRE".equals(nuevo) && pedidoDAO.existePedidoPendienteMesa(mesa.getIdMesa())) {
            throw new IllegalArgumentException("La mesa tiene un pedido pendiente. Primero complete o elimine el pedido.");
        }
        mesaDAO.cambiarEstado(mesa.getIdMesa(), nuevo);
        return nuevo;
    }
}
