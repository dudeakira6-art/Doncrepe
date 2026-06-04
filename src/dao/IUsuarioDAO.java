package dao;

import java.sql.SQLException;
import modelo.Usuario;

public interface IUsuarioDAO {
    Usuario validarLogin(String usuario, String password) throws SQLException;
}
