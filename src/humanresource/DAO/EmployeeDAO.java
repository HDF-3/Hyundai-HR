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

//    public int insertEmployee(EmployeeDTO employeeDTO){
//        Connection conn = null;
//        PreparedStatement pstmt = null;
//        int rowcount = 0;
//
//        try {
//            conn = ConnectionHelper.getConnection(DBType.ORACLE);
//            String sql = ""
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }finally {
//
//        }
//        return 0;
//    }

    public int updateEmployee(EmployeeDTO employeeDTO){

        return 0;
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
