package payroll.generator;

import java.math.BigDecimal;
import java.time.YearMonth;

import global.types.CommonStatus;
import humanresource.dao.EmployeeDAO;
import humanresource.dto.EmployeeDTO;
import payroll.dto.DeductionDTO;
import payroll.dto.EarningDTO;
import payroll.dto.PayrollDTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class PayrollGenerator {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final EarningGenerator earningGenerator = new EarningGenerator();
    private final DeductionGenerator deductionGenerator = new DeductionGenerator();

    public PayrollGenerateResult generate(
            Long payrollId,
            Long earningId,
            Long deductionId,
            Long employeeId,
            YearMonth yearMonth
    ) {
        EmployeeDTO employee = employeeDAO.selectEmployeeById(employeeId);

        EarningDTO earning = earningGenerator.generate(
                earningId,
                payrollId,
                employee,
                yearMonth
        );

        BigDecimal totalEarnings = calculateTotalEarnings(earning);

        DeductionDTO deduction = deductionGenerator.generate(
                deductionId,
                payrollId,
                yearMonth,
                totalEarnings
        );

        BigDecimal totalDeductions = calculateTotalDeductions(deduction);
        BigDecimal netPay = totalEarnings.subtract(totalDeductions);

        PayrollDTO payroll = new PayrollDTO();

        payroll.setPayrollId(payrollId);
        payroll.setEmployeeId(employeeId);
        payroll.setPayrollYearMonth(yearMonth);
        payroll.setTotalEarnings(totalEarnings);
        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(netPay);
        payroll.setConfirmedAt(null);
        payroll.setPayDate(null);
        payroll.setStatus(CommonStatus.CALCULATED);

        return new PayrollGenerateResult(payroll, earning, deduction);
    }

    private BigDecimal calculateTotalEarnings(EarningDTO earning) {
        return BigDecimal.ZERO
                .add(earning.getBaseSalary())
                .add(earning.getOvertimePay())
                .add(earning.getTransportationAllowance())
                .add(earning.getPerformanceBonus())
                .add(earning.getAdditionalAllowance());
    }

    private BigDecimal calculateTotalDeductions(DeductionDTO deduction) {
        return BigDecimal.ZERO
                .add(deduction.getNationalPension())
                .add(deduction.getHealthInsurance())
                .add(deduction.getLongTermCareInsurance())
                .add(deduction.getEmploymentInsurance())
                .add(deduction.getIncomeTax())
                .add(deduction.getLocalIncomeTax());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PayrollGenerateResult {
        private PayrollDTO payroll;
        private EarningDTO earning;
        private DeductionDTO deduction;
    }
}
