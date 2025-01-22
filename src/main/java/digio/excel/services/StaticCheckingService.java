package digio.excel.services;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Service
public class StaticCheckingService {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_ADDRESS_LENGTH = 100;
//    private static final int PHONE_NUMBER_LENGTH = 10;
    private static final int CITIZEN_ID_LENGTH = 13;

    public List<String> validateAndRejectExcel(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้");
        }

        Map<Integer, String> errorMap = new ConcurrentSkipListMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.forEach(row -> {
                if (row.getRowNum() > 0) {
                    String errors = validateRow(row);
                    if (!errors.isEmpty()) {
                        errorMap.put(row.getRowNum() + 1, errors);
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }

        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());
    }

    private String validateRow(Row row) {
        StringBuilder errorBuilder = new StringBuilder();

        validateField(getCellValue(row.getCell(0)), this::validateName, "ชื่อไม่ถูกต้อง", errorBuilder);
        validateField(getCellValue(row.getCell(1)), this::validateEmail, "อีเมลไม่ถูกต้อง", errorBuilder);
        validateField(getCellValue(row.getCell(2)), this::validateCitizenId, "บัตรประชาชนไม่ถูกต้อง", errorBuilder);
        validateField(getCellValue(row.getCell(3)), this::validateAddress, "ที่อยู่ไม่ถูกต้อง", errorBuilder);
        validateField(getCellValue(row.getCell(4)), this::validatePhoneNum, "หมายเลขโทรศัพท์ไม่ถูกต้อง", errorBuilder);

        return errorBuilder.toString();
    }

    private void validateField(String value, Validator validator, String errorMsg, StringBuilder errorBuilder) {
        if (!validator.validate(value)) {
            appendError(errorBuilder, errorMsg);
        }
    }

    private boolean validateName(String name) {
        return name != null && name.length() >= 2 && name.length() <= MAX_NAME_LENGTH
                && !name.matches(".*\\d.*|.*[!@#$%^&*(),.?\":{}|<>].*|\\s{2,}")
                && name.matches("^[ก-๙A-Za-z\\s]+$");
    }

    private boolean validateEmail(String email) {
        return email != null && EmailValidator.getInstance().isValid(email) && email.length() <= MAX_NAME_LENGTH;
    }

    private boolean validateCitizenId(String citizenId) {
        return citizenId != null && citizenId.matches("^\\d{" + CITIZEN_ID_LENGTH + "}$");
    }

    private boolean validateAddress(String address) {
        return address != null && address.length() <= MAX_ADDRESS_LENGTH && !address.matches(".*[<>#&@].*");
    }

    private boolean validatePhoneNum(String phoneNum) {
        return phoneNum != null && phoneNum.matches("^0[1-9][0-9]{8}$");
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return null;
        }
    }

    private void appendError(StringBuilder errorBuilder, String message) {
        if (errorBuilder.length() > 0) {
            errorBuilder.append(", ");
        }
        errorBuilder.append(message);
    }

    @FunctionalInterface
    private interface Validator {
        boolean validate(String value);
    }
}