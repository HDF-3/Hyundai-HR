package payroll.service;

import java.util.List;

import payroll.dao.PerformanceBonusPolicyDAO;
import payroll.dto.PerformanceBonusPolicyDTO;

public class PerformanceBonusPolicyService {

    private final PerformanceBonusPolicyDAO performanceBonusPolicyDAO = new PerformanceBonusPolicyDAO();

    public PerformanceBonusPolicyDTO getPerformanceBonusPolicy(Integer evalYear, Integer evalQuarter, String grade) {
        return performanceBonusPolicyDAO.findPerformanceBonusPolicy(evalYear, evalQuarter, grade);
    }

    public List<PerformanceBonusPolicyDTO> getPerformanceBonusPolicyList(Integer evalYear, Integer evalQuarter) {
        return performanceBonusPolicyDAO.findPerformanceBonusPolicyList(evalYear, evalQuarter);
    }

    public boolean updatePerformanceBonusPolicy(PerformanceBonusPolicyDTO performanceBonusPolicy) {
        return performanceBonusPolicyDAO.updatePerformanceBonusPolicy(performanceBonusPolicy) > 0;
    }
}
