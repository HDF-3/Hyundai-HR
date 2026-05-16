package payroll.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeductionDTO {
    private Long deductionId;
    private Long payrollId;
    private BigDecimal nationalPension;
    private BigDecimal healthInsurance;
    private BigDecimal longTermCareInsurance;
    private BigDecimal employmentInsurance;
    private BigDecimal incomeTax;
    private BigDecimal localIncomeTax;
}
