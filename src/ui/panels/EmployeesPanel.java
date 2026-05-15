package ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.table.DefaultTableModel;

import global.types.EmploymentStatus;
import humanresource.dto.DepartmentDTO;
import humanresource.dto.EmployeeDTO;
import humanresource.dto.EmployeeInfoDTO;
import humanresource.service.DepartmentService;
import humanresource.service.EmployeeService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class EmployeesPanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    private final DefaultTableModel employeeModel = UiKit.model("사번", "이름", "부서", "직급", "호봉", "입사일");
    private final JTable employeeTable = UiKit.table(employeeModel);
    private final DefaultTableModel departmentModel = UiKit.model("부서ID", "부서명", "설명", "관리자", "상위부서");
    private final JTable departmentTable = UiKit.table(departmentModel);
    private final JLabel status = UiKit.statusLabel();

    private final JTextField empIdField = UiKit.field(12);
    private final JTextField nameField = UiKit.field(12);
    private final JTextField deptIdField = UiKit.field(12);
    private final JTextField positionIdField = UiKit.field(12);
    private final JComboBox<EmploymentStatus> statusCombo = UiKit.combo(EmploymentStatus.values());
    private final JTextField hireDateField = UiKit.field(12);
    private final JTextField resignDateField = UiKit.field(12);
    private final JTextField contactField = UiKit.field(12);
    private final JTextField genderField = UiKit.field(12);
    private final JTextField emailField = UiKit.field(12);
    private final JTextField addressField = UiKit.field(12);
    private final JTextField salaryAccountField = UiKit.field(12);
    private final JTextField payGradeField = UiKit.field(12);
    private final JTextField passwordField = UiKit.field(12);
    private final JCheckBox adminBox = new JCheckBox("관리자");

    private final JTextField deptNewIdField = UiKit.field(10);
    private final JTextField deptNameField = UiKit.field(10);
    private final JTextField deptDescField = UiKit.field(14);
    private final JTextField deptManagerField = UiKit.field(10);
    private final JTextField deptParentField = UiKit.field(10);

    private EmployeeDTO selectedEmployee;

    public EmployeesPanel(AppSession session, EmployeeService employeeService, DepartmentService departmentService) {
        this.session = session;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        JPanel root = UiKit.screen("인사", "직원, 부서, 관리자 권한을 관리합니다.");
        add(root, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("직원", employeeTab());
        tabs.addTab("부서", departmentTab());
        root.add(tabs, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel employeeTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel tablePanel = UiKit.surface();
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton refresh = UiKit.button("새로고침");
        JButton load = UiKit.button("선택 불러오기");
        refresh.addActionListener(e -> refreshEmployees());
        load.addActionListener(e -> loadSelectedEmployee());
        toolbar.add(refresh);
        toolbar.add(load);
        tablePanel.add(toolbar, BorderLayout.NORTH);
        tablePanel.add(UiKit.scroll(employeeTable), BorderLayout.CENTER);
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedEmployee();
            }
        });

        JPanel formPanel = UiKit.surface();
        formPanel.add(new JLabel("직원 상세"), BorderLayout.NORTH);
        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "사번", empIdField);
        UiKit.addField(form, row++, "이름", nameField);
        UiKit.addField(form, row++, "부서ID", deptIdField);
        UiKit.addField(form, row++, "직급ID", positionIdField);
        UiKit.addField(form, row++, "상태", statusCombo);
        UiKit.addField(form, row++, "입사일", hireDateField);
        UiKit.addField(form, row++, "퇴사일", resignDateField);
        UiKit.addField(form, row++, "연락처", contactField);
        UiKit.addField(form, row++, "성별", genderField);
        UiKit.addField(form, row++, "이메일", emailField);
        UiKit.addField(form, row++, "주소", addressField);
        UiKit.addField(form, row++, "급여계좌", salaryAccountField);
        UiKit.addField(form, row++, "호봉", payGradeField);
        UiKit.addField(form, row++, "초기 비밀번호", passwordField);
        adminBox.setEnabled(false);
        adminBox.setToolTipText("TODO: EmployeeDAO.updateEmployee에 IS_ADMIN 저장 로직 필요");
        UiKit.addField(form, row++, "권한", adminBox);
        formPanel.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton register = UiKit.primaryButton("등록");
        JButton update = UiKit.button("수정");
        JButton clear = UiKit.button("비우기");
        register.addActionListener(e -> registerEmployee());
        update.addActionListener(e -> updateEmployee());
        clear.addActionListener(e -> clearEmployeeForm());
        actions.add(clear);
        actions.add(update);
        actions.add(register);
        formPanel.add(actions, BorderLayout.SOUTH);

        JPanel split = new JPanel(new GridLayout(1, 2, 12, 0));
        split.setOpaque(false);
        split.add(tablePanel);
        split.add(formPanel);
        tab.add(split, BorderLayout.CENTER);
        tab.add(UiKit.todo("TODO: 관리자 권한 저장은 백엔드 #3 수정 전까지 읽기 전용으로 표시됩니다."), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel departmentTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel tablePanel = UiKit.surface();
        JButton refresh = UiKit.button("새로고침");
        refresh.addActionListener(e -> refreshDepartments());
        tablePanel.add(refresh, BorderLayout.NORTH);
        tablePanel.add(UiKit.scroll(departmentTable), BorderLayout.CENTER);

        JPanel formPanel = UiKit.surface();
        formPanel.add(new JLabel("부서 등록"), BorderLayout.NORTH);
        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "부서ID", deptNewIdField);
        UiKit.addField(form, row++, "부서명", deptNameField);
        UiKit.addField(form, row++, "설명", deptDescField);
        UiKit.addField(form, row++, "관리자ID", deptManagerField);
        UiKit.addField(form, row++, "상위부서ID", deptParentField);
        formPanel.add(form, BorderLayout.CENTER);

        JButton register = UiKit.primaryButton("부서 등록");
        register.addActionListener(e -> registerDepartment());
        formPanel.add(register, BorderLayout.SOUTH);

        JPanel split = new JPanel(new GridLayout(1, 2, 12, 0));
        split.setOpaque(false);
        split.add(tablePanel);
        split.add(formPanel);
        tab.add(split, BorderLayout.CENTER);
        return tab;
    }

    @Override
    public void refresh() {
        refreshEmployees();
        refreshDepartments();
    }

    private void refreshEmployees() {
        status.setText("직원 목록 조회 중...");
        Async.run(this, () -> employeeService.getEmployeeInfoList(), list -> {
            employeeModel.setRowCount(0);
            for (EmployeeInfoDTO employee : safe(list)) {
                employeeModel.addRow(new Object[] {
                        employee.getEmpId(),
                        employee.getEName(),
                        employee.getDeptName(),
                        employee.getPositionName(),
                        employee.getPayGrade(),
                        employee.getHireDate()
                });
            }
            status.setText("직원 " + employeeModel.getRowCount() + "건");
        }, e -> {
            status.setText("직원 조회 실패");
            UiKit.error(this, e);
        });
    }

    private void refreshDepartments() {
        Async.run(this, () -> departmentService.getAllDepartments(), list -> {
            departmentModel.setRowCount(0);
            for (DepartmentDTO dept : safe(list)) {
                departmentModel.addRow(new Object[] {
                        dept.getDeptId(),
                        dept.getDeptName(),
                        dept.getDeptDesc(),
                        dept.getManagerId(),
                        dept.getParentDeptId()
                });
            }
        }, e -> UiKit.error(this, e));
    }

    private void loadSelectedEmployee() {
        int viewRow = employeeTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = employeeTable.convertRowIndexToModel(viewRow);
        Long empId = Long.valueOf(String.valueOf(employeeModel.getValueAt(modelRow, 0)));
        Async.run(this, () -> employeeService.getEmployeeinfo(empId), employee -> {
            selectedEmployee = employee;
            populateEmployee(employee);
        }, e -> UiKit.error(this, e));
    }

    private void populateEmployee(EmployeeDTO employee) {
        if (employee == null) {
            return;
        }
        empIdField.setText(UiKit.display(employee.getEmpId()));
        nameField.setText(UiKit.display(employee.getEname()));
        deptIdField.setText(UiKit.display(employee.getDeptId()));
        positionIdField.setText(UiKit.display(employee.getPositionId()));
        statusCombo.setSelectedItem(employee.getStatusId());
        hireDateField.setText(UiKit.display(employee.getHireDate()));
        resignDateField.setText(UiKit.display(employee.getResignDate()));
        contactField.setText(UiKit.display(employee.getContact()));
        genderField.setText(UiKit.display(employee.getGender()));
        emailField.setText(UiKit.display(employee.getEmail()));
        addressField.setText(UiKit.display(employee.getAddress()));
        salaryAccountField.setText(UiKit.display(employee.getSalAccount()));
        payGradeField.setText(UiKit.display(employee.getPayGrade()));
        passwordField.setText("");
        adminBox.setSelected(Boolean.TRUE.equals(employee.getIsAdmin()));
    }

    private void registerEmployee() {
        try {
            EmployeeDTO employee = readEmployeeForm(false);
            status.setText("직원 등록 중...");
            Async.run(this, () -> employeeService.registerEmployee(employee), result -> {
                status.setText(result ? "직원 등록 완료" : "직원 등록 실패");
                refreshEmployees();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void updateEmployee() {
        try {
            EmployeeDTO employee = readEmployeeForm(true);
            status.setText("직원 수정 중...");
            Async.run(this, () -> employeeService.modifyEmployeeInfo(employee), result -> {
                status.setText(result ? "직원 수정 완료" : "직원 수정 실패");
                refreshEmployees();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private EmployeeDTO readEmployeeForm(boolean updating) {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setEmpId(requiredLong(empIdField, "사번"));
        employee.setEname(requiredText(nameField, "이름"));
        employee.setDeptId(requiredLong(deptIdField, "부서ID"));
        employee.setPositionId(requiredLong(positionIdField, "직급ID"));
        employee.setStatusId((EmploymentStatus) statusCombo.getSelectedItem());
        employee.setHireDate(defaultDate(hireDateField, LocalDate.now()));
        employee.setResignDate(UiKit.dateValue(resignDateField));
        employee.setContact(UiKit.text(contactField));
        employee.setGender(UiKit.text(genderField));
        employee.setEmail(UiKit.text(emailField));
        employee.setAddress(UiKit.text(addressField));
        employee.setSalAccount(UiKit.text(salaryAccountField));
        Integer payGrade = UiKit.intValue(payGradeField);
        employee.setPayGrade(payGrade == null ? 1 : payGrade);

        if (updating && selectedEmployee != null) {
            employee.setPassword(selectedEmployee.getPassword());
            employee.setIsAdmin(selectedEmployee.getIsAdmin());
        } else {
            String password = UiKit.text(passwordField);
            employee.setPassword(password == null ? "1234" : password);
            employee.setIsAdmin(false);
        }
        return employee;
    }

    private void registerDepartment() {
        try {
            DepartmentDTO dept = new DepartmentDTO();
            dept.setDeptId(requiredLong(deptNewIdField, "부서ID"));
            dept.setDeptName(requiredText(deptNameField, "부서명"));
            dept.setDeptDesc(UiKit.text(deptDescField));
            dept.setManagerId(UiKit.longValue(deptManagerField));
            dept.setParentDeptId(UiKit.longValue(deptParentField));
            Async.run(this, () -> departmentService.registerDepartment(dept), result -> {
                status.setText(result > 0 ? "부서 등록 완료" : "부서 등록 실패");
                refreshDepartments();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void clearEmployeeForm() {
        selectedEmployee = null;
        empIdField.setText("");
        nameField.setText("");
        deptIdField.setText("");
        positionIdField.setText("");
        statusCombo.setSelectedItem(EmploymentStatus.ACTIVE);
        hireDateField.setText("");
        resignDateField.setText("");
        contactField.setText("");
        genderField.setText("");
        emailField.setText("");
        addressField.setText("");
        salaryAccountField.setText("");
        payGradeField.setText("");
        passwordField.setText("");
        adminBox.setSelected(false);
    }

    private static String requiredText(JTextField field, String label) {
        String value = UiKit.text(field);
        if (value == null) {
            throw new IllegalArgumentException(label + "은(는) 필수입니다.");
        }
        return value;
    }

    private static Long requiredLong(JTextField field, String label) {
        Long value = UiKit.longValue(field);
        if (value == null) {
            throw new IllegalArgumentException(label + "은(는) 필수입니다.");
        }
        return value;
    }

    private static LocalDate defaultDate(JTextField field, LocalDate fallback) {
        LocalDate value = UiKit.dateValue(field);
        return value == null ? fallback : value;
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? java.util.Collections.<T>emptyList() : list;
    }
}
