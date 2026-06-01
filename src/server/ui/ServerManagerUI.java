package server.ui;
import boss.BossConfig;

import server.ServerManager;
// import firewall.ProxyManager;
import linhManager.EmtiManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.Instant;

public class ServerManagerUI extends JFrame {

    // --- Class nội bộ quản lý Sidebar Item ---
    private static class NavItem {

        String name;
        Icon icon;
        String key;

        public NavItem(String name, String iconPath, String key) {
            this.name = name;
            this.key = key;
            this.icon = ServerGuiUtils.loadIcon(iconPath);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Instant serverStartTime;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JList<NavItem> sidebar;

    public static volatile boolean REQUEST_AUTO_RESTART = false;
    private JScrollPane scrollSidebar;
    private MiniDashboardPanel miniPanel;
    private Dimension lastFullSize = new Dimension(1600, 850);
    private JPanel sidebarWrapper;
    private JCheckBox chkAlwaysOnTop;

    public ServerManagerUI() {
        super("Server Control Panel - Manager");

        // Setup giao diện FlatLaf cho hiện đại (nếu có thư viện)
//        ServerGuiUtils.setupTheme();

//        initUI();
        startServerProcesses();

        this.serverStartTime = Instant.now();

        // Hook tắt server an toàn
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (REQUEST_AUTO_RESTART) {
                triggerRestartProcess();
            }
        }));
    }

    // --- Logic Restart Server ---
    public void triggerRestartProcess() {
        int seconds = 5;
        System.out.println(">>> Restarting Server in " + seconds + "s...");

        try {
            String currentDir = System.getProperty("user.dir");
            String osName = System.getProperty("os.name").toLowerCase();

            ProcessBuilder pb;
            if (osName.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "cmd", "/c",
                        "timeout /t " + seconds + " /nobreak && run.bat");
            } else {
                pb = new ProcessBuilder("bash", "-c", "sleep " + seconds + "; ./run.sh &");
            }

            pb.directory(new File(currentDir));
            pb.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Khởi tạo Giao diện ---
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245)); // Màu nền tổng thể sáng nhẹ

        // Danh sách Menu
        NavItem[] menuItems = {
                new NavItem("Dashboard", "/icon/dashboard.png", "Dashboard"),
                new NavItem("Account", "/icon/Account.png", "Account"),
                new NavItem("Players", "/icon/user2.png", "Players"),
                new NavItem("Bots", "/icon/user2.png", "Bots"),
                // new NavItem("Shop Items", "/icon/shop.png", "ShopEditor"),
                new NavItem("Giftcode", "/icon/gift.png", "Giftcode"),
                // new NavItem("Topup Reward", "/icon/topup.png", "TopupReward"),
                new NavItem("Events", "/icon/calendar.png", "Events"),
                // new NavItem("Boss Config", "/icon/monster.png", "Boss Config"),
                new NavItem("Mercenary", "/icon/monster.png", "Mercenary"),
                new NavItem("Security", "/icon/shield.png", "Security")
        };

        // Cấu hình Sidebar (JList)
        sidebar = new JList<>(menuItems);
        sidebar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sidebar.setSelectedIndex(0);
        sidebar.setFixedCellHeight(55); // Tăng chiều cao mỗi dòng
        sidebar.setBackground(new Color(255, 255, 255));
        sidebar.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Custom Renderer cho Sidebar đẹp hơn
        sidebar.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof NavItem) {
                    NavItem item = (NavItem) value;
                    lbl.setText(item.name);
                    if (item.icon != null) {
                        lbl.setIcon(item.icon);
                    }
                }

                lbl.setBorder(new EmptyBorder(0, 20, 0, 0)); // Padding trái
                lbl.setIconTextGap(15);
                lbl.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 14));

                if (isSelected) {
                    lbl.setBackground(new Color(230, 242, 255)); // Màu nền khi chọn (Xanh nhạt)
                    lbl.setForeground(new Color(0, 102, 204)); // Màu chữ khi chọn (Xanh đậm)
                    // Thêm vạch màu bên trái để đánh dấu
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0, 120, 215)),
                            new EmptyBorder(0, 16, 0, 0)));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(new Color(60, 60, 60));
                }
                return lbl;
            }
        });

        // Sidebar Container
        scrollSidebar = new JScrollPane(sidebar);
        scrollSidebar.setPreferredSize(new Dimension(260, getHeight())); // Rộng hơn chút
        scrollSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220))); // Viền phải nhẹ

        // Thêm nút Mini Mode xuống cuối sidebar
        sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.add(scrollSidebar, BorderLayout.CENTER);

        JPanel sidebarFooter = new JPanel();
        sidebarFooter.setLayout(new BoxLayout(sidebarFooter, BoxLayout.Y_AXIS));
        sidebarFooter.setOpaque(true);
        sidebarFooter.setBackground(Color.WHITE);
        sidebarFooter.setBorder(new EmptyBorder(5, 10, 5, 10));

        chkAlwaysOnTop = new JCheckBox("Always on Top (Ghim cửa sổ)", isAlwaysOnTop());
        chkAlwaysOnTop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkAlwaysOnTop.setForeground(new Color(100, 100, 100));
        chkAlwaysOnTop.setOpaque(false);
        chkAlwaysOnTop.setFocusPainted(false);
        chkAlwaysOnTop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkAlwaysOnTop.addActionListener(e -> setAlwaysOnTop(chkAlwaysOnTop.isSelected()));

        JButton btnMini = ServerGuiUtils.createStyledButton("Switch to Mini Mode", new Color(0, 51, 102), Color.WHITE);
        btnMini.addActionListener(e -> switchToMiniMode());
        btnMini.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        sidebarFooter.add(chkAlwaysOnTop);
        sidebarFooter.add(Box.createVerticalStrut(5));
        sidebarFooter.add(btnMini);

        sidebarWrapper.add(sidebarFooter, BorderLayout.SOUTH);

        add(sidebarWrapper, BorderLayout.WEST);

        // Content Panel (Chứa các màn hình chức năng)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); // Không viền thừa

        // Đăng ký các Panel con (với try-catch để tránh crash toàn bộ UI)
        try {
            contentPanel.add(new DashboardPanel(), "Dashboard");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Dashboard Error: " + e), "Dashboard");
        }
        try {
            contentPanel.add(new AccountPanel(), "Account");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Account Error: " + e), "Account");
        }
        try {
            contentPanel.add(new PlayersPanel(), "Players");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Players Error: " + e), "Players");
        }
        try {
            contentPanel.add(new BotManagerPanel(), "Bots");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Bots Error: " + e), "Bots");
        }
        try {
            contentPanel.add(new ShopEditorPanel(), "ShopEditor");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Shop Error: " + e), "ShopEditor");
        }
        try {
            contentPanel.add(new GiftcodePanel(), "Giftcode");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Giftcode Error: " + e), "Giftcode");
        }
        try {
            contentPanel.add(new TopupRewardPanel(), "TopupReward");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("TopupReward Error: " + e), "TopupReward");
        }
        try {
            contentPanel.add(new EventPanel(), "Events");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Events Error: " + e), "Events");
        }
        try {
            contentPanel.add(new BossEditorPanel(), "Boss Config");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("BossConfig Error: " + e), "Boss Config");
        }
        try {
            contentPanel.add(new MercenaryEditorPanel(), "Mercenary");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Mercenary Error: " + e), "Mercenary");
        }
        try {
            contentPanel.add(new SecurityPanel(), "Security");
        } catch (Throwable e) {
            e.printStackTrace();
            contentPanel.add(createErrorPanel("Security Error: " + e), "Security");
        }

        add(contentPanel, BorderLayout.CENTER);

        // Xử lý chuyển tab khi click sidebar
        sidebar.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                NavItem selected = sidebar.getSelectedValue();
                if (selected != null) {
                    cardLayout.show(contentPanel, selected.key);
                }
            }
        });

        // Cấu hình cửa sổ chính ban đầu
        setSize(lastFullSize);
        setMinimumSize(new Dimension(400, 300));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Sự kiện đóng cửa sổ an toàn
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        ServerManagerUI.this,
                        "Bạn có chắc muốn dừng Server và thoát chương trình?",
                        "Xác nhận tắt Server",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    shutdownServer();
                }
            }
        });

        // Mặc định khởi tạo ở chế độ Mini Mode như yêu cầu
        switchToMiniMode();
    }

    private void startServerProcesses() {
        System.out.println(">> [ServerManagerUI] PREPARE STARTING...");

        // Khởi động server trong thread riêng
        Thread serverThread = new Thread(() -> {
            System.out.println(">> [ServerManagerUI] THREAD STARTED. CALLING RUN...");
            try {
                ServerManager.gI().run();
            } catch (Throwable e) {
                System.err.println("FATAL ERROR IN SERVER START:");
                e.printStackTrace();
            }
        }, "ServerManager-Thread");

        serverThread.start();
        System.out.println(">> [ServerManagerUI] THREAD LAUNCHED.");

        // Start auto save if needed
        try {
            EmtiManager.getInstance().startAutoSave();
        } catch (Throwable t) {
            System.err.println("AutoSave Init Failed: " + t.getMessage());
        }

        // Move setVisible here
        try {
            Thread.sleep(500); // Wait a bit for logs
        } catch (Exception e) {
        }

        System.out.println(">> [ServerManagerUI] SHOWING UI...");
        // setVisible(true) sẽ được gọi bởi switchToMiniMode hoặc thủ công
        if (!isVisible()) {
            setVisible(true);
        }
    }

    public void switchToMiniMode() {
        if (sidebarWrapper.isVisible()) {
            lastFullSize = getSize();
        }

        // Hide sidebar
        sidebarWrapper.setVisible(false);

        // Setup mini panel
        if (miniPanel == null) {
            miniPanel = new MiniDashboardPanel(this);
        } else {
            miniPanel.updateAlwaysOnTopState();
        }

        // Remove contentPanel if it matches CENTER
        remove(contentPanel);
        add(miniPanel, BorderLayout.CENTER);

        // Adjust window for compact grid view
        setSize(360, 300); // Width enough for 2 columns, Height for 3 rows + button
        setResizable(false);
        revalidate();
        repaint();
    }

    public void switchToFullMode() {
        // Show sidebar
        sidebarWrapper.setVisible(true);

        // Restore content panel
        if (miniPanel != null) {
            remove(miniPanel);
        }
        add(contentPanel, BorderLayout.CENTER);

        // Restore window
        if (chkAlwaysOnTop != null) {
            chkAlwaysOnTop.setSelected(isAlwaysOnTop());
        }
        setSize(lastFullSize);
        setResizable(true);
        revalidate();
        repaint();
    }

    private void shutdownServer() {
        try {
            System.out.println(">> Đang lưu dữ liệu và đóng kết nối...");
            /*
             * if (ProxyManager.getInstance() != null) {
             * ProxyManager.getInstance().stopAll();
             * }
             */
            // EmtiManager doesn't support explicit stop, but we can assume System.exit
            // handles it
            /*
             * if (AutoSaveManager.getInstance() != null) {
             * AutoSaveManager.getInstance().stopAutoSave();
             * }
             */
        } catch (Exception e) {
            System.err.println("Lỗi khi đóng tài nguyên: " + e.getMessage());
        }

        System.out.println(">> Server shutting down... Bye!");
        System.exit(0);
    }

    private JPanel createErrorPanel(String message) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(255, 240, 240));
        JLabel lbl = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        lbl.setForeground(Color.RED);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        // Chạy trên luồng giao diện chuẩn Swing
        EventQueue.invokeLater(ServerManagerUI::new);
    }
}
