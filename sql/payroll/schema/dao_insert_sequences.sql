SET DEFINE OFF;

DECLARE
    PROCEDURE ensure_sequence(
        p_sequence_name IN VARCHAR2,
        p_table_name IN VARCHAR2,
        p_id_column IN VARCHAR2
    )
    IS
        v_count NUMBER;
        v_last_number NUMBER;
        v_start_with NUMBER;
    BEGIN
        EXECUTE IMMEDIATE
            'SELECT NVL(MAX(' || p_id_column || '), 0) + 1 FROM ' || p_table_name
            INTO v_start_with;

        SELECT COUNT(*)
        INTO v_count
        FROM user_sequences
        WHERE sequence_name = UPPER(p_sequence_name);

        IF v_count = 0 THEN
            EXECUTE IMMEDIATE
                'CREATE SEQUENCE ' || p_sequence_name ||
                ' START WITH ' || v_start_with ||
                ' INCREMENT BY 1 NOCACHE NOCYCLE';
            RETURN;
        END IF;

        SELECT last_number
        INTO v_last_number
        FROM user_sequences
        WHERE sequence_name = UPPER(p_sequence_name);

        IF v_last_number < v_start_with THEN
            EXECUTE IMMEDIATE
                'ALTER SEQUENCE ' || p_sequence_name ||
                ' RESTART START WITH ' || v_start_with;
        END IF;
    END;
BEGIN
    ensure_sequence('seq_payroll_id', 'payroll', 'payroll_id');
    ensure_sequence('seq_earning_id', 'earning', 'earning_id');
    ensure_sequence('seq_deduction_id', 'deduction', 'deduction_id');
    ensure_sequence('seq_salary_standard_id', 'salary_standard', 'salary_standard_id');
    ensure_sequence('seq_additional_allowance_id', 'additional_allowance', 'additional_allowance_id');
END;
/
