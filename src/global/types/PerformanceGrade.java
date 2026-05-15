package global.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PerformanceGrade {
    S("S", "탁월"),
    A("A", "우수"),
    B("B", "보통"),
    C("C", "개선필요"),
    D("D", "미흡");

    private final String code;
    private final String description;

    public static PerformanceGrade fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PerformanceGrade grade : values()) {
            if (grade.getCode().equalsIgnoreCase(code.trim())) {
                return grade;
            }
        }
        try {
            return fromCode(Integer.parseInt(code.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static PerformanceGrade fromCode(int code) {
        switch (code) {
            case 1:
                return S;
            case 2:
                return B;
            case 3:
                return D;
            default:
                return null;
        }
    }
}
