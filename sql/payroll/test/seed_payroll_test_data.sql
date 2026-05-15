SET DEFINE OFF;

/*
  Payroll test data seed.

  Target months:
    - 2026-04-01: monthly payroll, overtime, additional allowance, prorated salary
    - 2026-03-01: quarterly performance bonus

  How to use:
    1. Run this seed script.
    2. Run:
         EXEC create_monthly_payroll(DATE '2026-04-01');
         EXEC create_monthly_payroll(DATE '2026-03-01');
    3. Check payroll / earning / deduction rows for employee_id 99001 ~ 99004.
*/

-- Cleanup previous test rows.
DELETE FROM deduction
WHERE payroll_id IN (
    SELECT payroll_id
    FROM payroll
    WHERE employee_id IN (99001, 99002, 99003, 99004)
      AND payroll_year_month IN (DATE '2026-03-01', DATE '2026-04-01')
);

DELETE FROM earning
WHERE payroll_id IN (
    SELECT payroll_id
    FROM payroll
    WHERE employee_id IN (99001, 99002, 99003, 99004)
      AND payroll_year_month IN (DATE '2026-03-01', DATE '2026-04-01')
);

DELETE FROM payroll
WHERE employee_id IN (99001, 99002, 99003, 99004)
  AND payroll_year_month IN (DATE '2026-03-01', DATE '2026-04-01');

DELETE FROM additional_allowance
WHERE employee_id IN (99001, 99002, 99003, 99004)
  AND allowance_year_month IN (DATE '2026-03-01', DATE '2026-04-01');

DELETE FROM performance_evaluation
WHERE target_emp_id IN (99001, 99002, 99003, 99004)
  AND eval_year = 2026
  AND eval_quarter IN (1, 2);

DELETE FROM performance_bonus_policy
WHERE eval_year = 2026
  AND eval_quarter = 1
  AND grade = 'TEST';

DELETE FROM attendance
WHERE emp_id IN (99001, 99002, 99003, 99004)
  AND work_date >= DATE '2026-03-01'
  AND work_date < DATE '2026-05-01';

DELETE FROM work_time
WHERE emp_id IN (99001, 99002, 99003, 99004);

DELETE FROM employee
WHERE emp_id IN (99001, 99002, 99003, 99004);

DELETE FROM salary_standard
WHERE salary_standard_id = 99001
   OR (position_id = 5 AND pay_grade = 99);


-- Test salary standard.
-- regular_hourly_rate is a generated column: ROUND(base_salary / 209, 2)
INSERT INTO salary_standard (
    salary_standard_id,
    position_id,
    pay_grade,
    base_salary
) VALUES (
    99001,
    5,
    99,
    3000000
);


-- Test employees.
-- 99001: full-month employee with overtime and additional allowance.
-- 99002: mid-month hire, base salary should be prorated from hire date.
-- 99003: mid-month resignation, base salary should be prorated until resign date.
-- 99004: quarterly performance bonus target.
INSERT INTO employee (
    emp_id,
    dept_id,
    position_id,
    status_id,
    name,
    hire_date,
    resign_date,
    contact,
    gender,
    email,
    address,
    salary_account,
    is_admin,
    pay_grade,
    password
) VALUES (
    99001,
    4,
    5,
    1,
    'Payroll Test Normal',
    DATE '2025-01-01',
    NULL,
    '010-9900-0001',
    'M',
    'payroll.test.normal@example.com',
    'Seoul',
    'TEST-99001',
    'N',
    99,
    'test1234'
);

INSERT INTO employee (
    emp_id,
    dept_id,
    position_id,
    status_id,
    name,
    hire_date,
    resign_date,
    contact,
    gender,
    email,
    address,
    salary_account,
    is_admin,
    pay_grade,
    password
) VALUES (
    99002,
    4,
    5,
    1,
    'Payroll Test Mid Hire',
    DATE '2026-04-10',
    NULL,
    '010-9900-0002',
    'F',
    'payroll.test.hire@example.com',
    'Seoul',
    'TEST-99002',
    'N',
    99,
    'test1234'
);

INSERT INTO employee (
    emp_id,
    dept_id,
    position_id,
    status_id,
    name,
    hire_date,
    resign_date,
    contact,
    gender,
    email,
    address,
    salary_account,
    is_admin,
    pay_grade,
    password
) VALUES (
    99003,
    4,
    5,
    3,
    'Payroll Test Resigned',
    DATE '2025-01-01',
    DATE '2026-04-15',
    '010-9900-0003',
    'M',
    'payroll.test.resigned@example.com',
    'Seoul',
    'TEST-99003',
    'N',
    99,
    'test1234'
);

INSERT INTO employee (
    emp_id,
    dept_id,
    position_id,
    status_id,
    name,
    hire_date,
    resign_date,
    contact,
    gender,
    email,
    address,
    salary_account,
    is_admin,
    pay_grade,
    password
) VALUES (
    99004,
    4,
    5,
    1,
    'Payroll Test Bonus',
    DATE '2025-01-01',
    NULL,
    '010-9900-0004',
    'F',
    'payroll.test.bonus@example.com',
    'Seoul',
    'TEST-99004',
    'N',
    99,
    'test1234'
);


