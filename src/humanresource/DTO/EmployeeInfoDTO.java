package humanresource.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class EmployeeInfoDTO {
    private Long empId;
    private String eName;
    private String deptName;
    private String positionName;
    private int payGrade;
    private LocalDate hireDate;
    private String gender;
    private String contact;
    private String email;
    private String address;

}
