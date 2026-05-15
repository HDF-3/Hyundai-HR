package tools;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ScenarioDataSeeder {
    private static final String URL = "jdbc:oracle:thin:@//192.168.2.241:1521/xepdb1";
    private static final String USER = "HDF";
    private static final String PASSWORD = "0000";

    private static final LocalDate SCENARIO_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate ATTENDANCE_END = LocalDate.of(2026, 5, 14);
    private static final LocalDate CLOSED_UNTIL = LocalDate.of(2026, 4, 30);
    private static final Random RANDOM = new Random(20260515L);

    private final List<EmployeeSeed> employees = new ArrayList<>();
    private final Map<Integer, EmployeeSeed> employeeById = new LinkedHashMap<>();
    private final Map<Integer, Integer> managerByDepartment = new LinkedHashMap<>();
    private final Map<Integer, Integer> employeeCountByDepartment = new HashMap<>();
    private final Map<String, LeaveDay> leaveCalendar = new HashMap<>();
    private final Map<Integer, BigDecimal> usedAnnualLeaveByEmployee = new HashMap<>();
    private final List<AttendanceSeed> attendanceRows = new ArrayList<>();

    private int nextEmployeeId = 1016;
    private int nextAssignmentHistoryId = 1;
    private int nextAnnualLeaveId = 1;
    private int nextLeaveRequestId = 1;
    private int nextLeaveApprovalId = 1;
    private int nextAllowanceId = 1;
    private int nextEvaluationId = 1;
    private int nextAttendanceChangeRequestId = 1;

    public static void main(String[] args) throws Exception {
        new ScenarioDataSeeder().run();
    }

    private void run() throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            deleteExistingData(conn);
            insertReferenceData(conn);
            insertDepartments(conn);
            insertSalaryStandards(conn);

            buildEmployees();
            insertEmployees(conn);
            updateDepartmentManagers(conn);
            insertWorkTimes(conn);
            insertAssignmentHistory(conn);

            insertPerformanceBonusPolicies(conn);
            insertPerformanceEvaluations(conn);

            generateLeaveRequests(conn);
            insertAnnualLeaves(conn);

            generateAttendance();
            insertAttendance(conn);
            insertAttendanceChangeRequests(conn);
            insertAdditionalAllowances(conn);

            conn.commit();

            createPayroll(conn);
            updatePayrollStatuses(conn);
            resetSequences(conn);

            conn.commit();
            printSummary(conn);
        }
    }

    private void deleteExistingData(Connection conn) throws SQLException {
        execute(conn, "UPDATE department SET manager_id = NULL, parent_dept_id = NULL");

        List<String> deleteSql = Arrays.asList(
                "DELETE FROM clock_in_time_change",
                "DELETE FROM clock_out_time_change",
                "DELETE FROM attendance_change_request",
                "DELETE FROM leave_approval",
                "DELETE FROM leave_request",
                "DELETE FROM missing_punch",
                "DELETE FROM attendance",
                "DELETE FROM deduction",
                "DELETE FROM earning",
                "DELETE FROM payroll",
                "DELETE FROM additional_allowance",
                "DELETE FROM annual_leave",
                "DELETE FROM assignment_history",
                "DELETE FROM performance_evaluation",
                "DELETE FROM performance_bonus_policy",
                "DELETE FROM work_time",
                "DELETE FROM salary_standard",
                "DELETE FROM employee",
                "DELETE FROM department",
                "DELETE FROM position",
                "DELETE FROM assignment_change_reason",
                "DELETE FROM missing_punch_reason",
                "DELETE FROM request_status",
                "DELETE FROM employment_status"
        );

        for (String sql : deleteSql) {
            execute(conn, sql);
        }
    }

    private void insertReferenceData(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO employment_status(status_id, status_name) VALUES (?, ?)")) {
            addBatch(ps, 1, "재직");
            addBatch(ps, 2, "휴직");
            addBatch(ps, 3, "퇴직");
            ps.executeBatch();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO request_status(request_status_id, request_code, status_name) VALUES (?, ?, ?)")) {
            addBatch(ps, 1, "PENDING", "승인대기");
            addBatch(ps, 2, "APPROVED", "승인");
            addBatch(ps, 3, "REJECTED", "반려");
            addBatch(ps, 4, "CANCELED", "취소");
            addBatch(ps, 5, "DRAFT", "임시저장");
            addBatch(ps, 6, "CALCULATED", "급여산정");
            addBatch(ps, 7, "CONFIRMED", "급여확정");
            addBatch(ps, 8, "PAID", "지급완료");
            ps.executeBatch();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO missing_punch_reason(missing_reason_id, missing_type, missing_reason) VALUES (?, ?, ?)")) {
            addBatch(ps, 1, "CLOCK_IN_MISSING", "출근 체크 누락");
            addBatch(ps, 2, "OUT_SIDE", "외근 및 출장");
            addBatch(ps, 3, "CLOCK_OUT_MISSING", "퇴근 체크 누락");
            addBatch(ps, 4, "LATE", "지각");
            addBatch(ps, 5, "ANNUAL", "연차");
            addBatch(ps, 6, "EARLY_LEAVE", "조퇴");
            addBatch(ps, 7, "SICK", "병가");
            addBatch(ps, 8, "FAMILY_EVENT", "경조사");
            addBatch(ps, 9, "HALF_AM", "오전 반차");
            addBatch(ps, 10, "HALF_PM", "오후 반차");
            ps.executeBatch();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO assignment_change_reason(reason_id, reason_name) VALUES (?, ?)")) {
            addBatch(ps, 1, "입사");
            addBatch(ps, 2, "승진");
            addBatch(ps, 3, "부서 이동");
            addBatch(ps, 4, "직급 변경");
            addBatch(ps, 5, "퇴사");
            ps.executeBatch();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO position(position_id, position_name) VALUES (?, ?)")) {
            addBatch(ps, 1, "대표이사");
            addBatch(ps, 2, "임원");
            addBatch(ps, 3, "팀장");
            addBatch(ps, 4, "책임매니저");
            addBatch(ps, 5, "선임매니저");
            addBatch(ps, 6, "매니저");
            addBatch(ps, 7, "사원");
            addBatch(ps, 8, "인턴");
            ps.executeBatch();
        }
    }

    private void insertDepartments(Connection conn) throws SQLException {
        List<DepartmentSeed> departments = Arrays.asList(
                new DepartmentSeed(100, "경영총괄", "전사 전략, 투자, 리스크 관리 총괄", null),
                new DepartmentSeed(110, "인사팀", "채용, 보상, 조직문화, 노무 관리", 100),
                new DepartmentSeed(120, "재무회계팀", "회계 결산, 세무, 자금 운용", 100),
                new DepartmentSeed(130, "디지털전략본부", "디지털 전환과 사내 플랫폼 전략", 100),
                new DepartmentSeed(131, "IT플랫폼팀", "HR/ERP 사내 플랫폼 개발 및 운영", 130),
                new DepartmentSeed(132, "데이터AI팀", "데이터 분석, AI 모델, 리포팅 자동화", 130),
                new DepartmentSeed(140, "생산운영본부", "공장 운영, 생산 계획, 원가 개선", 100),
                new DepartmentSeed(141, "울산공장", "차량 생산 라인 운영과 현장 안전 관리", 140),
                new DepartmentSeed(142, "품질관리팀", "출하 품질, 공정 품질, 클레임 분석", 140),
                new DepartmentSeed(150, "국내영업팀", "법인/개인 고객 영업과 판매 전략", 100),
                new DepartmentSeed(151, "마케팅팀", "캠페인, 브랜드, 고객 인사이트", 150),
                new DepartmentSeed(160, "구매물류팀", "부품 구매, 협력사, 물류 네트워크", 100),
                new DepartmentSeed(170, "R&D센터", "선행 연구와 신차 개발 총괄", 100),
                new DepartmentSeed(171, "차량SW팀", "커넥티드카, OTA, 차량 제어 SW 개발", 170),
                new DepartmentSeed(180, "법무준법팀", "계약 검토, 컴플라이언스, 분쟁 대응", 100)
        );

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO department(dept_id, dept_name, dept_desc, manager_id, parent_dept_id) VALUES (?, ?, ?, NULL, ?)")) {
            for (DepartmentSeed dept : departments) {
                ps.setInt(1, dept.deptId);
                ps.setString(2, dept.name);
                ps.setString(3, dept.description);
                if (dept.parentDeptId == null) {
                    ps.setNull(4, java.sql.Types.NUMERIC);
                } else {
                    ps.setInt(4, dept.parentDeptId);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }

        managerByDepartment.put(100, 1001);
        managerByDepartment.put(110, 1002);
        managerByDepartment.put(120, 1003);
        managerByDepartment.put(130, 1004);
        managerByDepartment.put(140, 1005);
        managerByDepartment.put(150, 1006);
        managerByDepartment.put(170, 1007);
        managerByDepartment.put(180, 1008);
        managerByDepartment.put(160, 1009);
        managerByDepartment.put(131, 1010);
        managerByDepartment.put(132, 1011);
        managerByDepartment.put(141, 1012);
        managerByDepartment.put(142, 1013);
        managerByDepartment.put(151, 1014);
        managerByDepartment.put(171, 1015);
    }

    private void insertSalaryStandards(Connection conn) throws SQLException {
        int id = 1;
        int[] gradeOneBase = {0, 15000000, 9000000, 6500000, 5200000, 4200000, 3300000, 2800000, 2100000};
        int[] gradeStep = {0, 700000, 500000, 350000, 250000, 220000, 180000, 120000, 50000};

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO salary_standard(salary_standard_id, position_id, pay_grade, base_salary) VALUES (?, ?, ?, ?)")) {
            for (int positionId = 1; positionId <= 8; positionId++) {
                for (int grade = 1; grade <= 10; grade++) {
                    ps.setInt(1, id++);
                    ps.setInt(2, positionId);
                    ps.setInt(3, grade);
                    ps.setInt(4, gradeOneBase[positionId] + (grade - 1) * gradeStep[positionId]);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void buildEmployees() {
        addEmployee(new EmployeeSeed(1001, 100, 1, 1, "김현준", LocalDate.of(2012, 1, 2), null, "M", 10, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1002, 110, 2, 1, "박서연", LocalDate.of(2014, 3, 3), null, "F", 9, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1003, 120, 2, 1, "이도윤", LocalDate.of(2013, 6, 10), null, "M", 9, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1004, 130, 2, 1, "최민재", LocalDate.of(2015, 2, 2), null, "M", 8, "Y", "10:00", "19:00"));
        addEmployee(new EmployeeSeed(1005, 140, 2, 1, "정하윤", LocalDate.of(2014, 9, 1), null, "F", 8, "Y", "08:30", "17:30"));
        addEmployee(new EmployeeSeed(1006, 150, 2, 1, "강지훈", LocalDate.of(2016, 1, 4), null, "M", 8, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1007, 170, 2, 1, "윤서진", LocalDate.of(2015, 5, 11), null, "F", 8, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1008, 180, 3, 1, "장민서", LocalDate.of(2017, 4, 3), null, "F", 8, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1009, 160, 3, 1, "한지우", LocalDate.of(2017, 7, 10), null, "M", 8, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1010, 131, 3, 1, "오세훈", LocalDate.of(2018, 1, 8), null, "M", 8, "Y", "10:00", "19:00"));
        addEmployee(new EmployeeSeed(1011, 132, 3, 1, "배유진", LocalDate.of(2018, 3, 5), null, "F", 8, "Y", "10:00", "19:00"));
        addEmployee(new EmployeeSeed(1012, 141, 3, 1, "신민호", LocalDate.of(2016, 8, 16), null, "M", 8, "Y", "08:30", "17:30"));
        addEmployee(new EmployeeSeed(1013, 142, 3, 1, "서지아", LocalDate.of(2017, 10, 2), null, "F", 8, "Y", "08:30", "17:30"));
        addEmployee(new EmployeeSeed(1014, 151, 3, 1, "문태준", LocalDate.of(2018, 6, 4), null, "M", 8, "Y", "09:00", "18:00"));
        addEmployee(new EmployeeSeed(1015, 171, 3, 1, "권도현", LocalDate.of(2017, 2, 6), null, "M", 8, "Y", "10:00", "19:00"));

        int[][] targetCounts = {
                {100, 3}, {110, 9}, {120, 8}, {130, 4}, {131, 12},
                {132, 10}, {140, 4}, {141, 20}, {142, 11}, {150, 11},
                {151, 8}, {160, 10}, {170, 4}, {171, 13}, {180, 6}
        };

        for (int[] target : targetCounts) {
            int deptId = target[0];
            int count = target[1];
            while (employeeCountByDepartment.getOrDefault(deptId, 0) < count) {
                addGeneratedEmployee(deptId);
            }
        }
    }

    private void addGeneratedEmployee(int deptId) {
        int id = nextEmployeeId++;
        int positionId = choosePosition(deptId);
        int payGrade = choosePayGrade(positionId);
        LocalDate hireDate = chooseHireDate(id);
        int statusId = 1;
        LocalDate resignDate = null;

        if (id % 37 == 0 && hireDate.isBefore(LocalDate.of(2025, 1, 1))) {
            statusId = 3;
            resignDate = chooseResignDate(id);
        } else if (id % 29 == 0) {
            statusId = 2;
        }

        String gender = id % 2 == 0 ? "F" : "M";
        String[] schedule = chooseSchedule(deptId);
        addEmployee(new EmployeeSeed(
                id,
                deptId,
                positionId,
                statusId,
                generateName(id),
                hireDate,
                resignDate,
                gender,
                payGrade,
                "N",
                schedule[0],
                schedule[1]
        ));
    }

    private void addEmployee(EmployeeSeed employee) {
        employee.contact = phoneNumber(employee.empId);
        employee.email = "emp" + employee.empId + "@hyundai.example.com";
        employee.address = addressForDept(employee.deptId);
        employee.salaryAccount = bankAccount(employee.empId);
        employee.password = "0000";

        employees.add(employee);
        employeeById.put(employee.empId, employee);
        employeeCountByDepartment.put(employee.deptId, employeeCountByDepartment.getOrDefault(employee.deptId, 0) + 1);
    }

    private void insertEmployees(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO employee(emp_id, dept_id, position_id, status_id, name, hire_date, resign_date, contact, gender, email, address, salary_account, is_admin, pay_grade, password) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (EmployeeSeed employee : employees) {
                ps.setInt(1, employee.empId);
                ps.setInt(2, employee.deptId);
                ps.setInt(3, employee.positionId);
                ps.setInt(4, employee.statusId);
                ps.setString(5, employee.name);
                ps.setDate(6, Date.valueOf(employee.hireDate));
                setDate(ps, 7, employee.resignDate);
                ps.setString(8, employee.contact);
                ps.setString(9, employee.gender);
                ps.setString(10, employee.email);
                ps.setString(11, employee.address);
                ps.setString(12, employee.salaryAccount);
                ps.setString(13, employee.isAdmin);
                ps.setInt(14, employee.payGrade);
                ps.setString(15, employee.password);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void updateDepartmentManagers(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE department SET manager_id = ? WHERE dept_id = ?")) {
            for (Map.Entry<Integer, Integer> entry : managerByDepartment.entrySet()) {
                ps.setInt(1, entry.getValue());
                ps.setInt(2, entry.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertWorkTimes(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO work_time wt "
                        + "USING (SELECT ? emp_id, ? on_work_time, ? off_work_time, ? applied_date FROM dual) src "
                        + "ON (wt.emp_id = src.emp_id AND wt.applied_date = src.applied_date) "
                        + "WHEN MATCHED THEN UPDATE SET wt.on_work_time = src.on_work_time, wt.off_work_time = src.off_work_time "
                        + "WHEN NOT MATCHED THEN INSERT (emp_id, on_work_time, off_work_time, applied_date) "
                        + "VALUES (src.emp_id, src.on_work_time, src.off_work_time, src.applied_date)")) {
            for (EmployeeSeed employee : employees) {
                ps.setInt(1, employee.empId);
                ps.setString(2, employee.onWorkTime);
                ps.setString(3, employee.offWorkTime);
                ps.setDate(4, Date.valueOf(LocalDate.of(9999, 12, 31)));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertAssignmentHistory(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO assignment_history(history_id, emp_id, position_id, dept_id, reason_id, is_current, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (EmployeeSeed employee : employees) {
                boolean hasPastAssignment = employee.empId % 9 == 0 && employee.positionId > 4;

                if (hasPastAssignment) {
                    ps.setInt(1, nextAssignmentHistoryId++);
                    ps.setInt(2, employee.empId);
                    ps.setInt(3, Math.min(employee.positionId + 1, 8));
                    ps.setInt(4, employee.deptId);
                    ps.setInt(5, 1);
                    ps.setString(6, "N");
                    ps.setDate(7, Date.valueOf(employee.hireDate));
                    ps.setDate(8, Date.valueOf(LocalDate.of(2025, 12, 31)));
                    ps.addBatch();

                    ps.setInt(1, nextAssignmentHistoryId++);
                    ps.setInt(2, employee.empId);
                    ps.setInt(3, employee.positionId);
                    ps.setInt(4, employee.deptId);
                    ps.setInt(5, 2);
                    ps.setString(6, employee.statusId == 3 ? "N" : "Y");
                    ps.setDate(7, Date.valueOf(LocalDate.of(2026, 1, 1)));
                    setDate(ps, 8, employee.resignDate);
                    ps.addBatch();
                } else {
                    ps.setInt(1, nextAssignmentHistoryId++);
                    ps.setInt(2, employee.empId);
                    ps.setInt(3, employee.positionId);
                    ps.setInt(4, employee.deptId);
                    ps.setInt(5, employee.statusId == 3 ? 5 : 1);
                    ps.setString(6, employee.statusId == 3 ? "N" : "Y");
                    ps.setDate(7, Date.valueOf(employee.hireDate));
                    setDate(ps, 8, employee.resignDate);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void insertPerformanceBonusPolicies(Connection conn) throws SQLException {
        int id = 1;
        String[] grades = {"S", "A", "B", "C", "D"};
        double[] rates = {0.25, 0.15, 0.08, 0.03, 0.0};
        int[] fixedAmounts = {700000, 400000, 200000, 0, 0};

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO performance_bonus_policy(performance_bonus_policy_id, eval_year, eval_quarter, grade, bonus_rate, fixed_amount) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int quarter = 1; quarter <= 2; quarter++) {
                for (int i = 0; i < grades.length; i++) {
                    ps.setInt(1, id++);
                    ps.setInt(2, 2026);
                    ps.setInt(3, quarter);
                    ps.setString(4, grades[i]);
                    ps.setBigDecimal(5, BigDecimal.valueOf(rates[i]));
                    ps.setInt(6, fixedAmounts[i]);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void insertPerformanceEvaluations(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO performance_evaluation(evaluation_id, target_emp_id, eval_year, eval_quarter, grade) VALUES (?, ?, ?, ?, ?)")) {
            for (EmployeeSeed employee : employees) {
                if (!employee.hireDate.isBefore(LocalDate.of(2026, 4, 1))) {
                    continue;
                }

                ps.setInt(1, nextEvaluationId++);
                ps.setInt(2, employee.empId);
                ps.setInt(3, 2026);
                ps.setInt(4, 1);
                ps.setString(5, choosePerformanceGrade(employee));
                ps.addBatch();

                if (employee.statusId == 1 && RANDOM.nextInt(100) < 60) {
                    ps.setInt(1, nextEvaluationId++);
                    ps.setInt(2, employee.empId);
                    ps.setInt(3, 2026);
                    ps.setInt(4, 2);
                    ps.setString(5, choosePerformanceGrade(employee));
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private void generateLeaveRequests(Connection conn) throws SQLException {
        try (PreparedStatement requestPs = conn.prepareStatement(
                "INSERT INTO leave_request(leave_request_id, employee_id, leave_reason, start_date, end_date, leave_type_code, request_status) VALUES (?, ?, ?, ?, ?, ?, ?)");
             PreparedStatement approvalPs = conn.prepareStatement(
                     "INSERT INTO leave_approval(leave_approval_id, approver_id, leave_request_id, approval_date) VALUES (?, ?, ?, ?)")) {
            for (EmployeeSeed employee : employees) {
                if (employee.statusId == 3 && employee.resignDate != null && employee.resignDate.isBefore(SCENARIO_START)) {
                    continue;
                }

                int requestCount = 2 + RANDOM.nextInt(3);
                for (int i = 0; i < requestCount; i++) {
                    String leaveType = chooseLeaveType(i);
                    String status = chooseLeaveStatus(i);
                    LocalDate start = chooseLeaveStartDate(status, i);
                    int duration = leaveType.startsWith("HALF") ? 1 : 1 + RANDOM.nextInt(leaveType.equals("ANNUAL") ? 3 : 2);
                    LocalDate end = addWorkDays(start, duration - 1);

                    if (start.isBefore(employee.hireDate)) {
                        continue;
                    }
                    if (employee.resignDate != null && start.isAfter(employee.resignDate)) {
                        continue;
                    }

                    int requestId = nextLeaveRequestId++;
                    requestPs.setInt(1, requestId);
                    requestPs.setInt(2, employee.empId);
                    requestPs.setString(3, leaveReason(leaveType));
                    requestPs.setDate(4, Date.valueOf(start));
                    requestPs.setDate(5, Date.valueOf(end));
                    requestPs.setString(6, leaveType);
                    requestPs.setString(7, status);
                    requestPs.addBatch();

                    if ("APPROVED".equals(status)) {
                        markApprovedLeave(employee, leaveType, start, end);
                    }

                    if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
                        approvalPs.setInt(1, nextLeaveApprovalId++);
                        approvalPs.setInt(2, approverFor(employee));
                        approvalPs.setInt(3, requestId);
                        approvalPs.setTimestamp(4, Timestamp.valueOf(start.minusDays(3 + RANDOM.nextInt(7)).atTime(15, 30)));
                        approvalPs.addBatch();
                    }
                }
            }

            requestPs.executeBatch();
            approvalPs.executeBatch();
        }
    }

    private void markApprovedLeave(EmployeeSeed employee, String leaveType, LocalDate start, LocalDate end) {
        LocalDate cursor = start;
        while (!cursor.isAfter(end) && !cursor.isAfter(ATTENDANCE_END)) {
            if (isStandardWorkDay(cursor)) {
                int reasonId = missingReasonForLeaveType(leaveType);
                leaveCalendar.putIfAbsent(leaveKey(employee.empId, cursor), new LeaveDay(leaveType, reasonId));

                if ("ANNUAL".equals(leaveType)) {
                    addUsedAnnualLeave(employee.empId, BigDecimal.ONE);
                } else if ("HALF_AM".equals(leaveType) || "HALF_PM".equals(leaveType)) {
                    addUsedAnnualLeave(employee.empId, BigDecimal.valueOf(0.5));
                }
            }
            cursor = cursor.plusDays(1);
        }
    }

    private void insertAnnualLeaves(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO annual_leave(annual_leave_id, emp_id, granted_at, expired_at, granted_annual_leave, used_annual_leave, remaining_annual_leave, is_active) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (EmployeeSeed employee : employees) {
                BigDecimal granted = BigDecimal.valueOf(calculateGrantedAnnualLeave(employee));
                BigDecimal used = usedAnnualLeaveByEmployee.getOrDefault(employee.empId, BigDecimal.ZERO);
                BigDecimal remaining = granted.subtract(used);
                if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                    remaining = BigDecimal.ZERO;
                }

                ps.setInt(1, nextAnnualLeaveId++);
                ps.setInt(2, employee.empId);
                ps.setDate(3, Date.valueOf(LocalDate.of(2026, 1, 1)));
                ps.setDate(4, Date.valueOf(LocalDate.of(2026, 12, 31)));
                ps.setBigDecimal(5, granted);
                ps.setBigDecimal(6, used);
                ps.setBigDecimal(7, remaining);
                ps.setString(8, employee.statusId == 3 ? "N" : "Y");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void generateAttendance() {
        LocalDate cursor = SCENARIO_START;
        Set<LocalDate> holidays = new HashSet<>(Arrays.asList(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 16),
                LocalDate.of(2026, 2, 17),
                LocalDate.of(2026, 2, 18),
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 5, 5)
        ));

        while (!cursor.isAfter(ATTENDANCE_END)) {
            for (EmployeeSeed employee : employees) {
                if (!shouldCreateAttendance(employee, cursor, holidays)) {
                    continue;
                }

                String closed = !cursor.isAfter(CLOSED_UNTIL) ? "Y" : "N";
                LeaveDay leaveDay = leaveCalendar.get(leaveKey(employee.empId, cursor));
                if (leaveDay != null) {
                    attendanceRows.add(attendanceForLeave(employee, cursor, closed, leaveDay));
                } else {
                    attendanceRows.add(attendanceForWork(employee, cursor, closed));
                }
            }
            cursor = cursor.plusDays(1);
        }
    }

    private AttendanceSeed attendanceForLeave(EmployeeSeed employee, LocalDate date, String closed, LeaveDay leaveDay) {
        LocalTime start = LocalTime.parse(employee.onWorkTime);
        LocalTime end = LocalTime.parse(employee.offWorkTime);

        Timestamp on = null;
        Timestamp off = null;
        if ("HALF_AM".equals(leaveDay.leaveType)) {
            on = Timestamp.valueOf(date.atTime(LocalTime.of(13, 30).plusMinutes(RANDOM.nextInt(8))));
            off = Timestamp.valueOf(date.atTime(end.plusMinutes(5 + RANDOM.nextInt(20))));
        } else if ("HALF_PM".equals(leaveDay.leaveType)) {
            on = Timestamp.valueOf(date.atTime(start.plusMinutes(RANDOM.nextInt(10))));
            off = Timestamp.valueOf(date.atTime(LocalTime.of(14, 0).plusMinutes(RANDOM.nextInt(15))));
        }

        return new AttendanceSeed(employee.empId, date, on, off, closed, leaveDay.reasonId);
    }

    private AttendanceSeed attendanceForWork(EmployeeSeed employee, LocalDate date, String closed) {
        LocalTime start = LocalTime.parse(employee.onWorkTime);
        LocalTime end = LocalTime.parse(employee.offWorkTime);
        int onOffset = -8 + RANDOM.nextInt(20);
        int offOffset = -5 + RANDOM.nextInt(25);
        Integer missingReasonId = null;

        if (RANDOM.nextInt(100) < 5) {
            onOffset = 15 + RANDOM.nextInt(45);
            missingReasonId = 4;
        }

        boolean overtime = RANDOM.nextInt(100) < overtimeRate(employee);
        if (overtime) {
            offOffset += 35 + RANDOM.nextInt(140);
        }

        Timestamp on = Timestamp.valueOf(date.atTime(start.plusMinutes(onOffset)));
        Timestamp off = Timestamp.valueOf(date.atTime(end.plusMinutes(offOffset)));

        if (RANDOM.nextInt(1000) < 7) {
            on = null;
            missingReasonId = 1;
        } else if (RANDOM.nextInt(1000) < 8) {
            off = null;
            missingReasonId = 3;
        } else if (RANDOM.nextInt(1000) < 5) {
            missingReasonId = 2;
        }

        return new AttendanceSeed(employee.empId, date, on, off, closed, missingReasonId);
    }

    private void insertAttendance(Connection conn) throws SQLException {
        try (PreparedStatement attendancePs = conn.prepareStatement(
                "INSERT INTO attendance(emp_id, work_date, on_work_time, off_work_time, is_closed) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement missingPs = conn.prepareStatement(
                     "INSERT INTO missing_punch(emp_id, work_date, missing_reason_id) VALUES (?, ?, ?)")) {
            for (AttendanceSeed row : attendanceRows) {
                attendancePs.setInt(1, row.empId);
                attendancePs.setDate(2, Date.valueOf(row.workDate));
                setTimestamp(attendancePs, 3, row.onWorkTime);
                setTimestamp(attendancePs, 4, row.offWorkTime);
                attendancePs.setString(5, row.isClosed);
                attendancePs.addBatch();

                if (row.missingReasonId != null) {
                    missingPs.setInt(1, row.empId);
                    missingPs.setDate(2, Date.valueOf(row.workDate));
                    missingPs.setInt(3, row.missingReasonId);
                    missingPs.addBatch();
                }
            }

            attendancePs.executeBatch();
            missingPs.executeBatch();
        }
    }

    private void insertAttendanceChangeRequests(Connection conn) throws SQLException {
        List<AttendanceSeed> candidates = new ArrayList<>();
        for (AttendanceSeed row : attendanceRows) {
            if (row.onWorkTime != null && row.offWorkTime != null && row.workDate.isBefore(LocalDate.of(2026, 5, 1))) {
                candidates.add(row);
            }
        }

        try (PreparedStatement requestPs = conn.prepareStatement(
                "INSERT INTO attendance_change_request(emp_id, work_date, is_on_work_time_modified, is_off_work_time_modified, attendance_change_request_id, cancel_req_id, status) "
                        + "VALUES (?, ?, ?, ?, ?, NULL, ?)");
             PreparedStatement inPs = conn.prepareStatement(
                     "INSERT INTO clock_in_time_change(time_old, time_new, attendance_change_request_id) VALUES (?, ?, ?)");
             PreparedStatement outPs = conn.prepareStatement(
                     "INSERT INTO clock_out_time_change(time_old, time_new, attendance_change_request_id) VALUES (?, ?, ?)");
             PreparedStatement attendanceUpdatePs = conn.prepareStatement(
                     "UPDATE attendance SET on_work_time = ?, off_work_time = ? WHERE emp_id = ? AND work_date = ?")) {
            for (int i = 0; i < 90 && !candidates.isEmpty(); i++) {
                AttendanceSeed row = candidates.get(RANDOM.nextInt(candidates.size()));
                boolean changeIn = i % 3 == 0;
                boolean changeOut = !changeIn || i % 5 == 0;
                String status = changeRequestStatus(i);
                int requestId = nextAttendanceChangeRequestId++;

                Timestamp newIn = row.onWorkTime;
                Timestamp newOut = row.offWorkTime;
                if (changeIn) {
                    newIn = Timestamp.valueOf(row.onWorkTime.toLocalDateTime().plusMinutes(-10 + RANDOM.nextInt(25)));
                }
                if (changeOut) {
                    newOut = Timestamp.valueOf(row.offWorkTime.toLocalDateTime().plusMinutes(20 + RANDOM.nextInt(55)));
                }

                requestPs.setInt(1, row.empId);
                requestPs.setDate(2, Date.valueOf(row.workDate));
                requestPs.setString(3, changeIn ? "Y" : "N");
                requestPs.setString(4, changeOut ? "Y" : "N");
                requestPs.setInt(5, requestId);
                requestPs.setString(6, status);
                requestPs.addBatch();

                if (changeIn) {
                    inPs.setTimestamp(1, row.onWorkTime);
                    inPs.setTimestamp(2, newIn);
                    inPs.setInt(3, requestId);
                    inPs.addBatch();
                }

                if (changeOut) {
                    outPs.setTimestamp(1, row.offWorkTime);
                    outPs.setTimestamp(2, newOut);
                    outPs.setInt(3, requestId);
                    outPs.addBatch();
                }

                if ("APPROVED".equals(status)) {
                    attendanceUpdatePs.setTimestamp(1, newIn);
                    attendanceUpdatePs.setTimestamp(2, newOut);
                    attendanceUpdatePs.setInt(3, row.empId);
                    attendanceUpdatePs.setDate(4, Date.valueOf(row.workDate));
                    attendanceUpdatePs.addBatch();
                }
            }

            requestPs.executeBatch();
            inPs.executeBatch();
            outPs.executeBatch();
            attendanceUpdatePs.executeBatch();
        }
    }

    private void insertAdditionalAllowances(Connection conn) throws SQLException {
        String[] allowanceNames = {
                "야근식대", "출장비", "프로젝트수당", "자격수당", "현장근무수당", "교통보전", "당직수당"
        };

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO additional_allowance(additional_allowance_id, employee_id, additional_allowance_name, allowance_year_month, amount) VALUES (?, ?, ?, ?, ?)")) {
            for (EmployeeSeed employee : employees) {
                for (int month = 1; month <= 5; month++) {
                    LocalDate monthStart = LocalDate.of(2026, month, 1);
                    if (monthStart.isBefore(employee.hireDate.withDayOfMonth(1))) {
                        continue;
                    }
                    if (employee.resignDate != null && monthStart.isAfter(employee.resignDate.withDayOfMonth(1))) {
                        continue;
                    }

                    if (managerByDepartment.containsValue(employee.empId)) {
                        addAllowance(ps, employee.empId, "직책수당", monthStart, 250000 + employee.positionId * 20000);
                    }

                    int chance = RANDOM.nextInt(100);
                    if (chance < 55) {
                        addAllowance(ps, employee.empId, allowanceNames[RANDOM.nextInt(allowanceNames.length)], monthStart, randomAllowanceAmount(employee));
                    }
                    if (chance < 15) {
                        addAllowance(ps, employee.empId, allowanceNames[RANDOM.nextInt(allowanceNames.length)], monthStart, randomAllowanceAmount(employee));
                    }
                }
            }
            ps.executeBatch();
        }
    }

    private void createPayroll(Connection conn) throws SQLException {
        try (CallableStatement cs = conn.prepareCall("{call create_monthly_payroll(?)}")) {
            for (int month = 1; month <= 4; month++) {
                cs.setDate(1, Date.valueOf(LocalDate.of(2026, month, 1)));
                cs.execute();
            }
        }
    }

    private void updatePayrollStatuses(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE payroll SET status = ?, confirmed_at = ?, pay_date = ? WHERE payroll_year_month = ?")) {
            updatePayrollMonth(ps, "PAID", LocalDate.of(2026, 2, 5), LocalDate.of(2026, 2, 10), LocalDate.of(2026, 1, 1));
            updatePayrollMonth(ps, "PAID", LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 10), LocalDate.of(2026, 2, 1));
            updatePayrollMonth(ps, "CONFIRMED", LocalDate.of(2026, 4, 5), null, LocalDate.of(2026, 3, 1));
            ps.executeBatch();
        }
    }

    private void resetSequences(Connection conn) throws SQLException {
        resetSequence(conn, "EMPNO_SEQ", "employee", "emp_id");
        resetSequence(conn, "ATTENDANCE_CHANGE_REQUEST_ID", "attendance_change_request", "attendance_change_request_id");
        resetSequence(conn, "SEQ_ADDITIONAL_ALLOWANCE_ID", "additional_allowance", "additional_allowance_id");
        resetSequence(conn, "SEQ_ANNUAL_LEAVE_ID", "annual_leave", "annual_leave_id");
        resetSequence(conn, "SEQ_BONUS_ID", "performance_evaluation", "evaluation_id");
        resetSequence(conn, "SEQ_DEDUCTION_ID", "deduction", "deduction_id");
        resetSequence(conn, "SEQ_EARNING_ID", "earning", "earning_id");
        resetSequence(conn, "SEQ_LEAVE_APPROVAL_ID", "leave_approval", "leave_approval_id");
        resetSequence(conn, "SEQ_LEAVE_REQUEST_ID", "leave_request", "leave_request_id");
        resetSequence(conn, "SEQ_PAYROLL_ID", "payroll", "payroll_id");
        resetSequence(conn, "SEQ_PERFORMANCE_BONUS_POLICY", "performance_bonus_policy", "performance_bonus_policy_id");
        resetSequence(conn, "SEQ_SALARY_STANDARD_ID", "salary_standard", "salary_standard_id");
    }

    private void printSummary(Connection conn) throws SQLException {
        String[] tables = {
                "department", "employee", "work_time", "annual_leave", "leave_request", "leave_approval",
                "attendance", "missing_punch", "attendance_change_request", "clock_in_time_change",
                "clock_out_time_change", "additional_allowance", "performance_evaluation",
                "performance_bonus_policy", "payroll", "earning", "deduction"
        };

        System.out.println("=== Hyundai HR scenario seed completed ===");
        for (String table : tables) {
            System.out.printf("%-32s %,8d%n", table, countRows(conn, table));
        }
        System.out.println("Login samples: admin 1001 / 0000, HR admin 1002 / 0000, employee 1040 / 0000");
    }

    private int choosePosition(int deptId) {
        int value = RANDOM.nextInt(100);
        if (deptId == 100 || deptId == 130 || deptId == 140 || deptId == 170) {
            if (value < 30) {
                return 2;
            }
            if (value < 65) {
                return 4;
            }
            return 5;
        }
        if (deptId == 141 || deptId == 142) {
            if (value < 12) {
                return 4;
            }
            if (value < 35) {
                return 5;
            }
            if (value < 72) {
                return 6;
            }
            return 7;
        }
        if (deptId == 131 || deptId == 132 || deptId == 171) {
            if (value < 22) {
                return 4;
            }
            if (value < 50) {
                return 5;
            }
            if (value < 84) {
                return 6;
            }
            return 7;
        }
        if (value < 15) {
            return 4;
        }
        if (value < 42) {
            return 5;
        }
        if (value < 82) {
            return 6;
        }
        if (value < 96) {
            return 7;
        }
        return 8;
    }

    private int choosePayGrade(int positionId) {
        switch (positionId) {
            case 1:
                return 10;
            case 2:
                return 8 + RANDOM.nextInt(3);
            case 3:
                return 7 + RANDOM.nextInt(3);
            case 4:
                return 5 + RANDOM.nextInt(4);
            case 5:
                return 4 + RANDOM.nextInt(3);
            case 6:
                return 2 + RANDOM.nextInt(4);
            case 7:
                return 1 + RANDOM.nextInt(3);
            default:
                return 1;
        }
    }

    private LocalDate chooseHireDate(int id) {
        if (id % 23 == 0) {
            return LocalDate.of(2026, 1 + RANDOM.nextInt(3), 2 + RANDOM.nextInt(20));
        }
        int year = 2014 + RANDOM.nextInt(12);
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(24);
        return LocalDate.of(year, month, day);
    }

    private LocalDate chooseResignDate(int id) {
        LocalDate[] dates = {
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 5, 10)
        };
        return dates[id % dates.length];
    }

    private String[] chooseSchedule(int deptId) {
        if (deptId == 141 || deptId == 142 || deptId == 140) {
            return new String[] {"08:30", "17:30"};
        }
        if (deptId == 131 || deptId == 132 || deptId == 171) {
            return RANDOM.nextBoolean() ? new String[] {"10:00", "19:00"} : new String[] {"09:00", "18:00"};
        }
        return new String[] {"09:00", "18:00"};
    }

    private String generateName(int id) {
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황"};
        String[] firstNames = {"민준", "서연", "도윤", "하윤", "지호", "서아", "유준", "지우", "현우", "수아", "준서", "민서", "예준", "윤서", "지민", "다은", "하준", "소윤", "태준", "은서"};
        return lastNames[id % lastNames.length] + firstNames[(id * 7) % firstNames.length];
    }

    private String phoneNumber(int id) {
        return String.format("010-%04d-%04d", 2000 + (id % 7000), 1000 + ((id * 13) % 9000));
    }

    private String bankAccount(int id) {
        return String.format("088-%06d-%05d", 400000 + id, 10000 + (id * 17) % 90000);
    }

    private String addressForDept(int deptId) {
        if (deptId == 141 || deptId == 142 || deptId == 140) {
            return "울산광역시 북구 염포로";
        }
        if (deptId == 171 || deptId == 170) {
            return "경기도 화성시 남양읍 연구단지로";
        }
        return "서울특별시 서초구 헌릉로";
    }

    private String choosePerformanceGrade(EmployeeSeed employee) {
        int value = RANDOM.nextInt(100);
        int leadershipBonus = managerByDepartment.containsValue(employee.empId) ? 8 : 0;
        value += leadershipBonus;
        if (value >= 93) {
            return "S";
        }
        if (value >= 72) {
            return "A";
        }
        if (value >= 28) {
            return "B";
        }
        if (value >= 7) {
            return "C";
        }
        return "D";
    }

    private String chooseLeaveType(int index) {
        int value = RANDOM.nextInt(100);
        if (index == 0 || value < 46) {
            return "ANNUAL";
        }
        if (value < 61) {
            return "SICK";
        }
        if (value < 73) {
            return "FAMILY_EVENT";
        }
        if (value < 83) {
            return "HALF_AM";
        }
        if (value < 94) {
            return "HALF_PM";
        }
        return "OUT_SIDE";
    }

    private String chooseLeaveStatus(int index) {
        if (index == 0) {
            return "APPROVED";
        }
        int value = RANDOM.nextInt(100);
        if (value < 62) {
            return "APPROVED";
        }
        if (value < 78) {
            return "PENDING";
        }
        if (value < 91) {
            return "REJECTED";
        }
        return "CANCELED";
    }

    private LocalDate chooseLeaveStartDate(String status, int index) {
        if ("PENDING".equals(status)) {
            return LocalDate.of(2026, 5, 15).plusDays(RANDOM.nextInt(36));
        }
        int month = 1 + RANDOM.nextInt(5);
        int day = 2 + RANDOM.nextInt(month == 2 ? 20 : 24);
        LocalDate date = LocalDate.of(2026, month, day);
        while (!isStandardWorkDay(date)) {
            date = date.plusDays(1);
        }
        if (index == 1 && RANDOM.nextBoolean()) {
            date = LocalDate.of(2026, 4, 27 + RANDOM.nextInt(3));
        }
        return date;
    }

    private String leaveReason(String leaveType) {
        switch (leaveType) {
            case "ANNUAL":
                return "개인 일정 및 재충전";
            case "SICK":
                return "진료 및 회복";
            case "FAMILY_EVENT":
                return "가족 행사 참석";
            case "HALF_AM":
                return "오전 개인 용무";
            case "HALF_PM":
                return "오후 개인 용무";
            case "OUT_SIDE":
                return "협력사 방문 및 외근";
            default:
                return "휴가 신청";
        }
    }

    private int missingReasonForLeaveType(String leaveType) {
        switch (leaveType) {
            case "ANNUAL":
                return 5;
            case "SICK":
                return 7;
            case "FAMILY_EVENT":
                return 8;
            case "HALF_AM":
                return 9;
            case "HALF_PM":
                return 10;
            case "OUT_SIDE":
                return 2;
            default:
                return 5;
        }
    }

    private int approverFor(EmployeeSeed employee) {
        int managerId = managerByDepartment.getOrDefault(employee.deptId, 1001);
        if (managerId == employee.empId) {
            return 1001;
        }
        return managerId;
    }

    private void addUsedAnnualLeave(int empId, BigDecimal amount) {
        BigDecimal current = usedAnnualLeaveByEmployee.getOrDefault(empId, BigDecimal.ZERO);
        usedAnnualLeaveByEmployee.put(empId, current.add(amount));
    }

    private int calculateGrantedAnnualLeave(EmployeeSeed employee) {
        if (employee.hireDate.getYear() == 2026) {
            return 11;
        }
        int years = Math.max(0, Period.between(employee.hireDate, LocalDate.of(2026, 1, 1)).getYears());
        return Math.min(25, 15 + years / 2);
    }

    private boolean shouldCreateAttendance(EmployeeSeed employee, LocalDate date, Set<LocalDate> holidays) {
        if (date.isBefore(employee.hireDate)) {
            return false;
        }
        if (employee.resignDate != null && date.isAfter(employee.resignDate)) {
            return false;
        }
        if (employee.statusId == 2 && !date.isBefore(LocalDate.of(2026, 4, 1))) {
            return false;
        }
        if (holidays.contains(date)) {
            return false;
        }

        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return (employee.deptId == 141 || employee.deptId == 142) && date.getDayOfMonth() % 2 == 0;
        }
        return day != DayOfWeek.SUNDAY;
    }

    private boolean isStandardWorkDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    private LocalDate addWorkDays(LocalDate start, int daysToAdd) {
        LocalDate date = start;
        int added = 0;
        while (added < daysToAdd) {
            date = date.plusDays(1);
            if (isStandardWorkDay(date)) {
                added++;
            }
        }
        return date;
    }

    private int overtimeRate(EmployeeSeed employee) {
        if (employee.deptId == 141 || employee.deptId == 142) {
            return 24;
        }
        if (employee.deptId == 131 || employee.deptId == 132 || employee.deptId == 171) {
            return 21;
        }
        if (employee.deptId == 150 || employee.deptId == 151) {
            return 16;
        }
        return 11;
    }

    private String changeRequestStatus(int index) {
        int value = index % 10;
        if (value < 4) {
            return "APPROVED";
        }
        if (value < 7) {
            return "PENDING";
        }
        if (value < 9) {
            return "REJECTED";
        }
        return "CANCELED";
    }

    private int randomAllowanceAmount(EmployeeSeed employee) {
        int base = 30000 + RANDOM.nextInt(180000);
        if (employee.deptId == 141 || employee.deptId == 142) {
            base += 40000;
        }
        if (employee.positionId <= 4) {
            base += 60000;
        }
        return base;
    }

    private void addAllowance(PreparedStatement ps, int employeeId, String name, LocalDate monthStart, int amount) throws SQLException {
        ps.setInt(1, nextAllowanceId++);
        ps.setInt(2, employeeId);
        ps.setString(3, name);
        ps.setDate(4, Date.valueOf(monthStart.withDayOfMonth(1)));
        ps.setInt(5, amount);
        ps.addBatch();
    }

    private void updatePayrollMonth(PreparedStatement ps, String status, LocalDate confirmedAt, LocalDate payDate, LocalDate payrollMonth) throws SQLException {
        ps.setString(1, status);
        setDate(ps, 2, confirmedAt);
        setDate(ps, 3, payDate);
        ps.setDate(4, Date.valueOf(payrollMonth));
        ps.addBatch();
    }

    private void resetSequence(Connection conn, String sequenceName, String tableName, String columnName) throws SQLException {
        long nextValue = maxId(conn, tableName, columnName) + 1;
        if (nextValue < 1) {
            nextValue = 1;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER SEQUENCE " + sequenceName + " RESTART START WITH " + nextValue);
        } catch (SQLException e) {
            System.out.println("Sequence reset skipped for " + sequenceName + ": " + e.getMessage());
        }
    }

    private long maxId(Connection conn, String tableName, String columnName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NVL(MAX(" + columnName + "), 0) FROM " + tableName)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private int countRows(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String leaveKey(int empId, LocalDate date) {
        return empId + ":" + date;
    }

    private void execute(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private void addBatch(PreparedStatement ps, int id, String name) throws SQLException {
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.addBatch();
    }

    private void addBatch(PreparedStatement ps, int id, String code, String name) throws SQLException {
        ps.setInt(1, id);
        ps.setString(2, code);
        ps.setString(3, name);
        ps.addBatch();
    }

    private void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, java.sql.Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    private void setTimestamp(PreparedStatement ps, int index, Timestamp timestamp) throws SQLException {
        if (timestamp == null) {
            ps.setNull(index, java.sql.Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, timestamp);
        }
    }

    private static class DepartmentSeed {
        private final int deptId;
        private final String name;
        private final String description;
        private final Integer parentDeptId;

        private DepartmentSeed(int deptId, String name, String description, Integer parentDeptId) {
            this.deptId = deptId;
            this.name = name;
            this.description = description;
            this.parentDeptId = parentDeptId;
        }
    }

    private static class EmployeeSeed {
        private final int empId;
        private final int deptId;
        private final int positionId;
        private final int statusId;
        private final String name;
        private final LocalDate hireDate;
        private final LocalDate resignDate;
        private final String gender;
        private final int payGrade;
        private final String isAdmin;
        private final String onWorkTime;
        private final String offWorkTime;

        private String contact;
        private String email;
        private String address;
        private String salaryAccount;
        private String password;

        private EmployeeSeed(
                int empId,
                int deptId,
                int positionId,
                int statusId,
                String name,
                LocalDate hireDate,
                LocalDate resignDate,
                String gender,
                int payGrade,
                String isAdmin,
                String onWorkTime,
                String offWorkTime
        ) {
            this.empId = empId;
            this.deptId = deptId;
            this.positionId = positionId;
            this.statusId = statusId;
            this.name = name;
            this.hireDate = hireDate;
            this.resignDate = resignDate;
            this.gender = gender;
            this.payGrade = payGrade;
            this.isAdmin = isAdmin;
            this.onWorkTime = onWorkTime;
            this.offWorkTime = offWorkTime;
        }
    }

    private static class LeaveDay {
        private final String leaveType;
        private final int reasonId;

        private LeaveDay(String leaveType, int reasonId) {
            this.leaveType = leaveType;
            this.reasonId = reasonId;
        }
    }

    private static class AttendanceSeed {
        private final int empId;
        private final LocalDate workDate;
        private final Timestamp onWorkTime;
        private final Timestamp offWorkTime;
        private final String isClosed;
        private final Integer missingReasonId;

        private AttendanceSeed(
                int empId,
                LocalDate workDate,
                Timestamp onWorkTime,
                Timestamp offWorkTime,
                String isClosed,
                Integer missingReasonId
        ) {
            this.empId = empId;
            this.workDate = workDate;
            this.onWorkTime = onWorkTime;
            this.offWorkTime = offWorkTime;
            this.isClosed = isClosed;
            this.missingReasonId = missingReasonId;
        }
    }
}
