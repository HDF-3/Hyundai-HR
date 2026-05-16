package attendance.dao;

import java.time.YearMonth;

import attendance.dto.OvertimeDTO;

public class OvertimeDAO {

    public OvertimeDTO findOvertime(Long employeeId, YearMonth yearMonth) {
        // TODO: 실제 근태 테이블 연동 전까지 목데이터 반환
        return new OvertimeDTO(2.5f, 3);
    }
}
