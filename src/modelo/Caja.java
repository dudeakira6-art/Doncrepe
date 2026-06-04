package modelo;

import java.util.Date;

public class Caja {
    private int idCaja;
    private String codigoPedido;
    private double monto;
    private String metodoPago;
    private Date fecha;
    private String tipoMovimiento;

    public Caja(int idCaja, String codigoPedido, double monto, String metodoPago, Date fecha, String tipoMovimiento) {
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

    public Date getFecha() {
        return fecha;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }
}
