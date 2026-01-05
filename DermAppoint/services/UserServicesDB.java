package services;

import db.DataBaseConnection;
import models.Patient;
import models.Admin;
import models.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class UserServicesDB {

    private User currentUser;

    // Password hashing constants
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 32;
    private static final int HASH_ITERATIONS = 10000;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    // ================= CURRENT USER =================
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        if (currentUser != null) {
            invalidateSession(currentUser.getUserId());
            currentUser = null;
        }
    }

    // ================= SINGLE ADMIN LOGIN ENFORCEMENT =================

    private boolean isAnotherAdminLoggedIn(String excludeUserId) {
        String sql = "SELECT 1 FROM admin_sessions WHERE is_active = TRUE " +
                "AND expires_at > NOW() " +
                "AND admin_id != ? " +
                "LIMIT 1";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, excludeUserId);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void forceLogoutOtherAdmins(String currentAdminId) {
        String sql = "UPDATE admin_sessions SET is_active = FALSE, " +
                "expires_at = NOW() " +
                "WHERE admin_id != ? AND is_active = TRUE";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentAdminId);
            ps.executeUpdate();

            logSystemInfo("ADMIN_FORCE_LOGOUT",
                    "Force logged out other admins for admin: " + currentAdminId,
                    currentAdminId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String createAdminSession(String adminId, String ipAddress, String userAgent) {
        String sessionId = UUID.randomUUID().toString();
        String sessionToken = generateSessionToken();
        Timestamp expiresAt = new Timestamp(
                System.currentTimeMillis() + (SESSION_TIMEOUT_MINUTES * 60 * 1000));

        String sql = "INSERT INTO admin_sessions " +
                "(session_id, admin_id, session_token, ip_address, user_agent, expires_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sessionId);
            ps.setString(2, adminId);
            ps.setString(3, sessionToken);
            ps.setString(4, ipAddress);
            ps.setString(5, userAgent);
            ps.setTimestamp(6, expiresAt);

            ps.executeUpdate();

            return sessionId;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void invalidateSession(String userId) {
        String sql = "UPDATE admin_sessions SET is_active = FALSE, expires_at = NOW() " +
                "WHERE admin_id = ? AND is_active = TRUE";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean validateAdminSession(String adminId) {
        String sql = "SELECT 1 FROM admin_sessions " +
                "WHERE admin_id = ? AND is_active = TRUE " +
                "AND expires_at > NOW() " +
                "LIMIT 1";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, adminId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                updateLastActivity(adminId);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void updateLastActivity(String adminId) {
        String sql = "UPDATE admin_sessions SET last_activity = NOW() " +
                "WHERE admin_id = ? AND is_active = TRUE";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, adminId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLoggedInAdminId() {
        String sql = "SELECT admin_id FROM admin_sessions " +
                "WHERE is_active = TRUE AND expires_at > NOW() " +
                "ORDER BY last_activity DESC LIMIT 1";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("admin_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= PASSWORD HASHING UTILITIES =================
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes());

            for (int i = 1; i < HASH_ITERATIONS; i++) {
                hash = digest.digest(hash);
            }

            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash, String storedSalt) {
        String computedHash = hashPassword(password, storedSalt);
        return computedHash.equals(storedHash);
    }

    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }

    // ================= MODIFIED LOGIN FOR ORIGINAL MODELS =================
    public User loginAndGetUser(String email, String password, String ipAddress, String userAgent) {
        // Check if trying to log in as admin and another admin is already logged in
        String checkAdminSql = "SELECT user_id FROM users WHERE email = ? AND user_type = 'ADMIN'";
        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement checkPs = conn.prepareStatement(checkAdminSql)) {

            checkPs.setString(1, email);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                String adminId = rs.getString("user_id");
                String loggedInAdminId = getLoggedInAdminId();

                if (loggedInAdminId != null && !loggedInAdminId.equals(adminId)) {
                    logSystemWarning("ADMIN_LOGIN_BLOCKED",
                            "Admin login blocked for " + email +
                                    ". Admin " + loggedInAdminId + " is already logged in.",
                            null);
                    throw new RuntimeException("Another admin is already logged in. " +
                            "Please ask the current admin to log out first.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Proceed with normal login
        return loginAndGetUserInternal(email, password, ipAddress, userAgent);
    }

    private User loginAndGetUserInternal(String email, String password, String ipAddress, String userAgent) {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;

                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("password_salt");

                if (!verifyPassword(password, storedHash, storedSalt)) {
                    incrementFailedLoginAttempts(rs.getString("user_id"));
                    return null;
                }

                String userId = rs.getString("user_id");
                resetFailedLoginAttempts(userId);
                updateLastLogin(userId);

                String userType = rs.getString("user_type");

                if ("PATIENT".equalsIgnoreCase(userType)) {
                    Patient patient = new Patient();
                    patient.setUserId(userId);
                    patient.setFullName(rs.getString("full_name"));
                    patient.setEmail(rs.getString("email"));
                    patient.setPhone(rs.getString("phone"));

                    // Only set fields that exist in the original Patient class
                    // If Patient class has these methods, uncomment them
                    // patient.setAddress(rs.getString("address"));
                    // patient.setEmergencyContact(rs.getString("emergency_contact"));

                    Date bd = rs.getDate("birth_date");
                    if (bd != null) {
                        // If Patient class has setBirthDate method
                        try {
                            patient.getClass().getMethod("setBirthDate", LocalDate.class)
                                    .invoke(patient, bd.toLocalDate());
                        } catch (Exception e) {
                            // Method doesn't exist, skip it
                        }
                    }

                    currentUser = patient;
                    return patient;

                } else if ("ADMIN".equalsIgnoreCase(userType)) {
                    // Force logout other admins before logging in new admin
                    forceLogoutOtherAdmins(userId);

                    // Create admin session
                    createAdminSession(userId, ipAddress, userAgent);

                    Admin admin = new Admin();
                    admin.setUserId(userId);
                    admin.setFullName(rs.getString("full_name"));
                    admin.setEmail(rs.getString("email"));
                    admin.setRole(rs.getString("admin_role"));

                    // If Admin class has setPhone method
                    try {
                        admin.getClass().getMethod("setPhone", String.class)
                                .invoke(admin, rs.getString("phone"));
                    } catch (Exception e) {
                        // Method doesn't exist, skip it
                    }

                    currentUser = admin;

                    logSystemInfo("ADMIN_LOGIN", "Admin logged in: " + email, userId);
                    return admin;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            logSystemError("LOGIN_ERROR", "Login failed for email: " + email,
                    null, e.getMessage());
        }

        return null;
    }

    // ================= MODIFIED REGISTRATION FOR ORIGINAL MODELS =================
    public Patient registerPatient(
            String fullName,
            String email,
            String phone,
            String password,
            String address,
            LocalDate dob,
            String emergencyContact) {

        String checkSql = "SELECT 1 FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users " +
                "(user_id, email, password_hash, password_salt, full_name, phone, " +
                "birth_date, address, user_type, emergency_contact, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PATIENT', ?, TRUE)";

        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Check if email exists
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, email);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    throw new RuntimeException("Email already exists. Please use a different email.");
                }
            }

            // Generate user ID
            String userId = generatePatientId();

            // Generate salt and hash password
            String salt = generateSalt();
            String passwordHash = hashPassword(password, salt);

            // Insert new user
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, userId);
                ps.setString(2, email);
                ps.setString(3, passwordHash);
                ps.setString(4, salt);
                ps.setString(5, fullName);
                ps.setString(6, phone);

                if (dob != null)
                    ps.setDate(7, Date.valueOf(dob));
                else
                    ps.setNull(7, Types.DATE);

                ps.setString(8, address);
                ps.setString(9, emergencyContact);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating patient failed, no rows affected.");
                }
            }

            conn.commit();

            // Create patient object (with only available fields)
            Patient patient = new Patient();
            patient.setUserId(userId);
            patient.setFullName(fullName);
            patient.setEmail(email);
            patient.setPhone(phone);

            // Try to set additional fields if methods exist
            try {
                patient.getClass().getMethod("setAddress", String.class)
                        .invoke(patient, address);
            } catch (Exception e) {
                // Method doesn't exist, skip it
            }

            try {
                patient.getClass().getMethod("setBirthDate", LocalDate.class)
                        .invoke(patient, dob);
            } catch (Exception e) {
                // Method doesn't exist, skip it
            }

            logSystemInfo("USER_REGISTERED", "New patient registered: " + email, userId);

            return patient;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            logSystemError("REGISTRATION_ERROR", "Failed to register patient: " + email,
                    null, e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String generatePatientId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(user_id, 4) AS UNSIGNED)) as max_id " +
                "FROM users WHERE user_id LIKE 'PAT%'";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            int maxId = 0;
            if (rs.next()) {
                maxId = rs.getInt("max_id");
                if (rs.wasNull()) {
                    maxId = 0;
                }
            }

            return String.format("PAT%06d", maxId + 1);

        } catch (SQLException e) {
            e.printStackTrace();
            return "PAT" + (System.currentTimeMillis() % 1_000_000);
        }
    }

    // ================= HELPER METHODS =================
    private void incrementFailedLoginAttempts(String userId) {
        String sql = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1, " +
                "account_locked_until = CASE " +
                "  WHEN failed_login_attempts + 1 >= 5 THEN DATE_ADD(NOW(), INTERVAL 30 MINUTE) " +
                "  ELSE account_locked_until " +
                "END " +
                "WHERE user_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetFailedLoginAttempts(String userId) {
        String sql = "UPDATE users SET failed_login_attempts = 0, " +
                "account_locked_until = NULL " +
                "WHERE user_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLastLogin(String userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logSystemInfo(String level, String message, String userId) {
        logToSystem("INFO", level + ": " + message, userId, null);
    }

    private void logSystemWarning(String level, String message, String userId) {
        logToSystem("WARNING", level + ": " + message, userId, null);
    }

    private void logSystemError(String level, String message, String userId, String errorDetails) {
        logToSystem("ERROR", level + ": " + message + " | Error: " + errorDetails, userId, null);
    }

    private void logToSystem(String level, String message, String userId, String endpoint) {
        String sql = "INSERT INTO system_logs (log_level, message, user_id, endpoint, created_at) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, level);
            ps.setString(2, message);
            ps.setString(3, userId);
            ps.setString(4, endpoint);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to log to system_logs: " + e.getMessage());
        }
    }

    // ================= BACKWARD COMPATIBLE METHODS =================
    public User loginAndGetUser(String email, String password) {
        return loginAndGetUser(email, password, "127.0.0.1", "Unknown");
    }

    public boolean login(String email, String password) {
        return loginAndGetUser(email, password) != null;
    }

    // ================= ADDITIONAL METHODS =================
    public boolean logoutUser(String userId) {
        invalidateSession(userId);

        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            currentUser = null;
        }

        logSystemInfo("USER_LOGOUT", "User logged out: " + userId, userId);
        return true;
    }

    public boolean forceLogoutAdmin(String adminId) {
        if (currentUser != null && currentUser.getUserId().equals(adminId)) {
            currentUser = null;
        }

        invalidateSession(adminId);

        logSystemWarning("ADMIN_FORCE_LOGOUT",
                "Admin was force logged out: " + adminId,
                adminId);

        return true;
    }

    public boolean checkAndRenewAdminSession(String adminId) {
        if (!validateAdminSession(adminId)) {
            return false;
        }

        String sql = "UPDATE admin_sessions SET expires_at = DATE_ADD(NOW(), INTERVAL ? MINUTE) " +
                "WHERE admin_id = ? AND is_active = TRUE";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, SESSION_TIMEOUT_MINUTES);
            ps.setString(2, adminId);
            ps.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void cleanupExpiredSessions() {
        String sql = "UPDATE admin_sessions SET is_active = FALSE " +
                "WHERE expires_at <= NOW() AND is_active = TRUE";

        try (Connection conn = DataBaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            int cleaned = ps.executeUpdate();
            if (cleaned > 0) {
                logSystemInfo("SESSION_CLEANUP",
                        "Cleaned up " + cleaned + " expired sessions",
                        null);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}