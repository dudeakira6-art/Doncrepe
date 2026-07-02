package modelo;

import java.time.LocalDateTime;

public class Caja {
    private int idCaja;
    private String codigoPedido;
    private double monto;
    private String metodoPago;
    private LocalDateTime fecha;
    private String tipoMovimiento;

    public Caja(int idCaja, String codigoPedido, double monto, String metodoPago, LocalDateTime fecha, String tipoMovimiento) {
        this.idCaja = idCaja;
        this.codigoPedido = codigoPedido;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getIdCaja() {
        return idCaja;
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public double getMonto() {
        return monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }
}
