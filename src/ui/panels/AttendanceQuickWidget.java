package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import attendance.dto.NormalAttendanceDTO;
import attendance.dto.RequestWorkTimeDTO;
import attendance.service.AttendanceService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class AttendanceQuickWidget extends JPanel implements Refreshable {
    private static final Color CARD_BG = new Color(20, 45, 75);
    private static final Color CARD_LINE = new Color(45, 69, 99);
    private static final Color CARD_TEXT = new Color(230, 240, 250);
    private static final Color CARD_MUTED = new Color(168, 190, 214);

    private final AppSession session;
    private final Runnable onAttendanceChanged;
    private final AttendanceService attendanceService = new AttendanceService();

    private final JLabel nameLabel = new JLabel();
    private final JLabel roleLabel = new JLabel();
    private final JLabel workTimeLabel = new JLabel();
    private final JLabel stateLabel = new JLabel();
    private final JLabel onTimeLabel = new JLabel();
    private final JLabel offTimeLabel = new JLabel();
    private final JButton actionButton = UiKit.primaryButton("출근하기");

    public AttendanceQuickWidget(AppSession session) {
        this(session, null);
    }

    public AttendanceQuickWidget(AppSession session, Runnable onAttendanceChanged) {
        this.session = session;
        this.onAttendanceChanged = onAttendanceChanged;
        setLayout(new BorderLayout(0, 12));
        setOpaque(true);
        setBackground(CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_LINE),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel user = new JPanel(new GridLayout(0, 1, 0, 4));
        user.setOpaque(false);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(nameLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        roleLabel.setForeground(CARD_MUTED);
        workTimeLabel.setForeground(CARD_MUTED);
        user.add(nameLabel);
        user.add(roleLabel);
        user.add(workTimeLabel);

        JPanel attendance = new JPanel(new GridLayout(0, 1, 0, 5));
        attendance.setOpaque(false);
        stateLabel.setForeground(CARD_TEXT);
        stateLabel.setFont(stateLabel.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        onTimeLabel.setForeground(CARD_MUTED);
        offTimeLabel.setForeground(CARD_MUTED);
        attendance.add(stateLabel);
        attendance.add(onTimeLabel);
        attendance.add(offTimeLabel);

        actionButton.addActionListener(e -> registerToday());

        add(user, BorderLayout.NORTH);
        add(attendance, BorderLayout.CENTER);
        add(actionButton, BorderLayout.SOUTH);

        renderUser();
        renderWorkTime(null);
        renderAttendance(null);
    }

    @Override
    public void refresh() {
        Long employeeId = session.getEmployeeId();
        if (employeeId == null) {
            renderAttendance(null);
            renderWorkTime(null);
            actionButton.setEnabled(false);
            return;
        }

        Async.run(this, () -> todayAttendanceFromExistingRows(employeeId), this::renderAttendance);
        Async.run(this, () -> attendanceService.getAppliedWorkTime(employeeId), this::renderWorkTime);
    }

    private NormalAttendanceDTO todayAttendanceFromExistingRows(Long employeeId) {
        LocalDate today = LocalDate.now();
        for (NormalAttendanceDTO row : UiKit.safeList(attendanceService.findAllAttenDances(employeeId))) {
            if (today.equals(row.getWorkDate())) {
                return row;
            }
        }
        return null;
    }

    private void renderUser() {
        nameLabel.setText(session.getEmployeeName());
        roleLabel.setText((session.isAdmin() ? "관리자" : "일반 사용자") + " · " + session.getEmployeeId());
    }

    private void renderWorkTime(RequestWorkTimeDTO workTime) {
        if (workTime == null || workTime.getOnWorkTime() == null || workTime.getOffWorkTime() == null) {
            workTimeLabel.setText("근무시간 -");
            return;
        }
        workTimeLabel.setText("근무시간 "
                + UiKit.formatTime(workTime.getOnWorkTime())
                + " - "
                + UiKit.formatTime(workTime.getOffWorkTime()));
    }

    private void renderAttendance(NormalAttendanceDTO attendance) {
        if (attendance == null || attendance.getOnWorkTime() == null) {
            stateLabel.setText("출근 전");
            onTimeLabel.setText("출근 -");
            offTimeLabel.setText("퇴근 -");
            actionButton.setText("출근하기");
            actionButton.setEnabled(true);
            return;
        }

        onTimeLabel.setText("출근 " + UiKit.formatTime(attendance.getOnWorkTime()));
        if (attendance.getOffWorkTime() == null) {
            stateLabel.setText("근무 중");
            offTimeLabel.setText("퇴근 -");
            actionButton.setText("퇴근하기");
            actionButton.setEnabled(true);
            return;
        }

        stateLabel.setText("퇴근 완료");
        offTimeLabel.setText("퇴근 " + UiKit.formatTime(attendance.getOffWorkTime()));
        actionButton.setText("퇴근 완료");
        actionButton.setEnabled(false);
    }

    private void registerToday() {
        Long employeeId = session.getEmployeeId();
        if (employeeId == null) {
            return;
        }

        Async.run(this, () -> attendanceService.registerToday(employeeId), ignored -> {
            refresh();
            if (onAttendanceChanged != null) {
                onAttendanceChanged.run();
            }
        });
    }
}
