package leave.strategy;

import java.time.LocalDate;

public interface Leave {

    // 시작일과 종료일을 기준으로 실제 차감될 연차 일수를 계산
    double calculateDeduction(LocalDate start, LocalDate end);

    // 해당 휴가가 관리자 승인이 필요한 유형인지 반환
    boolean requiresApproval();
}
