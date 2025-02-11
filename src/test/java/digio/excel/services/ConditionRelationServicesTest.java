package digio.excel.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConditionRelationServicesTest {

    private ConditionRelationServices conditionRelationServices;
    private ObjectMapper objectMapper;
    private Workbook workbook;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        conditionRelationServices = new ConditionRelationServices();
        objectMapper = new ObjectMapper();

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("TestSheet");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("A");
        headerRow.createCell(1).setCellValue("B");

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("XXX");
        dataRow.createCell(1).setCellValue("YYY");
    }

    @Test
    void testGetColumnIndex() {
        int indexA = conditionRelationServices.getColumnIndex(sheet, "A");
        int indexB = conditionRelationServices.getColumnIndex(sheet, "B");

        assertEquals(0, indexA);
        assertEquals(1, indexB);
    }

    @Test // Con01 : A มีค่า B ต้องมีค่า
    void testValidateColumnRelations_T101() {
        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "", "valueB", "")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con01 : มีค่า B ต้องมีค่า ( A เป็น null )
    void testValidateColumnRelations_T102() {
        sheet.getRow(1).getCell(0).setCellValue("");
        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "", "valueB", "")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con01 : มีค่า B ต้องมีค่า ( B เป็น null )
    void testValidateColumnRelations_F101() {
        sheet.getRow(1).getCell(1).setCellValue("");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "", "valueB", "")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);

        assertFalse(errors.isEmpty(), "ควรมีข้อผิดพลาด");
        assertEquals(1, errors.size());
        assertEquals("ค่าของ A ไม่ตรงกับเงื่อนไขของ B", errors.get(0).get("message"));
    }

    @Test // Con02 : A มีค่าที่กำหนด B ต้องมีค่า
    void testValidateColumnRelations_T201() {
        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con02 : A มีค่าที่กำหนด B ต้องมีค่า ( A เป็น null )
    void testValidateColumnRelations_T202() {
        sheet.getRow(1).getCell(0).setCellValue("");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con02 : A มีค่าที่กำหนด B ต้องมีค่า ( A ไม่ใช่ค่าที่กำหนด, B เป็น null )
    void testValidateColumnRelations_T203() {
        sheet.getRow(1).getCell(1).setCellValue("sad");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con02 : A มีค่าที่กำหนด B ต้องมีค่า ( B เป็น null )
    void testValidateColumnRelations_F201() {
        sheet.getRow(1).getCell(1).setCellValue("");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertFalse(errors.isEmpty(), "ควรมีข้อผิดพลาด");
        assertEquals(1, errors.size());
        assertEquals("ค่าของ A ไม่ตรงกับเงื่อนไขของ B", errors.get(0).get("message"));
    }

    @Test // Con03 : A มีค่า B ต้องมีค่าตามที่กำหนด
    void testValidateColumnRelations_T301() {
        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con03 : A มีค่า B ต้องมีค่าตามที่กำหนด ( A เป็น null )
    void testValidateColumnRelations_T302() {
        sheet.getRow(1).getCell(0).setCellValue("");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con03 : A มีค่า B ต้องมีค่าตามที่กำหนด ( B เป็น null )
    void testValidateColumnRelations_F301() {
        sheet.getRow(1).getCell(1).setCellValue("");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertFalse(errors.isEmpty(), "ควรมีข้อผิดพลาด");
        assertEquals(1, errors.size());
        assertEquals("ค่าของ A ไม่ตรงกับเงื่อนไขของ B", errors.get(0).get("message"));
    }

    @Test // Con03 : A มีค่า B ต้องมีค่าตามที่กำหนด ( B ค่าไม่ตรงกับที่กำหนด )
    void testValidateColumnRelations_F302() {
        sheet.getRow(1).getCell(1).setCellValue("ggd");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertFalse(errors.isEmpty(), "ควรมีข้อผิดพลาด");
        assertEquals(1, errors.size());
        assertEquals("ค่าของ A ไม่ตรงกับเงื่อนไขของ B", errors.get(0).get("message"));
    }

    @Test // Con04 : A มีค่าที่กำหนด B ต้องมีค่าตามที่กำหนด
    void testValidateColumnRelations_T401() {
        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con04 : A มีค่าที่กำหนด B ต้องมีค่าตามที่กำหนด ( A ไม่ใช่ค่าที่กำหนด )
    void testValidateColumnRelations_T402() {
        sheet.getRow(1).getCell(0).setCellValue("asd");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con04 : A มีค่าที่กำหนด B ต้องมีค่าตามที่กำหนด ( A เป็น null )
    void testValidateColumnRelations_T403() {
        sheet.getRow(1).getCell(0).setCellValue("");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertTrue(errors.isEmpty(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test // Con04 : A มีค่าที่กำหนด B ต้องมีค่าตามที่กำหนด ( B เป็น null )
    void testValidateColumnRelations_F401() {
        sheet.getRow(1).getCell(1).setCellValue("");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertFalse(errors.isEmpty(), "ควรมีข้อผิดพลาด");
        assertEquals(1, errors.size());
        assertEquals("ค่าของ A ไม่ตรงกับเงื่อนไขของ B", errors.get(0).get("message"));
    }

    @Test // Con04 : A มีค่าที่กำหนด B ต้องมีค่าตามที่กำหนด ( B ไม่ใช่ค่าที่กำหนด )
    void testValidateColumnRelations_F402() {
        sheet.getRow(1).getCell(1).setCellValue("ghd");

        List<Map<String, String>> rules = List.of(
                Map.of("columnA", "A", "columnB", "B", "valueA", "XXX", "valueB", "YYY")
        );

        List<Map<String, Object>> errors = conditionRelationServices.validateColumnRelations(sheet, rules);
        assertFalse(errors.isEmpty(), "ควรมีข้อผิดพลาด");
        assertEquals(1, errors.size());
        assertEquals("ค่าของ A ไม่ตรงกับเงื่อนไขของ B", errors.get(0).get("message"));
    }

}