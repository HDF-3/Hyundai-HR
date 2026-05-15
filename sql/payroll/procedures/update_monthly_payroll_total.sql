SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE update_monthly_payroll_total (
    p_payroll_year_month IN DATE
)
IS
BEGIN
    UPDATE payroll p
    SET (
        total_earnings,
        total_deductions,
        net_pay
    ) = (
        SELECT
            calc.total_earnings,
            calc.total_deductions,
            calc.total_earnings - calc.total_deductions
        FROM (
            SELECT
                e.payroll_id,
                e.base_salary
              + e.overtime_pay
              + e.transportation_allowance
              + e.performance_bonus
              + e.additional_allowance AS total_earnings,
                d.national_pension
              + d.health_insurance
              + d.long_term_care_insurance
              + d.employment_insurance
              + d.income_tax
              + d.local_income_tax AS total_deductions
            FROM earning e
            JOIN deduction d
              ON d.payroll_id = e.payroll_id
        ) calc
        WHERE calc.payroll_id = p.payroll_id
    )
    WHERE p.payroll_year_month = p_payroll_year_month
      AND p.status = 'CALCULATED'
      AND EXISTS (
          SELECT 1
          FROM earning e
          JOIN deduction d
            ON d.payroll_id = e.payroll_id
          WHERE e.payroll_id = p.payroll_id
      );
END;
/
