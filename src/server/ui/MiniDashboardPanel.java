package server.ui;

import server.Client;
import server.Maintenance;
import server.Manager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Phiên bản thu nhỏ (Mini Panel) để theo dõi nhanh tình trạng Server
 */
public class MiniDashboardPanel extends JPanel {

    private JLabel lblCpu, lblRam, lblThreads, lblOnline, lblExp, lblMaintenance;
    private JCheckBox chkAlwaysOnTop;
    private final ServerManagerUI mainUI;

    public MiniDashboardPanel(ServerManagerUI mainUI) {
        this.mainUI = mainUI;
        initUI();
        startUpdateTimer();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 5));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(5, 12, 12, 12));

        // Header - Title and Pin Button on the same row
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        chkAlwaysOnTop = new JCheckBox("Always on Top (Ghim cửa sổ)", mainUI.isAlwaysOnTop());
        chkAlwaysOnTop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkAlwaysOnTop.setForeground(new Color(100, 100, 100));
        chkAlwaysOnTop.setOpaque(false);
        chkAlwaysOnTop.setFocusPainted(false);
        chkAlwaysOnTop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkAlwaysOnTop.addActionListener(e -> mainUI.setAlwaysOnTop(chkAlwaysOnTop.isSelected()));

        header.add(chkAlwaysOnTop, BorderLayout.WEST);

        // Metrics Grid (2 columns, 3 rows) - "Khối chữ nhật nhỏ"
        JPanel grid = new JPanel(new GridLayout(3, 2, 8, 8));
        grid.setOpaque(false);

        lblCpu = createMetricCard("CPU Usage", "0.0%", new Color(0, 123, 255));
        lblRam = createMetricCard("JVM RAM", "0 / 0 MB", new Color(111, 66, 193));
        lblThreads = createMetricCard("Threads", "0", new Color(40, 167, 69));
        lblOnline = createMetricCard("Online", "0", new Color(255, 193, 7));
        lblExp = createMetricCard("Exp Rate", "x1", new Color(255, 140, 0));
        lblMaintenance = createMetricCard("Status", "Checking...", new Color(220, 53, 69));

        grid.add(lblCpu);
        grid.add(lblRam);
        grid.add(lblThreads);
        grid.add(lblOnline);
        grid.add(lblExp);
        grid.add(lblMaintenance);

        // Footer - Button Maximize
        JButton btnMaximize = ServerGuiUtils.createStyledButton("Full size Panel", new Color(0, 102, 204), Color.WHITE);
        btnMaximize.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnMaximize.setPreferredSize(new Dimension(0, 35));
        btnMaximize.addActionListener(e -> mainUI.switchToFullMode());

        add(header, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
        add(btnMaximize, BorderLayout.SOUTH);
    }

    public void updateAlwaysOnTopState() {
        if (chkAlwaysOnTop != null) {
            chkAlwaysOnTop.setSelected(mainUI.isAlwaysOnTop());
        }
    }

    private JLabel createMetricCard(String title, String initialValue, Color accent) {
        JLabel card = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background shadow-like effect
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Border
                g2.setColor(new Color(230, 230, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Accent Bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 6, getHeight(), 12, 12);
                g2.fillRect(3, 0, 3, getHeight()); // Straighten internal edge

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setText("<html><body style='padding-left:10px;'><font color='gray' size='3'>" + title + ":</font><br>" +
                "<font color='#333333' size='4'><b>" + initialValue + "</b></font></body></html>");
        card.setBorder(new EmptyBorder(5, 5, 5, 5));
        return card;
    }

    private void updateMetric(JLabel label, String title, String value) {
        label.setText("<html><body style='padding-left:10px;'><font color='gray' size='3'>" + title + ":</font><br>" +
                "<font color='#333333' size='4'><b>" + value + "</b></font></body></html>");
    }

    private void startUpdateTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    updateMetrics();
                } catch (Exception e) {
                }
            }
        }, 0, 1000);
    }

    private void updateMetrics() {
        SwingUtilities.invokeLater(() -> {
            // Online
            if (Client.gI() != null) {
                int online = Client.gI().getPlayers().size();
                updateMetric(lblOnline, "Players Online", String.valueOf(online));
            }

            // RAM
            long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
            long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            long used = total - free;
            updateMetric(lblRam, "JVM RAM (Heap)", used + " / " + max + " MB");

            // Threads
            updateMetric(lblThreads, "Active Threads", String.valueOf(Thread.activeCount()));

            // CPU
            try {
                java.lang.management.OperatingSystemMXBean osBean = java.lang.management.ManagementFactory
                        .getOperatingSystemMXBean();
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    double cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
                    updateMetric(lblCpu, "System CPU", String.format("%.1f%%", Math.max(0, cpuLoad)));
                }
            } catch (Exception e) {
            }

            // Exp Rate
            updateMetric(lblExp, "Experience Rate", "x" + Manager.RATE_EXP_SERVER);

            // Maintenance
            try {
                if (Maintenance.isRunning) {
                    updateMetric(lblMaintenance, "Maintenance", "RUNNING NOW");
                } else {
                    java.util.Properties prop = new java.util.Properties();
                    try (java.io.FileInputStream fis = new java.io.FileInputStream("data/config/config.properties")) {
                        prop.load(fis);
                        boolean auto = Boolean.parseBoolean(prop.getProperty("server.autorestart", "false"));
                        if (auto) {
                            String h = prop.getProperty("server.maintenance.hour", "0");
                            String m = prop.getProperty("server.maintenance.min", "0");
                            updateMetric(lblMaintenance, "Time Restart (hh:mm)",
                                    h + ":" + String.format("%02d", Integer.parseInt(m)));
                        } else {
                            updateMetric(lblMaintenance, "Maintenance", "Ready");
                        }
                    }
                }
            } catch (Exception e) {
                updateMetric(lblMaintenance, "Maintenance", "N/A");
            }
        });
    }
}
