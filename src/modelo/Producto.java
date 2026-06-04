package modelo;

public class Producto {
    private int idProducto;
    private String nombre;
    private String categoria;
    private double precio;
    private String imagen;
    private boolean activo;

    public Producto(int idProducto, String nombre, String categoria, double precio, String imagen, boolean activo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagen = imagen;
        this.activo = activo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public String getImagen() {
        return imagen;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        return nombre + " - S/ " + String.format("%.2f", precio);
    }
}
