package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import global.types.CommonStatus;
import payroll.dao.DeductionDAO;
import payroll.dao.EarningDAO;
import payroll.dto.AdditionalAllowanceDTO;
import payroll.dto.DeductionDTO;
import payroll.dto.EarningDTO;
import payroll.dto.PayrollDTO;
import payroll.dto.PayrollDetailDTO;
import payroll.dto.PerformanceBonusPolicyDTO;
import payroll.dto.SalaryStandardDTO;
import payroll.service.PayrollService;
import payroll.service.PerformanceBonusPolicyService;
import payroll.service.SalaryStandardService;
import ui.Async;
import ui.ErrorReporter;
import ui.Refreshable;
import ui.UiKit;

public class PayrollAdminPanel extends JPanel implements Refreshable {
    private final PayrollService payrollService = new PayrollService();
    private final SalaryStandardService salaryStandardService = new SalaryStandardService();
    private final PerformanceBonusPolicyService bonusPolicyService = new PerformanceBonusPolicyService();
    private final EarningDAO earningDAO = new EarningDAO();
    private final DeductionDAO deductionDAO = new DeductionDAO();

    private final JTable payrollTable = UiKit.table("급여ID", "사번", "급여월", "총지급", "총공제", "실수령", "상태", "확정일", "지급일");
    private final JTable salaryStandardTable = UiKit.table("기준ID", "직급ID", "호봉", "기본급", "통상시급");
    private final JTable bonusPolicyTable = UiKit.table("정책ID", "연도", "분기", "등급", "비율", "고정금액");
    private final JPanel statementPanel = new JPanel(new BorderLayout());
    private final JLabel statusLabel = UiKit.statusLabel();

    private final JTextField searchMonthField = UiKit.field(8);
    private final JComboBox<String> searchStatusCombo = UiKit.combo(statusValues());
    private final JTextField searchDeptField = UiKit.field(8);
    private final JTextField searchPositionField = UiKit.field(8);
    private final JTextField searchEmpField = UiKit.field(8);

    private final JTextField salaryPositionField = UiKit.field(8);
    private final JTextField salaryPayGradeField = UiKit.field(6);

    private final JTextField policyYearField = UiKit.field(8);
    private final JTextField policyQuarterField = UiKit.field(4);

    private PayrollDetailDTO currentDetail;
    private EarningDTO currentEarning;
    private DeductionDTO currentDeduction;
    private List<AdditionalAllowanceDTO> currentAllowances = Collections.emptyList();
    private Long selectedPayrollId;

