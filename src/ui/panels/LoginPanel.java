package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import humanresource.dto.EmployeeDTO;
import humanresource.service.EmployeeService;
import ui.Async;
import ui.UiKit;

public class LoginPanel extends JPanel {
    private final EmployeeService employeeService = new EmployeeService();
    private final Consumer<EmployeeDTO> onLogin;
    private final JTextField empIdField = UiKit.field(18);
    private final JPasswordField passwordField = UiKit.passwordField(18);
    private final JLabel statusLabel = UiKit.statusLabel();

    public LoginPanel(Consumer<EmployeeDTO> onLogin) {
        this.onLogin = onLogin;
        setLayout(new GridBagLayout());
        setBackground(UiKit.BG);

        JPanel card = UiKit.surface();
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiKit.LINE),
                new EmptyBorder(28, 30, 28, 30)
        ));

        JLabel brand = new JLabel("Hyundai HR");
        brand.setFont(brand.getFont().deriveFont(28f));
        brand.setForeground(UiKit.PRIMARY_DARK);

        JLabel subtitle = new JLabel("인사, 근태, 휴가, 급여 관리 시스템입니다.");
        subtitle.setForeground(UiKit.MUTED);

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.add(brand, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);

        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "사번", empIdField);
        UiKit.addField(form, 1, "비밀번호", passwordField);

        JButton loginButton = UiKit.primaryButton("로그인");
        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
        empIdField.addActionListener(e -> passwordField.requestFocusInWindow());

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.add(loginButton, BorderLayout.NORTH);
        statusLabel.setForeground(new Color(90, 104, 124));
        bottom.add(statusLabel, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(card, gbc);
    }

    private void login() {
        Long empId;
        try {
            empId = UiKit.parseLong(empIdField.getText());
            if (empId == null) {
                throw new IllegalArgumentException("사번을 입력하세요.");
            }
        } catch (Exception e) {
            statusLabel.setText(e.getMessage());
            return;
        }

        String password = new String(passwordField.getPassword());
        statusLabel.setText("로그인 확인 중...");
        Async.run(this, () -> employeeService.authenticate(empId, password), employee -> {
            if (employee == null) {
                statusLabel.setText("사번 또는 비밀번호를 확인하세요.");
                return;
            }
            statusLabel.setText(" ");
            onLogin.accept(employee);
        });
    }
}
