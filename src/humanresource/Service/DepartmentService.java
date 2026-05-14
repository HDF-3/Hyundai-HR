package humanresource.Service;

public class DepartmentService {
    private final humanresource.DAO.DepartmentDAO departmentDAO;

    public DepartmentService() {
        this.departmentDAO = new humanresource.DAO.DepartmentDAO();
    }

    /**
     * 새로운 부서를 등록합니다.
     * @param departmentDTO 등록할 부서 정보
     * @return 성공 시 1, 실패 시 0
     */
    public int registerDepartment(humanresource.DTO.DepartmentDTO departmentDTO) {
        return departmentDAO.insertDepartment(departmentDTO);
    }

    /**
     * 부서를 삭제합니다. 단, 해당 부서에 소속된 직원이 없어야 합니다.
     * @param deptId 삭제할 부서 ID
     * @return 성공 시 1, 실패 시 0
     * @throws IllegalStateException 부서에 직원이 남아있는 경우 발생
     */
    public int removeDepartment(Long deptId) {
        int employeeCount = departmentDAO.countEmployeesInDept(deptId);
        if (employeeCount > 0) {
            throw new IllegalStateException("부서에 소속된 직원이 존재하여 삭제할 수 없습니다. (직원 수: " + employeeCount + ")");
        }
        return departmentDAO.deleteDepartment(deptId);
    }

    /**
     * 모든 부서 목록을 조회합니다.
     * @return 부서 정보 리스트
     */
    public java.util.List<humanresource.DTO.DepartmentDTO> getAllDepartments() {
        return departmentDAO.selectAllDepartment();
    }

    /**
     * 특정 부서의 현재 인원수를 확인합니다.
     * @param deptId 부서 ID
     * @return 소속 직원 수
     */
    public int getEmployeeCount(Long deptId) {
        return departmentDAO.countEmployeesInDept(deptId);
    }

}
