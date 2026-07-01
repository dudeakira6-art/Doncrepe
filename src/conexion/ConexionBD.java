package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConexionBD {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "don_crepe_db";
    private static final String CONFIG_FILE = "config/database.properties";

    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontro el driver MySQL Connector/J. Agregue el JAR al proyecto.", ex);
        }
        Properties configuracion = cargarConfiguracion();
        String host = valor(configuracion, "host", DEFAULT_HOST);
        String port = valor(configuracion, "port", DEFAULT_PORT);
        String database = valor(configuracion, "database", DEFAULT_DATABASE);
        String user = valorObligatorio(configuracion, "user");
        String password = valor(configuracion, "password", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, user, password);
    }

    private static Properties cargarConfiguracion() throws SQLException {
        Properties propiedades = new Properties();
        Path ruta = Paths.get(CONFIG_FILE);
        if (Files.exists(ruta)) {
            try (InputStream entrada = Files.newInputStream(ruta)) {
                propiedades.load(entrada);
                return propiedades;
            } catch (IOException ex) {
                throw new SQLException("No se pudo leer la configuracion de MySQL en " + ruta.toAbsolutePath(), ex);
            }
        }
        InputStream recurso = ConexionBD.class.getResourceAsStream("/database.properties");
        if (recurso != null) {
            try (InputStream entrada = recurso) {
                propiedades.load(entrada);
            } catch (IOException ex) {
                throw new SQLException("No se pudo leer la configuracion de MySQL embebida.", ex);
            }
        }
        return propiedades;
    }

    private static String valor(Properties propiedades, String clave, String porDefecto) {
        String sistema = System.getProperty("doncrepe.db." + clave);
        if (sistema != null && !sistema.isBlank()) {
            return sistema.trim();
        }
        String entorno = System.getenv("DONCREPE_DB_" + clave.toUpperCase());
        if (entorno != null && !entorno.isBlank()) {
            return entorno.trim();
        }
        String valor = propiedades.getProperty(clave);
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }

    private static String valorObligatorio(Properties propiedades, String clave) throws SQLException {
        String valor = valor(propiedades, clave, "");
        if (valor.isBlank()) {
            throw new SQLException("Falta configurar el parametro obligatorio '" + clave + "' en " + CONFIG_FILE
                    + " o en las variables DONCREPE_DB_" + clave.toUpperCase() + ".");
        }
        return valor;
    }
}
