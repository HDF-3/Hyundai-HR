SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE validate_monthly_payroll (
    p_payroll_year_month IN DATE
)
IS
    v_attendance_count NUMBER;
    v_closed_result CHAR(1);
BEGIN
    SELECT COUNT(*)
    INTO v_attendance_count
    FROM attendance
    WHERE work_date >= p_payroll_year_month
      AND work_date < ADD_MONTHS(p_payroll_year_month, 1);

    IF v_attendance_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, '해당 월 근태 데이터가 없습니다.');
    END IF;

    check_month_attendance_closed(
        TO_CHAR(p_payroll_year_month, 'YYYY-MM'),
        v_closed_result
    );

    IF v_closed_result <> 'Y' THEN
        RAISE_APPLICATION_ERROR(-20002, '해당 월 근태가 마감되지 않았습니다.');
    END IF;
END;
/
