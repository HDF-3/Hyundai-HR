package attendance.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseAttendanceDTO {
	private Long empId;
	private LocalDate workDate;
	private String closed;
}
