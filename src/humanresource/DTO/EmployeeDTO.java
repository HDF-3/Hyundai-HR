package humanresource.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class EmployeeDTO {
    private Long empId;
    private Long deptId;
    private Long positionId;
    //TODO
    // 재직, 휴가 상태 enum으로 관리하자
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
    //TODO
    // 고과 이력 DTO완성되면 아래 주석 제거할 것
    // private List<PerformanceEvaulationDTO> evalHistory;
    private Boolean isAdmin;

}
