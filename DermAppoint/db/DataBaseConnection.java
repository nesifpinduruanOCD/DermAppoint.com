package db;

import java.sql.*;
import java.util.List;
import utils.Config;
import utils.Logger;

public class DataBaseConnection {

    private static Connection connection;

    // ================= LOAD DRIVER =================
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Logger.log("MySQL JDBC Driver loaded");
        } catch (ClassNotFoundException e) {
            Logger.logError("MySQL JDBC Driver not found", e);
        }
    }

    private DataBaseConnection() {
        // private constructor to prevent instantiation
    }

    // ================= GET CONNECTION =================
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(
                        Config.DB_URL,
                        Config.DB_USER,
                        Config.DB_PASSWORD);
                Logger.log("Database connected successfully");
            }
        } catch (SQLException e) {
            Logger.logError("Database connection failed", e);
            throw new RuntimeException("Cannot connect to database", e);
        }
        return connection;
    }

    // ================= CLOSE CONNECTION =================
    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Logger.log("Database connection closed");
            }
        } catch (SQLException e) {
            Logger.logError("Failed to close database connection", e);
        }
    }

    // ================= TEST CONNECTION =================
    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(
                Config.DB_URL,
                Config.DB_USER,
                Config.DB_PASSWORD)) {
            return conn.isValid(3);
        } catch (SQLException e) {
            Logger.logError("Database test connection failed", e);
            return false;
        }
    }

    // ================= INITIALIZE DATABASE =================
    public static void initializeDatabase() {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {

            // USERS TABLE
            stmt.execute("""
                            CREATE TABLE IF NOT EXISTS users (
                                user_id VARCHAR(20) PRIMARY KEY,
                                email VARCHAR(100) UNIQUE NOT NULL,
                                password VARCHAR(255) NOT NULL,
                                full_name VARCHAR(100) NOT NULL,
                                phone VARCHAR(20),
                                birth_date DATE,
                                address TEXT,
                                user_type ENUM('PATIENT','ADMIN') NOT NULL,
                                emergency_contact VARCHAR(20),
                                admin_role VARCHAR(50),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                last_login TIMESTAMP,
                                is_active BOOLEAN DEFAULT TRUE
                            )
                    """);

            // SERVICES TABLE
            stmt.execute("""
                            CREATE TABLE IF NOT EXISTS services (
                                service_id VARCHAR(20) PRIMARY KEY,
                                service_name VARCHAR(100) NOT NULL,
                                description TEXT,
                                price DECIMAL(10,2),
                                duration_minutes INT NOT NULL,
                                required_preparation TEXT,
                                is_active BOOLEAN DEFAULT TRUE,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                            )
                    """);

            // DOCTORS TABLE
            stmt.execute("""
                            CREATE TABLE IF NOT EXISTS doctors (
                                doctor_id VARCHAR(20) PRIMARY KEY,
                                name VARCHAR(100) NOT NULL,
                                specialization VARCHAR(100),
                                available_days VARCHAR(50),
                                available_am BOOLEAN,
                                available_pm BOOLEAN,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                            )
                    """);

            // APPOINTMENTS TABLE
            stmt.execute("""
                            CREATE TABLE IF NOT EXISTS appointments (
                                appointment_id VARCHAR(50) PRIMARY KEY,
                                patient_id VARCHAR(20),
                                service_id VARCHAR(20),
                                doctor_id VARCHAR(20),
                                appointment_date DATE,
                                appointment_time TIME,
                                status ENUM('PENDING','APPROVED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (patient_id) REFERENCES users(user_id),
                                FOREIGN KEY (service_id) REFERENCES services(service_id),
                                FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
                            )
                    """);

            Logger.log("Database schema initialized");
            insertSampleData(conn);

        } catch (SQLException e) {
            Logger.logError("Database initialization failed", e);
        }
    }

    // ================= SAMPLE DATA =================
    private static void insertSampleData(Connection conn) throws SQLException {
        String insertAdmin = """
                        INSERT IGNORE INTO users
                        (user_id, email, password, full_name, phone, user_type, admin_role)
                        VALUES ('ADM001','admin@derma.com','admin123','Clinic Admin','09171234567','ADMIN','MANAGER')
                """;

        String insertPatient = """
                        INSERT IGNORE INTO users
                        (user_id, email, password, full_name, phone, user_type)
                        VALUES ('PAT001','john@gmail.com','patient123','John Doe','09170000000','PATIENT')
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(insertAdmin);
            stmt.execute(insertPatient);
        }

        Logger.log("Sample data inserted");
    }

    // ================= QUERY HELPERS =================
    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps.executeQuery();
    }

    public static int executeUpdate(String sql, Object... params) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps.executeUpdate();
    }

    public static int[] executeBatch(String sql, List<Object[]> batchParams) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(sql);
        for (Object[] params : batchParams) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.addBatch();
        }
        return ps.executeBatch();
    }
}
