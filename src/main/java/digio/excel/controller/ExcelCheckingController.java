package digio.excel.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import digio.excel.DTO.Calculate;
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
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelCheckingController {

    @Autowired
    private DynamicCheckingService dynamicCheckingService;

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
        @RequestParam("Condition") List<String> exceptedHeader,
        @RequestParam("calculate") String calculateJson){
        ResponseEntity<?> fileValidation = validateFile(file);
        if ( fileValidation != null) return fileValidation;

        if( exceptedHeader == null || exceptedHeader.isEmpty()){
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "โปรดระบุหัวข้อที่ต้องการในเทมเพลท"));
        }

        try{
            ObjectMapper objectMapper= new ObjectMapper();
            List<Calculate> calculater = objectMapper.readValue(calculateJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Calculate.class));

            List<Map<String, Object>> validationErrors = dynamicCheckingService.handleUploadWithTemplate(file, exceptedHeader, calculater);

            return validationErrors.isEmpty() ?
                    ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ excel ถูกต้อง ไม่มีึข้อผิดพลาด")):
                    ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
        } catch (Exception e){
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
