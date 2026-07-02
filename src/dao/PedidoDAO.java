package dao;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.Comprobante;
import modelo.DetallePedido;
import modelo.Pedido;
import modelo.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicio.CalculadoraPedido;

public class PedidoDAO implements IPedidoDAO {
    private static final String COL_ID_PEDIDO = "id_pedido";
    private static final String COL_ID_COMPROBANTE = "id_comprobante";
    private static final String TABLA_COMPROBANTES = "comprobantes";
    private static final String ESTADO_COMPLETADO = "COMPLETADO";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final Logger LOGGER = LoggerFactory.getLogger(PedidoDAO.class);
    private final CalculadoraPedido calculadora = new CalculadoraPedido();
    public List<Pedido> listarRecientes() throws SQLException {
        List<Pedido> pedidos = new ArrayList<Pedido>();
        String sql = "SELECT p." + COL_ID_PEDIDO + ", p.codigo, p.cliente, p.total, p.metodo_pago, p.estado, p.fecha, m.numero "
                + "FROM pedidos p LEFT JOIN mesas m ON m.id_mesa = p.id_mesa ORDER BY p.fecha DESC LIMIT 50";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pedidos.add(mapear(rs));
            }
        }
        return pedidos;
    }

    public int pedidosHoy() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE DATE(fecha) = CURDATE()";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int pedidosPendientes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE estado = '" + ESTADO_PENDIENTE + "'";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Pedido buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT p." + COL_ID_PEDIDO + ", p.codigo, p.cliente, p.total, p.metodo_pago, p.estado, p.fecha, m.numero "
                + "FROM pedidos p LEFT JOIN mesas m ON m.id_mesa = p.id_mesa WHERE p.codigo = ? LIMIT 1";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<DetallePedido> listarDetalles(String codigo) throws SQLException {
        List<DetallePedido> detalles = new ArrayList<DetallePedido>();
        String sql = "SELECT pr.id_producto, pr.nombre, pr.categoria, pr.precio, pr.imagen, pr.activo, d.cantidad "
                + "FROM detalle_pedido d "
                + "INNER JOIN pedidos p ON p.id_pedido = d.id_pedido "
                + "INNER JOIN productos pr ON pr.id_producto = d.id_producto "
                + "WHERE p.codigo = ? ORDER BY d.id_detalle";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getDouble("precio"),
                            rs.getString("imagen"),
                            rs.getBoolean("activo"));
                    detalles.add(new DetallePedido(producto, rs.getInt("cantidad")));
                }
            }
        }
        return detalles;
    }

    public boolean existePedidoPendienteMesa(int idMesa) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE id_mesa = ? AND estado = '" + ESTADO_PENDIENTE + "'";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public double ventasHoy() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM pedidos WHERE DATE(fecha) = CURDATE() AND estado = '" + ESTADO_COMPLETADO + "'";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public void crearPedido(int idUsuario, int idMesa, String cliente, String metodoPago, List<DetallePedido> detalles) throws SQLException {
        double total = calculadora.calcularTotal(detalles);

        String codigo = "P-" + System.currentTimeMillis();
        String sqlPedido = "INSERT INTO pedidos(codigo, id_usuario, id_mesa, cliente, total, metodo_pago, estado, fecha) "
                + "VALUES(?, ?, ?, ?, ?, ?, '" + ESTADO_PENDIENTE + "', NOW())";
        String sqlDetalle = "INSERT INTO detalle_pedido(id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES(?, ?, ?, ?, ?)";
        String sqlMesa = "UPDATE mesas SET estado = 'OCUPADO' WHERE id_mesa = ?";

        Connection cn = null;
        try {
            cn = ConexionBD.getConexion();
            asegurarPedidosPermiteDelivery(cn);
            cn.setAutoCommit(false);
            int idPedido;
            try (PreparedStatement ps = cn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, codigo);
                ps.setInt(2, idUsuario);
                if (idMesa > 0) {
                    ps.setInt(3, idMesa);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setString(4, cliente);
                ps.setDouble(5, total);
                ps.setString(6, metodoPago);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No se pudo obtener el ID del pedido.");
                    }
                    idPedido = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idPedido);
                for (DetallePedido detalle : detalles) {
                    ps.setInt(2, detalle.getProducto().getIdProducto());
                    ps.setInt(3, detalle.getCantidad());
                    ps.setDouble(4, detalle.getProducto().getPrecio());
                    ps.setDouble(5, detalle.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            if (idMesa > 0) {
                try (PreparedStatement ps = cn.prepareStatement(sqlMesa)) {
                    ps.setInt(1, idMesa);
                    ps.executeUpdate();
                }
            }

            cn.commit();
            LOGGER.info("Pedido {} guardado como pendiente con total S/ {}", codigo, total);
        } catch (SQLException ex) {
            if (cn != null) {
                cn.rollback();
            }
            LOGGER.error("No se pudo guardar el pedido {}.", codigo, ex);
            throw new SQLException("No se pudo guardar el pedido " + codigo + ".", ex);
        } finally {
            if (cn != null) {
                cn.setAutoCommit(true);
                cn.close();
            }
        }
    }

    public void registrarPago(String codigo, String metodoPago, Comprobante comprobante) throws SQLException {
        String sqlBuscar = "SELECT " + COL_ID_PEDIDO + ", id_mesa, total, estado FROM pedidos WHERE codigo = ? FOR UPDATE";
        String sqlPedido = "UPDATE pedidos SET metodo_pago = ?, estado = '" + ESTADO_COMPLETADO + "' WHERE id_pedido = ?";
        String sqlCaja = "INSERT INTO caja(id_pedido, monto, metodo_pago, fecha, tipo_movimiento) VALUES(?, ?, ?, NOW(), 'VENTA')";
        String sqlMesa = "UPDATE mesas SET estado = 'LIBRE' WHERE id_mesa = ?";
        String sqlComprobante = "INSERT INTO " + TABLA_COMPROBANTES + "(id_pedido, tipo, numero, cliente_nombre, dni, ruc, razon_social, direccion, archivo_pdf, fecha) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        Connection cn = null;
        try {
            cn = ConexionBD.getConexion();
            asegurarTablaComprobantes(cn);
            cn.setAutoCommit(false);
            int idPedido;
            int idMesa;
            double total;
            String estado;
            try (PreparedStatement ps = cn.prepareStatement(sqlBuscar)) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("No se encontró el pedido " + codigo);
                    }
                    idPedido = rs.getInt(COL_ID_PEDIDO);
                    idMesa = rs.getInt("id_mesa");
                    if (rs.wasNull()) {
                        idMesa = 0;
                    }
                    total = rs.getDouble("total");
                    estado = rs.getString("estado");
                }
            }
            if (ESTADO_COMPLETADO.equalsIgnoreCase(estado)) {
                throw new SQLException("El pedido " + codigo + " ya fue pagado.");
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlPedido)) {
                ps.setString(1, metodoPago);
                ps.setInt(2, idPedido);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlCaja)) {
                ps.setInt(1, idPedido);
                ps.setDouble(2, total);
                ps.setString(3, metodoPago);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlComprobante)) {
                ps.setInt(1, idPedido);
                ps.setString(2, comprobante.getTipo());
                ps.setString(3, comprobante.getNumero());
                ps.setString(4, comprobante.getClienteNombre());
                ps.setString(5, comprobante.getDni());
                ps.setString(6, comprobante.getRuc());
                ps.setString(7, comprobante.getRazonSocial());
                ps.setString(8, comprobante.getDireccion());
                ps.setString(9, comprobante.getArchivoPdf());
                ps.executeUpdate();
            }
            if (idMesa > 0) {
                try (PreparedStatement ps = cn.prepareStatement(sqlMesa)) {
                    ps.setInt(1, idMesa);
                    ps.executeUpdate();
                }
            }
            cn.commit();
            LOGGER.info("Pedido {} pagado con {} por S/ {}", codigo, metodoPago, total);
        } catch (SQLException ex) {
            if (cn != null) {
                cn.rollback();
            }
            LOGGER.error("No se pudo registrar el pago del pedido {}.", codigo, ex);
            throw new SQLException("No se pudo registrar el pago del pedido " + codigo + ".", ex);
        } finally {
            if (cn != null) {
                cn.setAutoCommit(true);
                cn.close();
            }
        }
    }

    public Comprobante buscarComprobantePorPedido(String codigo) throws SQLException {
        String sql = "SELECT co." + COL_ID_COMPROBANTE + ", co." + COL_ID_PEDIDO + ", co.tipo, co.numero, co.cliente_nombre, co.dni, co.ruc, "
                + "co.razon_social, co.direccion, co.archivo_pdf, co.fecha "
                + "FROM " + TABLA_COMPROBANTES + " co INNER JOIN pedidos p ON p." + COL_ID_PEDIDO + " = co." + COL_ID_PEDIDO + " "
                + "WHERE p.codigo = ? ORDER BY co.fecha DESC LIMIT 1";
        try (Connection cn = ConexionBD.getConexion()) {
            asegurarTablaComprobantes(cn);
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapearComprobante(rs) : null;
                }
            }
        }
    }

    public void eliminarPorCodigo(String codigo) throws SQLException {
        String sqlBuscar = "SELECT " + COL_ID_PEDIDO + ", id_mesa FROM pedidos WHERE codigo = ?";
        String sqlComprobante = "DELETE FROM " + TABLA_COMPROBANTES + " WHERE " + COL_ID_PEDIDO + " = ?";
        String sqlCaja = "DELETE FROM caja WHERE " + COL_ID_PEDIDO + " = ?";
        String sqlDetalle = "DELETE FROM detalle_pedido WHERE " + COL_ID_PEDIDO + " = ?";
        String sqlPedido = "DELETE FROM pedidos WHERE " + COL_ID_PEDIDO + " = ?";
        String sqlMesa = "UPDATE mesas SET estado = 'LIBRE' WHERE id_mesa = ?";

        Connection cn = null;
        try {
            cn = ConexionBD.getConexion();
            cn.setAutoCommit(false);
            int idPedido = 0;
            int idMesa = 0;
            try (PreparedStatement ps = cn.prepareStatement(sqlBuscar)) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("No se encontró el pedido " + codigo);
                    }
                    idPedido = rs.getInt(COL_ID_PEDIDO);
                    idMesa = rs.getInt("id_mesa");
                    if (rs.wasNull()) {
                        idMesa = 0;
                    }
                }
            }
            asegurarTablaComprobantes(cn);
            try (PreparedStatement ps = cn.prepareStatement(sqlComprobante)) {
                ps.setInt(1, idPedido);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlCaja)) {
                ps.setInt(1, idPedido);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idPedido);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlPedido)) {
                ps.setInt(1, idPedido);
                ps.executeUpdate();
            }
            if (idMesa > 0) {
                try (PreparedStatement ps = cn.prepareStatement(sqlMesa)) {
                    ps.setInt(1, idMesa);
                    ps.executeUpdate();
                }
            }
            cn.commit();
            LOGGER.info("Pedido {} eliminado correctamente.", codigo);
        } catch (SQLException ex) {
            if (cn != null) {
                cn.rollback();
            }
            LOGGER.error("No se pudo eliminar el pedido {}.", codigo, ex);
            throw new SQLException("No se pudo eliminar el pedido " + codigo + ".", ex);
        } finally {
            if (cn != null) {
                cn.setAutoCommit(true);
                cn.close();
            }
        }
    }

    private void asegurarPedidosPermiteDelivery(Connection cn) {
        String sql = "ALTER TABLE pedidos MODIFY id_mesa INT NULL";
        try (Statement st = cn.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            LOGGER.debug("No se modifico id_mesa a nullable; puede que ya este actualizado.", ex);
        }
    }

    private void asegurarTablaComprobantes(Connection cn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_COMPROBANTES + " ("
                + COL_ID_COMPROBANTE + " INT AUTO_INCREMENT PRIMARY KEY,"
                + COL_ID_PEDIDO + " INT NOT NULL,"
                + "tipo VARCHAR(30) NOT NULL,"
                + "numero VARCHAR(40) NOT NULL,"
                + "cliente_nombre VARCHAR(160),"
                + "dni VARCHAR(20),"
                + "ruc VARCHAR(20),"
                + "razon_social VARCHAR(180),"
                + "direccion VARCHAR(220),"
                + "archivo_pdf VARCHAR(255),"
                + "fecha DATETIME NOT NULL,"
                + "CONSTRAINT fk_comprobantes_pedido FOREIGN KEY (" + COL_ID_PEDIDO + ") REFERENCES pedidos(" + COL_ID_PEDIDO + ")"
                + ")";
        try (Statement st = cn.createStatement()) {
            st.execute(sql);
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido(
                rs.getInt(COL_ID_PEDIDO),
                rs.getString("codigo"),
                rs.getString("cliente"),
                rs.getDouble("total"),
                rs.getString("metodo_pago"),
                rs.getString("estado"),
                toLocalDateTime(rs, "fecha"));
        pedido.setMesaNumero(rs.getInt("numero"));
        return pedido;
    }

    private Comprobante mapearComprobante(ResultSet rs) throws SQLException {
        Comprobante comprobante = new Comprobante(
                rs.getInt(COL_ID_COMPROBANTE),
                rs.getInt(COL_ID_PEDIDO),
                rs.getString("tipo"),
                rs.getString("numero"),
                rs.getString("archivo_pdf"),
                toLocalDateTime(rs, "fecha"));
        comprobante.setClienteNombre(rs.getString("cliente_nombre"));
        comprobante.setDni(rs.getString("dni"));
        comprobante.setRuc(rs.getString("ruc"));
        comprobante.setRazonSocial(rs.getString("razon_social"));
        comprobante.setDireccion(rs.getString("direccion"));
        return comprobante;
    }

    private LocalDateTime toLocalDateTime(ResultSet rs, String columna) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(columna);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
