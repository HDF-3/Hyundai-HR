package attendance.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import attendance.dto.AttendanceModifyHistoryDTO;
import global.types.CommonStatus;
import global.types.DBType;
import global.utils.ConnectionHelper;

public class AttendanceModifyDAO {
	private static final String BASE_SQL = """
		    SELECT 
		        acr.attendance_change_request_id AS mod_history_id,
		        acr.cancel_req_id,
		        acr.emp_id,
		        acr.work_date,
		        acr.STATUS AS req_state,
		        acr.is_on_work_time_modified,
		        acr.is_off_work_time_modified,
		        citc.time_old AS old_clock_in_time,
		        citc.time_new AS new_clock_in_time,
		        cotc.time_old AS old_clock_out_time,
		        cotc.time_new AS new_clock_out_time
		    FROM attendance_change_request acr
		    LEFT JOIN clock_in_time_change citc
		        ON acr.attendance_change_request_id = citc.attendance_change_request_id
		    LEFT JOIN clock_out_time_change cotc
		        ON acr.attendance_change_request_id = cotc.attendance_change_request_id
		""";

	public int insertAttendanceModifyReq(AttendanceModifyHistoryDTO req) {
	    int result = -1;

	    try (Connection conn = ConnectionHelper.getConnection(DBType.ORACLE)) {
	        conn.setAutoCommit(false);

	        try {
	            AttendanceInfo attendanceInfo = findAttendanceInfo(conn, req.getEmpId(), req.getReqDate());
	            if (attendanceInfo == null) {
	                conn.rollback();
	                return -2;
	            }
	            
	            if (!attendanceInfo.isOpen()) {
	                conn.rollback();
	                return -3;
	            }

	            boolean isOnWorkTimeModified = req.getOnWorkTimeNew() != null;
	            boolean isOffWorkTimeModified = req.getOffWorkTimeNew() != null;

	            if (!isOnWorkTimeModified && !isOffWorkTimeModified) {
	                conn.rollback();
	                return 0;
	            }

	            if (isOnWorkTimeModified && req.getOnWorkTimeOld() == null) {
	                req.setOnWorkTimeOld(attendanceInfo.onWorkTime);
	            }

	            if (isOffWorkTimeModified && req.getOffWorkTimeOld() == null) {
	                req.setOffWorkTimeOld(attendanceInfo.offWorkTime);
	            }

	            CommonStatus reqState = req.getReqState() == null ? CommonStatus.PENDING : req.getReqState();
	            long requestId = insertAttendanceChangeRequest(
	                conn,
	                req.getEmpId(),
	                req.getReqDate(),
	                reqState,
	                isOnWorkTimeModified,
	                isOffWorkTimeModified,
	                req.getCancelReqId()
	            );

	            result = 1;

	            if (isOnWorkTimeModified) {
	                result += insertClockInTimeChange(
	                    conn,
	                    requestId,
	                    req.getReqDate(),
	                    req.getOnWorkTimeOld(),
	                    req.getOnWorkTimeNew()
	                );
	            }

	            if (isOffWorkTimeModified) {
	                result += insertClockOutTimeChange(
	                    conn,
	                    requestId,
	                    req.getReqDate(),
	                    req.getOffWorkTimeOld(),
	                    req.getOffWorkTimeNew()
	                );
	            }

	            req.setModHistoryId(requestId);
	            req.setReqState(reqState);

	            conn.commit();
	            return result;
	        } catch (SQLException e) {
	            conn.rollback();
	            throw e;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return result;
	}

	public int insertAttendanceModifyCancelReq(Long targetReqId) {
	    int result = -1;

	    try (Connection conn = ConnectionHelper.getConnection(DBType.ORACLE)) {
	        conn.setAutoCommit(false);

	        try {
	            if (targetReqId == null) {
	                conn.rollback();
	                return -2;
	            }

	            AttendanceModifyHistoryDTO targetReq = findAttendanceModifyReqById(conn, targetReqId);
	            if (targetReq == null || targetReq.getCancelReqId() != null) {
	                conn.rollback();
	                return -2;
	            }

	            AttendanceInfo attendanceInfo = findAttendanceInfo(conn, targetReq.getEmpId(), targetReq.getReqDate());
	            if (attendanceInfo == null) {
	                conn.rollback();
	                return -2;
	            }

	            if (!attendanceInfo.isOpen()) {
	                conn.rollback();
	                return -3;
	            }

	            CommonStatus latestState = findLatestStateOfSameChange(conn, targetReq);
	            if (latestState != CommonStatus.PENDING) {
	                conn.rollback();
	                return -4;
	            }

	            boolean isOnWorkTimeModified = targetReq.getOnWorkTimeNew() != null;
	            boolean isOffWorkTimeModified = targetReq.getOffWorkTimeNew() != null;

	            long cancelRequestId = insertAttendanceChangeRequest(
	                conn,
	                targetReq.getEmpId(),
	                targetReq.getReqDate(),
	                CommonStatus.PENDING,
	                isOnWorkTimeModified,
	                isOffWorkTimeModified,
	                targetReq.getModHistoryId()
	            );

	            result = 1;

	            if (isOnWorkTimeModified) {
	                result += insertClockInTimeChange(
	                    conn,
	                    cancelRequestId,
	                    targetReq.getReqDate(),
	                    targetReq.getOnWorkTimeOld(),
	                    targetReq.getOnWorkTimeNew()
	                );
	            }

	            if (isOffWorkTimeModified) {
	                result += insertClockOutTimeChange(
	                    conn,
	                    cancelRequestId,
	                    targetReq.getReqDate(),
	                    targetReq.getOffWorkTimeOld(),
	                    targetReq.getOffWorkTimeNew()
	                );
	            }

	            conn.commit();
	            return result;
	        } catch (SQLException e) {
	            conn.rollback();
	            throw e;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return result;
	}

	public int insertAttendanceModifyReqState(Long modHistoryId, CommonStatus state) {
	    int result = -1;

	    try (Connection conn = ConnectionHelper.getConnection(DBType.ORACLE)) {
	        conn.setAutoCommit(false);

	        try {
	            AttendanceModifyHistoryDTO req = findAttendanceModifyReqById(conn, modHistoryId);
	            if (req == null || state == null) {
	                conn.rollback();
	                return -2;
	            }
	            
	            AttendanceInfo attendanceInfo = findAttendanceInfo(conn, req.getEmpId(), req.getReqDate());
	            if (attendanceInfo == null) {
	                conn.rollback();
	                return -2;
	            }

	            if (!attendanceInfo.isOpen()) {
	                conn.rollback();
	                return -3;
	            }
	            
	            if (req.getCancelReqId() != null && state == CommonStatus.APPROVED) {
	                AttendanceModifyHistoryDTO targetReq = findAttendanceModifyReqById(conn, req.getCancelReqId());
	                if (targetReq == null) {
	                    conn.rollback();
	                    return -2;
	                }
	                
	                CommonStatus latestState = findLatestStateOfSameChange(conn, targetReq);
	                if (latestState != CommonStatus.PENDING) {
	                    conn.rollback();
	                    return -4;
	                }
	            }
	            
	            if (req.getCancelReqId() == null && state == CommonStatus.APPROVED) {
	                CommonStatus latestState = findLatestStateOfSameChange(conn, req);
	                if (latestState != CommonStatus.PENDING) {
	                    conn.rollback();
	                    return -4;
	                }
	            }

	            boolean isOnWorkTimeModified = req.getOnWorkTimeNew() != null;
	            boolean isOffWorkTimeModified = req.getOffWorkTimeNew() != null;

	            long requestId = insertAttendanceChangeRequest(
	                conn,
	                req.getEmpId(),
	                req.getReqDate(),
	                state,
	                isOnWorkTimeModified,
	                isOffWorkTimeModified,
	                req.getCancelReqId()
	            );

	            result = 1;

	            if (isOnWorkTimeModified) {
	                result += insertClockInTimeChange(
	                    conn,
	                    requestId,
	                    req.getReqDate(),
	                    req.getOnWorkTimeOld(),
	                    req.getOnWorkTimeNew()
	                );
	            }

	            if (isOffWorkTimeModified) {
	                result += insertClockOutTimeChange(
	                    conn,
	                    requestId,
	                    req.getReqDate(),
	                    req.getOffWorkTimeOld(),
	                    req.getOffWorkTimeNew()
	                );
	            }
	            
	            if (req.getCancelReqId() != null && state == CommonStatus.APPROVED) {
	                result += insertCanceledStateForTargetReq(conn, req.getCancelReqId());
	            }
	            
	            if (req.getCancelReqId() == null && state == CommonStatus.APPROVED) {
	                int attendanceUpdateResult = updateAttendanceTime(conn, req);
	                if (attendanceUpdateResult != 1) {
	                    conn.rollback();
	                    return -3;
	                }
	                
	                result += attendanceUpdateResult;
	            }

	            conn.commit();
	            return result;
	        } catch (SQLException e) {
	            conn.rollback();
	            throw e;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return result;
	}
	
	public List<AttendanceModifyHistoryDTO> findAttendanceModifyReq(Long empId) {
	    List<AttendanceModifyHistoryDTO> list = new ArrayList<>();

	    String sql = BASE_SQL + """
	        WHERE acr.emp_id = ?
	        ORDER BY acr.work_date DESC
	    """;

	    try (
	        Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
	        PreparedStatement pstmt = conn.prepareStatement(sql)
	    ) {
	        pstmt.setLong(1, empId);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                list.add(mapAttendanceModifyHistory(rs));
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}
	
	public List<AttendanceModifyHistoryDTO> findAttendanceModifyReq(LocalDate startDate, LocalDate endDate) {
	    List<AttendanceModifyHistoryDTO> list = new ArrayList<>();

	    String sql = BASE_SQL + """
	        WHERE acr.work_date BETWEEN ? AND ?
	        ORDER BY acr.work_date DESC
	    """;

	    try (
	        Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
	        PreparedStatement pstmt = conn.prepareStatement(sql)
	    ) {
	        pstmt.setDate(1, Date.valueOf(startDate));
	        pstmt.setDate(2, Date.valueOf(endDate));

	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                list.add(mapAttendanceModifyHistory(rs));
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}
	
	public List<AttendanceModifyHistoryDTO> findAttendanceModifyReq(Long empId, LocalDate startDate, LocalDate endDate) {
	    List<AttendanceModifyHistoryDTO> list = new ArrayList<>();

	    String sql = BASE_SQL + """
	        WHERE acr.emp_id = ?
	          AND acr.work_date BETWEEN ? AND ?
	        ORDER BY acr.work_date DESC
	    """;

	    try (
	        Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
	        PreparedStatement pstmt = conn.prepareStatement(sql)
	    ) {
	        pstmt.setLong(1, empId);
	        pstmt.setDate(2, Date.valueOf(startDate));
	        pstmt.setDate(3, Date.valueOf(endDate));

	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                list.add(mapAttendanceModifyHistory(rs));
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}
	
	public List<AttendanceModifyHistoryDTO> findAttendanceModifyReq(CommonStatus state) {
	    List<AttendanceModifyHistoryDTO> list = new ArrayList<>();

	    String sql = BASE_SQL + """
	        WHERE acr.STATUS = ?
	        ORDER BY acr.work_date DESC
	    """;

	    try (
	        Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
	        PreparedStatement pstmt = conn.prepareStatement(sql)
	    ) {
	        pstmt.setString(1, state.name());

	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                list.add(mapAttendanceModifyHistory(rs));
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return list;
	}
	
	private AttendanceModifyHistoryDTO mapAttendanceModifyHistory(ResultSet rs) throws SQLException {
	    AttendanceModifyHistoryDTO dto = new AttendanceModifyHistoryDTO();

	    dto.setModHistoryId(rs.getLong("mod_history_id"));
	    long cancelReqId = rs.getLong("cancel_req_id");
	    dto.setCancelReqId(rs.wasNull() ? null : cancelReqId);
	    dto.setEmpId(rs.getLong("emp_id"));

	    Date workDate = rs.getDate("work_date");
	    dto.setReqDate(workDate != null ? workDate.toLocalDate() : null);

	    String reqState = rs.getString("req_state");
	    dto.setReqState(reqState != null ? CommonStatus.valueOf(reqState) : null);

	    if ("Y".equals(rs.getString("is_on_work_time_modified"))) {
	        Timestamp oldIn = rs.getTimestamp("old_clock_in_time");
	        Timestamp newIn = rs.getTimestamp("new_clock_in_time");

	        dto.setOnWorkTimeOld(oldIn != null ? oldIn.toLocalDateTime().toLocalTime() : null);
	        dto.setOnWorkTimeNew(newIn != null ? newIn.toLocalDateTime().toLocalTime() : null);
	    }

	    if ("Y".equals(rs.getString("is_off_work_time_modified"))) {
	        Timestamp oldOut = rs.getTimestamp("old_clock_out_time");
	        Timestamp newOut = rs.getTimestamp("new_clock_out_time");

	        dto.setOffWorkTimeOld(oldOut != null ? oldOut.toLocalDateTime().toLocalTime() : null);
	        dto.setOffWorkTimeNew(newOut != null ? newOut.toLocalDateTime().toLocalTime() : null);
	    }

	    return dto;
	}

	private AttendanceModifyHistoryDTO findAttendanceModifyReqById(Connection conn, Long modHistoryId) throws SQLException {
	    String sql = BASE_SQL + """
	        WHERE acr.attendance_change_request_id = ?
	    """;

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setLong(1, modHistoryId);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return mapAttendanceModifyHistory(rs);
	            }
	        }
	    }

	    return null;
	}

	private AttendanceInfo findAttendanceInfo(Connection conn, Long empId, LocalDate workDate) throws SQLException {
	    String sql = """
	        SELECT on_work_time, off_work_time, is_closed
	        FROM attendance
	        WHERE emp_id = ?
	          AND work_date = ?
	    """;

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setLong(1, empId);
	        pstmt.setDate(2, Date.valueOf(workDate));

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                Timestamp onWorkTime = rs.getTimestamp("on_work_time");
	                Timestamp offWorkTime = rs.getTimestamp("off_work_time");

	                return new AttendanceInfo(
	                    onWorkTime != null ? onWorkTime.toLocalDateTime().toLocalTime() : null,
	                    offWorkTime != null ? offWorkTime.toLocalDateTime().toLocalTime() : null,
	                    rs.getString("is_closed")
	                );
	            }
	        }
	    }

	    return null;
	}

	private long insertAttendanceChangeRequest(
	    Connection conn,
	    Long empId,
	    LocalDate workDate,
	    CommonStatus state,
	    boolean isOnWorkTimeModified,
	    boolean isOffWorkTimeModified,
	    Long cancelReqId
	) throws SQLException {
	    String sql = """
	        INSERT INTO attendance_change_request (
	            emp_id,
	            work_date,
	            STATUS,
	            is_on_work_time_modified,
	            is_off_work_time_modified,
	            cancel_req_id
	        )
	        VALUES (?, ?, ?, ?, ?, ?)
	    """;

	    try (PreparedStatement pstmt = conn.prepareStatement(
	        sql,
	        new String[] { "ATTENDANCE_CHANGE_REQUEST_ID" }
	    )) {
	        pstmt.setLong(1, empId);
	        pstmt.setDate(2, Date.valueOf(workDate));
	        pstmt.setString(3, state.name());
	        pstmt.setString(4, isOnWorkTimeModified ? "Y" : "N");
	        pstmt.setString(5, isOffWorkTimeModified ? "Y" : "N");
	        if (cancelReqId == null) {
	            pstmt.setNull(6, Types.NUMERIC);
	        } else {
	            pstmt.setLong(6, cancelReqId);
	        }

	        pstmt.executeUpdate();

	        try (ResultSet rs = pstmt.getGeneratedKeys()) {
	            if (rs.next()) {
	                return rs.getLong(1);
	            }
	        }
	    }

	    return findLatestAttendanceChangeRequestId(conn, empId, workDate, state);
	}

	private long findLatestAttendanceChangeRequestId(
	    Connection conn,
	    Long empId,
	    LocalDate workDate,
	    CommonStatus state
	) throws SQLException {
	    String sql = """
	        SELECT attendance_change_request_id
	        FROM attendance_change_request
	        WHERE emp_id = ?
	          AND work_date = ?
	          AND STATUS = ?
	        ORDER BY attendance_change_request_id DESC
	        FETCH FIRST 1 ROW ONLY
	    """;

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setLong(1, empId);
	        pstmt.setDate(2, Date.valueOf(workDate));
	        pstmt.setString(3, state.name());

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getLong("attendance_change_request_id");
	            }
	        }
	    }

	    throw new SQLException("attendance_change_request_id를 조회하지 못했습니다.");
	}

	private CommonStatus findLatestStateOfSameChange(Connection conn, AttendanceModifyHistoryDTO req)
	    throws SQLException {
	    boolean isOnWorkTimeModified = req.getOnWorkTimeNew() != null;
	    boolean isOffWorkTimeModified = req.getOffWorkTimeNew() != null;

	    StringBuilder sql = new StringBuilder("""
	        SELECT acr.STATUS
	        FROM attendance_change_request acr
	        LEFT JOIN clock_in_time_change citc
	            ON acr.attendance_change_request_id = citc.attendance_change_request_id
	        LEFT JOIN clock_out_time_change cotc
	            ON acr.attendance_change_request_id = cotc.attendance_change_request_id
	        WHERE acr.emp_id = ?
	          AND acr.work_date = ?
	          AND acr.cancel_req_id IS NULL
	          AND acr.is_on_work_time_modified = ?
	          AND acr.is_off_work_time_modified = ?
	    """);

	    if (isOnWorkTimeModified) {
	        sql.append(" AND citc.time_old = ? AND citc.time_new = ? ");
	    } else {
	        sql.append(" AND citc.attendance_change_request_id IS NULL ");
	    }

	    if (isOffWorkTimeModified) {
	        sql.append(" AND cotc.time_old = ? AND cotc.time_new = ? ");
	    } else {
	        sql.append(" AND cotc.attendance_change_request_id IS NULL ");
	    }

	    sql.append("""
	        ORDER BY acr.attendance_change_request_id DESC
	        FETCH FIRST 1 ROW ONLY
	    """);

	    try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
	        int index = 1;
	        pstmt.setLong(index++, req.getEmpId());
	        pstmt.setDate(index++, Date.valueOf(req.getReqDate()));
	        pstmt.setString(index++, isOnWorkTimeModified ? "Y" : "N");
	        pstmt.setString(index++, isOffWorkTimeModified ? "Y" : "N");

	        if (isOnWorkTimeModified) {
	            setTimestamp(pstmt, index++, req.getReqDate(), req.getOnWorkTimeOld());
	            setTimestamp(pstmt, index++, req.getReqDate(), req.getOnWorkTimeNew());
	        }

	        if (isOffWorkTimeModified) {
	            setTimestamp(pstmt, index++, req.getReqDate(), req.getOffWorkTimeOld());
	            setTimestamp(pstmt, index++, req.getReqDate(), req.getOffWorkTimeNew());
	        }

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return CommonStatus.valueOf(rs.getString("STATUS"));
	            }
	        }
	    }

	    return req.getReqState();
	}

	private int insertCanceledStateForTargetReq(Connection conn, Long targetReqId) throws SQLException {
	    AttendanceModifyHistoryDTO targetReq = findAttendanceModifyReqById(conn, targetReqId);
	    if (targetReq == null) {
	        return 0;
	    }

	    boolean isOnWorkTimeModified = targetReq.getOnWorkTimeNew() != null;
	    boolean isOffWorkTimeModified = targetReq.getOffWorkTimeNew() != null;

	    long canceledRequestId = insertAttendanceChangeRequest(
	        conn,
	        targetReq.getEmpId(),
	        targetReq.getReqDate(),
	        CommonStatus.CANCELED,
	        isOnWorkTimeModified,
	        isOffWorkTimeModified,
	        null
	    );

	    int result = 1;

	    if (isOnWorkTimeModified) {
	        result += insertClockInTimeChange(
	            conn,
	            canceledRequestId,
	            targetReq.getReqDate(),
	            targetReq.getOnWorkTimeOld(),
	            targetReq.getOnWorkTimeNew()
	        );
	    }

	    if (isOffWorkTimeModified) {
	        result += insertClockOutTimeChange(
	            conn,
	            canceledRequestId,
	            targetReq.getReqDate(),
	            targetReq.getOffWorkTimeOld(),
	            targetReq.getOffWorkTimeNew()
	        );
	    }

	    return result;
	}

	private int insertClockInTimeChange(
	    Connection conn,
	    Long requestId,
	    LocalDate workDate,
	    LocalTime oldTime,
	    LocalTime newTime
	) throws SQLException {
	    String sql = """
	        INSERT INTO clock_in_time_change (
	            attendance_change_request_id,
	            time_old,
	            time_new
	        )
	        VALUES (?, ?, ?)
	    """;

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setLong(1, requestId);
	        setTimestamp(pstmt, 2, workDate, oldTime);
	        setTimestamp(pstmt, 3, workDate, newTime);

	        return pstmt.executeUpdate();
	    }
	}

	private int insertClockOutTimeChange(
	    Connection conn,
	    Long requestId,
	    LocalDate workDate,
	    LocalTime oldTime,
	    LocalTime newTime
	) throws SQLException {
	    String sql = """
	        INSERT INTO clock_out_time_change (
	            attendance_change_request_id,
	            time_old,
	            time_new
	        )
	        VALUES (?, ?, ?)
	    """;

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setLong(1, requestId);
	        setTimestamp(pstmt, 2, workDate, oldTime);
	        setTimestamp(pstmt, 3, workDate, newTime);

	        return pstmt.executeUpdate();
	    }
	}

	private int updateAttendanceTime(Connection conn, AttendanceModifyHistoryDTO req) throws SQLException {
	    boolean isOnWorkTimeModified = req.getOnWorkTimeNew() != null;
	    boolean isOffWorkTimeModified = req.getOffWorkTimeNew() != null;

	    if (isOnWorkTimeModified && isOffWorkTimeModified) {
	        String sql = """
	            UPDATE attendance
	            SET on_work_time = ?,
	                off_work_time = ?
	            WHERE emp_id = ?
	              AND work_date = ?
	              AND is_closed = 'N'
	        """;

	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            setTimestamp(pstmt, 1, req.getReqDate(), req.getOnWorkTimeNew());
	            setTimestamp(pstmt, 2, req.getReqDate(), req.getOffWorkTimeNew());
	            pstmt.setLong(3, req.getEmpId());
	            pstmt.setDate(4, Date.valueOf(req.getReqDate()));

	            return pstmt.executeUpdate();
	        }
	    }

	    if (isOnWorkTimeModified) {
	        String sql = """
	            UPDATE attendance
	            SET on_work_time = ?
	            WHERE emp_id = ?
	              AND work_date = ?
	              AND is_closed = 'N'
	        """;

	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            setTimestamp(pstmt, 1, req.getReqDate(), req.getOnWorkTimeNew());
	            pstmt.setLong(2, req.getEmpId());
	            pstmt.setDate(3, Date.valueOf(req.getReqDate()));

	            return pstmt.executeUpdate();
	        }
	    }

	    if (isOffWorkTimeModified) {
	        String sql = """
	            UPDATE attendance
	            SET off_work_time = ?
	            WHERE emp_id = ?
	              AND work_date = ?
	              AND is_closed = 'N'
	        """;

	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            setTimestamp(pstmt, 1, req.getReqDate(), req.getOffWorkTimeNew());
	            pstmt.setLong(2, req.getEmpId());
	            pstmt.setDate(3, Date.valueOf(req.getReqDate()));

	            return pstmt.executeUpdate();
	        }
	    }

	    return 0;
	}

	private void setTimestamp(PreparedStatement pstmt, int parameterIndex, LocalDate date, LocalTime time)
	    throws SQLException {
	    if (time == null) {
	        pstmt.setNull(parameterIndex, Types.TIMESTAMP);
	        return;
	    }

	    pstmt.setTimestamp(parameterIndex, Timestamp.valueOf(LocalDateTime.of(date, time)));
	}

	private static class AttendanceInfo {
	    private final LocalTime onWorkTime;
	    private final LocalTime offWorkTime;
	    private final String isClosed;

	    private AttendanceInfo(LocalTime onWorkTime, LocalTime offWorkTime, String isClosed) {
	        this.onWorkTime = onWorkTime;
	        this.offWorkTime = offWorkTime;
	        this.isClosed = isClosed;
	    }

	    private boolean isOpen() {
	        return "N".equals(isClosed);
	    }
	}
}
