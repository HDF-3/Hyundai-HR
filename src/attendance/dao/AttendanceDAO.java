package attendance.dao;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import attendance.dto.MissingAttendanceDTO;
import attendance.dto.NormalAttendanceDTO;
import attendance.dto.OvertimeDTO;
import attendance.dto.RequestWorkTimeDTO;
import java.time.YearMonth;
import global.types.DBType;
import global.types.LeaveType;
import global.utils.ConnectionHelper;

public class AttendanceDAO {

	public List<NormalAttendanceDTO> findAllAttenDances(Long empId) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<NormalAttendanceDTO> list = new ArrayList<>();
		
		String sql = "select * from attendance where emp_id = ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, empId);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					list.add(
						    new NormalAttendanceDTO(
						        rs.getLong("EMP_ID"),
						        rs.getDate("WORK_DATE").toLocalDate(),
						        rs.getTimestamp("ON_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("ON_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getTimestamp("OFF_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("OFF_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getString("IS_CLOSED")
						    )
						);
				}while(rs.next());
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	public List<NormalAttendanceDTO> findAllAttenDances(LocalDate sDate, LocalDate eDate) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<NormalAttendanceDTO> list = new ArrayList<>();
		
		String sql = 	"select * "
				+ 		"from attendance "
				+ 		"where WORK_DATE BETWEEN ? and ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1,  java.sql.Date.valueOf(sDate));
			pstmt.setDate(2, java.sql.Date.valueOf(eDate));
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					list.add(
						    new NormalAttendanceDTO(
						        rs.getLong("EMP_ID"),
						        rs.getDate("WORK_DATE").toLocalDate(),
						        rs.getTimestamp("ON_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("ON_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getTimestamp("OFF_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("OFF_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getString("IS_CLOSED")
						    )
						);
				}while(rs.next());
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	
	public List<NormalAttendanceDTO> findNormalAttenDances(Long empId) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<NormalAttendanceDTO> list = new ArrayList<>();
		
		String sql = "select * from attendance where emp_id = ?"
				+ "AND on_work_time is not null "
				+ "AND off_work_time is not null";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, empId);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					list.add(
						    new NormalAttendanceDTO(
						        rs.getLong("EMP_ID"),
						        rs.getDate("WORK_DATE").toLocalDate(),
						        rs.getTimestamp("ON_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("ON_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getTimestamp("OFF_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("OFF_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getString("IS_CLOSED")
						    )
						);
				}while(rs.next());
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	public List<NormalAttendanceDTO> findNormalAttenDances(LocalDate sDate, LocalDate eDate) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<NormalAttendanceDTO> list = new ArrayList<>();
		
		String sql = "select * from attendance where WORK_DATE BETWEEN ? and ?"
				+ "AND on_work_time is not null "
				+ "AND off_work_time is not null";;
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1,  java.sql.Date.valueOf(sDate));
			pstmt.setDate(2, java.sql.Date.valueOf(eDate));
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					list.add(
						    new NormalAttendanceDTO(
						        rs.getLong("EMP_ID"),
						        rs.getDate("WORK_DATE").toLocalDate(),
						        rs.getTimestamp("ON_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("ON_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getTimestamp("OFF_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("OFF_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getString("IS_CLOSED")
						    )
						);
				}while(rs.next());
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	
	public List<MissingAttendanceDTO> findMissingAttenDances(Long empId) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<MissingAttendanceDTO> list = new ArrayList<>();
		
		String sql = "select * from MISSING_PUNCH p "
				+ "join MISSING_PUNCH_REASON r on p.missing_reason_id = r.missing_reason_id "
				+ "join attendance a on p.emp_id = a.emp_id AND p.work_date = a.work_date "
				+ "where p.emp_id = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, empId);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					list.add(
						    new MissingAttendanceDTO(
						        rs.getLong("EMP_ID"),
						        rs.getDate("WORK_DATE").toLocalDate(),
						        rs.getTimestamp("ON_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("ON_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getTimestamp("OFF_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("OFF_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getString("IS_CLOSED"),
						        rs.getString("MISSING_TYPE"),
						        rs.getString("MISSING_REASON")
						    )
						);
				}while(rs.next());
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	public List<MissingAttendanceDTO> findMissingAttenDances(Long empId, LocalDate sDate, LocalDate eDate) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<MissingAttendanceDTO> list = new ArrayList<>();
		
		String sql = 	"select * "
				+ 		"from MISSING_PUNCH p "
				+ 		"join MISSING_PUNCH_REASON r "
				+ 		"	on p.missing_reason_id = r.missing_reason_id "
				+ 		"join attendance a "
				+ 		"	on p.emp_id = a.emp_id AND p.work_date = a.work_date "
				+ 		"where a.work_date between ? and ? "
				+ 		"	AND p.emp_id = ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1,  java.sql.Date.valueOf(sDate));
			pstmt.setDate(2, java.sql.Date.valueOf(eDate));
			pstmt.setLong(3, empId);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					list.add(
						    new MissingAttendanceDTO(
						        rs.getLong("EMP_ID"),
						        rs.getDate("WORK_DATE").toLocalDate(),
						        rs.getTimestamp("ON_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("ON_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getTimestamp("OFF_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("OFF_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getString("IS_CLOSED"),
						        rs.getString("MISSING_TYPE"),
						        rs.getString("MISSING_REASON")
						    )
						);
				}while(rs.next());
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	public List<MissingAttendanceDTO> findMissingAttenDances(LocalDate sDate, LocalDate eDate) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<MissingAttendanceDTO> list = new ArrayList<>();
		
		String sql = "select * from MISSING_PUNCH p "
				+ "join MISSING_PUNCH_REASON r on p.missing_reason_id = r.missing_reason_id "
				+ "join attendance a on p.emp_id = a.emp_id AND p.work_date = a.work_date "
				+ "where a.on_work_time between ? and ? ";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1,  java.sql.Date.valueOf(sDate));
			pstmt.setDate(2, java.sql.Date.valueOf(eDate));
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					list.add(
						    new MissingAttendanceDTO(
						        rs.getLong("EMP_ID"),
						        rs.getDate("WORK_DATE").toLocalDate(),
						        rs.getTimestamp("ON_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("ON_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getTimestamp("OFF_WORK_TIME") == null
						            ? null
						            : rs.getTimestamp("OFF_WORK_TIME").toLocalDateTime().toLocalTime(),
						        rs.getString("IS_CLOSED"),
						        rs.getString("MISSING_TYPE"),
						        rs.getString("MISSING_REASON")
						    )
						);
				}while(rs.next());
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	
	public int insertWorkTime(RequestWorkTimeDTO reqTime) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		int result =-1;
		
		String sql = 	"UPDATE work_time "
				+ 		"SET applied_date = LAST_DAY(ADD_MONTHS(?, -1)) "
				+ 		"WHERE emp_id = ? "
				+ 		"AND applied_date = DATE '9999-12-31'";
		String sql2 = 	"INSERT INTO work_time (emp_id, applied_date, on_work_time, off_work_time) "
				+ 		"values(?,?,?,?)";


		try {
			conn.setAutoCommit(false); //트랜잭션 위함
			pstmt = conn.prepareStatement(sql);
			pstmt.setDate(1, java.sql.Date.valueOf(reqTime.getAppliedMonth().atDay(1)));
			pstmt.setLong(2, reqTime.getEmpId());
			result = pstmt.executeUpdate();
			
			pstmt = conn.prepareStatement(sql2);
			pstmt.setLong(1, reqTime.getEmpId());
			pstmt.setDate(2,  Date.valueOf("9999-12-31"));
			pstmt.setString(3, reqTime.getOnWorkTime().toString());
			pstmt.setString(4, reqTime.getOffWorkTime().toString());
			result = pstmt.executeUpdate();
			
			conn.commit();
			return result;	
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return result;
		}finally {
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	
	public int mergeToday(Long empId) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		int result =-1;
		
		String sql = 	"MERGE INTO ATTENDANCE a "
		        + 		"USING ( "
		        + 		"SELECT ? AS emp_id, "
		        +		"		TRUNC(SYSDATE) AS work_date, "
		        + 		"		CAST(SYSTIMESTAMP AS TIMESTAMP) AS now_time "
		        + 		"FROM dual "
		        + 		") src "
		        + 		"ON ( "
		        + 		"a.emp_id = src.emp_id "
		        +		"AND a.work_date = src.work_date "
		        + 		") "
		        + 		"WHEN MATCHED THEN "
		        + 		"UPDATE SET "
		        +		"a.off_work_time = src.now_time "
		        + 		"WHERE a.off_work_time IS NULL "
		        + 		"WHEN NOT MATCHED THEN "
		        + 		"INSERT ( "
		        + 		"emp_id, "
		        +		"work_date, "
		        + 		"on_work_time, "
		        + 		"is_closed "
		        + 		") "
		        + 		"VALUES ( "
		        + 		"src.emp_id, "
		        + 		"src.work_date, "
		        + 		"src.now_time, "
		        + 		"'N' "
		        + 		")";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, empId);
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
	
	//급여부분 연계
	//굳이 결과 집합 사용할 필요 없으니 프로시저로 돌리고 값만 받기
	public char findIsClosed(YearMonth ym) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
	    CallableStatement cstmt = null;

	    String sql = "{ call CHECK_MONTH_ATTENDANCE_CLOSED(?, ?) }";

	    try {
	        cstmt = conn.prepareCall(sql);

	        cstmt.setString(1, ym.toString()); //"2026-05"
	        cstmt.registerOutParameter(2, Types.CHAR);

	        cstmt.execute();

	        return cstmt.getString(2).charAt(0);

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return 'N';

	    } finally {
	        ConnectionHelper.close(cstmt);
	        ConnectionHelper.close(conn);
	    }
	}
	
