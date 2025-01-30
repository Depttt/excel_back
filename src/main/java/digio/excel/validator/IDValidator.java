package digio.excel.validator;

public class IDValidator {

    public static String validate(String citizenId) {
        if (citizenId == null || !citizenId.matches("^\\d{13}$")) {
            return "บัตรประชาชนไม่ถูกต้อง";
        }

        return "success";
    }

    private static boolean isValidCitizenId(String citizenId) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(citizenId.charAt(i));
            sum += digit * (13 - i);
        }
        int checkDigit = (11 - (sum % 11)) % 10;
        int lastDigit = Character.getNumericValue(citizenId.charAt(12));
        return checkDigit == lastDigit;
    }
}