    public PayrollAdminPanel() {
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("급여 관리", "월별 직원 급여를 조회하고 명세서 항목, 기타수당, 급여 정책을 관리합니다.");
        searchMonthField.setText(YearMonth.now().minusMonths(1).toString());
        policyYearField.setText(String.valueOf(LocalDate.now().getYear()));
        policyQuarterField.setText("1");

        statementPanel.setOpaque(false);
        statementPanel.add(emptyStatement(), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("급여 명세 관리", payrollManagementTab());
        tabs.addTab("기본급 정책", salaryPolicyTab());
        tabs.addTab("성과급 정책", bonusPolicyTab());

        page.add(tabs, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);

        payrollTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && payrollTable.getSelectedRow() >= 0) {
                loadSelectedDetail();
            }
        });
        salaryStandardTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && salaryStandardTable.getSelectedRow() >= 0) {
                fillSelectedSalaryStandard();
            }
        });
        bonusPolicyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && bonusPolicyTable.getSelectedRow() >= 0) {
                fillSelectedBonusPolicy();
            }
        });
    }

    @Override
    public void refresh() {
        loadMonthPayrolls();
    }

    private JPanel payrollManagementTab() {
        JPanel filter = UiKit.form();
        UiKit.addField(filter, 0, "급여월", searchMonthField);
        UiKit.addField(filter, 1, "상태", searchStatusCombo);
        UiKit.addField(filter, 2, "부서ID", searchDeptField);
        UiKit.addField(filter, 3, "직급ID", searchPositionField);
        UiKit.addField(filter, 4, "사번", searchEmpField);

        JButton search = UiKit.primaryButton("검색");
        search.addActionListener(e -> guard(this::searchPayrolls));
        JButton monthList = UiKit.secondaryButton("월 전체 조회");
        monthList.addActionListener(e -> guard(this::loadMonthPayrolls));
        JButton create = UiKit.primaryButton("월 급여 생성");
        create.addActionListener(e -> guard(this::createMonthlyPayroll));
        JButton confirmMonth = UiKit.secondaryButton("월 급여 확정");
        confirmMonth.addActionListener(e -> guard(this::confirmMonth));
        JButton payMonth = UiKit.secondaryButton("월 급여 지급");
        payMonth.addActionListener(e -> guard(this::payMonth));
        JButton confirmOne = UiKit.secondaryButton("선택 확정");
        confirmOne.addActionListener(e -> guard(this::confirmSelected));
        JButton delete = UiKit.dangerButton("선택 삭제");
        delete.addActionListener(e -> guard(this::deleteSelected));

        JPanel top = UiKit.section("급여 검색/처리", filter);
        top.add(UiKit.actions(search, monthList, create, confirmMonth, payMonth, confirmOne, delete), BorderLayout.SOUTH);

        JScrollPane payrollScroll = UiKit.scroll(payrollTable);
        payrollScroll.setPreferredSize(new Dimension(520, 520));

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.add(top, BorderLayout.NORTH);
        left.add(payrollScroll, BorderLayout.CENTER);

        JScrollPane detailScroll = UiKit.scroll(statementPanel);
        detailScroll.setPreferredSize(new Dimension(560, 520));

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                UiKit.section("월별 직원 급여 목록", left),
                UiKit.section("급여 명세서 수정", detailScroll)
        );
        split.setResizeWeight(0.44);
        split.setBorder(null);
        split.setOpaque(false);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(split, BorderLayout.CENTER);
        return body;
    }

    private JPanel salaryPolicyTab() {
        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "직급ID", salaryPositionField);
        UiKit.addField(form, 1, "호봉", salaryPayGradeField);

        JButton load = UiKit.primaryButton("급여 기준 조회");
        load.addActionListener(e -> guard(this::loadSalaryStandard));
        JButton edit = UiKit.secondaryButton("선택 수정");
        edit.addActionListener(e -> guard(this::showSalaryStandardEditPopup));

        JPanel top = UiKit.section("기본급 정책 조회", form);
        top.add(UiKit.actions(load, edit), BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(UiKit.scroll(salaryStandardTable), BorderLayout.CENTER);
        return body;
    }

    private JPanel bonusPolicyTab() {
        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "평가연도", policyYearField);
        UiKit.addField(form, 1, "분기", policyQuarterField);

        JButton load = UiKit.primaryButton("성과급 정책 조회");
        load.addActionListener(e -> guard(this::loadBonusPolicies));
        JButton edit = UiKit.secondaryButton("선택 수정");
        edit.addActionListener(e -> guard(this::showBonusPolicyEditPopup));

        JPanel top = UiKit.section("성과급 정책 조회", form);
        top.add(UiKit.actions(load, edit), BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(UiKit.scroll(bonusPolicyTable), BorderLayout.CENTER);
        return body;
    }

    private void searchPayrolls() {
        Async.run(this, () -> payrollService.searchPayrollList(
                UiKit.parseYearMonth(searchMonthField.getText()),
                selectedStatus(),
                UiKit.parseLong(searchDeptField.getText()),
                UiKit.parseLong(searchPositionField.getText()),
                UiKit.parseLong(searchEmpField.getText())
        ), rows -> renderPayrolls(rows, "급여 검색: " + UiKit.safeSize(rows) + "건"));
    }

    private void loadMonthPayrolls() {
        Async.run(this, () -> payrollService.getPayrollList(requiredYearMonth(searchMonthField, "급여월")), rows -> {
            renderPayrolls(rows, "월 급여: " + UiKit.safeSize(rows) + "건");
            if (UiKit.safeList(rows).isEmpty()) {
                renderDetail(null);
            }
        });
    }

    private void renderPayrolls(List<PayrollDTO> rows, String message) {
        Long targetPayrollId = selectedPayrollId;
        UiKit.setRows(payrollTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getPayrollId(),
                        row.getEmployeeId(),
                        row.getPayrollYearMonth(),
                        UiKit.formatMoney(row.getTotalEarnings()),
                        UiKit.formatMoney(row.getTotalDeductions()),
                        UiKit.formatMoney(row.getNetPay()),
                        row.getStatus(),
                        row.getConfirmedAt(),
                        row.getPaidAt()
                })
                .collect(Collectors.toList()));

        statusLabel.setText(message);
        if (!selectPayroll(targetPayrollId) && payrollTable.getRowCount() > 0) {
            payrollTable.setRowSelectionInterval(0, 0);
        }
    }

    private boolean selectPayroll(Long payrollId) {
        if (payrollId == null) {
            return false;
        }
        for (int i = 0; i < payrollTable.getRowCount(); i++) {
            Object value = payrollTable.getValueAt(i, 0);
            if (payrollId.equals(UiKit.toLong(value))) {
                payrollTable.setRowSelectionInterval(i, i);
                return true;
            }
        }
        return false;
    }

    private void createMonthlyPayroll() {
        Async.runVoid(this, () -> payrollService.createMonthlyPayroll(requiredYearMonth(searchMonthField, "급여월")), () -> {
            statusLabel.setText("월 급여 생성 요청이 완료되었습니다.");
            loadMonthPayrolls();
        });
    }

    private void confirmMonth() {
        Async.run(this, () -> payrollService.confirmPayroll(requiredYearMonth(searchMonthField, "급여월")), result -> {
            statusLabel.setText(result ? "월 급여가 확정되었습니다." : "확정할 월 급여가 없습니다.");
            loadMonthPayrolls();
        });
    }

    private void payMonth() {
        Async.run(this, () -> payrollService.payPayroll(requiredYearMonth(searchMonthField, "급여월")), result -> {
            statusLabel.setText(result ? "월 급여 지급 처리가 완료되었습니다." : "지급 처리 결과가 없습니다.");
            loadMonthPayrolls();
        });
    }

    private void confirmSelected() {
        Long payrollId = UiKit.selectedLong(payrollTable, 0);
        Async.run(this, () -> payrollService.confirmPayroll(payrollId), result -> {
            statusLabel.setText(result ? "선택 급여가 확정되었습니다." : "확정할 급여가 없습니다.");
            loadMonthPayrolls();
        });
    }

    private void deleteSelected() {
        Long payrollId = UiKit.selectedLong(payrollTable, 0);
        Async.run(this, () -> payrollService.deletePayroll(payrollId), result -> {
            statusLabel.setText(result ? "선택 급여가 삭제되었습니다." : "삭제할 급여가 없습니다.");
            selectedPayrollId = null;
            loadMonthPayrolls();
        });
    }

    private void loadSelectedDetail() {
        if (payrollTable.getSelectedRow() < 0) {
            return;
        }
        Long payrollId = UiKit.selectedLong(payrollTable, 0);
        Long employeeId = UiKit.toLong(UiKit.selectedValue(payrollTable, 1));
        YearMonth yearMonth = (YearMonth) UiKit.selectedValue(payrollTable, 2);
        selectedPayrollId = payrollId;
        loadDetail(payrollId, employeeId, yearMonth);
    }

    private void loadDetail(Long payrollId, Long employeeId, YearMonth yearMonth) {
        Async.run(this, () -> new DetailData(
                payrollService.getPayrollDetail(payrollId),
                earningDAO.findEarningByPayrollId(payrollId),
                deductionDAO.findDeductionByPayrollId(payrollId),
                payrollService.getAdditionalAllowanceList(employeeId, yearMonth)
        ), data -> {
            renderDetail(data);
            statusLabel.setText("급여 상세를 불러왔습니다: " + payrollId);
        });
    }

    private void renderDetail(DetailData data) {
        currentDetail = data == null ? null : data.detail;
        currentEarning = data == null ? null : data.earning;
        currentDeduction = data == null ? null : data.deduction;
        currentAllowances = data == null || data.allowances == null ? Collections.emptyList() : data.allowances;

        statementPanel.removeAll();
        if (currentDetail == null) {
            statementPanel.add(emptyStatement(), BorderLayout.CENTER);
        } else {
            statementPanel.add(statement(currentDetail), BorderLayout.NORTH);
        }
        statementPanel.revalidate();
        statementPanel.repaint();
    }

    private JPanel emptyStatement() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel label = new JLabel("왼쪽 급여 목록에서 직원을 선택하세요.");
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
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
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
        JPanel grid = new JPanel(new GridLayout(1, 2, 14, 0));
        grid.setOpaque(false);
        grid.add(lineItems("지급 항목", new AmountLine[] {
                new AmountLine("기본급", detail.getBaseSalary(), () -> editEarningAmount("기본급", detail.getBaseSalary(), EarningDTO::setBaseSalary)),
                new AmountLine("연장수당", detail.getOvertimePay(), () -> editEarningAmount("연장수당", detail.getOvertimePay(), EarningDTO::setOvertimePay)),
                new AmountLine("교통비", detail.getTransportationAllowance(), () -> editEarningAmount("교통비", detail.getTransportationAllowance(), EarningDTO::setTransportationAllowance)),
                new AmountLine("성과급", detail.getPerformanceBonus(), () -> editEarningAmount("성과급", detail.getPerformanceBonus(), EarningDTO::setPerformanceBonus)),
                new AmountLine("기타수당", detail.getAdditionalAllowance(), this::showAllowanceEditor, "관리")
        }, "총 지급", detail.getTotalEarnings(), earningFormula(detail)));
        grid.add(lineItems("공제 항목", new AmountLine[] {
                new AmountLine("국민연금", detail.getNationalPension(), () -> editDeductionAmount("국민연금", detail.getNationalPension(), DeductionDTO::setNationalPension)),
                new AmountLine("건강보험", detail.getHealthInsurance(), () -> editDeductionAmount("건강보험", detail.getHealthInsurance(), DeductionDTO::setHealthInsurance)),
                new AmountLine("장기요양보험", detail.getLongTermCareInsurance(), () -> editDeductionAmount("장기요양보험", detail.getLongTermCareInsurance(), DeductionDTO::setLongTermCareInsurance)),
                new AmountLine("고용보험", detail.getEmploymentInsurance(), () -> editDeductionAmount("고용보험", detail.getEmploymentInsurance(), DeductionDTO::setEmploymentInsurance)),
                new AmountLine("소득세", detail.getIncomeTax(), () -> editDeductionAmount("소득세", detail.getIncomeTax(), DeductionDTO::setIncomeTax)),
                new AmountLine("지방소득세", detail.getLocalIncomeTax(), () -> editDeductionAmount("지방소득세", detail.getLocalIncomeTax(), DeductionDTO::setLocalIncomeTax))
        }, "총 공제", detail.getTotalDeductions(), deductionFormula(detail)));
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

    private JPanel lineItems(String title, AmountLine[] rows, String totalLabel, BigDecimal totalValue, String formulaDescription) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiKit.LINE),
                new EmptyBorder(14, 16, 14, 16)
        ));

        panel.add(lineItemHeader(title, formulaDescription));
        panel.add(Box.createVerticalStrut(10));

        for (AmountLine row : rows) {
            panel.add(amountRow(row.label, UiKit.formatMoney(row.value), false, row.action, row.actionText));
        }

        panel.add(Box.createVerticalStrut(8));
        panel.add(separator());
        panel.add(Box.createVerticalStrut(8));
        panel.add(amountRow(totalLabel, UiKit.formatMoney(totalValue), true, null, null));
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

        header.add(titleLabel, BorderLayout.WEST);
        header.add(formulaHelpButton(title, formulaDescription), BorderLayout.EAST);
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

    private JPanel amountRow(String label, String value, boolean total, Runnable action, String actionText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, total ? 30 : 34));

        JLabel labelView = new JLabel(label);
        labelView.setForeground(total ? UiKit.TEXT : UiKit.MUTED);
        labelView.setFont(new Font(Font.SANS_SERIF, total ? Font.BOLD : Font.PLAIN, 13));

        JLabel valueView = new JLabel(value);
        valueView.setHorizontalAlignment(SwingConstants.RIGHT);
        valueView.setForeground(UiKit.TEXT);
        valueView.setFont(new Font(Font.SANS_SERIF, total ? Font.BOLD : Font.PLAIN, 13));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(valueView);
        if (action != null) {
            JButton edit = smallButton(actionText == null ? "수정" : actionText);
            edit.addActionListener(e -> guard(action));
            right.add(edit);
        }

        row.add(labelView, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private Component separator() {
        JPanel line = new JPanel(new BorderLayout());
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(1, 1));
        line.setBackground(UiKit.LINE);
        return line;
    }

    private void editEarningAmount(String title, BigDecimal current, BiConsumer<EarningDTO, BigDecimal> updater) {
        BigDecimal value = promptMoney(title + " 수정", current);
        if (value == null) {
            return;
        }
        EarningDTO dto = earningSnapshot();
        updater.accept(dto, value);
        Async.run(this, () -> payrollService.updateEarning(dto), result -> {
            statusLabel.setText(result ? title + "이(가) 수정되었습니다." : "수정할 지급 항목이 없습니다.");
            loadMonthPayrolls();
        });
    }

    private void editDeductionAmount(String title, BigDecimal current, BiConsumer<DeductionDTO, BigDecimal> updater) {
        BigDecimal value = promptMoney(title + " 수정", current);
        if (value == null) {
            return;
        }
        DeductionDTO dto = deductionSnapshot();
        updater.accept(dto, value);
        Async.run(this, () -> payrollService.updateDeduction(dto), result -> {
            statusLabel.setText(result ? title + "이(가) 수정되었습니다." : "수정할 공제 항목이 없습니다.");
            loadMonthPayrolls();
        });
    }

    private BigDecimal promptMoney(String title, BigDecimal current) {
        JTextField amount = UiKit.field(12);
        amount.setText(current == null ? "" : UiKit.value(current));

        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "금액", amount);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    title,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                return requiredMoney(amount, "금액");
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "입력 확인", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private EarningDTO earningSnapshot() {
        if (currentDetail == null) {
            throw new IllegalStateException("먼저 급여 명세서를 선택하세요.");
        }
        if (currentEarning == null) {
            throw new IllegalStateException("수정할 지급 항목 ID를 찾을 수 없습니다.");
        }
        EarningDTO dto = new EarningDTO();
        dto.setEarningId(currentEarning.getEarningId());
        dto.setPayrollId(currentEarning.getPayrollId());
        dto.setBaseSalary(currentEarning.getBaseSalary());
        dto.setOvertimePay(currentEarning.getOvertimePay());
        dto.setTransportationAllowance(currentEarning.getTransportationAllowance());
        dto.setPerformanceBonus(currentEarning.getPerformanceBonus());
        dto.setAdditionalAllowance(currentEarning.getAdditionalAllowance());
        return dto;
    }

    private DeductionDTO deductionSnapshot() {
        if (currentDetail == null) {
            throw new IllegalStateException("먼저 급여 명세서를 선택하세요.");
        }
        if (currentDeduction == null) {
            throw new IllegalStateException("수정할 공제 항목 ID를 찾을 수 없습니다.");
        }
        DeductionDTO dto = new DeductionDTO();
        dto.setDeductionId(currentDeduction.getDeductionId());
        dto.setPayrollId(currentDeduction.getPayrollId());
        dto.setNationalPension(currentDeduction.getNationalPension());
        dto.setHealthInsurance(currentDeduction.getHealthInsurance());
        dto.setLongTermCareInsurance(currentDeduction.getLongTermCareInsurance());
        dto.setEmploymentInsurance(currentDeduction.getEmploymentInsurance());
        dto.setIncomeTax(currentDeduction.getIncomeTax());
        dto.setLocalIncomeTax(currentDeduction.getLocalIncomeTax());
        return dto;
    }

    private void showAllowanceEditor() {
        if (currentDetail == null) {
            throw new IllegalStateException("먼저 급여 명세서를 선택하세요.");
        }

        JTable table = UiKit.table("수당ID", "수당명", "적용월", "금액");
        JTextField allowanceId = UiKit.field(8);
        JTextField allowanceName = UiKit.field(16);
        JTextField allowanceAmount = UiKit.field(10);
        allowanceId.setEditable(false);

        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "수당ID", allowanceId);
        UiKit.addField(form, 1, "수당명", allowanceName);
        UiKit.addField(form, 2, "금액", allowanceAmount);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                allowanceId.setText(UiKit.value(UiKit.selectedValue(table, 0)));
                allowanceName.setText(UiKit.value(UiKit.selectedValue(table, 1)));
                allowanceAmount.setText(UiKit.value(UiKit.selectedValue(table, 3)));
            }
        });

        Runnable reload = () -> reloadAllowanceRows(table);
        JButton add = UiKit.primaryButton("추가");
        add.addActionListener(e -> runDialogAction(() -> {
            payrollService.addAdditionalAllowance(readDialogAllowance(null, allowanceName, allowanceAmount));
            reload.run();
            loadMonthPayrolls();
        }));
        JButton update = UiKit.secondaryButton("수정");
        update.addActionListener(e -> runDialogAction(() -> {
            payrollService.updateAdditionalAllowance(readDialogAllowance(requiredLong(allowanceId, "수당ID"), allowanceName, allowanceAmount));
            reload.run();
            loadMonthPayrolls();
        }));
        JButton delete = UiKit.dangerButton("삭제");
        delete.addActionListener(e -> runDialogAction(() -> {
            payrollService.deleteAdditionalAllowance(requiredLong(allowanceId, "수당ID"));
            allowanceId.setText("");
            allowanceName.setText("");
            allowanceAmount.setText("");
            reload.run();
            loadMonthPayrolls();
        }));

        JScrollPane scroll = UiKit.scroll(table);
        scroll.setPreferredSize(new Dimension(520, 190));

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBorder(new EmptyBorder(14, 14, 14, 14));
        body.setBackground(UiKit.BG);
        body.add(scroll, BorderLayout.CENTER);
        body.add(UiKit.section("기타수당 수정", form), BorderLayout.SOUTH);
        body.add(UiKit.actions(add, update, delete), BorderLayout.NORTH);

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, currentDetail.getPayrollYearMonth() + " 기타수당 관리", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(body);
        dialog.setSize(620, 480);
        dialog.setLocationRelativeTo(this);

        reload.run();
        dialog.setVisible(true);
    }

    private void reloadAllowanceRows(JTable table) {
        if (currentDetail == null) {
            return;
        }
        currentAllowances = payrollService.getAdditionalAllowanceList(
                currentDetail.getEmployeeId(),
                currentDetail.getPayrollYearMonth()
        );
        UiKit.setRows(table, UiKit.safeList(currentAllowances).stream()
                .map(row -> new Object[] {
                        row.getAdditionalAllowanceId(),
                        row.getAdditionalAllowanceName(),
                        row.getAdditionalAllowanceYearMonth(),
                        row.getAmount()
                })
                .collect(Collectors.toList()));
    }

    private AdditionalAllowanceDTO readDialogAllowance(Long allowanceId, JTextField nameField, JTextField amountField) {
        AdditionalAllowanceDTO dto = new AdditionalAllowanceDTO();
        dto.setAdditionalAllowanceId(allowanceId);
        dto.setEmployeeId(currentDetail.getEmployeeId());
        dto.setAdditionalAllowanceYearMonth(currentDetail.getPayrollYearMonth());
        dto.setAdditionalAllowanceName(requiredText(nameField, "수당명"));
        dto.setAmount(requiredMoney(amountField, "금액"));
        return dto;
    }

    private void loadSalaryStandard() {
        Long positionId = requiredLong(salaryPositionField, "직급ID");
        Integer payGrade = requiredInt(salaryPayGradeField, "호봉");
        Async.run(this, () -> salaryStandardService.getSalaryStandard(positionId, payGrade), row -> {
            renderSalaryStandards(row == null ? Collections.emptyList() : Collections.singletonList(row));
            statusLabel.setText(row == null ? "급여 기준을 찾을 수 없습니다." : "급여 기준을 불러왔습니다.");
            if (salaryStandardTable.getRowCount() > 0) {
                salaryStandardTable.setRowSelectionInterval(0, 0);
            }
        });
    }

    private void showSalaryStandardEditPopup() {
        SalaryStandardDTO selected = selectedSalaryStandard();

        JTextField baseSalary = UiKit.field(12);
        JTextField hourlyRate = UiKit.field(12);
        baseSalary.setText(UiKit.value(selected.getBaseSalary()));
        hourlyRate.setText(UiKit.value(selected.getRegularHourlyRate()));

        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "직급ID", readonlyField(selected.getPositionId()));
        UiKit.addField(form, 1, "호봉", readonlyField(selected.getPayGrade()));
        UiKit.addField(form, 2, "기본급", baseSalary);
        UiKit.addField(form, 3, "통상시급", hourlyRate);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "기본급 정책 수정",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        selected.setBaseSalary(requiredMoney(baseSalary, "기본급"));
        selected.setRegularHourlyRate(requiredMoney(hourlyRate, "통상시급"));
        Async.run(this, () -> salaryStandardService.updateSalaryStandard(selected), updated -> {
            statusLabel.setText(updated ? "급여 기준이 수정되었습니다." : "수정할 급여 기준이 없습니다.");
            loadSalaryStandard();
        });
    }

    private SalaryStandardDTO selectedSalaryStandard() {
        SalaryStandardDTO dto = new SalaryStandardDTO();
        dto.setSalaryStandardId(selectedLong(salaryStandardTable, 0));
        dto.setPositionId(selectedLong(salaryStandardTable, 1));
        dto.setPayGrade(selectedInt(salaryStandardTable, 2));
        dto.setBaseSalary(toMoney(UiKit.selectedValue(salaryStandardTable, 3)));
        dto.setRegularHourlyRate(toMoney(UiKit.selectedValue(salaryStandardTable, 4)));
        return dto;
    }

    private void fillSelectedSalaryStandard() {
        salaryPositionField.setText(UiKit.value(UiKit.selectedValue(salaryStandardTable, 1)));
        salaryPayGradeField.setText(UiKit.value(UiKit.selectedValue(salaryStandardTable, 2)));
    }

    private void renderSalaryStandards(List<SalaryStandardDTO> rows) {
        UiKit.setRows(salaryStandardTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getSalaryStandardId(),
                        row.getPositionId(),
                        row.getPayGrade(),
                        row.getBaseSalary(),
                        row.getRegularHourlyRate()
                })
                .collect(Collectors.toList()));
    }

    private void loadBonusPolicies() {
        Integer evalYear = requiredInt(policyYearField, "평가연도");
        Integer evalQuarter = requiredInt(policyQuarterField, "분기");
        Async.run(this, () -> bonusPolicyService.getPerformanceBonusPolicyList(evalYear, evalQuarter), rows -> {
            renderBonusPolicies(rows);
            statusLabel.setText("성과급 정책: " + UiKit.safeSize(rows) + "건");
            if (bonusPolicyTable.getRowCount() > 0) {
                bonusPolicyTable.setRowSelectionInterval(0, 0);
            }
        });
    }

    private void showBonusPolicyEditPopup() {
        PerformanceBonusPolicyDTO selected = selectedBonusPolicy();

        JTextField bonusRate = UiKit.field(10);
        JTextField fixedAmount = UiKit.field(12);
        bonusRate.setText(UiKit.value(selected.getBonusRate()));
        fixedAmount.setText(UiKit.value(selected.getFixedAmount()));

        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "평가연도", readonlyField(selected.getEvalYear()));
        UiKit.addField(form, 1, "분기", readonlyField(selected.getEvalQuarter()));
        UiKit.addField(form, 2, "등급", readonlyField(selected.getGrade()));
        UiKit.addField(form, 3, "성과급률", bonusRate);
        UiKit.addField(form, 4, "고정금액", fixedAmount);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "성과급 정책 수정",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        selected.setBonusRate(requiredMoney(bonusRate, "성과급률"));
        selected.setFixedAmount(requiredMoney(fixedAmount, "고정금액"));
        Async.run(this, () -> bonusPolicyService.updatePerformanceBonusPolicy(selected), updated -> {
            statusLabel.setText(updated ? "성과급 정책이 수정되었습니다." : "수정할 성과급 정책이 없습니다.");
            loadBonusPolicies();
        });
    }

    private PerformanceBonusPolicyDTO selectedBonusPolicy() {
        PerformanceBonusPolicyDTO dto = new PerformanceBonusPolicyDTO();
        dto.setPerformanceBonusPolicyId(selectedLong(bonusPolicyTable, 0));
        dto.setEvalYear(selectedInt(bonusPolicyTable, 1));
        dto.setEvalQuarter(selectedInt(bonusPolicyTable, 2));
        dto.setGrade(UiKit.value(UiKit.selectedValue(bonusPolicyTable, 3)));
        dto.setBonusRate(toMoney(UiKit.selectedValue(bonusPolicyTable, 4)));
        dto.setFixedAmount(toMoney(UiKit.selectedValue(bonusPolicyTable, 5)));
        return dto;
    }

    private void renderBonusPolicies(List<PerformanceBonusPolicyDTO> rows) {
        UiKit.setRows(bonusPolicyTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getPerformanceBonusPolicyId(),
                        row.getEvalYear(),
                        row.getEvalQuarter(),
                        row.getGrade(),
                        row.getBonusRate(),
                        row.getFixedAmount()
                })
                .collect(Collectors.toList()));
    }

    private void fillSelectedBonusPolicy() {
        policyYearField.setText(UiKit.value(UiKit.selectedValue(bonusPolicyTable, 1)));
        policyQuarterField.setText(UiKit.value(UiKit.selectedValue(bonusPolicyTable, 2)));
    }

    private JTextField readonlyField(Object value) {
        JTextField field = UiKit.field(10);
        field.setText(UiKit.value(value));
        field.setEditable(false);
        field.setBackground(new Color(248, 250, 252));
        return field;
    }

    private JButton smallButton(String text) {
        JButton button = UiKit.secondaryButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(197, 208, 224)),
                new EmptyBorder(4, 9, 4, 9)
        ));
        return button;
    }

    private CommonStatus selectedStatus() {
        String selected = (String) searchStatusCombo.getSelectedItem();
        if (selected == null || selected.trim().isEmpty()) {
            return null;
        }
        return CommonStatus.valueOf(selected);
    }

    private static String[] statusValues() {
        CommonStatus[] statuses = CommonStatus.values();
        String[] values = new String[statuses.length + 1];
        values[0] = "";
        for (int i = 0; i < statuses.length; i++) {
            values[i + 1] = statuses[i].name();
        }
        return values;
    }

    private void guard(Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            ErrorReporter.report(this, t);
        }
    }

    private void runDialogAction(Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            ErrorReporter.report(this, t);
        }
    }

    private Long requiredLong(JTextField field, String label) {
        Long value = UiKit.parseLong(field.getText());
        if (value == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요.");
        }
        return value;
    }

    private Integer requiredInt(JTextField field, String label) {
        Integer value = UiKit.parseInteger(field.getText());
        if (value == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요.");
        }
        return value;
    }

    private YearMonth requiredYearMonth(JTextField field, String label) {
        YearMonth value = UiKit.parseYearMonth(field.getText());
        if (value == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요.");
        }
        return value;
    }

    private String requiredText(JTextField field, String label) {
        String value = UiKit.text(field);
        if (value == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요.");
        }
        return value;
    }

    private BigDecimal requiredMoney(JTextField field, String label) {
        if (UiKit.text(field) == null) {
            throw new IllegalArgumentException(label + "을(를) 입력하세요.");
        }
        return UiKit.parseMoney(field.getText());
    }

    private BigDecimal toMoney(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return UiKit.parseMoney(UiKit.value(value));
    }

    private Long selectedLong(JTable table, int column) {
        return UiKit.toLong(UiKit.selectedValue(table, column));
    }

    private Integer selectedInt(JTable table, int column) {
        Object value = UiKit.selectedValue(table, column);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return UiKit.parseInteger(UiKit.value(value));
    }

    private static class DetailData {
        private final PayrollDetailDTO detail;
        private final EarningDTO earning;
        private final DeductionDTO deduction;
        private final List<AdditionalAllowanceDTO> allowances;

        private DetailData(PayrollDetailDTO detail, EarningDTO earning, DeductionDTO deduction, List<AdditionalAllowanceDTO> allowances) {
            this.detail = detail;
            this.earning = earning;
            this.deduction = deduction;
            this.allowances = allowances;
        }
    }

    private static class AmountLine {
        private final String label;
        private final BigDecimal value;
        private final Runnable action;
        private final String actionText;

        private AmountLine(String label, BigDecimal value, Runnable action) {
            this(label, value, action, "수정");
        }

        private AmountLine(String label, BigDecimal value, Runnable action, String actionText) {
            this.label = label;
            this.value = value;
            this.action = action;
            this.actionText = actionText;
        }
    }
}
