SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE insert_monthly_earning (
    p_payroll_year_month IN DATE
)
IS
    v_next_month DATE := ADD_MONTHS(p_payroll_year_month, 1);
    v_month_end DATE := LAST_DAY(p_payroll_year_month);
    v_month_days NUMBER := LAST_DAY(p_payroll_year_month) - p_payroll_year_month + 1;
BEGIN
    INSERT INTO earning (
        earning_id,
        payroll_id,
        base_salary,
        overtime_pay,
        transportation_allowance,
        performance_bonus,
        additional_allowance
    )
    WITH payroll_target AS (
        SELECT
            p.payroll_id,
            e.emp_id,
            e.hire_date,
            e.resign_date,
            ss.base_salary AS monthly_base_salary,
            ss.regular_hourly_rate
        FROM payroll p
        JOIN employee e
          ON e.emp_id = p.employee_id
        JOIN salary_standard ss
          ON ss.position_id = e.position_id
         AND ss.pay_grade = e.pay_grade
        WHERE p.payroll_year_month = p_payroll_year_month
          AND p.status = 'CALCULATED'
          AND NOT EXISTS (
              SELECT 1
              FROM earning er
              WHERE er.payroll_id = p.payroll_id
          )
    ),
    work_result AS (
        SELECT
            a.emp_id,
            ROUND(
                (
                    CAST(a.off_work_time AS DATE)
                    - TO_DATE(
                        TO_CHAR(a.work_date, 'YYYY-MM-DD') || ' ' || wt.off_work_time,
                        'YYYY-MM-DD HH24:MI'
                    )
                ) * 1440
            ) AS raw_overtime_minutes
        FROM attendance a
        JOIN payroll_target pt
          ON pt.emp_id = a.emp_id
        JOIN work_time wt
          ON wt.emp_id = a.emp_id
         AND wt.applied_date = (
             SELECT MIN(wt2.applied_date)
             FROM work_time wt2
             WHERE wt2.emp_id = a.emp_id
               AND wt2.applied_date >= a.work_date
         )
        WHERE a.work_date >= p_payroll_year_month
          AND a.work_date < v_next_month
          AND a.is_closed = 'Y'
          AND a.on_work_time IS NOT NULL
          AND a.off_work_time IS NOT NULL
    ),
    overtime_summary AS (
        SELECT
            emp_id,
            SUM(
                CASE
                    WHEN raw_overtime_minutes >= 30 THEN raw_overtime_minutes
                    ELSE 0
                END
            ) AS overtime_minutes,
            SUM(
                CASE
                    WHEN raw_overtime_minutes >= 30 THEN 1
                    ELSE 0
                END
            ) AS overtime_days
        FROM work_result
        GROUP BY emp_id
    ),
    allowance_summary AS (
        SELECT
            employee_id AS emp_id,
            SUM(amount) AS additional_allowance
        FROM additional_allowance
        WHERE allowance_year_month = p_payroll_year_month
        GROUP BY employee_id
    ),
    base_salary_calc AS (
        SELECT
            pt.payroll_id,
            pt.emp_id,
            pt.regular_hourly_rate,
            CASE
                WHEN TRUNC(pt.hire_date) <= p_payroll_year_month
                 AND (pt.resign_date IS NULL OR TRUNC(pt.resign_date) >= v_month_end)
                THEN pt.monthly_base_salary
                ELSE ROUND(
                    (pt.monthly_base_salary / v_month_days)
                    * GREATEST(
                        LEAST(NVL(TRUNC(pt.resign_date), v_month_end), v_month_end)
                        - GREATEST(TRUNC(pt.hire_date), p_payroll_year_month)
                        + 1,
                        0
                    )
                )
            END AS base_salary
        FROM payroll_target pt
    ),
    earning_amount AS (
        SELECT
            bsc.payroll_id,
            bsc.base_salary,
            ROUND((NVL(os.overtime_minutes, 0) / 60) * 2 * bsc.regular_hourly_rate) AS overtime_pay,
            NVL(os.overtime_days, 0) * 20000 AS transportation_allowance,
            CASE
                WHEN TO_NUMBER(TO_CHAR(p_payroll_year_month, 'MM')) IN (3, 6, 9, 12)
                THEN ROUND(bsc.base_salary * NVL(bp.bonus_rate, 0) + NVL(bp.fixed_amount, 0))
                ELSE 0
            END AS performance_bonus,
            NVL(als.additional_allowance, 0) AS additional_allowance
        FROM base_salary_calc bsc
        LEFT JOIN overtime_summary os
          ON os.emp_id = bsc.emp_id
        LEFT JOIN allowance_summary als
          ON als.emp_id = bsc.emp_id
        LEFT JOIN performance_evaluation pe
          ON pe.target_emp_id = bsc.emp_id
         AND pe.eval_year = TO_NUMBER(TO_CHAR(p_payroll_year_month, 'YYYY'))
         AND pe.eval_quarter = CEIL(TO_NUMBER(TO_CHAR(p_payroll_year_month, 'MM')) / 3)
         AND TO_NUMBER(TO_CHAR(p_payroll_year_month, 'MM')) IN (3, 6, 9, 12)
        LEFT JOIN performance_bonus_policy bp
          ON bp.eval_year = pe.eval_year
         AND bp.eval_quarter = pe.eval_quarter
         AND bp.grade = pe.grade
    )
    SELECT
        seq_earning_id.NEXTVAL,
        payroll_id,
        base_salary,
        overtime_pay,
        transportation_allowance,
        performance_bonus,
        additional_allowance
    FROM earning_amount;
END;
/
