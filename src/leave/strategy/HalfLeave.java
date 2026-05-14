package leave.strategy;

import java.time.LocalDate;

public class HalfLeave implements Leave {

    @Override
    public double calculateDeduction(LocalDate start, LocalDate end) {
        return 0.5;
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }
}
