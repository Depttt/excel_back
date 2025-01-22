package digio.excel.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDValidatorTest {

    @Test
    void testValidate_validId(){
        String id = "1055030338215";
        String result = IDValidator.validate(id);

        assertNull(result);
    }

    @Test
    void testValidate_null(){
        String id = " ";
        String result = IDValidator.validate(id);

        assertEquals("บัตรประชาชนไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_notId(){
        String id = "Hello world";
        String result = IDValidator.validate(id);

        assertEquals("บัตรประชาชนไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_randomNum(){
        String id = "123456789012";
        String result = IDValidator.validate(id);

        assertEquals("บัตรประชาชนไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_duplicateNum(){
        String id = "1111111111119";
        String result = IDValidator.validate(id);

        assertEquals("บัตรประชาชนไม่ถูกต้อง",result); //มันถูกตามหลักคำนวณ แต่ความเป็นจริงไม่น่ามีเด้อ
    }

    @Test
    void testValidate_minNum(){
        String id = "345";
        String result = IDValidator.validate(id);

        assertEquals("บัตรประชาชนไม่ถูกต้อง",result);
    }

}