package payroll.batch;

import java.time.YearMonth;

import payroll.service.PayrollService;

public class MonthlyPayrollBatch {

    private final PayrollService payrollService = new PayrollService();

    public void run() {
        payrollService.createMonthlyPayroll(getTargetMonth(););
    }

    private YearMonth getTargetMonth() {
        return YearMonth.now().minusMonths(1);
    }
}
