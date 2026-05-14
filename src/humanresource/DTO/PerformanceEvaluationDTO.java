package humanresource.DTO;


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
    private String evaluationYear;
    private int evaluationQuarter;
    private String comment;
    private global.types.PerformanceGrade performanceGrade;
    private Long targetEmpId;
    private Long evaluatorEmpId;
}
