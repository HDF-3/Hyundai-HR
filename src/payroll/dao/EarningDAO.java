package payroll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import global.types.DBType;
import global.utils.ConnectionHelper;
import payroll.dto.EarningDTO;

public class EarningDAO {

    private EarningDTO mapEarning(ResultSet rs) throws Exception {
        EarningDTO earning = new EarningDTO();

        earning.setEarningId(rs.getLong("earning_id"));
        earning.setPayrollId(rs.getLong("payroll_id"));
        earning.setBaseSalary(rs.getBigDecimal("base_salary"));
        earning.setBonus(rs.getBigDecimal("bonus"));
        earning.setOvertimePay(rs.getBigDecimal("overtime_pay"));
        earning.setTransportationAllowance(rs.getBigDecimal("transportation_allowance"));
        earning.setDepartmentAllowance(rs.getBigDecimal("department_allowance"));

        return earning;
    }
    
    public EarningDTO findEarningByPayrollId(Long payrollId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        EarningDTO earning = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select earning_id, payroll_id, base_salary, bonus, overtime_pay, transportation_allowance, department_allowance from earning where payroll_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, payrollId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                earning = mapEarning(rs);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return earning;
    }

    public int insertEarning(EarningDTO earningDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "insert into earning(earning_id, payroll_id, base_salary, bonus, overtime_pay, transportation_allowance, department_allowance) values(?,?,?,?,?,?,?)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, earningDTO.getEarningId());
            pstmt.setLong(2, earningDTO.getPayrollId());
            pstmt.setBigDecimal(3, earningDTO.getBaseSalary());
            pstmt.setBigDecimal(4, earningDTO.getBonus());
            pstmt.setBigDecimal(5, earningDTO.getOvertimePay());
            pstmt.setBigDecimal(6, earningDTO.getTransportationAllowance());
            pstmt.setBigDecimal(7, earningDTO.getDepartmentAllowance());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int updateEarning(EarningDTO earningDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update earning set base_salary=?, bonus=?, overtime_pay=?, transportation_allowance=?, department_allowance=? where earning_id=?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setBigDecimal(1, earningDTO.getBaseSalary());
            pstmt.setBigDecimal(2, earningDTO.getBonus());
            pstmt.setBigDecimal(3, earningDTO.getOvertimePay());
            pstmt.setBigDecimal(4, earningDTO.getTransportationAllowance());
            pstmt.setBigDecimal(5, earningDTO.getDepartmentAllowance());
            pstmt.setLong(6, earningDTO.getEarningId());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int deleteEarning(Long payrollId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "delete from earning where payroll_id=?";

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
}
