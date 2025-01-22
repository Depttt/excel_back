package digio.excel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import digio.excel.services.DynamicCheckingService;
import digio.excel.services.StaticCheckingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest (ExcelCheckingController.class)
class ExcelCheckingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DynamicCheckingService dynamicCheckingService;

    @MockitoBean
    private StaticCheckingService staticCheckingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void test_emptyExcelFile() throws Exception{
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/vnd.ms-excel", new byte[0]);

        mockMvc.perform(multipart("/api/excel/static")
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "โปรดอัปโหลดไฟล์ Excel ที่ถูกต้อง")
                )));

    }

    @Test
    public void test_staticNoErrors() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        Mockito.when(staticCheckingService.validateAndRejectExcel(validFile)).thenReturn(Collections.emptyList());

        mockMvc.perform(multipart("/api/excel/static")
                        .file(validFile))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) content().string("ตรวจสอบข้อมูลเรียบร้อย ไม่มีข้อผิดพลาด"));
    }

    @Test
    public void test_staticWithErrors() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        Mockito.when(staticCheckingService.validateAndRejectExcel(invalidFile))
                .thenReturn(List.of("Error 1", "Error 2"));

        mockMvc.perform(multipart("/api/excel/static")
                        .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of("Error 1", "Error 2"))));
    }

    @Test
    public void test_DynamicWithNoErrors() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        Mockito.when(dynamicCheckingService.validateExcel(validFile)).thenReturn(Collections.emptyList());

        mockMvc.perform(multipart("/api/excel/dynamic")
                        .file(validFile))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")
                )));
    }

    @Test
    public void test_headerPartButNoHeadersProvided() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        mockMvc.perform(multipart("/api/excel/headers")
                        .file(validFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "โปรดระบุหัวข้อที่ต้องการตรวจสอบ")
                )));
    }

}