package digio.excel.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DynamicCheckingServiceTest {

    private final DynamicCheckingService dynamicCheckingService = new DynamicCheckingService();

    @Test
    public void testValidateExcel_NoErrors() throws Exception {
        MockMultipartFile validFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล", "บัตรประชาชน", "เบอร์โทร", "ที่อยู่"},
                {"สมชาย ใจดี", "example@test.com", "1234567890123", "0812345678", "123 Main St"}
        });

        List<String> errors = dynamicCheckingService.validateExcel(validFile);
        assertEquals(0, errors.size(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test
    public void testValidateExcel_WithErrors() throws Exception {
        MockMultipartFile invalidFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล", "บัตรประชาชน", "เบอร์โทร", "ที่อยู่"},
                {"สมชาย123", "invalid-email", "123", "12345", "<invalid>"}
        });

        List<String> errors = dynamicCheckingService.validateExcel(invalidFile);
        assertEquals(1, errors.size(), "ควรมีข้อผิดพลาดในแถวที่ 2");
        assertEquals("แถวที่ 2: ชื่อไม่ควรมีตัวเลข, อีเมลไม่ถูกต้อง, บัตรประชาชนไม่ถูกต้อง, หมายเลขโทรศัพท์ไม่ถูกต้อง, ที่อยู่ไม่ถูกต้อง", errors.get(0));
    }

    @Test
    public void testValidateExcel_NoHeaders() throws Exception {
        MockMultipartFile noHeaderFile = createExcelFile(new String[][]{});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dynamicCheckingService.validateExcel(noHeaderFile));
        assertEquals("ไม่มีแถวหัวข้อในไฟล์ Excel", exception.getMessage());
    }

    @Test
    public void testValidateExcelWithSelectedHeaders_NotFound() throws Exception {
        MockMultipartFile validFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล"},
                {"สมชาย ใจดี", "example@test.com"}
        });

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dynamicCheckingService.validateExcelWithSelectedHeaders(validFile, List.of("เบอร์โทร")));
        assertEquals("ไม่พบหัวข้อที่เลือกในไฟล์ Excel", exception.getMessage());
    }

    @Test
    public void testValidateExcel_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dynamicCheckingService.validateExcel(emptyFile));
        assertEquals("ไม่สามารถอ่านไฟล์ Excel ได้", exception.getMessage());
    }

    private MockMultipartFile createExcelFile(String[][] data) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data[i][j]);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream(outputStream.toByteArray()));
    }
}