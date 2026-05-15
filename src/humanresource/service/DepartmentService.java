package humanresource.service;

public class DepartmentService {
    private final humanresource.dao.DepartmentDAO departmentDAO;

    public DepartmentService() {
        this.departmentDAO = new humanresource.dao.DepartmentDAO();
    }

    public int registerDepartment(humanresource.dto.DepartmentDTO departmentDTO) {
        return departmentDAO.insertDepartment(departmentDTO);
    }

    public int removeDepartment(Long deptId) {
        int employeeCount = getEmployeeCount(deptId);
               if (employeeCount > 0) {
            throw new IllegalStateException("부서에 소속된 직원이 존재하여 삭제할 수 없습니다. (직원 수: " + employeeCount + ")");
        }
        return departmentDAO.deleteDepartment(deptId);
    }

    public java.util.List<humanresource.dto.DepartmentDTO> getAllDepartments() {
        return departmentDAO.selectAllDepartment();
    }

    public int getEmployeeCount(Long deptId) {
        return departmentDAO.countEmployeesInDept(deptId);
    }

}