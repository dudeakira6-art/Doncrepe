package controlador;

import dao.IUsuarioDAO;
import dao.UsuarioDAO;
import java.sql.SQLException;
import modelo.Usuario;

public class LoginController {
    private final IUsuarioDAO usuarioDAO;

    public LoginController() {
        this(new UsuarioDAO());
    }

    public LoginController(IUsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String usuario, String password) throws SQLException {
        if (usuario == null || usuario.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        return usuarioDAO.validarLogin(usuario.trim(), password);
    }
}
