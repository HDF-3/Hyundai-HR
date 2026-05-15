package humanresource.service;

import humanresource.dto.PerformanceEvaluationDTO;

import java.util.List;

public class PerformanceEvaluationService {
    private final humanresource.dao.PerformanceEvaluationDAO performanceEvaluationDAO;

    public PerformanceEvaluationService() {
        this.performanceEvaluationDAO = new humanresource.dao.PerformanceEvaluationDAO();
    }


    public boolean registerPerformanceEvaluation(humanresource.dto.PerformanceEvaluationDTO performanceEvaluationDTO) {
        return performanceEvaluationDAO.insertPerformanceEvaluation(performanceEvaluationDTO) > 0;
    }

    public List<PerformanceEvaluationDTO> getPerformanceEvaluationHistory(Long empId){
        return performanceEvaluationDAO.selectHistoryByEmpId(empId);
    }
}