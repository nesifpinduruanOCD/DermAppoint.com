package GUI;

import services.UserServicesDB;
import utils.FontLoader;
import utils.ValidationUtils;
import db.DataBaseConnection;
import models.Patient;
import models.Admin;
import models.User;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserServicesDB userService;

    private static final Color PRIMARY_COLOR = new Color(139, 69, 19);
    private static final Color BORDER_COLOR = new Color(210, 180, 140);
    private static final Color FORM_BG = new Color(255, 228, 196);
    private static final Color TEXT_DARK = new Color(60, 30, 10);
    private static final Color ACCENT_COLOR = new Color(165, 42, 42);
    private static final Color SUCCESS_COLOR = new Color(34, 139, 34);
    private static final Color BACKGROUND_COLOR = new Color(253, 245, 230);

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckbox;
    private JButton loginButton;
    private JLabel errorLabel;

    private char defaultEchoChar;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserServicesDB();
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        initializeUI();
        testDatabaseConnection();
    }

    private void initializeUI() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BACKGROUND_COLOR);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(FORM_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        JLabel title = new JLabel("Login");
        title.setFont(FontLoader.loadCustomFont(26, Font.BOLD));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = new JTextField();
        passwordField = new JPasswordField();
        defaultEchoChar = passwordField.getEchoChar();

        styleField(emailField);
        styleField(passwordField);

        showPasswordCheckbox = new JCheckBox("Show password");
        showPasswordCheckbox.setBackground(FORM_BG);
        showPasswordCheckbox.addActionListener(e ->
                passwordField.setEchoChar(
                        showPasswordCheckbox.isSelected() ? (char) 0 : defaultEchoChar
                )
        );

        loginButton = new JButton("Log In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ACCENT_COLOR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        formPanel.add(title);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);
        formPanel.add(showPasswordCheckbox);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(errorLabel);

        container.add(formPanel);
        add(container, BorderLayout.CENTER);
    }

    private void styleField(JTextField field) {
        field.setMaximumSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
    }

    // ================= LOGIN LOGIC =================
    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password required");
            return;
        }

        if (!ValidationUtils.validateEmail(email)) {
            showError("Invalid email format");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        new SwingWorker<User, Void>() {

            @Override
            protected User doInBackground() {
                return userService.loginAndGetUser(email, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();

                    if (user == null) {
                        showError("Invalid email or password");
                        return;
                    }

                    userService.setCurrentUser(user);

                    // ✅ ROLE-BASED NAVIGATION (NO instanceof error)
                    switch (user.getRole()) {
                        case "PATIENT" -> mainFrame.showPatientDashboard((Patient) user);
                        case "ADMIN" -> mainFrame.showAdminDashboard((Admin) user);
                        default -> showError("Unknown user role");
                    }

                    clearForm();
                    showSuccess("Login successful");

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Login failed");
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Log In");
                }
            }
        }.execute();
    }

    // ================= HELPERS =================
    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(ACCENT_COLOR);
    }

    private void showSuccess(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(SUCCESS_COLOR);
    }

    private void clearForm() {
        emailField.setText("");
        passwordField.setText("");
        showPasswordCheckbox.setSelected(false);
        passwordField.setEchoChar(defaultEchoChar);
        errorLabel.setText(" ");
    }

    // ================= DB CHECK =================
    private void testDatabaseConnection() {
        new Thread(() -> {
            if (!DataBaseConnection.testConnection()) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                this,
                                "Database connection failed",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE
                        )
                );
            }
        }).start();
    }
}
