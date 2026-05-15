SET DEFINE OFF;

CREATE OR REPLACE TRIGGER trg_deduction_refresh_payroll
AFTER INSERT OR UPDATE OR DELETE ON deduction
FOR EACH ROW
DECLARE
    v_payroll_id NUMBER;
    v_total_earnings NUMBER(15,2);
    v_total_deductions NUMBER(15,2);
BEGIN
    v_payroll_id := NVL(:NEW.payroll_id, :OLD.payroll_id);

    SELECT NVL(SUM(
          base_salary
        + overtime_pay
        + transportation_allowance
        + performance_bonus
        + additional_allowance
    ), 0)
    INTO v_total_earnings
    FROM earning
    WHERE payroll_id = v_payroll_id;

    IF DELETING THEN
        v_total_deductions := 0;
    ELSE
        v_total_deductions :=
              NVL(:NEW.national_pension, 0)
            + NVL(:NEW.health_insurance, 0)
            + NVL(:NEW.long_term_care_insurance, 0)
            + NVL(:NEW.employment_insurance, 0)
            + NVL(:NEW.income_tax, 0)
            + NVL(:NEW.local_income_tax, 0);
    END IF;

    UPDATE payroll
    SET total_earnings = v_total_earnings,
        total_deductions = v_total_deductions,
        net_pay = v_total_earnings - v_total_deductions
    WHERE payroll_id = v_payroll_id;
END;
/

ALTER TRIGGER trg_deduction_refresh_payroll ENABLE;
