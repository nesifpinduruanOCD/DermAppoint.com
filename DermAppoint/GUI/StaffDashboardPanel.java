package GUI;

import models.Staff;
import services.UserService;
import utils.ColorScheme;
import utils.FontLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StaffDashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private UserService userService;
    private Staff currentStaff;

    // Sidebar buttons
    private JButton dashboardButton, appointmentsButton, profileButton, logoutButton;

    // ---------------- CONSTRUCTOR ----------------
    public StaffDashboardPanel(MainFrame mainFrame, Staff staff) {
        this.mainFrame = mainFrame;
        this.userService = mainFrame.getUserService();
        this.currentStaff = staff;
        initializeUI();
    }

    // ---------------- SET STAFF ----------------
    public void setStaff(Staff staff) {
        this.currentStaff = staff;
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);

        add(createSidebar(), BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(ColorScheme.BACKGROUND);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.BACKGROUND);
        JLabel welcomeLabel = new JLabel("Welcome, " + currentStaff.getFullName());
        welcomeLabel.setFont(FontLoader.loadCustomFont(24, Font.BOLD));
        welcomeLabel.setForeground(ColorScheme.TEXT_DARK);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JLabel dateLabel = new JLabel("Today: " + java.time.LocalDate.now());
        dateLabel.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        dateLabel.setForeground(ColorScheme.TEXT_MEDIUM);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        mainContent.add(headerPanel, BorderLayout.NORTH);

        // Placeholder appointments table
        String[][] data = {};
        String[] columns = { "ID", "Patient", "Date", "Time", "Status" };
        JTable table = new JTable(new DefaultTableModel(data, columns));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Appointments"));
        scrollPane.setPreferredSize(new Dimension(800, 300));
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ColorScheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel clinicLabel = new JLabel("DermaClinic Staff");
        clinicLabel.setFont(FontLoader.loadCustomFont(20, Font.BOLD));
        clinicLabel.setForeground(ColorScheme.TEXT_LIGHT);
        clinicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(clinicLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        dashboardButton = createSidebarButton("Dashboard");
        appointmentsButton = createSidebarButton("Appointments");
        profileButton = createSidebarButton("Profile");
        logoutButton = createSidebarButton("Logout");

        logoutButton.addActionListener(e -> {
            userService.logout();
            mainFrame.showWelcomeScreen();
        });

        sidebar.add(dashboardButton);
        sidebar.add(appointmentsButton);
        sidebar.add(profileButton);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutButton);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        button.setForeground(ColorScheme.TEXT_LIGHT);
        button.setBackground(ColorScheme.SIDEBAR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ColorScheme.HOVER);
                button.setForeground(ColorScheme.TEXT_DARK);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ColorScheme.SIDEBAR);
                button.setForeground(ColorScheme.TEXT_LIGHT);
            }
        });

        return button;
    }
}
