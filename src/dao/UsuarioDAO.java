package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Usuario;
import util.SeguridadPassword;

public class UsuarioDAO implements IUsuarioDAO {
    private static final String COLUMNA_USUARIO = "usuario";

    public Usuario validarLogin(String usuario, String password) throws SQLException {
        String sql = "SELECT id_usuario, nombre, " + COLUMNA_USUARIO + ", password, rol FROM usuarios WHERE LOWER(" + COLUMNA_USUARIO + ") = LOWER(?) LIMIT 1";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && SeguridadPassword.coincide(rs.getString(COLUMNA_USUARIO), password, rs.getString("password"))) {
                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString(COLUMNA_USUARIO),
                            SeguridadPassword.normalizarRol(rs.getString(COLUMNA_USUARIO), rs.getString("rol")));
                }
            }
        }
        return null;
    }
}
