package attendance.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import attendance.dao.AttendanceDAO;
import attendance.dto.MissingAttendanceDTO;
import attendance.dto.NormalAttendanceDTO;
import attendance.dto.OvertimeDTO;
import attendance.dto.RequestWorkTimeDTO;

public class AttendanceService {
	private AttendanceDAO attendanceDAO;
	
	public AttendanceService() {
		attendanceDAO = new AttendanceDAO();
	}
	
	public List<NormalAttendanceDTO> findAllAttenDances(Long empId){
		return attendanceDAO.findAllAttenDances(empId);
	}
	
	public List<NormalAttendanceDTO> findAllAttenDances(LocalDate sDate, LocalDate eDate){
		return attendanceDAO.findAllAttenDances(sDate, eDate);
	}
	
	public List<NormalAttendanceDTO> getNormalAttendances(Long empId){
		return attendanceDAO.findNormalAttenDances(empId);
	}
	
	public List<MissingAttendanceDTO> getMissingAttenDances(Long empId){
		return attendanceDAO.findMissingAttenDances(empId);
	}
	
	public List<MissingAttendanceDTO> getMissingAttenDances(LocalDate sDate, LocalDate eDate){
		return attendanceDAO.findMissingAttenDances(sDate, eDate);
	}
	
	public List<NormalAttendanceDTO> getNormalAttendances(LocalDate sDate, LocalDate eDate){
		return attendanceDAO.findNormalAttenDances(sDate, eDate);
	}
	
	public int registerWorkTime(RequestWorkTimeDTO reqTime) {
		return attendanceDAO.insertWorkTime(reqTime);
	}
	
	public int registerToday(Long empId) {
		return attendanceDAO.mergeToday(empId);
	}
	
	//급여부분 필요 메서드
	public boolean getIsClosed(YearMonth ym) {
		if(attendanceDAO.findIsClosed(ym)=='Y') return true;
		else return false;
	}
	
//	public OvertimeDTO getOvertimeAmount(Long empId, YearMonth ym) {
//		attendanceDAO.findOvertimeAmount(empId, ym.getYear(), ym.getMonthValue());
//	}
}
