package digio.excel.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConditionServiceTest {

    private ConditionService conditionService;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        conditionService = new ConditionService();

        // Mock Excel Sheet
        Workbook workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Test Sheet");

        // Mock Header Row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("A");
        headerRow.createCell(1).setCellValue("B");

        // Mock Data Row
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("123");
        dataRow.createCell(1).setCellValue("");
    }

    @Test
    void testValidation_ColumnA_HasValue_B_MustHaveValue() {
        // 🔹 ใช้ HashMap แทน Map.of()
        List<Map<String, String>> conditions = new ArrayList<>();
        Map<String, String> condition = new HashMap<>();
        condition.put("columnA", "A");
        condition.put("columnB", "B");
        condition.put("valueA", null);
        condition.put("valueB", null);
        condition.put("enabled", "true");
        conditions.add(condition);

        List<Map<String, Object>> errors = conditionService.validateCondition(sheet, conditions);

        // ✅ ตรวจสอบว่าเกิด Error เพราะ Column B ว่าง
        assertFalse(errors.isEmpty());
        assertEquals("ต้องมีค่า", errors.get(0).get("message"));
    }

    @Test
    void testValidation_ColumnA_HasSpecificValue_B_MustHaveValue() {
        // 🔹 ใช้ HashMap แทน Map.of()
        List<Map<String, String>> conditions = new ArrayList<>();
        Map<String, String> condition = new HashMap<>();
        condition.put("columnA", "A");
        condition.put("columnB", "B");
        condition.put("valueA", "123");
        condition.put("valueB", null);
        condition.put("enabled", "true");
        conditions.add(condition);

        List<Map<String, Object>> errors = conditionService.validateCondition(sheet, conditions);

        // ✅ ตรวจสอบว่าเกิด Error เพราะ Column B ว่าง
        assertFalse(errors.isEmpty());
        assertEquals("ต้องมีค่า", errors.get(0).get("message"));
    }

    @Test
    void testValidation_NoError() {
        // 🔹 เพิ่มค่าที่ Column B แล้วลองตรวจสอบ
        sheet.getRow(1).getCell(1).setCellValue("Some Value");

        List<Map<String, String>> conditions = new ArrayList<>();
        Map<String, String> condition = new HashMap<>();
        condition.put("columnA", "A");
        condition.put("columnB", "B");
        condition.put("valueA", null);
        condition.put("valueB", null);
        condition.put("enabled", "true");
        conditions.add(condition);

        List<Map<String, Object>> errors = conditionService.validateCondition(sheet, conditions);

        // ✅ ไม่มี Error เพราะ Column B มีค่าแล้ว
        assertTrue(errors.isEmpty());
    }
}
