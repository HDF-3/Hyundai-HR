package ui.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import humanresource.dto.DepartmentDTO;
import humanresource.service.DepartmentService;
import ui.Async;
import ui.Refreshable;
import ui.UiKit;

public class DepartmentsPanel extends JPanel implements Refreshable {
    private final DepartmentService departmentService = new DepartmentService();
    private final JTable departmentTable = UiKit.table("부서ID", "부서명", "설명", "관리자ID", "상위부서ID", "직원 수");
    private final JTree organizationTree = new JTree(new DefaultMutableTreeNode("Hyundai HR"));
    private final JLabel statusLabel = UiKit.statusLabel();

    private final JTextField deptIdField = UiKit.field(8);
    private final JTextField deptNameField = UiKit.field(16);
    private final JTextField deptDescField = UiKit.field(28);
    private final JTextField managerIdField = UiKit.field(8);
    private final JTextField parentDeptIdField = UiKit.field(8);

    public DepartmentsPanel() {
        setLayout(new BorderLayout());

        JPanel page = UiKit.page("부서 관리", "부서 등록, 삭제, 조직도 확인을 처리합니다.");

        JPanel form = UiKit.form();
        UiKit.addField(form, 0, "부서ID", deptIdField);
        UiKit.addField(form, 1, "부서명", deptNameField);
        UiKit.addField(form, 2, "설명", deptDescField);
        UiKit.addField(form, 3, "관리자ID", managerIdField);
        UiKit.addField(form, 4, "상위부서ID", parentDeptIdField);

        JButton refresh = UiKit.primaryButton("새로고침");
        refresh.addActionListener(e -> refresh());
        JButton register = UiKit.primaryButton("부서 등록");
        register.addActionListener(e -> registerDepartment());
        JButton remove = UiKit.dangerButton("선택 부서 삭제");
        remove.addActionListener(e -> removeDepartment());
        JButton count = UiKit.secondaryButton("선택 부서 인원 확인");
        count.addActionListener(e -> countEmployees());

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        left.add(UiKit.scroll(departmentTable), BorderLayout.CENTER);
        left.add(UiKit.actions(refresh, register, remove, count), BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(UiKit.section("부서 정보", form), BorderLayout.NORTH);
        right.add(UiKit.section("조직도", new JScrollPane(organizationTree)), BorderLayout.CENTER);

        JPanel body = new JPanel(new GridLayout(1, 2, 14, 0));
        body.setOpaque(false);
        body.add(left);
        body.add(right);

        page.add(body, BorderLayout.CENTER);
        page.add(statusLabel, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);

        departmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && departmentTable.getSelectedRow() >= 0) {
                fillSelected();
            }
        });
    }

    @Override
    public void refresh() {
        statusLabel.setText("부서 목록을 불러오는 중...");
        Async.run(this, departmentService::getAllDepartments, rows -> {
            renderRows(rows);
            renderTree(rows);
            statusLabel.setText("부서 목록: " + UiKit.safeSize(rows) + "건");
        });
    }

    private void renderRows(List<DepartmentDTO> rows) {
        UiKit.setRows(departmentTable, UiKit.safeList(rows).stream()
                .map(row -> new Object[] {
                        row.getDeptId(),
                        row.getDeptName(),
                        row.getDeptDesc(),
                        row.getManagerId(),
                        row.getParentDeptId(),
                        safeEmployeeCount(row.getDeptId())
                })
                .collect(Collectors.toList()));
    }

    private int safeEmployeeCount(Long deptId) {
        try {
            return departmentService.getEmployeeCount(deptId);
        } catch (Exception e) {
            return 0;
        }
    }

    private void renderTree(List<DepartmentDTO> rows) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Hyundai HR");
        Map<Long, DefaultMutableTreeNode> nodes = new HashMap<>();
        for (DepartmentDTO dept : UiKit.safeList(rows)) {
            nodes.put(dept.getDeptId(), new DefaultMutableTreeNode(dept.getDeptName() + " (" + dept.getDeptId() + ")"));
        }
        for (DepartmentDTO dept : UiKit.safeList(rows)) {
            DefaultMutableTreeNode node = nodes.get(dept.getDeptId());
            if (dept.getParentDeptId() == null || !nodes.containsKey(dept.getParentDeptId())) {
                root.add(node);
            } else {
                nodes.get(dept.getParentDeptId()).add(node);
            }
        }
        organizationTree.setModel(new DefaultTreeModel(root));
        for (int i = 0; i < organizationTree.getRowCount(); i++) {
            organizationTree.expandRow(i);
        }
    }

    private void fillSelected() {
        deptIdField.setText(UiKit.value(UiKit.selectedValue(departmentTable, 0)));
        deptNameField.setText(UiKit.value(UiKit.selectedValue(departmentTable, 1)));
        deptDescField.setText(UiKit.value(UiKit.selectedValue(departmentTable, 2)));
        managerIdField.setText(UiKit.value(UiKit.selectedValue(departmentTable, 3)));
        parentDeptIdField.setText(UiKit.value(UiKit.selectedValue(departmentTable, 4)));
    }

    private void registerDepartment() {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDeptId(requiredLong(deptIdField, "부서ID"));
        dto.setDeptName(requiredText(deptNameField, "부서명"));
        dto.setDeptDesc(UiKit.text(deptDescField));
        dto.setManagerId(UiKit.parseLong(managerIdField.getText()));
        dto.setParentDeptId(UiKit.parseLong(parentDeptIdField.getText()));

        Async.run(this, () -> departmentService.registerDepartment(dto), result -> {
            statusLabel.setText("부서 등록 완료: " + result + "건");
            refresh();
        });
    }

    private void removeDepartment() {
        Long deptId = UiKit.selectedLong(departmentTable, 0);
        Async.run(this, () -> departmentService.removeDepartment(deptId), result -> {
            statusLabel.setText("부서 삭제 완료: " + result + "건");
            refresh();
        });
    }

    private void countEmployees() {
        Long deptId = UiKit.selectedLong(departmentTable, 0);
        Async.run(this, () -> departmentService.getEmployeeCount(deptId), count -> {
            statusLabel.setText("선택 부서 직원 수: " + count + "명");
        });
    }

    private Long requiredLong(JTextField field, String label) {
        Long value = UiKit.parseLong(field.getText());
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
}
