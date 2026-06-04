package modelo;

public class Mesa {
    private int idMesa;
    private int numero;
    private String estado;

    public Mesa(int idMesa, int numero, String estado) {
        this.idMesa = idMesa;
        this.numero = numero;
        this.estado = estado;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public int getNumero() {
        return numero;
    }

    public String getEstado() {
        return estado;
    }
}
