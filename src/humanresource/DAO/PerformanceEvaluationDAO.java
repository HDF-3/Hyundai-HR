package humanresource.DAO;

import global.types.DBType;
import global.utils.ConnectionHelper;
import humanresource.DTO.PerformanceEvaluationDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PerformanceEvaluationDAO {
    public int insertPerformanceEvaluation(PerformanceEvaluationDTO performanceEvaluationDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "INSERT INTO PERFORMANCE_EVALUATION(EVALUATION_ID, EVALUATION_YEAR, EVALUATION_QUARTER, COMMENT, PERFORMANCE_GRADE) values(?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, performanceEvaluationDTO.getEvaluationId());
            pstmt.setString(2, performanceEvaluationDTO.getEvaluationYear());
            pstmt.setInt(3, performanceEvaluationDTO.getEvaluationQuarter());
            pstmt.setString(4, performanceEvaluationDTO.getComment());
            pstmt.setInt(5, performanceEvaluationDTO.getPerformanceGrade().getCode());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return rowcount;
    }

    public List<PerformanceEvaluationDTO> selectHistoryByEmpId(Long empId){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<PerformanceEvaluationDTO> performanceEvaluationDTOList = new ArrayList<>();
        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT * FROM PERFORMANCE_EVALUATION WHERE EMP_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, empId);
            rs = pstmt.executeQuery();

            while(rs.next()){
                PerformanceEvaluationDTO performanceEvaluationDTO = new PerformanceEvaluationDTO();
                performanceEvaluationDTO.setEvaluationId(rs.getLong("EVALUATION_ID"));
                performanceEvaluationDTO.setEvaluationYear(rs.getString("EVALUATION_YEAR"));
                performanceEvaluationDTO.setEvaluationQuarter(rs.getInt("EVALUATION_QUARTER"));
                performanceEvaluationDTO.setComment(rs.getString("COMMENT"));
                int performanceGrade = rs.getInt("PERFORMANCE_GRADE");
                performanceEvaluationDTO.setPerformanceGrade(global.types.PerformanceGrade.fromCode(performanceGrade));
                performanceEvaluationDTOList.add(performanceEvaluationDTO);

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return performanceEvaluationDTOList;
    }
    public int updatePerformanceEvaluation(PerformanceEvaluationDTO performanceEvaluationDTO){

        return 0;
    }
}
