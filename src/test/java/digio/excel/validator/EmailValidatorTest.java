package digio.excel.validator;

import jakarta.validation.constraints.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @Test
    void testValidate_ValidEmail(){
        String email = "9hldr@gmail.com";
        String result = EmailValidator.validate(email);

        assertNull(result);
    }

    @Test
    void testValidate_NullEmail(){
        String email = " ";
        String result = EmailValidator.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_OverLength(){
        String email = "ksdaksdasdaopsdasdoaskdaodsdsadksaodsadkwedmdkfosfksldks;dfoefpafaflkefle@hotmail.com";
        String result = EmailValidator.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_validDomain(){
        String email = "dospdkesd@araiwa.ac.th";
        String result = EmailValidator.validate(email);

        assertNull(result);
    }

    @Test
    void testValidate_noAt(){
        String email = "examplemail.com";
        String result = EmailValidator.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_noDomainName(){
        String email = "example@.com";
        String result = EmailValidator.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง",result);
    }

    @Test
    void testValidate_specialAlphabet(){
        String email = "user@domain$.com";
        String result = EmailValidator.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง",result);
    }

}