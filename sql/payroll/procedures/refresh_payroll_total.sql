SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE refresh_payroll_total (
    p_payroll_id IN NUMBER
)
IS
    v_total_earnings NUMBER(15,2);
    v_total_deductions NUMBER(15,2);
BEGIN
    SELECT NVL(SUM(
        base_salary
      + overtime_pay
      + transportation_allowance
      + performance_bonus
      + additional_allowance
    ), 0)
    INTO v_total_earnings
    FROM earning
    WHERE payroll_id = p_payroll_id;

    SELECT NVL(SUM(
        national_pension
      + health_insurance
      + long_term_care_insurance
      + employment_insurance
      + income_tax
      + local_income_tax
    ), 0)
    INTO v_total_deductions
    FROM deduction
    WHERE payroll_id = p_payroll_id;

    UPDATE payroll
    SET total_earnings = v_total_earnings,
        total_deductions = v_total_deductions,
        net_pay = v_total_earnings - v_total_deductions
    WHERE payroll_id = p_payroll_id;
END;
/
