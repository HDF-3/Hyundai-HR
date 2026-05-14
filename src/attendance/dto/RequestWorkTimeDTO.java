package attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestWorkTimeDTO {
	private Long empId;
	private LocalDate appliedDate;
	private LocalTime onWorkTime;
	private LocalTime offWorkTime;
}
