package leave.dao;

import global.types.DBType;
import global.utils.ConnectionHelper;
import leave.dto.AnnualLeaveDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/*
public List<Emp> getEmpList() {
        List<Emp> empList = new ArrayList<Emp>();
        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM emp");
                ResultSet rs = pstmt.executeQuery();
                ){
            while (rs.next()) {
                Emp emp = new Emp();
                emp.setEmpno(rs.getInt("empno"));
                emp.setEname(rs.getString("ename"));
                emp.setJob(rs.getString("job"));
                emp.setDeptno(rs.getInt("deptno"));
                empList.add(emp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return empList;
    }
    public Emp getEmpById(int empno) {
        Emp emp = new Emp();
        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM emp where empno=?");
                ){
            pstmt.setInt(1, empno);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                emp.setEmpno(rs.getInt("empno"));
                emp.setEname(rs.getString("ename"));
                emp.setJob(rs.getString("job"));
                emp.setDeptno(rs.getInt("deptno"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return emp;
    }
 */
public class LeaveDAO {
    public List<AnnualLeaveDTO> findAnnualLeaveListByEmployeeId(Long employeeId) {
        List<AnnualLeaveDTO> list = new ArrayList<>();
        String sql = "select GRANTED_ANNUAL_LEAVE, USED_ANNUAL_LEAVE, REMAINING_ANNUAL_LEAVE, IS_ACTIVE, GRANTED_AT, EXPIRED_AT " +
                "from ANNUAL_LEAVE " +
                "where EMP_ID = ?";
        try (
                Connection conn = ConnectionHelper.getConnection(DBType.ORACLE);
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setLong(1, employeeId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    AnnualLeaveDTO dto = new AnnualLeaveDTO();
                    dto.setGrantedAnnualLeave(rs.getDouble("GRANTED_ANNUAL_LEAVE"));
                    dto.setUsedAnnualLeave(rs.getDouble("USED_ANNUAL_LEAVE"));
                    dto.setRemainingAnnualLeave(rs.getDouble("REMAINING_ANNUAL_LEAVE"));
                    dto.setIsActive(rs.getString("IS_ACTIVE").charAt(0));
                    dto.setGrantedAt(rs.getDate("GRANTED_AT").toLocalDate());
                    dto.setExpiredAt(rs.getDate("EXPIRED_AT").toLocalDate());
                    list.add(dto);
                }
            }
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return list;
    }
}
