package payroll.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

import global.types.CommonStatus;
import global.types.DBType;
import global.utils.ConnectionHelper;
import payroll.dto.PayrollDetailDTO;

public class PayrollDetailDAO {

    private PayrollDetailDTO mapPayrollDetail(ResultSet rs) throws Exception {
        PayrollDetailDTO detail = new PayrollDetailDTO();

        detail.setPayrollId(rs.getLong("payroll_id"));
        detail.setEmployeeId(rs.getLong("employee_id"));
        detail.setEmployeeName(rs.getString("employee_name"));
        detail.setPayrollYearMonth(YearMonth.from(rs.getDate("payroll_year_month").toLocalDate()));

        detail.setTotalEarnings(rs.getBigDecimal("total_earnings"));
        detail.setTotalDeductions(rs.getBigDecimal("total_deductions"));
        detail.setNetPay(rs.getBigDecimal("net_pay"));
        detail.setStatus(CommonStatus.valueOf(rs.getString("status")));

        Date confirmedAt = rs.getDate("confirmed_at");
        Date payDate = rs.getDate("pay_date");

        if (confirmedAt != null) {
            detail.setConfirmedAt(confirmedAt.toLocalDate());
        }

        if (payDate != null) {
            detail.setPayDate(payDate.toLocalDate());
        }

        detail.setBaseSalary(rs.getBigDecimal("base_salary"));
        detail.setOvertimePay(rs.getBigDecimal("overtime_pay"));
        detail.setTransportationAllowance(rs.getBigDecimal("transportation_allowance"));
        detail.setPerformanceBonus(rs.getBigDecimal("performance_bonus"));
        detail.setAdditionalAllowance(rs.getBigDecimal("additional_allowance"));

        detail.setNationalPension(rs.getBigDecimal("national_pension"));
        detail.setHealthInsurance(rs.getBigDecimal("health_insurance"));
        detail.setLongTermCareInsurance(rs.getBigDecimal("long_term_care_insurance"));
        detail.setEmploymentInsurance(rs.getBigDecimal("employment_insurance"));
        detail.setIncomeTax(rs.getBigDecimal("income_tax"));
        detail.setLocalIncomeTax(rs.getBigDecimal("local_income_tax"));

        return detail;
    }

    public PayrollDetailDTO findPayrollDetail(Long payrollId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PayrollDetailDTO detail = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);

            String sql =
                    "select " +
                    "p.payroll_id, p.employee_id, e.name as employee_name, " +
                    "p.payroll_year_month, p.total_earnings, p.total_deductions, p.net_pay, " +
                    "p.status, p.confirmed_at, p.pay_date, " +
                    "er.base_salary, er.overtime_pay, er.transportation_allowance, " +
                    "er.performance_bonus, er.additional_allowance, " +
                    "d.national_pension, d.health_insurance, d.long_term_care_insurance, " +
                    "d.employment_insurance, d.income_tax, d.local_income_tax " +
                    "from payroll p " +
                    "join employee e on p.employee_id = e.emp_id " +
                    "left join earning er on p.payroll_id = er.payroll_id " +
                    "left join deduction d on p.payroll_id = d.payroll_id " +
                    "where p.payroll_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, payrollId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                detail = mapPayrollDetail(rs);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return detail;
    }
    
    public PayrollDetailDTO findPayrollDetail(Long employeeId, YearMonth yearMonth) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PayrollDetailDTO detail = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);

            String sql =
                    "select " +
                    "p.payroll_id, p.employee_id, e.name as employee_name, " +
                    "p.payroll_year_month, p.total_earnings, p.total_deductions, p.net_pay, " +
                    "p.status, p.confirmed_at, p.pay_date, " +
                    "er.base_salary, er.overtime_pay, er.transportation_allowance, " +
                    "er.performance_bonus, er.additional_allowance, " +
                    "d.national_pension, d.health_insurance, d.long_term_care_insurance, " +
                    "d.employment_insurance, d.income_tax, d.local_income_tax " +
                    "from payroll p " +
                    "join employee e on p.employee_id = e.emp_id " +
                    "left join earning er on p.payroll_id = er.payroll_id " +
                    "left join deduction d on p.payroll_id = d.payroll_id " +
                    "where p.employee_id = ? " +
                    "and p.payroll_year_month = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, employeeId);
            pstmt.setDate(2, Date.valueOf(yearMonth.atDay(1)));
            rs = pstmt.executeQuery();

            if (rs.next()) {
                detail = mapPayrollDetail(rs);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return detail;
    }

    public PayrollDetailDTO findPayrollDetail(Long employeeId, YearMonth yearMonth, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PayrollDetailDTO detail = null;

        try {
            String sql =
                    "select " +
                    "p.payroll_id, p.employee_id, e.name as employee_name, " +
                    "p.payroll_year_month, p.total_earnings, p.total_deductions, p.net_pay, " +
                    "p.status, p.confirmed_at, p.pay_date, " +
                    "er.base_salary, er.overtime_pay, er.transportation_allowance, " +
                    "er.performance_bonus, er.additional_allowance, " +
                    "d.national_pension, d.health_insurance, d.long_term_care_insurance, " +
                    "d.employment_insurance, d.income_tax, d.local_income_tax " +
                    "from payroll p " +
                    "join employee e on p.employee_id = e.emp_id " +
                    "left join earning er on p.payroll_id = er.payroll_id " +
                    "left join deduction d on p.payroll_id = d.payroll_id " +
                    "where p.employee_id = ? " +
                    "and p.payroll_year_month = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, employeeId);
            pstmt.setDate(2, Date.valueOf(yearMonth.atDay(1)));
            rs = pstmt.executeQuery();

            if (rs.next()) {
                detail = mapPayrollDetail(rs);
            }

            return detail;

        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
        }
    }

}
