package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "logs/system_log.txt";
    private static final DateTimeFormatter dtf = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void log(String message) {
        log(message, "INFO");
    }
    
    public static void log(String message, String level) {
        String timestamp = LocalDateTime.now().format(dtf);
        String logEntry = String.format("[%s] %s: %s", timestamp, level, message);
        
        // Print to console
        System.out.println(logEntry);
        
        // Write to file
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
    
    public static void logLogin(String email, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        log("Login attempt - Email: " + email + " - Status: " + status, "AUTH");
    }
    
    public static void logAppointmentAction(String action, String appointmentId, String user) {
        log("Appointment " + action + " - ID: " + appointmentId + " - By: " + user, "APPOINTMENT");
    }
    
    public static void logError(String error, Exception e) {
        log("ERROR: " + error + " - Exception: " + e.getMessage(), "ERROR");
        e.printStackTrace();
    }
}