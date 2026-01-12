import db.DataBaseConnection;
import GUI.MainFrame;
import utils.Config;
import utils.Logger;

import javax.swing.*;

public class DermAppointMain {
    public static void main(String[] args) {
        // Initialize configuration
        Config.createConfigTemplate();

        // Initialize database
        try {
            DataBaseConnection.initializeDatabase();

            if (!DataBaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to database. Please check your configuration.\n" +
                    "Using in-memory storage instead.",
                    "Database Connection Error",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            Logger.logError("Failed to initialize database", e);
        }

        // Set skin-friendly look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            Logger.logError("Failed to set look and feel", e);
        }

        // Run the application
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            Logger.log("DermAppoint system started");

            // Add shutdown hook to close database connection
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DataBaseConnection.closeConnection();
                Logger.log("Application shutdown completed");
            }));
        });
    }
}
