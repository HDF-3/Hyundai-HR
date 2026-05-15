package leave.dto;

import global.types.CommonStatus;
import global.types.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestDTO {
    private Long leaveRequestId;
    private Long employeeId;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private CommonStatus status;

}
