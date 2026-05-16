package attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import attendance.dao.AttendanceModifyDAO;
import attendance.dto.AttendanceModifyHistoryDTO;
import global.types.CommonStatus;

public class AttendanceModifyService {
	private AttendanceModifyDAO attendanceModifyDAO;

	public AttendanceModifyService() {
		attendanceModifyDAO = new AttendanceModifyDAO();
	}

	public int requestAttendanceModify(AttendanceModifyHistoryDTO req) {
		if (req == null) {
			throw new IllegalArgumentException("정정 신청 정보가 없습니다.");
		}
		if (req.getEmpId() == null) {
			throw new IllegalArgumentException("사번은 필수입니다.");
		}
		if (req.getReqDate() == null) {
			throw new IllegalArgumentException("근무일은 필수입니다.");
		}
		if (req.getOnWorkTimeNew() == null && req.getOffWorkTimeNew() == null) {
			throw new IllegalArgumentException("정정할 출근시간 또는 퇴근시간을 입력하세요.");
		}

		req.setReqState(CommonStatus.PENDING);
		return checkedResult(attendanceModifyDAO.insertAttendanceModifyReq(req));
	}

	public int requestOnWorkTimeModify(Long empId, LocalDate workDate, LocalTime newOnWorkTime) {
		AttendanceModifyHistoryDTO req = new AttendanceModifyHistoryDTO();
		req.setEmpId(empId);
		req.setReqDate(workDate);
		req.setOnWorkTimeNew(newOnWorkTime);

		return requestAttendanceModify(req);
	}

	public int requestOffWorkTimeModify(Long empId, LocalDate workDate, LocalTime newOffWorkTime) {
		AttendanceModifyHistoryDTO req = new AttendanceModifyHistoryDTO();
		req.setEmpId(empId);
		req.setReqDate(workDate);
		req.setOffWorkTimeNew(newOffWorkTime);

		return requestAttendanceModify(req);
	}

	public int requestWorkTimeModify(
		Long empId,
		LocalDate workDate,
		LocalTime newOnWorkTime,
		LocalTime newOffWorkTime
	) {
		AttendanceModifyHistoryDTO req = new AttendanceModifyHistoryDTO();
		req.setEmpId(empId);
		req.setReqDate(workDate);
		req.setOnWorkTimeNew(newOnWorkTime);
		req.setOffWorkTimeNew(newOffWorkTime);

		return requestAttendanceModify(req);
	}

	public int requestAttendanceModifyCancel(Long targetRequestId) {
		return checkedResult(attendanceModifyDAO.insertAttendanceModifyCancelReq(targetRequestId));
	}

	public int requestAttendanceModifyCancel(Long requesterEmpId, Long targetRequestId) {
		return checkedResult(attendanceModifyDAO.insertAttendanceModifyCancelReq(requesterEmpId, targetRequestId));
	}

	public int approveAttendanceModifyReq(Long requestId) {
		return checkedResult(attendanceModifyDAO.insertAttendanceModifyReqState(requestId, CommonStatus.APPROVED));
	}

	public int rejectAttendanceModifyReq(Long requestId) {
		return checkedResult(attendanceModifyDAO.insertAttendanceModifyReqState(requestId, CommonStatus.REJECTED));
	}

	public int approveAttendanceModifyCancelReq(Long cancelRequestId) {
		return checkedResult(attendanceModifyDAO.insertAttendanceModifyReqState(cancelRequestId, CommonStatus.APPROVED));
	}

	public int rejectAttendanceModifyCancelReq(Long cancelRequestId) {
		return checkedResult(attendanceModifyDAO.insertAttendanceModifyReqState(cancelRequestId, CommonStatus.REJECTED));
	}

	public List<AttendanceModifyHistoryDTO> getAttendanceModifyReqs(Long empId) {
		return attendanceModifyDAO.findAttendanceModifyReq(empId);
	}

	public List<AttendanceModifyHistoryDTO> getAttendanceModifyReqs(LocalDate startDate, LocalDate endDate) {
		return attendanceModifyDAO.findAttendanceModifyReq(startDate, endDate);
	}

	public List<AttendanceModifyHistoryDTO> getAttendanceModifyReqs(
		Long empId,
		LocalDate startDate,
		LocalDate endDate
	) {
		return attendanceModifyDAO.findAttendanceModifyReq(empId, startDate, endDate);
	}

	public List<AttendanceModifyHistoryDTO> getPendingAttendanceModifyReqs() {
		return attendanceModifyDAO.findAttendanceModifyReq(CommonStatus.PENDING);
	}

	public List<AttendanceModifyHistoryDTO> getApprovedAttendanceModifyReqs() {
		return attendanceModifyDAO.findAttendanceModifyReq(CommonStatus.APPROVED);
	}

	public List<AttendanceModifyHistoryDTO> getRejectedAttendanceModifyReqs() {
		return attendanceModifyDAO.findAttendanceModifyReq(CommonStatus.REJECTED);
	}

	public List<AttendanceModifyHistoryDTO> getCanceledAttendanceModifyReqs() {
		return attendanceModifyDAO.findAttendanceModifyReq(CommonStatus.CANCELED);
	}

	private int checkedResult(int result) {
		if (result > 0) {
			return result;
		}

		switch (result) {
			case 0:
				throw new IllegalArgumentException("정정할 출근시간 또는 퇴근시간을 입력하세요.");
			case -2:
				throw new IllegalStateException("대상 근태 또는 정정 요청을 찾을 수 없습니다.");
			case -3:
				throw new IllegalStateException("마감된 근태는 정정 처리할 수 없습니다.");
			case -4:
				throw new IllegalStateException("이미 처리되었거나 대기 중인 정정 요청입니다.");
			case -5:
				throw new IllegalArgumentException("현재 근태 시간과 동일합니다.");
			case -6:
				throw new IllegalArgumentException("퇴근시간은 출근시간보다 늦어야 합니다.");
			case -7:
				throw new IllegalStateException("취소신청이 대기 중인 정정 요청입니다.");
			case -8:
				throw new IllegalStateException("본인 정정 신청만 취소할 수 있습니다.");
			default:
				throw new IllegalStateException("근태 정정 처리 중 오류가 발생했습니다. (" + result + ")");
		}
	}
}
