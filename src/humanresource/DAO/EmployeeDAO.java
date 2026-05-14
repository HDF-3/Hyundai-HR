package humanresource.DAO;


import global.types.DBType;
import global.utils.ConnectionHelper;
import humanresource.DTO.EmployeeDTO;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    //TODO
    // insertEmployee(EmployeeDTO): Int     [O]
    // updateEmployee(EmployeeDTO) : int
    // selectEmployeeById(int) : EmployeeId     [O]
    // selectAllEmployees() : List<EmployeeDTO>    [O]

    public int insertEmployee(EmployeeDTO employeeDTO){
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "INSERT INTO EMPLOYEE(EMP_ID, DEPT_ID, POSITION_ID, ENAME) values(?,?, ?, ?)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, employeeDTO.getEmpId());
            pstmt.setLong(2, employeeDTO.getDeptId());
            pstmt.setLong(3, employeeDTO.getPositionId());
            pstmt.setString(4, employeeDTO.getEname());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return rowcount;
    }

  public int updateEmployee(EmployeeDTO employeeDTO) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            // 사번을 제외하고 변경 가능한 모든 항목을 업데이트 (입사일, 성별 등 절대 안 바뀌는 건 빼도 됨)
            String sql = "UPDATE EMPLOYEE SET DEPT_ID=?, POSITION_ID=?, STATUS_ID=?, ENAME=?, " +
                         "CONTACT=?, EMAIL=?, ADDRESS=?, SAL_ACCOUNT=?, PAY_GRADE=?, PASSWORD=? " +
                         "WHERE EMP_ID=?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, employeeDTO.getDeptId());
            pstmt.setLong(2, employeeDTO.getPositionId());
            pstmt.setString(3, employeeDTO.getStatusId().toString());
            pstmt.setString(4, employeeDTO.getEname());
            pstmt.setString(5, employeeDTO.getContact());
            pstmt.setString(6, employeeDTO.getEmail());
            pstmt.setString(7, employeeDTO.getAddress());
            pstmt.setString(8, employeeDTO.getSalAccount());
            pstmt.setInt(9, employeeDTO.getPayGrade());
            pstmt.setString(10, employeeDTO.getPassword());
            
            // WHERE 조건에 들어갈 사번
            pstmt.setLong(11, employeeDTO.getEmpId()); 

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return rowcount;
    }

    public EmployeeDTO selectEmployeeById(int empId){

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        EmployeeDTO emp = null;

        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT * FROM EMPLOYEE WHERE EMP_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, empId);
            rs = pstmt.executeQuery();

            if(rs.next()){
                emp = new EmployeeDTO();
                emp.setEmpId(rs.getLong("EMP_ID"));
                emp.setDeptId(rs.getLong("DEPT_ID"));
                emp.setPositionId(rs.getLong("POSITION_ID"));
            }


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return emp;
    }

    public List<EmployeeDTO> selectAllEmployees(){

        List<EmployeeDTO> empList = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT * FROM EMPLOYEE";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while(rs.next()){
                EmployeeDTO emp = new EmployeeDTO();
                emp.setEmpId(rs.getLong("EMP_ID"));
                emp.setDeptId(rs.getLong("DEPT_ID"));
                emp.setPositionId(rs.getLong("POSITION_ID"));
                emp.setEname(rs.getString("ENAME"));
                emp.setHireDate(rs.getDate("HIREDATE").toLocalDate());
                emp.setResignDate(rs.getDate("RESIGN_DATE").toLocalDate());
                emp.setContact(rs.getString("CONTACT"));
                emp.setGender(rs.getString("GENDER"));
                emp.setEmail(rs.getString("EMAIL"));
                emp.setAddress(rs.getString("ADDRESS"));
                emp.setSalAccount(rs.getString("SAL_ACCOUNT"));

                empList.add(emp);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return empList;
    }

}
