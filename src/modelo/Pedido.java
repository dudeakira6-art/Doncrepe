package modelo;

import java.time.LocalDateTime;

public class Pedido {
    private int idPedido;
    private String codigo;
    private String cliente;
    private double total;
    private String metodoPago;
    private String estado;
    private LocalDateTime fecha;
    private int mesaNumero;

    public Pedido(int idPedido, String codigo, String cliente, double total, String metodoPago, String estado, LocalDateTime fecha) {
        this.idPedido = idPedido;
        this.codigo = codigo;
        this.cliente = cliente;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.fecha = fecha;
    }

    public void setMesaNumero(int mesaNumero) {
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getMesaNumero() {
        return mesaNumero;
    }
}
