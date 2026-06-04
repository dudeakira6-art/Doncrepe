package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Caja;

public class CajaDAO implements ICajaDAO {
    public double totalDelDia() throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM caja WHERE DATE(fecha) = CURDATE() AND tipo_movimiento = 'VENTA'";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public List<Caja> listarPorFecha(String fecha) throws SQLException {
        List<Caja> movimientos = new ArrayList<Caja>();
        String sql = "SELECT c.id_caja, p.codigo, c.monto, c.metodo_pago, c.fecha, c.tipo_movimiento "
                + "FROM caja c LEFT JOIN pedidos p ON p.id_pedido = c.id_pedido "
                + "WHERE DATE(c.fecha) = ? ORDER BY c.fecha DESC";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(new Caja(
                            rs.getInt("id_caja"),
                            rs.getString("codigo"),
                            rs.getDouble("monto"),
                            rs.getString("metodo_pago"),
                            rs.getTimestamp("fecha"),
                            rs.getString("tipo_movimiento")));
                }
            }
        }
        return movimientos;
    }
}
