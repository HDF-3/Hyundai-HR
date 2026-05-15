package humanresource.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class AssignmentHistoryDTO {
    private Long historyId;
    private String eName;
    private String deptName;
    private String positionName;
    private int payGrade;
    private String reasonName;
    private LocalDate startDate;
    private LocalDate endDate;
}
