package digio.excel.services;

import digio.excel.services.StaticCheckingService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WebMvcTest(StaticCheckingService.class)
public class StaticCheckingServiceTest {

    private final StaticCheckingService staticCheckingService = new StaticCheckingService();

    @Test
    public void testValidateAndRejectExcel_NoErrors() throws Exception {
        MockMultipartFile validFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล", "บัตรประชาชน", "ที่อยู่", "หมายเลขโทรศัพท์"},
                {"สมชาย ใจดี", "example@test.com", "1234567890123", "123 Main St", "0812345678"}
        });

        List<String> errors = staticCheckingService.validateAndRejectExcel(validFile);
        assertEquals(0, errors.size(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test
    public void testValidateAndRejectExcel_WithErrors() throws Exception {
        MockMultipartFile invalidFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล", "บัตรประชาชน", "ที่อยู่", "หมายเลขโทรศัพท์"},
                {"สมชาย123", "invalid-email", "123", "<address>", "12345"}
        });

        List<String> errors = staticCheckingService.validateAndRejectExcel(invalidFile);
        assertEquals(1, errors.size(), "ควรมี 1 แถวที่มีข้อผิดพลาด");
        assertEquals("แถวที่ 2: ชื่อไม่ถูกต้อง, อีเมลไม่ถูกต้อง, บัตรประชาชนไม่ถูกต้อง, ที่อยู่ไม่ถูกต้อง, หมายเลขโทรศัพท์ไม่ถูกต้อง", errors.get(0));
    }

    @Test
    public void testValidateAndRejectExcel_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                staticCheckingService.validateAndRejectExcel(emptyFile));
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
