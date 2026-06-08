package dao;

import java.sql.SQLException;
import java.util.List;
import modelo.DetallePedido;
import modelo.Pedido;

public interface IPedidoDAO {
    List<Pedido> listarRecientes() throws SQLException;

    Pedido buscarPorCodigo(String codigo) throws SQLException;

    List<DetallePedido> listarDetalles(String codigo) throws SQLException;

    int pedidosHoy() throws SQLException;

    double ventasHoy() throws SQLException;

    void crearPedido(int idUsuario, int idMesa, String cliente, String metodoPago, List<DetallePedido> detalles) throws SQLException;

    void registrarPago(String codigo, String metodoPago) throws SQLException;

    void eliminarPorCodigo(String codigo) throws SQLException;
}
