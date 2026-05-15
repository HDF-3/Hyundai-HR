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
public class EmployeeDTO {
    private Long empId;
    private Long deptId;
    private Long positionId;
    private global.types.EmploymentStatus statusId;
    private String ename;
    private LocalDate hireDate;
    private LocalDate resignDate;
    private String contact;
    private String gender;
    private String email;
    private String address;
    private String salAccount;
    private int payGrade;
    private String password;
    private Boolean isAdmin;

}
