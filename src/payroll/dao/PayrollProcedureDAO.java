package payroll.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.time.YearMonth;

import global.types.DBType;
import global.utils.ConnectionHelper;

public class PayrollProcedureDAO {

    public void callCreateMonthlyPayroll(YearMonth yearMonth) {
        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);

            String sql = "{ call CREATE_MONTHLY_PAYROLL(?) }";

            cstmt = conn.prepareCall(sql);
            cstmt.setDate(1, Date.valueOf(yearMonth.atDay(1)));

            cstmt.execute();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create monthly payroll.", e);
        } finally {
            ConnectionHelper.close(cstmt);
            ConnectionHelper.close(conn);
        }
    }
}
