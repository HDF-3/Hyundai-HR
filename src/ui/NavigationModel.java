package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NavigationModel {
    private NavigationModel() {
    }

    public static List<Item> itemsFor(boolean admin) {
        List<Item> items = new ArrayList<>();
        items.add(new Item("profile", "내 정보"));
        items.add(new Item("attendance", "근태"));
        items.add(new Item("leave", "휴가"));
        items.add(new Item("payroll", "급여"));

        if (admin) {
            items.add(new Item("employees", "인사 관리"));
            items.add(new Item("departments", "부서 관리"));
            items.add(new Item("attendanceApproval", "근태 결재"));
            items.add(new Item("leaveApproval", "휴가 결재"));
            items.add(new Item("payrollAdmin", "급여 관리"));
        }

        return Collections.unmodifiableList(items);
    }

    public static final class Item {
        private final String route;
        private final String label;

        public Item(String route, String label) {
            this.route = route;
            this.label = label;
        }

        public String getRoute() {
            return route;
        }

        public String getLabel() {
            return label;
        }
    }
}
