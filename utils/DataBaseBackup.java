package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import db.DataBaseConnection;

public class DataBaseBackup {
    
    /**
     * Backup database to SQL file
     */
    public static boolean backupDatabase() {
        String backupDir = Config.BACKUP_DIRECTORY;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = backupDir + "/backup_" + timestamp + ".sql";
        
        try {
            // Create backup directory if it doesn't exist
            Files.createDirectories(Paths.get(backupDir));
            
            // Execute mysqldump command
            ProcessBuilder processBuilder = new ProcessBuilder(
                "mysqldump",
                "--host=localhost",
                "--port=3306",
                "--user=" + Config.DB_USER,
                "--password=" + Config.DB_PASSWORD,
                "--databases",
                Config.DB_NAME,
                "--result-file=" + backupFile,
                "--skip-comments",
                "--skip-dump-date",
                "--complete-insert",
                "--extended-insert"
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Logger.log("Database backup created: " + backupFile);
                
                // Clean up old backups (keep only last 7 days)
                cleanupOldBackups(backupDir);
                
                return true;
            } else {
                Logger.log("Database backup failed with exit code: " + exitCode, "ERROR");
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            Logger.logError("Failed to backup database", e);
            return false;
        }
    }
    
    /**
     * Clean up old backup files
     */
    private static void cleanupOldBackups(String backupDir) {
        File directory = new File(backupDir);
        File[] backupFiles = directory.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".sql"));
        
        if (backupFiles != null) {
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            
            for (File file : backupFiles) {
                if (file.lastModified() < sevenDaysAgo) {
                    try {
                        Files.delete(file.toPath());
                        Logger.log("Deleted old backup: " + file.getName());
                    } catch (IOException e) {
                        Logger.logError("Failed to delete old backup: " + file.getName(), e);
                    }
                }
            }
        }
    }
    
    /**
     * Export appointments to CSV file
     */
    public static boolean exportAppointmentsToCSV(LocalDate startDate, LocalDate endDate, String filename) {
        String exportDir = Config.EXPORT_DIRECTORY;
        String filePath = exportDir + "/" + filename + ".csv";
        
        try {
            Files.createDirectories(Paths.get(exportDir));
            
            String sql = """
                SELECT 
                    a.appointment_id,
                    a.patient_name,
                    a.appointment_date,
                    a.appointment_time,
                    s.service_name,
                    d.name as doctor_name,
                    a.status,
                    a.is_for_another_person,
                    a.other_person_name,
                    a.created_at
                FROM appointments a
                LEFT JOIN services s ON a.service_id = s.service_id
                LEFT JOIN doctors d ON a.doctor_id = d.doctor_id
                WHERE a.appointment_date BETWEEN ? AND ?
                ORDER BY a.appointment_date, a.appointment_time
            """;
            try (Connection conn = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 FileWriter writer = new FileWriter(filePath)) {
                
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                
                ResultSet rs = pstmt.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // Write header
                for (int i = 1; i <= columnCount; i++) {
                    writer.write(metaData.getColumnName(i));
                    if (i < columnCount) writer.write(",");
                }
                writer.write("\n");
                
                // Write data
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        writer.write(value != null ? "\"" + value.replace("\"", "\"\"") + "\"" : "");
                        if (i < columnCount) writer.write(",");
                    }
                    writer.write("\n");
                }
                
                Logger.log("Appointments exported to CSV: " + filePath);
                return true;
            }
            
        } catch (Exception e) {
            Logger.logError("Failed to export appointments to CSV", e);
            return false;
        }
    }
    
    /**
     * Generate monthly report
     */
    public static boolean generateMonthlyReport(int year, int month) {
        String reportDir = Config.EXPORT_DIRECTORY + "/reports";
        String filename = String.format("report_%04d_%02d.csv", year, month);
        String filePath = reportDir + "/" + filename;
        
        try {
            Files.createDirectories(Paths.get(reportDir));
            
            String sql = """
                SELECT 
                    DATE(a.appointment_date) as date,
                    COUNT(*) as total_appointments,
                    SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
                    SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled,
                    SUM(s.price) as total_revenue,
                    GROUP_CONCAT(DISTINCT d.name) as doctors
                FROM appointments a
                LEFT JOIN services s ON a.service_id = s.service_id
                LEFT JOIN doctors d ON a.doctor_id = d.doctor_id
                WHERE YEAR(a.appointment_date) = ? 
                AND MONTH(a.appointment_date) = ?
                GROUP BY DATE(a.appointment_date)
                ORDER BY date
            """;
            try (Connection conn = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 FileWriter writer = new FileWriter(filePath)) {
                
                pstmt.setInt(1, year);
                pstmt.setInt(2, month);
                
                ResultSet rs = pstmt.executeQuery();
                
                // Write header
                writer.write("Date,Total Appointments,Completed,Cancelled,Total Revenue,Doctors\n");
                
                // Write data
                while (rs.next()) {
                    writer.write(String.format("%s,%d,%d,%d,%.2f,%s\n",
                        rs.getDate("date"),
                        rs.getInt("total_appointments"),
                        rs.getInt("completed"),
                        rs.getInt("cancelled"),
                        rs.getDouble("total_revenue"),
                        rs.getString("doctors")
                    ));
                }
                
                Logger.log("Monthly report generated: " + filePath);
                return true;
            }
            
        } catch (Exception e) {
            Logger.logError("Failed to generate monthly report", e);
            return false;
        }
    }
}