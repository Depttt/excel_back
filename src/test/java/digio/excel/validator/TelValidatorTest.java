package digio.excel.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelValidatorTest {

    @Test
    void testValidate_validTel(){
        String tel = "0983345267";
        String result = TelValidator.validate(tel);

        assertNull(result);
    }

    @Test
    void testValidate_null(){
        String tel = " ";
        String result = TelValidator.validate(tel);

        assertEquals("หมายเลขโทรศัพท์ไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_DuplicateNum(){
        String tel = "Hello world";
        String result = TelValidator.validate(tel);

        assertEquals("หมายเลขโทรศัพท์ไม่ถูกต้อง",result);
    }

}