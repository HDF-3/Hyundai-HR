package humanresource.dao;

import global.types.DBType;
import global.utils.ConnectionHelper;
import humanresource.dto.AssignmentHistoryDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AssignmentHistoryDAO {

    public List<AssignmentHistoryDTO> selectHistoryByEmpId(Long empId) {
        List<AssignmentHistoryDTO> historyList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);

            String sql = "SELECT h.HISTORY_ID, e.NAME, d.DEPT_NAME, p.POSITION_NAME, h.PAY_GRADE, " +
                    "r.REASON_NAME, h.START_DATE, h.END_DATE " +
                    "FROM ASSIGNMENT_HISTORY h " +
                    "JOIN EMPLOYEE e ON h.EMP_ID = e.EMP_ID " +
                    "LEFT JOIN DEPARTMENT d ON h.DEPT_ID = d.DEPT_ID " +
                    "LEFT JOIN POSITION p ON h.POSITION_ID = p.POSITION_ID " +
                    "LEFT JOIN ASSIGNMENT_CHANGE_REASON r ON h.REASON_ID = r.REASON_ID " +
                    "WHERE h.EMP_ID = ? " +
                    "ORDER BY h.History_ID DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, empId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                AssignmentHistoryDTO dto = new AssignmentHistoryDTO();
                dto.setHistoryId(rs.getLong("HISTORY_ID"));
                dto.setEName(rs.getString("NAME"));
                dto.setDeptName(rs.getString("DEPT_NAME"));
                dto.setPositionName(rs.getString("POSITION_NAME"));
                
                dto.setPayGrade(rs.getInt("PAY_GRADE"));
                dto.setReasonName(rs.getString("REASON_NAME"));

                // 날짜 세팅 (NPE 방어)
                java.sql.Date startDate = rs.getDate("START_DATE");
                if(startDate != null) dto.setStartDate(startDate.toLocalDate());

                java.sql.Date endDate = rs.getDate("END_DATE");
                if(endDate != null) dto.setEndDate(endDate.toLocalDate());

                historyList.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return historyList;
    }
}