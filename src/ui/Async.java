package ui;

import java.awt.Component;
import java.awt.Cursor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingWorker;

public final class Async {
    private Async() {
    }

    public static <T> void run(Component owner, Supplier<T> task, Consumer<T> onSuccess) {
        Cursor before = owner == null ? null : owner.getCursor();
        if (owner != null) {
            owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        new SwingWorker<T, Void>() {
            private Throwable failure;

            @Override
            protected T doInBackground() {
                try {
                    return task.get();
                } catch (Throwable t) {
                    failure = t;
                    return null;
                }
            }

            @Override
            protected void done() {
                if (owner != null) {
                    owner.setCursor(before);
                }

                if (failure != null) {
                    ErrorReporter.report(owner, failure);
                    return;
                }

                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    ErrorReporter.report(owner, e);
                }
            }
        }.execute();
    }

    public static void runVoid(Component owner, Runnable task, Runnable onSuccess) {
        run(owner, () -> {
            task.run();
            return Boolean.TRUE;
        }, ignored -> onSuccess.run());
    }
}
