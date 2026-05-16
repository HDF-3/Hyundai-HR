SET DEFINE OFF;

BEGIN
    DBMS_SCHEDULER.DROP_JOB(
        job_name => 'JOB_DAILY_RESIGNATION',
        force => TRUE
    );
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -27475 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name => 'JOB_DAILY_RESIGNATION',
        job_type => 'PLSQL_BLOCK',
        job_action => 'BEGIN process_daily_resignation; END;',
        start_date => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;BYHOUR=0;BYMINUTE=0;BYSECOND=0',
        enabled => TRUE,
        comments => '매일 자정에 퇴사예정일 도래 직원을 퇴사 상태로 변경'
    );
END;
/
