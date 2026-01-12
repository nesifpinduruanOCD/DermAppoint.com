package GUI;

import models.DoctorUser;
import models.Appointment;
import models.Doctor;
import models.Service;
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

public class DoctorDashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private UserService userService;
    private AppointmentServiceDB appointmentService;
    private DoctorServiceDB doctorService;
    private ServicesServiceDB servicesService;
    private DoctorUser currentDoctor;

    private JButton dashboardButton, profileButton, logoutButton;

    private JPanel mainContent;

    public DoctorDashboardPanel(MainFrame mainFrame, DoctorUser doctor) {
        this.mainFrame = mainFrame;
        this.userService = mainFrame.getUserService();
        this.appointmentService = new AppointmentServiceDB();
        this.doctorService = new DoctorServiceDB();
        this.servicesService = new ServicesServiceDB();
        this.currentDoctor = doctor;
        initializeUI();
    }

    public void setDoctor(DoctorUser doctor) {
        this.currentDoctor = doctor;
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.BACKGROUND);

        add(createSidebar(), BorderLayout.WEST);

        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(ColorScheme.BACKGROUND);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainContent, BorderLayout.CENTER);

        dashboardButton.addActionListener(e -> showAppointments());
        profileButton.addActionListener(e -> showProfile());

        showAppointments();
    }

    private void showHeader(String title) {
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

    private void showAppointments() {
        mainContent.removeAll();
        showHeader("Doctor Dashboard");

        String[] columns = { "Appointment ID", "Patient", "Service", "Date/Time", "Status" };
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

            String doctorId = currentDoctor.getUserId();
            for (Appointment a : appointmentService.findByDoctorId(doctorId)) {
                String dt = a.getAppointmentDateTime() != null ? a.getAppointmentDateTime().toString() : "";
                model.addRow(new Object[] {
                        a.getAppointmentId(),
                        a.getPatientName(),
                        serviceNameById.getOrDefault(a.getServiceId(), a.getServiceId()),
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

    private void showProfile() {
        mainContent.removeAll();
        showHeader("Doctor Profile");

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createProfileRow("Name:", currentDoctor.getFullName()));
        panel.add(createProfileRow("Email:", currentDoctor.getEmail()));
        panel.add(createProfileRow("Phone:", currentDoctor.getPhone()));
        panel.add(createProfileRow("Address:", currentDoctor.getAddress()));

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

        JLabel clinicLabel = new JLabel("DermaClinic Doctor");
        clinicLabel.setFont(FontLoader.loadCustomFont(20, Font.BOLD));
        clinicLabel.setForeground(ColorScheme.TEXT_LIGHT);
        clinicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(clinicLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        dashboardButton = createSidebarButton("Dashboard");
        profileButton = createSidebarButton("Profile");
        logoutButton = createSidebarButton("Logout");

        logoutButton.addActionListener(e -> {
            userService.logout();
            mainFrame.showWelcomeScreen();
        });

        sidebar.add(dashboardButton);
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
