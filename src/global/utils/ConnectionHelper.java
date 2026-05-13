package global.utils;
import global.types.DBType;

import java.sql.*;

public class ConnectionHelper {

    public static Connection getConnection(DBType dbtype) {

        Connection conn = null;

        try {
            switch (dbtype) {
                case ORACLE:
                    conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/FREEPDB1", "do", "1234");
                    break;
                case POSTGRES:
                    conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/sampledb", "do", "1234");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    public static Connection getConnection(DBType dbtype, String id, String pwd) {

        Connection conn = null;

        try {
            switch (dbtype) {
                case ORACLE:
                    conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/FREEPDB1", id, pwd);
                    break;
                case POSTGRES:
                    conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/sampledb", id, pwd);
                    break;
                case MARIADB:
                    conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/SampleDB", id, pwd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    // 자원해제
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}