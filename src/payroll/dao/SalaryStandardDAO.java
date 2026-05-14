package payroll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;

import global.types.DBType;
import global.utils.ConnectionHelper;
import payroll.dto.SalaryStandardDTO;

public class SalaryStandardDAO {

    private SalaryStandardDTO mapSalaryStandard(ResultSet rs) throws Exception {
        SalaryStandardDTO salaryStandard = new SalaryStandardDTO();

        salaryStandard.setSalaryStandardId(rs.getLong("salary_standard_id"));
        salaryStandard.setPositionId(rs.getLong("position_id"));
        salaryStandard.setPayGrade(rs.getInt("pay_grade"));
        salaryStandard.setBaseSalary(rs.getBigDecimal("base_salary"));
        salaryStandard.setRegularHourlyRate(rs.getBigDecimal("regular_hourly_rate"));

        return salaryStandard;
    }

    public SalaryStandardDTO findSalaryStandard(Long positionId, Integer payGrade) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        SalaryStandardDTO salaryStandard = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select salary_standard_id, position_id, pay_grade, base_salary, regular_hourly_rate from salary_standard where position_id=? and pay_grade=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, positionId);
            pstmt.setInt(2, payGrade);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                salaryStandard = new SalaryStandardDTO();

                salaryStandard.setSalaryStandardId(rs.getLong("salary_standard_id"));
                salaryStandard.setPositionId(rs.getLong("position_id"));
                salaryStandard.setPayGrade(rs.getInt("pay_grade"));
                salaryStandard.setBaseSalary(rs.getBigDecimal("base_salary"));
                salaryStandard.setRegularHourlyRate(rs.getBigDecimal("regular_hourly_rate"));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return salaryStandard;
    }

    public int insertSalaryStandard(SalaryStandardDTO salaryStandard) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "insert into salary_standard(salary_standard_id, position_id, pay_grade, base_salary, regular_hourly_rate) values(?,?,?,?,?)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, salaryStandard.getSalaryStandardId());
            pstmt.setLong(2, salaryStandard.getPositionId());
            pstmt.setInt(3, salaryStandard.getPayGrade());
            pstmt.setBigDecimal(4, salaryStandard.getBaseSalary());
            pstmt.setBigDecimal(5, salaryStandard.getRegularHourlyRate());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int updateSalaryStandard(SalaryStandardDTO salaryStandard) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update salary_standard set base_salary=?, regular_hourly_rate=? where position_id=? and pay_grade=?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setBigDecimal(1, salaryStandard.getBaseSalary());
            pstmt.setBigDecimal(2, salaryStandard.getRegularHourlyRate());
            pstmt.setLong(3, salaryStandard.getPositionId());
            pstmt.setInt(4, salaryStandard.getPayGrade());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int deleteSalaryStandard(Long positionId, Integer payGrade) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "delete from salary_standard where position_id=? and pay_grade=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, positionId);
            pstmt.setInt(2, payGrade);

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
