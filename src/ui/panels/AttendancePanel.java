package ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import attendance.dto.AttendanceModifyHistoryDTO;
import attendance.dto.MissingAttendanceDTO;
import attendance.dto.NormalAttendanceDTO;
import attendance.dto.OvertimeDTO;
import attendance.dto.RequestWorkTimeDTO;
import attendance.service.AttendanceModifyService;
import attendance.service.AttendanceService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class AttendancePanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final AttendanceService attendanceService;
    private final AttendanceModifyService modifyService;

    private final JTextField empIdField = UiKit.field(10);
    private final JTextField startDateField = UiKit.field(10);
    private final JTextField endDateField = UiKit.field(10);
    private final DefaultTableModel attendanceModel = UiKit.model("사번", "근무일", "출근", "퇴근", "마감", "구분", "사유");
    private final JTable attendanceTable = UiKit.table(attendanceModel);

    private final JTextField workMonthField = UiKit.field(10);
    private final JTextField onWorkField = UiKit.field(8);
    private final JTextField offWorkField = UiKit.field(8);
    private final JTextField overtimeMonthField = UiKit.field(10);
    private final JLabel overtimeResult = new JLabel("-");

    private final JTextField modifyDateField = UiKit.field(10);
    private final JTextField modifyOnField = UiKit.field(8);
    private final JTextField modifyOffField = UiKit.field(8);
    private final DefaultTableModel modifyModel = UiKit.model("요청ID", "취소대상", "사번", "근무일", "상태", "출근 전", "출근 후", "퇴근 전", "퇴근 후");
    private final JTable modifyTable = UiKit.table(modifyModel);
    private final JLabel status = UiKit.statusLabel();

    public AttendancePanel(
            AppSession session,
            AttendanceService attendanceService,
            AttendanceModifyService modifyService
    ) {
        this.session = session;
        this.attendanceService = attendanceService;
        this.modifyService = modifyService;
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        JPanel root = UiKit.screen("근태", "출퇴근, 근무시간, 정정 신청을 처리합니다.");
        add(root, BorderLayout.CENTER);

        empIdField.setText(UiKit.display(session.getEmployeeId()));
        empIdField.setEditable(session.isAdmin());
        empIdField.setEnabled(session.isAdmin());
        startDateField.setText(LocalDate.now().withDayOfMonth(1).toString());
        endDateField.setText(LocalDate.now().toString());
        workMonthField.setText(YearMonth.now().plusMonths(1).toString());
        onWorkField.setText("09:00");
        offWorkField.setText("18:00");
        overtimeMonthField.setText(YearMonth.now().toString());
        modifyDateField.setText(LocalDate.now().toString());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("근태 조회", attendanceListTab());
        tabs.addTab("출퇴근/근무시간", workActionTab());
        tabs.addTab("정정 신청", modifyTab());
        root.add(tabs, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel attendanceListTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel filter = UiKit.surface();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(new JLabel("사번"));
        row.add(empIdField);
        row.add(new JLabel("시작"));
        row.add(startDateField);
        row.add(new JLabel("종료"));
        row.add(endDateField);
        JButton all = UiKit.button("전체");
        JButton normal = UiKit.button("정상");
        JButton missing = UiKit.button("누락/휴가");
        all.addActionListener(e -> loadAllAttendance());
        normal.addActionListener(e -> loadNormalAttendance());
        missing.addActionListener(e -> loadMissingAttendance());
        row.add(all);
        row.add(normal);
        row.add(missing);
        filter.add(row, BorderLayout.CENTER);

        JPanel tablePanel = UiKit.surface();
        tablePanel.add(UiKit.scroll(attendanceTable), BorderLayout.CENTER);

        tab.add(filter, BorderLayout.NORTH);
        tab.add(tablePanel, BorderLayout.CENTER);
        return tab;
    }

    private JPanel workActionTab() {
        JPanel tab = new JPanel(new GridLayout(1, 2, 12, 0));
        tab.setOpaque(false);

        JPanel attendance = UiKit.surface();
        attendance.add(new JLabel("오늘 출퇴근"), BorderLayout.NORTH);
        JPanel attendanceForm = UiKit.formPanel();
        UiKit.addField(attendanceForm, 0, "사용 사번", new JLabel(UiKit.display(session.getEmployeeId())));
        attendance.add(attendanceForm, BorderLayout.CENTER);
        JButton registerToday = UiKit.primaryButton("출퇴근 기록");
        registerToday.addActionListener(e -> registerToday());
        attendance.add(registerToday, BorderLayout.SOUTH);

        JPanel workTime = UiKit.surface();
        workTime.add(new JLabel("근무시간/연장근무"), BorderLayout.NORTH);
        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "적용월", workMonthField);
        UiKit.addField(form, row++, "출근시간", onWorkField);
        UiKit.addField(form, row++, "퇴근시간", offWorkField);
        UiKit.addField(form, row++, "연장근무월", overtimeMonthField);
        UiKit.addField(form, row++, "연장결과", overtimeResult);
        workTime.add(form, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton registerWorkTime = UiKit.button("근무시간 등록");
        JButton getOvertime = UiKit.primaryButton("연장근무 조회");
        registerWorkTime.addActionListener(e -> registerWorkTime());
        getOvertime.addActionListener(e -> loadOvertime());
        actions.add(registerWorkTime);
        actions.add(getOvertime);
        workTime.add(actions, BorderLayout.SOUTH);

        tab.add(attendance);
        tab.add(workTime);
        return tab;
    }

    private JPanel modifyTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel request = UiKit.surface();
        request.add(new JLabel("정정 신청"), BorderLayout.NORTH);
        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "근무일", modifyDateField);
        UiKit.addField(form, row++, "새 출근시간", modifyOnField);
        UiKit.addField(form, row++, "새 퇴근시간", modifyOffField);
        request.add(form, BorderLayout.CENTER);
        JButton submit = UiKit.primaryButton("정정 신청");
        submit.addActionListener(e -> submitModifyRequest());
        request.add(submit, BorderLayout.SOUTH);

        JPanel list = UiKit.surface();
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton refresh = UiKit.button("대기 목록");
        JButton approve = UiKit.button("승인");
        JButton reject = UiKit.button("반려");
        approve.setEnabled(session.isAdmin());
        reject.setEnabled(session.isAdmin());
        refresh.addActionListener(e -> loadPendingModifyRequests());
        approve.addActionListener(e -> processSelectedModify(true));
        reject.addActionListener(e -> processSelectedModify(false));
        toolbar.add(refresh);
        if (session.isAdmin()) {
            toolbar.add(approve);
            toolbar.add(reject);
        }
        list.add(toolbar, BorderLayout.NORTH);
        list.add(UiKit.scroll(modifyTable), BorderLayout.CENTER);

        JPanel split = new JPanel(new GridLayout(1, 2, 12, 0));
        split.setOpaque(false);
        split.add(request);
        split.add(list);
        tab.add(UiKit.todo("TODO: 정정 승인 후 기존 PENDING row가 최신상태 조회에서 제외되도록 백엔드 #1 수정 필요"), BorderLayout.NORTH);
        tab.add(split, BorderLayout.CENTER);
        return tab;
    }

    @Override
    public void refresh() {
        loadAllAttendance();
        loadPendingModifyRequests();
    }

    private void loadAllAttendance() {
        try {
            status.setText("근태 전체 조회 중...");
            Long empId = requiredEmpId();
            Async.run(this, () -> attendanceService.findAllAttenDances(empId), list -> {
                fillNormalRows(list, "전체", null, null);
                status.setText("근태 전체 " + attendanceModel.getRowCount() + "건");
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void loadNormalAttendance() {
        try {
            LocalDate start = UiKit.dateValue(startDateField);
            LocalDate end = UiKit.dateValue(endDateField);
            if (session.isAdmin() && start != null && end != null) {
                Async.run(this, () -> attendanceService.getNormalAttendances(start, end), list -> fillNormalRows(list, "정상", null, null), e -> UiKit.error(this, e));
            } else {
                Long empId = requiredEmpId();
                Async.run(this, () -> attendanceService.getNormalAttendances(empId), list -> fillNormalRows(filterNormalRows(list, start, end), "정상", null, null), e -> UiKit.error(this, e));
            }
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void loadMissingAttendance() {
        try {
            LocalDate start = UiKit.dateValue(startDateField);
            LocalDate end = UiKit.dateValue(endDateField);
            if (session.isAdmin() && start != null && end != null) {
                Async.run(this, () -> attendanceService.getMissingAttenDances(start, end), this::fillMissingRows, e -> UiKit.error(this, e));
            } else {
                Long empId = requiredEmpId();
                Async.run(this, () -> attendanceService.getMissingAttenDances(empId), list -> fillMissingRows(filterMissingRows(list, start, end)), e -> UiKit.error(this, e));
            }
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void registerToday() {
        try {
            Long empId = requiredEmpId();
            Async.run(this, () -> attendanceService.registerToday(empId), result -> {
                status.setText(result > 0 ? "출퇴근 기록 완료" : "출퇴근 기록 실패");
                loadAllAttendance();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void registerWorkTime() {
        try {
            RequestWorkTimeDTO dto = new RequestWorkTimeDTO(
                    requiredEmpId(),
                    UiKit.yearMonthValue(workMonthField),
                    UiKit.timeValue(onWorkField),
                    UiKit.timeValue(offWorkField)
            );
            Async.run(this, () -> attendanceService.registerWorkTime(dto), result -> {
                status.setText(result > 0 ? "근무시간 등록 완료" : "근무시간 등록 실패");
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void loadOvertime() {
        try {
            Long empId = requiredEmpId();
            YearMonth month = UiKit.yearMonthValue(overtimeMonthField);
            Async.run(this, () -> attendanceService.getOvertimeAmount(empId, month), overtime -> {
                if (overtime == null) {
                    overtimeResult.setText("-");
                } else {
                    overtimeResult.setText(overtime.getOvertimeHours() + "시간 · " + overtime.getOvertimeDays() + "일");
                }
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void submitModifyRequest() {
        try {
            AttendanceModifyHistoryDTO dto = new AttendanceModifyHistoryDTO();
            dto.setEmpId(requiredEmpId());
            dto.setReqDate(UiKit.dateValue(modifyDateField));
            dto.setOnWorkTimeNew(UiKit.timeValue(modifyOnField));
            dto.setOffWorkTimeNew(UiKit.timeValue(modifyOffField));
            Async.run(this, () -> modifyService.requestAttendanceModify(dto), result -> {
                status.setText(result > 0 ? "정정 신청 완료" : "정정 신청 실패: " + result);
                loadPendingModifyRequests();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void loadPendingModifyRequests() {
        Async.run(this,
                () -> session.isAdmin()
                        ? modifyService.getPendingAttendanceModifyReqs()
                        : modifyService.getAttendanceModifyReqs(requiredEmpId()),
                list -> {
            modifyModel.setRowCount(0);
            for (AttendanceModifyHistoryDTO dto : safe(list)) {
                modifyModel.addRow(new Object[] {
                        dto.getModHistoryId(),
                        dto.getCancelReqId(),
                        dto.getEmpId(),
                        dto.getReqDate(),
                        dto.getReqState(),
                        dto.getOnWorkTimeOld(),
                        dto.getOnWorkTimeNew(),
                        dto.getOffWorkTimeOld(),
                        dto.getOffWorkTimeNew()
                });
            }
        }, e -> UiKit.error(this, e));
    }

    private void processSelectedModify(boolean approve) {
        if (!session.isAdmin()) {
            UiKit.info(this, "관리자만 처리할 수 있습니다.");
            return;
        }
        int viewRow = modifyTable.getSelectedRow();
        if (viewRow < 0) {
            UiKit.info(this, "처리할 정정 요청을 선택하세요.");
            return;
        }
        int modelRow = modifyTable.convertRowIndexToModel(viewRow);
        Long requestId = Long.valueOf(String.valueOf(modifyModel.getValueAt(modelRow, 0)));
        Async.run(this,
                () -> approve
                        ? modifyService.approveAttendanceModifyReq(requestId)
                        : modifyService.rejectAttendanceModifyReq(requestId),
                result -> {
                    status.setText((approve ? "승인" : "반려") + " 처리 결과: " + result);
                    loadPendingModifyRequests();
                },
                e -> UiKit.error(this, e));
    }

    private void fillNormalRows(List<NormalAttendanceDTO> list, String type, String missingType, String missingReason) {
        attendanceModel.setRowCount(0);
        for (NormalAttendanceDTO dto : safe(list)) {
            attendanceModel.addRow(new Object[] {
                    dto.getEmpId(),
                    dto.getWorkDate(),
                    dto.getOnWorkTime(),
                    dto.getOffWorkTime(),
                    dto.getClosed(),
                    type,
                    missingReason
            });
        }
    }

    private void fillMissingRows(List<MissingAttendanceDTO> list) {
        attendanceModel.setRowCount(0);
        for (MissingAttendanceDTO dto : safe(list)) {
            attendanceModel.addRow(new Object[] {
                    dto.getEmpId(),
                    dto.getWorkDate(),
                    dto.getOnWorkTime(),
                    dto.getOffWorkTime(),
                    dto.getClosed(),
                    dto.getMissingType(),
                    dto.getMissingReason()
            });
        }
        status.setText("누락/휴가 " + attendanceModel.getRowCount() + "건");
    }

    private List<NormalAttendanceDTO> filterNormalRows(List<NormalAttendanceDTO> list, LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return list;
        }
        List<NormalAttendanceDTO> filtered = new java.util.ArrayList<NormalAttendanceDTO>();
        for (NormalAttendanceDTO dto : safe(list)) {
            if (isWithin(dto.getWorkDate(), start, end)) {
                filtered.add(dto);
            }
        }
        return filtered;
    }

    private List<MissingAttendanceDTO> filterMissingRows(List<MissingAttendanceDTO> list, LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return list;
        }
        List<MissingAttendanceDTO> filtered = new java.util.ArrayList<MissingAttendanceDTO>();
        for (MissingAttendanceDTO dto : safe(list)) {
            if (isWithin(dto.getWorkDate(), start, end)) {
                filtered.add(dto);
            }
        }
        return filtered;
    }

    private boolean isWithin(LocalDate value, LocalDate start, LocalDate end) {
        if (value == null) {
            return false;
        }
        if (start != null && value.isBefore(start)) {
            return false;
        }
        return end == null || !value.isAfter(end);
    }

    private Long requiredEmpId() {
        if (!session.isAdmin()) {
            return session.getEmployeeId();
        }
        Long value = UiKit.longValue(empIdField);
        if (value == null) {
            throw new IllegalArgumentException("사번은 필수입니다.");
        }
        return value;
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? java.util.Collections.<T>emptyList() : list;
    }
}
