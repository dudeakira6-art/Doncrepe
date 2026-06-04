package dao;

import java.sql.SQLException;
import java.util.List;
import modelo.Mesa;

public interface IMesaDAO {
    List<Mesa> listar() throws SQLException;

    void cambiarEstado(int idMesa, String estado) throws SQLException;
}
