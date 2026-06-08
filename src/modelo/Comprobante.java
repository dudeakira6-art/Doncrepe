package modelo;

import java.util.Date;

public class Comprobante {
    public static final String BOLETA_SIMPLE = "BOLETA_SIMPLE";
    public static final String BOLETA_DNI = "BOLETA_DNI";
    public static final String FACTURA = "FACTURA";

    private int idComprobante;
    private int idPedido;
    private String tipo;
    private String numero;
    private String clienteNombre;
    private String dni;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String archivoPdf;
    private Date fecha;

    public Comprobante(int idComprobante, int idPedido, String tipo, String numero, String clienteNombre,
            String dni, String ruc, String razonSocial, String direccion, String archivoPdf, Date fecha) {
        this.idComprobante = idComprobante;
        this.idPedido = idPedido;
        this.tipo = tipo;
        this.numero = numero;
        this.clienteNombre = clienteNombre;
        this.dni = dni;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.archivoPdf = archivoPdf;
        this.fecha = fecha;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNumero() {
        return numero;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getDni() {
        return dni;
    }

    public String getRuc() {
        return ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getArchivoPdf() {
        return archivoPdf;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getNombreVisible() {
        if (FACTURA.equals(tipo)) {
            return "Factura";
        }
        if (BOLETA_DNI.equals(tipo)) {
            return "Boleta";
        }
        return "Boleta simple";
    }
}
