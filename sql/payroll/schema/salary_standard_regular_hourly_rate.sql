SET DEFINE OFF;

-- regular_hourly_rate는 기본급 기준 통상시급입니다.
-- 기본급이 바뀌면 통상시급도 자동으로 다시 계산되도록 가상 컬럼으로 관리합니다.
DECLARE
    v_count NUMBER;
    v_virtual_column VARCHAR2(3);
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM user_tab_cols
    WHERE table_name = 'SALARY_STANDARD'
      AND column_name = 'REGULAR_HOURLY_RATE';

    IF v_count > 0 THEN
        SELECT virtual_column
        INTO v_virtual_column
        FROM user_tab_cols
        WHERE table_name = 'SALARY_STANDARD'
          AND column_name = 'REGULAR_HOURLY_RATE';

        IF v_virtual_column <> 'YES' THEN
            EXECUTE IMMEDIATE 'ALTER TABLE salary_standard DROP COLUMN regular_hourly_rate';
            v_count := 0;
        END IF;
    END IF;

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE '
            ALTER TABLE salary_standard ADD (
                regular_hourly_rate NUMBER(15,2)
                    GENERATED ALWAYS AS (ROUND(NVL(base_salary, 0) / 209, 2)) VIRTUAL
            )
        ';
    END IF;
END;
/
