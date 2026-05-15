package global.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PerformanceGrade {
    EXCELLENT(1, "상"),
    GOOD(2, "중"),
    POOR(3, "하");

    private final int code;
    private final String description;

    public static PerformanceGrade fromCode(int code) {
        for (PerformanceGrade grade : values()) {
            if (grade.getCode() == code) {
                return grade;
            }
        }
        return null; 
    }
}