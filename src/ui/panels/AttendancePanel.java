package ui.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import attendance.dto.AttendanceModifyHistoryDTO;
import attendance.dto.MissingAttendanceDTO;
import attendance.dto.NormalAttendanceDTO;
import attendance.dto.RequestWorkTimeDTO;
import attendance.service.AttendanceModifyService;
import attendance.service.AttendanceService;
import global.types.LeaveType;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class AttendancePanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final AttendanceService attendanceService = new AttendanceService();
    private final AttendanceModifyService modifyService = new AttendanceModifyService();

    private final JTable attendanceTable = UiKit.table("구분", "사번", "근무일", "출근", "퇴근", "마감", "미타각 유형", "사유");
    private final JTable modifyTable = UiKit.table("요청ID", "취소대상ID", "사번", "근무일", "상태", "기존 출근", "신규 출근", "기존 퇴근", "신규 퇴근");
    private final JLabel statusLabel = UiKit.statusLabel();

    private final JTextField searchEmpIdField = UiKit.field(10);
    private final UiKit.DateField startDateField = UiKit.dateField(LocalDate.now().withDayOfMonth(1), false);
    private final UiKit.DateField endDateField = UiKit.dateField(LocalDate.now(), false);
    private final JTextField appliedMonthField = UiKit.field(8);
    private final JTextField onTimeField = UiKit.field(6);
    private final JTextField offTimeField = UiKit.field(6);
    private final UiKit.DateField modifyDateField = UiKit.dateField(LocalDate.now(), false);
    private final JTextField modifyOnField = UiKit.field(6);
    private final JTextField modifyOffField = UiKit.field(6);
    private final UiKit.DateField leaveStartField = UiKit.dateField(LocalDate.now().plusDays(1), false);
    private final UiKit.DateField leaveEndField = UiKit.dateField(LocalDate.now().plusDays(1), false);
    private final JComboBox<LeaveType> leaveTypeCombo = UiKit.combo(LeaveType.values());

    public AttendancePanel(AppSession session) {
        this.session = session;
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("근태", "출퇴근 기록, 근태 조회, 유연근무, 근태 정정 신청을 처리합니다.");
        searchEmpIdField.setText(UiKit.value(session.getEmployeeId()));
        appliedMonthField.setText(YearMonth.now().plusMonths(1).toString());
        onTimeField.setText("09:00");
        offTimeField.setText("18:00");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("근태 조회", attendanceSearchTab());
        tabs.addTab("유연근무", workTimeTab());
        tabs.addTab("정정 신청", modifyTab());
        if (session.isAdmin()) {
            tabs.addTab("근태 사유 등록", leaveAttendanceTab());
        }

        page.add(tabs, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        loadMyAll();
        loadMyModifyRequests();
    }

    private JPanel attendanceSearchTab() {
        JPanel filter = UiKit.form();
        UiKit.addField(filter, 0, "사번", searchEmpIdField);
        UiKit.addField(filter, 1, "시작일", startDateField);
        UiKit.addField(filter, 2, "종료일", endDateField);

        JButton myAll = UiKit.primaryButton("내 전체");
        myAll.addActionListener(e -> loadMyAll());
        JButton myNormal = UiKit.secondaryButton("내 정상");
        myNormal.addActionListener(e -> loadNormalByEmployee());
        JButton myMissing = UiKit.secondaryButton("내 미타각");
        myMissing.addActionListener(e -> loadMissingByEmployee());
        JButton rangeAll = UiKit.secondaryButton("기간 전체");
        rangeAll.addActionListener(e -> loadAllByRange());
        JButton rangeNormal = UiKit.secondaryButton("기간 정상");
        rangeNormal.addActionListener(e -> loadNormalByRange());
        JButton rangeMissing = UiKit.secondaryButton("기간 미타각");
        rangeMissing.addActionListener(e -> loadMissingByRange());
        JButton clock = UiKit.primaryButton("출근/퇴근 기록");
        clock.addActionListener(e -> registerToday());

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(UiKit.section("조회 조건", filter), BorderLayout.NORTH);
        body.add(UiKit.scroll(attendanceTable), BorderLayout.CENTER);
        body.add(UiKit.actions(myAll, myNormal, myMissing, rangeAll, rangeNormal, rangeMissing, clock), BorderLayout.SOUTH);
        return body;
    }

    private JPanel workTimeTab() {
        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "적용월", appliedMonthField);
        UiKit.addField(form, 1, "출근시간", onTimeField);
        UiKit.addField(form, 2, "퇴근시간", offTimeField);

        JButton request = UiKit.primaryButton("유연근무 신청");
        request.addActionListener(e -> requestWorkTime());

        JPanel body = UiKit.section("다음 달 이후 유연근무 시간을 신청합니다.", form);
        body.add(UiKit.actions(request), BorderLayout.SOUTH);
        return body;
    }

    private JPanel modifyTab() {
        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "근무일", modifyDateField);
        UiKit.addField(form, 1, "신규 출근", modifyOnField);
        UiKit.addField(form, 2, "신규 퇴근", modifyOffField);

        JButton request = UiKit.primaryButton("정정 신청");
        request.addActionListener(e -> requestModify());
        JButton refresh = UiKit.secondaryButton("내 신청 조회");
        refresh.addActionListener(e -> loadMyModifyRequests());
        JButton cancel = UiKit.dangerButton("선택 신청 취소요청");
        cancel.addActionListener(e -> requestModifyCancel());

        JPanel top = UiKit.section("출근 또는 퇴근 시간 중 필요한 항목만 입력할 수 있습니다.", form);
        top.add(UiKit.actions(request, refresh, cancel), BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(UiKit.scroll(modifyTable), BorderLayout.CENTER);
        return body;
    }

    private JPanel leaveAttendanceTab() {
        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "사번", searchEmpIdField);
        UiKit.addField(form, 1, "시작일", leaveStartField);
        UiKit.addField(form, 2, "종료일", leaveEndField);
        UiKit.addField(form, 3, "유형", leaveTypeCombo);

        JButton register = UiKit.primaryButton("근태 사유 등록");
        register.addActionListener(e -> registerLeaveAttendance());
        JPanel body = UiKit.section("승인된 휴가/외근 등의 미타각 사유를 근태에 반영합니다.", form);
        body.add(UiKit.actions(register), BorderLayout.SOUTH);
        return body;
    }

    private void loadMyAll() {
        statusLabel.setText("내 근태를 불러오는 중...");
        Async.run(this, () -> attendanceService.findAllAttenDances(session.getEmployeeId()), rows -> {
            renderNormal("전체", rows);
            statusLabel.setText("조회 완료: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void loadNormalByEmployee() {
        Long empId = readSearchEmpId();
        Async.run(this, () -> attendanceService.getNormalAttendances(empId), rows -> {
            renderNormal("정상", rows);
            statusLabel.setText("정상 근태: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void loadMissingByEmployee() {
        Long empId = readSearchEmpId();
        Async.run(this, () -> attendanceService.getMissingAttenDances(empId), rows -> {
            renderMissing(rows);
            statusLabel.setText("미타각 근태: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void loadAllByRange() {
        Async.run(this, () -> attendanceService.findAllAttenDances(UiKit.requireDate(startDateField, "시작일"), UiKit.requireDate(endDateField, "종료일")), rows -> {
            renderNormal("전체", rows);
            statusLabel.setText("기간 전체: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void loadNormalByRange() {
        Async.run(this, () -> attendanceService.getNormalAttendances(UiKit.requireDate(startDateField, "시작일"), UiKit.requireDate(endDateField, "종료일")), rows -> {
            renderNormal("정상", rows);
            statusLabel.setText("기간 정상: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void loadMissingByRange() {
        Async.run(this, () -> attendanceService.getMissingAttenDances(UiKit.requireDate(startDateField, "시작일"), UiKit.requireDate(endDateField, "종료일")), rows -> {
            renderMissing(rows);
            statusLabel.setText("기간 미타각: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void renderNormal(String type, List<NormalAttendanceDTO> rows) {
        UiKit.setRows(attendanceTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        type,
                        row.getEmpId(),
                        row.getWorkDate(),
                        UiKit.formatTime(row.getOnWorkTime()),
                        UiKit.formatTime(row.getOffWorkTime()),
                        row.getClosed(),
                        "",
                        ""
                })
                .collect(Collectors.toList()));
    }

    private void renderMissing(List<MissingAttendanceDTO> rows) {
        List<Object[]> tableRows = new ArrayList<>();
        for (MissingAttendanceDTO row : UiKit.safeList(rows)) {
            tableRows.add(new Object[] {
                    "미타각",
                    row.getEmpId(),
                    row.getWorkDate(),
                    UiKit.formatTime(row.getOnWorkTime()),
                    UiKit.formatTime(row.getOffWorkTime()),
                    row.getClosed(),
                    row.getMissingType(),
                    row.getMissingReason()
            });
        }
        UiKit.setRows(attendanceTable, tableRows);
    }

    private void registerToday() {
        Async.run(this, () -> attendanceService.registerToday(session.getEmployeeId()), result -> {
            statusLabel.setText("근태 기록 완료: " + result + "건");
            loadMyAll();
        });
    }

    private void requestWorkTime() {
        RequestWorkTimeDTO dto = new RequestWorkTimeDTO(
                session.getEmployeeId(),
                UiKit.parseYearMonth(appliedMonthField.getText()),
                UiKit.parseTime(onTimeField.getText()),
                UiKit.parseTime(offTimeField.getText())
        );
        Async.run(this, () -> attendanceService.registerWorkTime(dto), result -> {
            statusLabel.setText("유연근무 신청 완료: " + result + "건");
        });
    }

    private void requestModify() {
        Async.run(this, () -> modifyService.requestWorkTimeModify(
                session.getEmployeeId(),
                UiKit.requireDate(modifyDateField, "근무일"),
                UiKit.parseTime(modifyOnField.getText()),
                UiKit.parseTime(modifyOffField.getText())
        ), result -> {
            statusLabel.setText("근태 정정 신청 완료: " + result + "건");
            loadMyModifyRequests();
        });
    }

    private void requestModifyCancel() {
        Long requestId = UiKit.selectedLong(modifyTable, 0);
        Async.run(this, () -> modifyService.requestAttendanceModifyCancel(session.getEmployeeId(), requestId), result -> {
            statusLabel.setText("정정 취소 신청 완료: " + result + "건");
            loadMyModifyRequests();
        });
    }

    private void loadMyModifyRequests() {
        Async.run(this, () -> modifyService.getAttendanceModifyReqs(session.getEmployeeId()), rows -> {
            UiKit.setRows(modifyTable, UiKit.safeList(rows).stream()
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
        });
    }

    private void registerLeaveAttendance() {
        Async.run(this, () -> attendanceService.registerLeave(
                readSearchEmpId(),
                UiKit.requireDate(leaveStartField, "시작일"),
                UiKit.requireDate(leaveEndField, "종료일"),
                (LeaveType) leaveTypeCombo.getSelectedItem()
        ), result -> statusLabel.setText("근태 사유 등록 완료: " + result + "건"));
    }

    private Long readSearchEmpId() {
        Long empId = UiKit.parseLong(searchEmpIdField.getText());
        if (empId == null) {
            throw new IllegalArgumentException("사번을 입력하세요.");
        }
        if (!session.isAdmin() && !empId.equals(session.getEmployeeId())) {
            throw new IllegalArgumentException("본인 근태만 조회할 수 있습니다.");
        }
        return empId;
    }
}
