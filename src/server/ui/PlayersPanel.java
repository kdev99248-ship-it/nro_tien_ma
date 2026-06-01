package server.ui;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import jdbc.DBConnecter;

public class PlayersPanel extends JPanel {

    private static final String ICON_FOLDER = "data/icon/";

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    // Cache
    private final Map<Integer, Integer> partHeadIconMap = new HashMap<>();
    private final Map<Integer, ImageIcon> headCache = new HashMap<>();
    private final Map<Integer, String> taskTemplateMap = new HashMap<>();

    private final Color COLOR_PRIMARY = new Color(0, 120, 215);
    private final Color COLOR_ALT_ROW = new Color(245, 245, 245);
    private final Color COLOR_GRID = new Color(220, 220, 220);
    private static final Font FONT_NUM = new Font("Consolas", Font.BOLD, 14);

    public PlayersPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        loadCacheData(); // Load task templates & head icons
        initTopControls();
        initTable();
    }

    private Connection getConnection() throws SQLException {
        return DBConnecter.getConnectionServer();
    }

    private void loadCacheData() {
        new Thread(() -> {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                // 1. Load Head Part Icons
                try (ResultSet rs = stmt.executeQuery("SELECT id, data FROM part WHERE type = 0")) {
                    while (rs.next()) {
                        try {
                            JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("data"));
                            if (arr != null && !arr.isEmpty()) {
                                JSONArray first = (JSONArray) arr.get(0);
                                if (first != null && !first.isEmpty()) {
                                    partHeadIconMap.put(rs.getInt("id"), Integer.parseInt(first.get(0).toString()));
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                // 2. Load Task Names
                try (ResultSet rs = stmt.executeQuery("SELECT id, name FROM task_main_template")) {
                    while (rs.next()) {
                        taskTemplateMap.put(rs.getInt("id"), rs.getString("name"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Load initial data
            SwingUtilities.invokeLater(() -> {
                setupEditors(); // Refresh (re-populate) editors with loaded data
                loadPlayersFromDB("");
            });
        }).start();
    }

    // --- ICON HELPER ---
    private ImageIcon drawHeadIcon(int headPartId) {
        if (headPartId <= 0)
            return null;
        if (headCache.containsKey(headPartId))
            return headCache.get(headPartId);
        Integer iconId = partHeadIconMap.get(headPartId);
        if (iconId != null) {
            try {
                String[] zoomLevels = { "x4", "x3", "x2", "x1" };
                for (String zoom : zoomLevels) {
                    File f = new File(ICON_FOLDER + zoom + "/" + iconId + ".png");
                    if (f.exists()) {
                        Image dimg = ImageIO.read(f).getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                        ImageIcon icon = new ImageIcon(dimg);
                        headCache.put(headPartId, icon);
                        return icon;
                    }
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    // --- UI SETUP ---
    private void initTopControls() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 10, 0));
        JPanel searchP = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchP.setOpaque(false);

        txtSearch = new JTextField(25);
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập tên nhân vật hoặc ID để tìm...");
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    loadPlayersFromDB(txtSearch.getText().trim());
            }
        });

        JButton btnSearch = createStyledButton("Tìm kiếm", COLOR_PRIMARY, Color.WHITE);
        btnSearch.addActionListener(e -> loadPlayersFromDB(txtSearch.getText().trim()));

        JButton btnReload = createStyledButton("Tải lại", new Color(40, 167, 69), Color.WHITE);
        btnReload.addActionListener(e -> {
            txtSearch.setText("");
            loadPlayersFromDB("");
        });

        searchP.add(txtSearch);
        searchP.add(btnSearch);
        searchP.add(btnReload);
        top.add(searchP, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);
    }

    private void initTable() {
        // Columns: "Head", "ID", "Tên NV", "Vàng", "Ngọc", "Hồng Ngọc", "Sức Mạnh",
        // "Tiềm Năng", "Nhiệm Vụ", "Ban", "State"
        String[] columns = { "Head", "ID", "Tên NV", "Vàng", "Ngọc", "Hồng Ngọc", "Sức Mạnh", "Tiềm Năng", "Nhiệm Vụ",
                "Ban", "State" };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Editable from Gold (3) to Ban (9)
                return col >= 3 && col <= 9;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? ImageIcon.class : Object.class;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (row >= getRowCount())
                    return;
                Object oldValue = getValueAt(row, column);
                if (String.valueOf(aValue).equals(String.valueOf(oldValue)))
                    return;

                super.setValueAt(aValue, row, column);
                updateCellData(row, column, aValue);
            }
        };

        table = new JTable(model);
        setupTableStyle();
        setupEditors();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void setupEditors() {
        // Yes/No Editor for Ban Column
        JComboBox<String> cbbBan = new JComboBox<>(new String[] { "YES", "NO" });
        table.getColumnModel().getColumn(9).setCellEditor(new DefaultCellEditor(cbbBan));

        // Task Editor (ComboBox)
        JComboBox<String> cbbTask = new JComboBox<>();
        // Populate tasks from map (Sorted by ID)
        taskTemplateMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> cbbTask.addItem(entry.getKey() + " - " + entry.getValue()));
        table.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(cbbTask));
    }

    private void setupTableStyle() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(COLOR_GRID);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(COLOR_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                l.setBackground(COLOR_PRIMARY);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setOpaque(true);
                l.setBorder(new MatteBorder(0, 0, 0, 1, new Color(255, 255, 255, 50)));
                return l;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected)
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_ALT_ROW);
                else
                    c.setBackground(new Color(220, 235, 255));

                setHorizontalAlignment(JLabel.CENTER);
                setForeground(Color.BLACK);

                if (column == 1) { // ID
                    setFont(FONT_NUM);
                    setForeground(new Color(0, 128, 128));
                } else if (column == 2) { // Tên NV
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setForeground(new Color(0, 102, 204));
                } else if (column >= 3 && column <= 7) { // Stats
                    setFont(FONT_NUM);
                    setForeground(new Color(153, 0, 153));
                } else if (column == 9) { // Ban
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if ("YES".equals(value))
                        setForeground(Color.RED);
                    else
                        setForeground(new Color(0, 128, 0));
                } else if (column == 10) { // State
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if ("Online".equals(value))
                        setForeground(new Color(0, 180, 0)); // Green
                    else
                        setForeground(Color.GRAY);
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }

                if (c instanceof JComponent)
                    ((JComponent) c).setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

                return c;
            }
        });

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(40); // Head
        cm.getColumn(1).setPreferredWidth(50); // ID
        cm.getColumn(2).setPreferredWidth(120); // Name
        cm.getColumn(3).setPreferredWidth(80); // Vang
        cm.getColumn(4).setPreferredWidth(60); // Ngoc
        cm.getColumn(5).setPreferredWidth(60); // H.Ngoc
        cm.getColumn(6).setPreferredWidth(90); // SM
        cm.getColumn(7).setPreferredWidth(90); // TN
        cm.getColumn(8).setPreferredWidth(150); // Task
        cm.getColumn(9).setPreferredWidth(50); // Ban
        cm.getColumn(10).setPreferredWidth(60); // State
    }

    private void updateCellData(int row, int col, Object value) {
        new Thread(() -> {
            try {
                int pid = Integer.parseInt(model.getValueAt(row, 1).toString());
                Object newVal = value;

                try (Connection conn = getConnection()) {
                    if (col == 9) { // Ban Column
                        int ban = "YES".equals(newVal) ? 1 : 0;
                        // Use nested query to update account linked to player
                        String updateBan = "UPDATE account SET ban=? WHERE id=(SELECT account_id FROM player WHERE id=? LIMIT 1)";
                        try (PreparedStatement ps = conn.prepareStatement(updateBan)) {
                            ps.setInt(1, ban);
                            ps.setInt(2, pid);
                            ps.executeUpdate();
                        }
                    } else {
                        // Data Columns
                        String dbCol = "";
                        int jsonIdx = -1;
                        boolean isTask = false;

                        /*
                         * 3: Vàng (inv[0])
                         * 4: Ngọc (inv[1])
                         * 5: Hồng Ngọc (inv[2])
                         * 6: Sức Mạnh (point[1])
                         * 7: Tiềm Năng (point[2])
                         * 8: Nhiệm Vụ (task[0])
                         */
                        switch (col) {
                            case 3:
                                dbCol = "data_inventory";
                                jsonIdx = 0;
                                break;
                            case 4:
                                dbCol = "data_inventory";
                                jsonIdx = 1;
                                break;
                            case 5:
                                dbCol = "data_inventory";
                                jsonIdx = 2;
                                break;
                            case 6:
                                dbCol = "data_point";
                                jsonIdx = 1;
                                break;
                            case 7:
                                dbCol = "data_point";
                                jsonIdx = 2;
                                break;
                            case 8:
                                dbCol = "data_task";
                                jsonIdx = 0;
                                isTask = true;
                                break;
                        }

                        if (!dbCol.isEmpty()) {
                            // Fetch current json
                            String sel = "SELECT " + dbCol + " FROM player WHERE id=? LIMIT 1";
                            String currentJsonStr = "";
                            try (PreparedStatement ps = conn.prepareStatement(sel)) {
                                ps.setInt(1, pid);
                                ResultSet rs = ps.executeQuery();
                                if (rs.next()) {
                                    currentJsonStr = rs.getString(1);
                                }
                            }

                            if (currentJsonStr != null && !currentJsonStr.isEmpty()) {
                                JSONArray arr = (JSONArray) JSONValue.parse(currentJsonStr);
                                if (arr != null && arr.size() > jsonIdx) {
                                    long valLong = 0;
                                    try {
                                        String sVal = newVal.toString().replace(",", "").replace(".", "").trim();
                                        if (isTask && sVal.contains(" ")) {
                                            sVal = sVal.split(" ")[0]; // Handle "ID - Name" format
                                        }
                                        valLong = Long.parseLong(sVal);
                                    } catch (Exception e) {
                                    }

                                    arr.set(jsonIdx, valLong);

                                    // If Task, reset index/count (optional safety)
                                    if (isTask && arr.size() > 2) {
                                        arr.set(1, 0); // index
                                        arr.set(2, 0); // count
                                    }

                                    // Update DB
                                    String up = "UPDATE player SET " + dbCol + "=? WHERE id=? LIMIT 1";
                                    try (PreparedStatement ps = conn.prepareStatement(up)) {
                                        ps.setString(1, arr.toJSONString());
                                        ps.setInt(2, pid);
                                        ps.executeUpdate();
                                    }

                                    // If Task, refresh display name if possible
                                    if (isTask) {
                                        int tid = (int) valLong;
                                        String tName = taskTemplateMap.getOrDefault(tid, "Unknown");
                                        String finalVal = tid + " - " + tName;
                                        SwingUtilities.invokeLater(() -> model.setValueAt(finalVal, row, col));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadPlayersFromDB(String keyword) {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> model.setRowCount(0));

            String sql = "SELECT p.id, p.head, p.name, p.data_inventory, p.data_point, p.data_task, a.ban " +
                    "FROM player p LEFT JOIN account a ON p.account_id = a.id ";

            if (!keyword.isEmpty()) {
                if (keyword.matches("\\d+"))
                    sql += "WHERE p.id = " + keyword + " OR p.account_id = " + keyword + " ";
                else
                    sql += "WHERE p.name LIKE '%" + keyword + "%' ";
            }
            sql += "ORDER BY p.id ASC LIMIT 100";

            try (Connection conn = getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(drawHeadIcon(rs.getInt("head")));
                    int pid = rs.getInt("id");
                    row.add(pid);
                    String name = rs.getString("name");
                    row.add(name != null ? name : "-(No Name)-");

                    long vang = 0, sm = 0, tn = 0;
                    int ngoc = 0, hngoc = 0, taskId = 0;

                    try {
                        JSONArray inv = (JSONArray) JSONValue.parse(rs.getString("data_inventory"));
                        if (inv != null) {
                            vang = Long.parseLong(inv.get(0).toString());
                            ngoc = Integer.parseInt(inv.get(1).toString());
                            hngoc = Integer.parseInt(inv.get(2).toString());
                        }
                    } catch (Exception e) {
                    }

                    try {
                        JSONArray point = (JSONArray) JSONValue.parse(rs.getString("data_point"));
                        if (point != null) {
                            sm = Long.parseLong(point.get(1).toString());
                            tn = Long.parseLong(point.get(2).toString());
                        }
                    } catch (Exception e) {
                    }

                    try {
                        JSONArray task = (JSONArray) JSONValue.parse(rs.getString("data_task"));
                        if (task != null) {
                            taskId = Integer.parseInt(task.get(0).toString());
                        }
                    } catch (Exception e) {
                    }

                    row.add(formatNum(vang));
                    row.add(formatNum(ngoc));
                    row.add(formatNum(hngoc));
                    row.add(formatNum(sm));
                    row.add(formatNum(tn));
                    row.add(taskId + " - " + taskTemplateMap.getOrDefault(taskId, "Unknown"));

                    int ban = rs.getInt("ban");
                    row.add(ban == 1 ? "YES" : "NO");

                    // Check Online State
                    boolean isOnline = isPlayerOnline(pid);
                    row.add(isOnline ? "Online" : "Offline");

                    SwingUtilities.invokeLater(() -> model.addRow(row));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean isPlayerOnline(int pid) {
        try {
            Class<?> clientClass = Class.forName("server.Client");
            Object instance = clientClass.getMethod("gI").invoke(null);
            java.lang.reflect.Method getPlayerMethod = clientClass.getMethod("getPlayerByID", int.class);
            Object player = getPlayerMethod.invoke(instance, pid);
            return player != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatNum(long num) {
        return java.text.NumberFormat.getInstance().format(num);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
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
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 15, 8, 15));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
