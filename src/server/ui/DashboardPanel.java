package server.ui;
import boss.BossManager;
import boss.BossID;
import boss.BossData;
import boss.BossConfig;


import server.Client;
import server.Maintenance;
import server.Manager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

// New Imports for Boss Logic
// Imports removed
// import org.json.simple.JSONArray;
// import org.json.simple.JSONValue;
import jdbc.DBConnecter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

public class DashboardPanel extends JPanel {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // UI Components for live update
    private JLabel lblOnline, lblTime;
    private JLabel lblCpu, lblRam, lblThreads, lblSessions;
    private JLabel lblUptime;
    private JLabel lblGiftcodes;
    private JTextArea txtLogs;
    private JLabel lblStatusTitle;

    // Data mocks
    private long serverStartTime;

    // Config components
    private JComboBox<String> cbHour;
    private JComboBox<String> cbMin;
    private JCheckBox chkAutoRestart;
    private JTextField txtRate;
    private Thread maintenanceThread;

    // Auto Optimize
    private JCheckBox chkAutoOptimize;
    private JComboBox<String> cbOptimizeInterval;
    private JLabel lblOptStatus;
    private final java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors
            .newScheduledThreadPool(1);
    private java.util.concurrent.ScheduledFuture<?> activeAutoOptimizeFuture = null;

    // --- Boss Icon Logic ---
    private static final String ICON_FOLDER = "data/icon/";
    private static final String BOSS_MANAGER_PATH = "src/boss/BossManager.java";
    private final Map<Integer, Integer> partIconMap = new HashMap<>();
    private final Map<Integer, ImageIcon> iconImageCache = new HashMap<>();
    private List<BossSummonEntry> cachedBossEntries = new ArrayList<>();

    // Helper Class for Boss List
    private static class BossSummonEntry {
        String keyName;
        int id;
        String displayName;
        int headIconId;

        public BossSummonEntry(String keyName, int id, String displayName, int headIconId) {
            this.keyName = keyName;
            this.id = id;
            this.displayName = displayName;
            this.headIconId = headIconId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public DashboardPanel() {
        this.serverStartTime = System.currentTimeMillis();

        // Load Icons on init
        loadPartDataFromDB();

        initUI();
        startUpdateTimer();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // --- TOP HEADER ---
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN SCROLLABLE CONTENT ---
        JPanel contentBody = new JPanel();
        contentBody.setLayout(new BoxLayout(contentBody, BoxLayout.Y_AXIS));
        contentBody.setBackground(new Color(245, 245, 245));
        // Chỉnh sửa lề (padding) của toàn bộ nội dung: Top, Left, Bottom, Right
        // Tăng Left/Right nếu muốn nội dung "bóp" hẹp lại vào giữa
        contentBody.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Statistics Cards
        contentBody.add(createStatsRow());
        contentBody.add(Box.createVerticalStrut(15));

        // 3. Game Statistics & Boss Manager
        JPanel statsAndBoss = new JPanel(new GridLayout(1, 2, 15, 0));
        statsAndBoss.setOpaque(false);
        statsAndBoss.add(createSection("Game Statistics", createGameStatsPanel()));
        statsAndBoss.add(createSection("Boss Manager (Triệu Hồi & Cài Đặt)", createBossManagerPanel()));

        JPanel statsAndBossWrapper = new JPanel(new BorderLayout());
        statsAndBossWrapper.setOpaque(false);
        statsAndBossWrapper.add(statsAndBoss, BorderLayout.CENTER);
        contentBody.add(statsAndBossWrapper);
        contentBody.add(Box.createVerticalStrut(15));

        // 5. Configuration & Optimization
        JPanel configAndOpt = new JPanel(new GridLayout(1, 2, 15, 0));
        configAndOpt.setOpaque(false);
        configAndOpt.add(createSection("Server Configuration (Exp & Schedule)", createConfigPanel()));
        configAndOpt.add(createSection("System Optimization & Booster (Server Only)", createOptimizationPanel()));

        JPanel configWrapper = new JPanel(new BorderLayout());
        configWrapper.setOpaque(false);
        configWrapper.add(configAndOpt, BorderLayout.CENTER);
        contentBody.add(configWrapper);
        contentBody.add(Box.createVerticalStrut(15));

        // 6. Logs Section
        contentBody.add(createLogSection());

        JScrollPane scrollPane = new JScrollPane(contentBody);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    // --- SECTIONS ---

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        lblStatusTitle = new JLabel("Server Running Properly", SwingConstants.LEFT);
        lblStatusTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblStatusTitle.setForeground(new Color(255, 140, 0)); // Orange default

        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        rightInfo.setOpaque(false);

        lblTime = new JLabel("00:00:00");
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblTime.setForeground(Color.BLACK);

        rightInfo.add(lblTime);

        p.add(lblStatusTitle, BorderLayout.WEST);
        p.add(rightInfo, BorderLayout.EAST);

        return p;
    }

    private JPanel createStatsRow() {
        JPanel p = new JPanel(new GridLayout(1, 4, 15, 0));
        p.setOpaque(false);

        // Chỉnh height (110) và width.
        // Lưu ý: GridLayout chia đều width cho 4 ô.
        // Để chỉnh tổng chiều rộng, hãy thay đổi số 0 (full) thành số cụ thể (ví dụ
        // 1000) ở PreferredSize VÀ MaximumSize.
        p.setPreferredSize(new Dimension(0, 110)); // 0 = Full width
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110)); // Giãn hết cỡ theo chiều ngang

