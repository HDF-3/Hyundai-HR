package ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import global.types.EmploymentStatus;
import humanresource.dto.AssignmentHistoryDTO;
import humanresource.dto.EmployeeDTO;
import humanresource.dto.EmployeeInfoDTO;
import humanresource.dto.PerformanceEvaluationDTO;
import humanresource.service.EmployeeService;
import humanresource.service.PerformanceEvaluationService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class ProfilePanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final EmployeeService employeeService = new EmployeeService();
    private final PerformanceEvaluationService evaluationService = new PerformanceEvaluationService();

    private final JTable historyTable = UiKit.table("이력ID", "이름", "부서", "직급", "호봉", "사유", "시작일", "종료일");
    private final JTable evaluationTable = UiKit.table("평가ID", "대상자", "연도", "분기", "등급");
    private final JLabel statusLabel = UiKit.statusLabel();
    private final JPanel infoMetrics = new JPanel(new GridLayout(1, 4, 12, 12));

    private final JTextField empIdField = UiKit.field(10);
    private final JTextField nameField = UiKit.field(14);
    private final JTextField deptNameField = UiKit.field(14);
    private final JTextField deptIdField = UiKit.field(8);
    private final JTextField positionNameField = UiKit.field(14);
    private final JTextField positionIdField = UiKit.field(8);
    private final JTextField payGradeField = UiKit.field(6);
    private final JComboBox<EmploymentStatus> statusCombo = UiKit.combo(EmploymentStatus.values());
    private final UiKit.DateField hireDateField = UiKit.dateField(LocalDate.now(), true);
    private final UiKit.DateField resignDateField = UiKit.dateField(null, true);
    private final JTextField contactField = UiKit.field(14);
    private final JTextField genderField = UiKit.field(6);
    private final JTextField emailField = UiKit.field(18);
    private final JTextField addressField = UiKit.field(24);
    private final JTextField salaryAccountField = UiKit.field(18);
    private final JCheckBox adminCheck = UiKit.checkBox("관리자");

    public ProfilePanel(AppSession session) {
        this.session = session;
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("내 정보", "본인 인사 정보, 인사발령, 인사평가를 확인합니다.");
        infoMetrics.setOpaque(false);
        configureReadOnlyFields();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("내 정보", profileTab());
        tabs.addTab("인사발령", historyTab());
        tabs.addTab("인사평가", evaluationTab());

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);
        content.add(refreshToolbar(), BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);

        page.add(content, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);
    }

    private JPanel profileTab() {
        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "사번", empIdField);
        UiKit.addField(form, 1, "이름", nameField);
        UiKit.addField(form, 2, "부서", deptNameField);
        UiKit.addField(form, 3, "부서ID", deptIdField);
        UiKit.addField(form, 4, "직급", positionNameField);
        UiKit.addField(form, 5, "직급ID", positionIdField);
        UiKit.addField(form, 6, "호봉", payGradeField);
        UiKit.addField(form, 7, "상태", statusCombo);
        UiKit.addField(form, 8, "입사일", hireDateField);
        UiKit.addField(form, 9, "퇴직예정일", resignDateField);
        UiKit.addField(form, 10, "연락처", contactField);
        UiKit.addField(form, 11, "성별", genderField);
        UiKit.addField(form, 12, "이메일", emailField);
        UiKit.addField(form, 13, "주소", addressField);
        UiKit.addField(form, 14, "급여계좌", salaryAccountField);
        UiKit.addField(form, 15, "권한", adminCheck);

        JPanel detail = UiKit.section("내 정보", form);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(infoMetrics, BorderLayout.NORTH);
        body.add(UiKit.scroll(detail), BorderLayout.CENTER);
        return body;
    }

    private JPanel historyTab() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(UiKit.scroll(historyTable), BorderLayout.CENTER);
        return body;
    }

    private JPanel evaluationTab() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(UiKit.scroll(evaluationTable), BorderLayout.CENTER);
        return body;
    }

    private JPanel refreshToolbar() {
        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);
        toolbar.add(refreshButton());
        return toolbar;
    }

    private JButton refreshButton() {
        JButton button = UiKit.secondaryButton("새로고침");
        button.addActionListener(e -> refresh());
        return button;
    }

    @Override
    public void refresh() {
        statusLabel.setText("내 정보를 불러오는 중...");
        Async.run(this, () -> {
            Long empId = session.getEmployeeId();
            return new ProfileData(
                    employeeService.getEmployeeInfo(empId),
                    employeeService.getEmployeeDetail(empId),
                    employeeService.getAssignmentHistory(empId),
                    evaluationService.getPerformanceEvaluationHistory(empId)
            );
        }, data -> {
            fillForm(data.employee, data.info);
            renderInfo(data.info);
            renderHistory(data.history);
            renderEvaluations(data.evaluations);
            statusLabel.setText(" ");
        });
    }

    private void fillForm(EmployeeDTO employee, EmployeeInfoDTO info) {
        if (employee == null) {
            return;
        }
        empIdField.setText(UiKit.value(employee.getEmpId()));
        nameField.setText(UiKit.value(employee.getEname()));
        deptNameField.setText(nameWithId(info == null ? null : info.getDeptName(), employee.getDeptId()));
        deptIdField.setText(UiKit.value(employee.getDeptId()));
        positionNameField.setText(nameWithId(info == null ? null : info.getPositionName(), employee.getPositionId()));
        positionIdField.setText(UiKit.value(employee.getPositionId()));
        payGradeField.setText(UiKit.value(employee.getPayGrade()));
        statusCombo.setSelectedItem(employee.getStatusId() == null ? EmploymentStatus.ACTIVE : employee.getStatusId());
        hireDateField.setDate(employee.getHireDate());
        resignDateField.setDate(employee.getResignDate());
        contactField.setText(UiKit.value(employee.getContact()));
        genderField.setText(UiKit.value(employee.getGender()));
        emailField.setText(UiKit.value(employee.getEmail()));
        addressField.setText(UiKit.value(employee.getAddress()));
        salaryAccountField.setText(UiKit.value(employee.getSalAccount()));
        adminCheck.setSelected(Boolean.TRUE.equals(employee.getIsAdmin()));
    }

    private void renderInfo(EmployeeInfoDTO info) {
        infoMetrics.removeAll();
        infoMetrics.add(UiKit.metric("사번", UiKit.value(session.getEmployeeId())));
        infoMetrics.add(UiKit.metric("부서", info == null ? "-" : UiKit.value(info.getDeptName())));
        infoMetrics.add(UiKit.metric("직급", info == null ? "-" : UiKit.value(info.getPositionName())));
        infoMetrics.add(UiKit.metric("호봉", info == null ? "-" : UiKit.value(info.getPayGrade())));
        infoMetrics.revalidate();
        infoMetrics.repaint();
    }

    private void renderHistory(List<AssignmentHistoryDTO> rows) {
        UiKit.setRows(historyTable, UiKit.safeList(rows).stream()
                .map(this::historyRow)
                .collect(java.util.stream.Collectors.toList()));
    }

    private Object[] historyRow(AssignmentHistoryDTO row) {
        return new Object[] {
                row.getHistoryId(),
                row.getEName(),
                row.getDeptName(),
                row.getPositionName(),
                row.getPayGrade(),
                row.getReasonName(),
                row.getStartDate(),
                row.getEndDate()
        };
    }

    private void renderEvaluations(List<PerformanceEvaluationDTO> rows) {
        UiKit.setRows(evaluationTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getEvaluationId(),
                        nameWithId(session.getEmployeeName(), row.getTargetEmpId()),
                        row.getEvaluationYear(),
                        row.getEvaluationQuarter(),
                        row.getPerformanceGrade()
                })
                .collect(java.util.stream.Collectors.toList()));
    }

    private void loadHistory() {
        Async.run(this, () -> employeeService.getAssignmentHistory(session.getEmployeeId()), rows -> {
            renderHistory(rows);
            statusLabel.setText("내 인사발령: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void loadEvaluations() {
        Async.run(this, () -> evaluationService.getPerformanceEvaluationHistory(session.getEmployeeId()), rows -> {
            renderEvaluations(rows);
            statusLabel.setText("내 인사평가: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void configureReadOnlyFields() {
        empIdField.setEditable(false);
        nameField.setEditable(false);
        deptNameField.setEditable(false);
        deptIdField.setEditable(false);
        positionNameField.setEditable(false);
        positionIdField.setEditable(false);
        payGradeField.setEditable(false);
        contactField.setEditable(false);
        genderField.setEditable(false);
        emailField.setEditable(false);
        addressField.setEditable(false);
        salaryAccountField.setEditable(false);
        statusCombo.setEnabled(false);
        setEnabledDeep(hireDateField, false);
        setEnabledDeep(resignDateField, false);
        adminCheck.setEnabled(false);
    }

    private void setEnabledDeep(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                setEnabledDeep(child, enabled);
            }
        }
    }

    private String nameWithId(String name, Object id) {
        String idText = UiKit.value(id);
        String nameText = UiKit.value(name);
        if (nameText.isEmpty()) {
            return idText.isEmpty() ? "" : "ID " + idText;
        }
        return idText.isEmpty() ? nameText : nameText + " (" + idText + ")";
    }

    private static class ProfileData {
        private final EmployeeDTO employee;
        private final EmployeeInfoDTO info;
        private final List<AssignmentHistoryDTO> history;
        private final List<PerformanceEvaluationDTO> evaluations;

        private ProfileData(
                EmployeeDTO employee,
                EmployeeInfoDTO info,
                List<AssignmentHistoryDTO> history,
                List<PerformanceEvaluationDTO> evaluations
        ) {
            this.employee = employee;
            this.info = info;
            this.history = history;
            this.evaluations = evaluations;
        }
    }
}
