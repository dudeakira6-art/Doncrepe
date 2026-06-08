package modelo;

import java.io.File;
import java.util.List;

public class ResultadoComprobante {
    private final Pedido pedido;
    private final List<DetallePedido> detalles;
    private final Comprobante comprobante;
    private final File archivoPdf;

    public ResultadoComprobante(Pedido pedido, List<DetallePedido> detalles, Comprobante comprobante, File archivoPdf) {
        this.pedido = pedido;
        this.detalles = detalles;
        this.comprobante = comprobante;
        this.archivoPdf = archivoPdf;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public File getArchivoPdf() {
        return archivoPdf;
    }
}
