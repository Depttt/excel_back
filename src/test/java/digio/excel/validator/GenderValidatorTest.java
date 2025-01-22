package digio.excel.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenderValidatorTest {

    @Test
    void test_validGender(){
        assertNull(GenderValidator.validateGender("ชาย"));
        assertNull(GenderValidator.validateGender("หญิง"));
        assertNull(GenderValidator.validateGender("ไม่ระบุ"));
    }

    @Test
    void test_extraSpace(){
        assertNull(GenderValidator.validateGender(" ชาย "));
        assertNull(GenderValidator.validateGender(" หญิง "));
        assertNull(GenderValidator.validateGender(" ไม่ระบุ "));
    }

    @Test
    void test_null(){
        assertEquals("เพศไม่ควรว่าง", GenderValidator.validateGender("  "));
    }

    @Test
    void test_invalid(){
        assertEquals("เพศไม่ถูกต้อง", GenderValidator.validateGender("อื่น ๆ"));
        assertEquals("เพศไม่ถูกต้อง", GenderValidator.validateGender("male"));
        assertEquals("เพศไม่ถูกต้อง", GenderValidator.validateGender("123"));
    }

}