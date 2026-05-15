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
			return -2;
		}

		req.setReqState(CommonStatus.PENDING);
		return attendanceModifyDAO.insertAttendanceModifyReq(req);
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
		return attendanceModifyDAO.insertAttendanceModifyCancelReq(targetRequestId);
	}

	public int approveAttendanceModifyReq(Long requestId) {
		return attendanceModifyDAO.insertAttendanceModifyReqState(requestId, CommonStatus.APPROVED);
	}

	public int rejectAttendanceModifyReq(Long requestId) {
		return attendanceModifyDAO.insertAttendanceModifyReqState(requestId, CommonStatus.REJECTED);
	}

	public int approveAttendanceModifyCancelReq(Long cancelRequestId) {
		return attendanceModifyDAO.insertAttendanceModifyReqState(cancelRequestId, CommonStatus.APPROVED);
	}

	public int rejectAttendanceModifyCancelReq(Long cancelRequestId) {
		return attendanceModifyDAO.insertAttendanceModifyReqState(cancelRequestId, CommonStatus.REJECTED);
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
}
