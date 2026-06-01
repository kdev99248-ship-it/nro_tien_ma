package server.ui;

import javax.swing.*;
import java.awt.*;

public class ShopEditorPanel extends JPanel {
    public ShopEditorPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Shop Editor - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}