	//급여부분 연계
	public OvertimeDTO findOvertimeAmount(Long empId, YearMonth ym) {		
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		OvertimeDTO overtimeInfo = new OvertimeDTO();
		
		String sql = "SELECT\n"
				+ "    NVL(SUM(overtime_minutes), 0) AS total_overtime_minutes,\n"
				+ "    COUNT(*) AS overtime_days\n"
				+ "FROM (\n"
				+ "    SELECT\n"
				+ "        a.emp_id,\n"
				+ "        a.work_date,\n"
				+ "        (\n"
				+ "            TO_NUMBER(TO_CHAR(a.off_work_time, 'HH24')) * 60\n"
				+ "          + TO_NUMBER(TO_CHAR(a.off_work_time, 'MI'))\n"
				+ "        )\n"
				+ "        -\n"
				+ "        (\n"
				+ "            TO_NUMBER(SUBSTR(w.off_work_time, 1, 2)) * 60\n"
				+ "          + TO_NUMBER(SUBSTR(w.off_work_time, 4, 2))\n"
				+ "        ) AS overtime_minutes\n"
				+ "    FROM attendance a\n"
				+ "    JOIN work_time w\n"
				+ "      ON w.emp_id = a.emp_id\n"
				+ "     AND w.applied_date = (\n"
				+ "            SELECT MIN(w2.applied_date)\n"
				+ "            FROM work_time w2\n"
				+ "            WHERE w2.emp_id = a.emp_id\n"
				+ "              AND w2.applied_date >= a.work_date\n"
				+ "        )\n"
				+ "    WHERE a.emp_id = ?\n"
				+ "      AND a.work_date >= ?\n"
				+ "      AND a.work_date < ?\n"
				+ "      AND a.off_work_time IS NOT NULL\n"
				+ ")\n"
				+ "WHERE overtime_minutes > 0";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, empId);
			pstmt.setDate(2, Date.valueOf(ym.atDay(1)));
			pstmt.setDate(3, Date.valueOf(ym.plusMonths(1).atDay(1)));
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				do {
					overtimeInfo.setOvertimeHours(rs.getFloat("total_overtime_minutes")/60);
					overtimeInfo.setOvertimeDays(rs.getInt("overtime_days"));
				}while(rs.next());
			}
			return overtimeInfo;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			ConnectionHelper.close(rs);
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
	
