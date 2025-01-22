package digio.excel.controller;


import digio.excel.services.DynamicCheckingService;
import digio.excel.services.StaticCheckingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
public class ExcelCheckingController {

    @Autowired
    private DynamicCheckingService dynamicCheckingService;

    @Autowired
    private StaticCheckingService staticCheckingService;

    @PostMapping("/static")
    public ResponseEntity<?> validateExcel(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        try {
            List<String> errors = staticCheckingService.validateAndRejectExcel(file);
            return errors.isEmpty() ?
                    ResponseEntity.ok("ตรวจสอบข้อมูลเรียบร้อย ไม่มีข้อผิดพลาด") :
                    ResponseEntity.badRequest().body(errors);
        } catch (RuntimeException e) {
            return handleException(e);
        }
    }

    @PostMapping("/dynamic")
    public ResponseEntity<?> validateExcelFile(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        try {
            List<String> validationErrors = dynamicCheckingService.validateExcel(file);
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
            List<String> validationErrors = dynamicCheckingService.validateExcelWithSelectedHeaders(file, selectedHeaders);
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
