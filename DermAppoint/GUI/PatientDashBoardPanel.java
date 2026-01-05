package GUI;

import models.Patient;
import services.AppointmentService;
import services.NotificationService;
import services.UserService;
import utils.ColorScheme;
import utils.FontLoader;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PatientDashBoardPanel extends JPanel {

    private MainFrame mainFrame;
    private UserService userService;
    private AppointmentService appointmentService;
    private NotificationService notificationService;
    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    public void setAppointmentService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private Patient currentPatient;

    private JPanel mainContentPanel;
    private Map<String, JButton> sidebarButtons = new HashMap<>();

    public PatientDashBoardPanel(MainFrame mainFrame, Patient patient) {
        this.mainFrame = mainFrame;
        this.userService = mainFrame.getUserService();
        this.appointmentService = new AppointmentService();
        this.notificationService = new NotificationService();
        this.currentPatient = patient;

        initializeUI();
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Main content
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(ColorScheme.BACKGROUND);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainContentPanel, BorderLayout.CENTER);

        showDashboard("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ColorScheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel clinicLabel = new JLabel("DermaClinic");
        clinicLabel.setFont(FontLoader.loadCustomFont(20, Font.BOLD));
        clinicLabel.setForeground(ColorScheme.TEXT_LIGHT);
        clinicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(clinicLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        String[] menuItems = { "Dashboard", "Book Appointment", "My Appointments",
                "Book for Family", "Services", "Profile", "Settings", "Logout" };

        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
            button.setForeground(ColorScheme.TEXT_LIGHT);
            button.setBackground(ColorScheme.SIDEBAR);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);

            button.addActionListener(e -> handleSidebarClick(item));

            sidebar.add(button);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
            sidebarButtons.put(item, button);
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void handleSidebarClick(String menuItem) {
        // Logout
        if ("Logout".equals(menuItem)) {
            userService.logout();
            mainFrame.showWelcomeScreen();
            return;
        }

        // Highlight active button
        sidebarButtons.forEach((name, btn) -> {
            if (name.equals(menuItem)) {
                btn.setBackground(ColorScheme.ACCENT);
                btn.setForeground(ColorScheme.TEXT_LIGHT);
            } else {
                btn.setBackground(ColorScheme.SIDEBAR);
                btn.setForeground(ColorScheme.TEXT_LIGHT);
            }
        });

        showDashboard(menuItem);
    }

    private void showDashboard(String menuItem) {
        mainContentPanel.removeAll();

        switch (menuItem) {
            case "Dashboard":
                mainContentPanel.add(createDashboardHome(), BorderLayout.CENTER);
                break;
            case "Book Appointment":
                mainContentPanel.add(createBookAppointmentPanel(), BorderLayout.CENTER);
                break;
            case "My Appointments":
                mainContentPanel.add(createMyAppointmentsPanel(), BorderLayout.CENTER);
                break;
            case "Book for Family":
                mainContentPanel.add(createBookForFamilyPanel(), BorderLayout.CENTER);
                break;
            case "Services":
                mainContentPanel.add(createServicesPanel(), BorderLayout.CENTER);
                break;
            case "Profile":
                mainContentPanel.add(createProfilePanel(), BorderLayout.CENTER);
                break;
            case "Settings":
                mainContentPanel.add(createSettingsPanel(), BorderLayout.CENTER);
                break;
            default:
                mainContentPanel.add(new JLabel("Coming Soon"), BorderLayout.CENTER);
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // ---------------- DASHBOARD HOME ----------------
    private JPanel createDashboardHome() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorScheme.BACKGROUND);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.BACKGROUND);
        JLabel welcomeLabel = new JLabel("Welcome, " + currentPatient.getFullName());
        welcomeLabel.setFont(FontLoader.loadCustomFont(24, Font.BOLD));
        welcomeLabel.setForeground(ColorScheme.TEXT_DARK);
        header.add(welcomeLabel, BorderLayout.WEST);

        JLabel dateLabel = new JLabel("Today: " + java.time.LocalDate.now());
        dateLabel.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        dateLabel.setForeground(ColorScheme.TEXT_MEDIUM);
        header.add(dateLabel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Cards
        JPanel cards = new JPanel(new GridLayout(2, 2, 20, 20));
        cards.setBackground(ColorScheme.BACKGROUND);
        cards.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        cards.add(createClickableCard("Book Appointment", "Schedule a new consultation", "Book Appointment"));
        cards.add(createClickableCard("My Appointments", "View upcoming appointments", "My Appointments"));
        cards.add(createClickableCard("Book for Family", "Book for family members", "Book for Family"));
        cards.add(createClickableCard("Services", "View available services", "Services"));

        panel.add(cards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createClickableCard(String title, String description, String targetMenu) {
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

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleSidebarClick(targetMenu);
            }
        });

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(card.getBackground());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontLoader.loadCustomFont(18, Font.BOLD));
        titleLabel.setForeground(ColorScheme.TEXT_DARK);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(FontLoader.loadCustomFont(12, Font.PLAIN));
        descLabel.setForeground(ColorScheme.TEXT_MEDIUM);

        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // ---------------- PLACEHOLDER PANELS ----------------
    private JPanel createBookAppointmentPanel() {
        return createPlaceholderPanel("Book Appointment Panel");
    }

    private JPanel createMyAppointmentsPanel() {
        return createPlaceholderPanel("My Appointments Panel");
    }

    private JPanel createBookForFamilyPanel() {
        return createPlaceholderPanel("Book for Family Panel");
    }

    private JPanel createServicesPanel() {
        return createPlaceholderPanel("Services Panel");
    }

    private JPanel createProfilePanel() {
        return createPlaceholderPanel("Profile Panel");
    }

    private JPanel createSettingsPanel() {
        return createPlaceholderPanel("Settings Panel");
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColorScheme.BACKGROUND);
        JLabel label = new JLabel(text);
        label.setFont(FontLoader.loadCustomFont(20, Font.BOLD));
        label.setForeground(ColorScheme.TEXT_DARK);
        panel.add(label);
        return panel;
    }
}
