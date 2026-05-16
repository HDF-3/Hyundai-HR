package payroll.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import global.types.DBType;
import global.utils.ConnectionHelper;
import payroll.dto.PerformanceBonusPolicyDTO;

public class PerformanceBonusPolicyDAO {

    private PerformanceBonusPolicyDTO mapPerformanceBonusPolicy(ResultSet rs) throws Exception {
        PerformanceBonusPolicyDTO performanceBonusPolicy = new PerformanceBonusPolicyDTO();

        performanceBonusPolicy.setPerformanceBonusPolicyId(rs.getLong("performance_bonus_policy_id"));
        performanceBonusPolicy.setEvalYear(rs.getInt("eval_year"));
        performanceBonusPolicy.setEvalQuarter(rs.getInt("eval_quarter"));
        performanceBonusPolicy.setGrade(rs.getString("grade"));
        performanceBonusPolicy.setBonusRate(rs.getBigDecimal("bonus_rate"));
        performanceBonusPolicy.setFixedAmount(rs.getBigDecimal("fixed_amount"));

        return performanceBonusPolicy;
    }

    public PerformanceBonusPolicyDTO findPerformanceBonusPolicy(Integer evalYear, Integer evalQuarter, String grade) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PerformanceBonusPolicyDTO performanceBonusPolicy = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select performance_bonus_policy_id, eval_year, eval_quarter, grade, bonus_rate, fixed_amount " +
                    "from performance_bonus_policy where eval_year=? and eval_quarter=? and grade=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, evalYear);
            pstmt.setInt(2, evalQuarter);
            pstmt.setString(3, grade);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                performanceBonusPolicy = mapPerformanceBonusPolicy(rs);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return performanceBonusPolicy;
    }

    public List<PerformanceBonusPolicyDTO> findPerformanceBonusPolicyList(Integer evalYear, Integer evalQuarter) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<PerformanceBonusPolicyDTO> performanceBonusPolicyList = new ArrayList<PerformanceBonusPolicyDTO>();

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select performance_bonus_policy_id, eval_year, eval_quarter, grade, bonus_rate, fixed_amount " +
                    "from performance_bonus_policy where eval_year=? and eval_quarter=? order by grade";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, evalYear);
            pstmt.setInt(2, evalQuarter);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                performanceBonusPolicyList.add(mapPerformanceBonusPolicy(rs));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return performanceBonusPolicyList;
    }

    public int updatePerformanceBonusPolicy(PerformanceBonusPolicyDTO performanceBonusPolicy) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "update performance_bonus_policy set bonus_rate=?, fixed_amount=? " +
                    "where eval_year=? and eval_quarter=? and grade=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setBigDecimal(1, performanceBonusPolicy.getBonusRate());
            pstmt.setBigDecimal(2, performanceBonusPolicy.getFixedAmount());
            pstmt.setInt(3, performanceBonusPolicy.getEvalYear());
            pstmt.setInt(4, performanceBonusPolicy.getEvalQuarter());
            pstmt.setString(5, performanceBonusPolicy.getGrade());

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
