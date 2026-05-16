package humanresource.dao;


import global.types.DBType;
import global.types.EmploymentStatus;
import global.utils.ConnectionHelper;
import humanresource.dto.EmployeeDTO;
import humanresource.dto.EmployeeInfoDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public int insertEmployee(EmployeeDTO employeeDTO){
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowcount = 0;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "INSERT INTO EMPLOYEE(EMP_ID, DEPT_ID, POSITION_ID, NAME, STATUS_ID, HIRE_DATE, PASSWORD, CONTACT, GENDER, EMAIL, ADDRESS, SALARY_ACCOUNT, PAY_GRADE, IS_ADMIN) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                pstmt.setNull(6, java.sql.Types.DATE);
            }

            if (employeeDTO.getPassword() != null) {
                pstmt.setString(7, employeeDTO.getPassword());
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }

            //여기부터는 null가능한 값들
            pstmt.setString(8, employeeDTO.getContact());
            pstmt.setString(9, employeeDTO.getGender());
            pstmt.setString(10, employeeDTO.getEmail());
            pstmt.setString(11, employeeDTO.getAddress());
            pstmt.setString(12, employeeDTO.getSalAccount());
            pstmt.setInt(13, employeeDTO.getPayGrade() == 0 ? 1 : employeeDTO.getPayGrade()); // 기본 호봉 1
            pstmt.setString(14, employeeDTO.getIsAdmin() != null && employeeDTO.getIsAdmin() ? "Y" : "N");

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
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
                    "CONTACT=?, EMAIL=?, ADDRESS=?, SALARY_ACCOUNT=?, PAY_GRADE=?, PASSWORD=?, RESIGN_DATE=?, IS_ADMIN=? " +
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
            pstmt.setString(12, employeeDTO.getIsAdmin() != null && employeeDTO.getIsAdmin() ? "Y" : "N");
            pstmt.setLong(13, employeeDTO.getEmpId());

            rowcount = pstmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
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
                String isAdmin = rs.getString("IS_ADMIN");
                emp.setIsAdmin("Y".equalsIgnoreCase(isAdmin) || "TRUE".equalsIgnoreCase(isAdmin));
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB Error", e);
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
            throw new RuntimeException("DB Error", e);
        }finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return empList;
    }


    public EmployeeInfoDTO selectEmployeeInfoDetail(Long empId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        EmployeeInfoDTO info = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT e.EMP_ID, e.NAME, d.DEPT_NAME, p.POSITION_NAME, " +
                    "e.PAY_GRADE, e.HIRE_DATE, e.GENDER, e.CONTACT, e.EMAIL, e.ADDRESS " +
                    "FROM EMPLOYEE e " +
                    "LEFT JOIN DEPARTMENT d ON e.DEPT_ID = d.DEPT_ID " +
                    "LEFT JOIN POSITION p ON e.POSITION_ID = p.POSITION_ID " +
                    "WHERE e.EMP_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, empId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                info = new EmployeeInfoDTO();
                info.setEmpId(rs.getLong("EMP_ID"));
                info.setEName(rs.getString("NAME"));
                info.setDeptName(rs.getString("DEPT_NAME"));
                info.setPositionName(rs.getString("POSITION_NAME"));
                info.setPayGrade(rs.getInt("PAY_GRADE"));

                java.sql.Date hireDate = rs.getDate("HIRE_DATE");
                if (hireDate != null) info.setHireDate(hireDate.toLocalDate());

                info.setGender(rs.getString("GENDER"));
                info.setContact(rs.getString("CONTACT"));
                info.setEmail(rs.getString("EMAIL"));
                info.setAddress(rs.getString("ADDRESS"));
            }
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return info;
    }

    public List<EmployeeInfoDTO> selectAllEmployeeInfoList() {
        List<EmployeeInfoDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT e.EMP_ID, e.NAME, d.DEPT_NAME, p.POSITION_NAME, e.HIRE_DATE " +
                    "FROM EMPLOYEE e " +
                    "LEFT JOIN DEPARTMENT d ON e.DEPT_ID = d.DEPT_ID " +
                    "LEFT JOIN POSITION p ON e.POSITION_ID = p.POSITION_ID " +
                    "ORDER BY e.EMP_ID ASC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                EmployeeInfoDTO info = new EmployeeInfoDTO();
                info.setEmpId(rs.getLong("EMP_ID"));
                info.setEName(rs.getString("NAME"));
                info.setDeptName(rs.getString("DEPT_NAME"));
                info.setPositionName(rs.getString("POSITION_NAME"));

                java.sql.Date hireDate = rs.getDate("HIRE_DATE");
                if (hireDate != null) info.setHireDate(hireDate.toLocalDate());

                list.add(info);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return list;
    }

    /**
     * 이름으로 직원 검색 (부분 일치)
     */
    public List<EmployeeInfoDTO> searchEmployeesByName(String name) {
        List<EmployeeInfoDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT e.EMP_ID, e.NAME, d.DEPT_NAME, p.POSITION_NAME, e.PAY_GRADE, e.HIRE_DATE " +
                    "FROM EMPLOYEE e " +
                    "LEFT JOIN DEPARTMENT d ON e.DEPT_ID = d.DEPT_ID " +
                    "LEFT JOIN POSITION p ON e.POSITION_ID = p.POSITION_ID " +
                    "WHERE e.NAME LIKE ? " +
                    "ORDER BY e.EMP_ID ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + name + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                EmployeeInfoDTO info = new EmployeeInfoDTO();
                info.setEmpId(rs.getLong("EMP_ID"));
                info.setEName(rs.getString("NAME"));
                info.setDeptName(rs.getString("DEPT_NAME"));
                info.setPositionName(rs.getString("POSITION_NAME"));
                info.setPayGrade(rs.getInt("PAY_GRADE"));

                java.sql.Date hireDate = rs.getDate("HIRE_DATE");
                if (hireDate != null) info.setHireDate(hireDate.toLocalDate());

                list.add(info);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return list;
    }

    /**
     * 부서ID로 직원 검색
     */
    public List<EmployeeInfoDTO> searchEmployeesByDeptId(Long deptId) {
        List<EmployeeInfoDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            String sql = "SELECT e.EMP_ID, e.NAME, d.DEPT_NAME, p.POSITION_NAME, e.PAY_GRADE, e.HIRE_DATE " +
                    "FROM EMPLOYEE e " +
                    "LEFT JOIN DEPARTMENT d ON e.DEPT_ID = d.DEPT_ID " +
                    "LEFT JOIN POSITION p ON e.POSITION_ID = p.POSITION_ID " +
                    "WHERE e.DEPT_ID = ? " +
                    "ORDER BY e.EMP_ID ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, deptId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                EmployeeInfoDTO info = new EmployeeInfoDTO();
                info.setEmpId(rs.getLong("EMP_ID"));
                info.setEName(rs.getString("NAME"));
                info.setDeptName(rs.getString("DEPT_NAME"));
                info.setPositionName(rs.getString("POSITION_NAME"));
                info.setPayGrade(rs.getInt("PAY_GRADE"));

                java.sql.Date hireDate = rs.getDate("HIRE_DATE");
                if (hireDate != null) info.setHireDate(hireDate.toLocalDate());

                list.add(info);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return list;
    }

    /**
     * 이름과 부서로 복합 검색
     */
    public List<EmployeeInfoDTO> searchEmployees(String name, Long deptId) {
        List<EmployeeInfoDTO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            StringBuilder sql = new StringBuilder(
                    "SELECT e.EMP_ID, e.NAME, d.DEPT_NAME, p.POSITION_NAME, e.PAY_GRADE, e.HIRE_DATE " +
                    "FROM EMPLOYEE e " +
                    "LEFT JOIN DEPARTMENT d ON e.DEPT_ID = d.DEPT_ID " +
                    "LEFT JOIN POSITION p ON e.POSITION_ID = p.POSITION_ID " +
                    "WHERE 1=1 "
            );

            int paramIndex = 1;
            if (name != null && !name.isEmpty()) {
                sql.append("AND e.NAME LIKE ? ");
            }
            if (deptId != null) {
                sql.append("AND e.DEPT_ID = ? ");
            }
            sql.append("ORDER BY e.EMP_ID ASC");

            pstmt = conn.prepareStatement(sql.toString());

            if (name != null && !name.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + name + "%");
            }
            if (deptId != null) {
                pstmt.setLong(paramIndex, deptId);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                EmployeeInfoDTO info = new EmployeeInfoDTO();
                info.setEmpId(rs.getLong("EMP_ID"));
                info.setEName(rs.getString("NAME"));
                info.setDeptName(rs.getString("DEPT_NAME"));
                info.setPositionName(rs.getString("POSITION_NAME"));
                info.setPayGrade(rs.getInt("PAY_GRADE"));

                java.sql.Date hireDate = rs.getDate("HIRE_DATE");
                if (hireDate != null) info.setHireDate(hireDate.toLocalDate());

                list.add(info);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB Error", e);
        } finally {
            ConnectionHelper.close(rs);
            ConnectionHelper.close(pstmt);
            ConnectionHelper.close(conn);
        }
        return list;
    }

}
