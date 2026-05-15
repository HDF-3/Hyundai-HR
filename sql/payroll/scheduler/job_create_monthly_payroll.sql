SET DEFINE OFF;

BEGIN
    DBMS_SCHEDULER.DROP_JOB(
        job_name => 'JOB_CREATE_MONTHLY_PAYROLL',
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
        job_name => 'JOB_CREATE_MONTHLY_PAYROLL',
        job_type => 'PLSQL_BLOCK',
        job_action => 'BEGIN CREATE_MONTHLY_PAYROLL(ADD_MONTHS(TRUNC(SYSDATE, ''MM''), -1)); END;',
        start_date => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MONTHLY;BYMONTHDAY=5;BYHOUR=2;BYMINUTE=0;BYSECOND=0',
        enabled => TRUE,
        comments => '매월 5일 02시에 전월 급여 자동 생성'
    );
END;
/
