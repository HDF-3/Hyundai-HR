package attendance.service;

import attendance.dao.AttendanceDAO;
import attendance.dto.RequestWorkTimeDTO;

public class AttendanceService {
	private AttendanceDAO attendanceDAO;
	
	public AttendanceService() {
		attendanceDAO = new AttendanceDAO();
	}
	
	//개인별 유연근무시간 등록
	public int registerWorkTime(RequestWorkTimeDTO reqTime) {
		return attendanceDAO.insertWorkTime(reqTime);
	}
}
