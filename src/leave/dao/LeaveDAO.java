package leave.dao;

import global.types.DBType;
import global.utils.ConnectionHelper;
import leave.dto.AnnualLeaveDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
}
