package digio.excel.validator;

import org.apache.commons.math3.analysis.function.Add;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressValidatorTest {

    @Test
    void testValidate_ValidAddress(){
        String address = "123 ถนนสุขุมวิท กรุงเทพ";
        String result = AddressValidator.validate(address);

        assertNull(result);
    }

    @Test
    void testValidate_NullAddress(){
        String address = " ";
        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ไม่ควรว่าง", result);
    }

    @Test
    void testValidate_OverLength(){
        String address = "บ้านเลขที่ 123 หมู่ที่ 456 แขวงบางนา เขตบางนา กรุงเทพมหานคร ประเทศไทย" +
                        "1234567890123456789012345678901234567890";
        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ยาวเกินไป", result);
    }

    @Test
    void testValidate_SpecialAlphabet(){
        String address = " 232 l@mpinyy 10982 Ka$etsart_ ";
        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ไม่ควรมีอักขระพิเศษ", result);
    }

    @Test
    void testValidate_MinLength(){
        String address = " บ้านฉัน ";
        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่สั้นเกินไป", result);
    }

    @Test
    void testValidate_Duplicate(){
        String address = " 22222222222222222 bbbbbbbbbbbbbbbbbbbangna ";
        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ไม่ควรมีตัวอักษรหรือตัวเลขซ้ำกัน",result);
    }

    @Test
    void testValidate_UnknowAddress(){
        String address = "n/a";
        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ไม่ถูกต้อง",result);
    }

}