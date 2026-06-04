package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Producto;

public class ProductoDAO implements IProductoDAO {
    public List<Producto> listarActivos() throws SQLException {
        List<Producto> productos = new ArrayList<Producto>();
        String sql = "SELECT id_producto, nombre, categoria, precio, imagen, activo FROM productos WHERE activo = 1 ORDER BY categoria, nombre";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }
        return productos;
    }

    public void agregar(String nombre, String categoria, double precio, String imagen) throws SQLException {
        String sql = "INSERT INTO productos(nombre, categoria, precio, imagen, activo) VALUES(?, ?, ?, ?, 1)";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, categoria);
            ps.setDouble(3, precio);
            ps.setString(4, imagen);
            ps.executeUpdate();
        }
    }

    public void actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, categoria = ?, precio = ?, imagen = ? WHERE id_producto = ?";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getCategoria());
            ps.setDouble(3, producto.getPrecio());
            ps.setString(4, producto.getImagen());
            ps.setInt(5, producto.getIdProducto());
            ps.executeUpdate();
        }
    }

    public void desactivar(int idProducto) throws SQLException {
        String sql = "UPDATE productos SET activo = 0 WHERE id_producto = ?";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.executeUpdate();
        }
    }

    public int contarActivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE activo = 1";
        try (Connection cn = ConexionBD.getConexion();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getDouble("precio"),
                rs.getString("imagen"),
                rs.getBoolean("activo"));
    }
}
