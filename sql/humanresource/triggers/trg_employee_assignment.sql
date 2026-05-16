CREATE OR REPLACE TRIGGER trg_employee_assignment
AFTER UPDATE OF dept_id, position_id, pay_grade ON employee
FOR EACH ROW
DECLARE
    v_reason_id NUMBER;
    v_history_id NUMBER;
BEGIN
    -- 변경 사유 식별
    -- 2: 승진 (Promotion) - 직급 변경
    -- 3: 부서 이동 (Department Move) - 부서 변경
    -- 4: 직급 변경 (Rank Change)
    -- 6: 호봉 상승 (Pay Grade Change) - 호봉 변경
    IF NVL(:OLD.position_id, -1) != NVL(:NEW.position_id, -1) THEN
        v_reason_id := 2; -- 승진
    ELSIF NVL(:OLD.dept_id, -1) != NVL(:NEW.dept_id, -1) THEN
        v_reason_id := 3; -- 부서 이동
    ELSIF NVL(:OLD.pay_grade, -1) != NVL(:NEW.pay_grade, -1) THEN
        v_reason_id := 6; -- 호봉 상승
    ELSE
        -- 관련된 컬럼(부서, 직급, 호봉)의 실질적인 변경이 없으면 무시
        RETURN;
    END IF;

    -- 기존 '현재 적용중'인 이력의 종료일을 오늘로 설정하고 현재 상태를 'N'으로 변경
    UPDATE assignment_history
    SET is_current = 'N',
        end_date = TRUNC(SYSDATE)
    WHERE emp_id = :NEW.emp_id
      AND is_current = 'Y';

    -- 새로운 이력 ID 생성 (시퀀스가 명확하지 않아 MAX 값 활용, 시퀀스 사용 시 seq_assignment_history_id.NEXTVAL 권장)
    SELECT NVL(MAX(history_id), 0) + 1 INTO v_history_id FROM assignment_history;

    -- 새 발령 이력 삽입
    INSERT INTO assignment_history (
        history_id,
        emp_id,
        position_id,
        dept_id,
        reason_id,
        is_current,
        start_date,
        end_date,
        pay_grade
    ) VALUES (
        v_history_id,
        :NEW.emp_id,
        :NEW.position_id,
        :NEW.dept_id,
        v_reason_id,
        'Y',
        TRUNC(SYSDATE),
        NULL,
        :NEW.pay_grade
    );
END;
/
