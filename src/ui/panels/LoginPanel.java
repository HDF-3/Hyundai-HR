package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import humanresource.dto.EmployeeDTO;
import humanresource.service.EmployeeService;
import ui.Async;
import ui.HyundaiHrApp;
import ui.UiKit;

public class LoginPanel extends JPanel {
    private final HyundaiHrApp app;
    private final EmployeeService employeeService;
    private final JTextField empIdField = UiKit.field(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel statusLabel = UiKit.statusLabel();

    public LoginPanel(HyundaiHrApp app, EmployeeService employeeService) {
        this.app = app;
        this.employeeService = employeeService;
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        setBackground(UiKit.BG);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        add(center, BorderLayout.CENTER);

        JPanel card = UiKit.surface();
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiKit.LINE),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        card.add(content, BorderLayout.CENTER);

        JLabel title = new JLabel("Hyundai HR");
        title.setFont(title.getFont().deriveFont(26f));
        title.setForeground(UiKit.TEXT);
        JLabel subtitle = new JLabel("사번과 비밀번호로 로그인하세요.");
        subtitle.setForeground(UiKit.MUTED);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 0);
        content.add(title, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 22, 0);
        content.add(subtitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.insets = new Insets(6, 0, 6, 12);
        content.add(new JLabel("사번"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        content.add(empIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(6, 0, 6, 12);
        content.add(new JLabel("비밀번호"), gbc);

        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiKit.LINE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        content.add(passwordField, gbc);

        JButton loginButton = UiKit.primaryButton("로그인");
        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(18, 0, 8, 0);
        content.add(loginButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 0, 0, 0);
        content.add(statusLabel, gbc);

        center.add(card);
    }

    private void login() {
        try {
            final Long empId = Long.valueOf(UiKit.text(empIdField));
            final String password = new String(passwordField.getPassword());
            statusLabel.setText("로그인 확인 중...");

            Async.run(this,
                    () -> employeeService.authenticate(empId, password),
                    this::onLoginResult,
                    e -> {
                        statusLabel.setText("로그인 실패");
                        UiKit.error(this, e);
                    });
        } catch (RuntimeException e) {
            UiKit.validation(this, new IllegalArgumentException("사번은 숫자로 입력하세요."));
        }
    }

    private void onLoginResult(EmployeeDTO employee) {
        if (employee == null) {
            statusLabel.setText("사번 또는 비밀번호를 확인하세요.");
            return;
        }
        app.showShell(employee);
    }
}
