package GUI;

import utils.FontLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class WelcomePanel extends JPanel {
    private MainFrame mainFrame;
    // Enhanced skin tone palette with darker browns
    @SuppressWarnings("unused")
    private static final Color PRIMARY_COLOR = new Color(210, 180, 140); // Tan skin tone
    private static final Color SECONDARY_COLOR = new Color(255, 245, 238); // Soft peach/ivory
    private static final Color TEXT_DARK = new Color(60, 45, 35); // Much darker brown (almost chocolate)
    private static final Color TEXT_LIGHT = new Color(120, 90, 75); // Darker medium brown
    private static final Color CARD_BG = new Color(255, 250, 245); // Off-white with warm tint
    private static final Color BORDER_COLOR = new Color(230, 215, 200); // Medium warm beige
    @SuppressWarnings("unused")
    private static final Color ACCENT_COLOR = new Color(170, 130, 105); // Darker warm brown for accents
    private static final Color DEEPER_ACCENT = new Color(140, 100, 80); // Even darker accent for important elements
    private static final Color SKIN_SHADOW = new Color(220, 200, 185); // For subtle shadows
    
    // Texture-related variables
    private BufferedImage skinTexture;
    private Random random = new Random();
    private boolean textureInitialized = false;

    // Responsive thresholds
    private static final int XL_SCREEN = 1200;
    private static final int LG_SCREEN = 900;
    private static final int MD_SCREEN = 768;
    private static final int SM_SCREEN = 480;

    // Components
    private JPanel contentPanel;
    private JPanel heroPanel;
    private JPanel statsPanel;
    private JPanel featuresPanel;
    private JPanel ctaPanel;
    private JLabel titleLabel;
    private JLabel descriptionLabel;
    private JLabel taglineLabel;
    private JLabel sectionTitleLabel;
    private JLabel ctaTitleLabel;
    private JLabel ctaSubtitleLabel;

    public WelcomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(SECONDARY_COLOR);
        initializeUI();
        setupResponsiveBehavior();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Create skin texture effect
        if (!textureInitialized || skinTexture == null || 
            skinTexture.getWidth() != getWidth() || 
            skinTexture.getHeight() != getHeight()) {
            createSkinTexture(getWidth(), getHeight());
        }
        
        // Draw the texture
        if (skinTexture != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2d.drawImage(skinTexture, 0, 0, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private void createSkinTexture(int width, int height) {
        if (width <= 0 || height <= 0) return;
        
        skinTexture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = skinTexture.createGraphics();
        
        // Create subtle skin texture with pores and fine lines
        g2d.setColor(new Color(255, 250, 245, 10));
        g2d.fillRect(0, 0, width, height);
        
        // Add subtle pore-like dots
        g2d.setColor(new Color(200, 180, 160, 3));
        for (int i = 0; i < width * height / 200; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(2) + 1;
            g2d.fillOval(x, y, size, size);
        }
        
        // Add fine lines (like skin texture)
        g2d.setColor(new Color(190, 170, 150, 2));
        for (int i = 0; i < 50; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int length = random.nextInt(50) + 10;
            int angle = random.nextInt(180);
            int x2 = x1 + (int)(length * Math.cos(Math.toRadians(angle)));
            int y2 = y1 + (int)(length * Math.sin(Math.toRadians(angle)));
            
            g2d.setStroke(new BasicStroke(0.5f));
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        g2d.dispose();
        textureInitialized = true;
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Main container with perfect centering
        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create soft radial gradient background for content
                Color[] colors = {new Color(255, 250, 245, 240), new Color(255, 245, 238, 220)};
                int width = getWidth();
                int height = getHeight();
                
                RadialGradientPaint gradient = new RadialGradientPaint(
                    new Point(width/2, height/2), 
                    Math.max(width, height) * 0.8f,
                    new float[]{0.0f, 1.0f},
                    colors
                );
                
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create all sections
        createHeader();
        createHeroSection();
        createStatsSection();
        createFeaturesSection();
        createCTASection();

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupResponsiveBehavior() {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                textureInitialized = false; // Force texture recreation
                updateResponsiveLayout();
            }
        });
    }

    private void updateResponsiveLayout() {
        int width = getWidth();

        // Update all sections based on screen size
        updateSectionSizes(width);
        updateFontSizes(width);
        updateLayoutColumns(width);

        // Revalidate everything
        revalidate();
        repaint();
    }

    private void updateSectionSizes(int width) {
        // Update content panel max width
        if (width > XL_SCREEN) {
            contentPanel.setMaximumSize(new Dimension(1200, Integer.MAX_VALUE));
        } else if (width > LG_SCREEN) {
            contentPanel.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));
        } else if (width > MD_SCREEN) {
            contentPanel.setMaximumSize(new Dimension(768, Integer.MAX_VALUE));
        } else {
            contentPanel.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));
        }
    }

    private void updateFontSizes(int width) {
        float scale = getFontScale(width);

        // Update all label fonts
        updateComponentFontRecursive(contentPanel, scale);
    }

    private void updateComponentFontRecursive(Container container, float scale) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                Font font = label.getFont();
                if (font != null) {
                    label.setFont(font.deriveFont(font.getSize() * scale));
                }
            } else if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                Font font = button.getFont();
                if (font != null) {
                    button.setFont(font.deriveFont(font.getSize() * scale));
                }
            } else if (comp instanceof JTextArea) {
                JTextArea area = (JTextArea) comp;
                Font font = area.getFont();
                if (font != null) {
                    area.setFont(font.deriveFont(font.getSize() * scale));
                }
            } else if (comp instanceof Container) {
                updateComponentFontRecursive((Container) comp, scale);
            }
        }
    }

    private float getFontScale(int width) {
        if (width > XL_SCREEN)
            return 1.0f;
        if (width > LG_SCREEN)
            return 0.95f;
        if (width > MD_SCREEN)
            return 0.9f;
        if (width > SM_SCREEN)
            return 0.85f;
        return 0.8f;
    }

    private void updateLayoutColumns(int width) {
        // Update stats layout
        if (statsPanel != null) {
            int columns = getStatsColumns(width);
            statsPanel.setLayout(new GridLayout(1, columns, 20, 20));
            statsPanel.revalidate();
        }

        // Update features layout
        if (featuresPanel != null) {
            int columns = getFeaturesColumns(width);
            featuresPanel.setLayout(new GridLayout(0, columns, 20, 20));
            featuresPanel.revalidate();
        }
    }

    private int getStatsColumns(int width) {
        if (width > LG_SCREEN)
            return 3;
        if (width > MD_SCREEN)
            return 2;
        return 1;
    }

    private int getFeaturesColumns(int width) {
        if (width > LG_SCREEN)
            return 3;
        if (width > MD_SCREEN)
            return 2;
        return 1;
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create subtle gradient background
                LinearGradientPaint gradient = new LinearGradientPaint(
                    0, 0, 0, getHeight(),
                    new float[]{0.0f, 1.0f},
                    new Color[]{CARD_BG, new Color(250, 240, 230)}
                );
                
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Logo section
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        logoPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("🩺");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel appName = new JLabel("DermAppoint");
        appName.setFont(FontLoader.loadCustomFont(24, Font.BOLD));
        appName.setForeground(DEEPER_ACCENT); // Darker brown for logo

        logoPanel.add(logoIcon);
        logoPanel.add(appName);

        // Navigation
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        navPanel.setOpaque(false);

        JButton aboutBtn = createNavButton("About");
        JButton contactBtn = createNavButton("Contact");
        JButton helpBtn = createNavButton("Help");

        navPanel.add(aboutBtn);
        navPanel.add(contactBtn);
        navPanel.add(helpBtn);

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);

        // Border and padding
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        button.setForeground(TEXT_LIGHT);
        button.setBackground(CARD_BG);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(DEEPER_ACCENT);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(TEXT_LIGHT);
            }
        });

        return button;
    }

    private void createHeroSection() {
        heroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create soft shadow effect
                g2d.setColor(new Color(240, 235, 230));
                g2d.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 20, 20);
                
                // Main background with subtle gradient
                LinearGradientPaint gradient = new LinearGradientPaint(
                    0, 0, 0, getHeight(),
                    new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{CARD_BG, new Color(255, 252, 248), CARD_BG}
                );
                
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setOpaque(false);
        heroPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        heroPanel.setMaximumSize(new Dimension(900, 400));

        // Tagline
        taglineLabel = new JLabel("PROFESSIONAL DERMATOLOGY CARE");
        taglineLabel.setFont(FontLoader.loadCustomFont(14, Font.BOLD));
        taglineLabel.setForeground(DEEPER_ACCENT); // Darker brown for tagline
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroPanel.add(taglineLabel);
        heroPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Title - Much darker brown
        String titleText = "<html><div style='text-align: center; font-size: 52px; font-weight: bold; color: #3C2B20; line-height: 1.2;'>"
                +
                "Transform Your Skin Health<br>" +
                "<span style='font-size: 48px; color: #5C4033;'>With Expert Care</span>" +
                "</div></html>";

        titleLabel = new JLabel(titleText);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        heroPanel.add(titleLabel);
        heroPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Description - Darker brown text
        String descriptionText = "<html><div style='text-align: center; font-size: 18px; color: #7D5D4D; line-height: 1.6; max-width: 800px;'>"
                +
                "DermAppoint connects you with certified dermatologists for personalized skin care solutions.<br>" +
                "From acne treatment to skin cancer screening, we provide <b style='color:#5C4033;'>comprehensive</b> care tailored to your needs."
                +
                "</div></html>";

        descriptionLabel = new JLabel(descriptionText);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        heroPanel.add(descriptionLabel);
        heroPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Container for centering
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(heroPanel, BorderLayout.CENTER);

        contentPanel.add(container);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));
    }

    private void createStatsSection() {
        JPanel sectionContainer = new JPanel(new BorderLayout());
        sectionContainer.setOpaque(false);

        statsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create stat cards
        statsPanel.add(createStatCard("500+", "Expert Dermatologists", "Board-certified specialists"));
        statsPanel.add(createStatCard("10K+", "Patients Helped", "Successfully treated cases"));
        statsPanel.add(createStatCard("99%", "Satisfaction Rate", "Patient recommendation score"));

        // Center the stats panel
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        centerWrapper.add(statsPanel, gbc);

        sectionContainer.add(centerWrapper, BorderLayout.CENTER);
        sectionContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 40, 20));

        contentPanel.add(sectionContainer);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));
    }

    private JPanel createStatCard(String number, String title, String subtitle) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create subtle 3D effect
                g2d.setColor(SKIN_SHADOW);
                g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 12, 12);
                
                // Main card background with gradient
                LinearGradientPaint gradient = new LinearGradientPaint(
                    0, 0, 0, getHeight(),
                    new float[]{0.0f, 1.0f},
                    new Color[]{new Color(255, 252, 248), CARD_BG}
                );
                
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(300, 150));

        // Number - Darker accent color
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(FontLoader.loadCustomFont(36, Font.BOLD));
        numberLabel.setForeground(DEEPER_ACCENT);
        numberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title - Dark brown
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontLoader.loadCustomFont(16, Font.BOLD));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle - Medium brown
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FontLoader.loadCustomFont(12, Font.PLAIN));
        subtitleLabel.setForeground(TEXT_LIGHT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(numberLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(subtitleLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private void createFeaturesSection() {
        JPanel sectionContainer = new JPanel(new BorderLayout());
        sectionContainer.setOpaque(false);

        // Section title - Very dark brown
        sectionTitleLabel = new JLabel("Why Choose DermAppoint?");
        sectionTitleLabel.setFont(FontLoader.loadCustomFont(32, Font.BOLD));
        sectionTitleLabel.setForeground(new Color(70, 50, 40)); // Deep chocolate brown
        sectionTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sectionTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);
        titleContainer.add(sectionTitleLabel);
        titleContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // Features grid
        featuresPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        featuresPanel.setOpaque(false);

        // Add feature cards
        featuresPanel.add(createFeatureCard("🎯", "Personalized Treatment",
                "Customized care plans based on your unique skin type and concerns"));
        featuresPanel.add(createFeatureCard("⚡", "Quick Appointments",
                "Book same-day consultations with our extensive network of dermatologists"));
        featuresPanel.add(createFeatureCard("📱", "Digital Health Tools",
                "Track progress, access resources, and receive reminders through our platform"));
        featuresPanel.add(createFeatureCard("🛡️", "Privacy & Security",
                "Your health data is protected with bank-level encryption and privacy controls"));
        featuresPanel.add(createFeatureCard("📊", "Progress Tracking",
                "Monitor your skin health journey with detailed analytics and photo documentation"));
        featuresPanel.add(createFeatureCard("👥", "Second Opinions",
                "Access multiple expert opinions to ensure the best treatment approach"));

        // Center wrapper
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        centerWrapper.add(titleContainer, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        centerWrapper.add(featuresPanel, gbc);

        sectionContainer.add(centerWrapper, BorderLayout.CENTER);
        sectionContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 40, 20));

        contentPanel.add(sectionContainer);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));
    }

    private JPanel createFeatureCard(String emoji, String title, String description) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create paper-like texture effect
                g2d.setColor(new Color(255, 252, 248, 30));
                for (int i = 0; i < 20; i++) {
                    int x = random.nextInt(getWidth());
                    int y = random.nextInt(getHeight());
                    int size = random.nextInt(5) + 1;
                    g2d.fillOval(x, y, size, size);
                }
                
                // Main card with subtle gradient
                LinearGradientPaint gradient = new LinearGradientPaint(
                    0, 0, getWidth(), getHeight(),
                    new float[]{0.0f, 1.0f},
                    new Color[]{CARD_BG, new Color(250, 245, 240)}
                );
                
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Subtle border
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(350, 220));

        // Emoji
        JLabel emojiLabel = new JLabel(emoji);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title - Dark accent color
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontLoader.loadCustomFont(18, Font.BOLD));
        titleLabel.setForeground(DEEPER_ACCENT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Description - Darker brown text
        JTextArea descArea = new JTextArea(description);
        descArea.setFont(FontLoader.loadCustomFont(14, Font.PLAIN));
        descArea.setForeground(TEXT_DARK); // Darker brown for readability
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setOpaque(false);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        card.add(Box.createVerticalGlue());
        card.add(emojiLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descArea);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private void createCTASection() {
        ctaPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Blue background
                g2d.setColor(new Color(30, 144, 255));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        ctaPanel.setLayout(new BoxLayout(ctaPanel, BoxLayout.Y_AXIS));
        ctaPanel.setOpaque(false);
        ctaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ctaPanel.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
        ctaPanel.setMaximumSize(new Dimension(800, 400));

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(3, 1, 0, 16));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(320, 220));
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton newRegistrationBtn = createSimpleButton("New Registration");
        newRegistrationBtn.addActionListener(e -> mainFrame.showSignUpScreen());

        JButton viewRecordsBtn = createSimpleButton("View Records");
        viewRecordsBtn.addActionListener(e -> mainFrame.showLoginScreen());

        JButton exitBtn = createSimpleButton("Exit");
        exitBtn.addActionListener(e -> System.exit(0));

        Dimension buttonSize = new Dimension(240, 54);
        newRegistrationBtn.setPreferredSize(buttonSize);
        viewRecordsBtn.setPreferredSize(buttonSize);
        exitBtn.setPreferredSize(buttonSize);

        buttonsPanel.add(newRegistrationBtn);
        buttonsPanel.add(viewRecordsBtn);
        buttonsPanel.add(exitBtn);

        // Center wrapper
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        centerWrapper.add(buttonsPanel, gbc);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 40, 20));

        contentPanel.add(centerWrapper);
        contentPanel.add(Box.createVerticalGlue());
    }

    private JButton createSimpleButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Light blue background
                g2d.setColor(new Color(173, 216, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Dark blue border
                g2d.setColor(new Color(0, 0, 139));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        
        return button;
    }
}