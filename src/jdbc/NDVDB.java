package jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import utils.Logger;

public class NDVDB {

    private static String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8";
    private static String DB_HOST = "localhost";
    private static String DB_PORT = "3306";
//    private static String DB_NAME = "linhthuydanhbac";
    private static String DB_SERVER = "ngocrong_user";
    public static String DB_DATA = "ngocrong_data";
    public static String DB_USER = "root";
    private static String DB_PASSWORD = "";
    private static int MIN_CONN = 1;
    private static int MAX_CONN = 1;
    private static long MAX_LIFE_TIME = 120000L;
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    private static final HikariConfig config2 = new HikariConfig();
    private static final HikariDataSource ds2;

    public static Connection getConnectionServer() throws SQLException {
        return ds.getConnection();
    }

    public static Connection getConnectionDATA() throws SQLException {
        return ds2.getConnection();
    }

    public static void close() {
        ds.close();
        ds2.close();
    }

    private static void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("data/config/config.properties")) {
            properties.load(input);

            DRIVER = properties.getProperty("database.driver", DRIVER);
            DB_HOST = properties.getProperty("database.host", DB_HOST);
            DB_PORT = properties.getProperty("database.port", DB_PORT);
            DB_SERVER = properties.getProperty("database.server", DB_SERVER);
            DB_DATA = properties.getProperty("database.data", DB_DATA);
            DB_USER = properties.getProperty("database.user", DB_USER);
            DB_PASSWORD = properties.getProperty("database.pass", DB_PASSWORD);
            MIN_CONN = Integer.parseInt(properties.getProperty("database.min", String.valueOf(MIN_CONN)));
            MAX_CONN = Integer.parseInt(properties.getProperty("database.max", String.valueOf(MAX_CONN)));
            MAX_LIFE_TIME = Long.parseLong(properties.getProperty("database.lifetime", String.valueOf(MAX_LIFE_TIME)));

            Logger.warning("  _         _____     _    _    _____    _____         ____    _____     _   __   _    _   \n");
            Logger.warning(" | |       |  _  |   | |  | |  |_   _|  /  ___|       / ___|  |  _  |   | | / /  | |  | |  \n");
            Logger.warning(" | |       | | | |   | |  | |    | |    \\ `--.       | |  _   | | | |   | |/ /   | |  | |  \n");
            Logger.warning(" | |       | | | |   | |  | |    | |     `--. \\      | | | |  | | | |   |    \\   | |  | |  \n");
            Logger.warning(" | |____   | |_| |   | |_/ /    _| |_   /\\__/ /      | |_| |  | |_| |   | |\\  \\  | |_/ /   \n");
            Logger.warning(" |_____/   \\_____/   \\____/    |_____|  \\____/        \\____|  \\_____/   \\_| \\_/  \\____/    \n");
            Logger.log("[0;32m", "Successfully loaded file properties!\n");

        } catch (IOException | NumberFormatException e) {
            Logger.log("[4;31m", "Không thể load file properties!\n");
        }
    }

    public static NDVResultSet executeQuery(String query) throws Exception {
        try (Connection connection = NDVDB.getConnectionServer(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return new ResultSetImpl(preparedStatement.executeQuery());
        } catch (Exception e) {
            try (Connection connection = NDVDB.getConnectionDATA(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                return new ResultSetImpl(preparedStatement.executeQuery());
            } catch (Exception ex) {
                Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
                throw e;
            }
        }
    }

    public static NDVResultSet executeQuery(String query, Object... params) throws Exception {
        try (Connection connection = NDVDB.getConnectionServer(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return new ResultSetImpl(preparedStatement.executeQuery());
        } catch (Exception e) {
            try (Connection connection = NDVDB.getConnectionDATA(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                return new ResultSetImpl(preparedStatement.executeQuery());
            } catch (Exception ex) {
                Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
                throw e;
            }
        }
    }

    public static int executeUpdate(String query) throws Exception {
        try (Connection connection = NDVDB.getConnectionServer(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            try (Connection connection = NDVDB.getConnectionDATA(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                return preparedStatement.executeUpdate();
            } catch (Exception ex) {
                Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
                throw e;
            }
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

        try (Connection connection = NDVDB.getConnectionServer(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            try (Connection connection = NDVDB.getConnectionDATA(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                return preparedStatement.executeUpdate();
            } catch (Exception ex) {
                Logger.log("[4;31m", "Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
                throw e;
            }
        }
    }

    private static void setupConfig(HikariConfig c, String dbName, String password) {
        c.setDriverClassName(DRIVER);
        c.setJdbcUrl(String.format(URL, DB_HOST, DB_PORT, dbName));
        c.setUsername(DB_USER);
        c.setPassword(password);
        c.setMinimumIdle(MIN_CONN);
        c.setMaximumPoolSize(MAX_CONN);
        c.setMaxLifetime(MAX_LIFE_TIME);
        c.addDataSourceProperty("cachePrepStmts", "true");
        c.addDataSourceProperty("prepStmtCacheSize", "250");
        c.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        c.addDataSourceProperty("useServerPrepStmts", "true");
        c.addDataSourceProperty("useLocalSessionState", "true");
        c.addDataSourceProperty("rewriteBatchedStatements", "true");
        c.addDataSourceProperty("cacheResultSetMetadata", "true");
        c.addDataSourceProperty("cacheServerConfiguration", "true");
        c.addDataSourceProperty("elideSetAutoCommits", "true");
        c.addDataSourceProperty("maintainTimeStats", "true");
    }

    static {
        loadProperties();

        // Thử mật khẩu trước khi khởi tạo Hikari (Tự động thích nghi với XAMPP mật khẩu trống hoặc 123456)
        if (DB_USER.equals("root")) {
            try {
                Class.forName(DRIVER);
                String url = String.format(URL, DB_HOST, DB_PORT, DB_SERVER);
                try (Connection conn = java.sql.DriverManager.getConnection(url, DB_USER, DB_PASSWORD)) {
                    // OK
                } catch (java.sql.SQLException e) {
                    if (e.getMessage().contains("Access denied") || e.getErrorCode() == 1045) {
                        boolean found = false;
                        // Thử mật khẩu TRỐNG
                        if (!DB_PASSWORD.isEmpty()) {
                            try (Connection conn2 = java.sql.DriverManager.getConnection(url, DB_USER, "")) {
                                DB_PASSWORD = "";
                                Logger.log(" [0;32m", "NDVDB: Mật khẩu root sai, đã chuyển sang mật khẩu TRỐNG.\n");
                                found = true;
                            } catch (java.sql.SQLException e2) {}
                        }
                        // Thử mật khẩu 123456
                        if (!found && !DB_PASSWORD.equals("123456")) {
                            try (Connection conn2 = java.sql.DriverManager.getConnection(url, DB_USER, "123456")) {
                                DB_PASSWORD = "123456";
                                Logger.log(" [0;32m", "NDVDB: Mật khẩu root sai, đã chuyển sang mật khẩu 123456.\n");
                                found = true;
                            } catch (java.sql.SQLException e3) {}
                        }
                    }
                }
            } catch (Exception e) {}
        }

        setupConfig(config, DB_SERVER, DB_PASSWORD);
        
        HikariDataSource tempDs1 = null;
        try {
            tempDs1 = new HikariDataSource(config);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            Logger.log(" [4;31m", "NDVDB: KHÔNG THỂ KẾT NỐI DATABASE! Lỗi: " + msg + "\n");
            System.exit(0);
        }
        ds = tempDs1;

        setupConfig(config2, DB_DATA, DB_PASSWORD);
        HikariDataSource tempDs2 = null;
        try {
            tempDs2 = new HikariDataSource(config2);
        } catch (Exception e) {
            Logger.log(" [4;31m", "NDVDB: KHÔNG THỂ KẾT NỐI DATABASE DATA (DS2)! Lỗi: " + e.getMessage() + "\n");
            System.exit(0);
        }
        ds2 = tempDs2;
    }
}
