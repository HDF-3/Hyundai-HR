package ui.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import global.types.CommonStatus;
import global.types.LeaveType;
import leave.dto.AnnualLeaveDTO;
import leave.dto.LeaveRequestDTO;
import leave.service.LeaveService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class LeavePanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final LeaveService leaveService = new LeaveService();
    private final JPanel summary = new JPanel(new GridLayout(1, 4, 12, 12));
    private final JTable annualTable = UiKit.table("연차ID", "사번", "부여일", "만료일", "부여", "사용", "잔여", "활성");
    private final JTable requestTable = UiKit.table("신청ID", "사번", "유형", "시작일", "종료일", "상태", "사유");
    private final JLabel statusLabel = UiKit.statusLabel();

    private final JComboBox<LeaveType> leaveTypeCombo = UiKit.combo(LeaveType.values());
    private final UiKit.DateField startDateField = UiKit.dateField(LocalDate.now().plusDays(1), false);
    private final UiKit.DateField endDateField = UiKit.dateField(LocalDate.now().plusDays(1), false);
    private final JTextField reasonField = UiKit.field(30);

    public LeavePanel(AppSession session) {
        this.session = session;
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("휴가", "연차 현황을 확인하고 휴가를 신청하거나 취소합니다.");
        summary.setOpaque(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("연차 현황", annualTab());
        tabs.addTab("휴가 신청", requestTab());
        tabs.addTab("신청 내역", requestListTab());

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(summary, BorderLayout.NORTH);
        body.add(tabs, BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        loadAnnualLeave();
        loadRequests();
    }

    private JPanel annualTab() {
        JButton refresh = UiKit.primaryButton("연차 새로고침");
        refresh.addActionListener(e -> loadAnnualLeave());

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(UiKit.scroll(annualTable), BorderLayout.CENTER);
        panel.add(UiKit.actions(refresh), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel requestTab() {
        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "휴가 유형", leaveTypeCombo);
        UiKit.addField(form, 1, "시작일", startDateField);
        UiKit.addField(form, 2, "종료일", endDateField);
        UiKit.addField(form, 3, "사유", reasonField);

        JButton request = UiKit.primaryButton("휴가 신청");
        request.addActionListener(e -> requestLeave());

        JPanel panel = UiKit.section("휴가 신청 정보", form);
        panel.add(UiKit.actions(request), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel requestListTab() {
        JButton cancel = UiKit.dangerButton("선택 휴가 취소");
        cancel.addActionListener(e -> cancelLeave());

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.add(UiKit.scroll(requestTable), BorderLayout.CENTER);
        panel.add(UiKit.actions(cancel), BorderLayout.SOUTH);
        return panel;
    }

    private void loadAnnualLeave() {
        statusLabel.setText("연차 현황을 불러오는 중...");
        Async.run(this, () -> new AnnualData(
                leaveService.findActiveAnnualLeave(session.getEmployeeId()),
                leaveService.getAnnualLeaveRows(session.getEmployeeId())
        ), data -> {
            renderSummary(data.active);
            UiKit.setRows(annualTable, UiKit.safeList(data.rows).stream()
                    .map(row -> new Object[] {
                            row.getAnnualLeaveId(),
                            row.getEmployeeId(),
                            row.getGrantedAt(),
                            row.getExpiredAt(),
                            row.getGrantedAnnualLeave(),
                            row.getUsedAnnualLeave(),
                            row.getRemainingAnnualLeave(),
                            row.getIsActive()
                    })
                    .collect(Collectors.toList()));
            statusLabel.setText("연차 현황 조회 완료");
        });
    }

    private void renderSummary(AnnualLeaveDTO active) {
        summary.removeAll();
        summary.add(UiKit.metric("사번", UiKit.value(session.getEmployeeId())));
        summary.add(UiKit.metric("부여 연차", active == null ? "-" : active.getGrantedAnnualLeave() + "일"));
        summary.add(UiKit.metric("사용 연차", active == null ? "-" : active.getUsedAnnualLeave() + "일"));
        summary.add(UiKit.metric("잔여 연차", active == null ? "-" : active.getRemainingAnnualLeave() + "일"));
        summary.revalidate();
        summary.repaint();
    }

    private void requestLeave() {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(session.getEmployeeId());
        dto.setLeaveType((LeaveType) leaveTypeCombo.getSelectedItem());
        dto.setStartDate(UiKit.requireDate(startDateField, "시작일"));
        dto.setEndDate(UiKit.requireDate(endDateField, "종료일"));
        dto.setReason(UiKit.text(reasonField));
        dto.setStatus(CommonStatus.PENDING);

        Async.run(this, () -> leaveService.requestLeave(dto), result -> {
            statusLabel.setText(result ? "휴가 신청이 접수되었습니다." : "휴가 신청이 처리되지 않았습니다.");
            loadRequests();
            loadAnnualLeave();
        });
    }

    private void loadRequests() {
        Async.run(this, () -> leaveService.getLeaveRequests(session.getEmployeeId()), rows -> {
            renderRequests(rows);
            statusLabel.setText("휴가 신청 내역: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void renderRequests(List<LeaveRequestDTO> rows) {
        UiKit.setRows(requestTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getLeaveRequestId(),
                        row.getEmployeeId(),
                        row.getLeaveType(),
                        row.getStartDate(),
                        row.getEndDate(),
                        row.getStatus(),
                        row.getReason()
                })
                .collect(Collectors.toList()));
    }

    private void cancelLeave() {
        Long leaveRequestId = UiKit.selectedLong(requestTable, 0);
        Async.run(this, () -> leaveService.cancelLeave(leaveRequestId), result -> {
            statusLabel.setText(result ? "휴가 신청이 취소되었습니다." : "취소할 수 없는 휴가 신청입니다.");
            loadRequests();
            loadAnnualLeave();
        });
    }

    private static class AnnualData {
        private final AnnualLeaveDTO active;
        private final List<AnnualLeaveDTO> rows;

        private AnnualData(AnnualLeaveDTO active, List<AnnualLeaveDTO> rows) {
            this.active = active;
            this.rows = rows;
        }
    }
}
