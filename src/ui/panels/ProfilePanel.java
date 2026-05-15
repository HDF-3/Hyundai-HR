package ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import humanresource.dto.EmployeeDTO;
import humanresource.service.EmployeeService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class ProfilePanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final EmployeeService employeeService;

    private final JLabel empIdValue = valueLabel();
    private final JLabel nameValue = valueLabel();
    private final JLabel deptValue = valueLabel();
    private final JLabel positionValue = valueLabel();
    private final JLabel statusValue = valueLabel();
    private final JLabel payGradeValue = valueLabel();
    private final JLabel hireDateValue = valueLabel();
    private final JLabel resignDateValue = valueLabel();
    private final JLabel contactValue = valueLabel();
    private final JLabel genderValue = valueLabel();
    private final JLabel emailValue = valueLabel();
    private final JLabel addressValue = valueLabel();
    private final JLabel salaryAccountValue = valueLabel();
    private final JLabel roleValue = valueLabel();
    private final JLabel status = UiKit.statusLabel();

    public ProfilePanel(AppSession session, EmployeeService employeeService) {
        this.session = session;
        this.employeeService = employeeService;
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        JPanel root = UiKit.screen("내 정보", "현재 로그인한 계정의 사원 정보를 확인합니다.");
        add(root, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refresh = UiKit.button("새로고침");
        refresh.addActionListener(e -> refresh());
        actions.add(refresh);
        root.add(actions, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setOpaque(false);
        grid.add(basicInfoPanel());
        grid.add(contactInfoPanel());

        root.add(grid, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel basicInfoPanel() {
        JPanel panel = UiKit.surface();
        JLabel title = new JLabel("기본 정보");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(title, BorderLayout.NORTH);

        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "사번", empIdValue);
        UiKit.addField(form, row++, "이름", nameValue);
        UiKit.addField(form, row++, "부서 ID", deptValue);
        UiKit.addField(form, row++, "직급 ID", positionValue);
        UiKit.addField(form, row++, "재직 상태", statusValue);
        UiKit.addField(form, row++, "호봉", payGradeValue);
        UiKit.addField(form, row++, "입사일", hireDateValue);
        UiKit.addField(form, row++, "퇴사일", resignDateValue);
        UiKit.addField(form, row++, "권한", roleValue);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel contactInfoPanel() {
        JPanel panel = UiKit.surface();
        JLabel title = new JLabel("연락/계좌 정보");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(title, BorderLayout.NORTH);

        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "연락처", contactValue);
        UiKit.addField(form, row++, "성별", genderValue);
        UiKit.addField(form, row++, "이메일", emailValue);
        UiKit.addField(form, row++, "주소", addressValue);
        UiKit.addField(form, row++, "급여 계좌", salaryAccountValue);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refresh() {
        Long employeeId = session.getEmployeeId();
        if (employeeId == null) {
            clear();
            status.setText("로그인 정보가 없습니다.");
            return;
        }

        status.setText("내 정보 조회 중...");
        Async.run(this, () -> employeeService.getEmployeeinfo(employeeId), this::applyEmployee, e -> {
            status.setText("내 정보 조회 실패");
            UiKit.error(this, e);
        });
    }

    private void applyEmployee(EmployeeDTO employee) {
        if (employee == null) {
            clear();
            status.setText("사원 정보를 찾을 수 없습니다.");
            return;
        }

        session.setCurrentEmployee(employee);
        empIdValue.setText(UiKit.display(employee.getEmpId()));
        nameValue.setText(UiKit.display(employee.getEname()));
        deptValue.setText(UiKit.display(employee.getDeptId()));
        positionValue.setText(UiKit.display(employee.getPositionId()));
        statusValue.setText(UiKit.display(employee.getStatusId()));
        payGradeValue.setText(UiKit.display(employee.getPayGrade()));
        hireDateValue.setText(UiKit.display(employee.getHireDate()));
        resignDateValue.setText(UiKit.display(employee.getResignDate()));
        contactValue.setText(UiKit.display(employee.getContact()));
        genderValue.setText(UiKit.display(employee.getGender()));
        emailValue.setText(UiKit.display(employee.getEmail()));
        addressValue.setText(UiKit.display(employee.getAddress()));
        salaryAccountValue.setText(UiKit.display(employee.getSalAccount()));
        roleValue.setText(Boolean.TRUE.equals(employee.getIsAdmin()) ? "관리자" : "사용자");
        status.setText("내 정보 조회 완료");
    }

    private void clear() {
        JLabel[] labels = {
                empIdValue,
                nameValue,
                deptValue,
                positionValue,
                statusValue,
                payGradeValue,
                hireDateValue,
                resignDateValue,
                contactValue,
                genderValue,
                emailValue,
                addressValue,
                salaryAccountValue,
                roleValue
        };
        for (JLabel label : labels) {
            label.setText("-");
        }
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("-");
        label.setForeground(UiKit.TEXT);
        return label;
    }
}
