package leave.dao;

import global.types.CommonStatus;
import global.types.DBType;
import global.types.LeaveType;
import global.utils.ConnectionHelper;
import leave.dto.LeaveRequestDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LeaveApprovalDAO {
    public List<LeaveRequestDTO> findPendingRequest(Long adminId) {
        List<LeaveRequestDTO> list = new ArrayList<>();
        String sql = "select lr.* " +
                "from LEAVE_REQUEST lr " + "join EMPLOYEE e " +
                "on lr.EMPLOYEE_ID = e.EMP_ID " +
                "where e.DEPT_ID = (SELECT DEPT_ID FROM EMPLOYEE WHERE EMP_ID = ?) " +
                "and lr.REQUEST_STATUS = 'PENDING' " +
                "order by lr.START_DATE ASC";
        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ){
            pstmt.setLong(1, adminId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequestDTO dto = new LeaveRequestDTO();
                    dto.setLeaveRequestId(rs.getLong("LEAVE_REQUEST_ID"));
                    dto.setEmployeeId(rs.getLong("EMPLOYEE_ID"));
                    dto.setReason(rs.getString("LEAVE_REASON"));
                    dto.setStartDate(rs.getDate("START_DATE").toLocalDate());
                    dto.setEndDate(rs.getDate("END_DATE").toLocalDate());
                    dto.setLeaveType(LeaveType.valueOf(rs.getString("LEAVE_TYPE_CODE")));
                    dto.setStatus(CommonStatus.valueOf(rs.getString("REQUEST_STATUS")));
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateLeaveRequestStatus(Long leaveRequestId, CommonStatus status) {
        String sql = "update LEAVE_REQUEST SET REQUEST_STATUS = ? WHERE LEAVE_REQUEST_ID = ?";

        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ){
            pstmt.setString(1, status.toString());
            pstmt.setLong(2, leaveRequestId);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
