package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class UiKit {
    public static final Color BG = new Color(245, 247, 250);
    public static final Color SURFACE = Color.WHITE;
    public static final Color LINE = new Color(220, 226, 235);
    public static final Color TEXT = new Color(34, 40, 49);
    public static final Color MUTED = new Color(102, 112, 133);
    public static final Color PRIMARY = new Color(22, 119, 255);
    public static final Color SUCCESS = new Color(31, 136, 61);
    public static final Color WARNING = new Color(176, 111, 0);

    private UiKit() {
    }

    public static JPanel screen(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        titleLabel.setForeground(TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED);

        header.add(titleLabel, BorderLayout.NORTH);
        header.add(subtitleLabel, BorderLayout.SOUTH);
        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    public static JPanel surface() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    public static JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE);
        return panel;
    }

    public static void addField(JPanel form, int row, String label, Component field) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(4, 0, 4, 8);
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(MUTED);
        form.add(jLabel, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.weightx = 1;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(4, 0, 4, 0);
        form.add(field, fieldGbc);
    }

    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return button;
    }

    public static JButton primaryButton(String text) {
        JButton button = button(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JLabel statusLabel() {
        JLabel label = new JLabel("준비됨");
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel todo(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(new Color(255, 247, 230));
        label.setForeground(WARNING);
        label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return label;
    }

    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                if (!isSelected) {
                    component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 252));
                    component.setForeground(TEXT);
                }
                return component;
            }
        });
        return table;
    }

    public static JScrollPane scroll(Component component) {
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        pane.setPreferredSize(new Dimension(500, 260));
        return pane;
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static <T> JComboBox<T> combo(T[] values) {
        JComboBox<T> combo = new JComboBox<T>(values);
        combo.setBackground(Color.WHITE);
        return combo;
    }

    public static String text(JTextField field) {
        String value = field.getText();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public static Long longValue(JTextField field) {
        String value = text(field);
        return value == null ? null : Long.valueOf(value);
    }

    public static Integer intValue(JTextField field) {
        String value = text(field);
        return value == null ? null : Integer.valueOf(value);
    }

    public static BigDecimal decimalValue(JTextField field) {
        String value = text(field);
        return value == null ? null : new BigDecimal(value.replace(",", ""));
    }

    public static LocalDate dateValue(JTextField field) {
        String value = text(field);
        return value == null ? null : LocalDate.parse(value);
    }

    public static LocalTime timeValue(JTextField field) {
        String value = text(field);
        return value == null ? null : LocalTime.parse(value);
    }

    public static YearMonth yearMonthValue(JTextField field) {
        String value = text(field);
        return value == null ? null : YearMonth.parse(value);
    }

    public static String display(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static String money(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return NumberFormat.getNumberInstance().format(value);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "알림", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, Exception e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        JOptionPane.showMessageDialog(parent, cause.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
    }

    public static void validation(Component parent, RuntimeException e) {
        String message = e instanceof DateTimeParseException
                ? "날짜/시간 형식을 확인하세요. 날짜는 YYYY-MM-DD, 월은 YYYY-MM, 시간은 HH:mm 입니다."
                : e.getMessage();
        JOptionPane.showMessageDialog(parent, message, "입력 확인", JOptionPane.WARNING_MESSAGE);
    }
}
