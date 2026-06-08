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
        return listarPorRango(fecha, fecha);
    }

    public List<Caja> listarPorRango(String desde, String hasta) throws SQLException {
        List<Caja> movimientos = new ArrayList<Caja>();
        String sql = "SELECT c.id_caja, p.codigo, c.monto, c.metodo_pago, c.fecha, "
                + "CASE WHEN p.id_mesa IS NULL THEN 'Delivery' ELSE 'Local' END AS tipo_atencion "
                + "FROM caja c LEFT JOIN pedidos p ON p.id_pedido = c.id_pedido "
                + "WHERE p.estado = 'COMPLETADO' AND DATE(c.fecha) BETWEEN ? AND ? ORDER BY c.fecha DESC";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(new Caja(
                            rs.getInt("id_caja"),
                            rs.getString("codigo"),
                            rs.getDouble("monto"),
                            rs.getString("metodo_pago"),
                            rs.getTimestamp("fecha"),
                            rs.getString("tipo_atencion")));
                }
            }
        }
        return movimientos;
    }
}
