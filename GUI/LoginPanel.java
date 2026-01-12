package GUI;

import services.UserServicesDB;
import models.Admin;
import models.DoctorUser;
import models.Patient;
import models.Staff;
import utils.FontLoader;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserServicesDB userService;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private char defaultPasswordEchoChar;
    private JComboBox<String> accountTypeCombo;
    private JButton loginButton;
    private JButton backButton;
    private JLabel errorLabel;

    private JPanel contentPanel;

    // Color palette (matches SignUpPanel)
    private static final Color BACKGROUND_COLOR = new Color(255, 250, 245);
    private static final Color CARD_BG = new Color(255, 248, 240);
    private static final Color BORDER_COLOR = new Color(210, 180, 140);
    private static final Color TEXT_COLOR = new Color(60, 40, 30);
    private static final Color ERROR_COLOR = new Color(180, 0, 0);
    private static final Color BUTTON_BG = new Color(140, 100, 80);
    private static final Color BUTTON_HOVER = new Color(160, 120, 90);

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserServicesDB();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        initializeUI();
    }

    private void initializeUI() {
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Title
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(FontLoader.loadCustomFont(32, Font.BOLD));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Card Panel
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setAlignmentX(CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input fields
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        defaultPasswordEchoChar = passwordField.getEchoChar();

        accountTypeCombo = new JComboBox<>(new String[]{"Patient", "Doctor", "Admin", "Staff"});
        accountTypeCombo.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        accountTypeCombo.setForeground(TEXT_COLOR);
        accountTypeCombo.setBackground(Color.WHITE);

        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.setFont(FontLoader.loadCustomFont(12, Font.PLAIN));
        showPasswordCheckBox.setForeground(TEXT_COLOR);
        showPasswordCheckBox.addActionListener(e -> {
            char echo = showPasswordCheckBox.isSelected() ? (char) 0 : defaultPasswordEchoChar;
            passwordField.setEchoChar(echo);
        });

        // Add input fields with labels
        addLabelField(card, gbc, 0, "Email:", emailField);
        addLabelField(card, gbc, 1, "Password:", passwordField);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(showPasswordCheckBox, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel typeLabel = new JLabel("Account Type:");
        typeLabel.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        typeLabel.setForeground(TEXT_COLOR);
        card.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(accountTypeCombo, gbc);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(errorLabel, gbc);

        contentPanel.add(card);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        // Create buttons with ActionListener lambdas
        loginButton = createCTAButton("Login", e -> performLogin());
        backButton = createCTAButton("Back", e -> mainFrame.showWelcomeScreen());

        buttonPanel.add(loginButton);
        buttonPanel.add(backButton);

        contentPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void addLabelField(JPanel panel, GridBagConstraints gbc, int y, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        label.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        label.setForeground(TEXT_COLOR);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        if (field instanceof JTextComponent) {
            ((JTextComponent) field).setForeground(TEXT_COLOR);
            ((JTextComponent) field).setBackground(Color.WHITE);
        }
        panel.add(field, gbc);
    }

    // Refactored createCTAButton to accept ActionListener lambda
    private JButton createCTAButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(FontLoader.loadCustomFont(16, Font.BOLD));
        button.setForeground(Color.BLACK);
        button.setBackground(BUTTON_BG);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(action);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_BG);
            }
        });

        return button;
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String accountType = (String) accountTypeCombo.getSelectedItem();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email and password are required.");
            return;
        }

        try {
            if ("Admin".equalsIgnoreCase(accountType)) {
                Admin admin = userService.loginAdmin(email, password);
                if (admin != null) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    mainFrame.showAdminDashboard(admin);
                    return;
                }
            } else if ("Doctor".equalsIgnoreCase(accountType)) {
                DoctorUser doctor = userService.loginDoctor(email, password);
                if (doctor != null) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    mainFrame.showDoctorDashboard(doctor);
                    return;
                }
            } else if ("Staff".equalsIgnoreCase(accountType)) {
                Staff staff = userService.loginStaff(email, password);
                if (staff != null) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    mainFrame.showStaffDashboard(staff);
                    return;
                }
            } else {
                Patient patient = userService.loginPatient(email, password);
                if (patient != null) {
                    userService.setCurrentUser(patient);
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    mainFrame.showPatientDashboard(patient);
                    return;
                }
            }

            errorLabel.setText("Invalid email or password.");
        } catch (Exception ex) {
            errorLabel.setText("Login failed: " + ex.getMessage());
        }
    }
}
