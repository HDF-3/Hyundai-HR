package payroll.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryStandardDTO {
    private Long salaryStandardId;
    private Long positionId;
    private Integer payGrade;
    private BigDecimal baseSalary;
    private BigDecimal regularHourlyRate;
}
