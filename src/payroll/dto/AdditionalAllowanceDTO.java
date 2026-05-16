package payroll.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalAllowanceDTO {
    private Long additionalAllowanceId;
    private Long employeeId;
    private String additionalAllowanceName;
    private YearMonth additionalAllowanceYearMonth;
    private BigDecimal amount;
}
