package GUI;

import models.Appointment;
import models.Doctor;
import models.Service;
import models.Staff;
import services.AppointmentServiceDB;
import services.DoctorServiceDB;
import services.ServicesServiceDB;
import services.UserService;
import utils.ColorScheme;
import utils.FontLoader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StaffDashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private UserService userService;
    private AppointmentServiceDB appointmentService;
    private DoctorServiceDB doctorService;
    private ServicesServiceDB servicesService;
    private Staff currentStaff;

    // Sidebar buttons
    private JButton dashboardButton, appointmentsButton, profileButton, logoutButton;

    // ---------------- CONSTRUCTOR ----------------
    public StaffDashboardPanel(MainFrame mainFrame, Staff staff) {
        this.mainFrame = mainFrame;
        this.userService = mainFrame.getUserService();
        this.appointmentService = new AppointmentServiceDB();
        this.doctorService = new DoctorServiceDB();
        this.servicesService = new ServicesServiceDB();
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
        add(mainContent, BorderLayout.CENTER);

        dashboardButton.addActionListener(e -> showDashboard(mainContent));
        appointmentsButton.addActionListener(e -> showAppointments(mainContent));
        profileButton.addActionListener(e -> showProfile(mainContent));

        showDashboard(mainContent);
    }

    private void showHeader(JPanel mainContent, String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.BACKGROUND);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontLoader.loadCustomFont(24, Font.BOLD));
        titleLabel.setForeground(ColorScheme.TEXT_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel dateLabel = new JLabel("Today: " + java.time.LocalDate.now());
        dateLabel.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        dateLabel.setForeground(ColorScheme.TEXT_MEDIUM);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        mainContent.add(headerPanel, BorderLayout.NORTH);
    }

    private void showDashboard(JPanel mainContent) {
        mainContent.removeAll();
        showHeader(mainContent, "Staff Dashboard");

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, " + currentStaff.getFullName());
        welcome.setFont(FontLoader.loadCustomFont(18, Font.PLAIN));
        welcome.setForeground(ColorScheme.TEXT_MEDIUM);
        center.add(welcome, BorderLayout.NORTH);

        JButton manageServicesBtn = new JButton("Manage Services");
        manageServicesBtn.addActionListener(e -> showServices(mainContent));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(manageServicesBtn);

        center.add(actions, BorderLayout.CENTER);
        mainContent.add(center, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void showAppointments(JPanel mainContent) {
        mainContent.removeAll();
        showHeader(mainContent, "Appointments");

        String[] columns = { "Appointment ID", "Patient", "Service", "Doctor", "Date/Time", "Status" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        mainContent.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve");
        JButton completeBtn = new JButton("Complete");
        JButton cancelBtn = new JButton("Cancel");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(refreshBtn);
        actions.add(approveBtn);
        actions.add(completeBtn);
        actions.add(cancelBtn);
        mainContent.add(actions, BorderLayout.SOUTH);

        Runnable reload = () -> {
            model.setRowCount(0);

            Map<String, String> serviceNameById = new HashMap<>();
            for (Service s : servicesService.getAllServices(false)) {
                serviceNameById.put(s.getServiceId(), s.getServiceName());
            }

            Map<String, String> doctorNameById = new HashMap<>();
            for (Doctor d : doctorService.getAllDoctors(false)) {
                doctorNameById.put(d.getDoctorId(), d.getName());
            }

            for (Appointment a : appointmentService.findAll()) {
                String dt = a.getAppointmentDateTime() != null ? a.getAppointmentDateTime().toString() : "";
                model.addRow(new Object[] {
                        a.getAppointmentId(),
                        a.getPatientName(),
                        serviceNameById.getOrDefault(a.getServiceId(), a.getServiceId()),
                        doctorNameById.getOrDefault(a.getDoctorId(), a.getDoctorId()),
                        dt,
                        a.getStatus()
                });
            }
        };

        refreshBtn.addActionListener(e -> reload.run());
        approveBtn.addActionListener(e -> updateSelectedAppointmentStatus(table, model, "APPROVED", reload));
        completeBtn.addActionListener(e -> updateSelectedAppointmentStatus(table, model, "COMPLETED", reload));
        cancelBtn.addActionListener(e -> updateSelectedAppointmentStatus(table, model, "CANCELLED", reload));

        SwingUtilities.invokeLater(reload);

        revalidate();
        repaint();
    }

    private void updateSelectedAppointmentStatus(JTable table, DefaultTableModel model, String status, Runnable reload) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an appointment first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Set selected appointment to " + status + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean ok = appointmentService.updateAppointmentStatus(appointmentId, status);
        if (ok) {
            reload.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update appointment.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showServices(JPanel mainContent) {
        mainContent.removeAll();
        showHeader(mainContent, "Services");

        String[] columns = { "Service ID", "Name", "Price", "Duration", "Active" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        mainContent.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add Service");
        JButton toggleBtn = new JButton("Toggle Active");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(refreshBtn);
        actions.add(addBtn);
        actions.add(toggleBtn);
        mainContent.add(actions, BorderLayout.SOUTH);

        Runnable reload = () -> {
            model.setRowCount(0);
            for (Service s : servicesService.getAllServices(false)) {
                model.addRow(new Object[] {
                        s.getServiceId(),
                        s.getServiceName(),
                        s.getPrice(),
                        s.getDurationMinutes(),
                        s.isActive()
                });
            }
        };

        refreshBtn.addActionListener(e -> reload.run());

        addBtn.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField priceField = new JTextField();
            JTextField durationField = new JTextField();
            JTextField prepField = new JTextField();
            JTextArea descArea = new JTextArea(4, 20);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);

            JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
            form.add(new JLabel("Service Name:"));
            form.add(nameField);
            form.add(new JLabel("Description:"));
            form.add(new JScrollPane(descArea));
            form.add(new JLabel("Price:"));
            form.add(priceField);
            form.add(new JLabel("Duration (minutes):"));
            form.add(durationField);
            form.add(new JLabel("Required Preparation:"));
            form.add(prepField);

            int result = JOptionPane.showConfirmDialog(this, form, "Add Service", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();
            String prep = prepField.getText().trim();
            double price;
            int duration;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                duration = Integer.parseInt(durationField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid price or duration.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Service created = servicesService.addService(name, desc, price, duration, prep);
            if (created != null) {
                reload.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add service.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        toggleBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a service first.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String serviceId = (String) model.getValueAt(row, 0);
            boolean active = Boolean.TRUE.equals(model.getValueAt(row, 4));
            boolean ok = servicesService.toggleServiceStatus(serviceId, !active);
            if (ok) {
                reload.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update service.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        SwingUtilities.invokeLater(reload);

        revalidate();
        repaint();
    }

    private void showProfile(JPanel mainContent) {
        mainContent.removeAll();
        showHeader(mainContent, "Staff Profile");

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createProfileRow("Name:", currentStaff.getFullName()));
        panel.add(createProfileRow("Email:", currentStaff.getEmail()));
        panel.add(createProfileRow("Phone:", currentStaff.getPhone()));
        panel.add(createProfileRow("Address:", currentStaff.getAddress()));

        mainContent.add(panel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createProfileRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JLabel l = new JLabel(label);
        l.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        l.setForeground(ColorScheme.TEXT_DARK);

        JLabel v = new JLabel(value == null ? "" : value);
        v.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        v.setForeground(ColorScheme.TEXT_MEDIUM);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        return row;
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