-- Work time rows.
-- applied_date means valid-until date.
-- The row with the smallest applied_date >= work_date is used.
-- For March/April 2026, the 09:00~18:00 row ending on 2026-05-31 should be selected.
INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99001, '08:00', '17:00', DATE '9999-12-31');

INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99001, '09:00', '18:00', DATE '2026-05-31');

INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99002, '08:00', '17:00', DATE '9999-12-31');

INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99002, '09:00', '18:00', DATE '2026-05-31');

INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99003, '08:00', '17:00', DATE '9999-12-31');

INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99003, '09:00', '18:00', DATE '2026-05-31');

INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99004, '08:00', '17:00', DATE '9999-12-31');

INSERT INTO work_time (emp_id, on_work_time, off_work_time, applied_date)
VALUES (99004, '09:00', '18:00', DATE '2026-05-31');


-- April attendance.
-- Overtime counts only when actual off time is at least 30 minutes after scheduled off time.
-- 99001: 20 minutes does not count, 30 + 120 minutes count.
INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99001, DATE '2026-04-01', TIMESTAMP '2026-04-01 09:00:00', TIMESTAMP '2026-04-01 18:00:00', 'Y');

INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99001, DATE '2026-04-02', TIMESTAMP '2026-04-02 09:00:00', TIMESTAMP '2026-04-02 18:20:00', 'Y');

INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99001, DATE '2026-04-03', TIMESTAMP '2026-04-03 09:00:00', TIMESTAMP '2026-04-03 18:30:00', 'Y');

INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99001, DATE '2026-04-04', TIMESTAMP '2026-04-04 09:00:00', TIMESTAMP '2026-04-04 20:00:00', 'Y');

-- 99002: mid-month hire, one overtime day.
INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99002, DATE '2026-04-10', TIMESTAMP '2026-04-10 09:00:00', TIMESTAMP '2026-04-10 18:00:00', 'Y');

INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99002, DATE '2026-04-15', TIMESTAMP '2026-04-15 09:00:00', TIMESTAMP '2026-04-15 19:00:00', 'Y');

-- 99003: mid-month resignation, one overtime day.
INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99003, DATE '2026-04-01', TIMESTAMP '2026-04-01 09:00:00', TIMESTAMP '2026-04-01 18:00:00', 'Y');

INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99003, DATE '2026-04-15', TIMESTAMP '2026-04-15 09:00:00', TIMESTAMP '2026-04-15 18:45:00', 'Y');

-- 99004: full-month employee for regular April payroll.
INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99004, DATE '2026-04-01', TIMESTAMP '2026-04-01 09:00:00', TIMESTAMP '2026-04-01 18:00:00', 'Y');


-- March attendance for quarterly performance bonus scenario.
INSERT INTO attendance (emp_id, work_date, on_work_time, off_work_time, is_closed)
VALUES (99004, DATE '2026-03-31', TIMESTAMP '2026-03-31 09:00:00', TIMESTAMP '2026-03-31 18:00:00', 'Y');


-- Additional allowances for April.
INSERT INTO additional_allowance (
    additional_allowance_id,
    employee_id,
    additional_allowance_name,
    allowance_year_month,
    amount
) VALUES (
    99001,
    99001,
    'Meal Support',
    DATE '2026-04-01',
    30000
);

INSERT INTO additional_allowance (
    additional_allowance_id,
    employee_id,
    additional_allowance_name,
    allowance_year_month,
    amount
) VALUES (
    99002,
    99001,
    'Project Support',
    DATE '2026-04-01',
    20000
);

INSERT INTO additional_allowance (
    additional_allowance_id,
    employee_id,
    additional_allowance_name,
    allowance_year_month,
    amount
) VALUES (
    99003,
    99004,
    'Certification Support',
    DATE '2026-04-01',
    100000
);


-- Performance bonus policy and evaluation for March.
-- March is quarter 1, so 99004 should receive base_salary * 0.10 + 100000.
INSERT INTO performance_bonus_policy (
    performance_bonus_policy_id,
    eval_year,
    eval_quarter,
    grade,
    bonus_rate,
    fixed_amount
) VALUES (
    99001,
    2026,
    1,
    'TEST',
    0.10,
    100000
);

INSERT INTO performance_evaluation (
    evaluation_id,
    target_emp_id,
    eval_year,
    eval_quarter,
    grade
) VALUES (
    99001,
    99004,
    2026,
    1,
    'TEST'
);

COMMIT;

PROMPT Payroll test data seed completed.
PROMPT Run EXEC create_monthly_payroll(DATE '2026-04-01'); for April scenario.
PROMPT Run EXEC create_monthly_payroll(DATE '2026-03-01'); for March performance bonus scenario.
