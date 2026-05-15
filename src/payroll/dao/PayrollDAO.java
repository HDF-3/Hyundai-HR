package payroll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import global.types.CommonStatus;
import global.types.DBType;
import global.utils.ConnectionHelper;
import payroll.dto.PayrollDTO;

public class PayrollDAO {

    private PayrollDTO mapPayroll(ResultSet rs) throws Exception {
        PayrollDTO payroll = new PayrollDTO();

        payroll.setPayrollId(rs.getLong("payroll_id"));
        payroll.setEmployeeId(rs.getLong("employee_id"));
        payroll.setPayrollYearMonth(YearMonth.from(rs.getDate("payroll_year_month").toLocalDate()));
        payroll.setTotalEarnings(rs.getBigDecimal("total_earnings"));
        payroll.setTotalDeductions(rs.getBigDecimal("total_deductions"));
        payroll.setNetPay(rs.getBigDecimal("net_pay"));

        Date confirmedAt = rs.getDate("confirmed_at");
        Date payDate = rs.getDate("pay_date");

        payroll.setConfirmedAt(confirmedAt == null ? null : confirmedAt.toLocalDate());
        payroll.setPayDate(payDate == null ? null : payDate.toLocalDate());
        payroll.setStatus(CommonStatus.valueOf(rs.getString("status")));

        return payroll;
    }

    private void setNullableDate(PreparedStatement pstmt, int parameterIndex, LocalDate date) throws SQLException {
        if (date == null) {
            pstmt.setNull(parameterIndex, java.sql.Types.DATE);
            return;
        }

        pstmt.setDate(parameterIndex, Date.valueOf(date));
    }

   
    public PayrollDTO findPayroll(Long payrollId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PayrollDTO payroll = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select payroll_id, employee_id, payroll_year_month, total_earnings, total_deductions, net_pay, confirmed_at, pay_date, status from payroll where payroll_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, payrollId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                payroll = mapPayroll(rs);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return payroll;
    }

    public List<PayrollDTO> findPayrollList(YearMonth payrollYearMonth) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PayrollDTO> payrollList = new ArrayList<PayrollDTO>();

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select payroll_id, employee_id, payroll_year_month, total_earnings, total_deductions, net_pay, confirmed_at, pay_date, status from payroll where payroll_year_month=? order by employee_id";

            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(payrollYearMonth.atDay(1)));
            rs = pstmt.executeQuery();

            while (rs.next()) {
                payrollList.add(mapPayroll(rs));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return payrollList;
    }

    public List<PayrollDTO> findPayrollList(Long employeeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PayrollDTO> payrollList = new ArrayList<PayrollDTO>();

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select payroll_id, employee_id, payroll_year_month, total_earnings, total_deductions, net_pay, confirmed_at, pay_date, status from payroll where employee_id=? order by payroll_year_month desc";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, employeeId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                payrollList.add(mapPayroll(rs));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return payrollList;
    }

    public int insertPayroll(PayrollDTO payroll) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "insert into payroll(payroll_id, employee_id, payroll_year_month, total_earnings, total_deductions, net_pay, confirmed_at, pay_date, status) values(?,?,?,?,?,?,?,?,?)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, payroll.getPayrollId());
            pstmt.setLong(2, payroll.getEmployeeId());
            pstmt.setDate(3, Date.valueOf(payroll.getPayrollYearMonth().atDay(1)));
            pstmt.setBigDecimal(4, payroll.getTotalEarnings());
            pstmt.setBigDecimal(5, payroll.getTotalDeductions());
            pstmt.setBigDecimal(6, payroll.getNetPay());
            setNullableDate(pstmt, 7, payroll.getConfirmedAt());
            setNullableDate(pstmt, 8, payroll.getPayDate());
            pstmt.setString(9, payroll.getStatus().name());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int updatePayroll(PayrollDTO payroll) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update payroll set employee_id=?, payroll_year_month=?, total_earnings=?, total_deductions=?, net_pay=?, confirmed_at=?, pay_date=?, status=? where payroll_id=?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, payroll.getEmployeeId());
            pstmt.setDate(2, Date.valueOf(payroll.getPayrollYearMonth().atDay(1)));
            pstmt.setBigDecimal(3, payroll.getTotalEarnings());
            pstmt.setBigDecimal(4, payroll.getTotalDeductions());
            pstmt.setBigDecimal(5, payroll.getNetPay());
            setNullableDate(pstmt, 6, payroll.getConfirmedAt());
            setNullableDate(pstmt, 7, payroll.getPayDate());
            pstmt.setString(8, payroll.getStatus().name());
            pstmt.setLong(9, payroll.getPayrollId());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }
    
    public int updatePayrollStatusByMonth(YearMonth yearMonth, CommonStatus status) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update payroll set status=? where payroll_year_month=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            pstmt.setDate(2, Date.valueOf(yearMonth.atDay(1)));

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int updatePayrollStatus(Long payrollId, CommonStatus status) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update payroll set status=? where payroll_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            pstmt.setLong(2, payrollId);

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int refreshPayrollTotal(Long payrollId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update payroll p "
                    + "set (total_earnings, total_deductions, net_pay) = ( "
                    + "    select "
                    + "        e.base_salary + e.overtime_pay + e.transportation_allowance + e.performance_bonus + e.additional_allowance, "
                    + "        d.national_pension + d.health_insurance + d.long_term_care_insurance + d.employment_insurance + d.income_tax + d.local_income_tax, "
                    + "        (e.base_salary + e.overtime_pay + e.transportation_allowance + e.performance_bonus + e.additional_allowance) "
                    + "        - (d.national_pension + d.health_insurance + d.long_term_care_insurance + d.employment_insurance + d.income_tax + d.local_income_tax) "
                    + "    from earning e "
                    + "    join deduction d on d.payroll_id = e.payroll_id "
                    + "    where e.payroll_id = p.payroll_id "
                    + ") "
                    + "where p.payroll_id=? "
                    + "and exists ( "
                    + "    select 1 "
                    + "    from earning e "
                    + "    join deduction d on d.payroll_id = e.payroll_id "
                    + "    where e.payroll_id = p.payroll_id "
                    + ")";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, payrollId);

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int deletePayroll(Long payrollId, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;

        try {
            String sql = "delete from payroll where payroll_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, payrollId);

            return pstmt.executeUpdate();

        } finally {
            ConnectionHelper.close(pstmt);
        }
    }

}
