package leave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnualLeaveDTO {
    private Long annualLeaveId;
    private Long employeeId;
    private LocalDate grantedAt;
    private LocalDate expiredAt;
    private double grantedAnnualLeave;
    private double usedAnnualLeave;
    private char isActive;
    private double remainingAnnualLeave;
}
