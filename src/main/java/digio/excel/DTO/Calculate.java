package digio.excel.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Calculate {
    @JsonProperty("type")
    private String type;

    @JsonProperty("addend")
    private String addend;

    @JsonProperty("operand")
    private String operand;

    @JsonProperty("result")
    private String result;

    @Override
    public String toString(){
        return "Calculater{" +
                "type='" + type + '\''+
                ", addend='" + addend + '\'' +
                ", operand='" + operand + '\'' +
                ", result='" + result + '\'' +
                "}";
    }
}
