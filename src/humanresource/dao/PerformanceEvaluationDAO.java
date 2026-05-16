package humanresource.dao;

import global.types.DBType;
import global.utils.ConnectionHelper;
import humanresource.dto.PerformanceEvaluationDTO;
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
            String sql = "INSERT INTO PERFORMANCE_EVALUATION(EVALUATION_ID, TARGET_EMP_ID, EVAL_YEAR, EVAL_QUARTER, GRADE) VALUES (SEQ_BONUS_ID.NEXTVAL, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, dto.getTargetEmpId());
            pstmt.setInt(2, Integer.parseInt(dto.getEvaluationYear()));

            if(dto.getEvaluationQuarter() != null) {
                pstmt.setLong(3, dto.getEvaluationQuarter());
            } else {
                pstmt.setNull(3, java.sql.Types.NUMERIC);
            }
            
            pstmt.setInt(4, dto.getPerformanceGrade().getCode());

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
            String sql = "SELECT * FROM PERFORMANCE_EVALUATION WHERE TARGET_EMP_ID = ? ORDER BY EVAL_YEAR DESC, EVAL_QUARTER DESC, EVALUATION_ID DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, empId);
            rs = pstmt.executeQuery();

            while(rs.next()) {
                PerformanceEvaluationDTO dto = new PerformanceEvaluationDTO();

                dto.setEvaluationId(rs.getLong("EVALUATION_ID"));
                dto.setTargetEmpId(rs.getLong("TARGET_EMP_ID"));
                dto.setEvaluationYear(rs.getString("EVAL_YEAR"));
                
                long quarter = rs.getLong("EVAL_QUARTER");
                if(!rs.wasNull()) {
                    dto.setEvaluationQuarter(quarter);
                }

                int gradeCode = rs.getInt("GRADE");
                dto.setPerformanceGrade(PerformanceGrade.fromCode(gradeCode));

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
