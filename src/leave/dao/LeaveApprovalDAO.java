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
                "where e.DEPT_ID = (select DEPT_ID from EMPLOYEE where EMP_ID = ?) " +
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
    public LeaveRequestDTO getLeaveRequestById(Connection conn, Long leaveRequestId) {
        String sql = "select LEAVE_REQUEST_ID, EMPLOYEE_ID, LEAVE_REASON, START_DATE, END_DATE, LEAVE_TYPE_CODE, REQUEST_STATUS " +
                "from LEAVE_REQUEST " +
                "where LEAVE_REQUEST_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, leaveRequestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LeaveRequestDTO dto = new LeaveRequestDTO();
                    dto.setLeaveRequestId(rs.getLong("LEAVE_REQUEST_ID"));
                    dto.setEmployeeId(rs.getLong("EMPLOYEE_ID"));
                    dto.setReason(rs.getString("LEAVE_REASON"));
                    dto.setStartDate(rs.getDate("START_DATE").toLocalDate());
                    dto.setEndDate(rs.getDate("END_DATE").toLocalDate());
                    dto.setLeaveType(LeaveType.valueOf(rs.getString("LEAVE_TYPE_CODE")));
                    dto.setStatus(CommonStatus.valueOf(rs.getString("REQUEST_STATUS")));

                    return dto;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 조회된 데이터가 없거나 예외 발생 시 null 반환
    }
    public boolean updateLeaveRequestStatus(Connection conn, Long leaveRequestId, CommonStatus status) {
        String sql = "update LEAVE_REQUEST set REQUEST_STATUS = ? where LEAVE_REQUEST_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, status.toString());
            pstmt.setLong(2, leaveRequestId);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deductAnnualLeave(Connection conn, Long empId, double deductionDays) {
        String sql = "update ANNUAL_LEAVE " +
                "set USED_ANNUAL_LEAVE = USED_ANNUAL_LEAVE + ? " +
                "where EMP_ID = ? and IS_ACTIVE = 'Y'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, deductionDays);
            pstmt.setLong(2, empId);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isAdmin(Connection conn, Long adminId) {
        String sql = "select count(*) " +
                "from EMPLOYEE " +
                "where EMP_ID = ? " +
                "and IS_ADMIN = 'Y'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean insertLeaveApprovalLog(Connection conn, Long adminId, Long leaveRequestId) {
        String sql = "insert into LEAVE_APPROVAL (LEAVE_APPROVAL_ID, APPROVER_ID, LEAVE_REQUEST_ID, APPROVAL_DATE) " +
                "values (SEQ_LEAVE_APPROVAL_ID.NEXTVAL, ?, ?, SYSDATE)";


        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, adminId);
            pstmt.setLong(2, leaveRequestId);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
