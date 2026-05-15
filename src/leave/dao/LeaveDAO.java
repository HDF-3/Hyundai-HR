package leave.dao;

import global.types.DBType;
import global.utils.ConnectionHelper;
import leave.dto.AnnualLeaveDTO;
import leave.dto.LeaveRequestDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveDAO {
    public List<AnnualLeaveDTO> findAnnualLeaveListByEmployeeId(Long employeeId) {
        List<AnnualLeaveDTO> list = new ArrayList<>();
        String sql = "select GRANTED_ANNUAL_LEAVE, USED_ANNUAL_LEAVE, REMAINING_ANNUAL_LEAVE, IS_ACTIVE, GRANTED_AT, EXPIRED_AT " +
                "from ANNUAL_LEAVE " +
                "where EMP_ID = ?";
        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setLong(1, employeeId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    AnnualLeaveDTO dto = new AnnualLeaveDTO();
                    dto.setGrantedAnnualLeave(rs.getDouble("GRANTED_ANNUAL_LEAVE"));
                    dto.setUsedAnnualLeave(rs.getDouble("USED_ANNUAL_LEAVE"));
                    dto.setRemainingAnnualLeave(rs.getDouble("REMAINING_ANNUAL_LEAVE"));
                    dto.setIsActive(rs.getString("IS_ACTIVE").charAt(0));
                    dto.setGrantedAt(rs.getDate("GRANTED_AT").toLocalDate());
                    dto.setExpiredAt(rs.getDate("EXPIRED_AT").toLocalDate());
                    list.add(dto);
                }
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return list;
    }
    public AnnualLeaveDTO findAnnualLeaveById(Long employeeId) {
        AnnualLeaveDTO dto = new AnnualLeaveDTO();
        String sql = "select EMP_ID, GRANTED_ANNUAL_LEAVE, USED_ANNUAL_LEAVE, REMAINING_ANNUAL_LEAVE, IS_ACTIVE, GRANTED_AT, EXPIRED_AT " +
                "from ANNUAL_LEAVE " +
                "where EMP_ID = ? and IS_ACTIVE = 'Y'";
        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ){
            pstmt.setLong(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    dto.setEmployeeId(rs.getLong("EMP_ID"));
                    dto.setGrantedAnnualLeave(rs.getDouble("GRANTED_ANNUAL_LEAVE"));
                    dto.setUsedAnnualLeave(rs.getDouble("USED_ANNUAL_LEAVE"));
                    dto.setRemainingAnnualLeave(rs.getDouble("REMAINING_ANNUAL_LEAVE"));
                    dto.setIsActive(rs.getString("IS_ACTIVE").charAt(0));
                    dto.setGrantedAt(rs.getDate("GRANTED_AT").toLocalDate());
                    dto.setExpiredAt(rs.getDate("EXPIRED_AT").toLocalDate());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dto;
    }
    public boolean insertLeaveRequest(LeaveRequestDTO dto) {
        String sql = "insert into LEAVE_REQUEST (LEAVE_REQUEST_ID, EMPLOYEE_ID, LEAVE_REASON, " +
                "START_DATE, END_DATE, LEAVE_TYPE_CODE, REQUEST_STATUS) " +
                "values (SEQ_LEAVE_REQUEST_ID.NEXTVAL, ?, ?, ?, ?, ?, ?)";
        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ){
            pstmt.setLong(1, dto.getEmployeeId());
            pstmt.setString(2, dto.getReason());
            // LocalDate를 java.sql.Date로 변환
            pstmt.setDate(3, Date.valueOf(dto.getStartDate()));
            pstmt.setDate(4, Date.valueOf(dto.getEndDate()));
            // Enum을 문자열 코드로 저장
            pstmt.setString(5, dto.getLeaveType().name());
            pstmt.setString(6, dto.getStatus().name());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
