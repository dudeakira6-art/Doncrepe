package dao;

import java.sql.SQLException;
import java.util.List;
import modelo.Caja;

public interface ICajaDAO {
    double totalDelDia() throws SQLException;

    List<Caja> listarPorFecha(String fecha) throws SQLException;
}
