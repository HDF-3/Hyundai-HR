package ui;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class ErrorReporter {
    private static volatile boolean dialogEnabled = true;

    private ErrorReporter() {
    }

    public static void report(Component owner, Throwable failure) {
        if (failure == null) {
            return;
        }

        failure.printStackTrace(System.err);
        if (!dialogEnabled || GraphicsEnvironment.isHeadless()) {
            return;
        }

        String message = messageFor(failure);
        Runnable show = () -> JOptionPane.showMessageDialog(owner, message, "처리 실패", JOptionPane.ERROR_MESSAGE);
        if (SwingUtilities.isEventDispatchThread()) {
            show.run();
        } else {
            SwingUtilities.invokeLater(show);
        }
    }

    static String messageFor(Throwable failure) {
        Throwable root = rootCause(failure);
        String message = clean(failure.getMessage());
        String rootMessage = clean(root.getMessage());

        StringBuilder builder = new StringBuilder("작업 중 오류가 발생했습니다.");
        builder.append("\n\n");
        builder.append(message == null ? failure.getClass().getSimpleName() : message);

        if (root != failure && rootMessage != null && !containsSameMessage(message, rootMessage)) {
            builder.append("\n");
            builder.append(rootMessage);
        }

        builder.append("\n\n자세한 내용은 콘솔 로그를 확인하세요.");
        return builder.toString();
    }

    static void setDialogEnabledForTests(boolean enabled) {
        dialogEnabled = enabled;
    }

    private static boolean containsSameMessage(String message, String rootMessage) {
        return message != null && message.contains(rootMessage);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Throwable rootCause(Throwable failure) {
        Throwable current = failure;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
