package ui;

import humanresource.dto.EmployeeDTO;

public class AppSession {
    private EmployeeDTO currentEmployee;

    public EmployeeDTO getCurrentEmployee() {
        return currentEmployee;
    }

    public void setCurrentEmployee(EmployeeDTO currentEmployee) {
        this.currentEmployee = currentEmployee;
    }

    public Long getEmployeeId() {
        return currentEmployee == null ? null : currentEmployee.getEmpId();
    }

    public String getEmployeeName() {
        if (currentEmployee == null || currentEmployee.getEname() == null) {
            return "Guest";
        }
        return currentEmployee.getEname();
    }

    public boolean isAdmin() {
        return currentEmployee != null && Boolean.TRUE.equals(currentEmployee.getIsAdmin());
    }

    public void clear() {
        currentEmployee = null;
    }
}
