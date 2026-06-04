package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Mesa;

public class MesaDAO implements IMesaDAO {
    public List<Mesa> listar() throws SQLException {
        List<Mesa> mesas = new ArrayList<Mesa>();
        String sql = "SELECT id_mesa, numero, estado FROM mesas ORDER BY numero";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mesas.add(new Mesa(rs.getInt("id_mesa"), rs.getInt("numero"), rs.getString("estado")));
            }
        }
        return mesas;
    }

    public void cambiarEstado(int idMesa, String estado) throws SQLException {
        String sql = "UPDATE mesas SET estado = ? WHERE id_mesa = ?";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idMesa);
            ps.executeUpdate();
        }
    }
}
