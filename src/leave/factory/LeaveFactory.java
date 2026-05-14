package leave.factory;

import global.types.LeaveType;
import leave.strategy.*;

public class LeaveFactory {
    public static LeavePolicy getLeave(LeaveType leaveType) {
        switch (leaveType) {
            case ANNUAL:
                return new AnnualLeave();
            case HALF_AM:
            case HALF_PM:
                return new HalfLeave();
            case OUT_SIDE:
                return new OutSide();
            case SICK:
                return new SickLeave();
            case FAMILY_EVENT:
                return new FamilyEvent();
            default:
                throw new IllegalArgumentException("지원하지 않는 휴가 타입입니다.");
        }
    }
}
