package payroll.service;

import java.time.YearMonth;

import payroll.dao.PayrollProcedureDAO;

public class PayrollService {

    private final PayrollProcedureDAO payrollProcedureDAO = new PayrollProcedureDAO();

    public void createMonthlyPayroll(YearMonth targetMonth) {
        payrollProcedureDAO.callCreateMonthlyPayroll(targetMonth);
    }
}
