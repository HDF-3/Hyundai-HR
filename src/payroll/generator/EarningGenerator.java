package payroll.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

import humanresource.dao.EmployeeDAO;
import humanresource.dto.EmployeeDTO;
import payroll.dao.AdditionalAllowanceDAO;
import payroll.dao.SalaryStandardDAO;
import payroll.dto.AdditionalAllowanceDTO;
import payroll.dto.EarningDTO;
import payroll.dto.SalaryStandardDTO;
import attendance.dao.OvertimeDAO;
import attendance.dto.OvertimeDTO;

public class EarningGenerator {

    private static final BigDecimal THIRTY_MINUTE_UNIT = new BigDecimal("2");
    private static final BigDecimal TRANSPORTATION_ALLOWANCE_PER_DAY = new BigDecimal("20000");

    private final SalaryStandardDAO salaryStandardDAO = new SalaryStandardDAO();
    private final OvertimeDAO overtimeDAO = new OvertimeDAO();
    private final AdditionalAllowanceDAO additionalAllowanceDAO = new AdditionalAllowanceDAO();


    public EarningDTO generate(
            Long earningId,
            Long payrollId,
            EmployeeDTO employee,
            YearMonth yearMonth
    ) {
        SalaryStandardDTO salaryStandard = salaryStandardDAO.findSalaryStandard(employee.getPositionId(), employee.getPayGrade());
        OvertimeDTO overtime = overtimeDAO.findOvertime(employee.getEmpId(), yearMonth);

        EarningDTO earning = new EarningDTO();

        earning.setEarningId(earningId);
        earning.setPayrollId(payrollId);
        earning.setBaseSalary(salaryStandard.getBaseSalary());
        earning.setOvertimePay(calculateOvertimePay(salaryStandard.getRegularHourlyRate(), overtime.getOvertimeHours()));
        earning.setTransportationAllowance(calculateTransportationAllowance(overtime.getOvertimeDays()));
        earning.setPerformanceBonus(BigDecimal.ZERO);
        earning.setAdditionalAllowance(calculateAdditionalAllowance(employee.getEmpId(), yearMonth));

        return earning;
    }

    private BigDecimal calculateOvertimePay(BigDecimal regularHourlyRate, float overtimeHours) {
        BigDecimal thirtyMinuteUnits = BigDecimal.valueOf(overtimeHours)
                .multiply(THIRTY_MINUTE_UNIT);

        return thirtyMinuteUnits
                .multiply(regularHourlyRate);
    }

    private BigDecimal calculateTransportationAllowance(Integer overtimeDays) {
        return TRANSPORTATION_ALLOWANCE_PER_DAY
                .multiply(BigDecimal.valueOf(overtimeDays));
    }

    private BigDecimal calculateAdditionalAllowance(Long employeeId, YearMonth yearMonth) {
        BigDecimal total = BigDecimal.ZERO;

        for (AdditionalAllowanceDTO allowance :
                additionalAllowanceDAO.findAdditionalAllowanceList(employeeId, yearMonth)) {
            total = total.add(allowance.getAmount());
        }

        return total;
    }
}