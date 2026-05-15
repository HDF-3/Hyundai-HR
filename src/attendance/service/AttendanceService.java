package attendance.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
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
	
	//급여부분 필요 메서드
	public OvertimeDTO getOvertimeAmount(Long empId, YearMonth ym) {
		return attendanceDAO.findOvertimeAmount(empId, ym);
	}
	
	//연차/반차/외근 등 미타각 사유 발생 시 실행 메서드
	public int registerLeave(Long empId, LocalDate d, LeaveType lt) {
		int missing_punch_reason_type = -1;
		
		if(LocalDate.now().isAfter(d) 
				||LocalDate.now().equals(d)){
			System.out.println("Leave 신청은 오늘 이후로만 신청이 가능합니다");		
			return -1;
				}
		
		//근태 DB에서 사용하는 형식으로 변환..
		switch(lt) {
			case ANNUAL :
				missing_punch_reason_type=5;
				break;
			case HALF_AM :
				missing_punch_reason_type=9;
				break;
			case HALF_PM :
				missing_punch_reason_type=10;
				break;
			case OUT_SIDE :
				missing_punch_reason_type=2;
				break;
			case SICK :
				missing_punch_reason_type=7;
				break;
			case FAMILY_EVENT :
				missing_punch_reason_type=8;
				break;			
		}
		

		int effectedRowNum = attendanceDAO.insertLeave(empId, d, missing_punch_reason_type);	

		
		
		if(effectedRowNum==-1) {
			System.out.println("신청불가");
			return -1;
		}else if(effectedRowNum==-2) {
			System.out.println("이미 해당 일자에 신청 정보가 존재합니다");
			return -1;
		}
		else {
			System.out.println("정상신청");
			return effectedRowNum;
		}
	}
	
	//연차 취소 메서드 필요
	public int cancelLeave(Long empId, LocalDate d) {
		if(LocalDate.now().isAfter(d) ||LocalDate.now().equals(d)){
			System.out.println("Leave 취소신청은 오늘 이후로만 신청이 가능합니다");		
			return -1;
		}
		
		int effectedRowNum = attendanceDAO.deleteLeave(empId, d);
		
		if(effectedRowNum==0) {
			System.out.println("삭제할 정보가 없습니다");
			return -1;
		}else if(effectedRowNum<0) {
			System.out.println("에러발생");
			return -1;
		}else {
			System.out.println("근태 정보 정상 삭제");
			return effectedRowNum;
		}
	}
}
