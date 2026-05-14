package humanresource.DAO;


import global.types.DBType;
import global.types.EmploymentStatus;
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
            String sql = "INSERT INTO EMPLOYEE(EMP_ID, DEPT_ID, POSITION_ID, NAME, STATUS_ID, HIRE_DATE, PASSWORD) values(?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, employeeDTO.getEmpId());
            pstmt.setLong(2, employeeDTO.getDeptId());
            pstmt.setLong(3, employeeDTO.getPositionId());
            pstmt.setString(4, employeeDTO.getEname());
            
            if (employeeDTO.getStatusId() != null) {
                pstmt.setInt(5, employeeDTO.getStatusId().getCode());
            } else {
                pstmt.setInt(5, EmploymentStatus.ACTIVE.getCode());
            }

            if (employeeDTO.getHireDate() != null) {
                pstmt.setDate(6, java.sql.Date.valueOf(employeeDTO.getHireDate()));
            } else {
                pstmt.setDate(6, java.sql.Date.valueOf(java.time.LocalDate.now()));
            }

            // 비밀번호 설정 (초기 비밀번호는'1234')
            if (employeeDTO.getPassword() != null) {
                pstmt.setString(7, employeeDTO.getPassword());
            } else {
                pstmt.setString(7, "1234"); 
            }

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
            String sql = "UPDATE EMPLOYEE SET DEPT_ID=?, POSITION_ID=?, STATUS_ID=?, NAME=?, " +
                    "CONTACT=?, EMAIL=?, ADDRESS=?, SALARY_ACCOUNT=?, PAY_GRADE=?, PASSWORD=?, RESIGN_DATE=? " +
                    "WHERE EMP_ID=?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, employeeDTO.getDeptId());
            pstmt.setLong(2, employeeDTO.getPositionId());
            if (employeeDTO.getStatusId() != null) {
                pstmt.setInt(3, employeeDTO.getStatusId().getCode());
            } else {
                pstmt.setNull(3, java.sql.Types.NUMERIC); // 혹시 비어있을 경우를 대비한 방어 로직
            }
            pstmt.setString(4, employeeDTO.getEname());
            pstmt.setString(5, employeeDTO.getContact());
            pstmt.setString(6, employeeDTO.getEmail());
            pstmt.setString(7, employeeDTO.getAddress());
            pstmt.setString(8, employeeDTO.getSalAccount());
            pstmt.setInt(9, employeeDTO.getPayGrade());
            pstmt.setString(10, employeeDTO.getPassword());
            
            if (employeeDTO.getResignDate() != null) {
                pstmt.setDate(11, java.sql.Date.valueOf(employeeDTO.getResignDate()));
            } else {
                pstmt.setNull(11, java.sql.Types.DATE);
            }
            
            // WHERE 조건에 들어갈 사번
            pstmt.setLong(12, employeeDTO.getEmpId());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return rowcount;
    }

    public EmployeeDTO selectEmployeeById(Long empId){

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        EmployeeDTO emp = null;

        try{
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT * FROM EMPLOYEE WHERE EMP_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, empId);
            rs = pstmt.executeQuery();

            if(rs.next()){
                emp = new EmployeeDTO();
                emp.setEmpId(rs.getLong("EMP_ID"));
                emp.setDeptId(rs.getLong("DEPT_ID"));
                emp.setPositionId(rs.getLong("POSITION_ID"));

                int statusCode = rs.getInt("STATUS_ID");
                emp.setStatusId(EmploymentStatus.fromCode(statusCode));
                emp.setEname(rs.getString("NAME"));

                java.sql.Date hireDate = rs.getDate("HIRE_DATE");
                if(hireDate != null) emp.setHireDate(hireDate.toLocalDate());

                java.sql.Date resignDate = rs.getDate("RESIGN_DATE");
                if(resignDate != null) emp.setResignDate(resignDate.toLocalDate());

                emp.setContact(rs.getString("CONTACT"));
                emp.setGender(rs.getString("GENDER"));
                emp.setEmail(rs.getString("EMAIL"));
                emp.setAddress(rs.getString("ADDRESS"));
                emp.setSalAccount(rs.getString("SALARY_ACCOUNT"));
                emp.setPayGrade(rs.getInt("PAY_GRADE"));
                emp.setPassword(rs.getString("PASSWORD"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
                emp.setEname(rs.getString("NAME"));
                java.sql.Date hireDate = rs.getDate("HIRE_DATE");
                if(hireDate != null) {
                    emp.setHireDate(hireDate.toLocalDate());
                }
                java.sql.Date resignDate = rs.getDate("RESIGN_DATE");
                if(resignDate != null) {
                    emp.setResignDate(resignDate.toLocalDate());
                }
                emp.setContact(rs.getString("CONTACT"));
                emp.setGender(rs.getString("GENDER"));
                emp.setEmail(rs.getString("EMAIL"));
                emp.setAddress(rs.getString("ADDRESS"));
                emp.setSalAccount(rs.getString("SALARY_ACCOUNT"));

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
