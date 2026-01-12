package GUI;

import models.Patient;
import services.UserServicesDB;
import utils.FontLoader;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SignUpPanel extends JPanel {

    private final MainFrame mainFrame;
    private final UserServicesDB userService;

    private JTextField firstNameField;
    private JTextField middleNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPasswordCheckBox;
    private char defaultPasswordEchoChar;
    private JTextField birthDateField;
    private JTextArea addressArea;
    private JComboBox<String> roleComboBox;
    private JButton signUpButton;
    private JButton backButton;
    private JLabel errorLabel;

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Design palette
    private static final Color BACKGROUND_COLOR = new Color(255, 250, 245);
    private static final Color CARD_BG = new Color(255, 248, 240);
    private static final Color BORDER_COLOR = new Color(210, 180, 140);
    private static final Color TEXT_COLOR = new Color(60, 40, 30);
    private static final Color ERROR_COLOR = new Color(180, 0, 0);
    private static final Color BUTTON_BG = new Color(140, 100, 80);
    private static final Color BUTTON_HOVER = new Color(160, 120, 90);

    private BufferedImage texture;
    private Random random = new Random();
    private boolean textureInitialized = false;

    private JPanel contentPanel;

    public SignUpPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserServicesDB();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        initializeUI();
        setupResponsiveBehavior();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!textureInitialized || texture == null || texture.getWidth() != getWidth() || texture.getHeight() != getHeight()) {
            createTexture(getWidth(), getHeight());
        }

        if (texture != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
            g2d.drawImage(texture, 0, 0, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private void createTexture(int width, int height) {
        if (width <= 0 || height <= 0) return;

        texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();

        g2d.setColor(new Color(255, 250, 245, 10));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(200, 180, 160, 3));
        for (int i = 0; i < width * height / 200; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(2) + 1;
            g2d.fillOval(x, y, size, size);
        }

        g2d.dispose();
        textureInitialized = true;
    }

    private void initializeUI() {
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Title
        JLabel titleLabel = new JLabel("Create Your Account");
        titleLabel.setFont(FontLoader.loadCustomFont(32, Font.BOLD));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Form Card
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
        firstNameField = new JTextField(20);
        middleNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        defaultPasswordEchoChar = passwordField.getEchoChar();
        birthDateField = new JTextField(20);
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        addLabelField(card, gbc, 0, "First Name:", firstNameField);
        addLabelField(card, gbc, 1, "Middle Name:", middleNameField);
        addLabelField(card, gbc, 2, "Last Name:", lastNameField);
        addLabelField(card, gbc, 3, "Email:", emailField);
        addLabelField(card, gbc, 4, "Phone:", phoneField);
        addLabelField(card, gbc, 5, "Password:", passwordField);
        addLabelField(card, gbc, 6, "Confirm Password:", confirmPasswordField);

        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.setFont(FontLoader.loadCustomFont(12, Font.PLAIN));
        showPasswordCheckBox.setForeground(TEXT_COLOR);
        showPasswordCheckBox.addActionListener(e -> {
            char echo = showPasswordCheckBox.isSelected() ? (char) 0 : defaultPasswordEchoChar;
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(showPasswordCheckBox, gbc);
        gbc.gridwidth = 1;

        addLabelField(card, gbc, 8, "Birth Date (MM/dd/yyyy):", birthDateField);
        addLabelField(card, gbc, 9, "Address:", new JScrollPane(addressArea));

        // Role selection
        roleComboBox = new JComboBox<>(new String[]{"Patient", "Doctor", "Staff"});
        roleComboBox.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        roleComboBox.setForeground(TEXT_COLOR);
        roleComboBox.setBackground(Color.WHITE);
        addLabelField(card, gbc, 10, "Role:", roleComboBox);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 11; // updated index
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(errorLabel, gbc);

        contentPanel.add(card);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        signUpButton = createCTAButton("Sign Up", e -> performSignUp());
        backButton = createCTAButton("Back", e -> mainFrame.showWelcomeScreen());

        buttonPanel.add(signUpButton);
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

    private void performSignUp() {
        String firstName = firstNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String address = addressArea.getText().trim();
        String birth = birthDateField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("First Name, Last Name, Email, Password, and Confirm Password are required.");
            return;
        }

        if (!ValidationUtils.validateName(firstName) || !ValidationUtils.validateName(lastName)
                || (!middleName.isEmpty() && !ValidationUtils.validateName(middleName))) {
            errorLabel.setText("Please enter a valid name (letters and common punctuation only). ");
            return;
        }

        String fullName;
        if (middleName.isEmpty()) {
            fullName = firstName + " " + lastName;
        } else {
            fullName = firstName + " " + middleName + " " + lastName;
        }

        if (!ValidationUtils.validatePassword(password)) {
            errorLabel.setText("Password must be at least 6 characters and include 1 uppercase and 1 special character.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        LocalDate dob = null;
        if (!birth.isEmpty()) {
            try {
                dob = LocalDate.parse(birth, DISPLAY_FMT);
            } catch (Exception e) {
                errorLabel.setText("Invalid date format (MM/dd/yyyy)");
                return;
            }
        }

        Patient p = userService.registerPatient(fullName, email, phone, password, address, dob, role);

        if (p != null) {
            JOptionPane.showMessageDialog(this, "Account created successfully!");
            mainFrame.showLoginScreen();
        } else {
            if (userService.loginPatient(email, password) != null
                    || userService.loginStaff(email, password) != null
                    || userService.loginAdmin(email, password) != null) {
                errorLabel.setText("Registration failed. Email may already exist.");
            } else {
                errorLabel.setText("Registration failed. Please check your details and database connection.");
            }
        }
    }

    private JButton createCTAButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(FontLoader.loadCustomFont(16, Font.BOLD));
        button.setForeground(Color.BLACK);
        button.setBackground(BUTTON_BG);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(action);

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

    private void setupResponsiveBehavior() {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                textureInitialized = false;
                revalidate();
                repaint();
            }
        });
    }
}
