package GUI;

import services.UserServicesDB;
import utils.*;
import models.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SignUpPanel extends JPanel {

    private MainFrame mainFrame;
    private UserServicesDB userService;

    // Brown and skin tone color scheme
    private static final Color PRIMARY_COLOR = new Color(139, 69, 19); // Saddle Brown
    private static final Color SECONDARY_COLOR = new Color(245, 222, 179); // Wheat (light skin tone)
    private static final Color TEXT_DARK = new Color(60, 30, 10); // Dark brown
    private static final Color TEXT_LIGHT = new Color(139, 90, 43); // Medium brown
    private static final Color ACCENT_COLOR = new Color(165, 42, 42); // Brown red
    private static final Color BORDER_COLOR = new Color(210, 180, 140); // Tan
    private static final Color SUCCESS_COLOR = new Color(34, 139, 34); // Forest green
    private static final Color SEPARATOR_COLOR = new Color(230, 190, 150); // Light tan
    private static final Color BUTTON_COLOR = new Color(160, 82, 45); // Sienna
    private static final Color LINK_COLOR = new Color(139, 69, 19); // Saddle brown
    
    // Additional skin tone variations
    private static final Color LIGHT_SKIN = new Color(255, 228, 196); // Bisque
    private static final Color MEDIUM_SKIN = new Color(222, 184, 135); // Burlywood
    private static final Color DARK_SKIN = new Color(205, 133, 63); // Peru
    private static final Color BACKGROUND_COLOR = new Color(253, 245, 230); // Old lace

    // Form fields
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField birthDateField;
    private JTextArea addressArea;
    private JCheckBox termsCheckbox;
    private JButton signUpButton;
    private JLabel errorLabel;
    private JLabel backToLoginLabel;

    // Layout containers
    private JPanel mainContainer;
    private JPanel formPanel;
    private JPanel fieldsGrid;

    // Form validation
    private boolean[] fieldValid = new boolean[6];
    private boolean isTwoColumnLayout = true;

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public SignUpPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.userService = new UserServicesDB();
        setBackground(BACKGROUND_COLOR); // Changed to skin tone background
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        // Main container with GridBagLayout for perfect centering
        mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        // Create form panel
        formPanel = createFormPanel();
        mainContainer.add(formPanel, gbc);

        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        add(scrollPane, BorderLayout.CENTER);

        // Initialize validation
        initializeFieldValidation();

        // Add resize listener
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateFormLayout();
            }
        });
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_SKIN); // Changed to light skin tone
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2), // Thicker border
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));
        panel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        // Add subtle rounded corners
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));

        // Header
        panel.add(createHeaderPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Separator
        panel.add(createSeparator());
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Form fields
        panel.add(createFormFieldsPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Separator
        panel.add(createSeparator());
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Terms and conditions
        panel.add(createTermsPanel());
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Create account button
        panel.add(createButtonPanel());

        // Back to login link
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(createBackToLoginPanel());

        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_SKIN);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Create Your Account");
        titleLabel.setFont(FontLoader.loadCustomFont(28, Font.BOLD));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join DermAppoint to manage your skin health journey");
        subtitleLabel.setFont(FontLoader.loadCustomFont(16, Font.PLAIN));
        subtitleLabel.setForeground(TEXT_LIGHT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(subtitleLabel);

        return panel;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(DARK_SKIN); // Changed to darker skin tone
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2)); // Thicker line
        return separator;
    }

    private JPanel createFormFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(LIGHT_SKIN);

        fieldsGrid = new JPanel(new GridBagLayout());
        fieldsGrid.setBackground(LIGHT_SKIN);

        // Create two-column layout initially
        createTwoColumnLayout();

        panel.add(fieldsGrid, BorderLayout.CENTER);
        return panel;
    }

    private void createTwoColumnLayout() {
        fieldsGrid.removeAll();
        fieldsGrid.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        // Left column
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsGrid.add(createFieldPanel("Full Name *", "Enter your full name", "fullName"), gbc);

        gbc.gridy = 1;
        fieldsGrid.add(createFieldPanel("Email Address *", "name@example.com", "email"), gbc);

        gbc.gridy = 2;
        fieldsGrid.add(createFieldPanel("Phone Number *", "(123) 456-7890", "phone"), gbc);

        // Password in left column, row 3
        gbc.gridy = 3;
        fieldsGrid.add(createPasswordFieldPanel("Password *", "password"), gbc);

        // Confirm Password below Password in left column, row 4
        gbc.gridy = 4;
        fieldsGrid.add(createPasswordFieldPanel("Confirm Password *", "confirmPassword"), gbc);

        // Right column
        gbc.gridx = 1;
        gbc.gridy = 0;
        fieldsGrid.add(createFieldPanel("Date of Birth", "MM/DD/YYYY", "birthDate"), gbc);

        gbc.gridy = 1;
        gbc.gridheight = 4; // Address spans 4 rows (rows 1-4)
        gbc.fill = GridBagConstraints.BOTH;
        fieldsGrid.add(createAddressPanel(), gbc);

        isTwoColumnLayout = true;
    }

    private void createSingleColumnLayout() {
        fieldsGrid.removeAll();
        fieldsGrid.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.gridy = 0;
        fieldsGrid.add(createFieldPanel("Full Name *", "Enter your full name", "fullName"), gbc);

        gbc.gridy = 1;
        fieldsGrid.add(createFieldPanel("Email Address *", "name@example.com", "email"), gbc);

        gbc.gridy = 2;
        fieldsGrid.add(createFieldPanel("Phone Number *", "(123) 456-7890", "phone"), gbc);

        gbc.gridy = 3;
        fieldsGrid.add(createFieldPanel("Date of Birth", "MM/DD/YYYY", "birthDate"), gbc);

        gbc.gridy = 4;
        fieldsGrid.add(createPasswordFieldPanel("Password *", "password"), gbc);

        gbc.gridy = 5;
        fieldsGrid.add(createPasswordFieldPanel("Confirm Password *", "confirmPassword"), gbc);

        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        fieldsGrid.add(createAddressPanel(), gbc);

        isTwoColumnLayout = false;
    }

    private JPanel createFieldPanel(String labelText, String placeholder, String fieldType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_SKIN);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField textField = new JTextField();
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        textField.setBackground(Color.WHITE);
        textField.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        textField.setMaximumSize(new Dimension(400, 40));

        // Add rounded corners to text fields
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        // Set placeholder
        textField.setText(placeholder);
        textField.setForeground(TEXT_LIGHT);
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(TEXT_DARK);
                    textField.setBackground(new Color(255, 250, 240)); // Floral white
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(TEXT_LIGHT);
                    textField.setText(placeholder);
                    textField.setBackground(Color.WHITE);
                }
            }
        });

        // Store reference
        switch (fieldType) {
            case "fullName":
                fullNameField = textField;
                break;
            case "email":
                emailField = textField;
                break;
            case "phone":
                phoneField = textField;
                break;
            case "birthDate":
                birthDateField = textField;
                break;
        }

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(textField);

        return panel;
    }

    private JPanel createPasswordFieldPanel(String labelText, String fieldType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_SKIN);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBackground(LIGHT_SKIN);
        fieldPanel.setMaximumSize(new Dimension(400, 40));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 40)));
        passwordField.setBackground(Color.WHITE);
        passwordField.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));

        // Add rounded corners
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 40)));

        // Toggle button
        JButton toggleBtn = new JButton("👁");
        toggleBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        toggleBtn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        toggleBtn.setBackground(MEDIUM_SKIN); // Changed to medium skin tone
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect to toggle button
        toggleBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                toggleBtn.setBackground(DARK_SKIN);
                toggleBtn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                toggleBtn.setBackground(MEDIUM_SKIN);
                toggleBtn.setForeground(Color.BLACK);
            }
        });

        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == '•') {
                passwordField.setEchoChar((char) 0);
                toggleBtn.setText("🙈");
            } else {
                passwordField.setEchoChar('•');
                toggleBtn.setText("👁");
            }
        });

        fieldPanel.add(passwordField, BorderLayout.CENTER);
        fieldPanel.add(toggleBtn, BorderLayout.EAST);

        // Store reference
        if (fieldType.equals("password")) {
            this.passwordField = passwordField;
        } else {
            this.confirmPasswordField = passwordField;
        }

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(fieldPanel);

        return panel;
    }

    private JPanel createAddressPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_SKIN);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("Address");
        label.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        addressArea = new JTextArea(4, 20);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        addressArea.setBackground(Color.WHITE);
        addressArea.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        // Add rounded corners
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        // Placeholder
        addressArea.setText("Enter your full address");
        addressArea.setForeground(TEXT_LIGHT);
        addressArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (addressArea.getText().equals("Enter your full address")) {
                    addressArea.setText("");
                    addressArea.setForeground(TEXT_DARK);
                    addressArea.setBackground(new Color(255, 250, 240)); // Floral white
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (addressArea.getText().isEmpty()) {
                    addressArea.setForeground(TEXT_LIGHT);
                    addressArea.setText("Enter your full address");
                    addressArea.setBackground(Color.WHITE);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(addressArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setMaximumSize(new Dimension(400, 120));

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(scrollPane);

        return panel;
    }

    private JPanel createTermsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(LIGHT_SKIN);

        termsCheckbox = new JCheckBox();
        termsCheckbox.setBackground(LIGHT_SKIN);

        JLabel termsLabel = new JLabel("I agree to the");
        termsLabel.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        termsLabel.setForeground(TEXT_LIGHT);

        JLabel termsLink = new JLabel("Terms and Conditions");
        termsLink.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        termsLink.setForeground(PRIMARY_COLOR);
        termsLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        termsLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(SignUpPanel.this,
                        "Terms and Conditions will be displayed here.",
                        "Terms and Conditions",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                termsLink.setText("<html><u>Terms and Conditions</u></html>");
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                termsLink.setText("Terms and Conditions");
            }
        });

        JLabel andLabel = new JLabel("and");
        andLabel.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        andLabel.setForeground(TEXT_LIGHT);

        JLabel privacyLink = new JLabel("Privacy Policy");
        privacyLink.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        privacyLink.setForeground(PRIMARY_COLOR);
        privacyLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        privacyLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(SignUpPanel.this,
                        "Privacy Policy will be displayed here.",
                        "Privacy Policy",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                privacyLink.setText("<html><u>Privacy Policy</u></html>");
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                privacyLink.setText("Privacy Policy");
            }
        });

        panel.add(termsCheckbox);
        panel.add(termsLabel);
        panel.add(termsLink);
        panel.add(andLabel);
        panel.add(privacyLink);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_SKIN);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        errorLabel.setForeground(ACCENT_COLOR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create Account button with enhanced styling
        signUpButton = createStyledButton("Create Account");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.addActionListener(e -> performSignUp());

        panel.add(errorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(signUpButton);

        return panel;
    }

    private JPanel createBackToLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(LIGHT_SKIN);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel backText = new JLabel("Already have an account?");
        backText.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        backText.setForeground(TEXT_LIGHT);

        backToLoginLabel = new JLabel("Back to Login");
        backToLoginLabel.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        backToLoginLabel.setForeground(LINK_COLOR);
        backToLoginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        backToLoginLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backToLoginLabel.setForeground(DARK_SKIN);
                backToLoginLabel.setText("<html><u>Back to Login</u></html>");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backToLoginLabel.setForeground(LINK_COLOR);
                backToLoginLabel.setText("Back to Login");
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearForm();
                mainFrame.showLoginScreen();
            }
        });

        panel.add(backText);
        panel.add(backToLoginLabel);

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw button background with gradient
                Color startColor = BUTTON_COLOR;
                Color endColor = new Color(139, 69, 19); // Darker brown

                if (getModel().isPressed()) {
                    startColor = new Color(101, 67, 33); // Dark brown
                    endColor = new Color(60, 30, 10); // Very dark brown
                } else if (getModel().isRollover()) {
                    startColor = new Color(205, 133, 63); // Peru
                    endColor = new Color(160, 82, 45); // Sienna
                }

                GradientPaint gradient = new GradientPaint(
                        0, 0, startColor,
                        0, getHeight(), endColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25); // More rounded

                // Draw button border
                g2d.setColor(new Color(101, 67, 33)); // Dark brown border
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);

                // Draw text with shadow effect
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                
                // Text shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawString(getText(), 
                    (getWidth() - textWidth) / 2 + 2, 
                    (getHeight() + textHeight) / 2 - fm.getDescent() + 2);
                
                // Main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), 
                    (getWidth() - textWidth) / 2, 
                    (getHeight() + textHeight) / 2 - fm.getDescent());
            }
        };

        button.setFont(FontLoader.loadCustomFont(16, Font.BOLD));
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(220, 50));
        button.setMaximumSize(new Dimension(250, 50));
        button.setMinimumSize(new Dimension(180, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 50, 12, 50));

        // Center the text
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);

        // Add subtle shadow effect
        button.setMargin(new Insets(5, 15, 5, 15));

        return button;
    }

    private void updateFormLayout() {
        int width = getWidth();

        // Update form panel max width
        if (width > 1200) {
            formPanel.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE));
        } else if (width > 900) {
            formPanel.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
        } else if (width > 768) {
            formPanel.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));
        } else {
            formPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
            if (isTwoColumnLayout) {
                createSingleColumnLayout();
            }
        }

        // Switch back to two-column if we have enough width
        if (width > 768 && !isTwoColumnLayout) {
            createTwoColumnLayout();
        }

        // Update font sizes
        updateFontSizes(width);

        // Revalidate
        formPanel.revalidate();
        formPanel.repaint();
    }

    private void updateFontSizes(int width) {
        float scale = getFontScale(width);

        Component[] components = formPanel.getComponents();
        for (Component comp : components) {
            updateComponentFontRecursive(comp, scale);
        }
    }

    private void updateComponentFontRecursive(Component comp, float scale) {
        if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            Font font = label.getFont();
            label.setFont(font.deriveFont(font.getSize() * scale));
        } else if (comp instanceof JTextField) {
            JTextField field = (JTextField) comp;
            Font font = field.getFont();
            field.setFont(font.deriveFont(font.getSize() * scale));
        } else if (comp instanceof JTextArea) {
            JTextArea area = (JTextArea) comp;
            Font font = area.getFont();
            area.setFont(font.deriveFont(font.getSize() * scale));
        } else if (comp instanceof JButton) {
            JButton button = (JButton) comp;
            Font font = button.getFont();
            button.setFont(font.deriveFont(font.getSize() * scale));
        } else if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                updateComponentFontRecursive(child, scale);
            }
        }
    }

    private float getFontScale(int width) {
        if (width > 1200)
            return 1.0f;
        if (width > 768)
            return 0.95f;
        if (width > 480)
            return 0.9f;
        return 0.85f;
    }

    private void initializeFieldValidation() {
        fullNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                validateField(0, ValidationUtils.validateName(fullNameField.getText()),
                        "Please enter a valid name");
            }
        });

        emailField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                validateField(1, ValidationUtils.validateEmail(emailField.getText()),
                        "Please enter a valid email address");
            }
        });

        phoneField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                validateField(2, ValidationUtils.validatePhone(phoneField.getText()),
                        "Please enter a valid phone number");
            }
        });

        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String password = new String(passwordField.getPassword());
                boolean valid = password.length() >= 6;
                validateField(3, valid, "Password must be at least 6 characters");
            }
        });

        confirmPasswordField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String password = new String(passwordField.getPassword());
                String confirm = new String(confirmPasswordField.getPassword());
                boolean valid = password.equals(confirm);
                validateField(4, valid, "Passwords do not match");
            }
        });

        birthDateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String text = birthDateField.getText();
                boolean valid = text.isEmpty() || validateDate(text);
                validateField(5, valid, "Please use MM/DD/YYYY format");
            }
        });
    }

    private boolean validateDate(String date) {
        try {
            LocalDate.parse(date, DISPLAY_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void validateField(int index, boolean isValid, String errorMessage) {
        fieldValid[index] = isValid;

        Component field = getFieldComponent(index);
        if (field instanceof JTextField) {
            JTextField tf = (JTextField) field;
            Color borderColor = isValid ? SUCCESS_COLOR : ACCENT_COLOR;
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 2),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        } else if (field instanceof JPasswordField) {
            JPasswordField pf = (JPasswordField) field;
            Color borderColor = isValid ? SUCCESS_COLOR : ACCENT_COLOR;
            pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 2),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        }

        if (!isValid) {
            showError(errorMessage);
        } else {
            clearError();
        }
    }

    private Component getFieldComponent(int index) {
        switch (index) {
            case 0:
                return fullNameField;
            case 1:
                return emailField;
            case 2:
                return phoneField;
            case 3:
                return passwordField;
            case 4:
                return confirmPasswordField;
            case 5:
                return birthDateField;
            default:
                return null;
        }
    }

    private void performSignUp() {
        String fullName = getFieldValue(fullNameField, "Enter your full name");
        String email = getFieldValue(emailField, "name@example.com");
        String phone = getFieldValue(phoneField, "(123) 456-7890");
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String birth = getFieldValue(birthDateField, "MM/DD/YYYY");
        String address = getAddressValue();
        String userType = "PATIENT";

        if (!validateAllFields()) {
            return;
        }

        LocalDate dob = null;
        if (!birth.isEmpty() && !birth.equals("MM/DD/YYYY")) {
            try {
                dob = LocalDate.parse(birth, DISPLAY_FMT);
            } catch (DateTimeParseException e) {
                showError("Invalid date format. Use MM/DD/YYYY");
                return;
            }
        }

        Patient p = userService.registerPatient(fullName, email, phone, password, address, dob, null);

        if (p == null) {
            showError("Registration failed. Email may already exist.");
            return;
        }

        showSuccess("Account created successfully! Redirecting to login...");
        Timer timer = new Timer(2000, e -> {
            clearForm();
            mainFrame.showLoginScreen();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private String getFieldValue(JTextField field, String placeholder) {
        String value = field.getText().trim();
        return value.equals(placeholder) ? "" : value;
    }

    private String getAddressValue() {
        String value = addressArea.getText().trim();
        return value.equals("Enter your full address") ? "" : value;
    }

    private boolean validateAllFields() {
        if (fullNameField.getText().equals("Enter your full name") ||
                emailField.getText().equals("name@example.com") ||
                phoneField.getText().equals("(123) 456-7890") ||
                passwordField.getPassword().length == 0 ||
                confirmPasswordField.getPassword().length == 0) {
            showError("Please fill in all required fields (*)");
            return false;
        }

        for (int i = 0; i < 5; i++) {
            if (!fieldValid[i]) {
                showError("Please correct the highlighted fields");
                return false;
            }
        }

        if (!termsCheckbox.isSelected()) {
            showError("You must agree to the Terms and Conditions");
            return false;
        }

        return true;
    }

    private void clearForm() {
        fullNameField.setText("Enter your full name");
        fullNameField.setForeground(TEXT_LIGHT);
        fullNameField.setBackground(Color.WHITE);
        emailField.setText("name@example.com");
        emailField.setForeground(TEXT_LIGHT);
        emailField.setBackground(Color.WHITE);
        phoneField.setText("(123) 456-7890");
        phoneField.setForeground(TEXT_LIGHT);
        phoneField.setBackground(Color.WHITE);
        birthDateField.setText("MM/DD/YYYY");
        birthDateField.setForeground(TEXT_LIGHT);
        birthDateField.setBackground(Color.WHITE);
        passwordField.setText("");
        confirmPasswordField.setText("");
        addressArea.setText("Enter your full address");
        addressArea.setForeground(TEXT_LIGHT);
        addressArea.setBackground(Color.WHITE);
        termsCheckbox.setSelected(false);
        clearError();

        resetFieldBorders();
    }

    private void resetFieldBorders() {
        Component[] fields = { fullNameField, emailField, phoneField,
                passwordField, confirmPasswordField, birthDateField };
        for (Component field : fields) {
            if (field != null) {
                ((JComponent) field).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            }
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(ACCENT_COLOR);
    }

    private void showSuccess(String msg) {
        errorLabel.setText(msg);
        errorLabel.setForeground(SUCCESS_COLOR);
    }

    private void clearError() {
        errorLabel.setText(" ");
    }
}