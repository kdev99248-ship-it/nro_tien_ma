package jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Properties;

import utils.Logger;

public class DBConnecter {

    private static String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Ho_Chi_Minh";
    private static String DB_HOST = "127.0.0.1";
    private static String DB_PORT = "3306";
    public static String DB_DATA = "nrosamurai";
    public static String DB_USER = "root";
    private static String DB_PASSWORD = "123456";
    private static int MIN_CONN = 5;
    private static int MAX_CONN = 30;
    private static long MAX_LIFE_TIME = 300000L;  // 5 phút
    private static long CONNECTION_TIMEOUT = 10000L; // 10 giây chờ lấy conn
    private static long IDLE_TIMEOUT = 120000L;       // Thu hồi conn nhàn rỗi sau 2 phút
    private static long LEAK_DETECTION = 15000L;      // Cảnh báo nếu conn giữ > 15 giây
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    public static Connection getConnectionServer() throws SQLException {
        if (ds == null) {
            throw new SQLException("DataSource is not initialized.");
        }
        return ds.getConnection();
    }

    public static void close() {
        ds.close();
    }

    private static void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("data/config/config.properties")) {
            properties.load(input);

            DRIVER = properties.getProperty("database.driver", DRIVER);
            DB_HOST = properties.getProperty("database.host", DB_HOST);
            DB_PORT = properties.getProperty("database.port", DB_PORT);
            // DB_SERVER = properties.getProperty("database.server", DB_SERVER);
            DB_DATA = properties.getProperty("database.name", DB_DATA);
            DB_USER = properties.getProperty("database.user", DB_USER);
            DB_PASSWORD = properties.getProperty("database.pass", DB_PASSWORD);
            MIN_CONN = Integer.parseInt(properties.getProperty("database.min", String.valueOf(MIN_CONN)));
            MAX_CONN = Integer.parseInt(properties.getProperty("database.max", String.valueOf(MAX_CONN)));
            MAX_LIFE_TIME = Long.parseLong(properties.getProperty("database.lifetime", String.valueOf(MAX_LIFE_TIME)));
            CONNECTION_TIMEOUT = Long.parseLong(properties.getProperty("database.connection_timeout", String.valueOf(CONNECTION_TIMEOUT)));
            IDLE_TIMEOUT = Long.parseLong(properties.getProperty("database.idle_timeout", String.valueOf(IDLE_TIMEOUT)));
            LEAK_DETECTION = Long.parseLong(properties.getProperty("database.leak_detection", String.valueOf(LEAK_DETECTION)));

            Logger.warning("NRO_MOD\n");

            Logger.log("[0;32m", "Successfully loaded file properties!\n");

        } catch (IOException | NumberFormatException e) {
            Logger.log("[4;31m", "Không thể load file properties!\n");
        }
    }

    public static NDVResultSet executeQuery(String query) throws Exception {
        try (Connection connection = DBConnecter.getConnectionServer();
                PreparedStatement preparedStatement = connection.prepareStatement(query,
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            return new ResultSetImpl(preparedStatement.executeQuery());
        } catch (Exception e) {
            Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    public static NDVResultSet executeQuery(String query, Object... params) throws Exception {
        try (Connection connection = DBConnecter.getConnectionServer();
                PreparedStatement preparedStatement = connection.prepareStatement(query,
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return new ResultSetImpl(preparedStatement.executeQuery());
        } catch (Exception e) {
            Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    public static int executeUpdate(String query) throws Exception {
        try (Connection connection = DBConnecter.getConnectionServer();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    public static int executeUpdate(String query, Object... params) throws Exception {
        if (query.toLowerCase().startsWith("insert") && query.endsWith("()")) {
            StringBuilder placeholder = new StringBuilder();
            placeholder.append("(");
            for (int i = 0; i < params.length; i++) {
                placeholder.append("?");
                if (i < params.length - 1) {
                    placeholder.append(",");
                }
            }
            placeholder.append(")");
            query = query.replace("()", placeholder.toString());
        }

        try (Connection connection = DBConnecter.getConnectionServer();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
    }

    private static void setupConfig(HikariConfig c, String password) {
        c.setDriverClassName(DRIVER);
        c.setJdbcUrl(String.format(URL, DB_HOST, DB_PORT, DB_DATA));
        c.setUsername(DB_USER);
        c.setPassword(password);
        c.setMinimumIdle(MIN_CONN);
        c.setMaximumPoolSize(MAX_CONN);
        c.setMaxLifetime(MAX_LIFE_TIME);
        c.setConnectionTimeout(CONNECTION_TIMEOUT);
        c.setIdleTimeout(IDLE_TIMEOUT);
        c.setLeakDetectionThreshold(LEAK_DETECTION);
        c.setPoolName("NRO-HikariPool");
        c.addDataSourceProperty("cachePrepStmts", "true");
        c.addDataSourceProperty("prepStmtCacheSize", "500");
        c.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
        c.addDataSourceProperty("useServerPrepStmts", "true");
        c.addDataSourceProperty("useLocalSessionState", "true");
        c.addDataSourceProperty("rewriteBatchedStatements", "true");
        c.addDataSourceProperty("cacheResultSetMetadata", "true");
        c.addDataSourceProperty("cacheServerConfiguration", "true");
        c.addDataSourceProperty("elideSetAutoCommits", "true");
        c.addDataSourceProperty("maintainTimeStats", "false");
    }

    static {
        loadProperties();

        // Thử mật khẩu trước khi khởi tạo Hikari (Tự động thích nghi với XAMPP mật khẩu trống hoặc 123456)
        if (DB_USER.equals("root")) {
            try {
                Class.forName(DRIVER);
                String url = String.format(URL, DB_HOST, DB_PORT, DB_DATA);
                try (Connection conn = java.sql.DriverManager.getConnection(url, DB_USER, DB_PASSWORD)) {
                    // OK
                } catch (java.sql.SQLException e) {
                    if (e.getMessage().contains("Access denied") || e.getErrorCode() == 1045) {
                        boolean found = false;
                        // Thử mật khẩu TRỐNG
                        if (!DB_PASSWORD.isEmpty()) {
                            try (Connection conn2 = java.sql.DriverManager.getConnection(url, DB_USER, "")) {
                                DB_PASSWORD = "";
                                Logger.log(" [0;32m", "Mật khẩu root sai, đã chuyển sang mật khẩu TRỐNG.\n");
                                found = true;
                            } catch (java.sql.SQLException e2) {}
                        }
                        // Thử mật khẩu 123456
                        if (!found && !DB_PASSWORD.equals("123456")) {
                            try (Connection conn2 = java.sql.DriverManager.getConnection(url, DB_USER, "123456")) {
                                DB_PASSWORD = "123456";
                                Logger.log(" [0;32m", "Mật khẩu root sai, đã chuyển sang mật khẩu 123456.\n");
                                found = true;
                            } catch (java.sql.SQLException e3) {}
                        }
                    }
                }
            } catch (Exception e) {}
        }

        setupConfig(config, DB_PASSWORD);
        
        HikariDataSource tempDs = null;
        try {
            tempDs = new HikariDataSource(config);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            Logger.log(" [4;31m", "Lỗi kết nối database: " + msg + "\n");
            Logger.log(" [4;31m", "KHÔNG THỂ KẾT NỐI DATABASE! Vui lòng kiểm tra lại cấu hình SQL hoặc MySQL (XAMPP) đã bật chưa.\n");
            System.exit(0);
        }
        ds = tempDs;
    }
}
