package server.ui;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import jdbc.DBConnecter;

public class AccountPanel extends JPanel {

    // ========================================================================
    // 1. CẤU HÌNH GIAO DIỆN
    // ========================================================================
    private static final String ICON_FOLDER = "data/icon/";

    // Fonts
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_DATA = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_NUM = new Font("Consolas", Font.BOLD, 14); // Font số

    // Colors
    private static final Color COL_PRIMARY = new Color(0, 120, 215);
    private static final Color COL_BG = Color.WHITE;
    private static final Color COL_SECTION_BG = new Color(250, 252, 255);
    private static final Color COL_BORDER = new Color(220, 220, 220);
    private static final Color COL_TEXT_GRAY = new Color(100, 100, 100);

    // Format Date
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    // Data Cache
    private final Map<Integer, Integer> partHeadIconMap = new HashMap<>();
    private final Map<Integer, ImageIcon> headCache = new HashMap<>();

    // Components
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public AccountPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(COL_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initUI();
        loadHeadPartCache(); // Cache icons first
    }

    // ========================================================================
    // 2. GIAO DIỆN CHÍNH (MAIN LIST)
    // ========================================================================
    private void initUI() {
        // --- Header ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COL_BG);

        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(60, 60, 60));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(COL_BG);

        txtSearch = new JTextField(25);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm ID, Username, Tên nhân vật...");
        txtSearch.setPreferredSize(new Dimension(300, 40));
        txtSearch.setFont(FONT_DATA);
        txtSearch.putClientProperty("JTextField.showClearButton", true);

        JButton btnSearch = createButton("Tìm kiếm", COL_PRIMARY);
        JButton btnReload = createButton("Làm mới", new Color(40, 167, 69));

