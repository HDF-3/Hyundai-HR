SET DEFINE OFF;

PROMPT [Payroll] schema
@@schema/salary_standard_regular_hourly_rate.sql
@@schema/dao_insert_sequences.sql

PROMPT [Payroll] procedures
@@procedures/check_month_attendance_closed.sql
@@procedures/validate_monthly_payroll.sql
@@procedures/insert_monthly_payroll.sql
@@procedures/insert_monthly_earning.sql
@@procedures/insert_monthly_deduction.sql
@@procedures/update_monthly_payroll_total.sql
@@procedures/refresh_payroll_total.sql
@@procedures/create_monthly_payroll.sql

PROMPT [Payroll] triggers
@@triggers/trg_earning_refresh_payroll.sql
@@triggers/trg_deduction_refresh_payroll.sql

PROMPT [Payroll] scheduler
@@scheduler/job_create_monthly_payroll.sql
