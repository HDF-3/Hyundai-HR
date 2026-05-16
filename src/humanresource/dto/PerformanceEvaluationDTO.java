package humanresource.dto;

import global.types.PerformanceGrade;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class PerformanceEvaluationDTO {
    private Long evaluationId;
    private Long targetEmpId;
    private String evaluationYear;
    private Long evaluationQuarter;
    private PerformanceGrade performanceGrade;
}