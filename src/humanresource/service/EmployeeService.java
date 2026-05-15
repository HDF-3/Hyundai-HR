package humanresource.service;

import humanresource.dto.EmployeeDTO;
import humanresource.dto.EmployeeInfoDTO;
import humanresource.dto.AssignmentHistoryDTO;

import java.time.LocalDate;
import java.util.List;

public class EmployeeService {


    private final humanresource.dao.EmployeeDAO employeeDAO;
    private final humanresource.dao.AssignmentHistoryDAO assignmentHistoryDAO;

    public EmployeeService(){
        this.employeeDAO = new humanresource.dao.EmployeeDAO();
        this.assignmentHistoryDAO = new humanresource.dao.AssignmentHistoryDAO();
    }

    public List<EmployeeDTO> getAllEmployees(){
        return employeeDAO.selectAllEmployees();
    }

    public boolean registerEmployee(humanresource.dto.EmployeeDTO employeeDTO){
        String rawPassword = employeeDTO.getPassword();
        employeeDTO.setPassword(global.utils.PasswordUtils.encrypt(rawPassword));
        return employeeDAO.insertEmployee(employeeDTO) > 0;
    }

    public EmployeeDTO getEmployeeinfo(Long empId){
        return employeeDAO.selectEmployeeById(empId);
    }

    public boolean modifyEmployeeInfo(humanresource.dto.EmployeeDTO employeeDTO){
        return employeeDAO.updateEmployee(employeeDTO) > 0;
    }

    public boolean registerResignationDate(Long empId, LocalDate expectedResignDate){
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false;


        emp.setResignDate(expectedResignDate);

        return employeeDAO.updateEmployee(emp) > 0;
    }
    public boolean promoteEmployee(Long empId, Long positionId){
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false;

        emp.setPositionId(positionId);

        return employeeDAO.updateEmployee(emp) > 0;

    }

    public boolean promoteEmployeePaygrade(Long empId, int newPayGrade){
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false;

        emp.setPayGrade(newPayGrade);
        return employeeDAO.updateEmployee(emp) > 0;
    }

    public boolean transferDepartment(Long empId, Long newDeptId){
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false;

        emp.setDeptId(newDeptId);
        return employeeDAO.updateEmployee(emp) > 0;
    }

    public EmployeeInfoDTO getEmployeeDetail(Long empId) {
        return employeeDAO.selectEmployeeInfoDetail(empId);
    }

    public List<EmployeeInfoDTO> getEmployeeInfoList() {
        return employeeDAO.selectAllEmployeeInfoList();
    }

    public EmployeeDTO authenticate(Long empId, String rawPassword) {
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);


        if (emp != null) {
            String encryptedInput = global.utils.PasswordUtils.encrypt(rawPassword);
            if (encryptedInput.equals(emp.getPassword())) {
                return emp;
            }
        }
        return null;
    }

    public boolean updateAdminRole(Long empId, String isAdminFlag) {
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false;

        boolean isAdmin = isAdminFlag != null && (isAdminFlag.equalsIgnoreCase("Y") || isAdminFlag.equalsIgnoreCase("true"));
        emp.setIsAdmin(isAdmin);
        return employeeDAO.updateEmployee(emp) > 0;
    }

    public List<AssignmentHistoryDTO> getAssignmentHistory(Long empId) {
        return assignmentHistoryDAO.selectHistoryByEmpId(empId);
    }
}
