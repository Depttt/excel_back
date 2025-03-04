package digio.excel.services;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @InjectMocks
    private TemplateService templateService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    private MultipartFile createExcelFile(String[][] data) throws IOException {
        Workbook workbook = WorkbookFactory.create(true);
        var sheet = workbook.createSheet("Sheet1");

        for (int i = 0; i < data.length; i++) {
            var row = sheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                row.createCell(j).setCellValue(data[i][j]);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new MockMultipartFile("file.xlsx", out.toByteArray());
    }

    @Test
    public void test_uploadEmptyFile(){
        MultipartFile emptyFile = new MockMultipartFile("file.xlsx", new byte[0]);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.handleUploadWithTemplate(emptyFile, List.of(),List.of(), List.of(), List.of())
        );

        assertEquals("ไฟล์ว่างเปล่า ไม่สามารถอ่านข้อมูลได้", thrown.getMessage());
    }

    @Test
    public void test_uploadWithEmptySheet() throws IOException {
        MultipartFile file = createExcelFile(new String[][]{{},{}});

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.handleUploadWithTemplate(file, List.of(), List.of(), List.of(), List.of())
        );

        assertEquals("ไฟล์นี้ไม่มีข้อมูล", thrown.getMessage());
    }

    @Test
    public void test_uploadWithNoErrorsInfo() throws Exception {
        MultipartFile file = createExcelFile(new String[][]{
                {"Jane Doe", "eieieei@gmail.com", "0945672039"},
                {"สมชาย สวัสดี", "test@example.com", "0945672041"}
        });

        List<Map<String, Object>> result = templateService.handleUploadWithTemplate(
                file,
                List.of("ชื่อนามสกุล", "อีเมล", "เบอร์โทรศัพท์"),
                List.of(), List.of(), List.of());

        Map<String, Object> resultSum = result.getFirst();
        String summary = (String) resultSum.get("summary");

        assertNull(summary);
    }

    @Test
    public void test_uploadWithErrorsInfo() throws Exception {
        MultipartFile file = createExcelFile(new String[][]{
                {"Jane Doe", "eieieei@gmail.com", "1112"},
                {"สมชาย", "test@example.com", "0945672041"}
        });

        List<Map<String, Object>> result = templateService.handleUploadWithTemplate(
                file,
                List.of("ชื่อนามสกุล", "อีเมล", "เบอร์โทรศัพท์"),
                List.of(), List.of(), List.of());

        Map<String, Object> resultSum = result.getFirst();
        String summary = (String) resultSum.get("summary");

        assertEquals("Errors found",summary);

        System.out.println(resultSum);
    }

    @Test
    public void test_uploadWithWrongHeaderType() throws IOException {
        MultipartFile file = createExcelFile(new String[][] {
                {"jane doe","jane@example.com","0945672041"},
                {"john doe","john@example.com","0645672032"}
        });

        List<Map<String , Object>> result = templateService.handleUploadWithTemplate(
                file,
                List.of("ชื่อนามสกุล","เบอร์โทรศัพท์","อีเมล"),
                List.of(),List.of(),List.of()
        );

        Map<String,Object> resultSum = result.getFirst();
        String summary = (String) resultSum.get("summary");

        assertEquals("Errors found",summary);
    }

    @Test
    public void test_uploadWithIncompleteInfo() throws IOException {
        MultipartFile file = createExcelFile(new String[][]{
                {"jane doe","jane@example.com","0945672041"},
                {"john doe","john@example.com","0645672032"}
        });

        List<Map<String, Object>> result = templateService.handleUploadWithTemplate(
                file,
                List.of("ชื่อนามสกุล", "อีเมล","เบอร์โทรศัพท์","ที่อยู่"),
                List.of(),List.of(),List.of()
        );

        Map<String, Object> resultSum = result.getFirst();
        String summary = (String) resultSum.get("summary");

        System.out.println(result);
        assertEquals("Errors found",summary);
    }

    @Test
    public void test_uploadWithCondition() throws IOException {
        MultipartFile file = createExcelFile(new String[][]{
                {"50000.0","19000.0","31000.0"},
                {"1000.0","400.0","600.0"}
        });

        List<Map<String, Object>> result = templateService.handleUploadWithTemplate(
                file,
                List.of("balance", "transferAmount","currentBalance"),
                List.of("[-,balance,transferAmount,currentBalance]"),List.of(),List.of()
        );

        assertNotNull(result); // ไม่ null เพราะผลลัพธ์มันออกมาเป็น [{}]
        System.out.println(result);
    }

    @Test
    public void test_uploadWithConditionButWrongHeader() throws IOException {
        MultipartFile file = createExcelFile(new String[][]{
                {"50000.0","19000.0","31000.0"},
                {"1000.0","400.0","600.0"}
        });

        List<Map<String, Object>> result = templateService.handleUploadWithTemplate(
                file,
                List.of("b", "t","c"),
                List.of("[-,balance,transferAmount,currentBalance]"),List.of(),List.of()
        );

        Map<String, Object> resultSum = result.getFirst();
        String summary = (String) resultSum.get("summary");

//        System.out.println(resultSum);
        assertEquals("Errors found",summary);
    }
}