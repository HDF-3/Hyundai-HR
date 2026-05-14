package humanresource.DAO;

import global.types.DBType;
import global.utils.ConnectionHelper;
import humanresource.DTO.PerformanceEvaluationDTO;
import global.types.PerformanceGrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PerformanceEvaluationDAO {

    public int insertPerformanceEvaluation(PerformanceEvaluationDTO dto) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "INSERT INTO PERFORMANCE_EVALUATION(TARGET_EMP_ID, EVAL_YEAR, EVAL_QUARTER, GRADE) VALUES (?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, dto.getTargetEmpId());
            pstmt.setString(2, dto.getEvaluationYear());
            pstmt.setInt(3, dto.getEvaluationQuarter());
            pstmt.setString(4, dto.getPerformanceGrade().name()); // Enum 이름을 문자열로 저장

            rowcount = pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Insert Error: " + e.getMessage());
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return rowcount;
    }

    public List<PerformanceEvaluationDTO> selectHistoryByEmpId(Long empId){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<PerformanceEvaluationDTO> dtoList = new ArrayList<>();
        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT * FROM PERFORMANCE_EVALUATION WHERE TARGET_EMP_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, empId);
            rs = pstmt.executeQuery();

            while(rs.next()) {
                PerformanceEvaluationDTO dto = new PerformanceEvaluationDTO();

                dto.setEvaluationId(rs.getLong("EVALUATION_ID"));
                dto.setTargetEmpId(rs.getLong("TARGET_EMP_ID"));
                dto.setEvaluationYear(rs.getString("EVAL_YEAR"));
                dto.setEvaluationQuarter(rs.getInt("EVAL_QUARTER"));

                String gradeStr = rs.getString("GRADE");
                dto.setPerformanceGrade(PerformanceGrade.valueOf(gradeStr));

                dtoList.add(dto);
            }

        } catch (Exception e) {
            System.out.println("Select Error: " + e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return dtoList;
    }

    public int updatePerformanceEvaluation(PerformanceEvaluationDTO dto){
        return 0;
    }
}