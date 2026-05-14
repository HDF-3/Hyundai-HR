package leave.strategy;

import java.time.LocalDate;

public class SickLeave implements Leave {

    @Override
    public double calculateDeduction(LocalDate start, LocalDate end) {
        return 0.0;
    }

    // 병가는 일단 다녀오되, 승인이 필요함. (진단서 보고 판단)
    @Override
    public boolean requiresApproval() {
        return true;
    }
}
