package global.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmploymentStatus {
    ACTIVE(1, "재직"),
    ON_LEAVE(2, "휴직"),
    RESIGNED(3, "퇴직");

    private final int code;
    private final String description;


    public static EmploymentStatus fromCode(int code) {
        for (EmploymentStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}