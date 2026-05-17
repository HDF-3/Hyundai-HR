package attendance.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import attendance.dao.AttendanceDAO;
import attendance.dto.MissingAttendanceDTO;
import attendance.dto.NormalAttendanceDTO;
import attendance.dto.OvertimeDTO;
import attendance.dto.RequestWorkTimeDTO;
import global.types.LeaveType;

public class AttendanceService {
	private AttendanceDAO attendanceDAO;
	
	public AttendanceService() {
		attendanceDAO = new AttendanceDAO();
	}
	
	public List<NormalAttendanceDTO> findAllAttenDances(Long empId){
		return attendanceDAO.findAllAttenDances(empId);
	}

	public List<NormalAttendanceDTO> findAllAttenDances(Long empId, LocalDate sDate, LocalDate eDate){
		return attendanceDAO.findAllAttenDances(empId, sDate, eDate);
	}
	
	public List<NormalAttendanceDTO> findAllAttenDances(LocalDate sDate, LocalDate eDate){
		return attendanceDAO.findAllAttenDances(sDate, eDate);
	}
	
	public List<NormalAttendanceDTO> getNormalAttendances(Long empId){
		return attendanceDAO.findNormalAttenDances(empId);
	}

	public List<NormalAttendanceDTO> getNormalAttendances(Long empId, LocalDate sDate, LocalDate eDate){
		return attendanceDAO.findNormalAttenDances(empId, sDate, eDate);
	}
	
	public List<MissingAttendanceDTO> getMissingAttenDances(Long empId){
		return attendanceDAO.findMissingAttenDances(empId);
	}

	public List<MissingAttendanceDTO> getMissingAttenDances(Long empId, LocalDate sDate, LocalDate eDate){
		return attendanceDAO.findMissingAttenDances(empId, sDate, eDate);
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
            throw new RuntimeException("퇴근시간이 출근시간보다 앞설 수 없습니다");
		}else if(minutes < 9 * 60) {
            throw new RuntimeException("근무시간 최소는 8시간입니다(휴게시간 1시간 제외)");
		}else if(YearMonth.now().isAfter(reqTime.getAppliedMonth())
				||YearMonth.now().equals(reqTime.getAppliedMonth())) {
            throw new RuntimeException("유연근무 신청은 다음달 이후로 신청할 수 있습니다");
		}
		
		return attendanceDAO.insertWorkTime(reqTime);
	}
	
	public int registerToday(Long empId) {
		//사전에 신청한 휴가일 도래했을 때 출퇴근 못찍도록 해야함
		//출근 전에 기록이 존재한다는 것은 이미 해당 일자에 leave 신청을 한 것
		if(1==attendanceDAO.findMissingAttenDances(empId, LocalDate.now(), LocalDate.now()).size()) {
            throw new RuntimeException("오늘 일자로 Leave 기록이 존재합니다. 출근하실 수 없습니다");
		}
		return attendanceDAO.mergeToday(empId);
	}
	
	//급여부분 필요 메서드
	public boolean getIsClosed(YearMonth ym) {
		if(attendanceDAO.findIsClosed(ym)=='Y') return true;
		else return false;
	}
	
	//급여부분 필요 메서드
	public OvertimeDTO getOvertimeAmount(Long empId, YearMonth ym) {
		return attendanceDAO.findOvertimeAmount(empId, ym);
	}
	
	//연차/반차/외근 등 미타각 사유 발생 시 실행 메서드
	public int registerLeave(Long empId, LocalDate startDate, LocalDate endDate, LeaveType lt) {
		if(LocalDate.now().isAfter(startDate) || LocalDate.now().equals(startDate)){
            throw new RuntimeException("휴가 신청은 오늘 이후로만 가능합니다");
		}
		if(startDate.isAfter(endDate)){
            throw new RuntimeException("종료일이 시작일보다 빠를 수 없습니다");
		}
		
		int reasonId = convertLeaveTypeToReasonId(lt);

	    List<LocalDate> dates = new ArrayList<>();

	    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
	        DayOfWeek day = date.getDayOfWeek();

	        if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
	            dates.add(date);
	        }
	    }

	    if (dates.isEmpty()) {
	    	throw new RuntimeException("신청 가능한 평일이 없습니다");
	    }

	    int res=attendanceDAO.insertLeaveRange(empId, dates, reasonId);
	    if(res ==-2) {
	    	throw new RuntimeException("해당 일자에 이미 신청 정보가 존재합니다");
	    }
	    if(res ==-1) {
	    	throw new RuntimeException("SQLException 발생");
	    }
	    return res;
	}
	
	//연차 취소 메서드
	/*
	public int cancelLeave(Long empId, LocalDate d) {
		if(LocalDate.now().isAfter(d) || LocalDate.now().equals(d)){
	    	throw new RuntimeException("Leave 취소신청은 오늘 이후로만 신청이 가능합니다");
		}
		
		int effectedRowNum = attendanceDAO.deleteLeave(empId, d);
		
		if(effectedRowNum==0) {
	    	throw new RuntimeException("삭제할 정보가 없습니다");
		}else if(effectedRowNum<0) {
	    	throw new RuntimeException("SQL Exception 발생");
		}else {
			System.out.println("근태 정보 정상 삭제");
			return effectedRowNum;
		}
	}
	*/
	
	private int convertLeaveTypeToReasonId(LeaveType lt) {
	    switch (lt) {
	        case ANNUAL:
	            return 5;
	        case HALF_AM:
	            return 9;
	        case HALF_PM:
	            return 10;
	        case OUT_SIDE:
	            return 2;
	        case SICK:
	            return 7;
	        case FAMILY_EVENT:
	            return 8;
	        default:
	            throw new IllegalArgumentException("지원하지 않는 휴가 유형입니다.");
	    }
	}
}
