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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    @Transactional(readOnly = true)
    public byte[] export(ExportQuoteRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndActiveTrue(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.vehicleId()));
        CalculateOnRoadCostResponse calc = onRoadCostService.calculate(request.toCalculateRequest());

        try (InputStream in = new ClassPathResource("templates/bang-bao-gia.xlsx").getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = resolveSheet(workbook, vehicle.getQuoteSheetName());
            workbook.setActiveSheet(workbook.getSheetIndex(sheet));
            fillQuote(sheet, vehicle, calc, request);
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
        writeAfterLabel(sheet, "Địa chỉ:", "Địa chỉ: " + nullToEmpty(request.customerAddress()));
        writeBesideLabel(sheet, "Loại xe:", vehicle.getName());
        writeBesideLabel(sheet, "Đời xe:", vehicle.getYear() == null ? "" : String.valueOf(vehicle.getYear()));
        writeBesideLabel(sheet, "Ngày:", DATE.format(LocalDate.now()));
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
}