//	public int insertLeave(Long empId, LocalDate d, int lt){
//		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
//		PreparedStatement pstmt = null;
//		int result =-1;
//		String sql = 	"INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed ) "
//				+ 		"values(?,?,?,?,?)";
//		String sql2 = 	"INSERT INTO missing_punch (emp_id, work_date, missing_reason_id) "
//				+ 		"values(?,?,?)";
//
//		try {
//			conn.setAutoCommit(false); //트랜잭션 위함
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setLong(1, empId);
//			pstmt.setDate(2,  Date.valueOf(d));
//			pstmt.setDate(3,  Date.valueOf(d));
//			pstmt.setDate(4,  Date.valueOf(d));
//			pstmt.setString(5, "N");
//			result = pstmt.executeUpdate();
//			
//			pstmt = conn.prepareStatement(sql2);
//			pstmt.setLong(1, empId);
//			pstmt.setDate(2,  Date.valueOf(d));
//			pstmt.setInt(3, lt);
//			result = pstmt.executeUpdate();
//			
//			conn.commit();
//			return result;	
//		}catch(SQLIntegrityConstraintViolationException e) {
//			return -2;
//		}catch (SQLException e) {
//			e.printStackTrace();
//			try {
//				conn.rollback();
//			} catch (SQLException e1) {
//				e1.printStackTrace();
//			}
//			return result;
//		}finally {
//			ConnectionHelper.close(pstmt);
//			ConnectionHelper.close(conn);
//		}
//	}
	
	public int insertLeaveRange(Long empId, List<LocalDate> dates, int reasonId) {
	    Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
	    PreparedStatement pstmt1 = null;
	    PreparedStatement pstmt2 = null;

	    int total = 0;

	    String sql1 = "INSERT INTO attendance ("
	            + "emp_id, "
	            + "work_date, "
	            + "on_work_time, "
	            + "off_work_time, "
	            + "is_closed"
	            + ") VALUES (?, ?, NULL, NULL, ?)";

	    String sql2 = "INSERT INTO missing_punch ("
	            + "emp_id, "
	            + "work_date, "
	            + "missing_reason_id"
	            + ") VALUES (?, ?, ?)";

	    try {
	        conn.setAutoCommit(false);

	        pstmt1 = conn.prepareStatement(sql1);
	        pstmt2 = conn.prepareStatement(sql2);

	        for (LocalDate date : dates) {
	        	pstmt1.setLong(1, empId);
	        	pstmt1.setDate(2, Date.valueOf(date));
	        	pstmt1.setString(3, "N");
	            total += pstmt1.executeUpdate();

	            pstmt2.setLong(1, empId);
	            pstmt2.setDate(2, Date.valueOf(date));
	            pstmt2.setInt(3, reasonId);
	            total += pstmt2.executeUpdate();
	        }

	        conn.commit();
	        return total;

	    } catch (SQLIntegrityConstraintViolationException e) {
	        try {
	            conn.rollback();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        return -2;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        try {
	            conn.rollback();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        return -1;
	    } finally {
	        ConnectionHelper.close(pstmt2);
	        ConnectionHelper.close(pstmt1);
	        ConnectionHelper.close(conn);
	    }
	}
	
	public int deleteLeave(Long empId, LocalDate d) {
		Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
		PreparedStatement pstmt = null;
		int result =-1;
		String sql = 	"DELETE FROM missing_punch"
				+ 		"WHERE emp_id=? AND work_date=?";
		String sql2 = 	"DELETE FROM attendance"
				+ 		"WHERE emp_id=? AND work_date=?";

		try {
			conn.setAutoCommit(false); //트랜잭션 위함
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, empId);
			pstmt.setDate(2,  Date.valueOf(d));
			result = pstmt.executeUpdate();
			
			pstmt = conn.prepareStatement(sql2);
			pstmt.setLong(1, empId);
			pstmt.setDate(2, Date.valueOf(d));
			result = pstmt.executeUpdate();
			
			conn.commit();
			return result;	
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return result;
		}finally {
			ConnectionHelper.close(pstmt);
			ConnectionHelper.close(conn);
		}
	}
}
