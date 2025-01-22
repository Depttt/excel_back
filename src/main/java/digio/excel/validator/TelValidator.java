package digio.excel.validator;

public class TelValidator {

    public static String validate(String tel){
        if (tel == null || !tel.matches("^0[1-9][0-9]{8}$")) {
            return "หมายเลขโทรศัพท์ไม่ถูกต้อง";
        }
        return null;
    }
}
