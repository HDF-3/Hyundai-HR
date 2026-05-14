package humanresource.Service;

import humanresource.DTO.EmployeeDTO;
import humanresource.DTO.EmployeeInfoDTO;

import java.time.LocalDate;
import java.util.List;

public class EmployeeService {
    //TODO
    //+ registerEmployee(EmployeeDTO) : Boolean
    //+ getEmployeeInfo(Long empId): EmployeeDTO
    //+ modifyEmployeeInfo(EmployeeDTO): Boolean
    //+ processResignation(String empId) : Boolean
    //+ promoteEmployee(Long empId, Long newPositionId) : Boolean
    //+ promoteEmployeePaygrade(Long empId, Long newPositionId) : Boolean
    //+ transferDepartment(Long empId, int newDeptId) : Boolean


    private final humanresource.DAO.EmployeeDAO employeeDAO;

    public EmployeeService(){
        this.employeeDAO = new humanresource.DAO.EmployeeDAO();
    }

    public List<EmployeeDTO> getAllEmployees(){
        return employeeDAO.selectAllEmployees();
    }

    public boolean registerEmployee(humanresource.DTO.EmployeeDTO employeeDTO){
        return employeeDAO.insertEmployee(employeeDTO) > 0;
    }

    public EmployeeDTO getEmployeeinfo(Long empId){
        return employeeDAO.selectEmployeeById(empId);
    }

    public boolean modifyEmployeeInfo(humanresource.DTO.EmployeeDTO employeeDTO){
        return employeeDAO.updateEmployee(employeeDTO) > 0;
    }

    public boolean registerResignationDate(Long empId, LocalDate expectedResignDate){
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false; // 사원이 없으면 실패


        emp.setResignDate(expectedResignDate);

        return employeeDAO.updateEmployee(emp) > 0;
    }
    public boolean promoteEmployee(Long empId, Long positionId){
        EmployeeDTO emp = employeeDAO.selectEmployeeById(empId);
        if (emp == null) return false; // 사원이 없으면 실패

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


}
