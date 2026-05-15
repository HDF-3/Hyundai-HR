package ui.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.YearMonth;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import attendance.dto.AttendanceModifyHistoryDTO;
import attendance.service.AttendanceModifyService;
import humanresource.dto.EmployeeInfoDTO;
import humanresource.service.EmployeeService;
import leave.dto.AnnualLeaveDTO;
import leave.dto.LeaveRequestDTO;
import leave.service.LeaveService;
import payroll.dto.PayrollDTO;
import payroll.service.PayrollService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class DashboardPanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final EmployeeService employeeService;
    private final AttendanceModifyService attendanceModifyService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;

    private final JLabel employeeCount = cardValue("...");
    private final JLabel pendingAttendance = cardValue("...");
    private final JLabel pendingLeave = cardValue("...");
    private final JLabel payrollCount = cardValue("...");
    private final JLabel annualLeave = cardValue("...");
    private final JLabel status = UiKit.statusLabel();

    public DashboardPanel(
            AppSession session,
            EmployeeService employeeService,
            AttendanceModifyService attendanceModifyService,
            LeaveService leaveService,
            PayrollService payrollService
    ) {
        this.session = session;
        this.employeeService = employeeService;
        this.attendanceModifyService = attendanceModifyService;
        this.leaveService = leaveService;
        this.payrollService = payrollService;
        build();
    }

    private void build() {
        JPanel root = UiKit.screen("대시보드", "오늘 처리할 HR 업무를 한눈에 봅니다.");
        setLayout(new BorderLayout());
        add(root, BorderLayout.CENTER);

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setOpaque(false);
        grid.add(metric("직원", employeeCount, "등록된 직원 수"));
        grid.add(metric("근태 정정", pendingAttendance, "대기 중인 정정 요청"));
        grid.add(metric("휴가 승인", pendingLeave, "관리자 승인 대기"));
        grid.add(metric("급여", payrollCount, "이번 달 급여 건수"));
        grid.add(metric("내 연차", annualLeave, "잔여 연차"));
        grid.add(todoCard());

        root.add(grid, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel metric(String title, JLabel value, String subtitle) {
        JPanel panel = UiKit.surface();
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UiKit.MUTED);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(UiKit.MUTED);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        panel.add(subtitleLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel todoCard() {
        JPanel panel = UiKit.surface();
        panel.add(new JLabel("TODO 연결부"), BorderLayout.NORTH);
        panel.add(new JLabel("<html>휴가→근태 반영<br>근태 정정 최신상태 조회<br>성과평가 등급 매핑</html>"), BorderLayout.CENTER);
        return panel;
    }

    private static JLabel cardValue(String value) {
        JLabel label = new JLabel(value);
        label.setFont(label.getFont().deriveFont(28f));
        label.setForeground(UiKit.TEXT);
        return label;
    }

    @Override
    public void refresh() {
        status.setText("대시보드 새로고침 중...");
        Async.run(this, () -> loadSummary(), this::applySummary, e -> {
            status.setText("새로고침 실패");
            UiKit.error(this, e);
        });
    }

    private Summary loadSummary() {
        List<EmployeeInfoDTO> employees = employeeService.getEmployeeInfoList();
        List<AttendanceModifyHistoryDTO> attendance = attendanceModifyService.getPendingAttendanceModifyReqs();
        List<LeaveRequestDTO> leaves = session.isAdmin()
                ? leaveService.getPendingApprovalRequests(session.getEmployeeId())
                : java.util.Collections.<LeaveRequestDTO>emptyList();
        List<PayrollDTO> payrolls = payrollService.getPayrollList(YearMonth.now());
        AnnualLeaveDTO activeLeave = session.getEmployeeId() == null
                ? null
                : leaveService.findActiveAnnualLeave(session.getEmployeeId());

        return new Summary(
                employees == null ? 0 : employees.size(),
                attendance == null ? 0 : attendance.size(),
                leaves == null ? 0 : leaves.size(),
                payrolls == null ? 0 : payrolls.size(),
                activeLeave == null ? null : activeLeave.getRemainingAnnualLeave()
        );
    }

    private void applySummary(Summary summary) {
        employeeCount.setText(String.valueOf(summary.employeeCount));
        pendingAttendance.setText(String.valueOf(summary.pendingAttendance));
        pendingLeave.setText(session.isAdmin() ? String.valueOf(summary.pendingLeave) : "-");
        payrollCount.setText(String.valueOf(summary.payrollCount));
        annualLeave.setText(summary.annualLeave == null ? "-" : String.valueOf(summary.annualLeave));
        status.setText("새로고침 완료");
    }

    private static class Summary {
        private final int employeeCount;
        private final int pendingAttendance;
        private final int pendingLeave;
        private final int payrollCount;
        private final Double annualLeave;

        private Summary(int employeeCount, int pendingAttendance, int pendingLeave, int payrollCount, Double annualLeave) {
            this.employeeCount = employeeCount;
            this.pendingAttendance = pendingAttendance;
            this.pendingLeave = pendingLeave;
            this.payrollCount = payrollCount;
            this.annualLeave = annualLeave;
        }
    }
}
