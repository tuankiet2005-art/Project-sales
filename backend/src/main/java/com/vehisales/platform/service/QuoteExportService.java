package com.vehisales.platform.service;

import com.vehisales.platform.api.dto.CalculateOnRoadCostResponse;
import com.vehisales.platform.api.dto.ExportQuoteRequest;
import com.vehisales.platform.api.dto.FeeLineResponse;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.exception.ResourceNotFoundException;
import com.vehisales.platform.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.TextAlign;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTextBox;
import org.apache.poi.xssf.usermodel.XSSFTextParagraph;
import org.apache.poi.xssf.usermodel.XSSFTextRun;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuoteExportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VehicleRepository vehicleRepository;
    private final OnRoadCostService onRoadCostService;
    private final QuoteHistoryService quoteHistoryService;

    @Transactional
    public byte[] export(ExportQuoteRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndActiveTrue(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.vehicleId()));
        CalculateOnRoadCostResponse calc = onRoadCostService.calculate(request.toCalculateRequest());
        quoteHistoryService.persist(request, calc, vehicle);

        try (InputStream in = new ClassPathResource("templates/bang-bao-gia.xlsx").getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = resolveSheet(workbook, vehicle.getQuoteSheetName());
            workbook.setActiveSheet(workbook.getSheetIndex(sheet));
            fillQuote(sheet, vehicle, calc, request);
            revealHeaderLogos(sheet, request.language());
            translateSheet(sheet, request.language());

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to export quote", ex);
        }
    }

    private Sheet resolveSheet(XSSFWorkbook workbook, String preferred) {
        if (preferred != null && workbook.getSheet(preferred) != null) {
            return workbook.getSheet(preferred);
        }
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                return sheet;
            }
        }
        return workbook.getSheetAt(0);
    }

    public String filename(ExportQuoteRequest request) {
        String language = QuoteLabels.normalize(request.language());
        return "quote-" + language + ".xlsx";
    }

    private void revealHeaderLogos(Sheet sheet, String language) {
        for (int index = 0; index < sheet.getNumMergedRegions(); index++) {
            CellRangeAddress range = sheet.getMergedRegion(index);
            Row firstRow = sheet.getRow(range.getFirstRow());
            if (firstRow == null) {
                continue;
            }
            Cell first = firstRow.getCell(range.getFirstColumn());
            String text = cellText(first);
            if (text == null || (!text.toUpperCase().contains("MOVEO") && !text.toUpperCase().contains("MITSUBISHI"))) {
                continue;
            }
            XSSFWorkbook workbook = (XSSFWorkbook) sheet.getWorkbook();
            XSSFCellStyle style = workbook.createCellStyle();
            style.cloneStyleFrom(first.getCellStyle());
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(true);
            first.setCellStyle(style);
            first.setCellValue("");
            for (int rowIndex = range.getFirstRow(); rowIndex <= range.getLastRow(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    row.setHeightInPoints(Math.max(row.getHeightInPoints(), 22f));
                }
            }
            String headerText = fitHeaderBetweenLogos(text);
            if (!QuoteLabels.isVietnamese(language)) {
                headerText = QuoteLabels.translate(headerText, language);
            }
            composeMergedHeader(sheet, range, headerText);
            return;
        }
    }

    private void composeMergedHeader(Sheet sheet, CellRangeAddress header, String text) {
        if (!(sheet instanceof XSSFSheet xssfSheet)) {
            return;
        }
        XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
        if (drawing == null) {
            return;
        }
        long headerHeight = headerHeightEmu(sheet, header);
        long totalWidth = 0;
        for (int col = header.getFirstColumn(); col <= header.getLastColumn(); col++) {
            totalWidth += columnWidthEmu(xssfSheet, col);
        }
        final int edge = 50_000;
        final int gap = 120_000;
        long mitsuWidth = 0;
        long mitsuHeight = 0;
        long moveoWidth = 0;
        long moveoHeight = 0;
        CTOneCellAnchor mitsu = null;
        CTOneCellAnchor moveo = null;
        for (CTOneCellAnchor anchor : drawing.getCTDrawing().getOneCellAnchorList()) {
            if (anchor.getPic() == null || anchor.getPic().getNvPicPr() == null) {
                continue;
            }
            String name = anchor.getPic().getNvPicPr().getCNvPr().getName();
            if (isPicture(name, "image3.png")) {
                mitsu = anchor;
                mitsuHeight = Math.round(headerHeight * 0.78d);
                mitsuWidth = Math.round(mitsuHeight * 94d / 70d);
            } else if (isPicture(name, "image12.png")) {
                moveo = anchor;
                moveoHeight = Math.min(anchor.getExt().getCy(), Math.round(headerHeight * 0.72d));
                moveoWidth = anchor.getExt().getCx();
                if (anchor.getExt().getCy() > 0 && moveoHeight != anchor.getExt().getCy()) {
                    moveoWidth = Math.round(moveoWidth * (moveoHeight / (double) anchor.getExt().getCy()));
                }
            }
        }
        long textStart = edge + mitsuWidth + gap;
        long textEnd = totalWidth - edge - moveoWidth - gap;
        if (textEnd <= textStart + 200_000) {
            textEnd = totalWidth - edge - gap;
        }
        if (mitsu != null) {
            pinPicture(xssfSheet, mitsu, header, edge, mitsuWidth, mitsuHeight, headerHeight);
        }
        if (moveo != null) {
            pinPicture(xssfSheet, moveo, header, totalWidth - edge - moveoWidth, moveoWidth, moveoHeight, headerHeight);
        }
        int[] start = colAndOffset(xssfSheet, header, textStart);
        int[] end = colAndOffset(xssfSheet, header, textEnd);
        Row lastRow = sheet.getRow(header.getLastRow());
        int lastRowEmu = (int) Math.round((lastRow == null ? 22f : lastRow.getHeightInPoints()) * 12700d);
        XSSFClientAnchor textAnchor = drawing.createAnchor(
                start[1], 0, end[1], lastRowEmu,
                start[0], header.getFirstRow(), end[0], header.getLastRow());
        XSSFTextBox box = drawing.createTextbox(textAnchor);
        box.setNoFill(true);
        box.setLineWidth(0);
        box.setVerticalAlignment(VerticalAlignment.CENTER);
        box.clearText();
        for (String line : text.split("\n")) {
            XSSFTextParagraph paragraph = box.addNewTextParagraph(line);
            paragraph.setTextAlign(TextAlign.CENTER);
            for (XSSFTextRun run : paragraph.getTextRuns()) {
                run.setFont("Times New Roman");
                run.setFontSize(12);
                run.setBold(true);
                run.setFontColor(new java.awt.Color(0x1F, 0x1F, 0x1F));
            }
        }
    }

    private void pinPicture(
            XSSFSheet sheet,
            CTOneCellAnchor anchor,
            CellRangeAddress header,
            long x,
            long width,
            long height,
            long headerHeight
    ) {
        int[] pos = colAndOffset(sheet, header, Math.max(0, x));
        long top = Math.max(0, (headerHeight - height) / 2);
        anchor.getFrom().setCol(pos[0]);
        anchor.getFrom().setColOff(pos[1]);
        anchor.getFrom().setRow(header.getFirstRow());
        anchor.getFrom().setRowOff((int) Math.min(Integer.MAX_VALUE, top));
        anchor.getExt().setCx(width);
        anchor.getExt().setCy(height);
    }

    private boolean isPicture(String name, String fileName) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.equalsIgnoreCase(fileName) || trimmed.equalsIgnoreCase(fileName.replace(".png", ""));
    }

    private long columnWidthEmu(XSSFSheet sheet, int column) {
        return Math.round(sheet.getColumnWidthInPixels(column) * 9525d);
    }

    private int[] colAndOffset(XSSFSheet sheet, CellRangeAddress header, long emuFromStart) {
        long remaining = Math.max(0, emuFromStart);
        for (int col = header.getFirstColumn(); col <= header.getLastColumn(); col++) {
            long width = columnWidthEmu(sheet, col);
            if (remaining <= width || col == header.getLastColumn()) {
                return new int[]{col, (int) Math.min(Integer.MAX_VALUE, remaining)};
            }
            remaining -= width;
        }
        return new int[]{header.getLastColumn(), 0};
    }

    private long headerHeightEmu(Sheet sheet, CellRangeAddress header) {
        long headerHeight = 0;
        for (int rowIndex = header.getFirstRow(); rowIndex <= header.getLastRow(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            float points = row == null ? sheet.getDefaultRowHeightInPoints() : row.getHeightInPoints();
            headerHeight += Math.round(points * 12700d);
        }
        return headerHeight;
    }

    private String fitHeaderBetweenLogos(String text) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (String raw : text.replace("\r", "").split("\n")) {
            for (String part : raw.split("\\s+-\\s+")) {
                lines.addAll(wrapHeaderLine(part.trim(), 38));
            }
        }
        return String.join("\n", lines);
    }

    private java.util.List<String> wrapHeaderLine(String text, int maxChars) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text.isBlank()) {
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
                continue;
            }
            if (current.length() + 1 + word.length() <= maxChars) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private void translateSheet(Sheet sheet, String language) {
        if (QuoteLabels.isVietnamese(language)) {
            return;
        }
        for (Row row : sheet) {
            for (Cell cell : row) {
                String text = cellText(cell);
                if (text == null) {
                    continue;
                }
                String translated = QuoteLabels.translate(text, language);
                if (!translated.equals(text)) {
                    cell.setCellValue(translated);
                }
            }
        }
    }

    private void fillQuote(Sheet sheet, Vehicle vehicle, CalculateOnRoadCostResponse calc, ExportQuoteRequest request) {
        Map<String, BigDecimal> amounts = new HashMap<>();
        for (FeeLineResponse fee : calc.fees()) {
            amounts.put(fee.code(), fee.includedInTotal() ? fee.amount() : BigDecimal.ZERO);
        }

        String color = request.color() == null || request.color().isBlank()
                ? nullToEmpty(vehicle.getDefaultColor())
                : request.color();
        BigDecimal deposit = calc.deposit() == null ? BigDecimal.ZERO : calc.deposit();
        BigDecimal extras = calc.accessoriesTotal() == null ? BigDecimal.ZERO : calc.accessoriesTotal();
        BigDecimal secondPayment = calc.estimatedOnRoadTotal().subtract(deposit);
        String accessoryNames = calc.accessories() == null || calc.accessories().isEmpty()
                ? "Phụ kiện trang bị thêm (Nếu có)"
                : "Phụ kiện trang bị thêm: " + calc.accessories().stream()
                .map(item -> item.name() + " (" + item.amount().toPlainString() + ")")
                .reduce((left, right) -> left + "; " + right)
                .orElse("");

        writeAfterLabel(sheet, "Khách hàng:", "Khách hàng: " + request.customerName());
        writeAfterLabel(sheet, "Địa chỉ:", "Địa chỉ: " + padAddress(request.customerAddress()));
        writeBesideLabel(sheet, "Loại xe:", vehicle.getName());
        writeAfterLabel(sheet, "Đời xe:", "Đời xe: " + (vehicle.getYear() == null ? "" : vehicle.getYear()));
        writeAfterLabel(sheet, "Ngày:", "Ngày: " + DATE.format(LocalDate.now()));
        writeBesideLabel(sheet, "Giá niêm yết:", calc.listPrice());
        writeBesideLabel(sheet, "Giảm giá:", zeroIfNull(calc.discountAmount()));
        writeBesideLabel(sheet, "Giá Bán:", calc.salePrice());
        writeBesideLabel(sheet, "Màu xe", color);
        writeBesideLabel(sheet, "TG giao xe:", nullToEmpty(vehicle.getDeliveryNote()));
        writeBesideLabel(sheet, "Thuế trước bạ (tạm tính)", amounts.getOrDefault("REGISTRATION_TAX", BigDecimal.ZERO));
        writeBesideLabel(sheet, "Phí bấm biển số", amounts.getOrDefault("LICENSE_PLATE", BigDecimal.ZERO));
        writeBesideLabel(sheet, "Lệ phí đăng kiểm", amounts.getOrDefault("INSPECTION", BigDecimal.ZERO));
        writeBesideLabel(sheet, "Bảo hiểm TNDS + Người ngồi xe (1 năm)", amounts.getOrDefault("COMPULSORY_INSURANCE", BigDecimal.ZERO));
        writeBesideLabel(sheet, "Phí sử dụng đường bộ (1 năm)", amounts.getOrDefault("ROAD_USE", BigDecimal.ZERO));
        writeBesideLabel(sheet, "Bảo hiểm vật chất thân vỏ xe", amounts.getOrDefault("OPTIONAL_BODY_INSURANCE", BigDecimal.ZERO));
        writeBesideLabel(sheet, "Biển số mica", amounts.getOrDefault("MICA_PLATE", BigDecimal.ZERO));
        writeBesideLabel(sheet, "Phí dịch vụ đăng ký xe",
                firstNonNull(amounts.get("REGISTRATION_SERVICE"), amounts.get("REGISTRATION_FEE"), BigDecimal.ZERO));
        writeBesideLabel(sheet, "Tổng Chi Phí Đăng ký xe", calc.totalMandatoryFees().add(calc.totalOptionalFees()));
        writeAfterLabel(sheet, "Phụ kiện trang bị thêm", accessoryNames);
        writeBesideLabel(sheet, "TỔNG LĂNG BÁNH", calc.estimatedOnRoadTotal());
        writeBesideLabel(sheet, "TỔNG CP PHÁT SINH", extras);
        writeBesideLabel(sheet, "Chi Phí Phát sinh thêm (Nếu có)", extras);
        writeBesideLabel(sheet, "Tiền cọc:", deposit);
        writeBesideLabel(sheet, "THANH TOÁN LẦN 2", secondPayment.max(BigDecimal.ZERO));
    }

    private void writeAfterLabel(Sheet sheet, String prefix, String fullValue) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                String text = cellText(cell);
                if (text != null && text.startsWith(prefix)) {
                    cell.setCellValue(fullValue);
                    return;
                }
            }
        }
    }

    private void writeBesideLabel(Sheet sheet, String label, Object value) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                String text = cellText(cell);
                if (text == null) {
                    continue;
                }
                if (text.equals(label) || text.startsWith(label)) {
                    Cell target = row.getCell(cell.getColumnIndex() + 1);
                    if (target == null) {
                        target = row.createCell(cell.getColumnIndex() + 1);
                    }
                    writeValue(target, value);
                    return;
                }
            }
        }
    }

    private void writeValue(Cell cell, Object value) {
        if (value instanceof BigDecimal decimal) {
            cell.setCellValue(decimal.doubleValue());
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value == null ? "" : String.valueOf(value));
        }
    }

    private String cellText(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.STRING) {
            return null;
        }
        return cell.getStringCellValue().trim();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal firstNonNull(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value != null) {
                return value;
            }
        }
        return BigDecimal.ZERO;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String padAddress(String address) {
        String value = nullToEmpty(address);
        return "Địa chỉ: " + value + "                                                                                          TVBH:        - SĐT: ";
    }
}
