package ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import global.types.EmploymentStatus;
import global.types.PerformanceGrade;
import global.utils.PasswordUtils;
import humanresource.dto.AssignmentHistoryDTO;
import humanresource.dto.EmployeeDTO;
import humanresource.dto.EmployeeInfoDTO;
import humanresource.dto.PerformanceEvaluationDTO;
import humanresource.service.EmployeeService;
import humanresource.service.PerformanceEvaluationService;
import ui.AppSession;
import ui.Async;
import ui.ErrorReporter;
import ui.Refreshable;
import ui.UiKit;

public class EmployeesPanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final EmployeeService employeeService = new EmployeeService();
    private final PerformanceEvaluationService evaluationService = new PerformanceEvaluationService();

    private final JTable employeeTable = UiKit.table("사번", "이름", "부서", "직급", "입사일", "퇴직예정일", "성별", "연락처", "이메일", "주소", "급여계좌");
    private final javax.swing.JLabel statusLabel = UiKit.statusLabel();

    private final JTextField searchNameField = UiKit.field(12);
    private final JTextField searchDeptField = UiKit.field(8);
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
    private final JTextField accountField = UiKit.field(18);
    private final JTextField passwordField = UiKit.field(12);
    private final JCheckBox adminCheck = new JCheckBox("관리자");

    private EmployeeDTO selectedEmployee;

    public EmployeesPanel(AppSession session) {
        this.session = session;
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("인사 관리", "직원 등록, 인사 변경, 발령 이력과 인사평가를 관리합니다.");

        page.add(employeeTab(), BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);

        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && employeeTable.getSelectedRow() >= 0) {
                loadSelectedEmployee();
            }
        });
    }

    @Override
    public void refresh() {
        loadAll();
    }

    private JPanel employeeTab() {
        deptNameField.setEditable(false);
        positionNameField.setEditable(false);

        JPanel search = UiKit.form();
        UiKit.addField(search, 0, "이름", searchNameField);
        UiKit.addField(search, 1, "부서ID", searchDeptField);

        JButton searchButton = UiKit.secondaryButton("검색");
        searchButton.addActionListener(e -> guard(this::search));

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
        UiKit.addField(form, 14, "급여계좌", accountField);
        passwordField.setToolTipText("입력하면 관리자 권한으로 새 비밀번호를 저장합니다. 비워두면 기존 비밀번호를 유지합니다.");
        UiKit.addField(form, 15, "새 비밀번호", passwordField);
        UiKit.addField(form, 16, "권한", adminCheck);
        JButton refresh = refreshButton();
        JButton register = UiKit.primaryButton("신규 직원 등록");
        register.addActionListener(e -> guard(this::registerEmployee));
        JButton update = UiKit.secondaryButton("수정");
        update.addActionListener(e -> guard(this::updateEmployee));
        JButton assignmentHistory = UiKit.secondaryButton("발령 이력 보기");
        assignmentHistory.addActionListener(e -> guard(this::showAssignmentHistoryPopup));
        JButton evaluationHistory = UiKit.secondaryButton("인사평가 이력");
        evaluationHistory.addActionListener(e -> guard(this::showEvaluationHistoryPopup));

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        JPanel searchSection = UiKit.section("검색", search);
        searchSection.add(rightActions(searchButton), BorderLayout.SOUTH);
        left.add(searchSection, BorderLayout.NORTH);
        left.add(employeeListPanel(), BorderLayout.CENTER);

        JPanel right = UiKit.section("직원 상세", form);
        JPanel actionRows = new JPanel(new GridLayout(0, 1, 0, 8));
        actionRows.setOpaque(false);
        actionRows.add(UiKit.actions(update, assignmentHistory, evaluationHistory));
        right.add(actionRows, BorderLayout.SOUTH);
        JScrollPane detailScroll = UiKit.scroll(right);
        detailScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setOpaque(false);
        body.add(left);
        body.add(detailScroll);

        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.add(rightActions(refresh, register), BorderLayout.NORTH);
        wrapper.add(body, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel employeeListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(UiKit.scroll(employeeTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel rightActions(java.awt.Component... buttons) {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        panel.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            if (i > 0) {
                panel.add(javax.swing.Box.createHorizontalStrut(8));
            }
            panel.add(buttons[i]);
        }
        return panel;
    }

    private JButton refreshButton() {
        JButton button = UiKit.secondaryButton("새로고침");
        button.addActionListener(e -> guard(this::loadAll));
        return button;
    }

    private void guard(Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            ErrorReporter.report(this, t);
        }
    }

    private void loadAll() {
        statusLabel.setText("직원 목록을 불러오는 중...");
        Async.run(this, this::loadEmployeeRows, data -> {
            renderEmployees(data.employees, data.infoByEmployeeId);
            statusLabel.setText("직원 목록: " + UiKit.safeSize(data.employees) + "건");
        });
    }

    private void search() {
        String name = UiKit.text(searchNameField);
        Long deptId = UiKit.parseLong(searchDeptField.getText());
        Async.run(this, () -> {
            EmployeeRowsData allRows = loadEmployeeRows();
            List<EmployeeDTO> filtered = UiKit.safeList(allRows.employees).stream()
                    .filter(row -> matchesEmployeeSearch(row, name, deptId))
                    .collect(Collectors.toList());
            return new EmployeeRowsData(filtered, allRows.infoByEmployeeId);
        }, data -> {
            renderEmployees(data.employees, data.infoByEmployeeId);
            statusLabel.setText("검색 결과: " + UiKit.safeSize(data.employees) + "건");
        });
    }

    private EmployeeRowsData loadEmployeeRows() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        Map<Long, EmployeeInfoDTO> infoByEmployeeId = new HashMap<>();
        for (EmployeeInfoDTO info : UiKit.safeList(employeeService.getEmployeeInfoList())) {
            infoByEmployeeId.put(info.getEmpId(), info);
        }
        return new EmployeeRowsData(employees, infoByEmployeeId);
    }

    private boolean matchesEmployeeSearch(EmployeeDTO row, String name, Long deptId) {
        boolean nameMatched = name == null || (row.getEname() != null && row.getEname().contains(name));
        boolean deptMatched = deptId == null || deptId.equals(row.getDeptId());
        return nameMatched && deptMatched;
    }

    private void renderEmployees(List<EmployeeDTO> rows, Map<Long, EmployeeInfoDTO> infoByEmployeeId) {
        UiKit.setRows(employeeTable, UiKit.safeList(rows).stream()
                .map(row -> {
                    EmployeeInfoDTO info = infoByEmployeeId.get(row.getEmpId());
                    return new Object[] {
                        row.getEmpId(),
                        row.getEname(),
                        nameWithId(info == null ? null : info.getDeptName(), row.getDeptId()),
                        nameWithId(info == null ? null : info.getPositionName(), row.getPositionId()),
                        row.getHireDate(),
                        row.getResignDate(),
                        row.getGender(),
                        row.getContact(),
                        row.getEmail(),
                        row.getAddress(),
                        row.getSalAccount()
                    };
                })
                .collect(Collectors.toList()));
    }

    private void loadSelectedEmployee() {
        Long empId = UiKit.selectedLong(employeeTable, 0);
        Async.run(this, () -> new EmployeeDetailData(
                employeeService.getEmployeeInfo(empId),
                employeeService.getEmployeeDetail(empId)
        ), data -> {
            selectedEmployee = data.employee;
            fillEmployee(data.employee, data.info);
        });
    }

    private void fillEmployee(EmployeeDTO employee, EmployeeInfoDTO info) {
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
        accountField.setText(UiKit.value(employee.getSalAccount()));
        passwordField.setText("");
        adminCheck.setSelected(Boolean.TRUE.equals(employee.getIsAdmin()));
    }

    private void registerEmployee() {
        EmployeeDTO dto = readEmployeeForm(false);
        Async.run(this, () -> employeeService.registerEmployee(dto), result -> {
            statusLabel.setText(result ? "직원이 등록되었습니다." : "직원 등록 결과가 없습니다.");
            loadAll();
        });
    }

    private void updateEmployee() {
        EmployeeDTO dto = readEmployeeForm(true);
        String password = UiKit.text(passwordField);
        if (password != null) {
            dto.setPassword(PasswordUtils.encrypt(password));
        }
        Async.run(this, () -> employeeService.updateEmployeeInfo(dto), result -> {
            statusLabel.setText(result ? "직원 정보가 수정되었습니다." : "수정된 정보가 없습니다.");
            loadAll();
        });
    }

    private EmployeeDTO readEmployeeForm(boolean preservePassword) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmpId(requireLong(empIdField, "사번"));
        dto.setEname(requiredText(nameField, "이름"));
        dto.setDeptId(requireLong(deptIdField, "부서ID"));
        dto.setPositionId(requireLong(positionIdField, "직급ID"));
        Integer payGrade = UiKit.parseInteger(payGradeField.getText());
        dto.setPayGrade(payGrade == null ? 1 : payGrade);
        dto.setStatusId((EmploymentStatus) statusCombo.getSelectedItem());
        dto.setHireDate(hireDateField.getDate());
        dto.setResignDate(resignDateField.getDate());
        dto.setContact(UiKit.text(contactField));
        dto.setGender(UiKit.text(genderField));
        dto.setEmail(UiKit.text(emailField));
        dto.setAddress(UiKit.text(addressField));
        dto.setSalAccount(UiKit.text(accountField));
        dto.setIsAdmin(adminCheck.isSelected());

        String password = UiKit.text(passwordField);
        if (password != null) {
            dto.setPassword(password);
        } else if (preservePassword && selectedEmployee != null) {
            dto.setPassword(selectedEmployee.getPassword());
        }
        return dto;
    }

    private void showAssignmentHistoryPopup() {
        Long empId = readSelectedEmpId();
        Async.run(this, () -> employeeService.getAssignmentHistory(empId), rows -> {
            JTable table = UiKit.table("이력ID", "이름", "부서", "직급", "호봉", "사유", "시작일", "종료일");
            UiKit.setRows(table, UiKit.safeList(rows).stream()
                    .map(this::historyRow)
                    .collect(Collectors.toList()));
            showTablePopup(table, "발령 이력", "발령 이력: " + UiKit.safeSize(rows) + "건");
        });
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

    private void showEvaluationHistoryPopup() {
        showEvaluationHistoryPopup(readSelectedEmpId());
    }

    private void showEvaluationHistoryPopup(Long empId) {
        Async.run(this, () -> evaluationService.getPerformanceEvaluationHistory(empId), rows -> {
            JTable table = UiKit.table("평가ID", "대상자", "연도", "분기", "등급");
            renderEvaluationRows(table, rows, empId);
            showEvaluationHistoryDialog(table, empId, "인사평가: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void showEvaluationHistoryDialog(JTable table, Long empId, String status) {
        JButton register = UiKit.primaryButton("인사평가 등록");
        register.addActionListener(e -> showEvaluationRegisterPopup(empId, table));

        JScrollPane scroll = UiKit.scroll(table);
        scroll.setPreferredSize(new Dimension(760, 260));

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBorder(new javax.swing.border.EmptyBorder(14, 14, 14, 14));
        body.setBackground(UiKit.BG);
        body.add(rightActions(register), BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);

        Window owner = SwingUtilities.getWindowAncestor(this);
        javax.swing.JDialog dialog = new javax.swing.JDialog(owner, "인사평가 이력", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(body);
        dialog.setSize(820, 420);
        dialog.setLocationRelativeTo(this);
        statusLabel.setText(status);
        dialog.setVisible(true);
    }

    private void showEvaluationRegisterPopup(Long empId, JTable historyTable) {
        JTextField yearField = UiKit.field(8);
        JTextField quarterField = UiKit.field(4);
        JComboBox<PerformanceGrade> gradeCombo = UiKit.combo(PerformanceGrade.values());
        yearField.setText(String.valueOf(LocalDate.now().getYear()));
        quarterField.setText("1");

        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "대상자", readonlyField(nameWithId(selectedEmployee == null ? null : selectedEmployee.getEname(), empId)));
        UiKit.addField(form, 1, "평가연도", yearField);
        UiKit.addField(form, 2, "분기", quarterField);
        UiKit.addField(form, 3, "등급", gradeCombo);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "인사평가 등록",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            PerformanceEvaluationDTO dto = new PerformanceEvaluationDTO();
            dto.setTargetEmpId(empId);
            dto.setEvaluationYear(requiredText(yearField, "평가연도"));
            dto.setEvaluationQuarter(requireLong(quarterField, "분기"));
            dto.setPerformanceGrade((PerformanceGrade) gradeCombo.getSelectedItem());

            Async.run(this, () -> {
                try {
                    return evaluationService.registerPerformanceEvaluation(dto, session.getEmployee());
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }, registered -> {
                statusLabel.setText(registered ? "인사평가가 등록되었습니다." : "인사평가 등록 결과가 없습니다.");
                reloadEvaluationHistory(empId, historyTable);
            });
        } catch (Throwable t) {
            ErrorReporter.report(this, t);
        }
    }

    private void reloadEvaluationHistory(Long empId, JTable historyTable) {
        Async.run(this, () -> evaluationService.getPerformanceEvaluationHistory(empId), rows -> {
            renderEvaluationRows(historyTable, rows, empId);
            statusLabel.setText("인사평가: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void renderEvaluationRows(JTable table, List<PerformanceEvaluationDTO> rows, Long empId) {
        String targetName = selectedEmployee != null && empId.equals(selectedEmployee.getEmpId())
                ? selectedEmployee.getEname()
                : null;
        UiKit.setRows(table, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getEvaluationId(),
                        nameWithId(targetName, row.getTargetEmpId()),
                        row.getEvaluationYear(),
                        row.getEvaluationQuarter(),
                        row.getPerformanceGrade()
                })
                .collect(Collectors.toList()));
    }

    private void showTablePopup(JTable table, String title, String status) {
        JScrollPane scroll = UiKit.scroll(table);
        scroll.setPreferredSize(new Dimension(760, 260));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.PLAIN_MESSAGE);
        statusLabel.setText(status);
    }

    private Long readSelectedEmpId() {
        if (employeeTable.getSelectedRow() >= 0) {
            return UiKit.selectedLong(employeeTable, 0);
        }
        return requireLong(empIdField, "사번");
    }

    private JTextField readonlyField(String value) {
        JTextField field = UiKit.field(18);
        field.setText(UiKit.value(value));
        field.setEditable(false);
        field.setBackground(new java.awt.Color(248, 250, 252));
        return field;
    }

    private Long requireLong(JTextField field, String label) {
        Long value = UiKit.parseLong(field.getText());
        if (value == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요.");
        }
        return value;
    }

    private String requiredText(JTextField field, String label) {
        String value = UiKit.text(field);
        if (value == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요.");
        }
        return value;
    }

    private String nameWithId(String name, Object id) {
        String idText = UiKit.value(id);
        String nameText = UiKit.value(name);
        if (nameText.isEmpty()) {
            return idText.isEmpty() ? "" : "ID " + idText;
        }
        return idText.isEmpty() ? nameText : nameText + " (" + idText + ")";
    }

    private static class EmployeeRowsData {
        private final List<EmployeeDTO> employees;
        private final Map<Long, EmployeeInfoDTO> infoByEmployeeId;

        private EmployeeRowsData(List<EmployeeDTO> employees, Map<Long, EmployeeInfoDTO> infoByEmployeeId) {
            this.employees = employees;
            this.infoByEmployeeId = infoByEmployeeId;
        }
    }

    private static class EmployeeDetailData {
        private final EmployeeDTO employee;
        private final EmployeeInfoDTO info;

        private EmployeeDetailData(EmployeeDTO employee, EmployeeInfoDTO info) {
            this.employee = employee;
            this.info = info;
        }
    }
}
