package payroll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import global.types.DBType;
import global.utils.ConnectionHelper;
import payroll.dto.DeductionDTO;

public class DeductionDAO {

    private DeductionDTO mapDeduction(ResultSet rs) throws Exception {
        DeductionDTO deduction = new DeductionDTO();

        deduction.setDeductionId(rs.getLong("deduction_id"));
        deduction.setPayrollId(rs.getLong("payroll_id"));
        deduction.setNationalPension(rs.getBigDecimal("national_pension"));
        deduction.setHealthInsurance(rs.getBigDecimal("health_insurance"));
        deduction.setLongTermCareInsurance(rs.getBigDecimal("long_term_care_insurance"));
        deduction.setEmploymentInsurance(rs.getBigDecimal("employment_insurance"));
        deduction.setIncomeTax(rs.getBigDecimal("income_tax"));
        deduction.setLocalIncomeTax(rs.getBigDecimal("local_income_tax"));

        return deduction;
    }
    
    public DeductionDTO findDeductionByPayrollId(Long payrollId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DeductionDTO deduction = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select deduction_id, payroll_id, national_pension, health_insurance, long_term_care_insurance, employment_insurance, income_tax, local_income_tax from deduction where payroll_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, payrollId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                deduction = mapDeduction(rs);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return deduction;
    }

    public int insertDeduction(DeductionDTO deductionDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "insert into deduction(deduction_id, payroll_id, national_pension, health_insurance, long_term_care_insurance, employment_insurance, income_tax, local_income_tax) values(?,?,?,?,?,?,?,?)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, deductionDTO.getDeductionId());
            pstmt.setLong(2, deductionDTO.getPayrollId());
            pstmt.setBigDecimal(3, deductionDTO.getNationalPension());
            pstmt.setBigDecimal(4, deductionDTO.getHealthInsurance());
            pstmt.setBigDecimal(5, deductionDTO.getLongTermCareInsurance());
            pstmt.setBigDecimal(6, deductionDTO.getEmploymentInsurance());
            pstmt.setBigDecimal(7, deductionDTO.getIncomeTax());
            pstmt.setBigDecimal(8, deductionDTO.getLocalIncomeTax());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int updateDeduction(DeductionDTO deductionDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update deduction set national_pension=?, health_insurance=?, long_term_care_insurance=?, employment_insurance=?, income_tax=?, local_income_tax=? where deduction_id=?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setBigDecimal(1, deductionDTO.getNationalPension());
            pstmt.setBigDecimal(2, deductionDTO.getHealthInsurance());
            pstmt.setBigDecimal(3, deductionDTO.getLongTermCareInsurance());
            pstmt.setBigDecimal(4, deductionDTO.getEmploymentInsurance());
            pstmt.setBigDecimal(5, deductionDTO.getIncomeTax());
            pstmt.setBigDecimal(6, deductionDTO.getLocalIncomeTax());
            pstmt.setLong(7, deductionDTO.getDeductionId());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return rowcount;
    }

    public int deleteDeduction(Long payrollId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "delete from deduction where payroll_id=?";

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
