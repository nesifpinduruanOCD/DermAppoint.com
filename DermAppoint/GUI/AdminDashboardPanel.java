package GUI;

import models.Admin;
import services.UserService;
import utils.ColorScheme;
import utils.FontLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * AdminDashboardPanel provides the main interface for Admin users.
 * It includes sidebar navigation, dashboard cards, and patient records table.
 */
public class AdminDashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private UserService userService;
    private Admin currentAdmin;

    // Sidebar buttons
    private JButton dashboardButton, managePatientsButton, manageStaffButton, profileButton, logoutButton;

    // ---------------- CONSTRUCTOR ----------------
    public AdminDashboardPanel(MainFrame mainFrame, Admin admin) {
        this.mainFrame = mainFrame;
        this.userService = mainFrame.getUserService();
        this.currentAdmin = admin;
        initializeUI();
    }

    // ---------------- SET ADMIN ----------------
    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }

    // ---------------- INITIALIZE UI ----------------
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

        JLabel welcomeLabel = new JLabel("Welcome, " + currentAdmin.getFullName());
        welcomeLabel.setFont(FontLoader.loadCustomFont(24, Font.BOLD));
        welcomeLabel.setForeground(ColorScheme.TEXT_DARK);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JLabel dateLabel = new JLabel("Today: " + java.time.LocalDate.now());
        dateLabel.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        dateLabel.setForeground(ColorScheme.TEXT_MEDIUM);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        mainContent.add(headerPanel, BorderLayout.NORTH);

        // Dashboard cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        cardsPanel.setBackground(ColorScheme.BACKGROUND);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        cardsPanel.add(createDashboardCard("Manage Patients", "View and manage patients"));
        cardsPanel.add(createDashboardCard("Manage Staff", "View and manage clinic staff"));
        cardsPanel.add(createDashboardCard("Reports", "Generate system reports"));

        mainContent.add(cardsPanel, BorderLayout.CENTER);

        // Table placeholder for patients
        String[][] data = {};
        String[] columns = { "ID", "Name", "Email", "Status" };
        JTable table = new JTable(new DefaultTableModel(data, columns));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Patient Records"));
        scrollPane.setPreferredSize(new Dimension(800, 300));
        mainContent.add(scrollPane, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);
    }

    // ---------------- SIDEBAR ----------------
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ColorScheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel clinicLabel = new JLabel("DermaClinic Admin");
        clinicLabel.setFont(FontLoader.loadCustomFont(20, Font.BOLD));
        clinicLabel.setForeground(ColorScheme.TEXT_LIGHT);
        clinicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(clinicLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        dashboardButton = createSidebarButton("Dashboard");
        managePatientsButton = createSidebarButton("Manage Patients");
        manageStaffButton = createSidebarButton("Manage Staff");
        profileButton = createSidebarButton("Profile");
        logoutButton = createSidebarButton("Logout");

        logoutButton.addActionListener(e -> {
            userService.logout();
            mainFrame.showWelcomeScreen();
        });

        sidebar.add(dashboardButton);
        sidebar.add(managePatientsButton);
        sidebar.add(manageStaffButton);
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

    // ---------------- DASHBOARD CARD ----------------
    private JPanel createDashboardCard(String title, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(ColorScheme.HOVER);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
            }
        });

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontLoader.loadCustomFont(18, Font.BOLD));
        titleLabel.setForeground(ColorScheme.TEXT_DARK);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(FontLoader.loadCustomFont(12, Font.PLAIN));
        descLabel.setForeground(ColorScheme.TEXT_MEDIUM);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(card.getBackground());
        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }
}