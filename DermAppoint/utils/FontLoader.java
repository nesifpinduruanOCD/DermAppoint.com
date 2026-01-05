package utils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and managing fonts in the application
 * Provides skin-friendly, readable typography for medical systems
 */
public class FontLoader {
    
    // Font cache to avoid reloading fonts
    private static final Map<String, Font> fontCache = new HashMap<>();
    
    // Font keys for different use cases
    public static final String FONT_PRIMARY = "primary";
    public static final String FONT_SECONDARY = "secondary";
    public static final String FONT_HEADING = "heading";
    public static final String FONT_BODY = "body";
    public static final String FONT_MONO = "mono";
    
    // Default font names (will use system fonts if custom fonts not found)
    private static final String[] PRIMARY_FONT_NAMES = {
        "Segoe UI", "Helvetica Neue", "Arial", "sans-serif"
    };
    
    private static final String[] HEADING_FONT_NAMES = {
        "Montserrat", "Roboto", "Segoe UI", "Arial", "sans-serif"
    };
    
    private static final String[] MONO_FONT_NAMES = {
        "Consolas", "Monaco", "Courier New", "monospace"
    };
    
    // Custom font file paths (relative to resources/fonts/)
    private static final String CUSTOM_PRIMARY_FONT = "fonts/Roboto-Regular.ttf";
    private static final String CUSTOM_BOLD_FONT = "fonts/Roboto-Bold.ttf";
    private static final String CUSTOM_LIGHT_FONT = "fonts/Roboto-Light.ttf";
    private static final String CUSTOM_MONO_FONT = "fonts/RobotoMono-Regular.ttf";
    
    // Font sizes for different elements
    public static final int SIZE_EXTRA_LARGE = 36;
    public static final int SIZE_LARGE = 24;
    public static final int SIZE_MEDIUM = 16;
    public static final int SIZE_SMALL = 14;
    public static final int SIZE_EXTRA_SMALL = 12;
    public static final int SIZE_TINY = 10;
    
    // Font weights
    public static final int WEIGHT_LIGHT = Font.PLAIN;
    public static final int WEIGHT_REGULAR = Font.PLAIN;
    public static final int WEIGHT_MEDIUM = Font.PLAIN;
    public static final int WEIGHT_BOLD = Font.BOLD;
    public static final int WEIGHT_EXTRABOLD = Font.BOLD;
    
    static {
        // Initialize fonts when class is loaded
        initializeFonts();
    }
    
    /**
     * Initialize and load custom fonts if available
     */
    private static void initializeFonts() {
        try {
            // Try to load custom fonts
            loadCustomFont(CUSTOM_PRIMARY_FONT, FONT_PRIMARY);
            loadCustomFont(CUSTOM_BOLD_FONT, "bold");
            loadCustomFont(CUSTOM_LIGHT_FONT, "light");
            loadCustomFont(CUSTOM_MONO_FONT, FONT_MONO);
            
        } catch (Exception e) {
            System.err.println("Custom fonts not found, using system fonts: " + e.getMessage());
        }
    }
    
    /**
     * Load a custom font from resources
     */
    private static void loadCustomFont(String fontPath, String cacheKey) {
        try {
            // Try to load from resources
            InputStream fontStream = FontLoader.class.getClassLoader().getResourceAsStream(fontPath);
            
            if (fontStream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(font);
                fontCache.put(cacheKey, font);
                System.out.println("Loaded custom font: " + cacheKey);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("Failed to load font " + fontPath + ": " + e.getMessage());
        }
    }
    
    /**
     * Get a font with specified size and style
     * Uses cached custom fonts or falls back to system fonts
     */
    public static Font loadCustomFont(int size, int style) {
        return loadCustomFont(size, style, FONT_PRIMARY);
    }
    
    /**
     * Get a font with specified size, style, and type
     */
    public static Font loadCustomFont(int size, int style, String fontType) {
        String cacheKey = fontType + "_" + size + "_" + style;
        
        // Return cached font if available
        if (fontCache.containsKey(cacheKey)) {
            return fontCache.get(cacheKey);
        }
        
        Font font = null;
        
        // Try to use custom font first
        if (fontType.equals(FONT_PRIMARY) && fontCache.containsKey(FONT_PRIMARY)) {
            Font baseFont = fontCache.get(FONT_PRIMARY);
            font = baseFont.deriveFont(style, size);
        } else if (fontType.equals("bold") && fontCache.containsKey("bold")) {
            Font baseFont = fontCache.get("bold");
            font = baseFont.deriveFont(style, size);
        } else if (fontType.equals("light") && fontCache.containsKey("light")) {
            Font baseFont = fontCache.get("light");
            font = baseFont.deriveFont(style, size);
        } else if (fontType.equals(FONT_MONO) && fontCache.containsKey(FONT_MONO)) {
            Font baseFont = fontCache.get(FONT_MONO);
            font = baseFont.deriveFont(style, size);
        }
        
        // If custom font not available, use system font
        if (font == null) {
            font = getSystemFont(size, style, fontType);
        }
        
        // Cache the font
        fontCache.put(cacheKey, font);
        
        return font;
    }
    
    /**
     * Get appropriate system font based on font type
     */
    private static Font getSystemFont(int size, int style, String fontType) {
        String fontName = PRIMARY_FONT_NAMES[0]; // Default
        
        switch (fontType) {
            case FONT_HEADING:
                fontName = findAvailableFont(HEADING_FONT_NAMES);
                break;
            case FONT_MONO:
                fontName = findAvailableFont(MONO_FONT_NAMES);
                break;
            case FONT_PRIMARY:
            case FONT_SECONDARY:
            case FONT_BODY:
            default:
                fontName = findAvailableFont(PRIMARY_FONT_NAMES);
                break;
        }
        
        return new Font(fontName, style, size);
    }
    
    /**
     * Find the first available font from a list of font names
     */
    private static String findAvailableFont(String[] fontNames) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        for (String preferredFont : fontNames) {
            for (String availableFont : availableFonts) {
                if (availableFont.equalsIgnoreCase(preferredFont)) {
                    return availableFont;
                }
            }
        }
        
        // Return the first available font as fallback
        return availableFonts.length > 0 ? availableFonts[0] : "Dialog";
    }
    
