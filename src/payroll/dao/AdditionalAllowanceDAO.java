package payroll.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import global.types.DBType;
import global.utils.ConnectionHelper;
import payroll.dto.AdditionalAllowanceDTO;

public class AdditionalAllowanceDAO {

    private AdditionalAllowanceDTO mapAdditionalAllowance(ResultSet rs) throws Exception {
        AdditionalAllowanceDTO additionalAllowance = new AdditionalAllowanceDTO();

        additionalAllowance.setAdditionalAllowanceId(rs.getLong("additional_allowance_id"));
        additionalAllowance.setEmployeeId(rs.getLong("employee_id"));
        additionalAllowance.setAdditionalAllowanceName(rs.getString("additional_allowance_name"));
        additionalAllowance.setAdditionalAllowanceYearMonth(
            YearMonth.from(rs.getDate("allowance_year_month").toLocalDate())
        );
        additionalAllowance.setAmount(rs.getBigDecimal("amount"));

        return additionalAllowance;
    }

    public List<AdditionalAllowanceDTO> findAdditionalAllowanceList(Long employeeId, YearMonth yearMonth) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AdditionalAllowanceDTO> additionalAllowanceList = new ArrayList<AdditionalAllowanceDTO>();

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select additional_allowance_id, employee_id, additional_allowance_name, allowance_year_month, amount from additional_allowance where employee_id=? and allowance_year_month=? order by additional_allowance_id";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, employeeId);
            pstmt.setDate(2, Date.valueOf(yearMonth.atDay(1)));

            rs = pstmt.executeQuery();

            while (rs.next()) {
                additionalAllowanceList.add(mapAdditionalAllowance(rs));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return additionalAllowanceList;
    }

    public List<AdditionalAllowanceDTO> findAdditionalAllowanceList(
            Long employeeId,
            YearMonth yearMonth,
            Connection conn
    ) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AdditionalAllowanceDTO> additionalAllowanceList = new ArrayList<AdditionalAllowanceDTO>();

        try {
            String sql = "select additional_allowance_id, employee_id, additional_allowance_name, allowance_year_month, amount from additional_allowance where employee_id=? and allowance_year_month=? order by additional_allowance_id";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, employeeId);
            pstmt.setDate(2, Date.valueOf(yearMonth.atDay(1)));

            rs = pstmt.executeQuery();

            while (rs.next()) {
                additionalAllowanceList.add(mapAdditionalAllowance(rs));
            }

            return additionalAllowanceList;

        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
        }
    }

    public AdditionalAllowanceDTO findAdditionalAllowance(Long additionalAllowanceId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdditionalAllowanceDTO additionalAllowance = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "select additional_allowance_id, employee_id, additional_allowance_name, allowance_year_month, amount from additional_allowance where additional_allowance_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, additionalAllowanceId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                additionalAllowance = mapAdditionalAllowance(rs);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }

        return additionalAllowance;
    }

    public AdditionalAllowanceDTO findAdditionalAllowance(Long additionalAllowanceId, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AdditionalAllowanceDTO additionalAllowance = null;

        try {
            String sql = "select additional_allowance_id, employee_id, additional_allowance_name, allowance_year_month, amount from additional_allowance where additional_allowance_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, additionalAllowanceId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                additionalAllowance = mapAdditionalAllowance(rs);
            }

            return additionalAllowance;

        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
        }
    }

    public int insertAdditionalAllowance(AdditionalAllowanceDTO additionalAllowance, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;

        try {
            String sql = "insert into additional_allowance(additional_allowance_id, employee_id, additional_allowance_name, allowance_year_month, amount) values(seq_additional_allowance_id.nextval,?,?,?,?)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, additionalAllowance.getEmployeeId());
            pstmt.setString(2, additionalAllowance.getAdditionalAllowanceName());
            pstmt.setDate(3, Date.valueOf(additionalAllowance.getAdditionalAllowanceYearMonth().atDay(1)));
            pstmt.setBigDecimal(4, additionalAllowance.getAmount());

            return pstmt.executeUpdate();

        } finally {
            ConnectionHelper.close(pstmt);
        }
    }

    public int updateAdditionalAllowance(AdditionalAllowanceDTO additionalAllowance, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;

        try {
            String sql = "update additional_allowance set employee_id=?, additional_allowance_name=?, allowance_year_month=?, amount=? where additional_allowance_id=?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, additionalAllowance.getEmployeeId());
            pstmt.setString(2, additionalAllowance.getAdditionalAllowanceName());
            pstmt.setDate(3, Date.valueOf(additionalAllowance.getAdditionalAllowanceYearMonth().atDay(1)));
            pstmt.setBigDecimal(4, additionalAllowance.getAmount());
            pstmt.setLong(5, additionalAllowance.getAdditionalAllowanceId());

            return pstmt.executeUpdate();

        } finally {
            ConnectionHelper.close(pstmt);
        }
    }

    public int deleteAdditionalAllowance(Long additionalAllowanceId, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;

        try {
            String sql = "delete from additional_allowance where additional_allowance_id=?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, additionalAllowanceId);

            return pstmt.executeUpdate();

        } finally {
            ConnectionHelper.close(pstmt);
        }
    }

}
