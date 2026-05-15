SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE insert_monthly_deduction (
    p_payroll_year_month IN DATE
)
IS
BEGIN
    INSERT INTO deduction (
        deduction_id,
        payroll_id,
        national_pension,
        health_insurance,
        long_term_care_insurance,
        employment_insurance,
        income_tax,
        local_income_tax
    )
    WITH earning_total AS (
        SELECT
            p.payroll_id,
            e.base_salary
          + e.overtime_pay
          + e.transportation_allowance
          + e.performance_bonus
          + e.additional_allowance AS total_earnings
        FROM payroll p
        JOIN earning e
          ON e.payroll_id = p.payroll_id
        WHERE p.payroll_year_month = p_payroll_year_month
          AND p.status = 'CALCULATED'
          AND NOT EXISTS (
              SELECT 1
              FROM deduction d
              WHERE d.payroll_id = p.payroll_id
          )
    ),
    deduction_amount AS (
        SELECT
            et.payroll_id,
            ROUND(et.total_earnings * 0.045) AS national_pension,
            ROUND(et.total_earnings * 0.03545) AS health_insurance,
            ROUND(ROUND(et.total_earnings * 0.03545) * 0.1295) AS long_term_care_insurance,
            ROUND(et.total_earnings * 0.009) AS employment_insurance,
            ROUND(
                CASE
                    WHEN et.total_earnings <= 3000000 THEN
                        et.total_earnings * 0.03
                    WHEN et.total_earnings <= 5000000 THEN
                        3000000 * 0.03
                      + (et.total_earnings - 3000000) * 0.06
                    ELSE
                        3000000 * 0.03
                      + 2000000 * 0.06
                      + (et.total_earnings - 5000000) * 0.10
                END
            ) AS income_tax
        FROM earning_total et
    ),
    deduction_total AS (
        SELECT
            da.*,
            ROUND(da.income_tax * 0.10) AS local_income_tax
        FROM deduction_amount da
    )
    SELECT
        seq_deduction_id.NEXTVAL,
        payroll_id,
        national_pension,
        health_insurance,
        long_term_care_insurance,
        employment_insurance,
        income_tax,
        local_income_tax
    FROM deduction_total;
END;
/
