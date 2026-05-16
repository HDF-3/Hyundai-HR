package humanresource.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class DepartmentDTO {
    private Long deptId;
    private String deptName;
    private String deptDesc;
    private Long managerId;
    private Long parentDeptId;

}
