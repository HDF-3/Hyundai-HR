SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE create_monthly_payroll (
    p_payroll_year_month IN DATE
)
IS
BEGIN
    IF p_payroll_year_month IS NULL THEN
        RAISE_APPLICATION_ERROR(-20000, '급여 생성 월은 필수입니다.');
    END IF;

    validate_monthly_payroll(p_payroll_year_month);
    insert_monthly_payroll(p_payroll_year_month);
    insert_monthly_earning(p_payroll_year_month);
    insert_monthly_deduction(p_payroll_year_month);
    update_monthly_payroll_total(p_payroll_year_month);

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
