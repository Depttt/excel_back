package digio.excel.controller;


import digio.excel.services.DynamicCheckingService;
import digio.excel.services.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelCheckingController {

    @Autowired
    private DynamicCheckingService dynamicCheckingService;

    @Autowired
    private TemplateService templateService;

    @PostMapping("/dynamic")
    public ResponseEntity<?> validateExcelFile(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        try {
            List<Map<String, Object>> validationErrors = dynamicCheckingService.validateExcel(file);
            return validationErrors.isEmpty() ?
                    ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")) :
                    ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/headers")
    public ResponseEntity<?> validateExcelWithSelectedHeaders(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "headers", required = false) List<String> selectedHeaders) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        if (selectedHeaders == null || selectedHeaders.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "โปรดระบุหัวข้อที่ต้องการตรวจสอบ"));
        }

        try {
            List<Map<String, Object>> validationErrors = dynamicCheckingService.validateExcelWithSelectedHeaders(file, selectedHeaders);
            return validationErrors.isEmpty() ?
                    ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")) :
                    ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/template")
    public ResponseEntity<?> handleUploadWithTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("condition") List<String> expectedHeaders,
            @RequestParam("calculator") List<String> calculator) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        if (expectedHeaders == null || expectedHeaders.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "โปรดระบุหัวข้อที่ต้องการตรวจสอบในเทมเพลต"));
        }

        try {
            List<Map<String, Object>> validationErrors = templateService.handleUploadWithTemplate(file, expectedHeaders, calculator);

            return validationErrors.isEmpty() ?
                    ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")) :
                    ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private ResponseEntity<?> validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "โปรดอัปโหลดไฟล์ Excel ที่ถูกต้อง"));
        }
        return null;
    }

    private ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(500).body(Collections.singletonMap("message", "เกิดข้อผิดพลาด: " + e.getMessage()));
    }
}