        btnSearch.addActionListener(e -> searchData(txtSearch.getText()));
        btnReload.addActionListener(e -> {
            txtSearch.setText("");
            loadData();
        });

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReload);

        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = { "Head", "ID", "Tài khoản", "Tên NV", "Mật khẩu", "Trạng thái", "Ban", "VND", "Tổng Nạp",
                "Is Admin" };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? ImageIcon.class : Object.class;
            }
        };

        table = new JTable(model);
        setupTableStyle();

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 1).toString());
                    openEditDialog(id);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void setupTableStyle() {
        table.setFont(FONT_DATA);
        table.setRowHeight(55);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COL_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        // Fix header background not showing on some LAFs
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                l.setBackground(COL_PRIMARY);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setOpaque(true); // Force background paint
                l.setBorder(new MatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 50))); // Divider
                return l;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected)
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                else
                    c.setBackground(new Color(220, 235, 255));

                setHorizontalAlignment(JLabel.CENTER);
                setForeground(Color.BLACK);

                if (column == 1) { // ID
                    setFont(FONT_NUM);
                    setForeground(new Color(0, 128, 128)); // Teal
                } else if (column == 2) { // Tài khoản
                    setFont(FONT_BOLD);
                    setForeground(new Color(60, 60, 60)); // Dark Gray
                } else if (column == 3) { // Tên NV
                    setFont(FONT_BOLD);
                    setForeground(new Color(0, 102, 204));
                } else if (column == 4) { // Mật khẩu
                    setFont(FONT_DATA);
                    setForeground(Color.GRAY);
                } else if (column == 5) {
                    setFont(FONT_BOLD);
                    String s = value.toString();
                    if (s.contains("Active"))
                        setForeground(new Color(0, 150, 0));
                    else
                        setForeground(Color.GRAY);
                } else if (column == 6) {
                    setFont(FONT_BOLD);
                    if ("YES".equals(value))
                        setForeground(Color.RED);
                    else
                        setForeground(new Color(0, 128, 0));
                } else if (column == 7 || column == 8) {
                    setFont(FONT_NUM);
                    setForeground(new Color(153, 0, 153));
                } else if (column == 9) { // Is Admin
                    setFont(FONT_BOLD);
                    if ("YES".equals(value))
                        setForeground(Color.RED);
                    else
                        setForeground(new Color(0, 128, 0));
                } else {
                    setFont(FONT_DATA);
                }

                if (c instanceof JComponent)
                    ((JComponent) c).setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
                return c;
            }
        });

        // Widths
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(60);
        cm.getColumn(1).setPreferredWidth(50);
        cm.getColumn(2).setPreferredWidth(120);
        cm.getColumn(3).setPreferredWidth(120);
        cm.getColumn(4).setPreferredWidth(80);
    }

    // ========================================================================
    // 3. EDIT DIALOG (FIXED LAYOUT)
    // ========================================================================
    private void openEditDialog(int accountId) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chỉnh sửa ID: " + accountId, true);
        d.setSize(600, 500);
        d.setLocationRelativeTo(null);
        d.setLayout(new BorderLayout());
        d.setBackground(COL_BG);

        // --- PREPARE FIELDS ---
        JTextField txtUser = createField(false);
        JTextField txtPass = createField(true);
        // JTextField txtEmail = createField(true); // Tạm bỏ email nếu không cần

        JCheckBox chkActive = new JCheckBox("Kích hoạt (Active)");
        JCheckBox chkBan = new JCheckBox("Khóa (Ban)");
        chkBan.setForeground(Color.RED);
        JCheckBox chkAdmin = new JCheckBox("Is Admin");

        // JTextField txtRole = createField(true);
        // JTextField txtVip = createField(true);

        JTextField txtVnd = createField(true);
        txtVnd.setFont(FONT_NUM);
        txtVnd.setForeground(Color.RED);
        JTextField txtTongNap = createField(true);
        txtTongNap.setFont(FONT_NUM);
        txtTongNap.setForeground(Color.BLUE);
        JTextField txtCoin = createField(true); // Nếu có

        final int[] headInfo = { -1 }; // wrapper for head id

        // --- LOAD DATA ---
        loadAccountData(accountId, txtUser, txtPass, chkActive, chkBan, chkAdmin, txtVnd, txtTongNap, headInfo);

        // --- BUILD UI ---
        JPanel pMain = new JPanel(new GridBagLayout());
        pMain.setBackground(COL_BG);
        pMain.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0); // Spacing bottom
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // 1. PROFILE SECTION
        JPanel pProfile = createSectionPanel("THÔNG TIN TÀI KHOẢN");
        pProfile.setLayout(new GridBagLayout()); // Use GridBag for flexible layout inside

        GridBagConstraints gp = new GridBagConstraints();
        gp.insets = new Insets(5, 5, 5, 10);
        gp.fill = GridBagConstraints.HORIZONTAL;

        // Avatar (Left)
        JLabel lblAvt = new JLabel(getAvatar(headInfo[0], txtUser.getText(), 80));
        gp.gridx = 0;
        gp.gridy = 0;
        gp.gridheight = 3;
        gp.weightx = 0;
        pProfile.add(lblAvt, gp);

        // Reset gridheight
        gp.gridheight = 1;
        gp.weightx = 1.0;

        // Row 1: User, Pass
        JPanel pRow1 = new JPanel(new GridLayout(1, 2, 15, 0));
        pRow1.setOpaque(false);
        pRow1.add(createInputGroup("Tài khoản:", txtUser));
        pRow1.add(createInputGroup("Mật khẩu:", txtPass));

        gp.gridx = 1;
        gp.gridy = 0;
        pProfile.add(pRow1, gp);

        // Row 3: Checkboxes
        JPanel pRow3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pRow3.setOpaque(false);
        styleCheck(chkActive);
        styleCheck(chkBan);
        styleCheck(chkAdmin);
        pRow3.add(chkActive);
        pRow3.add(chkBan);
        pRow3.add(chkAdmin);

        gp.gridx = 1;
        gp.gridy = 2;
        pProfile.add(pRow3, gp);

        gbc.gridy = 0;
        pMain.add(pProfile, gbc);

        // 2. ASSETS SECTION
        JPanel pAsset = new JPanel(new GridLayout(1, 2, 15, 0)); // 2 columns equal width
        pAsset.setOpaque(false);
        pAsset.add(createBorderedGroup("VND (Coin)", txtVnd));
        pAsset.add(createBorderedGroup("Tổng Nạp", txtTongNap));

        gbc.gridy = 1;
        pMain.add(pAsset, gbc);

        // --- BUTTONS ---
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBtn.setBackground(new Color(245, 245, 245));
        pBtn.setBorder(new MatteBorder(1, 0, 0, 0, COL_BORDER));

        JButton btnSave = createButton("LƯU THAY ĐỔI", new Color(0, 120, 215));
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnCancel = createButton("Đóng", new Color(108, 117, 125));
        btnCancel.addActionListener(e -> d.dispose());

        btnSave.addActionListener(e -> {
            saveAccount(d, accountId, txtPass, chkActive, chkBan, chkAdmin, txtVnd, txtTongNap);
        });

        pBtn.add(btnCancel);
        pBtn.add(btnSave);

        d.add(pMain, BorderLayout.CENTER);
        d.add(pBtn, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ========================================================================
    // 4. DATA LOGIC (LOAD & SAVE)
    // ========================================================================
    private void loadAccountData(int id, JTextField user, JTextField pass,
            JCheckBox act, JCheckBox ban, JCheckBox adm,
            JTextField vnd, JTextField tongnap, int[] headRef) {
        try (Connection conn = DBConnecter.getConnectionServer();
                // Chú ý: Cần chắc chắn tên cột trong bảng account của TOMAHOC
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT a.*, (SELECT head FROM player WHERE account_id = a.id LIMIT 1) as p_head FROM account a WHERE a.id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                headRef[0] = rs.getInt("p_head");
                user.setText(rs.getString("username"));
                try {
                    pass.setText(rs.getString("password"));
                } catch (Exception ignored) {
                }

                try {
                    act.setSelected(rs.getInt("active") == 1);
                } catch (Exception ignored) {
                }
                try {
                    ban.setSelected(rs.getInt("ban") == 1);
                } catch (Exception ignored) {
                }
                try {
                    adm.setSelected(rs.getInt("is_admin") == 1);
                } catch (Exception ignored) {
                } // Hoặc isAdmin

                // Thử đọc các field tiền tệ, nếu không có thì bỏ qua
                try {
                    vnd.setText(String.valueOf(rs.getInt("vnd")));
                } catch (Exception e) {
                    try {
                        vnd.setText(String.valueOf(rs.getInt("cash")));
                    } catch (Exception ex) {
                        vnd.setText("0");
                    }
                }

                try {
                    tongnap.setText(String.valueOf(rs.getInt("tongnap")));
                } catch (Exception e) {
                    try {
                        tongnap.setText(String.valueOf(rs.getInt("danap")));
                    } catch (Exception ex) {
                        tongnap.setText("0");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAccount(JDialog d, int id, JTextField pass,
            JCheckBox act, JCheckBox ban, JCheckBox adm,
            JTextField vnd, JTextField tongnap) {
        if (JOptionPane.showConfirmDialog(d, "Lưu dữ liệu?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        new Thread(() -> {
            // Dựa trên schema user cung cấp:
            // cash: int
            // danap: int
            // is_admin: tinyint(1)
            // admin: int
            String sql = "UPDATE account SET password=?, active=?, ban=?, is_admin=?, admin=?, cash=?, danap=? WHERE id=?";

            try (Connection conn = DBConnecter.getConnectionServer();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                int i = 1;
                ps.setString(i++, pass.getText());
                ps.setInt(i++, act.isSelected() ? 1 : 0);
                ps.setInt(i++, ban.isSelected() ? 1 : 0);

                int adminVal = adm.isSelected() ? 1 : 0;
                ps.setInt(i++, adminVal); // is_admin
                ps.setInt(i++, adminVal); // admin

                ps.setInt(i++, safeInt(vnd)); // cash
                ps.setInt(i++, safeInt(tongnap)); // danap
                ps.setInt(i++, id);

                ps.executeUpdate();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(d, "Lưu thành công!");
                    d.dispose();
                    loadData();
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(d, "Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    // ========================================================================
    // 5. HELPER UI BUILDERS (Làm đẹp Dialog)
    // ========================================================================

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(COL_SECTION_BG);
        TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(COL_BORDER), title);
        b.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setTitleColor(COL_PRIMARY);
        p.setBorder(new CompoundBorder(b, new EmptyBorder(5, 5, 5, 5)));
        return p;
    }

    private JPanel createInputGroup(String label, JTextField txt) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(COL_TEXT_GRAY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(txt, BorderLayout.CENTER);
        return p;
    }

    private JPanel createBorderedGroup(String title, JTextField txt) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
                new LineBorder(COL_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(COL_TEXT_GRAY);

        txt.setBorder(null);
        txt.setBackground(Color.WHITE);

        p.add(lbl, BorderLayout.NORTH);
        p.add(txt, BorderLayout.CENTER);
        return p;
    }

    private JTextField createField(boolean edit) {
        JTextField t = new JTextField();
        t.setEditable(edit);
        t.setFont(FONT_DATA);
        if (edit) {
            t.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COL_BORDER), new EmptyBorder(5, 8, 5, 8)));
        } else {
            t.setBorder(null);
            t.setOpaque(false);
            t.setFont(new Font("Segoe UI", Font.BOLD, 15));
            t.setForeground(new Color(0, 102, 204));
        }
        return t;
    }

    private void styleCheck(JCheckBox c) {
        c.setFont(FONT_BOLD);
        c.setOpaque(false);
        c.setFocusPainted(false);
    }

    private int safeInt(JTextField t) {
        try {
            return Integer.parseInt(t.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private JButton createButton(String text, Color bg) {
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
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 15, 8, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private String formatNum(long num) {
        return java.text.NumberFormat.getInstance().format(num);
    }

    // ========================================================================
    // 6. DB LOADERS (TABLE)
    // ========================================================================
    private void loadHeadPartCache() {
        new Thread(() -> {
            try (Connection conn = DBConnecter.getConnectionServer();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, data FROM part WHERE type = 0")) {
                while (rs.next()) {
                    try {
                        JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("data")); // Parse json simple
                        if (arr != null && !arr.isEmpty()) {
                            JSONArray first = (JSONArray) arr.get(0);
                            if (first != null && !first.isEmpty()) {
                                partHeadIconMap.put(rs.getInt("id"), Integer.parseInt(first.get(0).toString()));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
            }
            SwingUtilities.invokeLater(this::loadData);
        }).start();
    }

    private ImageIcon getAvatar(int headId, String text, int size) {
        if (headId > 0) {
            if (headCache.containsKey(headId) && size == 28)
                return headCache.get(headId);
            Integer iconId = partHeadIconMap.get(headId);
            if (iconId != null) {
                try {
                    String[] zooms = { "x4", "x3", "x2", "x1" };
                    for (String z : zooms) {
                        File f = new File(ICON_FOLDER + z + "/" + iconId + ".png");
                        if (f.exists()) {
                            Image img = ImageIO.read(f).getScaledInstance(size, size, Image.SCALE_SMOOTH);
                            ImageIcon icon = new ImageIcon(img);
                            if (size == 28)
                                headCache.put(headId, icon);
                            return icon;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return AvatarGenerator.generate(text, size);
    }

    private void loadData() {
        // Cố gắng detect column name
        new Thread(() -> {
            String colVnd = "vnd";
            String colTongNap = "tongnap";

            try (Connection conn = DBConnecter.getConnectionServer()) {
                DatabaseMetaData md = conn.getMetaData();
                try (ResultSet rs = md.getColumns(null, null, "account", "cash")) {
                    if (rs.next())
                        colVnd = "cash";
                }
                try (ResultSet rs = md.getColumns(null, null, "account", "danap")) {
                    if (rs.next())
                        colTongNap = "danap";
                }

                String sql = "SELECT a.id, a.username, a.password, a.active, a.ban, a." + colVnd + ", a." + colTongNap
                        + ", a.admin, " +
                        "(SELECT head FROM player WHERE account_id = a.id LIMIT 1) AS head, " +
                        "(SELECT name FROM player WHERE account_id = a.id LIMIT 1) AS p_name FROM account a ORDER BY a.id ASC";

                try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                    model.setRowCount(0);
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        String u = rs.getString("username");
                        row.add(getAvatar(rs.getInt("head"), u, 28));
                        row.add(rs.getInt("id"));
                        row.add(u);
                        String pn = rs.getString("p_name");
                        row.add(pn == null ? "-(Chưa tạo)-" : pn);
                        row.add("******");

                        int active = rs.getInt("active");
                        int ban = rs.getInt("ban");
                        row.add(active == 1 ? "Active" : "In-Active");
                        row.add(ban == 1 ? "YES" : "NO");

                        row.add(formatNum(rs.getInt(colVnd)));
                        row.add(formatNum(rs.getInt(colTongNap)));

                        int isAdmin = rs.getInt("admin");
                        row.add(isAdmin == 1 ? "YES" : "NO");

                        SwingUtilities.invokeLater(() -> model.addRow(row));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void searchData(String txt) {
        if (txt.isEmpty()) {
            loadData();
            return;
        }

        new Thread(() -> {
            String colVnd = "vnd";
            String colTongNap = "tongnap";
            try (Connection conn = DBConnecter.getConnectionServer()) {
                DatabaseMetaData md = conn.getMetaData();
                try (ResultSet rs = md.getColumns(null, null, "account", "cash")) {
                    if (rs.next())
                        colVnd = "cash";
                }
                try (ResultSet rs = md.getColumns(null, null, "account", "danap")) {
                    if (rs.next())
                        colTongNap = "danap";
                }

                String sql = "SELECT a.id, a.username, a.password, a.active, a.ban, a." + colVnd + ", a." + colTongNap
                        + ", a.admin, " +
                        "(SELECT head FROM player WHERE account_id = a.id LIMIT 1) AS head, " +
                        "(SELECT name FROM player WHERE account_id = a.id LIMIT 1) AS p_name FROM account a " +
                        "WHERE a.username LIKE '%" + txt + "%' OR a.id='" + txt
                        + "' OR (SELECT name FROM player WHERE account_id=a.id LIMIT 1) LIKE '%" + txt + "%'";

                try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                    model.setRowCount(0);
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        String u = rs.getString("username");
                        row.add(getAvatar(rs.getInt("head"), u, 28));
                        row.add(rs.getInt("id"));
                        row.add(u);
                        String pn = rs.getString("p_name");
                        row.add(pn == null ? "-(Chưa tạo)-" : pn);
                        row.add("******");

                        int active = rs.getInt("active");
                        int ban = rs.getInt("ban");
                        row.add(active == 1 ? "Active" : "In-Active");
                        row.add(ban == 1 ? "YES" : "NO");

                        row.add(formatNum(rs.getInt(colVnd)));
                        row.add(formatNum(rs.getInt(colTongNap)));

                        int isAdmin = rs.getInt("admin");
                        row.add(isAdmin == 1 ? "YES" : "NO");

                        SwingUtilities.invokeLater(() -> model.addRow(row));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    static class AvatarGenerator {
        private static final Color[] COLORS = { new Color(26, 188, 156), new Color(46, 204, 113),
                new Color(52, 152, 219), new Color(155, 89, 182), new Color(230, 126, 34), new Color(231, 76, 60) };

        public static ImageIcon generate(String text, int size) {
            if (text == null || text.isEmpty())
                text = "?";
            String l = text.substring(0, 1).toUpperCase();
            Color bg = COLORS[Math.abs(text.hashCode()) % COLORS.length];
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Float(0, 0, size, size));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(l, (size - fm.stringWidth(l)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
            return new ImageIcon(img);
        }
    }
}
