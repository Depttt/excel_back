package digio.excel.validator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class AgeValidatorTest {

    @Test
    void test_validDate(){
        LocalDate validDate = LocalDate.now().minusYears(18);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        assertNull(AgeValidator.validateDateOfBirth(validDate.format(formatter)));
    }

    @Test
    void test_validDateButALtFormat(){
        LocalDate validDate = LocalDate.now().minusYears(18);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        assertNull(AgeValidator.validateDateOfBirth(validDate.format(formatter)));
    }

    @Test
    void test_null(){
        assertEquals("วันเกิดไม่ควรว่าง", AgeValidator.validateDateOfBirth("  "));
    }

    @Test
    void test_invalidFormat() {
        assertEquals("รูปแบบวันเกิดไม่ถูกต้อง", AgeValidator.validateDateOfBirth("32-13-2020")); // Invalid date
        assertEquals("รูปแบบวันเกิดไม่ถูกต้อง", AgeValidator.validateDateOfBirth("2020/12/31")); // Unsupported format
    }

    @Test
    void test_futureDate(){
        LocalDate futureDate = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        assertEquals("วันเกิดไม่สามารถเป็นวันที่ในอนาคต", AgeValidator.validateDateOfBirth(futureDate.format(formatter)));
    }

    @Test
    void test_underAge(){
        LocalDate under18Date = LocalDate.now().minusYears(17).minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        assertEquals("อายุไม่ถึงขั้นต่ำที่กำหนด (ต้องมีอายุอย่างน้อย 18 ปี)", AgeValidator.validateDateOfBirth(under18Date.format(formatter)));
    }

}