        lblCpu = new JLabel("0%");
        lblRam = new JLabel("0 / 0 MB");
        lblThreads = new JLabel("0");
        lblSessions = new JLabel("0");

        p.add(createMetricCard("Server CPU", lblCpu, "", new Color(0, 123, 255)));
        p.add(createMetricCard("JVM RAM (Heap)", lblRam, "", new Color(111, 66, 193)));
        p.add(createMetricCard("Threads", lblThreads, "", new Color(40, 167, 69)));
        p.add(createMetricCard("Player Online", lblSessions, "", new Color(255, 193, 7)));

        return p;
    }

    private JPanel createMetricCard(String title, JLabel valueLbl, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        // Padding: Top, Left, Bottom, Right
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.GRAY);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLbl.setForeground(Color.DARK_GRAY);

        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(30, 4));
        bar.setBackground(accent);
        JPanel barContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barContainer.setOpaque(false);
        barContainer.add(bar);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(valueLbl, BorderLayout.CENTER);
        if (!sub.isEmpty()) {
            JLabel s = new JLabel(sub);
            s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            s.setForeground(Color.LIGHT_GRAY);
            center.add(s, BorderLayout.SOUTH);
        }

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(barContainer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createSection(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        content.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(" " + title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setBorder(new EmptyBorder(0, 5, 5, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(lbl, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createGameStatsPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 5));
        p.setOpaque(false);

        lblGiftcodes = new JLabel("Giftcodes: N/A");
        p.add(lblGiftcodes);

        lblUptime = new JLabel("Uptime: 0d 00h 00m 00s");
        p.add(lblUptime);

        return p;
    }

    private JPanel createBossManagerPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(false);

        JButton btnOpenMenu = createFlatButton("Danh sách boss(Search & Call Boss)", new Color(0, 110, 220),
                Color.WHITE);
        btnOpenMenu.setPreferredSize(new Dimension(300, 35));

        btnOpenMenu.addActionListener(e -> showBossSummonDialog());

        p.add(btnOpenMenu);
        return p;
    }

    private JPanel createConfigPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        p.setOpaque(false);

        p.add(new JLabel("Rate: x"));
        txtRate = new JTextField(String.valueOf(Manager.RATE_EXP_SERVER), 3);
        p.add(txtRate);
        JButton btnSetExp = createFlatButton("Set EXP", new Color(0, 110, 220), Color.WHITE);
        p.add(btnSetExp);

        btnSetExp.addActionListener(e -> {
            try {
                saveServerConfig(false);
                int rate = Manager.RATE_EXP_SERVER;
                JOptionPane.showMessageDialog(this, "Đã set EXP Rate: x" + rate + " và lưu config!");
                log("Set EXP Rate: x" + rate);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
            }
        });

        p.add(Box.createHorizontalStrut(20));

        p.add(new JLabel("Hẹn giờ:"));
        cbHour = new JComboBox<>();
        for (int i = 0; i < 24; i++)
            cbHour.addItem(String.format("%02d", i));

        cbMin = new JComboBox<>();
        for (int i = 0; i < 60; i++)
            cbMin.addItem(String.format("%02d", i));

        p.add(cbHour);
        p.add(new JLabel(":"));
        p.add(cbMin);

        chkAutoRestart = new JCheckBox("AutoRestart");
        chkAutoRestart.setOpaque(false);
        p.add(chkAutoRestart);

        JButton btnSaveConfig = createFlatButton("Lưu Cấu Hình", new Color(0, 110, 220), Color.WHITE);
        p.add(btnSaveConfig);

        btnSaveConfig.addActionListener(e -> saveServerConfig(true));

        // Load initial config
        loadServerConfig();

        return p;
    }

    private void loadServerConfig() {
        try (java.io.FileInputStream fis = new java.io.FileInputStream("data/config/config.properties")) {
            java.util.Properties prop = new java.util.Properties();
            prop.load(fis);

            // EXP
            String expObj = prop.getProperty("server.expserver");
            if (expObj != null) {
                try {
                    Manager.RATE_EXP_SERVER = Byte.parseByte(expObj);
                    txtRate.setText(String.valueOf(Manager.RATE_EXP_SERVER));
                } catch (Exception e) {
                }
            }

            // Maintenance
            try {
                int scheduleHour = Integer.parseInt(prop.getProperty("server.maintenance.hour", "-1"));
                int scheduleMin = Integer.parseInt(prop.getProperty("server.maintenance.min", "-1"));
                boolean isAutoRestart = Boolean.parseBoolean(prop.getProperty("server.autorestart", "false"));

                if (scheduleHour != -1 && scheduleMin != -1) {
                    if (scheduleHour >= 0 && scheduleHour < 24)
                        cbHour.setSelectedIndex(scheduleHour);
                    if (scheduleMin >= 0 && scheduleMin < 60)
                        cbMin.setSelectedIndex(scheduleMin);
                }
                chkAutoRestart.setSelected(isAutoRestart);

                if (scheduleHour != -1 && scheduleMin != -1 && isAutoRestart) {
                    startMaintenanceThread(scheduleHour, scheduleMin);
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            log("Lỗi load config: " + e.getMessage());
        }
    }

    private void saveServerConfig(boolean showSuccess) {
        try {
            // Update Data game variable
            int rate = Integer.parseInt(txtRate.getText().trim());
            Manager.RATE_EXP_SERVER = (byte) rate;

            int h = Integer.parseInt(cbHour.getSelectedItem().toString());
            int m = Integer.parseInt(cbMin.getSelectedItem().toString());
            boolean auto = chkAutoRestart.isSelected();

            // Prepare updates
            java.util.Map<String, String> updates = new java.util.HashMap<>();
            updates.put("server.expserver", String.valueOf(rate));
            updates.put("server.maintenance.hour", String.valueOf(h));
            updates.put("server.maintenance.min", String.valueOf(m));
            updates.put("server.autorestart", String.valueOf(auto));

            java.io.File f = new java.io.File("data/config/config.properties");
            java.util.List<String> lines = new java.util.ArrayList<>();
            if (f.exists()) {
                lines = java.nio.file.Files.readAllLines(f.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            }

            java.util.List<String> newLines = new java.util.ArrayList<>();
            java.util.Set<String> processedKeys = new java.util.HashSet<>();

            for (String line : lines) {
                String trimmed = line.trim();
                boolean replaced = false;
                if (!trimmed.startsWith("#") && trimmed.contains("=")) {
                    String[] parts = trimmed.split("=", 2);
                    String key = parts[0].trim();
                    if (updates.containsKey(key)) {
                        newLines.add(key + "=" + updates.get(key));
                        processedKeys.add(key);
                        replaced = true;
                    }
                }
                if (!replaced) {
                    newLines.add(line);
                }
            }

            // Append new keys if they didn't exist
            if (processedKeys.size() < updates.size()) {
                newLines.add(""); // Empty line for separation
                newLines.add("# DASHBOARD AUTOGEN");
                for (java.util.Map.Entry<String, String> entry : updates.entrySet()) {
                    if (!processedKeys.contains(entry.getKey())) {
                        newLines.add(entry.getKey() + "=" + entry.getValue());
                    }
                }
            }

            java.nio.file.Files.write(f.toPath(), newLines, java.nio.charset.StandardCharsets.UTF_8);

            // Start thread
            if (auto) {
                startMaintenanceThread(h, m);
                log("Đã lưu config & hẹn giờ bảo trì lúc " + String.format("%02d:%02d", h, m));
            } else {
                if (maintenanceThread != null)
                    maintenanceThread.interrupt();
                log("Đã lưu config. Tắt hẹn giờ bảo trì.");
            }

            if (showSuccess)
                JOptionPane.showMessageDialog(this, "Lưu cấu hình thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu config: " + e.getMessage());
        }
    }

    private void startMaintenanceThread(int h, int m) {
        if (maintenanceThread != null && maintenanceThread.isAlive()) {
            maintenanceThread.interrupt();
        }

        maintenanceThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    java.time.LocalTime now = java.time.LocalTime.now();
                    if (now.getHour() == h && now.getMinute() == m && now.getSecond() == 0) {
                        try {
                            SwingUtilities.invokeLater(() -> log("AUTORESTART: Đang thực hiện bảo trì tự động..."));
                            Maintenance.gI().start(120); // Bảo trì sau 2 phút (120s)
                        } catch (Exception ex) {
                        }
                        break;
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        maintenanceThread.start();
    }

    private JPanel createOptimizationPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        // 1. Buttons Panel
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setOpaque(false);

        JButton btnMaintain = createFlatButton("Bảo Trì (2p)", new Color(255, 165, 0), Color.BLACK);
        JButton btnGc = createFlatButton("Dọn dẹp JVM RAM", new Color(40, 167, 69), Color.WHITE);
        JButton btnOpt = createFlatButton("Tối ưu CPU & VPS", new Color(0, 110, 220), Color.WHITE);
        JButton btnLog = createFlatButton("Xóa Log Cache", new Color(108, 117, 125), Color.WHITE);

        btnMaintain.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn bảo trì sau 2 phút?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Maintenance.gI().start(120);
                    log("Đã kích hoạt bảo trì 2 phút.");
                } catch (Exception ex) {
                    log("Lỗi lệnh bảo trì: " + ex.getMessage());
                }
            }
        });

        btnGc.addActionListener(e -> performRamCleanup());
        btnOpt.addActionListener(e -> performCpuOptimization());
        btnLog.addActionListener(e -> {
            txtLogs.setText("");
            log("System: Log cache cleared manually.");
        });

        p.add(btnMaintain);
        p.add(btnGc);
        p.add(btnOpt);
        p.add(btnLog);

        // 2. Auto Opt UI Panel
        JPanel pAuto = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pAuto.setOpaque(false);
        pAuto.setBorder(new EmptyBorder(0, 5, 5, 0));

        chkAutoOptimize = new JCheckBox("Tự động tối ưu hóa");
        chkAutoOptimize.setOpaque(false);
        chkAutoOptimize.setFont(new Font("Segoe UI", Font.BOLD, 12));

        String[] intervals = { "5 Phút", "10 Phút", "30 Phút", "60 Phút" };
        cbOptimizeInterval = new JComboBox<>(intervals);
        cbOptimizeInterval.setSelectedIndex(1); // 10 min

        lblOptStatus = new JLabel("Trạng thái: Tắt");
        lblOptStatus.setForeground(Color.RED);

        chkAutoOptimize.addActionListener(e -> toggleAutoOptimization());
        cbOptimizeInterval.addActionListener(e -> {
            if (chkAutoOptimize.isSelected())
                toggleAutoOptimization();
        });

        pAuto.add(chkAutoOptimize);
        pAuto.add(new JLabel("Mỗi:"));
        pAuto.add(cbOptimizeInterval);
        pAuto.add(Box.createHorizontalStrut(15));
        pAuto.add(lblOptStatus);

        // Add to Container
        container.add(p, BorderLayout.NORTH);
        container.add(pAuto, BorderLayout.CENTER);

        return container;
    }

    private void toggleAutoOptimization() {
        if (activeAutoOptimizeFuture != null) {
            activeAutoOptimizeFuture.cancel(false);
            activeAutoOptimizeFuture = null;
        }

        if (chkAutoOptimize.isSelected()) {
            String selected = (String) cbOptimizeInterval.getSelectedItem();
            int minutes = 10;
            if (selected.contains("5"))
                minutes = 5;
            else if (selected.contains("30"))
                minutes = 30;
            else if (selected.contains("60"))
                minutes = 60;

            lblOptStatus.setText("Trạng thái: Đang chạy (" + minutes + "p/lần)");
            lblOptStatus.setForeground(new Color(0, 153, 51));

            activeAutoOptimizeFuture = scheduler.scheduleAtFixedRate(() -> {
                log("AUTO-OPT: Bắt đầu chu trình tối ưu tự động...");
                performRamCleanup();
            }, minutes, minutes, java.util.concurrent.TimeUnit.MINUTES);

            log("SYSTEM: Đã bật tự động tối ưu hóa (" + minutes + " phút/lần).");
        } else {
            lblOptStatus.setText("Trạng thái: Tắt");
            lblOptStatus.setForeground(Color.RED);
            log("SYSTEM: Đã tắt tự động tối ưu hóa.");
        }
    }

    private JPanel createLogSection() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Server Logs"));

        txtLogs = new JTextArea(8, 50);
        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(txtLogs);

        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();

                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Add border if background is light
        boolean isLight = (bg.getRed() > 220 && bg.getGreen() > 220 && bg.getBlue() > 220);
        if (isLight) {
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        } else {
            b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        }

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            String time = LocalDateTime.now().format(timeFormatter);
            txtLogs.append("[" + time + "] " + msg + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        });
    }

    private void performRamCleanup() {
        new Thread(() -> {
            long before = Runtime.getRuntime().freeMemory();
            System.gc();
            long after = Runtime.getRuntime().freeMemory();
            long freed = after - before;
            long freedMB = freed / 1024 / 1024;
            String msg = (freed > 0)
                    ? "OPTIMIZE: Đã dọn dẹp JVM Heap. Giải phóng: " + freedMB + " MB."
                    : "OPTIMIZE: JVM RAM đã ở trạng thái tối ưu.";
            log(msg);
        }).start();
    }

    private void performCpuOptimization() {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                if (txtLogs.getDocument().getLength() > 50000) {
                    txtLogs.setText(""); // Clear if too big
                    log("CPU OPT: Đã xóa bộ đệm Log để giảm tải UI.");
                } else {
                    log("CPU OPT: Bộ đệm Log ổn định. Chưa cần xóa.");
                }
            });
            // System.runFinalization(); // MUBI doesn't rely heavily on this but it helps
            // clean up native resources
            // For now just log check
        }).start();
    }

    private void startUpdateTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    updateMetrics();
                } catch (Exception e) {
                    // ignore
                }
            }
        }, 0, 1000);
    }

    private void updateMetrics() {
        SwingUtilities.invokeLater(() -> {
            lblTime.setText(LocalDateTime.now().format(timeFormatter));

            lblThreads.setText(String.valueOf(Thread.activeCount()));

            if (Client.gI() != null) {
                int online = Client.gI().getPlayers().size();
                lblSessions.setText(String.valueOf(online));
            }

            // RAM Calculation
            long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            long used = total - free;
            lblRam.setText(used + " / " + max + " MB");

            // CPU Calculation
            try {
                java.lang.management.OperatingSystemMXBean osBean = java.lang.management.ManagementFactory
                        .getOperatingSystemMXBean();
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                    double cpuLoad = sunOsBean.getProcessCpuLoad() * 100; // Use getSystemCpuLoad() for full system
                    // If cpuLoad value is negative (unavailable), show 0.0
                    if (cpuLoad < 0)
                        cpuLoad = 0.0;
                    lblCpu.setText(String.format("%.1f%%", cpuLoad));
                } else {
                    lblCpu.setText("N/A");
                }
            } catch (Exception e) {
                lblCpu.setText("Err");
            }

            long diff = System.currentTimeMillis() - serverStartTime;
            long seconds = diff / 1000;
            long d = seconds / 86400;
            long h = (seconds % 86400) / 3600;
            long m = (seconds % 3600) / 60;
            long s = seconds % 60;
            lblUptime.setText(String.format("Uptime: %dd %02dh %02dm %02ds", d, h, m, s));

            if (Maintenance.isRunning) {
                lblStatusTitle.setText("Maintenance Scheduled");
                lblStatusTitle.setForeground(Color.RED);
            } else {
                lblStatusTitle.setText("Server Running Properly");
                lblStatusTitle.setForeground(new Color(255, 140, 0));
            }

            if (models.GiftCode.GiftCodeManager.gI() != null) {
                lblGiftcodes.setText("Giftcodes: " + models.GiftCode.GiftCodeManager.gI().listGiftCode.size());
            }
        });
    }

    // --- BOSS UI LOGIC ---

    private void loadPartDataFromDB() {
        new Thread(() -> {
            try (Connection conn = DBConnecter.getConnectionServer();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, DATA FROM part WHERE TYPE = 0")) {

                partIconMap.clear();
                while (rs.next()) {
                    int partId = rs.getInt("id");
                    String json = rs.getString("DATA");
                    try {
                        // Regex based parsing to avoid library dependency issues
                        // Structure is usually [[iconId], ...] or [[iconId, ...], ...]
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\[\\s*\\[\\s*(\\d+)");
                        java.util.regex.Matcher m = p.matcher(json);
                        if (m.find()) {
                            int iconId = Integer.parseInt(m.group(1));
                            partIconMap.put(partId, iconId);
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                log("Error loading Part DB for Icons: " + e.getMessage());
            }
        }).start();
    }

    private ImageIcon getIconByIconId(int iconId, int size) {
        if (iconId <= -1)
            return null;
        if (iconImageCache.containsKey(iconId)) {
            Image img = iconImageCache.get(iconId).getImage();
            if (img.getWidth(null) != size) {
                return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
            return iconImageCache.get(iconId);
        }
        try {
            String[] zoomLevels = { "x4", "x3", "x2", "x1" };
            for (String zoom : zoomLevels) {
                File f = new File(ICON_FOLDER + zoom + "/" + iconId + ".png");
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    Image dimg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(dimg);
                    iconImageCache.put(iconId, icon);
                    return icon;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void prepareBossData() {
        cachedBossEntries.clear();
        try {
            // Method: Load from Database via BossManager
            List<BossConfig> configs = BossManager.gI().getAllBossConfigs();
            for (BossConfig config : configs) {
                int bossId = config.getBossId();
                String displayName = config.getName();
                int iconId = -1;
                
                // Try to get icon from the first level's outfit
                BossData[] dataArr = config.getData();
                if (dataArr != null && dataArr.length > 0) {
                    short[] outfit = dataArr[0].getOutfit();
                    if (outfit != null && outfit.length > 0) {
                        int headPart = outfit[0];
                        iconId = partIconMap.getOrDefault(headPart, headPart);
                    }
                }
                
                String key = "ID_" + bossId; // Use ID as key if symbolic name is not available
                cachedBossEntries.add(new BossSummonEntry(key, bossId, displayName, iconId));
            }
        } catch (Exception e) {
            log("Error loading boss data from database: " + e.getMessage());
        }
    }

    private void prepareBossDataFallback() {
        // Fallback is no longer needed with DB system
    }


    private void showBossSummonDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog d = new JDialog(window != null ? (Frame) window : null, "Triệu Hồi Boss (Searchable)", true);
        d.setSize(500, 600);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout(5, 5));

        if (cachedBossEntries.isEmpty()) {
            prepareBossData();
        }

        JTextField txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Nhập tên boss để tìm..."));

        DefaultListModel<BossSummonEntry> listModel = new DefaultListModel<>();
        cachedBossEntries.forEach(listModel::addElement);

        JList<BossSummonEntry> list = new JList<>(listModel);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof BossSummonEntry) {
                    BossSummonEntry entry = (BossSummonEntry) value;
                    lbl.setText(entry.displayName);
                    if (entry.headIconId != -1) {
                        ImageIcon icon = getIconByIconId(entry.headIconId, 25);
                        if (icon != null)
                            lbl.setIcon(icon);
                    }
                    lbl.setIconTextGap(10);
                }
                return lbl;
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            void filter() {
                String text = txtSearch.getText().toLowerCase();
                listModel.clear();
                for (BossSummonEntry entry : cachedBossEntries) {
                    if (entry.displayName.toLowerCase().contains(text) || entry.keyName.toLowerCase().contains(text)) {
                        listModel.addElement(entry);
                    }
                }
            }
        });

        JButton btnSummon = createFlatButton("TRIỆU HỒI NGAY", new Color(40, 167, 69), Color.WHITE);
        btnSummon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSummon.setPreferredSize(new Dimension(0, 40));

        Runnable doSummon = () -> {
            BossSummonEntry selected = list.getSelectedValue();
            if (selected != null) {
                try {
                    BossManager.gI().createBoss(selected.id);
                    log("SUMMON: Đã triệu hồi boss " + selected.displayName);
                    JOptionPane.showMessageDialog(d, "Đã triệu hồi thành công:\n" + selected.displayName);
                } catch (Exception ex) {
                    log("Error Summon: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(d, "Vui lòng chọn một Boss!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        };

        btnSummon.addActionListener(e -> doSummon.run());
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    doSummon.run();
            }
        });

        d.add(txtSearch, BorderLayout.NORTH);
        d.add(new JScrollPane(list), BorderLayout.CENTER);
        d.add(btnSummon, BorderLayout.SOUTH);
        d.setVisible(true);
    }
}