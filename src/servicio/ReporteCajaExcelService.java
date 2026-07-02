package servicio;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import modelo.Caja;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReporteCajaExcelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReporteCajaExcelService.class);
    private static final List<String> ENCABEZADOS = ImmutableList.of("Pedido", "Monto", "Metodo", "Fecha", "Tipo");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public File exportar(List<Caja> movimientos, String fechaReporte, File archivo) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream salida = new FileOutputStream(archivo)) {
            Sheet sheet = workbook.createSheet("Caja " + fechaReporte);
            CellStyle encabezado = crearEstiloEncabezado(workbook);
            escribirEncabezado(sheet, encabezado);
            escribirMovimientos(sheet, movimientos);
            for (int i = 0; i < ENCABEZADOS.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(salida);
        }
        LOGGER.info("Reporte de caja exportado: {} columnas [{}]", archivo.getAbsolutePath(), Joiner.on(", ").join(ENCABEZADOS));
        return archivo;
    }

    private CellStyle crearEstiloEncabezado(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        estilo.setFont(fuente);
        return estilo;
    }

    private void escribirEncabezado(Sheet sheet, CellStyle estilo) {
        Row fila = sheet.createRow(0);
        for (int i = 0; i < ENCABEZADOS.size(); i++) {
            fila.createCell(i).setCellValue(ENCABEZADOS.get(i));
            fila.getCell(i).setCellStyle(estilo);
        }
    }

    private void escribirMovimientos(Sheet sheet, List<Caja> movimientos) {
        for (int i = 0; i < movimientos.size(); i++) {
            Caja caja = movimientos.get(i);
            Row fila = sheet.createRow(i + 1);
            fila.createCell(0).setCellValue(caja.getCodigoPedido());
            fila.createCell(1).setCellValue(caja.getMonto());
            fila.createCell(2).setCellValue(caja.getMetodoPago());
            fila.createCell(3).setCellValue(FORMATO_FECHA.format(caja.getFecha()));
            fila.createCell(4).setCellValue(caja.getTipoMovimiento());
        }
    }
}
