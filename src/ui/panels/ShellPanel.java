package ui.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import ui.AppSession;
import ui.NavigationModel;
import ui.Refreshable;
import ui.UiKit;

public class ShellPanel extends JPanel {
    private final AppSession session;
    private final Runnable onLogout;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JPanel> panels = new LinkedHashMap<>();
    private final Map<String, JButton> buttons = new LinkedHashMap<>();
    private AttendanceQuickWidget attendanceWidget;
    private String activeRoute = "profile";

    public ShellPanel(AppSession session, Runnable onLogout) {
        this.session = session;
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setBackground(UiKit.BG);

        createPanels();
        add(sidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        open("profile");
    }

    private void createPanels() {
        register("profile", new ProfilePanel(session));
        register("attendance", new AttendancePanel(session));
        register("leave", new LeavePanel(session));
        register("payroll", new PayrollPanel(session));

        if (session.isAdmin()) {
            register("employees", new EmployeesPanel(session));
            register("departments", new DepartmentsPanel());
            register("attendanceApproval", new AttendanceApprovalPanel());
            register("leaveApproval", new LeaveApprovalPanel(session));
            register("payrollAdmin", new PayrollAdminPanel());
        }
    }

    private void register(String route, JPanel panel) {
        panels.put(route, panel);
        content.add(panel, route);
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 18));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(14, 35, 61));
        sidebar.setBorder(new EmptyBorder(22, 16, 18, 16));

        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setOpaque(false);

        JLabel brand = new JLabel("<html><b>Hyundai HR</b><br><span style='font-size:10px;'>Swing Console</span></html>");
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(0, 4, 8, 4));
        header.add(brand, BorderLayout.NORTH);

        attendanceWidget = new AttendanceQuickWidget(session);
        header.add(attendanceWidget, BorderLayout.CENTER);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        for (NavigationModel.Item item : NavigationModel.itemsFor(session.isAdmin())) {
            JButton button = navButton(item);
            buttons.put(item.getRoute(), button);
            nav.add(button);
            nav.add(Box.createVerticalStrut(8));
        }
        JScrollPane navScroll = new JScrollPane(
                nav,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        navScroll.setBorder(null);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.getVerticalScrollBar().setUnitIncrement(12);

        JButton logout = UiKit.secondaryButton("로그아웃");
        logout.addActionListener(e -> onLogout.run());

        sidebar.add(header, BorderLayout.NORTH);
        sidebar.add(navScroll, BorderLayout.CENTER);
        sidebar.add(logout, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton navButton(NavigationModel.Item item) {
        JButton button = new JButton(item.getLabel());
        UiKit.styleButton(button);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBorder(new EmptyBorder(10, 12, 10, 12));
        button.setForeground(new Color(219, 232, 245));
        button.setBackground(new Color(14, 35, 61));
        button.setPreferredSize(new Dimension(198, 40));
        button.setMinimumSize(new Dimension(0, 40));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.addActionListener(e -> open(item.getRoute()));
        return button;
    }

    private void open(String route) {
        activeRoute = route;
        cardLayout.show(content, route);
        buttons.forEach((key, button) -> {
            boolean active = key.equals(activeRoute);
            button.setBackground(active ? UiKit.PRIMARY : new Color(14, 35, 61));
            button.setForeground(active ? Color.WHITE : new Color(219, 232, 245));
        });

        JPanel panel = panels.get(route);
        if (panel instanceof Refreshable) {
            ((Refreshable) panel).refresh();
        }
        if (attendanceWidget != null) {
            attendanceWidget.refresh();
        }
    }
}
