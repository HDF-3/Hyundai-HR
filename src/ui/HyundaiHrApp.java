package ui;

import java.awt.AWTEvent;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import humanresource.dto.EmployeeDTO;
import ui.panels.LoginPanel;
import ui.panels.ShellPanel;

public class HyundaiHrApp extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private static boolean errorHandlingInstalled;

    private HyundaiHrApp() {
        super("Hyundai HR");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));
        setLocationByPlatform(true);

        root.add(new LoginPanel(this::openShell), "login");
        setContentPane(root);
        cardLayout.show(root, "login");
    }

    public static void launch() {
        installGlobalErrorHandling();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // System look and feel is a visual enhancement, not a startup requirement.
            }
            UiKit.installLookAndFeelDefaults();
            HyundaiHrApp app = new HyundaiHrApp();
            app.pack();
            app.setVisible(true);
        });
    }

    private static synchronized void installGlobalErrorHandling() {
        if (errorHandlingInstalled) {
            return;
        }
        errorHandlingInstalled = true;

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> ErrorReporter.report(null, error));
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue() {
            @Override
            protected void dispatchEvent(AWTEvent event) {
                try {
                    super.dispatchEvent(event);
                } catch (Throwable error) {
                    ErrorReporter.report(null, error);
                }
            }
        });
    }

    private void openShell(EmployeeDTO employee) {
        AppSession session = new AppSession(employee);
        root.add(new ShellPanel(session, this::logout), "shell");
        cardLayout.show(root, "shell");
        revalidate();
        repaint();
    }

    private void logout() {
        root.removeAll();
        root.add(new LoginPanel(this::openShell), "login");
        cardLayout.show(root, "login");
        revalidate();
        repaint();
    }
}
