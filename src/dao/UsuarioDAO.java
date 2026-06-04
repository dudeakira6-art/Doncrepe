package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Usuario;

public class UsuarioDAO implements IUsuarioDAO {
    public Usuario validarLogin(String usuario, String password) throws SQLException {
        String sql = "SELECT id_usuario, nombre, usuario, rol FROM usuarios WHERE usuario = ? AND password = ? LIMIT 1";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("usuario"),
                            rs.getString("rol"));
                }
            }
        }
        return null;
    }
}