    /**
     * Create a font with proper scaling for high DPI displays
     */
    public static Font getScaledFont(int baseSize, int style, String fontType) {
        float scaleFactor = getDisplayScaleFactor();
        int scaledSize = (int) (baseSize * scaleFactor);
        return loadCustomFont(scaledSize, style, fontType);
    }
    
    /**
     * Calculate display scale factor for high DPI support
     */
    private static float getDisplayScaleFactor() {
        // Default scale factor
        float scaleFactor = 1.0f;
        
        try {
            // Get screen resolution
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            
            // Adjust scale factor based on resolution
            if (screenWidth > 1920) {
                scaleFactor = 1.25f; // 2K+ displays
            } else if (screenWidth > 2560) {
                scaleFactor = 1.5f; // 4K displays
            }
        } catch (Exception e) {
            // Use default scale factor
        }
        
        return scaleFactor;
    }
    
    /**
     * Get a font suitable for medical form labels
     */
    public static Font getFormLabelFont() {
        return loadCustomFont(SIZE_SMALL, WEIGHT_MEDIUM, FONT_PRIMARY);
    }
    
    /**
     * Get a font suitable for medical form input fields
     */
    public static Font getFormInputFont() {
        return loadCustomFont(SIZE_MEDIUM, WEIGHT_REGULAR, FONT_PRIMARY);
    }
    
    /**
     * Get a font suitable for section headings
     */
    public static Font getSectionHeadingFont() {
        return loadCustomFont(SIZE_LARGE, WEIGHT_BOLD, FONT_HEADING);
    }
    
    /**
     * Get a font suitable for body text
     */
    public static Font getBodyTextFont() {
        return loadCustomFont(SIZE_MEDIUM, WEIGHT_REGULAR, FONT_BODY);
    }
    
    /**
     * Get a font suitable for small print/disclaimers
     */
    public static Font getSmallPrintFont() {
        return loadCustomFont(SIZE_TINY, WEIGHT_LIGHT, FONT_SECONDARY);
    }
    
    /**
     * Get a font suitable for monospaced text (for codes, IDs, etc.)
     */
    public static Font getMonospacedFont(int size) {
        return loadCustomFont(size, WEIGHT_REGULAR, FONT_MONO);
    }
    
    /**
     * Apply font scaling to all components in a container recursively
     */
    public static void applyFontScaling(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComponent) {
                Font currentFont = component.getFont();
                if (currentFont != null) {
                    float scaleFactor = getDisplayScaleFactor();
                    Font scaledFont = currentFont.deriveFont(currentFont.getSize2D() * scaleFactor);
                    component.setFont(scaledFont);
                }
            }
            
            if (component instanceof Container) {
                applyFontScaling((Container) component);
            }
        }
    }
    
    /**
     * Create a styled label with proper font for dermatology clinic
     */
    public static JLabel createStyledLabel(String text, int fontSize, int fontStyle, String fontType) {
        JLabel label = new JLabel(text);
        Font font = loadCustomFont(fontSize, fontStyle, fontType);
        label.setFont(font);
        
        // Set appropriate foreground color based on font style
        if (fontStyle == WEIGHT_BOLD) {
            label.setForeground(ColorScheme.TEXT_DARK);
        } else {
            label.setForeground(ColorScheme.TEXT_MEDIUM);
        }
        
        return label;
    }
    
    /**
     * Create a button with styled font
     */
    public static JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(loadCustomFont(SIZE_MEDIUM, WEIGHT_BOLD, FONT_PRIMARY));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    /**
     * Create a text field with styled font
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(getFormInputFont());
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textField.setBackground(Color.WHITE);
        
        return textField;
    }
    
    /**
     * Create a text area with styled font
     */
    public static JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setFont(getBodyTextFont());
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        return textArea;
    }
    
    /**
     * Create a combo box with styled font
     */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(getFormInputFont());
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorScheme.BORDER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        return comboBox;
    }
    
    /**
     * Get a font for appointment status display with appropriate color
     */
    public static Font getStatusFont(String status) {
        int style = WEIGHT_BOLD;
        int size = SIZE_SMALL;
        
        switch (status.toUpperCase()) {
            case "APPROVED":
            case "COMPLETED":
                style = WEIGHT_BOLD;
                break;
            case "PENDING":
                style = Font.ITALIC;
                break;
            case "CANCELLED":
                style = WEIGHT_REGULAR;
                break;
        }
        
        return loadCustomFont(size, style, FONT_PRIMARY);
    }
    
    /**
     * Get appropriate color for status text
     */
    public static Color getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "APPROVED":
                return ColorScheme.APPROVED;
            case "PENDING":
                return ColorScheme.PENDING;
            case "CANCELLED":
                return ColorScheme.CANCELLED;
            case "COMPLETED":
                return ColorScheme.COMPLETED;
            default:
                return ColorScheme.TEXT_MEDIUM;
        }
    }
    
    /**
     * Test method to display all available fonts
     */
    public static void displayAvailableFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        
        System.out.println("=== Available Fonts ===");
        for (String fontName : fontNames) {
            System.out.println(fontName);
        }
        System.out.println("=======================");
    }
}