package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class UiKit {
    public static final Color BG = new Color(246, 248, 251);
    public static final Color SURFACE = Color.WHITE;
    public static final Color LINE = new Color(218, 226, 236);
    public static final Color TEXT = new Color(24, 32, 43);
    public static final Color MUTED = new Color(101, 116, 139);
    public static final Color PRIMARY = new Color(10, 76, 153);
    public static final Color PRIMARY_DARK = new Color(6, 49, 103);
    public static final Color DANGER = new Color(190, 18, 60);
    public static final int FIELD_HEIGHT = 34;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private UiKit() {
    }

    public static JPanel page(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(22, 24, 24, 24));
        panel.setBackground(BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        header.add(titleLabel, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.trim().isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setForeground(MUTED);
            subtitleLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
            header.add(subtitleLabel, BorderLayout.CENTER);
        }

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    public static JPanel surface() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JPanel section(String title, JComponent body) {
        JPanel panel = surface();
        JLabel label = new JLabel(title);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        label.setForeground(TEXT);
        panel.add(label, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    public static JButton primaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(new Color(234, 241, 249));
        button.setForeground(PRIMARY_DARK);
        return button;
    }

    public static JButton dangerButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(new Color(255, 241, 242));
        button.setForeground(DANGER);
        return button;
    }

    private static JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(197, 208, 224)),
                new EmptyBorder(8, 13, 8, 13)
        ));
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        return button;
    }

    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(7, 9, 7, 9)
        ));
        field.setPreferredSize(new Dimension(Math.max(90, columns * 11), FIELD_HEIGHT));
        return field;
    }

    public static <T> JComboBox<T> combo(T[] values) {
        JComboBox<T> combo = new FixedHeightComboBox<>(values);
        combo.setBackground(SURFACE);
        combo.setForeground(TEXT);
        combo.setFocusable(false);
        combo.setMaximumRowCount(10);
        combo.setBorder(BorderFactory.createLineBorder(LINE));
        Dimension size = new Dimension(preferredComboWidth(values), FIELD_HEIGHT);
        combo.setPreferredSize(size);
        combo.setMinimumSize(new Dimension(90, FIELD_HEIGHT));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        combo.setAlignmentY(Component.CENTER_ALIGNMENT);
        combo.setUI(new CenteredComboBoxUi());
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(7, 9, 7, 9));
                label.setForeground(isSelected ? list.getSelectionForeground() : TEXT);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setPreferredSize(new Dimension(label.getPreferredSize().width, FIELD_HEIGHT));
                if (value == null) {
                    label.setText("");
                }
                return label;
            }
        });
        return combo;
    }

    private static <T> int preferredComboWidth(T[] values) {
        int longest = 0;
        if (values != null) {
            for (T value : values) {
                longest = Math.max(longest, String.valueOf(value).length());
            }
        }
        return Math.max(110, longest * 12 + 42);
    }

    private static final class FixedHeightComboBox<T> extends JComboBox<T> {
        private FixedHeightComboBox(T[] values) {
            super(values);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(size.width, FIELD_HEIGHT);
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            return new Dimension(size.width, FIELD_HEIGHT);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension size = super.getMaximumSize();
            return new Dimension(size.width, FIELD_HEIGHT);
        }
    }

    private static final class CenteredComboBoxUi extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("▼");
            button.setFocusable(false);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(0, 8, 0, 8));
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBackground(SURFACE);
            button.setForeground(MUTED);
            button.setPreferredSize(new Dimension(30, FIELD_HEIGHT));
            button.setMinimumSize(new Dimension(30, FIELD_HEIGHT));
            return button;
        }

        @Override
        protected Rectangle rectangleForCurrentValue() {
            int buttonWidth = arrowButton == null ? 0 : arrowButton.getWidth();
            Insets insets = comboBox.getInsets();
            int width = comboBox.getWidth() - insets.left - insets.right - buttonWidth;
            int height = comboBox.getHeight() - insets.top - insets.bottom;
            return new Rectangle(insets.left, insets.top, Math.max(0, width), Math.max(0, height));
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(comboBox.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            ListCellRenderer<Object> renderer = (ListCellRenderer<Object>) comboBox.getRenderer();
            Component component = renderer.getListCellRendererComponent(
                    listBox,
                    comboBox.getSelectedItem(),
                    -1,
                    false,
                    false
            );
            component.setFont(comboBox.getFont());
            component.setForeground(comboBox.isEnabled() ? comboBox.getForeground() : MUTED);
            component.setBackground(comboBox.getBackground());
            currentValuePane.paintComponent(g, component, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, true);
        }
    }

    public static DateField dateField(LocalDate date, boolean optional) {
        return new DateField(date, optional);
    }

    public static JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    public static void addField(JPanel form, int row, String label, JComponent component) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(4, 0, 4, 10);
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(MUTED);
        form.add(jLabel, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.weightx = 1;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(4, 0, 4, 0);
        form.add(component, fieldGbc);
    }

    public static JPanel actions(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    public static JTable table(String... columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(232, 238, 246));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.getTableHeader().setForeground(MUTED);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalTime) {
                    setText(formatTime((LocalTime) value));
                    return;
                }
                super.setValue(value);
            }
        };
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        table.setDefaultRenderer(Object.class, renderer);
        return table;
    }

    public static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        return pane;
    }

    public static JScrollPane scroll(JComponent component) {
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        pane.getViewport().setBackground(SURFACE);
        return pane;
    }

    public static void setRows(JTable table, List<Object[]> rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Object[] row : safeList(rows)) {
            model.addRow(row);
        }
    }

    public static <T> List<T> safeList(List<T> rows) {
        return rows == null ? Collections.emptyList() : rows;
    }

    public static int safeSize(List<?> rows) {
        return safeList(rows).size();
    }

    public static Object selectedValue(JTable table, int column) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            throw new IllegalStateException("먼저 표에서 항목을 선택하세요.");
        }
        int modelRow = table.convertRowIndexToModel(selected);
        return table.getModel().getValueAt(modelRow, column);
    }

    public static Long selectedLong(JTable table, int column) {
        return toLong(selectedValue(table, column));
    }

    public static JLabel statusLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel metric(String title, String value) {
        JLabel label = new JLabel("<html><div style='color:#65748b;font-size:10px;'>" + escape(title)
                + "</div><div style='font-size:18px;font-weight:bold;color:#18202b;margin-top:4px;'>"
                + escape(value) + "</div></html>");
        label.setBorder(new EmptyBorder(10, 12, 10, 12));
        label.setOpaque(true);
        label.setBackground(new Color(250, 252, 255));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    public static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return parseLong(String.valueOf(value));
    }

    public static Long parseLong(String value) {
        String trimmed = clean(value);
        if (trimmed == null) {
            return null;
        }
        return Long.parseLong(trimmed);
    }

    public static Integer parseInteger(String value) {
        String trimmed = clean(value);
        if (trimmed == null) {
            return null;
        }
        return Integer.parseInt(trimmed);
    }

    public static BigDecimal parseMoney(String value) {
        String trimmed = clean(value);
        if (trimmed == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(trimmed.replace(",", ""));
    }

    public static LocalDate parseDate(String value) {
        String trimmed = clean(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜는 yyyy-MM-dd 형식으로 입력하세요: " + value);
        }
    }

    public static LocalDate requireDate(JTextField field, String label) {
        LocalDate date = parseDate(field.getText());
        if (date == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요. 날짜 형식: yyyy-MM-dd");
        }
        return date;
    }

    public static LocalDate requireDate(DateField field, String label) {
        LocalDate date = field.getDate();
        if (date == null) {
            throw new IllegalArgumentException(label + "을(를) 선택하세요.");
        }
        return date;
    }

    public static YearMonth parseYearMonth(String value) {
        String trimmed = clean(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return YearMonth.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("연월은 yyyy-MM 형식으로 입력하세요: " + value);
        }
    }

    public static LocalTime parseTime(String value) {
        String trimmed = clean(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalTime.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("시간은 HH:mm 형식으로 입력하세요: " + value);
        }
    }

    public static String text(JTextField field) {
        return clean(field.getText());
    }

    public static String formatMoney(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return NumberFormat.getNumberInstance().format(value);
    }

    public static String formatTime(LocalTime value) {
        return value == null ? "" : value.format(TIME_FORMAT);
    }

    public static String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static final class DateField extends JPanel {
        private final boolean optional;
        private final JTextField textField;
        private final JButton calendarButton;
        private final JPopupMenu calendarPopup = new JPopupMenu();
        private YearMonth visibleMonth;

        private DateField(LocalDate date, boolean optional) {
            super(new BorderLayout(6, 0));
            this.optional = optional;
            setOpaque(false);

            LocalDate initialDate = date == null && !optional ? LocalDate.now() : date;
            textField = field(10);
            textField.setPreferredSize(new Dimension(116, FIELD_HEIGHT));
            textField.setMinimumSize(new Dimension(116, FIELD_HEIGHT));

            calendarButton = new JButton("▼");
            calendarButton.setToolTipText("달력 열기");
            calendarButton.setFocusPainted(false);
            calendarButton.setOpaque(true);
            calendarButton.setBackground(SURFACE);
            calendarButton.setForeground(PRIMARY_DARK);
            calendarButton.setPreferredSize(new Dimension(40, FIELD_HEIGHT));
            calendarButton.setMinimumSize(new Dimension(40, FIELD_HEIGHT));
            calendarButton.setBorder(BorderFactory.createLineBorder(LINE));
            calendarButton.addActionListener(e -> showCalendar());

            add(textField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);

            setDate(initialDate);
        }

        public LocalDate getDate() {
            LocalDate date = parseDate(textField.getText());
            if (date == null && !optional && textField.getText() != null && !textField.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("날짜는 yyyy-MM-dd 형식으로 입력하세요: " + textField.getText());
            }
            return date;
        }

        public void setDate(LocalDate date) {
            textField.setText(date == null ? "" : date.format(DATE_FORMAT));
            visibleMonth = YearMonth.from(date == null ? LocalDate.now() : date);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (textField != null) {
                textField.setEnabled(enabled);
                textField.setEditable(enabled);
            }
            if (calendarButton != null) {
                calendarButton.setEnabled(enabled);
            }
        }

        private void showCalendar() {
            LocalDate selected = readDateLenient();
            visibleMonth = YearMonth.from(selected == null ? LocalDate.now() : selected);
            rebuildCalendar(selected);
            calendarPopup.show(this, 0, getHeight());
        }

        private LocalDate readDateLenient() {
            try {
                return parseDate(textField.getText());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        private void rebuildCalendar(LocalDate selected) {
            calendarPopup.removeAll();

            JPanel calendar = new JPanel(new BorderLayout(0, 8));
            calendar.setBorder(new EmptyBorder(10, 10, 10, 10));
            calendar.setBackground(SURFACE);
            calendar.add(calendarHeader(selected), BorderLayout.NORTH);
            calendar.add(calendarDays(selected), BorderLayout.CENTER);
            calendar.add(calendarActions(), BorderLayout.SOUTH);

            calendarPopup.add(calendar);
            calendarPopup.pack();
        }

        private JPanel calendarHeader(LocalDate selected) {
            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.setOpaque(false);

            JButton previous = calendarNavButton("<");
            previous.addActionListener(e -> {
                visibleMonth = visibleMonth.minusMonths(1);
                rebuildCalendar(selected);
            });

            JButton next = calendarNavButton(">");
            next.addActionListener(e -> {
                visibleMonth = visibleMonth.plusMonths(1);
                rebuildCalendar(selected);
            });

            JLabel monthLabel = new JLabel(visibleMonth.toString(), SwingConstants.CENTER);
            monthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            monthLabel.setForeground(TEXT);

            header.add(previous, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(next, BorderLayout.EAST);
            return header;
        }

        private JButton calendarNavButton(String text) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(36, 30));
            button.setBorder(BorderFactory.createLineBorder(LINE));
            button.setBackground(SURFACE);
            button.setForeground(PRIMARY_DARK);
            return button;
        }

        private JPanel calendarDays(LocalDate selected) {
            JPanel days = new JPanel(new GridLayout(0, 7, 4, 4));
            days.setOpaque(false);

            String[] names = {"월", "화", "수", "목", "금", "토", "일"};
            for (String name : names) {
                JLabel label = new JLabel(name, SwingConstants.CENTER);
                label.setForeground(MUTED);
                label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                days.add(label);
            }

            LocalDate firstDay = visibleMonth.atDay(1);
            int offset = firstDay.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++) {
                days.add(new JLabel(""));
            }

            for (int day = 1; day <= visibleMonth.lengthOfMonth(); day++) {
                LocalDate date = visibleMonth.atDay(day);
                JButton button = new JButton(String.valueOf(day));
                button.setFocusPainted(false);
                button.setPreferredSize(new Dimension(36, 30));
                button.setBorder(BorderFactory.createLineBorder(date.equals(selected) ? PRIMARY : LINE));
                button.setBackground(date.equals(LocalDate.now()) ? new Color(234, 241, 249) : SURFACE);
                button.setForeground(TEXT);
                button.addActionListener(e -> {
                    setDate(date);
                    calendarPopup.setVisible(false);
                });
                days.add(button);
            }
            return days;
        }

        private JPanel calendarActions() {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            actions.setOpaque(false);

            JButton today = calendarNavButton("오늘");
            today.setPreferredSize(new Dimension(58, 30));
            today.addActionListener(e -> {
                setDate(LocalDate.now());
                calendarPopup.setVisible(false);
            });
            actions.add(today);

            if (optional) {
                JButton clear = calendarNavButton("비우기");
                clear.setPreferredSize(new Dimension(68, 30));
                clear.addActionListener(e -> {
                    setDate(null);
                    calendarPopup.setVisible(false);
                });
                actions.add(clear);
            }

            return actions;
        }
    }
}
