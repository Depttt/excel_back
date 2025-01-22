package digio.excel.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameValidatorTest {

    @Test
    void testValidate_validName(){
        assertNull(NameValidator.validate("John Doe"));
        assertNull( NameValidator.validate("สมชาย ใจดี"));
    }

    @Test
    void testValidate_nullName(){
        assertEquals("ชื่อไม่ควรว่าง", NameValidator.validate("     "));
    }

    @Test
    void testValidate_keyword(){
        assertEquals("ชื่อไม่ถูกต้อง", NameValidator.validate("unknown user"));
        assertEquals("ชื่อไม่ถูกต้อง", NameValidator.validate("invalid entry"));
        assertEquals("ชื่อไม่ถูกต้อง", NameValidator.validate("not specified"));
    }

    @Test
    void testValidate_email(){
        assertEquals("ชื่อไม่ควรเป็นอีเมล", NameValidator.validate("example@test.com"));
    }

    @Test
    void testValidate_phoneNumber(){
        assertEquals("ชื่อไม่ควรเป็นหมายเลขโทรศัพท์", NameValidator.validate("1234567890"));
    }

    @Test
    void testValidate_specialChar(){
        assertEquals("ชื่อไม่ควรมีอักขระพิเศษ", NameValidator.validate("John@Doe"));
    }

    @Test
    void testValidate_number(){
        assertEquals("ชื่อไม่ควรมีตัวเลข", NameValidator.validate("John123"));
    }

    @Test
    void testValidate_doubleSpace(){
        assertEquals("ชื่อไม่ควรมีช่องว่างซ้ำ", NameValidator.validate("John  Doe"));
    }

    @Test
    void testValidate_InvalidChar(){
        assertEquals("ชื่อไม่ควรมีอักขระพิเศษ", NameValidator.validate("John_!"));
    }

    @Test
    void testValidate_minName(){
        assertEquals("ชื่อควรมีความยาวอย่างน้อย 2 ตัวอักษร", NameValidator.validate("J Doe"));
    }

    @Test
    void testValidate_minLastName(){
        assertEquals("นามสกุลควรมีความยาวอย่างน้อย 2 ตัวอักษร", NameValidator.validate("Jane D"));
    }

    @Test
    void testValidate_maxName(){
        assertEquals("ชื่อยาวเกินไป", NameValidator.validate("John".repeat(20) + " Doe"));
    }

    @Test
    void testValidate_maxLastName(){
        assertEquals("นามสกุลยาวเกินไป", NameValidator.validate("John " + "Doe".repeat(20)));
    }
}