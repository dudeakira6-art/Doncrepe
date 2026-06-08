package controlador;

import dao.CajaDAO;
import dao.ICajaDAO;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import modelo.Caja;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import servicio.ReporteCajaExcelService;

public class HistorialCajaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistorialCajaController.class);
    private final ICajaDAO cajaDAO;
    private final ReporteCajaExcelService reporteCajaExcelService;

    public HistorialCajaController() {
        this(new CajaDAO(), new ReporteCajaExcelService());
    }

    public HistorialCajaController(ICajaDAO cajaDAO) {
        this(cajaDAO, new ReporteCajaExcelService());
    }

    public HistorialCajaController(ICajaDAO cajaDAO, ReporteCajaExcelService reporteCajaExcelService) {
        this.cajaDAO = cajaDAO;
        this.reporteCajaExcelService = reporteCajaExcelService;
    }

    public List<Caja> listarPorFecha(String fecha) throws SQLException {
        String fechaNormalizada = StringUtils.trimToEmpty(fecha);
        LOGGER.info("Consultando historial de caja para la fecha {}", fechaNormalizada);
        return cajaDAO.listarPorFecha(fechaNormalizada);
    }

    public List<Caja> listarPorRango(String desde, String hasta) throws SQLException {
        String desdeNormalizada = StringUtils.trimToEmpty(desde);
        String hastaNormalizada = StringUtils.trimToEmpty(hasta);
        LOGGER.info("Consultando historial de caja entre {} y {}", desdeNormalizada, hastaNormalizada);
        return cajaDAO.listarPorRango(desdeNormalizada, hastaNormalizada);
    }

    public double sumarMovimientos(List<Caja> movimientos) {
        double total = 0;
        for (Caja caja : movimientos) {
            total += caja.getMonto();
        }
        return total;
    }

    public File exportarExcel(String fecha, File carpetaDestino) throws SQLException, IOException {
        String fechaNormalizada = StringUtils.trimToEmpty(fecha);
        List<Caja> movimientos = listarPorFecha(fechaNormalizada);
        if (!carpetaDestino.exists() && !carpetaDestino.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de reportes.");
        }
        File archivo = new File(carpetaDestino, "caja_" + fechaNormalizada + ".xlsx");
        return reporteCajaExcelService.exportar(movimientos, fechaNormalizada, archivo);
    }

    public File exportarExcel(String desde, String hasta, File carpetaDestino) throws SQLException, IOException {
        String desdeNormalizada = StringUtils.trimToEmpty(desde);
        String hastaNormalizada = StringUtils.trimToEmpty(hasta);
        List<Caja> movimientos = listarPorRango(desdeNormalizada, hastaNormalizada);
        if (!carpetaDestino.exists() && !carpetaDestino.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de reportes.");
        }
        File archivo = new File(carpetaDestino, "caja_" + desdeNormalizada + "_" + hastaNormalizada + ".xlsx");
        return reporteCajaExcelService.exportar(movimientos, desdeNormalizada + " a " + hastaNormalizada, archivo);
    }
}
