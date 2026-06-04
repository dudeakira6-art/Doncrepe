package controlador;

import dao.IProductoDAO;
import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.List;
import modelo.Producto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductosController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductosController.class);
    private final IProductoDAO productoDAO;

    public ProductosController() {
        this(new ProductoDAO());
    }

    public ProductosController(IProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    public List<Producto> listarProductos() throws SQLException {
        return productoDAO.listarActivos();
    }

    public void guardarProducto(Producto producto) throws SQLException {
        Producto normalizado = normalizarProducto(producto);
        validarProducto(normalizado);
        if (normalizado.getIdProducto() == 0) {
            productoDAO.agregar(normalizado.getNombre(), normalizado.getCategoria(), normalizado.getPrecio(), normalizado.getImagen());
            LOGGER.info("Producto agregado: {} - S/ {}", normalizado.getNombre(), normalizado.getPrecio());
        } else {
            productoDAO.actualizar(normalizado);
            LOGGER.info("Producto actualizado: {} - S/ {}", normalizado.getNombre(), normalizado.getPrecio());
        }
    }

    public void eliminarProducto(Producto producto) throws SQLException {
        productoDAO.desactivar(producto.getIdProducto());
        LOGGER.info("Producto desactivado: {}", producto.getNombre());
    }

    public Producto normalizarProducto(Producto producto) {
        String nombre = capitalizarPalabras(producto.getNombre());
        String categoria = capitalizarPalabras(producto.getCategoria());
        String imagen = StringUtils.trimToEmpty(producto.getImagen());
        return new Producto(producto.getIdProducto(), nombre, categoria, producto.getPrecio(), imagen, producto.isActivo());
    }

    private void validarProducto(Producto producto) {
        if (StringUtils.isBlank(producto.getNombre())) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (StringUtils.isBlank(producto.getCategoria())) {
            throw new IllegalArgumentException("La categoria del producto es obligatoria.");
        }
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
    }

    private String capitalizarPalabras(String texto) {
        String limpio = StringUtils.trimToEmpty(texto).toLowerCase();
        String[] palabras = StringUtils.split(limpio);
        if (palabras == null || palabras.length == 0) {
            return "";
        }
        for (int i = 0; i < palabras.length; i++) {
            palabras[i] = StringUtils.capitalize(palabras[i]);
        }
        return StringUtils.join(palabras, " ");
    }
}
