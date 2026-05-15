package humanresource.service;

import humanresource.dto.EmployeeDTO;
import humanresource.dto.PerformanceEvaluationDTO;
import humanresource.dao.EmployeeDAO;
import humanresource.dao.PerformanceEvaluationDAO;

import java.util.List;

public class PerformanceEvaluationService {
    private final PerformanceEvaluationDAO performanceEvaluationDAO;
    private final EmployeeDAO employeeDAO;

    public PerformanceEvaluationService() {
        this.performanceEvaluationDAO = new PerformanceEvaluationDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    public boolean registerPerformanceEvaluation(PerformanceEvaluationDTO evalDTO, EmployeeDTO evaluator) throws Exception {

        if (evaluator.getIsAdmin() == null || !evaluator.getIsAdmin()) {
            throw new IllegalAccessException("인사 고과 등록 권한이 없습니다. (관리자 전용)");
        }

        EmployeeDTO targetEmp = employeeDAO.selectEmployeeById(evalDTO.getTargetEmpId());
        if (targetEmp == null) {
            throw new IllegalArgumentException("평가 대상 직원을 찾을 수 없습니다.");
        }

        if (!evaluator.getDeptId().equals(targetEmp.getDeptId())) {
            throw new IllegalAccessException("타 부서 직원의 인사 고과는 등록할 수 없습니다.");
        }

        return performanceEvaluationDAO.insertPerformanceEvaluation(evalDTO) > 0;
    }

    public List<PerformanceEvaluationDTO> getPerformanceEvaluationHistory(Long empId){
        return performanceEvaluationDAO.selectHistoryByEmpId(empId);
    }
}