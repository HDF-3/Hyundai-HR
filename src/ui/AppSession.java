package ui;

import humanresource.dto.EmployeeDTO;

public class AppSession {
    private final EmployeeDTO employee;

    public AppSession(EmployeeDTO employee) {
        this.employee = employee;
    }

    public EmployeeDTO getEmployee() {
        return employee;
    }

    public Long getEmployeeId() {
        return employee == null ? null : employee.getEmpId();
    }

    public String getEmployeeName() {
        if (employee == null || employee.getEname() == null || employee.getEname().trim().isEmpty()) {
            return "사용자";
        }
        return employee.getEname();
    }

    public boolean isAdmin() {
        return employee != null && Boolean.TRUE.equals(employee.getIsAdmin());
    }
}
