package leave.strategy;

import java.time.LocalDate;

public class FamilyEvent implements LeavePolicy {

    @Override
    public double calculateDeduction(LocalDate start, LocalDate end) {
        return 0.0;
    }
    // 경조사 역시 선조치 후보고. 그러나 서류 제출 필수이므로 일단 승인이 필요함 true
    @Override
    public boolean requiresApproval() {
        return true;
    }
}
