package ui.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import attendance.service.AttendanceModifyService;
import attendance.service.AttendanceService;
import humanresource.service.DepartmentService;
import humanresource.service.EmployeeService;
import humanresource.service.PerformanceEvaluationService;
import leave.service.LeaveService;
import payroll.service.PayrollService;
import ui.AppSession;
import ui.HyundaiHrApp;
import ui.Refreshable;
import ui.UiKit;

public class ShellPanel extends JPanel {
    private final HyundaiHrApp app;
    private final AppSession session;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final JLabel statusLabel = UiKit.statusLabel();
    private final Map<String, JButton> navButtons = new LinkedHashMap<String, JButton>();
    private final Map<String, Refreshable> refreshables = new LinkedHashMap<String, Refreshable>();
    private String currentKey;

    public ShellPanel(
            HyundaiHrApp app,
            AppSession session,
            EmployeeService employeeService,
            DepartmentService departmentService,
            PerformanceEvaluationService performanceEvaluationService,
            AttendanceService attendanceService,
            AttendanceModifyService attendanceModifyService,
            LeaveService leaveService,
            PayrollService payrollService
    ) {
        this.app = app;
        this.session = session;
        setLayout(new BorderLayout());
        setBackground(UiKit.BG);

        add(sidebar(), BorderLayout.WEST);
        add(shell(
                employeeService,
                departmentService,
                performanceEvaluationService,
                attendanceService,
                attendanceModifyService,
                leaveService,
                payrollService
        ), BorderLayout.CENTER);
        navigate("dashboard");
    }

    private JPanel shell(
            EmployeeService employeeService,
            DepartmentService departmentService,
            PerformanceEvaluationService performanceEvaluationService,
            AttendanceService attendanceService,
            AttendanceModifyService attendanceModifyService,
            LeaveService leaveService,
            PayrollService payrollService
    ) {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.add(topBar(), BorderLayout.NORTH);

        addScreen("dashboard", new DashboardPanel(session, employeeService, attendanceModifyService, leaveService, payrollService));
        addScreen("profile", new ProfilePanel(session, employeeService));
        if (session.isAdmin()) {
            addScreen("employees", new EmployeesPanel(session, employeeService, departmentService, performanceEvaluationService));
        }
        addScreen("attendance", new AttendancePanel(session, attendanceService, attendanceModifyService));
        addScreen("leave", new LeavePanel(session, leaveService));
        addScreen("payroll", new PayrollPanel(session, payrollService));

        shell.add(content, BorderLayout.CENTER);
        return shell;
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 16));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(new Color(32, 39, 49));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));

        JLabel brand = new JLabel("<html><b>Hyundai HR</b><br><span style='font-size:10px'>"
                + (session.isAdmin() ? "Admin Console" : "Self Service")
                + "</span></html>");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Dialog", Font.PLAIN, 17));
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 8));
        nav.setOpaque(false);
        addNav(nav, "profile", "내 정보");
        addNav(nav, "dashboard", "대시보드");
        if (session.isAdmin()) {
            addNav(nav, "employees", "인사");
        }
        addNav(nav, "attendance", "근태");
        addNav(nav, "leave", "휴가");
        addNav(nav, "payroll", "급여");
        sidebar.add(nav, BorderLayout.CENTER);

        JButton logout = UiKit.button("로그아웃");
        logout.addActionListener(e -> app.showLogin());
        sidebar.add(logout, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel topBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiKit.LINE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        JLabel user = new JLabel(session.getEmployeeName() + " · " + session.getEmployeeId()
                + " · " + (session.isAdmin() ? "Admin" : "User"));
        user.setForeground(UiKit.TEXT);

        JLabel meta = new JLabel("오늘 " + LocalDate.now() + " · DB HDF");
        meta.setForeground(UiKit.MUTED);

        top.add(user, BorderLayout.WEST);
        top.add(meta, BorderLayout.EAST);
        return top;
    }

    private void addNav(JPanel nav, String key, String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.addActionListener(e -> navigate(key));
        navButtons.put(key, button);
        nav.add(button);
    }

    private void addScreen(String key, JPanel panel) {
        content.add(panel, key);
        if (panel instanceof Refreshable) {
            refreshables.put(key, (Refreshable) panel);
        }
    }

    public void navigate(String key) {
        if (!navButtons.containsKey(key)) {
            return;
        }
        currentKey = key;
        cardLayout.show(content, key);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            entry.getValue().setBackground(active ? UiKit.PRIMARY : new Color(42, 50, 62));
            entry.getValue().setForeground(Color.WHITE);
        }
        Refreshable refreshable = refreshables.get(key);
        if (refreshable != null) {
            refreshable.refresh();
        }
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    public String getCurrentKey() {
        return currentKey;
    }
}
