package global.utils;
import global.types.DBType;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class ConnectionHelper {

    private static final Properties props = new Properties();

    static {
        try {
            InputStream input = ConnectionHelper.class.getClassLoader().getResourceAsStream("db.properties");
            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(DBType dbtype) {

        Connection conn = null;

        try {
            switch (dbtype) {
                case ORACLE:
                    conn = DriverManager.getConnection(
                            props.getProperty("oracle.url"),
                            props.getProperty("oracle.user"),
                            props.getProperty("oracle.password")
                    );
                    break;
                case POSTGRES:
                    conn = DriverManager.getConnection(
                            props.getProperty("postgres.url"),
                            props.getProperty("postgres.user"),
                            props.getProperty("postgres.password")
                    );
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
                    conn = DriverManager.getConnection(props.getProperty("oracle.url"), id, pwd);
                    break;
                case MARIADB:
                    conn = DriverManager.getConnection(props.getProperty("mariadb.url"), id, pwd);
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