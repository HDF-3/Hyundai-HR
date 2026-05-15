SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE check_month_attendance_closed (
    p_year_month IN VARCHAR2,
    p_result OUT CHAR
)
IS
    v_not_closed_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_not_closed_count
    FROM attendance
    WHERE work_date >= TO_DATE(p_year_month || '-01', 'YYYY-MM-DD')
      AND work_date < ADD_MONTHS(TO_DATE(p_year_month || '-01', 'YYYY-MM-DD'), 1)
      AND is_closed <> 'Y';

    IF v_not_closed_count = 0 THEN
        p_result := 'Y';
    ELSE
        p_result := 'N';
    END IF;
END;
/
