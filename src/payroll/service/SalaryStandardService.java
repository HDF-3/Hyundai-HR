package payroll.service;

import payroll.dao.SalaryStandardDAO;
import payroll.dto.SalaryStandardDTO;

public class SalaryStandardService {

    private final SalaryStandardDAO salaryStandardDAO = new SalaryStandardDAO();

    public SalaryStandardDTO getSalaryStandard(Long positionId, Integer payGrade) {
        return salaryStandardDAO.findSalaryStandard(positionId, payGrade);
    }

    public boolean updateSalaryStandard(SalaryStandardDTO salaryStandard) {
        return salaryStandardDAO.updateSalaryStandard(salaryStandard) > 0;
    }
}
