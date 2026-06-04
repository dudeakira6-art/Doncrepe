package modelo;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String usuario;
    private String rol;

    public Usuario(int idUsuario, String nombre, String usuario, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.usuario = usuario;
        this.rol = rol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }
}
