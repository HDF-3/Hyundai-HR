package leave.strategy;

import java.time.LocalDate;

public class OutSide implements Leave{
    @Override
    public double calculateDeduction(LocalDate start, LocalDate end) {
        return 0.0;
    }

    @Override
    public boolean requiresApproval() {
        return false;
    }
}
