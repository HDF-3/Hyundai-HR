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
public class PayrollDTO {
	private Long payrollId;
	private Long employeeId;
	private YearMonth payrollYearMonth;
	private BigDecimal totalEarnings;
	private BigDecimal totalDeductions;
	private BigDecimal netPay;
	private LocalDate confirmedAt;
	private LocalDate paidAt;
	private CommonStatus status;
}
