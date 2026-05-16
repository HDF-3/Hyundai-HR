SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE process_daily_resignation
IS
    v_count NUMBER := 0;
BEGIN
    UPDATE employee
    SET status_id = 3 -- RESIGNED
    WHERE resign_date IS NOT NULL
      AND TRUNC(resign_date) <= TRUNC(SYSDATE)
      AND status_id != 3;

    v_count := SQL%ROWCOUNT;

    COMMIT;

    IF v_count > 0 THEN
        DBMS_OUTPUT.PUT_LINE('퇴사 처리 완료: ' || v_count || '명');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
