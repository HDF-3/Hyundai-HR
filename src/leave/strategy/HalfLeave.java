package leave.strategy;

import java.time.LocalDate;

public class HalfLeave implements LeavePolicy {

    @Override
    public double calculateDeduction(LocalDate start, LocalDate end) {
        if (!start.isEqual(end)) {
            throw new IllegalArgumentException("반차는 시작일과 종료일이 같아야 합니다. (입력된 기간: " + start + " ~ " + end + ")");
        }
        return 0.5;
    }

    @Override
    public boolean requiresApproval() {
        return true;
    }
}
