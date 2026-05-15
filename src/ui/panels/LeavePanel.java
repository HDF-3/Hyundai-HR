package ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

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
    private final LeaveService leaveService;

    private final JLabel activeLeaveSummary = new JLabel("-");
    private final DefaultTableModel annualModel = UiKit.model("연차ID", "사번", "부여일", "만료일", "부여", "사용", "잔여", "활성");
    private final JTable annualTable = UiKit.table(annualModel);

    private final JTextField empIdField = UiKit.field(10);
    private final JComboBox<LeaveType> leaveTypeCombo = UiKit.combo(LeaveType.values());
    private final JTextField startField = UiKit.field(10);
    private final JTextField endField = UiKit.field(10);
    private final JTextField reasonField = UiKit.field(24);

    private final DefaultTableModel requestModel = UiKit.model("신청ID", "사번", "유형", "시작", "종료", "상태", "사유");
    private final JTable requestTable = UiKit.table(requestModel);
    private final DefaultTableModel approvalModel = UiKit.model("신청ID", "사번", "유형", "시작", "종료", "상태", "사유");
    private final JTable approvalTable = UiKit.table(approvalModel);
    private final JLabel status = UiKit.statusLabel();

    public LeavePanel(AppSession session, LeaveService leaveService) {
        this.session = session;
        this.leaveService = leaveService;
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        JPanel root = UiKit.screen("휴가", "연차 현황, 휴가 신청, 관리자 승인을 처리합니다.");
        add(root, BorderLayout.CENTER);

        empIdField.setText(UiKit.display(session.getEmployeeId()));
        startField.setText(LocalDate.now().plusDays(1).toString());
        endField.setText(LocalDate.now().plusDays(1).toString());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("연차 현황", annualTab());
        tabs.addTab("휴가 신청", requestTab());
        tabs.addTab("내 신청", myRequestsTab());
        tabs.addTab("승인", approvalTab());
        root.add(tabs, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel annualTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel summary = UiKit.surface();
        summary.add(new JLabel("활성 연차"), BorderLayout.NORTH);
        activeLeaveSummary.setFont(activeLeaveSummary.getFont().deriveFont(22f));
        summary.add(activeLeaveSummary, BorderLayout.CENTER);

        JPanel table = UiKit.surface();
        JButton refresh = UiKit.button("새로고침");
        refresh.addActionListener(e -> refreshAnnualLeave());
        table.add(refresh, BorderLayout.NORTH);
        table.add(UiKit.scroll(annualTable), BorderLayout.CENTER);

        tab.add(summary, BorderLayout.NORTH);
        tab.add(table, BorderLayout.CENTER);
        return tab;
    }

    private JPanel requestTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel formPanel = UiKit.surface();
        formPanel.add(new JLabel("휴가 신청"), BorderLayout.NORTH);
        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "사번", empIdField);
        UiKit.addField(form, row++, "휴가유형", leaveTypeCombo);
        UiKit.addField(form, row++, "시작일", startField);
        UiKit.addField(form, row++, "종료일", endField);
        UiKit.addField(form, row++, "사유", reasonField);
        formPanel.add(form, BorderLayout.CENTER);

        JButton submit = UiKit.primaryButton("신청");
        submit.addActionListener(e -> submitLeaveRequest());
        formPanel.add(submit, BorderLayout.SOUTH);
        tab.add(formPanel, BorderLayout.NORTH);
        tab.add(UiKit.todo("TODO: 외근 자동승인/휴가 승인 후 근태 반영은 백엔드 #2/#5 수정 후 완전 연결됩니다."), BorderLayout.CENTER);
        return tab;
    }

    private JPanel myRequestsTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);
        JPanel panel = UiKit.surface();
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton refresh = UiKit.button("새로고침");
        JButton cancel = UiKit.button("선택 취소");
        refresh.addActionListener(e -> refreshRequests());
        cancel.addActionListener(e -> cancelSelectedLeave());
        toolbar.add(refresh);
        toolbar.add(cancel);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(UiKit.scroll(requestTable), BorderLayout.CENTER);
        tab.add(panel, BorderLayout.CENTER);
        return tab;
    }

    private JPanel approvalTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);
        JPanel panel = UiKit.surface();
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton refresh = UiKit.button("승인 대기");
        JButton approve = UiKit.primaryButton("승인");
        JButton reject = UiKit.button("반려");
        approve.setEnabled(session.isAdmin());
        reject.setEnabled(session.isAdmin());
        refresh.addActionListener(e -> refreshApprovals());
        approve.addActionListener(e -> processSelectedLeave(CommonStatus.APPROVED));
        reject.addActionListener(e -> processSelectedLeave(CommonStatus.REJECTED));
        toolbar.add(refresh);
        toolbar.add(approve);
        toolbar.add(reject);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(UiKit.scroll(approvalTable), BorderLayout.CENTER);
        tab.add(UiKit.todo("TODO: 승인 처리는 가능하지만 근태 테이블 반영은 아직 별도 로직입니다."), BorderLayout.NORTH);
        tab.add(panel, BorderLayout.CENTER);
        return tab;
    }

    @Override
    public void refresh() {
        refreshAnnualLeave();
        refreshRequests();
        refreshApprovals();
    }

    private void refreshAnnualLeave() {
        Long empId = currentEmployeeId();
        if (empId == null) {
            return;
        }
        Async.run(this, () -> new LeaveSnapshot(
                leaveService.findActiveAnnualLeave(empId),
                leaveService.getAnnualLeaveRows(empId)
        ), snapshot -> {
            AnnualLeaveDTO active = snapshot.active;
            if (active == null) {
                activeLeaveSummary.setText("-");
            } else {
                activeLeaveSummary.setText(active.getRemainingAnnualLeave() + "일 남음 / " + active.getGrantedAnnualLeave() + "일 부여");
            }
            annualModel.setRowCount(0);
            for (AnnualLeaveDTO dto : safe(snapshot.rows)) {
                annualModel.addRow(new Object[] {
                        dto.getAnnualLeaveId(),
                        dto.getEmployeeId(),
                        dto.getGrantedAt(),
                        dto.getExpiredAt(),
                        dto.getGrantedAnnualLeave(),
                        dto.getUsedAnnualLeave(),
                        dto.getRemainingAnnualLeave(),
                        dto.getIsActive()
                });
            }
        }, e -> UiKit.error(this, e));
    }

    private void submitLeaveRequest() {
        try {
            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setEmployeeId(requiredEmpId());
            dto.setLeaveType((LeaveType) leaveTypeCombo.getSelectedItem());
            dto.setStartDate(UiKit.dateValue(startField));
            dto.setEndDate(UiKit.dateValue(endField));
            dto.setReason(UiKit.text(reasonField));
            dto.setStatus(CommonStatus.PENDING);

            Async.run(this, () -> leaveService.requestLeave(dto), result -> {
                status.setText(result ? "휴가 신청 완료" : "휴가 신청 실패");
                refresh();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void refreshRequests() {
        Long empId = currentEmployeeId();
        if (empId == null) {
            return;
        }
        Async.run(this, () -> leaveService.getLeaveRequests(empId), list -> {
            requestModel.setRowCount(0);
            for (LeaveRequestDTO dto : safe(list)) {
                addLeaveRow(requestModel, dto);
            }
            status.setText("내 휴가 신청 " + requestModel.getRowCount() + "건");
        }, e -> UiKit.error(this, e));
    }

    private void refreshApprovals() {
        if (!session.isAdmin()) {
            approvalModel.setRowCount(0);
            return;
        }
        Async.run(this, () -> leaveService.getPendingApprovalRequests(session.getEmployeeId()), list -> {
            approvalModel.setRowCount(0);
            for (LeaveRequestDTO dto : safe(list)) {
                addLeaveRow(approvalModel, dto);
            }
        }, e -> UiKit.error(this, e));
    }

    private void cancelSelectedLeave() {
        int viewRow = requestTable.getSelectedRow();
        if (viewRow < 0) {
            UiKit.info(this, "취소할 휴가 신청을 선택하세요.");
            return;
        }
        int modelRow = requestTable.convertRowIndexToModel(viewRow);
        Long requestId = Long.valueOf(String.valueOf(requestModel.getValueAt(modelRow, 0)));
        Async.run(this, () -> leaveService.cancelLeave(requestId), result -> {
            status.setText(result ? "휴가 신청 취소 완료" : "휴가 신청 취소 실패");
            refreshRequests();
        }, e -> UiKit.error(this, e));
    }

    private void processSelectedLeave(CommonStatus targetStatus) {
        int viewRow = approvalTable.getSelectedRow();
        if (viewRow < 0) {
            UiKit.info(this, "처리할 휴가 신청을 선택하세요.");
            return;
        }
        int modelRow = approvalTable.convertRowIndexToModel(viewRow);
        Long requestId = Long.valueOf(String.valueOf(approvalModel.getValueAt(modelRow, 0)));
        Async.run(this, () -> {
            leaveService.processApproval(session.getEmployeeId(), requestId, targetStatus);
            return Boolean.TRUE;
        }, result -> {
            status.setText("휴가 " + targetStatus + " 처리 요청 완료");
            refresh();
        }, e -> UiKit.error(this, e));
    }

    private void addLeaveRow(DefaultTableModel model, LeaveRequestDTO dto) {
        model.addRow(new Object[] {
                dto.getLeaveRequestId(),
                dto.getEmployeeId(),
                dto.getLeaveType(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getStatus(),
                dto.getReason()
        });
    }

    private Long currentEmployeeId() {
        return UiKit.longValue(empIdField);
    }

    private Long requiredEmpId() {
        Long value = currentEmployeeId();
        if (value == null) {
            throw new IllegalArgumentException("사번은 필수입니다.");
        }
        return value;
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? java.util.Collections.<T>emptyList() : list;
    }

    private static class LeaveSnapshot {
        private final AnnualLeaveDTO active;
        private final List<AnnualLeaveDTO> rows;

        private LeaveSnapshot(AnnualLeaveDTO active, List<AnnualLeaveDTO> rows) {
            this.active = active;
            this.rows = rows;
        }
    }
}
