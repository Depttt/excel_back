package digio.excel.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConditionService {

    public List<Map<String, Object>> validateCondition(Sheet sheet, List<Map<String, String>> conditions){
        List<Map<String, Object>> errors = new ArrayList<>();

        for (Row row : sheet) {
            for (Map<String, String> condition : conditions) {
                String columnA = condition.get("columnA");
                String columnB = condition.get("columnB");
                String valueA = condition.get("valueA");
                String valueB = condition.get("valueB");

                Cell columnACell = row.getCell(getColumnIndex(sheet, columnA));
                Cell columnBCell = row.getCell(getColumnIndex(sheet, columnB));

                if (valueA == null && valueB == null) {
                    if (!isCellEmpty(columnACell) && isCellEmpty(columnBCell)) {
                        errors.add(createError(row.getRowNum(), getColumnIndex(sheet, columnB), "ต้องมีค่า"));
                    }
                } else if (valueA == null && valueB != null) {
                    if (!isCellEmpty(columnACell) && !getCellValue(columnBCell).equals(valueB)) {
                        errors.add(createError(row.getRowNum(), getColumnIndex(sheet, columnB), "ค่าต้องเป็น " + valueB));
                    }
                } else if (valueA != null && valueB == null) {
                    if (getCellValue(columnACell).equals(valueA) && isCellEmpty(columnBCell)) {
                        errors.add(createError(row.getRowNum(), getColumnIndex(sheet, columnB), "ต้องมีค่า"));
                    }
                }
            }
        }
        return errors;
    }

    private int getColumnIndex(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0); // ดึงแถวแรก (Header)
        if (headerRow == null) {
            throw new IllegalArgumentException("ไม่พบแถว header");
        }

        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().trim().equalsIgnoreCase(columnName)) {
                return cell.getColumnIndex(); // คืนค่า index ของคอลัมน์ที่เจอ
            }
        }

        throw new IllegalArgumentException("ไม่พบคอลัมน์: " + columnName);
    }

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.toString().trim().isEmpty();
    }

    private String getCellValue(Cell cell) {
        return cell != null ? cell.toString().trim() : "";
    }

    private Map<String, Object> createError(int row, int column, String message) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("row", row);
        errorDetails.put("column", column);
        errorDetails.put("message", message);
        return errorDetails;
    }
}