package GUI;

import models.Appointment;
import models.Doctor;
import models.Patient;
import models.Service;
import models.TimeSlot;
import services.AppointmentServiceDB;
import services.DoctorServiceDB;
import services.ServicesServiceDB;
import services.NotificationService;
import services.UserService;
import utils.ColorScheme;
import utils.FontLoader;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class PatientDashBoardPanel extends JPanel {

    private MainFrame mainFrame;
    private UserService userService;
    private AppointmentServiceDB appointmentService;
    private DoctorServiceDB doctorService;
    private ServicesServiceDB servicesService;
    private NotificationService notificationService;

    public AppointmentServiceDB getAppointmentService() {
        return appointmentService;
    }

    public void setAppointmentService(AppointmentServiceDB appointmentService) {
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
        this.appointmentService = new AppointmentServiceDB();
        this.doctorService = new DoctorServiceDB();
        this.servicesService = new ServicesServiceDB();
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
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(ColorScheme.BACKGROUND);

        JLabel title = new JLabel("Book Appointment");
        title.setFont(FontLoader.loadCustomFont(20, Font.BOLD));
        title.setForeground(ColorScheme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ColorScheme.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JComboBox<Service> serviceCombo = new JComboBox<>();
        for (Service s : servicesService.getAllServices(true)) {
            serviceCombo.addItem(s);
        }
        serviceCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value != null ? value.getServiceName() : "");
            label.setOpaque(true);
            label.setBackground(isSelected ? ColorScheme.HOVER : Color.WHITE);
            label.setForeground(ColorScheme.TEXT_DARK);
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return label;
        });

        JTextField dateField = new JTextField(12);
        dateField.setToolTipText("YYYY-MM-DD");

        JComboBox<String> amPmCombo = new JComboBox<>(new String[] { "AM", "PM" });
        JComboBox<Doctor> doctorCombo = new JComboBox<>();
        doctorCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String text = "";
            if (value != null) {
                text = value.getName();
                if (value.getSpecialization() != null && !value.getSpecialization().isBlank()) {
                    text += " - " + value.getSpecialization();
                }
            }
            JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setBackground(isSelected ? ColorScheme.HOVER : Color.WHITE);
            label.setForeground(ColorScheme.TEXT_DARK);
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return label;
        });

        JComboBox<TimeSlot> timeCombo = new JComboBox<>();

        JButton loadAvailabilityBtn = new JButton("Load Availability");
        JButton bookBtn = new JButton("Book Appointment");

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Service:"), gbc);
        gbc.gridx = 1;
        form.add(serviceCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        form.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 1;
        form.add(amPmCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1;
        form.add(doctorCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1;
        form.add(timeCombo, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(loadAvailabilityBtn);
        actions.add(bookBtn);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(actions, gbc);

        panel.add(form, BorderLayout.CENTER);

        Runnable reloadDoctorsAndTimes = () -> {
            doctorCombo.removeAllItems();
            timeCombo.removeAllItems();

            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String slot = (String) amPmCombo.getSelectedItem();
            for (Doctor d : doctorService.getAvailableDoctors(date, slot)) {
                doctorCombo.addItem(d);
            }
        };

        Runnable reloadTimes = () -> {
            timeCombo.removeAllItems();
            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim());
            } catch (Exception ex) {
                return;
            }

            Doctor doc = (Doctor) doctorCombo.getSelectedItem();
            if (doc == null) {
                return;
            }

            String slot = (String) amPmCombo.getSelectedItem();
            for (TimeSlot ts : doctorService.getAvailableTimeSlots(doc.getDoctorId(), date)) {
                if (!ts.isAvailable()) {
                    continue;
                }
                if ("AM".equals(slot) && ts.getTime().compareTo("12:00") < 0) {
                    timeCombo.addItem(ts);
                } else if ("PM".equals(slot) && ts.getTime().compareTo("12:00") >= 0) {
                    timeCombo.addItem(ts);
                }
            }
        };

        loadAvailabilityBtn.addActionListener(e -> {
            reloadDoctorsAndTimes.run();
            reloadTimes.run();
        });
        doctorCombo.addActionListener(e -> reloadTimes.run());
        amPmCombo.addActionListener(e -> {
            reloadDoctorsAndTimes.run();
            reloadTimes.run();
        });

        bookBtn.addActionListener(e -> {
            Service svc = (Service) serviceCombo.getSelectedItem();
            if (svc == null) {
                JOptionPane.showMessageDialog(this, "No services available.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Doctor doc = (Doctor) doctorCombo.getSelectedItem();
            if (doc == null) {
                JOptionPane.showMessageDialog(this, "Select a doctor.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            TimeSlot ts = (TimeSlot) timeCombo.getSelectedItem();
            if (ts == null) {
                JOptionPane.showMessageDialog(this, "Select a time.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date. Use YYYY-MM-DD", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalTime time;
            try {
                time = LocalTime.parse(ts.getTime() + ":00");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid time selected.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Appointment created = appointmentService.createAppointment(currentPatient, svc.getServiceId(), LocalDateTime.of(date, time), doc.getDoctorId());
                if (created == null) {
                    JOptionPane.showMessageDialog(this, "Failed to book appointment.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                notificationService.sendAppointmentConfirmation(currentPatient.getEmail(),
                        "Service: " + svc.getServiceName() + "\nDoctor: " + doc.getName() + "\nDate/Time: " + created.getAppointmentDateTime());
                JOptionPane.showMessageDialog(this, "Appointment booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                handleSidebarClick("My Appointments");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createMyAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(ColorScheme.BACKGROUND);

        JLabel title = new JLabel("My Appointments");
        title.setFont(FontLoader.loadCustomFont(20, Font.BOLD));
        title.setForeground(ColorScheme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        String[] columns = { "Appointment ID", "Service", "Doctor", "Date/Time", "Status" };
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Selected");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(refreshBtn);
        actions.add(cancelBtn);
        panel.add(actions, BorderLayout.SOUTH);

        Runnable reload = () -> {
            model.setRowCount(0);

            java.util.Map<String, String> serviceNameById = new java.util.HashMap<>();
            for (Service s : servicesService.getAllServices(false)) {
                serviceNameById.put(s.getServiceId(), s.getServiceName());
            }

            java.util.Map<String, String> doctorNameById = new java.util.HashMap<>();
            for (Doctor d : doctorService.getAllDoctors(false)) {
                doctorNameById.put(d.getDoctorId(), d.getName());
            }

            for (Appointment a : appointmentService.findByPatientId(currentPatient.getUserId())) {
                String dt = a.getAppointmentDateTime() != null ? a.getAppointmentDateTime().toString() : "";
                model.addRow(new Object[] {
                        a.getAppointmentId(),
                        serviceNameById.getOrDefault(a.getServiceId(), a.getServiceId()),
                        doctorNameById.getOrDefault(a.getDoctorId(), a.getDoctorId()),
                        dt,
                        a.getStatus()
                });
            }
        };

        refreshBtn.addActionListener(e -> reload.run());

        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select an appointment first.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String appointmentId = (String) model.getValueAt(row, 0);
            String status = (String) model.getValueAt(row, 4);
            if (status != null && status.equalsIgnoreCase("CANCELLED")) {
                JOptionPane.showMessageDialog(this, "Appointment is already cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Cancel selected appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            Appointment target = null;
            for (Appointment a : appointmentService.findByPatientId(currentPatient.getUserId())) {
                if (appointmentId != null && appointmentId.equals(a.getAppointmentId())) {
                    target = a;
                    break;
                }
            }

            if (target == null) {
                JOptionPane.showMessageDialog(this, "Appointment not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = appointmentService.cancelAppointment(target, currentPatient);
            if (ok) {
                notificationService.sendCancellationNotice(currentPatient.getEmail(), "Appointment ID: " + appointmentId);
                reload.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel appointment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        SwingUtilities.invokeLater(reload);
        return panel;
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