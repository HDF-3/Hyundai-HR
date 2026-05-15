package leave.strategy;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class AnnualLeave implements LeavePolicy {
    @Override
    public double calculateDeduction(LocalDate start, LocalDate end) {
        // 주말 제외 로직. 공휴일은 추후 처리 예정
        double count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            // 날짜에 따라 요일을 가져옴
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }

    // 연차는 반드시 승인 필수
    @Override
    public boolean requiresApproval() {
        return true;
    }
}
