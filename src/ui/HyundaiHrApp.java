package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;

import attendance.service.AttendanceModifyService;
import attendance.service.AttendanceService;
import humanresource.dto.EmployeeDTO;
import humanresource.service.DepartmentService;
import humanresource.service.EmployeeService;
import leave.service.LeaveService;
import payroll.service.PayrollService;
import ui.panels.LoginPanel;
import ui.panels.ShellPanel;

public class HyundaiHrApp extends JFrame {
    private final AppSession session = new AppSession();
    private final EmployeeService employeeService = new EmployeeService();
    private final DepartmentService departmentService = new DepartmentService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final AttendanceModifyService attendanceModifyService = new AttendanceModifyService();
    private final LeaveService leaveService = new LeaveService();
    private final PayrollService payrollService = new PayrollService();

    public HyundaiHrApp() {
        super("Hyundai HR");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        showLogin();
    }

    public static void launch() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception ignored) {
                    // Keep the platform look and feel when Nimbus is unavailable.
                }
                new HyundaiHrApp().setVisible(true);
            }
        });
    }

    public void showLogin() {
        session.clear();
        setContentPane(new LoginPanel(this, employeeService));
        revalidate();
        repaint();
    }

    public void showShell(EmployeeDTO employee) {
        session.setCurrentEmployee(employee);
        setContentPane(new ShellPanel(
                this,
                session,
                employeeService,
                departmentService,
                attendanceService,
                attendanceModifyService,
                leaveService,
                payrollService
        ));
        revalidate();
        repaint();
    }
}
