package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimationsUtil {
    
    /**
     * Create a fade-in animation for a component
     */
    public static void fadeIn(JComponent component, int durationMillis) {
        component.setVisible(true);
        
        Timer timer = new Timer(16, null); // ~60 FPS
        final long startTime = System.currentTimeMillis();
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / durationMillis);
                
                // Calculate alpha value
                int alpha = (int) (progress * 255);
                Color bg = component.getBackground();
                Color fg = component.getForeground();
                
                // Create transparent versions
                Color transparentBg = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), alpha);
                Color transparentFg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), alpha);
                
                component.setBackground(transparentBg);
                component.setForeground(transparentFg);
                component.repaint();
                
                if (progress >= 1.0f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * Create a slide animation for panels
     */
    public static void slideIn(JComponent component, int startX, int endX, int durationMillis) {
        Timer timer = new Timer(16, null);
        final long startTime = System.currentTimeMillis();
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float) elapsed / durationMillis);
                
                // Easing function for smooth animation
                float easedProgress = easeOutCubic(progress);
                int currentX = (int) (startX + (endX - startX) * easedProgress);
                
                component.setLocation(currentX, component.getY());
                component.repaint();
                
                if (progress >= 1.0f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * Easing function for smooth animations
     */
    private static float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }
    
    /**
     * Create a pulsing animation for important notifications
     */
    public static void pulse(JComponent component, Color pulseColor, int cycles) {
        Timer timer = new Timer(500, null);
        final Color originalColor = component.getBackground();
        final int[] count = {0};
        
        timer.addActionListener(new ActionListener() {
            boolean pulseState = false;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pulseState) {
                    component.setBackground(pulseColor);
                } else {
                    component.setBackground(originalColor);
                }
                pulseState = !pulseState;
                component.repaint();
                
                count[0]++;
                if (count[0] >= cycles * 2) {
                    ((Timer) e.getSource()).stop();
                    component.setBackground(originalColor);
                }
            }
        });
        
        timer.start();
    }
}