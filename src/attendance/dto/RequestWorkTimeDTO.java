package attendance.dto;

import java.time.LocalTime;
import java.time.YearMonth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestWorkTimeDTO {
	private Long empId;
	private YearMonth appliedMonth;
	private LocalTime onWorkTime;
	private LocalTime offWorkTime;
}
