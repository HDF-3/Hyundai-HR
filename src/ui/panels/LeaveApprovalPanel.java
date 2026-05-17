package ui.panels;

import java.awt.BorderLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import global.types.CommonStatus;
import leave.dto.LeaveRequestDTO;
import leave.service.LeaveService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class LeaveApprovalPanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final LeaveService leaveService = new LeaveService();
    private final JTable approvalTable = UiKit.table("신청ID", "사번", "유형", "시작일", "종료일", "상태", "사유");
    private final JLabel statusLabel = UiKit.statusLabel();

    public LeaveApprovalPanel(AppSession session) {
        this.session = session;
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("휴가 결재", "관리자 소속 기준의 대기 휴가를 승인 또는 반려합니다.");

        JButton refresh = UiKit.primaryButton("결재 대기 조회");
        refresh.addActionListener(e -> refresh());
        JButton approve = UiKit.primaryButton("선택 승인");
        approve.addActionListener(e -> process(CommonStatus.APPROVED));
        JButton reject = UiKit.dangerButton("선택 반려");
        reject.addActionListener(e -> process(CommonStatus.REJECTED));

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(UiKit.actions(refresh, approve, reject), BorderLayout.NORTH);
        body.add(UiKit.scroll(approvalTable), BorderLayout.CENTER);

        page.add(body, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        statusLabel.setText("휴가 결재 대기 목록을 불러오는 중...");
        Async.run(this, () -> leaveService.getPendingApprovalRequests(session.getEmployeeId()), rows -> {
            render(rows);
            statusLabel.setText("결재 대기: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void render(List<LeaveRequestDTO> rows) {
        UiKit.setRows(approvalTable, UiKit.safeList(rows).stream()
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

    private void process(CommonStatus status) {
        Long leaveRequestId = UiKit.selectedLong(approvalTable, 0);
        Async.runVoid(this, () -> leaveService.processApproval(session.getEmployeeId(), leaveRequestId, status), () -> {
            statusLabel.setText("휴가 결재 처리를 요청했습니다: " + status);
            refresh();
        });
    }
}
