package attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MissingAttendanceDTO extends BaseAttendanceDTO{
	public MissingAttendanceDTO(Long empId, LocalDate ld, LocalTime t1, LocalTime t2, String closed, String missingType, String missingReason){
		this.setEmpId(empId);
		this.setWorkDate(ld);
		this.setOnWorkTime(t1);
		this.setOffWorkTime(t2);
		this.setClosed(closed);
		this.setMissingType(missingType);
		this.setMissingReason(missingReason);
	}
	private LocalTime onWorkTime;
	private LocalTime offWorkTime;
	private String missingType;
	private String missingReason;
}
