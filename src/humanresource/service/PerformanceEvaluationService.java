package humanresource.service;

import humanresource.dao.EmployeeDAO;
import humanresource.dao.PerformanceEvaluationDAO;
import humanresource.dto.EmployeeDTO;
import humanresource.dto.PerformanceEvaluationDTO;

import java.util.List;

public class PerformanceEvaluationService {
    private final PerformanceEvaluationDAO performanceEvaluationDAO;
    private final EmployeeDAO employeeDAO;

    public PerformanceEvaluationService() {
        this.performanceEvaluationDAO = new PerformanceEvaluationDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    public boolean registerPerformanceEvaluation(PerformanceEvaluationDTO evalDTO, EmployeeDTO evaluator) throws IllegalAccessException, IllegalArgumentException {
        if (evaluator == null || evaluator.getIsAdmin() == null || !evaluator.getIsAdmin()) {
            throw new IllegalAccessException("인사평가 등록 권한이 없습니다.");
        }

        EmployeeDTO targetEmp = employeeDAO.selectEmployeeById(evalDTO.getTargetEmpId());
        if (targetEmp == null) {
            throw new IllegalArgumentException("평가 대상 직원을 찾을 수 없습니다.");
        }

        if (!evaluator.getDeptId().equals(targetEmp.getDeptId())) {
            throw new IllegalAccessException("인사평가는 같은 부서 소속 직원에게만 부여할 수 있습니다.");
        }

        return performanceEvaluationDAO.insertPerformanceEvaluation(evalDTO) > 0;
    }

    public List<PerformanceEvaluationDTO> getPerformanceEvaluationHistory(Long empId) {
        return performanceEvaluationDAO.selectHistoryByEmpId(empId);
    }
}
