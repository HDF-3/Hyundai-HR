package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import payroll.dto.AdditionalAllowanceDTO;
import payroll.dto.PayrollDTO;
import payroll.dto.PayrollDetailDTO;
import payroll.service.PayrollService;
import ui.AppSession;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class PayrollPanel extends JPanel implements Refreshable {
    private final AppSession session;
    private final PayrollService payrollService = new PayrollService();
    private final JTable payrollTable = UiKit.table("급여ID", "급여월", "총지급", "총공제", "실수령", "상태", "지급일");
    private final JPanel statementPanel = new JPanel(new BorderLayout());
    private final JLabel statusLabel = UiKit.statusLabel();
    private PayrollDetailDTO currentDetail;
    private List<AdditionalAllowanceDTO> currentAllowances = Collections.emptyList();

    public PayrollPanel(AppSession session) {
        this.session = session;
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("급여", "월별 급여 목록에서 원하는 달을 선택해 명세서를 확인합니다.");
        statementPanel.setOpaque(false);
        statementPanel.add(emptyStatement(), BorderLayout.CENTER);

        payrollTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && payrollTable.getSelectedRow() >= 0) {
                loadDetail();
            }
        });

        JScrollPane payrollScroll = UiKit.scroll(payrollTable);
        payrollScroll.setPreferredSize(new Dimension(430, 520));

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                UiKit.section("월별 급여 목록", payrollScroll),
                UiKit.section("급여 명세서", statementPanel)
        );
        split.setResizeWeight(0.36);
        split.setBorder(null);
        split.setOpaque(false);

        page.add(split, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        statusLabel.setText("급여 내역을 불러오는 중...");
        Async.run(this, () -> payrollService.getPayrollList(session.getEmployeeId()), rows -> {
            renderRows(rows);
            statusLabel.setText("급여 내역: " + UiKit.safeSize(rows) + "건");
            if (!UiKit.safeList(rows).isEmpty()) {
                payrollTable.setRowSelectionInterval(0, 0);
            } else {
                renderDetail(null);
                renderAllowances(null);
            }
        });
    }

    private void renderRows(List<PayrollDTO> rows) {
        UiKit.setRows(payrollTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getPayrollId(),
                        row.getPayrollYearMonth(),
                        UiKit.formatMoney(row.getTotalEarnings()),
                        UiKit.formatMoney(row.getTotalDeductions()),
                        UiKit.formatMoney(row.getNetPay()),
                        row.getStatus(),
                        row.getPaidAt()
                })
                .collect(Collectors.toList()));
    }

    private void loadDetail() {
        if (payrollTable.getSelectedRow() < 0) {
            return;
        }
        Long payrollId = UiKit.selectedLong(payrollTable, 0);
        YearMonth yearMonth = (YearMonth) UiKit.selectedValue(payrollTable, 1);
        Async.run(this, () -> new DetailView(
                payrollService.getPayrollDetail(payrollId),
                payrollService.getAdditionalAllowanceList(session.getEmployeeId(), yearMonth)
        ), detailView -> {
            renderDetail(detailView.detail);
            renderAllowances(detailView.allowances);
            statusLabel.setText("상세 조회 완료: " + payrollId);
        });
    }

    private void renderDetail(PayrollDetailDTO detail) {
        currentDetail = detail;
        statementPanel.removeAll();
        if (detail == null) {
            statementPanel.add(emptyStatement(), BorderLayout.CENTER);
        } else {
            statementPanel.add(statement(detail), BorderLayout.CENTER);
        }
        statementPanel.revalidate();
        statementPanel.repaint();
    }

    private void renderAllowances(List<AdditionalAllowanceDTO> allowances) {
        currentAllowances = allowances == null ? Collections.emptyList() : allowances;
    }

    private JPanel emptyStatement() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel("급여 목록에서 월을 선택하세요.");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(UiKit.MUTED);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statement(PayrollDetailDTO detail) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.add(statementHeader(detail), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(totalStrip(detail));
        body.add(Box.createVerticalStrut(14));
        body.add(detailColumns(detail));
        body.add(Box.createVerticalStrut(14));
        body.add(allowanceAction(detail));

        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statementHeader(PayrollDetailDTO detail) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JLabel title = new JLabel(detail.getPayrollYearMonth() + " 급여 명세서");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(UiKit.TEXT);
        panel.add(title, BorderLayout.WEST);

        JLabel meta = new JLabel("<html><div style='text-align:right;'>"
                + UiKit.value(detail.getEmployeeName()) + " · " + UiKit.value(detail.getEmployeeId())
                + "<br><span style='color:#65748b;'>" + UiKit.value(detail.getStatus()) + "</span></div></html>");
        meta.setHorizontalAlignment(SwingConstants.RIGHT);
        meta.setForeground(UiKit.MUTED);
        panel.add(meta, BorderLayout.EAST);

        return panel;
    }

    private JPanel totalStrip(PayrollDetailDTO detail) {
        JPanel panel = new JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);
        panel.add(totalBox("총 지급액", UiKit.formatMoney(detail.getTotalEarnings()), UiKit.TEXT));
        panel.add(totalBox("총 공제액", UiKit.formatMoney(detail.getTotalDeductions()), UiKit.DANGER));
        panel.add(totalBox("실수령액", UiKit.formatMoney(detail.getNetPay()), UiKit.PRIMARY));
        return panel;
    }

    private JPanel totalBox(String title, String value, Color valueColor) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setBackground(new Color(250, 252, 255));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiKit.LINE),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UiKit.MUTED);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(valueColor);
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        box.add(titleLabel, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }

    private JPanel detailColumns(PayrollDetailDTO detail) {
        JPanel grid = new JPanel(new java.awt.GridLayout(1, 2, 14, 0));
        grid.setOpaque(false);
        grid.add(lineItems("지급 항목", new Object[][] {
                {"기본급", UiKit.formatMoney(detail.getBaseSalary())},
                {"연장수당", UiKit.formatMoney(detail.getOvertimePay())},
                {"교통비", UiKit.formatMoney(detail.getTransportationAllowance())},
                {"성과급", UiKit.formatMoney(detail.getPerformanceBonus())},
                {"기타수당", UiKit.formatMoney(detail.getAdditionalAllowance())}
        }, "총 지급", UiKit.formatMoney(detail.getTotalEarnings()), earningFormula(detail)));
        grid.add(lineItems("공제 항목", new Object[][] {
                {"국민연금", UiKit.formatMoney(detail.getNationalPension())},
                {"건강보험", UiKit.formatMoney(detail.getHealthInsurance())},
                {"장기요양보험", UiKit.formatMoney(detail.getLongTermCareInsurance())},
                {"고용보험", UiKit.formatMoney(detail.getEmploymentInsurance())},
                {"소득세", UiKit.formatMoney(detail.getIncomeTax())},
                {"지방소득세", UiKit.formatMoney(detail.getLocalIncomeTax())}
        }, "총 공제", UiKit.formatMoney(detail.getTotalDeductions()), deductionFormula(detail)));
        return grid;
    }

    private String earningFormula(PayrollDetailDTO detail) {
        return "<html><body style='width:360px;'>"
                + "<b>총 지급 계산</b><br><br>"
                + "기본급: " + UiKit.formatMoney(detail.getBaseSalary()) + "<br>"
                + "연장수당: " + UiKit.formatMoney(detail.getOvertimePay()) + "<br>"
                + "교통비: " + UiKit.formatMoney(detail.getTransportationAllowance()) + "<br>"
                + "성과급: " + UiKit.formatMoney(detail.getPerformanceBonus()) + "<br>"
                + "기타수당: " + UiKit.formatMoney(detail.getAdditionalAllowance()) + "<br><br>"
                + "총 지급 = 기본급 + 연장수당 + 교통비 + 성과급 + 기타수당<br>"
                + "= " + UiKit.formatMoney(detail.getBaseSalary())
                + " + " + UiKit.formatMoney(detail.getOvertimePay())
                + " + " + UiKit.formatMoney(detail.getTransportationAllowance())
                + " + " + UiKit.formatMoney(detail.getPerformanceBonus())
                + " + " + UiKit.formatMoney(detail.getAdditionalAllowance()) + "<br>"
                + "= " + UiKit.formatMoney(detail.getTotalEarnings())
                + "</body></html>";
    }

    private String deductionFormula(PayrollDetailDTO detail) {
        return "<html><body style='width:390px;'>"
                + "<b>총 공제 계산</b><br><br>"
                + "국민연금: " + UiKit.formatMoney(detail.getNationalPension()) + "<br>"
                + "건강보험: " + UiKit.formatMoney(detail.getHealthInsurance()) + "<br>"
                + "장기요양보험: " + UiKit.formatMoney(detail.getLongTermCareInsurance()) + "<br>"
                + "고용보험: " + UiKit.formatMoney(detail.getEmploymentInsurance()) + "<br>"
                + "소득세: " + UiKit.formatMoney(detail.getIncomeTax()) + "<br>"
                + "지방소득세: " + UiKit.formatMoney(detail.getLocalIncomeTax()) + "<br><br>"
                + "총 공제 = 국민연금 + 건강보험 + 장기요양보험 + 고용보험 + 소득세 + 지방소득세<br>"
                + "= " + UiKit.formatMoney(detail.getNationalPension())
                + " + " + UiKit.formatMoney(detail.getHealthInsurance())
                + " + " + UiKit.formatMoney(detail.getLongTermCareInsurance())
                + " + " + UiKit.formatMoney(detail.getEmploymentInsurance())
                + " + " + UiKit.formatMoney(detail.getIncomeTax())
                + " + " + UiKit.formatMoney(detail.getLocalIncomeTax()) + "<br>"
                + "= " + UiKit.formatMoney(detail.getTotalDeductions()) + "<br><br>"
                + "실수령액 = 총 지급 - 총 공제<br>"
                + "= " + UiKit.formatMoney(detail.getTotalEarnings())
                + " - " + UiKit.formatMoney(detail.getTotalDeductions()) + "<br>"
                + "= " + UiKit.formatMoney(detail.getNetPay())
                + "</body></html>";
    }

    private JPanel lineItems(String title, Object[][] rows, String totalLabel, String totalValue, String formulaDescription) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiKit.LINE),
                new EmptyBorder(14, 16, 14, 16)
        ));

        panel.add(lineItemHeader(title, formulaDescription));
        panel.add(Box.createVerticalStrut(10));

        for (Object[] row : rows) {
            panel.add(amountRow(String.valueOf(row[0]), String.valueOf(row[1]), false));
        }

        panel.add(Box.createVerticalStrut(8));
        panel.add(separator());
        panel.add(Box.createVerticalStrut(8));
        panel.add(amountRow(totalLabel, totalValue, true));
        return panel;
    }

    private JPanel lineItemHeader(String title, String formulaDescription) {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLabel.setForeground(UiKit.TEXT);

        JButton help = formulaHelpButton(title, formulaDescription);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(help, BorderLayout.EAST);
        return header;
    }

    private JButton formulaHelpButton(String title, String formulaDescription) {
        JButton help = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(234, 241, 249));
                g2.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
                g2.setColor(UiKit.PRIMARY);
                g2.setFont(getFont());
                String text = "?";
                int x = (getWidth() - g2.getFontMetrics().stringWidth(text)) / 2;
                int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiKit.LINE);
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                g2.dispose();
            }
        };
        help.setHorizontalAlignment(SwingConstants.CENTER);
        help.setVerticalAlignment(SwingConstants.CENTER);
        help.setForeground(UiKit.PRIMARY);
        help.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        help.setFocusPainted(false);
        help.setContentAreaFilled(false);
        help.setBorderPainted(false);
        help.setOpaque(false);
        help.setPreferredSize(new Dimension(22, 22));
        help.setMinimumSize(new Dimension(22, 22));
        help.setMaximumSize(new Dimension(22, 22));
        help.setToolTipText("계산식 보기");
        help.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                new JLabel(formulaDescription),
                title + " 계산 공식",
                JOptionPane.PLAIN_MESSAGE
        ));
        return help;
    }

    private JPanel amountRow(String label, String value, boolean total) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelView = new JLabel(label);
        labelView.setForeground(total ? UiKit.TEXT : UiKit.MUTED);
        labelView.setFont(new Font(Font.SANS_SERIF, total ? Font.BOLD : Font.PLAIN, 13));

        JLabel valueView = new JLabel(value);
        valueView.setHorizontalAlignment(SwingConstants.RIGHT);
        valueView.setForeground(UiKit.TEXT);
        valueView.setFont(new Font(Font.SANS_SERIF, total ? Font.BOLD : Font.PLAIN, 13));

        row.add(labelView, BorderLayout.WEST);
        row.add(valueView, BorderLayout.EAST);
        return row;
    }

    private Component separator() {
        JPanel line = new JPanel(new BorderLayout());
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(1, 1));
        line.setBackground(UiKit.LINE);
        return line;
    }

    private JPanel allowanceAction(PayrollDetailDTO detail) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton open = UiKit.secondaryButton("기타수당 내역 확인");
        open.addActionListener(e -> showAllowancePopup());

        panel.add(open);
        int height = open.getPreferredSize().height;
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return panel;
    }

    private void showAllowancePopup() {
        JTable table = UiKit.table("수당명", "적용월", "금액");
        UiKit.setRows(table, UiKit.safeList(currentAllowances).stream()
                .map(row -> new Object[] {
                        row.getAdditionalAllowanceName(),
                        row.getAdditionalAllowanceYearMonth(),
                        UiKit.formatMoney(row.getAmount())
                })
                .collect(Collectors.toList()));

        JScrollPane scroll = UiKit.scroll(table);
        scroll.setPreferredSize(new Dimension(420, 180));

        String month = currentDetail == null ? "" : UiKit.value(currentDetail.getPayrollYearMonth());
        JOptionPane.showMessageDialog(
                this,
                scroll,
                month + " 기타수당 내역",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private static class DetailView {
        private final PayrollDetailDTO detail;
        private final List<AdditionalAllowanceDTO> allowances;

        private DetailView(PayrollDetailDTO detail, List<AdditionalAllowanceDTO> allowances) {
            this.detail = detail;
            this.allowances = allowances;
        }
    }
}
