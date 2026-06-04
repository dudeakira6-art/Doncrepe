package modelo;

import java.util.Date;

public class Pedido {
    private int idPedido;
    private String codigo;
    private String cliente;
    private double total;
    private String metodoPago;
    private String estado;
    private Date fecha;
    private int mesaNumero;

    public Pedido(int idPedido, String codigo, String cliente, double total, String metodoPago, String estado, Date fecha, int mesaNumero) {
        this.idPedido = idPedido;
        this.codigo = codigo;
        this.cliente = cliente;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.fecha = fecha;
        this.mesaNumero = mesaNumero;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCliente() {
        return cliente;
    }

    public double getTotal() {
        return total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public Date getFecha() {
        return fecha;
    }

    public int getMesaNumero() {
        return mesaNumero;
    }
}
