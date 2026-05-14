package payroll.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EarningDTO {
    private Long earningId;
    private Long payrollId;
    private BigDecimal baseSalary;
    private BigDecimal overtimePay;
    private BigDecimal transportationAllowance;
    private BigDecimal performanceBonus;
    private BigDecimal additionalAllowance;
}
