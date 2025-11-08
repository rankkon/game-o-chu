package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder; 
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public final class Theme {

    private Theme() {}

    // === BẢNG MÀU ===
    public static final Color COLOR_BACKGROUND = new Color(245, 248, 250); // Nền xám nhạt
    public static final Color COLOR_WHITE = Color.WHITE;
    public static final Color COLOR_TEXT_DARK = new Color(56, 73, 89); // #384959 (Xanh đậm)
    public static final Color COLOR_PRIMARY = new Color(106, 137, 167); // #6A89A7 (Xanh thép)
    public static final Color COLOR_ACCENT = new Color(136, 189, 242); // #88BDF2 (Xanh sáng)
    public static final Color COLOR_BORDER = new Color(200, 210, 220); // Viền xám nhạt

    // === GÓC BO TRÒN ===
    public static final int CORNER_RADIUS = 12;

    // === PHÔNG CHỮ ===
    private static Font getFont(String preferredFont, int style, int size) {
        try {
            return new Font(preferredFont, style, size);
        } catch (Exception e) {
            System.err.println("Không tìm thấy phông chữ '" + preferredFont + "'. Sử dụng 'Arial' dự phòng.");
            return new Font("Arial", style, size);
        }
    }
    
    public static final Font FONT_TITLE = getFont("Segoe UI", Font.BOLD, 48);
    public static final Font FONT_SUBTITLE = getFont("Segoe UI", Font.BOLD, 36);
    public static final Font FONT_LABEL = getFont("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_INPUT = getFont("Segoe UI", Font.PLAIN, 20);
    public static final Font FONT_BUTTON = getFont("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_BUTTON_SMALL = getFont("Segoe UI", Font.BOLD, 18);
    

    // === VIỀN (BORDER) ===
    public static final Border BORDER_ROUNDED_INPUT = new CompoundBorder(
        new RoundedBorder(COLOR_PRIMARY, 1, CORNER_RADIUS),
        BorderFactory.createEmptyBorder(8, 12, 8, 12) 
    );
    
    public static final Border BORDER_ROUNDED_PANEL = new RoundedBorder(COLOR_BORDER, 1, CORNER_RADIUS);


    // === CÁC HÀM STYLE ===
    public static void styleButtonPrimary(JButton button) {
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 50));
        button.setFont(FONT_BUTTON);

        button.setBorder(new RoundedBorder(COLOR_PRIMARY, 0, CORNER_RADIUS)); 
        button.setContentAreaFilled(false); 

        // Thêm hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(COLOR_PRIMARY.darker());
                button.repaint(); 
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(COLOR_PRIMARY);
                button.repaint(); 
            }
        });

        button.setUI(new RoundedButtonUI());
    }

    public static void styleButtonSecondary(JButton button) {
        button.setBackground(COLOR_WHITE); // Nền trắng
        button.setForeground(COLOR_ACCENT); // Chữ màu xanh sáng
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(FONT_BUTTON_SMALL);
        
        button.setBorder(new RoundedBorder(COLOR_ACCENT, 1, CORNER_RADIUS)); 
        button.setContentAreaFilled(false); 
        button.setPreferredSize(new Dimension(150, 40));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(COLOR_PRIMARY); 
                ((RoundedBorder)button.getBorder()).setColor(COLOR_PRIMARY); 
                button.repaint();
            }
            public void mouseExited(MouseEvent evt) {
                button.setForeground(COLOR_ACCENT);
                ((RoundedBorder)button.getBorder()).setColor(COLOR_ACCENT);
                button.repaint();
            }
        });

        button.setUI(new RoundedButtonUI(true)); 
    }
    
    // --- Lớp hỗ trợ vẽ Border bo tròn ---
    public static class RoundedBorder extends AbstractBorder {
        private Color color;
        private int thickness;
        private int radius;

        public RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }
        
        public void setColor(Color newColor) {
            this.color = newColor;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(color);
            for (int i = 0; i < thickness; i++) {
                g2.draw(new RoundRectangle2D.Double(x + i, y + i, width - 1 - 2 * i, height - 1 - 2 * i, radius, radius));
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(radius / 2 + thickness, radius / 2 + thickness, radius / 2 + thickness, radius / 2 + thickness);
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = radius / 2 + thickness;
            return insets;
        }
    }
    
    // --- Lớp hỗ trợ vẽ nền bo tròn cho JButton, JTextField, JPasswordField, JComboBox ---
    public static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private boolean transparentBackground = false; 
        public RoundedButtonUI() {}
        public RoundedButtonUI(boolean transparentBackground) {
            this.transparentBackground = transparentBackground;
        }

        @Override
        public void paint(Graphics g, javax.swing.JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            JButton button = (JButton) c;
            int width = button.getWidth();
            int height = button.getHeight();

            if (!transparentBackground) {
                g2.setColor(button.getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS));
            }

            super.paint(g2, c);
            g2.dispose();
        }
    }
    
    // --- Lớp JPanel với nền bo tròn ---
    public static class RoundedPanel extends JPanel {
        public RoundedPanel(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(getBackground()); 
            g2.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS));
            g2.dispose();
        }
    }

    // --- Lớp JTextField với nền bo tròn ---
    public static class RoundedTextField extends JTextField {
        public RoundedTextField() {
            super();
            setOpaque(false);
            setBorder(new CompoundBorder(new RoundedBorder(COLOR_PRIMARY, 1, CORNER_RADIUS), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, CORNER_RADIUS, CORNER_RADIUS));
            super.paintComponent(g2); 
            g2.dispose();
        }
    }
    
    // --- Lớp JPasswordField với nền bo tròn ---
    public static class RoundedPasswordField extends JPasswordField {
        public RoundedPasswordField() {
            super();
            setOpaque(false);
            setBorder(new CompoundBorder(new RoundedBorder(COLOR_PRIMARY, 1, CORNER_RADIUS), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, CORNER_RADIUS, CORNER_RADIUS));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // --- Lớp JComboBox với nền bo tròn ---
    public static class RoundedComboBox<E> extends JComboBox<E> {
        public RoundedComboBox(E[] items) {
            super(items);
            setOpaque(false);
            setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton button = super.createArrowButton();
                    button.setBackground(Theme.COLOR_PRIMARY); 
                    button.setContentAreaFilled(false);
                    button.setBorder(BorderFactory.createEmptyBorder());
                    return button;
                }
                @Override
                public void paint(Graphics g, javax.swing.JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int width = c.getWidth();
                    int height = c.getHeight();

                    g2.setColor(Theme.COLOR_WHITE); 
                    g2.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS));

                    g2.setColor(Theme.COLOR_PRIMARY);
                    g2.draw(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS));

                    super.paint(g2, c);
                    g2.dispose();
                }
            });
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); 
        }
    }
}