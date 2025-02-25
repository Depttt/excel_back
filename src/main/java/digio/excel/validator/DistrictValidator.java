package digio.excel.validator;

public class DistrictValidator {
    public static String validateDistrict(String district) {
        if (district == null || district.isBlank()){
            return "ชื่ออำเภอไม่ควรว่าง";
        }

        String trimmedDistrict = district.trim();

        if (!trimmedDistrict.matches("^[ก-๙\s]+$")){
            return "รูปแบบอำเภอไม่ถูกต้อง";
        }

        return "success";
    }
}
