package controlador;

import dao.IMesaDAO;
import dao.MesaDAO;
import java.sql.SQLException;
import java.util.List;
import modelo.Mesa;

public class MesasController {
    private final IMesaDAO mesaDAO;

    public MesasController() {
        this(new MesaDAO());
    }

    public MesasController(IMesaDAO mesaDAO) {
        this.mesaDAO = mesaDAO;
    }

    public List<Mesa> listarMesas() throws SQLException {
        return mesaDAO.listar();
    }

    public String alternarEstado(Mesa mesa) throws SQLException {
        String nuevo = "LIBRE".equalsIgnoreCase(mesa.getEstado()) ? "OCUPADO" : "LIBRE";
        mesaDAO.cambiarEstado(mesa.getIdMesa(), nuevo);
        return nuevo;
    }
}
