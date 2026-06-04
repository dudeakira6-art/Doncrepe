package dao;

import java.sql.SQLException;
import java.util.List;
import modelo.Producto;

public interface IProductoDAO {
    List<Producto> listarActivos() throws SQLException;

    void agregar(String nombre, String categoria, double precio, String imagen) throws SQLException;

    void actualizar(Producto producto) throws SQLException;

    void desactivar(int idProducto) throws SQLException;

    int contarActivos() throws SQLException;
}
