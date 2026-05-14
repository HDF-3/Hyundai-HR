package attendance.service;

import java.time.Duration;
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
	
	//본인 유연근무시간 등록
	public int registerWorkTime(RequestWorkTimeDTO reqTime) {
		long minutes = Duration.between(
			    reqTime.getOnWorkTime(),
			    reqTime.getOffWorkTime()
			).toMinutes();
		if(reqTime.getOffWorkTime().compareTo(reqTime.getOnWorkTime())<0 ) {
			System.out.println("퇴근시간이 출근시간보다 앞설 수 없습니다");
			return -1;
		}else if(minutes < 9 * 60) {
			System.out.println("근무시간 최소는 8시간입니다(휴게시간 1시간 제외)");
			return -1;
		}else if(YearMonth.now().isAfter(reqTime.getAppliedMonth())
				||YearMonth.now().equals(reqTime.getAppliedMonth())) {
			System.out.println("유연근무 신청은 다음달 이후로 신청할 수 있습니다");
			return -1;
		}
		
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
