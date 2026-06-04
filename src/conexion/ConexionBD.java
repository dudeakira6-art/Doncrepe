package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "don_crepe_db";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true";

    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontro el driver MySQL Connector/J. Agregue el JAR al proyecto.", ex);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
