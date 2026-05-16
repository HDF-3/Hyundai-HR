package attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NormalAttendanceDTO extends BaseAttendanceDTO{
	public NormalAttendanceDTO(Long empId, LocalDate ld, LocalTime t1, LocalTime t2, String closed){
		this.setEmpId(empId);
		this.setWorkDate(ld);
		this.setOnWorkTime(t1);
		this.setOffWorkTime(t2);
		this.setClosed(closed);
	}
	private LocalTime onWorkTime;
	private LocalTime offWorkTime;
}
