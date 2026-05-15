package ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

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
    private final PayrollService payrollService;

    private final JTextField monthField = UiKit.field(10);
    private final JTextField employeeFilterField = UiKit.field(10);
    private final DefaultTableModel payrollModel = UiKit.model("급여ID", "사번", "월", "총지급", "총공제", "실수령", "상태", "확정일", "지급일");
    private final JTable payrollTable = UiKit.table(payrollModel);

    private final JLabel detailLabel = new JLabel("급여를 선택하세요.");

    private final JTextField allowanceIdField = UiKit.field(10);
    private final JTextField allowanceEmpIdField = UiKit.field(10);
    private final JTextField allowanceNameField = UiKit.field(14);
    private final JTextField allowanceMonthField = UiKit.field(10);
    private final JTextField allowanceAmountField = UiKit.field(10);
    private final DefaultTableModel allowanceModel = UiKit.model("수당ID", "사번", "월", "수당명", "금액");
    private final JTable allowanceTable = UiKit.table(allowanceModel);
    private final JLabel status = UiKit.statusLabel();

    public PayrollPanel(AppSession session, PayrollService payrollService) {
        this.session = session;
        this.payrollService = payrollService;
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        JPanel root = UiKit.screen("급여", "월별 급여 생성, 조회, 확정, 지급, 추가수당을 관리합니다.");
        add(root, BorderLayout.CENTER);

        monthField.setText(YearMonth.now().minusMonths(1).toString());
        employeeFilterField.setText(session.isAdmin() ? "" : UiKit.display(session.getEmployeeId()));
        employeeFilterField.setEditable(session.isAdmin());
        employeeFilterField.setEnabled(session.isAdmin());
        allowanceEmpIdField.setText(UiKit.display(session.getEmployeeId()));
        allowanceEmpIdField.setEditable(session.isAdmin());
        allowanceEmpIdField.setEnabled(session.isAdmin());
        allowanceMonthField.setText(YearMonth.now().minusMonths(1).toString());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("급여 목록", payrollListTab());
        tabs.addTab("상세", detailTab());
        if (session.isAdmin()) {
            tabs.addTab("추가수당", allowanceTab());
        }
        root.add(tabs, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel payrollListTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel toolbarPanel = UiKit.surface();
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(new JLabel("급여월"));
        toolbar.add(monthField);
        if (session.isAdmin()) {
            toolbar.add(new JLabel("사번"));
            toolbar.add(employeeFilterField);
        }

        JButton refreshMonth = UiKit.button("월 조회");
        JButton refreshEmployee = UiKit.button("사번 조회");
        JButton create = UiKit.primaryButton("급여 생성");
        JButton confirm = UiKit.button("월 확정");
        JButton pay = UiKit.button("월 지급");
        JButton delete = UiKit.button("선택 삭제");
        JButton detail = UiKit.button("상세 보기");

        refreshMonth.addActionListener(e -> refreshPayrollByMonth());
        refreshEmployee.addActionListener(e -> refreshPayrollByEmployee());
        create.addActionListener(e -> createPayroll());
        confirm.addActionListener(e -> updateMonthStatus(true));
        pay.addActionListener(e -> updateMonthStatus(false));
        delete.addActionListener(e -> deleteSelectedPayroll());
        detail.addActionListener(e -> loadSelectedDetail());

        toolbar.add(refreshMonth);
        if (session.isAdmin()) {
            toolbar.add(refreshEmployee);
            toolbar.add(create);
            toolbar.add(confirm);
            toolbar.add(pay);
            toolbar.add(delete);
        }
        toolbar.add(detail);
        toolbarPanel.add(toolbar, BorderLayout.CENTER);

        JPanel tablePanel = UiKit.surface();
        tablePanel.add(UiKit.scroll(payrollTable), BorderLayout.CENTER);
        payrollTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedDetail();
            }
        });

        tab.add(toolbarPanel, BorderLayout.NORTH);
        tab.add(tablePanel, BorderLayout.CENTER);
        tab.add(UiKit.todo("TODO: 확정/지급 상태 변경 시 confirmed_at/pay_date 저장은 백엔드 #7 수정 필요"), BorderLayout.SOUTH);
        return tab;
    }

    private JPanel detailTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);
        JPanel detail = UiKit.surface();
        detailLabel.setVerticalAlignment(JLabel.TOP);
        detail.add(detailLabel, BorderLayout.CENTER);
        tab.add(detail, BorderLayout.CENTER);
        return tab;
    }

    private JPanel allowanceTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setOpaque(false);

        JPanel formPanel = UiKit.surface();
        formPanel.add(new JLabel("추가수당 등록"), BorderLayout.NORTH);
        JPanel form = UiKit.formPanel();
        int row = 0;
        UiKit.addField(form, row++, "수당ID(선택)", allowanceIdField);
        UiKit.addField(form, row++, "사번", allowanceEmpIdField);
        UiKit.addField(form, row++, "수당명", allowanceNameField);
        UiKit.addField(form, row++, "적용월", allowanceMonthField);
        UiKit.addField(form, row++, "금액", allowanceAmountField);
        formPanel.add(form, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton load = UiKit.button("수당 조회");
        JButton add = UiKit.primaryButton("등록");
        load.addActionListener(e -> refreshAllowances());
        add.addActionListener(e -> addAllowance());
        actions.add(load);
        actions.add(add);
        formPanel.add(actions, BorderLayout.SOUTH);

        JPanel tablePanel = UiKit.surface();
        tablePanel.add(UiKit.scroll(allowanceTable), BorderLayout.CENTER);

        JPanel split = new JPanel(new GridLayout(1, 2, 12, 0));
        split.setOpaque(false);
        split.add(formPanel);
        split.add(tablePanel);
        tab.add(split, BorderLayout.CENTER);
        return tab;
    }

    @Override
    public void refresh() {
        refreshPayrollByMonth();
        if (session.isAdmin()) {
            refreshAllowances();
        }
    }

    private void refreshPayrollByMonth() {
        try {
            YearMonth month = UiKit.yearMonthValue(monthField);
            if (session.isAdmin()) {
                Async.run(this, () -> payrollService.getPayrollList(month), this::fillPayrollRows, e -> UiKit.error(this, e));
            } else {
                Async.run(this, () -> payrollService.getPayrollList(session.getEmployeeId()), list -> fillPayrollRows(filterPayrollRows(list, month)), e -> UiKit.error(this, e));
            }
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void refreshPayrollByEmployee() {
        try {
            Long employeeId = session.isAdmin() ? UiKit.longValue(employeeFilterField) : session.getEmployeeId();
            if (employeeId == null) {
                throw new IllegalArgumentException("사번을 입력하세요.");
            }
            Async.run(this, () -> payrollService.getPayrollList(employeeId), this::fillPayrollRows, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void createPayroll() {
        if (!session.isAdmin()) {
            UiKit.info(this, "관리자만 처리할 수 있습니다.");
            return;
        }
        try {
            YearMonth month = UiKit.yearMonthValue(monthField);
            Async.run(this, () -> {
                payrollService.createMonthlyPayroll(month);
                return Boolean.TRUE;
            }, result -> {
                status.setText("급여 생성 완료");
                refreshPayrollByMonth();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void updateMonthStatus(boolean confirm) {
        if (!session.isAdmin()) {
            UiKit.info(this, "관리자만 처리할 수 있습니다.");
            return;
        }
        try {
            YearMonth month = UiKit.yearMonthValue(monthField);
            Async.run(this,
                    () -> confirm ? payrollService.confirmPayroll(month) : payrollService.payPayroll(month),
                    result -> {
                        status.setText((confirm ? "확정" : "지급") + (result ? " 완료" : " 실패"));
                        refreshPayrollByMonth();
                    },
                    e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void deleteSelectedPayroll() {
        if (!session.isAdmin()) {
            UiKit.info(this, "관리자만 처리할 수 있습니다.");
            return;
        }
        Long payrollId = selectedPayrollId();
        if (payrollId == null) {
            UiKit.info(this, "삭제할 급여를 선택하세요.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "선택한 급여를 삭제할까요?", "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        Async.run(this, () -> payrollService.deletePayroll(payrollId), result -> {
            status.setText(result ? "급여 삭제 완료" : "급여 삭제 실패");
            refreshPayrollByMonth();
        }, e -> UiKit.error(this, e));
    }

    private void loadSelectedDetail() {
        Long payrollId = selectedPayrollId();
        if (payrollId == null) {
            return;
        }
        Async.run(this, () -> payrollService.getPayrollDetail(payrollId), this::showDetail, e -> UiKit.error(this, e));
    }

    private void refreshAllowances() {
        if (!session.isAdmin()) {
            allowanceModel.setRowCount(0);
            return;
        }
        try {
            Long empId = UiKit.longValue(allowanceEmpIdField);
            YearMonth month = UiKit.yearMonthValue(allowanceMonthField);
            if (empId == null || month == null) {
                return;
            }
            Async.run(this, () -> payrollService.getAdditionalAllowanceList(empId, month), list -> {
                allowanceModel.setRowCount(0);
                for (AdditionalAllowanceDTO dto : safe(list)) {
                    allowanceModel.addRow(new Object[] {
                            dto.getAdditionalAllowanceId(),
                            dto.getEmployeeId(),
                            dto.getAdditionalAllowanceYearMonth(),
                            dto.getAdditionalAllowanceName(),
                            UiKit.money(dto.getAmount())
                    });
                }
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private void addAllowance() {
        if (!session.isAdmin()) {
            UiKit.info(this, "관리자만 처리할 수 있습니다.");
            return;
        }
        try {
            AdditionalAllowanceDTO dto = new AdditionalAllowanceDTO();
            Long allowanceId = UiKit.longValue(allowanceIdField);
            dto.setAdditionalAllowanceId(allowanceId == null ? 0L : allowanceId);
            dto.setEmployeeId(requiredLong(allowanceEmpIdField, "사번"));
            dto.setAdditionalAllowanceName(requiredText(allowanceNameField, "수당명"));
            dto.setAdditionalAllowanceYearMonth(UiKit.yearMonthValue(allowanceMonthField));
            BigDecimal amount = UiKit.decimalValue(allowanceAmountField);
            if (amount == null) {
                throw new IllegalArgumentException("금액은 필수입니다.");
            }
            dto.setAmount(amount);
            Async.run(this, () -> payrollService.addAdditionalAllowance(dto), result -> {
                status.setText(result ? "추가수당 등록 완료" : "추가수당 등록 실패");
                refreshAllowances();
                refreshPayrollByMonth();
            }, e -> UiKit.error(this, e));
        } catch (RuntimeException e) {
            UiKit.validation(this, e);
        }
    }

    private List<PayrollDTO> filterPayrollRows(List<PayrollDTO> list, YearMonth month) {
        if (month == null) {
            return list;
        }
        List<PayrollDTO> filtered = new java.util.ArrayList<PayrollDTO>();
        for (PayrollDTO dto : safe(list)) {
            if (month.equals(dto.getPayrollYearMonth())) {
                filtered.add(dto);
            }
        }
        return filtered;
    }

    private void fillPayrollRows(List<PayrollDTO> list) {
        payrollModel.setRowCount(0);
        for (PayrollDTO dto : safe(list)) {
            payrollModel.addRow(new Object[] {
                    dto.getPayrollId(),
                    dto.getEmployeeId(),
                    dto.getPayrollYearMonth(),
                    UiKit.money(dto.getTotalEarnings()),
                    UiKit.money(dto.getTotalDeductions()),
                    UiKit.money(dto.getNetPay()),
                    dto.getStatus(),
                    dto.getConfirmedAt(),
                    dto.getPayDate()
            });
        }
        status.setText("급여 " + payrollModel.getRowCount() + "건");
    }

    private void showDetail(PayrollDetailDTO detail) {
        if (detail == null) {
            detailLabel.setText("상세 정보가 없습니다.");
            return;
        }
        detailLabel.setText("<html>"
                + "<h2>급여 #" + detail.getPayrollId() + "</h2>"
                + "<table cellpadding='5'>"
                + row("사번", detail.getEmployeeId())
                + row("이름", detail.getEmployeeName())
                + row("급여월", detail.getPayrollYearMonth())
                + row("상태", detail.getStatus())
                + row("총지급", UiKit.money(detail.getTotalEarnings()))
                + row("총공제", UiKit.money(detail.getTotalDeductions()))
                + row("실수령", UiKit.money(detail.getNetPay()))
                + row("기본급", UiKit.money(detail.getBaseSalary()))
                + row("연장수당", UiKit.money(detail.getOvertimePay()))
                + row("교통수당", UiKit.money(detail.getTransportationAllowance()))
                + row("성과급", UiKit.money(detail.getPerformanceBonus()))
                + row("추가수당", UiKit.money(detail.getAdditionalAllowance()))
                + row("국민연금", UiKit.money(detail.getNationalPension()))
                + row("건강보험", UiKit.money(detail.getHealthInsurance()))
                + row("장기요양", UiKit.money(detail.getLongTermCareInsurance()))
                + row("고용보험", UiKit.money(detail.getEmploymentInsurance()))
                + row("소득세", UiKit.money(detail.getIncomeTax()))
                + row("지방소득세", UiKit.money(detail.getLocalIncomeTax()))
                + "</table></html>");
    }

    private String row(String label, Object value) {
        return "<tr><td style='color:#667085'>" + label + "</td><td>" + UiKit.display(value) + "</td></tr>";
    }

    private Long selectedPayrollId() {
        int viewRow = payrollTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = payrollTable.convertRowIndexToModel(viewRow);
        return Long.valueOf(String.valueOf(payrollModel.getValueAt(modelRow, 0)));
    }

    private static String requiredText(JTextField field, String label) {
        String value = UiKit.text(field);
        if (value == null) {
            throw new IllegalArgumentException(label + "은(는) 필수입니다.");
        }
        return value;
    }

    private static Long requiredLong(JTextField field, String label) {
        Long value = UiKit.longValue(field);
        if (value == null) {
            throw new IllegalArgumentException(label + "은(는) 필수입니다.");
        }
        return value;
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? java.util.Collections.<T>emptyList() : list;
    }
}
