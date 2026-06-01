package server.ui;

import javax.swing.*;
import java.awt.*;

public class BossEditorPanel extends JPanel {
    public BossEditorPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Boss Config - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}
