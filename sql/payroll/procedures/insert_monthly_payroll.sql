SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE insert_monthly_payroll (
    p_payroll_year_month IN DATE
)
IS
BEGIN
    INSERT INTO payroll (
        payroll_id,
        employee_id,
        payroll_year_month,
        total_earnings,
        total_deductions,
        net_pay,
        confirmed_at,
        pay_date,
        status
    )
    SELECT
        seq_payroll_id.NEXTVAL,
        e.emp_id,
        p_payroll_year_month,
        0,
        0,
        0,
        NULL,
        NULL,
        'CALCULATED'
    FROM employee e
    JOIN salary_standard ss
      ON ss.position_id = e.position_id
     AND ss.pay_grade = e.pay_grade
    WHERE EXISTS (
        SELECT 1
        FROM attendance a
        WHERE a.emp_id = e.emp_id
          AND a.work_date >= p_payroll_year_month
          AND a.work_date < ADD_MONTHS(p_payroll_year_month, 1)
          AND a.is_closed = 'Y'
          AND a.on_work_time IS NOT NULL
          AND a.off_work_time IS NOT NULL
    )
      AND NOT EXISTS (
        SELECT 1
        FROM payroll p
        WHERE p.employee_id = e.emp_id
          AND p.payroll_year_month = p_payroll_year_month
    );
END;
/
