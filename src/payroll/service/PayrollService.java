package payroll.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import global.types.CommonStatus;
import payroll.dao.AdditionalAllowanceDAO;
import payroll.dao.DeductionDAO;
import payroll.dao.EarningDAO;
import payroll.dao.PayrollDAO;
import payroll.dao.PayrollDetailDAO;
import payroll.dao.PayrollProcedureDAO;
import payroll.dto.AdditionalAllowanceDTO;
import payroll.dto.DeductionDTO;
import payroll.dto.EarningDTO;
import payroll.dto.PayrollDTO;
import payroll.dto.PayrollDetailDTO;

public class PayrollService {

    private final PayrollDAO payrollDAO = new PayrollDAO();
    private final PayrollDetailDAO payrollDetailDAO = new PayrollDetailDAO();
    private final PayrollProcedureDAO payrollProcedureDAO = new PayrollProcedureDAO();
    private final AdditionalAllowanceDAO additionalAllowanceDAO = new AdditionalAllowanceDAO();
    private final EarningDAO earningDAO = new EarningDAO();
    private final DeductionDAO deductionDAO = new DeductionDAO();

    public void createMonthlyPayroll(YearMonth yearMonth) {
        payrollProcedureDAO.callCreateMonthlyPayroll(yearMonth);
    }

    public List<PayrollDTO> getPayrollList(YearMonth yearMonth) {
        return payrollDAO.findPayrollList(yearMonth);
    }

    public List<PayrollDTO> getPayrollList(Long employeeId) {
        return payrollDAO.findPayrollList(employeeId);
    }

    public PayrollDetailDTO getPayrollDetail(Long payrollId) {
        return payrollDetailDAO.findPayrollDetail(payrollId);
    }

    public boolean deletePayroll(Long payrollId) {
        return payrollDAO.deletePayroll(payrollId) > 0;
    }

    public boolean confirmPayroll(YearMonth yearMonth) {
        return payrollDAO.updatePayrollStatusByMonth(yearMonth, CommonStatus.CONFIRMED) > 0;
    }

    public boolean payPayroll(YearMonth yearMonth) {
        return payrollDAO.updatePayrollStatusByMonth(yearMonth, CommonStatus.PAID) > 0;
    }

    public boolean updateEarning(EarningDTO earning) {
        return earningDAO.updateEarning(earning) > 0;
    }

    public boolean updateDeduction(DeductionDTO deduction) {
        return deductionDAO.updateDeduction(deduction) > 0;
    }

    public List<AdditionalAllowanceDTO> getAdditionalAllowanceList(Long employeeId, YearMonth yearMonth) {
        return additionalAllowanceDAO.findAdditionalAllowanceList(employeeId, yearMonth);
    }

    public boolean addAdditionalAllowance(AdditionalAllowanceDTO additionalAllowance) {
        int rowcount = additionalAllowanceDAO.insertAdditionalAllowance(additionalAllowance);

        if (rowcount > 0) {
            refreshAdditionalAllowanceTotal(
                    additionalAllowance.getEmployeeId(),
                    additionalAllowance.getAdditionalAllowanceYearMonth()
            );
        }

        return rowcount > 0;
    }

    public boolean updateAdditionalAllowance(AdditionalAllowanceDTO additionalAllowance) {
        AdditionalAllowanceDTO beforeUpdate = additionalAllowanceDAO.findAdditionalAllowance(
                additionalAllowance.getAdditionalAllowanceId()
        );
        int rowcount = additionalAllowanceDAO.updateAdditionalAllowance(additionalAllowance);

        if (rowcount > 0) {
            if (beforeUpdate != null) {
                refreshAdditionalAllowanceTotal(
                        beforeUpdate.getEmployeeId(),
                        beforeUpdate.getAdditionalAllowanceYearMonth()
                );
            }

            refreshAdditionalAllowanceTotal(
                    additionalAllowance.getEmployeeId(),
                    additionalAllowance.getAdditionalAllowanceYearMonth()
            );
        }

        return rowcount > 0;
    }

    public boolean deleteAdditionalAllowance(Long additionalAllowanceId) {
        AdditionalAllowanceDTO additionalAllowance = additionalAllowanceDAO.findAdditionalAllowance(additionalAllowanceId);
        int rowcount = additionalAllowanceDAO.deleteAdditionalAllowance(additionalAllowanceId);

        if (rowcount > 0 && additionalAllowance != null) {
            refreshAdditionalAllowanceTotal(
                    additionalAllowance.getEmployeeId(),
                    additionalAllowance.getAdditionalAllowanceYearMonth()
            );
        }

        return rowcount > 0;
    }

    private void refreshAdditionalAllowanceTotal(Long employeeId, YearMonth yearMonth) {
        PayrollDetailDTO payrollDetail = payrollDetailDAO.findPayrollDetail(employeeId, yearMonth);

        if (payrollDetail == null) {
            return;
        }

        BigDecimal additionalAllowanceTotal = BigDecimal.ZERO;

        for (AdditionalAllowanceDTO allowance : additionalAllowanceDAO.findAdditionalAllowanceList(employeeId, yearMonth)) {
            additionalAllowanceTotal = additionalAllowanceTotal.add(allowance.getAmount());
        }

        earningDAO.updateAdditionalAllowance(
                payrollDetail.getPayrollId(),
                additionalAllowanceTotal
        );
    }
}
