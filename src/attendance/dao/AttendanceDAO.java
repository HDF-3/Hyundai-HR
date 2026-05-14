package attendance.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;

import attendance.dto.RequestWorkTimeDTO;
import global.types.DBType;
import global.utils.ConnectionHelper;

public class AttendanceDAO {
	public int insertWorkTime(RequestWorkTimeDTO reqTime) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		int result =-1;
		
		String sql = "INSERT INTO work_time (emp_id, applied_date, on_work_time, off_work_time) values(?,?,?,?)";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, reqTime.getEmpId());
			pstmt.setDate(2, Date.valueOf(reqTime.getAppliedDate()));
			pstmt.setString(3, reqTime.getOnWorkTime().toString());
			pstmt.setString(4, reqTime.getOffWorkTime().toString());
			result = pstmt.executeUpdate();
			
			return result;	
		} catch (SQLException e) {
			e.printStackTrace();
			return result;
		}finally {
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
}
