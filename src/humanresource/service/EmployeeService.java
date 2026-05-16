package humanresource.service;

import humanresource.dto.EmployeeDTO;
import humanresource.dto.EmployeeInfoDTO;
import humanresource.dto.AssignmentHistoryDTO;
import humanresource.dao.EmployeeDAO;
import humanresource.dao.AssignmentHistoryDAO;
import global.utils.PasswordUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeService {


    private final EmployeeDAO employeeDAO;
    private final AssignmentHistoryDAO assignmentHistoryDAO;

    public EmployeeService(){
        this.employeeDAO = new EmployeeDAO();
        this.assignmentHistoryDAO = new AssignmentHistoryDAO();
    }

    public List<EmployeeDTO> getAllEmployees(){
        return employeeDAO.selectAllEmployees();
    }

    public boolean registerEmployee(EmployeeDTO employeeDTO){
        if (employeeDTO.getHireDate() == null) {
            employeeDTO.setHireDate(LocalDate.now());
        }
        String rawPassword = employeeDTO.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "1234";
        }
        employeeDTO.setPassword(PasswordUtils.encrypt(rawPassword));
        return employeeDAO.insertEmployee(employeeDTO) > 0;
    }

    public EmployeeDTO getEmployeeInfo(Long empId){
        return employeeDAO.selectEmployeeById(empId);
    }

    public boolean updateEmployeeInfo(EmployeeDTO employeeDTO){
        return employeeDAO.updateEmployee(employeeDTO) > 0;
    }

    private boolean executeIfEmployeeExists(Long empId, java.util.function.Consumer<EmployeeDTO> action) {
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false;
        action.accept(emp);
        return employeeDAO.updateEmployee(emp) > 0;
    }

    public boolean registerResignationDate(Long empId, LocalDate expectedResignDate){
        return executeIfEmployeeExists(empId, emp -> emp.setResignDate(expectedResignDate));
    }
    public boolean promoteEmployee(Long empId, Long positionId){
        return executeIfEmployeeExists(empId, emp -> emp.setPositionId(positionId));
    }

    public boolean promoteEmployeePaygrade(Long empId, int newPayGrade){
        return executeIfEmployeeExists(empId, emp -> emp.setPayGrade(newPayGrade));
    }

    public boolean transferDepartment(Long empId, Long newDeptId){
        return executeIfEmployeeExists(empId, emp -> emp.setDeptId(newDeptId));
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
            String encryptedInput = PasswordUtils.encrypt(rawPassword);
            if (java.util.Objects.equals(encryptedInput, emp.getPassword())
                    || java.util.Objects.equals(rawPassword, emp.getPassword())) {
                return emp;
            }
        }
        return null;
    }

    public boolean updateAdminRole(Long empId, String isAdminFlag) {
        return executeIfEmployeeExists(empId, emp -> {
            boolean isAdmin = isAdminFlag != null && (isAdminFlag.equalsIgnoreCase("Y") || isAdminFlag.equalsIgnoreCase("true"));
            emp.setIsAdmin(isAdmin);
        });
    }

    public List<AssignmentHistoryDTO> getAssignmentHistory(Long empId) {
        return assignmentHistoryDAO.selectHistoryByEmpId(empId);
    }

    /**
     * 이름으로 직원 검색 (부분 일치)
     */
    public List<EmployeeInfoDTO> searchEmployeesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return employeeDAO.searchEmployeesByName(name.trim());
    }

    /**
     * 부서ID로 직원 검색
     */
    public List<EmployeeInfoDTO> searchEmployeesByDeptId(Long deptId) {
        if (deptId == null || deptId <= 0) {
            return new ArrayList<>();
        }
        return employeeDAO.searchEmployeesByDeptId(deptId);
    }

    /**
     * 이름과 부서로 복합 검색
     */
    public List<EmployeeInfoDTO> searchEmployees(String name, Long deptId) {
        return employeeDAO.searchEmployees(
                (name != null && !name.trim().isEmpty()) ? name.trim() : null,
                (deptId != null && deptId > 0) ? deptId : null
        );
    }
}
