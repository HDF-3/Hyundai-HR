package payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import global.types.CommonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDetailDTO {
    private Long payrollId;
    private Long employeeId;
    private String employeeName;
    private YearMonth payrollYearMonth;

    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private CommonStatus status;
    private LocalDate confirmedAt;
    private LocalDate paidAt;

    private BigDecimal baseSalary;
    private BigDecimal overtimePay;
    private BigDecimal transportationAllowance;
    private BigDecimal performanceBonus;
    private BigDecimal additionalAllowance;

    private BigDecimal nationalPension;
    private BigDecimal healthInsurance;
    private BigDecimal longTermCareInsurance;
    private BigDecimal employmentInsurance;
    private BigDecimal incomeTax;
    private BigDecimal localIncomeTax;
}
