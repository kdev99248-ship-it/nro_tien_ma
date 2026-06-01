package server.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class ServerGuiUtils {

    public static void setupTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception e) {
            System.err.println("Failed to initialize LaF: " + e.getMessage());
        }
    }

    public static TitledBorder createSectionBorder(String title) {
        return BorderFactory.createTitledBorder(
                new LineBorder(new Color(220, 220, 220)), title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Color.DARK_GRAY);
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new javax.swing.border.EmptyBorder(8, 15, 8, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel createStyledLabel(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }

    public static Icon loadIcon(String path) {
        try {
            URL url = ServerGuiUtils.class.getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
        }
        return createFallbackIcon(path);
    }

    private static Icon createFallbackIcon(String path) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);

                String p = path.toLowerCase();

                if (p.contains("dashboard")) {
                    g2.setColor(new Color(0, 120, 215));
                    g2.fill(new Rectangle2D.Double(2, 2, 7, 7));
                    g2.fill(new Rectangle2D.Double(11, 2, 7, 7));
                    g2.fill(new Rectangle2D.Double(2, 11, 7, 7));
                    g2.fill(new Rectangle2D.Double(11, 11, 7, 7));
                } else if (p.contains("user") || p.contains("player")) {
                    g2.setColor(new Color(0, 153, 51)); // Màu xanh lá
                    g2.fill(new Ellipse2D.Double(5, 2, 10, 10)); // Đầu
                    g2.fill(new Arc2D.Double(2, 12, 16, 8, 0, 180, Arc2D.CHORD)); // Thân
                } else if (p.contains("account")) {
                    g2.setColor(new Color(65, 105, 225)); // Màu xanh hoàng gia
                    g2.fill(new RoundRectangle2D.Double(3, 5, 14, 10, 2, 2));
                    g2.setColor(new Color(173, 216, 230)); // Xanh nhạt
                    g2.fill(new Ellipse2D.Double(5, 7, 5, 5));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Rectangle2D.Double(11, 8, 4, 2));
                    g2.fill(new Rectangle2D.Double(11, 11, 3, 2));
                } else if (p.contains("shop")) {
                    g2.setColor(new Color(255, 87, 34));
                    Path2D roof = new Path2D.Double();
                    roof.moveTo(2, 4);
                    roof.lineTo(18, 4);
                    roof.lineTo(20, 9);
                    roof.lineTo(0, 9);
                    roof.closePath();
                    g2.fill(roof);
                    g2.setColor(new Color(255, 255, 255, 100));
                    g2.fillRect(5, 4, 2, 5);
                    g2.fillRect(10, 4, 2, 5);
                    g2.fillRect(15, 4, 2, 5);
                    g2.setColor(new Color(240, 230, 140));
                    g2.fillRect(3, 9, 14, 9);
                    g2.setColor(new Color(101, 67, 33));
                    g2.fillRect(8, 11, 4, 7);
                    g2.setColor(new Color(135, 206, 250));
                    g2.fillRect(4, 11, 3, 4);
                    g2.fillRect(13, 11, 3, 4);
                } else if (p.contains("gift")) {
                    g2.setColor(new Color(255, 69, 58));
                    g2.fill(new Rectangle2D.Double(3, 6, 14, 12));
                    g2.setColor(new Color(200, 30, 30));
                    g2.fill(new Rectangle2D.Double(2, 4, 16, 4));
                    g2.setColor(new Color(255, 215, 0));
                    g2.fill(new Rectangle2D.Double(8, 4, 4, 14));
                    g2.fill(new Ellipse2D.Double(6, 1, 4, 4));
                    g2.fill(new Ellipse2D.Double(10, 1, 4, 4));
                } else if (p.contains("topup") || p.contains("reward")) {
                    g2.setColor(new Color(102, 102, 255)); // Màu xanh tím
                    g2.fill(new RoundRectangle2D.Double(2, 5, 16, 10, 3, 3));
                    g2.setColor(new Color(255, 215, 0)); // Màu vàng chip
                    g2.fill(new Rectangle2D.Double(4, 8, 4, 3));
                    g2.setColor(new Color(255, 255, 255, 150));
                    g2.fill(new Rectangle2D.Double(4, 12, 12, 1));
                } else if (p.contains("shield") || p.contains("security")) {
                    g2.setColor(new Color(220, 53, 69));
                    Path2D pPath = new Path2D.Double();
                    pPath.moveTo(10, 1);
                    pPath.lineTo(18, 4);
                    pPath.lineTo(18, 10);
                    pPath.curveTo(18, 16, 10, 19, 10, 19);
                    pPath.curveTo(10, 19, 2, 16, 2, 10);
                    pPath.lineTo(2, 4);
                    pPath.closePath();
                    g2.fill(pPath);
                } else if (p.contains("firewall")) {
                    g2.setColor(new Color(255, 69, 0));
                    g2.fill(new Rectangle2D.Double(2, 4, 16, 12));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Rectangle2D.Double(4, 6, 2, 2));
                    g2.fill(new Rectangle2D.Double(8, 6, 2, 2));
                    g2.fill(new Rectangle2D.Double(12, 6, 2, 2));
                } else if (p.contains("calendar") || p.contains("event")) {
                    g2.setColor(new Color(102, 51, 153));
                    g2.fill(new Rectangle2D.Double(3, 4, 14, 13));
                    g2.setColor(Color.WHITE);
                    g2.fillRect(3, 4, 14, 4);
                } else if (p.contains("monster") || p.contains("boss")) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.fill(new Ellipse2D.Double(2, 2, 16, 16));
                    g2.setColor(Color.RED);
                    g2.fill(new Ellipse2D.Double(6, 7, 3, 3));
                    g2.fill(new Ellipse2D.Double(11, 7, 3, 3));
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fill(new Ellipse2D.Double(4, 4, 12, 12));
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }
}
