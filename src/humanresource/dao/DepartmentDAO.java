package humanresource.dao;

import global.types.DBType;
import global.utils.ConnectionHelper;
import humanresource.dto.DepartmentDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {
    public int insertDepartment(DepartmentDTO departmentDTO){
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "INSERT INTO DEPARTMENT(DEPT_ID, DEPT_NAME, DEPT_DESC, MANAGER_ID, PARENT_DEPT_ID) VALUES(?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, departmentDTO.getDeptId());
            pstmt.setString(2, departmentDTO.getDeptName());

            if(departmentDTO.getDeptDesc() != null) {
                pstmt.setString(3, departmentDTO.getDeptDesc());
            } else {
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            }

            if(departmentDTO.getManagerId() != null && departmentDTO.getManagerId() > 0) {
                pstmt.setLong(4, departmentDTO.getManagerId());
            } else {
                pstmt.setNull(4, java.sql.Types.NUMERIC);
            }

            if(departmentDTO.getParentDeptId() != null && departmentDTO.getParentDeptId() > 0) {
                pstmt.setLong(5, departmentDTO.getParentDeptId());
            } else {
                pstmt.setNull(5, java.sql.Types.NUMERIC);
            }

            rowcount = pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return rowcount;
    }

    public int deleteDepartment(Long deptId){
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "DELETE FROM DEPARTMENT WHERE DEPT_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, deptId);

            rowcount = pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        }finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return rowcount;
    }

    public List<DepartmentDTO> selectAllDepartment(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<DepartmentDTO> deptList = new ArrayList<>();


        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT * FROM DEPARTMENT";

            pstmt = conn.prepareStatement(sql);

            rs = pstmt.executeQuery();

            while(rs.next()){
                DepartmentDTO dept = new DepartmentDTO();
                dept.setDeptId(rs.getLong("DEPT_ID"));
                dept.setDeptName(rs.getString("DEPT_NAME"));
                dept.setDeptDesc(rs.getString("DEPT_DESC"));
                
                long managerId = rs.getLong("MANAGER_ID");
                if (!rs.wasNull()) dept.setManagerId(managerId);
                
                long parentDeptId = rs.getLong("PARENT_DEPT_ID");
                if (!rs.wasNull()) dept.setParentDeptId(parentDeptId);

                deptList.add(dept);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        }finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(rs);
            ConnectionHelper.close(conn);
        }
        return deptList;
    }

    public int countEmployeesInDept(Long deptId){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT COUNT(*) FROM EMPLOYEE WHERE DEPT_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, deptId);
            rs = pstmt.executeQuery();

            if(rs.next()){
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        }finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(rs);
            ConnectionHelper.close(conn);
        }

        return count;

    }
}
