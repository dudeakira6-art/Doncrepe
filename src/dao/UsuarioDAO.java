package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Usuario;
import util.SeguridadPassword;

public class UsuarioDAO implements IUsuarioDAO {
    public Usuario validarLogin(String usuario, String password) throws SQLException {
        String sql = "SELECT id_usuario, nombre, usuario, password, rol FROM usuarios WHERE LOWER(usuario) = LOWER(?) LIMIT 1";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && SeguridadPassword.coincide(rs.getString("usuario"), password, rs.getString("password"))) {
                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("usuario"),
                            SeguridadPassword.normalizarRol(rs.getString("usuario"), rs.getString("rol")));
                }
            }
        }
        return null;
    }
}
