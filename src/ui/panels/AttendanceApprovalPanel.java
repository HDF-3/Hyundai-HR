package ui.panels;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import attendance.dto.AttendanceModifyHistoryDTO;
import attendance.service.AttendanceModifyService;
import global.types.CommonStatus;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class AttendanceApprovalPanel extends JPanel implements Refreshable {
    private final AttendanceModifyService modifyService = new AttendanceModifyService();
    private final JTable requestTable = UiKit.table("요청ID", "취소대상ID", "사번", "근무일", "상태", "기존 출근", "신규 출근", "기존 퇴근", "신규 퇴근");
    private final JLabel statusLabel = UiKit.statusLabel();
    private final JTextField empIdField = UiKit.field(8);
    private final UiKit.DateField startDateField = UiKit.dateField(LocalDate.now().withDayOfMonth(1), false);
    private final UiKit.DateField endDateField = UiKit.dateField(LocalDate.now(), false);
    private final JComboBox<CommonStatus> statusCombo = UiKit.combo(CommonStatus.values());

    public AttendanceApprovalPanel() {
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("근태 결재", "근태 정정 및 정정 취소 신청을 조회하고 승인/반려합니다.");

        JPanel filter = UiKit.form();
        UiKit.addField(filter, 0, "사번", empIdField);
        UiKit.addField(filter, 1, "시작일", startDateField);
        UiKit.addField(filter, 2, "종료일", endDateField);
        UiKit.addField(filter, 3, "상태", statusCombo);

        JButton pending = UiKit.primaryButton("대기 조회");
        pending.addActionListener(e -> loadByStatus(CommonStatus.PENDING));
        JButton byStatus = UiKit.secondaryButton("상태 조회");
        byStatus.addActionListener(e -> loadByStatus((CommonStatus) statusCombo.getSelectedItem()));
        JButton byEmp = UiKit.secondaryButton("사번 조회");
        byEmp.addActionListener(e -> loadByEmployee());
        JButton byRange = UiKit.secondaryButton("기간 조회");
        byRange.addActionListener(e -> loadByRange());
        JButton byEmpRange = UiKit.secondaryButton("사번+기간 조회");
        byEmpRange.addActionListener(e -> loadByEmployeeRange());
        JButton approve = UiKit.primaryButton("선택 승인");
        approve.addActionListener(e -> approveSelected());
        JButton reject = UiKit.dangerButton("선택 반려");
        reject.addActionListener(e -> rejectSelected());

        JPanel top = UiKit.section("조회 조건", filter);
        top.add(UiKit.actions(pending, byStatus, byEmp, byRange, byEmpRange, approve, reject), BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(UiKit.scroll(requestTable), BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        loadByStatus(CommonStatus.PENDING);
    }

    private void loadByStatus(CommonStatus status) {
        Async.run(this, () -> {
            switch (status) {
                case APPROVED:
                    return modifyService.getApprovedAttendanceModifyReqs();
                case REJECTED:
                    return modifyService.getRejectedAttendanceModifyReqs();
                case CANCELED:
                    return modifyService.getCanceledAttendanceModifyReqs();
                case PENDING:
                default:
                    return modifyService.getPendingAttendanceModifyReqs();
            }
        }, rows -> render(rows, status + " 조회: " + UiKit.safeSize(rows) + "건"));
    }

    private void loadByEmployee() {
        Async.run(this, () -> modifyService.getAttendanceModifyReqs(requiredEmpId()), rows -> render(rows, "사번 조회: " + UiKit.safeSize(rows) + "건"));
    }

    private void loadByRange() {
        Async.run(this, () -> modifyService.getAttendanceModifyReqs(
                UiKit.requireDate(startDateField, "시작일"),
                UiKit.requireDate(endDateField, "종료일")
        ), rows -> render(rows, "기간 조회: " + UiKit.safeSize(rows) + "건"));
    }

    private void loadByEmployeeRange() {
        Async.run(this, () -> modifyService.getAttendanceModifyReqs(
                requiredEmpId(),
                UiKit.requireDate(startDateField, "시작일"),
                UiKit.requireDate(endDateField, "종료일")
        ), rows -> render(rows, "복합 조회: " + UiKit.safeSize(rows) + "건"));
    }

    private void approveSelected() {
        Long requestId = UiKit.selectedLong(requestTable, 0);
        boolean cancelRequest = UiKit.selectedValue(requestTable, 1) != null;
        Async.run(this, () -> cancelRequest
                ? modifyService.approveAttendanceModifyCancelReq(requestId)
                : modifyService.approveAttendanceModifyReq(requestId), result -> {
            statusLabel.setText("승인 처리 완료: " + result + "건");
            refresh();
        });
    }

    private void rejectSelected() {
        Long requestId = UiKit.selectedLong(requestTable, 0);
        boolean cancelRequest = UiKit.selectedValue(requestTable, 1) != null;
        Async.run(this, () -> cancelRequest
                ? modifyService.rejectAttendanceModifyCancelReq(requestId)
                : modifyService.rejectAttendanceModifyReq(requestId), result -> {
            statusLabel.setText("반려 처리 완료: " + result + "건");
            refresh();
        });
    }

    private void render(List<AttendanceModifyHistoryDTO> rows, String status) {
        UiKit.setRows(requestTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getModHistoryId(),
                        row.getCancelReqId(),
                        row.getEmpId(),
                        row.getReqDate(),
                        row.getReqState(),
                        UiKit.formatTime(row.getOnWorkTimeOld()),
                        UiKit.formatTime(row.getOnWorkTimeNew()),
                        UiKit.formatTime(row.getOffWorkTimeOld()),
                        UiKit.formatTime(row.getOffWorkTimeNew())
                })
                .collect(Collectors.toList()));
        statusLabel.setText(status);
    }

    private Long requiredEmpId() {
        Long empId = UiKit.parseLong(empIdField.getText());
        if (empId == null) {
            throw new IllegalArgumentException("사번을 입력하세요.");
        }
        return empId;
    }
}
