package ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

public final class Async {
    private Async() {
    }

    public static <T> void run(
            JComponent owner,
            Supplier<T> task,
            Consumer<T> onSuccess,
            Consumer<Exception> onError
    ) {
        owner.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return task.get();
            }

            @Override
            protected void done() {
                owner.setCursor(java.awt.Cursor.getDefaultCursor());
                try {
                    onSuccess.accept(get());
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        };
        worker.execute();
    }
}
