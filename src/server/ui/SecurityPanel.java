package server.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class SecurityPanel extends JPanel {

    // --- UI Components ---
    private JTable tableBlocked;
    private DefaultTableModel modelBlocked;
    private JTextArea logArea;
    private JToggleButton btnLockdown;

    // --- Logic Variables ---
    private final Set<String> blockedIPs = ConcurrentHashMap.newKeySet();
    private final Set<String> whiteList = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> ipHitCount = new ConcurrentHashMap<>();

    public SecurityPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Khởi tạo WhiteList (IP Local)
        whiteList.add("127.0.0.1");
        whiteList.add("localhost");

        initControls();
        initViews();
        loadBlockedIPs();

        logSecurity("Security Panel initialized.");
    }

    private void initControls() {
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        ctrl.setOpaque(false);
        ctrl.setBorder(ServerGuiUtils.createSectionBorder("Security Controls"));

        // Button Add IP
        JButton btnAddIP = ServerGuiUtils.createStyledButton("+ Block IP", new Color(220, 53, 69), Color.WHITE);
        btnAddIP.addActionListener(e -> {
            String ip = JOptionPane.showInputDialog(this, "Nhập IP cần chặn:", "Block IP", JOptionPane.PLAIN_MESSAGE);
            if (ip != null && !ip.trim().isEmpty()) {
                blockIP(ip.trim(), "Manual Block");
            }
        });

        // Button Lockdown
        btnLockdown = new JToggleButton("Lockdown Mode: OFF") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed() || isSelected()) {
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
        btnLockdown.setFocusPainted(false);
        btnLockdown.setContentAreaFilled(false);
        btnLockdown.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLockdown.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnLockdown.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLockdown.setBackground(new Color(255, 240, 240));
        btnLockdown.addActionListener(e -> {
            boolean on = btnLockdown.isSelected();
            btnLockdown.setText("Lockdown Mode: " + (on ? "ON" : "OFF"));
            btnLockdown.setBackground(on ? new Color(255, 100, 100) : new Color(255, 240, 240));
            if (on)
                logSecurity("!!! LOCKDOWN ENABLED - Chỉ cho phép IP trong whitelist !!!");
            else
                logSecurity("Lockdown Disabled.");
        });

        // Button Unblock All
        JButton btnUnblockAll = ServerGuiUtils.createStyledButton("Unblock All IPs", new Color(0, 120, 215),
                Color.WHITE);
        btnUnblockAll.addActionListener(e -> unblockAllIps());

        // Button Add Whitelist
        JButton btnWhitelist = ServerGuiUtils.createStyledButton("+ Whitelist IP", new Color(40, 167, 69), Color.WHITE);
        btnWhitelist.addActionListener(e -> {
            String ip = JOptionPane.showInputDialog(this, "Nhập IP để thêm vào Whitelist:", "Add Whitelist",
                    JOptionPane.PLAIN_MESSAGE);
            if (ip != null && !ip.trim().isEmpty()) {
                whiteList.add(ip.trim());
                logSecurity("Added to Whitelist: " + ip.trim());
            }
        });

        ctrl.add(btnAddIP);
        ctrl.add(btnWhitelist);
        ctrl.add(btnLockdown);
        ctrl.add(btnUnblockAll);

        add(ctrl, BorderLayout.NORTH);
    }

    private void initViews() {
        // Table Panel
        modelBlocked = new DefaultTableModel(new String[] { "IP Address", "Reason", "Time Blocked" }, 0);
        tableBlocked = new JTable(modelBlocked);
        tableBlocked.setRowHeight(24);
        tableBlocked.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane scrollTable = new JScrollPane(tableBlocked);
        scrollTable.setBorder(ServerGuiUtils.createSectionBorder("Blocked IPs"));

        // Log Panel
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(ServerGuiUtils.createSectionBorder("Security Logs"));

        // Split Pane
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, scrollLog);
        split.setDividerLocation(280);
        split.setResizeWeight(0.5);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    private void blockIP(String ip, String reason) {
        if (whiteList.contains(ip)) {
            logSecurity("Cannot block whitelisted IP: " + ip);
            return;
        }

        if (blockedIPs.add(ip)) {
            SwingUtilities
                    .invokeLater(() -> modelBlocked.addRow(new Object[] { ip, reason, LocalTime.now().toString() }));
            logSecurity("BLOCKED: " + ip + " - Reason: " + reason);
            saveBlockedIPs();
        }
    }

    private void unblockAllIps() {
        if (blockedIPs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách IP chặn đang trống.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Gỡ chặn tất cả " + blockedIPs.size() + " IP?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            blockedIPs.clear();
            ipHitCount.clear();
            SwingUtilities.invokeLater(() -> modelBlocked.setRowCount(0));
            logSecurity("All IPs Unblocked.");
            saveBlockedIPs();
        }
    }

    private void logSecurity(String msg) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + time + "] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void saveBlockedIPs() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String ip : blockedIPs) {
                sb.append(ip).append("\n");
            }
            Files.writeString(Paths.get("blocked_ips.txt"), sb.toString());
        } catch (Exception e) {
            logSecurity("Error saving blocked IPs: " + e.getMessage());
        }
    }

    private void loadBlockedIPs() {
        try {
            File f = new File("blocked_ips.txt");
            if (f.exists()) {
                for (String line : Files.readAllLines(f.toPath())) {
                    if (!line.trim().isEmpty()) {
                        blockedIPs.add(line.trim());
                        SwingUtilities.invokeLater(() -> modelBlocked
                                .addRow(new Object[] { line.trim(), "Restored", "Previous Session" }));
                    }
                }
                if (!blockedIPs.isEmpty()) {
                    logSecurity("Restored " + blockedIPs.size() + " blocked IPs from file.");
                }
            }
        } catch (Exception e) {
            logSecurity("Error loading blocked IPs.");
        }
    }

    public boolean isBlocked(String ip) {
        return blockedIPs.contains(ip);
    }

    public boolean isLockdownMode() {
        return btnLockdown.isSelected();
    }

    public boolean isWhitelisted(String ip) {
        return whiteList.contains(ip);
    }
}
