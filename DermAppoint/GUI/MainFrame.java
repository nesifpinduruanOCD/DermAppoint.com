package GUI;

import models.Admin;
import models.Patient;
import models.Staff;
import services.UserService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private UserService userService;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        super("DermAppoint - Dermatology Management System");
        this.userService = new UserService();
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        
        // Set application icon
        try {
            // If you have an icon image, you can set it here
            // setIconImage(new ImageIcon("path/to/icon.png").getImage());
        } catch (Exception e) {
            // Use default if custom icon fails
        }
        
        // Use CardLayout for smooth transitions between panels
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel);
        
        // Add all panels to CardLayout
        initializePanels();
        
        // Show welcome screen by default
        cardLayout.show(contentPanel, "welcome");
    }
    
    private void initializePanels() {
        // Add all panels to the CardLayout
        contentPanel.add(createWelcomePanel(), "welcome");
        contentPanel.add(new LoginPanel(this), "login");
        contentPanel.add(new SignUpPanel(this), "signup");
        
        // Placeholder panels for dashboards (will be created when needed)
        contentPanel.add(createLoadingPanel(), "loading");
    }
    
    private JPanel createWelcomePanel() {
        return new WelcomePanel(this);
    }
    
    private JPanel createLoadingPanel() {
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(new Color(245, 245, 249));
        
        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        loadingLabel.setForeground(new Color(74, 144, 226));
        
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        
        return loadingPanel;
    }

    // ---------------- NAVIGATION METHODS ----------------
    public void showWelcomeScreen() {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(contentPanel, "welcome");
            setTitle("DermAppoint - Welcome");
        });
    }

    public void showLoginScreen() {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(contentPanel, "login");
            setTitle("DermAppoint - Login");
        });
    }

    public void showSignUpScreen() {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(contentPanel, "signup");
            setTitle("DermAppoint - Sign Up");
        });
    }

    // ---------------- DASHBOARDS ----------------
    public void showPatientDashboard(Patient patient) {
        SwingUtilities.invokeLater(() -> {
            try {
                PatientDashBoardPanel dashboard = new PatientDashBoardPanel(this, patient);
                contentPanel.add(dashboard, "patient_dashboard_" + patient.getUsername());
                cardLayout.show(contentPanel, "patient_dashboard_" + patient.getUsername());
                setTitle("DermAppoint - Patient Dashboard");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading dashboard: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                showLoginScreen();
            }
        });
    }

    public void showAdminDashboard(Admin admin) {
        SwingUtilities.invokeLater(() -> {
            try {
                AdminDashboardPanel dashboard = new AdminDashboardPanel(this, admin);
                contentPanel.add(dashboard, "admin_dashboard_" + admin.getUsername());
                cardLayout.show(contentPanel, "admin_dashboard_" + admin.getUsername());
                setTitle("DermAppoint - Admin Dashboard");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading dashboard: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                showLoginScreen();
            }
        });
    }

    public void showStaffDashboard(Staff staff) {
        SwingUtilities.invokeLater(() -> {
            try {
                StaffDashboardPanel dashboard = new StaffDashboardPanel(this, staff);
                contentPanel.add(dashboard, "staff_dashboard_" + staff.getUsername());
                cardLayout.show(contentPanel, "staff_dashboard_" + staff.getUsername());
                setTitle("DermAppoint - Staff Dashboard");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error loading dashboard: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                showLoginScreen();
            }
        });
    }
    
    public void showLoadingScreen() {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(contentPanel, "loading");
        });
    }
    
    public void logout() {
        // Remove all dashboard panels (except welcome, login, signup)
        Component[] components = contentPanel.getComponents();
        for (Component comp : components) {
            String name = ((JPanel)comp).getName();
            if (name != null && (name.startsWith("patient_dashboard_") || 
                                 name.startsWith("admin_dashboard_") || 
                                 name.startsWith("staff_dashboard_"))) {
                contentPanel.remove(comp);
            }
        }
        
        // Show welcome screen
        showWelcomeScreen();
        
        // Optional: Clear any session data
        // userService.logout();
    }

    // ---------------- SERVICES ----------------
    public UserService getUserService() {
        return userService;
    }
    
    // ---------------- UTILITY METHODS ----------------
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void showSuccess(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    public void showInfo(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    public boolean showConfirm(String message) {
        int result = JOptionPane.showConfirmDialog(this, message, "Confirm", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            
            // Center the frame on screen
            frame.setLocationRelativeTo(null);
            
            // Make frame visible
            frame.setVisible(true);
            
            // Optional: Add window listener for cleanup
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    // Perform cleanup if needed
                    // frame.getUserService().cleanup();
                }
            });
        });
    }
}