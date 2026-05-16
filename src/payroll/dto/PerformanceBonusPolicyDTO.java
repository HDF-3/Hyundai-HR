package payroll.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceBonusPolicyDTO {
    private Long performanceBonusPolicyId;
    private Integer evalYear;
    private Integer evalQuarter;
    private String grade;
    private BigDecimal bonusRate;
    private BigDecimal fixedAmount;
}
