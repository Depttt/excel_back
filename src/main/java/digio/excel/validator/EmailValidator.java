package digio.excel.validator;

public class EmailValidator {

    public static final int MAX_NAME_LENGTH = 50;

    public static String validate(String email){

        if (email == null || !org.apache.commons.validator.routines.EmailValidator.getInstance().isValid(email) || email.length() > MAX_NAME_LENGTH ){
            return  "อีเมลไม่ถูกต้อง";
        }

        return null;
    }
}
