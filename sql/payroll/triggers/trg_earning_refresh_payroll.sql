SET DEFINE OFF;

CREATE OR REPLACE TRIGGER trg_earning_refresh_payroll
AFTER INSERT OR UPDATE OR DELETE ON earning
FOR EACH ROW
DECLARE
    v_payroll_id NUMBER;
    v_total_earnings NUMBER(15,2);
    v_total_deductions NUMBER(15,2);
BEGIN
    v_payroll_id := NVL(:NEW.payroll_id, :OLD.payroll_id);

    IF DELETING THEN
        v_total_earnings := 0;
    ELSE
        v_total_earnings :=
              NVL(:NEW.base_salary, 0)
            + NVL(:NEW.overtime_pay, 0)
            + NVL(:NEW.transportation_allowance, 0)
            + NVL(:NEW.performance_bonus, 0)
            + NVL(:NEW.additional_allowance, 0);
    END IF;

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
    WHERE payroll_id = v_payroll_id;

    UPDATE payroll
    SET total_earnings = v_total_earnings,
        total_deductions = v_total_deductions,
        net_pay = v_total_earnings - v_total_deductions
    WHERE payroll_id = v_payroll_id;
END;
/

ALTER TRIGGER trg_earning_refresh_payroll ENABLE;
