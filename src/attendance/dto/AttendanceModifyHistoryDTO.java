package attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import global.types.CommonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceModifyHistoryDTO {
	private Long modHistoryId;
	private Long cancelReqId;
	private Long empId;
	private LocalDate reqDate;
	private CommonStatus reqState;
	private LocalTime onWorkTimeOld;
	private LocalTime onWorkTimeNew;
	private LocalTime offWorkTimeOld;
	private LocalTime offWorkTimeNew;
}
