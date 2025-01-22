package digio.excel.services;

import digio.excel.validator.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Service
public class DynamicCheckingService {

    private final Map<Pattern, Function<String, String>> validationRules = new HashMap<>();

    public DynamicCheckingService() {
        initializeDefaultValidationRules();
    }

    private void initializeDefaultValidationRules() {
        Arrays.stream(ValidationType.values()).forEach(type ->
                validationRules.put(type.getPattern(), type.getValidator())
        );
    }

    private enum ValidationType {
        NAME("^(ชื่อ|name|ชื่อนามสกุล|fullname).*", NameValidator::validate),
        EMAIL("^(อีเมล|email).*", EmailValidator::validate),
        CITIZEN_ID("^(บัตรประชาชน|citizenid).*", IDValidator::validate),
        PHONE("^(เบอร์โทร|phone).*", TelValidator::validate),
        ADDRESS("^(ที่อยู่|address).*", AddressValidator::validate);

        private final Pattern pattern;
        private final Function<String, String> validator;

        ValidationType(String regex, Function<String, String> validator) {
            this.pattern = Pattern.compile(regex);
            this.validator = validator;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public Function<String, String> getValidator() {
            return validator;
        }
    }

    public List<String> validateExcel(MultipartFile file) {
        return validate(file, null, "ไม่สามารถอ่านไฟล์ Excel ได้");
    }

    public List<String> validateExcelWithSelectedHeaders(MultipartFile file, List<String> selectedHeaders) {
        if (selectedHeaders == null || selectedHeaders.isEmpty()) {
            throw new IllegalArgumentException("โปรดระบุหัวข้อที่ต้องการตรวจสอบ");
        }
        return validate(file, selectedHeaders, "ไม่สามารถอ่านไฟล์ Excel ได้");
    }

    private List<String> validate(MultipartFile file, List<String> selectedHeaders, String errorMessage) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้");
        }
        Map<Integer, StringBuilder> errorMap = new TreeMap<>();
        Instant startTime = Instant.now();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            if (selectedHeaders != null && getSelectedHeaderIndices(headers, selectedHeaders).isEmpty()) {
                throw new IllegalArgumentException("ไม่พบหัวข้อที่เลือกในไฟล์ Excel");
            }

            processRows(sheet, headers, selectedHeaders == null ? null : getSelectedHeaderIndices(headers, selectedHeaders), errorMap);
        } catch (IOException e) {
            throw new IllegalArgumentException(errorMessage, e);
        }

        Instant endTime = Instant.now();
        System.out.println("เวลาในการประมวลผล: " + formatDuration(Duration.between(startTime, endTime).toMillis()));

        return formatErrorMap(errorMap);
    }

    private List<String> extractHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("ไม่มีแถวหัวข้อในไฟล์ Excel");
        }

        return StreamSupport.stream(headerRow.spliterator(), false)
                .map(cell -> cell.getStringCellValue().trim().toLowerCase())
                .collect(Collectors.toList());
    }

    private List<Integer> getSelectedHeaderIndices(List<String> headers, List<String> selectedHeaders) {
        return selectedHeaders.stream()
                .map(headers::indexOf)
                .filter(index -> index >= 0)
                .collect(Collectors.toList());
    }

    private void processRows(Sheet sheet, List<String> headers, List<Integer> selectedHeaderIndices, Map<Integer, StringBuilder> errorMap) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || isRowEmpty(row)) continue;

            StringBuilder errorBuilder = new StringBuilder();
            List<Integer> indices = selectedHeaderIndices != null ? selectedHeaderIndices :
                    IntStream.range(0, headers.size()).boxed().collect(Collectors.toList());

            indices.forEach(index -> {
                String header = headers.get(index);
                String cellValue = getCellValue(row.getCell(index));
                applyValidationRules(header, cellValue, errorBuilder);
            });

            if (!errorBuilder.isEmpty()) {
                errorMap.put(row.getRowNum() + 1, errorBuilder);
            }
        }
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private void applyValidationRules(String header, String cellValue, StringBuilder errorBuilder) {
        boolean matched = validationRules.entrySet().stream()
                .anyMatch(entry -> {
                    if (entry.getKey().matcher(header).matches()) {
                        String error = entry.getValue().apply(cellValue);
                        if (error != null) {
                            appendError(errorBuilder, error);
                        }
                        return true;
                    }
                    return false;
                });

        if (!matched) {
            appendError(errorBuilder, "พบหัวข้อที่ไม่รู้จัก: " + header);
        }
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private void appendError(StringBuilder errorBuilder, String errorMessage) {
        if (!errorBuilder.isEmpty()) {
            errorBuilder.append(", ");
        }
        errorBuilder.append(errorMessage);
    }

    private List<String> formatErrorMap(Map<Integer, StringBuilder> errorMap) {
        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.toList());
    }

    private String formatDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + " ชั่วโมง " + (minutes % 60) + " นาที";
        } else if (minutes > 0) {
            return minutes + " นาที " + (seconds % 60) + " วินาที";
        } else {
            return seconds + " วินาที";
        }
    }
}

