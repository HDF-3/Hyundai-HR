package humanresource.Service;

import humanresource.DTO.PerformanceEvaluationDTO;

import java.util.List;

public class PerformanceEvaluationService {
    private final humanresource.DAO.PerformanceEvaluationDAO performanceEvaluationDAO;

    public PerformanceEvaluationService() {
        this.performanceEvaluationDAO = new humanresource.DAO.PerformanceEvaluationDAO();
    }

    public int registerPerformanceEvaluation(humanresource.DTO.PerformanceEvaluationDTO performanceEvaluationDTO) {
        return performanceEvaluationDAO.insertPerformanceEvaluation(performanceEvaluationDTO);
    }

    public List<PerformanceEvaluationDTO> getPerformanceEvaluationHistory(Long empId){
        return performanceEvaluationDAO.selectHistoryByEmpId(empId);
    }
}
