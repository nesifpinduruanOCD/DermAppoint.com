package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    // ================= Database Configuration =================
    public static String DB_NAME;
    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;

    // ================= Application Configuration =================
    public static int MAX_AM_SLOTS;
    public static int MAX_PM_SLOTS;
    public static int APPOINTMENT_REMINDER_HOURS;
    public static String CLINIC_NAME;
    public static String CLINIC_ADDRESS;
    public static String CLINIC_PHONE;

    // ================= Email Configuration =================
    public static String SMTP_HOST;
    public static String SMTP_PORT;
    public static String SMTP_USER;
    public static String SMTP_PASSWORD;

    // ================= File Paths =================
    public static String LOG_DIRECTORY;
    public static String EXPORT_DIRECTORY;
    public static String BACKUP_DIRECTORY;

    // ================= Static Block =================
    static {
        loadConfiguration();
    }

    private Config() {
        // private constructor to prevent instantiation
    }

    // ================= Load Configuration =================
    private static void loadConfiguration() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config/dermappoint.properties")) {
            props.load(fis);

            // Database
            DB_NAME = props.getProperty("db.name", "dermappoint");
            DB_USER = props.getProperty("db.user", "root");
            DB_PASSWORD = props.getProperty("db.password", "");
            DB_URL = props.getProperty("db.url",
                    "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");

            // Application
            MAX_AM_SLOTS = Integer.parseInt(props.getProperty("app.max_am_slots", "20"));
            MAX_PM_SLOTS = Integer.parseInt(props.getProperty("app.max_pm_slots", "20"));
            APPOINTMENT_REMINDER_HOURS = Integer.parseInt(props.getProperty("app.reminder_hours", "24"));
            CLINIC_NAME = props.getProperty("clinic.name", "DermaClinic");
            CLINIC_ADDRESS = props.getProperty("clinic.address", "123 Skin Care St, Dermatology City");
            CLINIC_PHONE = props.getProperty("clinic.phone", "09170000000");

            // Email
            SMTP_HOST = props.getProperty("email.smtp.host", "smtp.gmail.com");
            SMTP_PORT = props.getProperty("email.smtp.port", "587");
            SMTP_USER = props.getProperty("email.smtp.user", "");
            SMTP_PASSWORD = props.getProperty("email.smtp.password", "");

            // File Paths
            LOG_DIRECTORY = props.getProperty("paths.logs", "logs");
            EXPORT_DIRECTORY = props.getProperty("paths.exports", "exports");
            BACKUP_DIRECTORY = props.getProperty("paths.backups", "backups");

            Logger.log("Configuration loaded successfully from properties file");

        } catch (IOException e) {
            setDefaultValues();
            Logger.log("Config file not found. Using default values.");
        }
    }

    // ================= Set Default Values =================
    private static void setDefaultValues() {
        // Database
        DB_NAME = "dermappoint";
        DB_USER = "root";
        DB_PASSWORD = "";
        DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        // Application
        MAX_AM_SLOTS = 20;
        MAX_PM_SLOTS = 20;
        APPOINTMENT_REMINDER_HOURS = 24;
        CLINIC_NAME = "DermaClinic";
        CLINIC_ADDRESS = "123 Skin Care St, Dermatology City";
        CLINIC_PHONE = "09170000000";

        // Email
        SMTP_HOST = "smtp.gmail.com";
        SMTP_PORT = "587";
        SMTP_USER = "";
        SMTP_PASSWORD = "";

        // File Paths
        LOG_DIRECTORY = "logs";
        EXPORT_DIRECTORY = "exports";
        BACKUP_DIRECTORY = "backups";
    }

    // ================= Create Config Template =================
    public static void createConfigTemplate() {
        Properties props = new Properties();

        // Database
        props.setProperty("db.name", "dermappoint");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "");
        props.setProperty("db.url",
                "jdbc:mysql://localhost:3306/dermappoint?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");

        // Application
        props.setProperty("app.max_am_slots", "20");
        props.setProperty("app.max_pm_slots", "20");
        props.setProperty("app.reminder_hours", "24");
        props.setProperty("clinic.name", "DermaClinic");
        props.setProperty("clinic.address", "123 Skin Care St, Dermatology City");
        props.setProperty("clinic.phone", "09170000000");

        // Email
        props.setProperty("email.smtp.host", "smtp.gmail.com");
        props.setProperty("email.smtp.port", "587");
        props.setProperty("email.smtp.user", "");
        props.setProperty("email.smtp.password", "");

        // File Paths
        props.setProperty("paths.logs", "logs");
        props.setProperty("paths.exports", "exports");
        props.setProperty("paths.backups", "backups");

        try {
            Files.createDirectories(Paths.get("config"));
        } catch (IOException e) {
            Logger.logError("Failed to create config directory", e);
        }

        try (FileOutputStream fos = new FileOutputStream("config/dermappoint.properties")) {
            props.store(fos, "DermAppoint Configuration Template");
            Logger.log("Configuration template created at config/dermappoint.properties");
        } catch (IOException e) {
            Logger.logError("Failed to create configuration template", e);
        }
    }

    // ================= Get DB Connection URL =================
    public static String getConnectionString() {
        return DB_URL;
    }
}