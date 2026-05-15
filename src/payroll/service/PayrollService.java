package payroll.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.YearMonth;
import java.util.List;

import global.types.CommonStatus;
import global.types.DBType;
import global.utils.ConnectionHelper;
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
        Connection conn = null;

        try {
            conn = openTransaction();

            deductionDAO.deleteDeduction(payrollId, conn);
            earningDAO.deleteEarning(payrollId, conn);
            int rowcount = payrollDAO.deletePayroll(payrollId, conn);

            conn.commit();
            return rowcount > 0;

        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Failed to delete payroll.", e);
        } finally {
            ConnectionHelper.close(conn);
        }
    }

    public boolean confirmPayroll(YearMonth yearMonth) {
        return payrollDAO.updatePayrollStatusByMonth(yearMonth, CommonStatus.CONFIRMED) > 0;
    }

    public boolean payPayroll(YearMonth yearMonth) {
        return payrollDAO.updatePayrollStatusByMonth(yearMonth, CommonStatus.PAID) > 0;
    }

    public boolean updateEarning(EarningDTO earning) {
        Connection conn = null;

        try {
            conn = openTransaction();
            int rowcount = earningDAO.updateEarning(earning, conn);

            conn.commit();
            return rowcount > 0;

        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Failed to update earning.", e);
        } finally {
            ConnectionHelper.close(conn);
        }
    }

    public boolean updateDeduction(DeductionDTO deduction) {
        Connection conn = null;

        try {
            conn = openTransaction();
            int rowcount = deductionDAO.updateDeduction(deduction, conn);

            conn.commit();
            return rowcount > 0;

        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Failed to update deduction.", e);
        } finally {
            ConnectionHelper.close(conn);
        }
    }

    public List<AdditionalAllowanceDTO> getAdditionalAllowanceList(Long employeeId, YearMonth yearMonth) {
        return additionalAllowanceDAO.findAdditionalAllowanceList(employeeId, yearMonth);
    }

    public boolean addAdditionalAllowance(AdditionalAllowanceDTO additionalAllowance) {
        Connection conn = null;

        try {
            conn = openTransaction();
            int rowcount = additionalAllowanceDAO.insertAdditionalAllowance(additionalAllowance, conn);

            if (rowcount > 0) {
                refreshAdditionalAllowanceTotal(
                        additionalAllowance.getEmployeeId(),
                        additionalAllowance.getAdditionalAllowanceYearMonth(),
                        conn
                );
            }

            conn.commit();
            return rowcount > 0;

        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Failed to add additional allowance.", e);
        } finally {
            ConnectionHelper.close(conn);
        }
    }

    public boolean updateAdditionalAllowance(AdditionalAllowanceDTO additionalAllowance) {
        Connection conn = null;

        try {
            conn = openTransaction();
            AdditionalAllowanceDTO beforeUpdate = additionalAllowanceDAO.findAdditionalAllowance(
                    additionalAllowance.getAdditionalAllowanceId(),
                    conn
            );
            int rowcount = additionalAllowanceDAO.updateAdditionalAllowance(additionalAllowance, conn);

            if (rowcount > 0) {
                if (beforeUpdate != null) {
                    refreshAdditionalAllowanceTotal(
                            beforeUpdate.getEmployeeId(),
                            beforeUpdate.getAdditionalAllowanceYearMonth(),
                            conn
                    );
                }

                refreshAdditionalAllowanceTotal(
                        additionalAllowance.getEmployeeId(),
                        additionalAllowance.getAdditionalAllowanceYearMonth(),
                        conn
                );
            }

            conn.commit();
            return rowcount > 0;

        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Failed to update additional allowance.", e);
        } finally {
            ConnectionHelper.close(conn);
        }
    }

    public boolean deleteAdditionalAllowance(Long additionalAllowanceId) {
        Connection conn = null;

        try {
            conn = openTransaction();
            AdditionalAllowanceDTO additionalAllowance = additionalAllowanceDAO.findAdditionalAllowance(
                    additionalAllowanceId,
                    conn
            );
            int rowcount = additionalAllowanceDAO.deleteAdditionalAllowance(additionalAllowanceId, conn);

            if (rowcount > 0 && additionalAllowance != null) {
                refreshAdditionalAllowanceTotal(
                        additionalAllowance.getEmployeeId(),
                        additionalAllowance.getAdditionalAllowanceYearMonth(),
                        conn
                );
            }

            conn.commit();
            return rowcount > 0;

        } catch (Exception e) {
            rollback(conn);
            throw new RuntimeException("Failed to delete additional allowance.", e);
        } finally {
            ConnectionHelper.close(conn);
        }
    }

    private void refreshAdditionalAllowanceTotal(Long employeeId, YearMonth yearMonth, Connection conn) throws Exception {
        PayrollDetailDTO payrollDetail = payrollDetailDAO.findPayrollDetail(employeeId, yearMonth, conn);

        if (payrollDetail == null) {
            return;
        }

        BigDecimal additionalAllowanceTotal = BigDecimal.ZERO;

        for (AdditionalAllowanceDTO allowance : additionalAllowanceDAO.findAdditionalAllowanceList(employeeId, yearMonth, conn)) {
            additionalAllowanceTotal = additionalAllowanceTotal.add(allowance.getAmount());
        }

        earningDAO.updateAdditionalAllowance(
                payrollDetail.getPayrollId(),
                additionalAllowanceTotal,
                conn
        );
    }

    private Connection openTransaction() throws Exception {
        Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);

        if (conn == null) {
            throw new IllegalStateException("Database connection is null.");
        }

        conn.setAutoCommit(false);
        return conn;
    }

    private void rollback(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
