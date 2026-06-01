package server.ui;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import jdbc.DBConnecter;

public class GiftcodePanel extends JPanel {

    // --- Cấu hình Icon ---
    private static final String ICON_FOLDER = "data/icon/";

    private JTable table;
    private DefaultTableModel model;

    // Font chuẩn đẹp
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 13);

    // Cache dữ liệu
    private final Map<Integer, String> itemTemplateMap = new HashMap<>();
    private final Map<Integer, Integer> itemIconMap = new HashMap<>();
    private final Map<Integer, String> optionTemplateMap = new HashMap<>();

    private final List<ItemData> listAllItems = new ArrayList<>();

    private static class ItemData {
        int id;
        String name;
        int type;
        int gender;

        public ItemData(int id, String name, int type, int gender) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.gender = gender;
        }
    }

    // Cache Icon (RAM)
    private final Map<Integer, ImageIcon> iconCache = new HashMap<>();
    private final Map<Integer, Boolean> noIconCache = new HashMap<>();

    public GiftcodePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Nạp dữ liệu nền
        new Thread(this::loadCacheData).start();

        // 2. Giao diện chính
        initUI();

        // 3. Tải danh sách Giftcode
        loadDataFromDB();
    }

    // --- UTILS UI HELPER ---
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

    private Border createSectionBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 200)), title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 14));
        border.setTitleColor(new Color(0, 102, 204));
        return new CompoundBorder(border, new EmptyBorder(5, 5, 5, 5));
    }

    private Connection getConnection() throws SQLException {
        return DBConnecter.getConnectionServer();
    }

    // --- LOAD CACHE ---
    private void loadCacheData() {
        listAllItems.clear();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT id, name, icon_id, type, gender FROM item_template")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int iconId = rs.getInt("icon_id");
                    int type = rs.getInt("type");
                    int gender = rs.getInt("gender");

                    itemTemplateMap.put(id, name);
                    itemIconMap.put(id, iconId);
                    listAllItems.add(new ItemData(id, name, type, gender));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT id, name FROM item_option_template")) {
                while (rs.next())
                    optionTemplateMap.put(rs.getInt("id"), rs.getString("name"));
            } catch (Exception e) {
                initHardcodedOptionData();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initHardcodedOptionData() {
        String rawData = "0\tTấn công+#\n50\tSức đánh+#%\n77\tHP+#%\n103\tKI +#%\n14\tChí mạng+#%\n30\tKhóa giao dịch";
        for (String line : rawData.split("\n")) {
            String[] p = line.split("\t");
            if (p.length >= 2)
                optionTemplateMap.put(Integer.parseInt(p[0]), p[1]);
        }
    }

    private String getItemName(int id) {
        return itemTemplateMap.getOrDefault(id, "Unknown Item (" + id + ")");
    }

    private String getOptionName(int id) {
        return optionTemplateMap.getOrDefault(id, "Option " + id);
    }

    // --- LOAD ICON ---
    private ImageIcon getItemIcon(int itemId) {
        if (iconCache.containsKey(itemId))
            return iconCache.get(itemId);
        if (noIconCache.containsKey(itemId))
            return null;

        try {
            int iconId = itemIconMap.getOrDefault(itemId, -1);
            if (iconId == -1) {
                noIconCache.put(itemId, true);
                return null;
            }
            String[] zoomLevels = { "x4", "x3", "x2", "x1" };
            File f = null;
            for (String zoom : zoomLevels) {
                f = new File(ICON_FOLDER + zoom + "/" + iconId + ".png");
                if (f.exists())
                    break;
            }
            if (f != null && f.exists()) {
                BufferedImage img = ImageIO.read(f);
                Image dimg = img.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(dimg);
                iconCache.put(itemId, icon);
                return icon;
            }
        } catch (Exception e) {
        }
        noIconCache.put(itemId, true);
        return null;
    }

    // --- GIAO DIỆN CHÍNH ---
    private void initUI() {
        // Toolbar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setOpaque(false);
        top.setBorder(createSectionBorder("Bảng Điều Khiển"));

        JButton btnAdd = createStyledButton("Tạo Code Mới", new Color(40, 167, 69), Color.WHITE);
        JButton btnReload = createStyledButton("Tải lại", new Color(0, 123, 255), Color.WHITE);
        JButton btnDelete = createStyledButton("Xóa Code", new Color(220, 53, 69), Color.WHITE);

        btnAdd.addActionListener(e -> openGiftcodeEditor(null));
        btnReload.addActionListener(e -> loadDataFromDB());
        btnDelete.addActionListener(e -> deleteSelectedGiftcode());

        top.add(btnAdd);
        top.add(btnReload);
        top.add(btnDelete);
        add(top, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Mã Code", "Lượt còn", "Ngày tạo", "Hết hạn", "Loại", "Chi tiết (JSON)" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(30);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));

        // Format cột Code
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(FONT_BOLD);
                setForeground(new Color(0, 102, 0));
                return this;
            }
        });

        // Format cột Ngày tháng (Cột 3 và 4)
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            SimpleDateFormat dbFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat viewFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                try {
                    String s = (String) value;
                    Date d = dbFmt.parse(s);
                    c.setText(viewFmt.format(d));

                    if (column == 4) {
                        if (d.before(new Date())) {
                            c.setForeground(Color.RED);
                            c.setText(c.getText() + " (Đã hết)");
                        } else {
                            c.setForeground(new Color(0, 0, 200));
                        }
                    }
                } catch (Exception e) {
                    c.setText(value != null ? value.toString() : "");
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(3).setCellRenderer(dateRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(dateRenderer);

        table.getColumnModel().getColumn(6).setPreferredWidth(300);

        // Double click sửa
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1)
                        openGiftcodeEditor(row);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadDataFromDB() {
        model.setRowCount(0);
        new Thread(() -> {
            try (Connection conn = getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM giftcode ORDER BY id ASC")) {

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("code"));
                    row.add(rs.getInt("count_left"));
                    row.add(rs.getString("datecreate"));
                    row.add(rs.getString("expired"));
                    row.add(rs.getInt("type"));
                    row.add(rs.getString("detail"));
                    SwingUtilities.invokeLater(() -> model.addRow(row));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteSelectedGiftcode() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String code = (String) model.getValueAt(row, 1);

        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa Code: " + code + "?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                try (Connection conn = getConnection();
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM giftcode WHERE id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    SwingUtilities.invokeLater(this::loadDataFromDB);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // ========================================================================
    // 1. EDITOR GIFTCODE
    // ========================================================================
    private void openGiftcodeEditor(Integer rowIndex) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Giftcode Editor", true);
        dialog.setSize(1100, 750);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);
        ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(createSectionBorder("Thông tin cấu hình"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtCode = new JTextField(rowIndex != null ? model.getValueAt(rowIndex, 1).toString()
                : "GIFT" + System.currentTimeMillis() / 1000, 15);
        txtCode.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtCode.setForeground(new Color(180, 0, 0));
        JTextField txtCount = new JTextField(rowIndex != null ? model.getValueAt(rowIndex, 2).toString() : "100", 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Mã Code:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(txtCode, gbc);
        gbc.gridx = 2;
        infoPanel.add(new JLabel("Số lượng nhập:"), gbc);
        gbc.gridx = 3;
        infoPanel.add(txtCount, gbc);

        String expStr = rowIndex != null ? model.getValueAt(rowIndex, 4).toString() : "2030-01-01 00:00:00";
        String[] dateTime = expStr.split(" ");
        String[] dateParts = dateTime[0].split("-");
        String[] timeParts = dateTime.length > 1 ? dateTime[1].split(":") : new String[] { "00", "00", "00" };

        JComboBox<Integer> cbYear = new JComboBox<>();
        for (int i = 2023; i <= 2035; i++)
            cbYear.addItem(i);
        cbYear.setSelectedItem(Integer.parseInt(dateParts[0]));
        JComboBox<Integer> cbMonth = new JComboBox<>();
        for (int i = 1; i <= 12; i++)
            cbMonth.addItem(i);
        cbMonth.setSelectedItem(Integer.parseInt(dateParts[1]));
        JComboBox<Integer> cbDay = new JComboBox<>();
        for (int i = 1; i <= 31; i++)
            cbDay.addItem(i);
        cbDay.setSelectedItem(Integer.parseInt(dateParts[2]));
        JComboBox<Integer> cbHour = new JComboBox<>();
        for (int i = 0; i < 24; i++)
            cbHour.addItem(i);
        cbHour.setSelectedItem(Integer.parseInt(timeParts[0]));
        JComboBox<Integer> cbMin = new JComboBox<>();
        for (int i = 0; i < 60; i++)
            cbMin.addItem(i);
        cbMin.setSelectedItem(Integer.parseInt(timeParts[1]));

        JPanel pDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pDate.add(new JLabel("Ngày"));
        pDate.add(cbDay);
        pDate.add(new JLabel("/"));
        pDate.add(cbMonth);
        pDate.add(new JLabel("/"));
        pDate.add(cbYear);
        pDate.add(new JLabel(" | Giờ:"));
        pDate.add(cbHour);
        pDate.add(new JLabel(":"));
        pDate.add(cbMin);

        gbc.gridx = 0;
        gbc.gridy = 1;
        infoPanel.add(new JLabel("Thời hạn:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        infoPanel.add(pDate, gbc);
        gbc.gridwidth = 1;

        String[] typeDescs = { "0 - Tất cả mọi người", "1 - Chỉ thành viên kích hoạt",
                "2 - Chỉ tài khoản mới tạo (<7 ngày)" };
        JComboBox<String> cbType = new JComboBox<>(typeDescs);
        if (rowIndex != null) {
            int t = Integer.parseInt(model.getValueAt(rowIndex, 5).toString());
            if (t < 3)
                cbType.setSelectedIndex(t);
        }

        gbc.gridx = 0;
        gbc.gridy = 2;
        infoPanel.add(new JLabel("Đối tượng:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        infoPanel.add(cbType, gbc);
        gbc.gridwidth = 1;

        JPanel itemPanel = new JPanel(new BorderLayout(5, 5));
        itemPanel.setBorder(createSectionBorder("Danh sách Phần Thưởng"));

        String[] iCols = { "ID", "Icon", "Tên Item", "Số lượng", "Options (JSON)" };
        DefaultTableModel iModel = new DefaultTableModel(iCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 1 ? ImageIcon.class : Object.class;
            }
        };
        JTable iTable = new JTable(iModel);
        iTable.setRowHeight(35);
        iTable.setFont(FONT_PLAIN);
        iTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        iTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        iTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        iTable.getColumnModel().getColumn(4).setPreferredWidth(300);

        if (rowIndex != null) {
            loadItemsToTable((String) model.getValueAt(rowIndex, 6), iModel);
        }

        JPanel iTool = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddI = createStyledButton("Thêm Item", new Color(23, 162, 184), Color.WHITE);
        JButton btnEditI = createStyledButton("Sửa", new Color(255, 193, 7), Color.BLACK);
        JButton btnDelI = createStyledButton("Xóa", new Color(220, 53, 69), Color.WHITE);
        iTool.add(btnAddI);
        iTool.add(btnEditI);
        iTool.add(btnDelI);

        btnAddI.addActionListener(e -> openItemBuilder(iModel, -1));
        btnEditI.addActionListener(e -> {
            if (iTable.getSelectedRow() != -1)
                openItemBuilder(iModel, iTable.getSelectedRow());
        });
        iTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && iTable.getSelectedRow() != -1)
                    openItemBuilder(iModel, iTable.getSelectedRow());
            }
        });
        btnDelI.addActionListener(e -> {
            if (iTable.getSelectedRow() != -1)
                iModel.removeRow(iTable.getSelectedRow());
        });

        itemPanel.add(iTool, BorderLayout.NORTH);
        itemPanel.add(new JScrollPane(iTable), BorderLayout.CENTER);

        JButton btnSave = createStyledButton("LƯU DỮ LIỆU", new Color(0, 100, 0), Color.WHITE);
        btnSave.setPreferredSize(new Dimension(200, 45));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSave.addActionListener(e -> {
            String expDate = String.format("%04d-%02d-%02d %02d:%02d:00",
                    cbYear.getSelectedItem(), cbMonth.getSelectedItem(), cbDay.getSelectedItem(),
                    cbHour.getSelectedItem(), cbMin.getSelectedItem());
            int type = cbType.getSelectedIndex();

            saveGiftcodeToDB(rowIndex, txtCode.getText(), txtCount.getText(), expDate, type, iModel, dialog);
        });

        JPanel bot = new JPanel();
        bot.add(btnSave);

        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(itemPanel, BorderLayout.CENTER);
        dialog.add(bot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadItemsToTable(String json, DefaultTableModel mod) {
        try {
            JSONArray arr = (JSONArray) JSONValue.parse(json);
            if (arr == null)
                return;
            for (Object o : arr) {
                JSONObject obj = (JSONObject) o;
                int id = Integer.parseInt(obj.get("temp_id").toString());
                int qty = Integer.parseInt(obj.get("quantity").toString());
                String opt = obj.get("options").toString();

                mod.addRow(new Object[] { id, getItemIcon(id), getItemName(id), qty, opt });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========================================================================
    // 2. ITEM BUILDER
    // ========================================================================
    private void openItemBuilder(DefaultTableModel parentModel, int editRow) {
        JDialog dialog = new JDialog((Frame) null, "Cấu hình Vật Phẩm", true);
        dialog.setSize(750, 650);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(null);
        ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pTop = new JPanel(new GridBagLayout());
        pTop.setBorder(createSectionBorder("Chọn Item"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = new JTextField();
        JTextField txtQty = new JTextField("1");
        JLabel lblName = new JLabel("Chưa chọn item");
        lblName.setFont(FONT_BOLD);
        lblName.setForeground(Color.BLUE);
        JLabel lblIcon = new JLabel();
        lblIcon.setPreferredSize(new Dimension(30, 30));
        JButton btnSearchItem = createStyledButton("🔍 Tìm nhanh", new Color(0, 123, 255), Color.WHITE);

        String oldJsonOpt = "[]";
        if (editRow != -1) {
            txtId.setText(parentModel.getValueAt(editRow, 0).toString());
            lblName.setText(parentModel.getValueAt(editRow, 2).toString());
            txtQty.setText(parentModel.getValueAt(editRow, 3).toString());
            oldJsonOpt = parentModel.getValueAt(editRow, 4).toString();
        }

        txtId.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                try {
                    int id = Integer.parseInt(txtId.getText());
                    lblName.setText(getItemName(id));
                    lblIcon.setIcon(getItemIcon(id));
                } catch (Exception ex) {
                    lblName.setText("...");
                    lblIcon.setIcon(null);
                }
            }
        });

        if (!txtId.getText().isEmpty()) {
            try {
                int id = Integer.parseInt(txtId.getText());
                lblIcon.setIcon(getItemIcon(id));
            } catch (Exception ignored) {
            }
        }

        btnSearchItem.addActionListener(e -> showItemSearchDialog(dialog, (id, name) -> {
            txtId.setText(String.valueOf(id));
            lblName.setText(name);
        }));

        g.gridx = 0;
        g.gridy = 0;
        pTop.add(new JLabel("ID Item:"), g);
        g.gridx = 1;
        pTop.add(txtId, g);
        g.gridx = 2;
        pTop.add(btnSearchItem, g);

        g.gridx = 0;
        g.gridy = 1;
        pTop.add(new JLabel("Thông tin:"), g);
        JPanel pInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pInfo.add(lblIcon);
        pInfo.add(lblName);
        g.gridx = 1;
        g.gridwidth = 2;
        pTop.add(pInfo, g);
        g.gridwidth = 1;

        g.gridx = 0;
        g.gridy = 2;
        pTop.add(new JLabel("Số lượng:"), g);
        g.gridx = 1;
        pTop.add(txtQty, g);

        String[] oCols = { "Option ID", "Chỉ số (Param)", "Mô tả tự động" };
        DefaultTableModel oModel = new DefaultTableModel(oCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 2;
            }
        };
        JTable oTable = new JTable(oModel);
        oTable.setRowHeight(25);
        oTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        oTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        oTable.getColumnModel().getColumn(2).setPreferredWidth(300);

        oModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (row >= 0 && row < oModel.getRowCount() && (col == 0 || col == 1)) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        int id = Integer.parseInt(oModel.getValueAt(row, 0).toString());
                        int param = Integer.parseInt(oModel.getValueAt(row, 1).toString());
                        String template = getOptionName(id);
                        oModel.setValueAt(template.replace("#", String.valueOf(param)), row, 2);
                    } catch (Exception ex) {
                    }
                });
            }
        });

        loadOptionsToTable(oldJsonOpt, oModel);

        JPanel pOpt = new JPanel(new BorderLayout());
        pOpt.setBorder(createSectionBorder("Danh sách Chỉ Số (Option)"));

        JPanel pOptTool = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddO = createStyledButton("Thêm dòng", new Color(40, 167, 69), Color.WHITE);
        JButton btnFindO = createStyledButton("🔍 Tra cứu Option", new Color(23, 162, 184), Color.WHITE);
        JButton btnDelO = createStyledButton("Xóa dòng", new Color(220, 53, 69), Color.WHITE);

        btnAddO.addActionListener(e -> oModel.addRow(new Object[] { "0", "0", getOptionName(0).replace("#", "0") }));

        btnFindO.addActionListener(e -> showOptionSearchDialog(dialog, (id, name) -> {
            oModel.addRow(new Object[] { id, "0", name.replace("#", "0") });
        }));

        btnDelO.addActionListener(e -> {
            if (oTable.getSelectedRow() != -1)
                oModel.removeRow(oTable.getSelectedRow());
        });

        pOptTool.add(btnAddO);
        pOptTool.add(btnFindO);
        pOptTool.add(btnDelO);
        pOpt.add(pOptTool, BorderLayout.NORTH);
        pOpt.add(new JScrollPane(oTable), BorderLayout.CENTER);

        JButton btnOk = createStyledButton("XÁC NHẬN & LƯU ITEM", new Color(40, 167, 69), Color.WHITE);
        btnOk.addActionListener(e -> {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < oModel.getRowCount(); i++) {
                try {
                    JSONObject o = new JSONObject();
                    o.put("id", Integer.parseInt(oModel.getValueAt(i, 0).toString()));
                    o.put("param", Integer.parseInt(oModel.getValueAt(i, 1).toString()));
                    arr.add(o);
                } catch (Exception ex) {
                }
            }

            Object[] row = {
                    txtId.getText(),
                    getItemIcon(Integer.parseInt(txtId.getText())),
                    lblName.getText(),
                    txtQty.getText(),
                    arr.toJSONString()
            };
            if (editRow == -1)
                parentModel.addRow(row);
            else
                for (int i = 0; i < 5; i++)
                    parentModel.setValueAt(row[i], editRow, i);

            dialog.dispose();
        });

        dialog.add(pTop, BorderLayout.NORTH);
        dialog.add(pOpt, BorderLayout.CENTER);
        dialog.add(btnOk, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadOptionsToTable(String json, DefaultTableModel mod) {
        try {
            JSONArray arr = (JSONArray) JSONValue.parse(json);
            if (arr == null)
                return;
            for (Object o : arr) {
                JSONObject obj = (JSONObject) o;
                int id = Integer.parseInt(obj.get("id").toString());
                int param = Integer.parseInt(obj.get("param").toString());
                mod.addRow(new Object[] { id, param, getOptionName(id).replace("#", String.valueOf(param)) });
            }
        } catch (Exception e) {
        }
    }

    // ========================================================================
    // 3. DIALOG TÌM KIẾM
    // ========================================================================
    interface ItemCallback {
        void onSelect(int id, String name);
    }

    private void showItemSearchDialog(JDialog parent, ItemCallback callback) {
        JDialog d = new JDialog(parent, "Tìm kiếm Vật Phẩm", true);
        d.setSize(950, 600);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(parent);

        JPanel pFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pFilter.setBackground(new Color(240, 240, 240));
        pFilter.setBorder(new EmptyBorder(5, 5, 5, 5));

        JTextField txtSearch = new JTextField(25);
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập Tên hoặc ID item...");

        String[] types = { "- Tất cả Loại -", "0 - Áo", "1 - Quần", "2 - Găng", "3 - Giày", "4 - Rada",
                "5 - Cải trang/Tóc", "6 - Đậu thần", "12 - Ngọc rồng", "27 - Vật phẩm", "29 - Capsule/Bánh",
                "32 - Giáp tập" };
        JComboBox<String> cbType = new JComboBox<>(types);

        String[] genders = { "- Tất cả Hệ -", "0 - Trái Đất", "1 - Namếc", "2 - Xayda", "3 - Chung/Tất cả" };
        JComboBox<String> cbGender = new JComboBox<>(genders);

        pFilter.add(new JLabel("Tìm kiếm (Tên/ID):"));
        pFilter.add(txtSearch);
        pFilter.add(new JLabel(" | Loại:"));
        pFilter.add(cbType);
        pFilter.add(new JLabel(" | Hệ:"));
        pFilter.add(cbGender);

        DefaultTableModel m = new DefaultTableModel(new String[] { "ID", "Icon", "Tên Item", "Type", "Gender" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 1 ? ImageIcon.class : Object.class;
            }
        };

        for (ItemData item : listAllItems) {
            m.addRow(new Object[] { item.id, getItemIcon(item.id), item.name, item.type, item.gender });
        }

        JTable t = new JTable(m);
        t.setRowHeight(35);
        t.setFont(FONT_PLAIN);
        t.getColumnModel().getColumn(0).setPreferredWidth(60);
        t.getColumnModel().getColumn(1).setPreferredWidth(50);
        t.getColumnModel().getColumn(2).setPreferredWidth(350);

        t.getColumnModel().getColumn(3).setMinWidth(0);
        t.getColumnModel().getColumn(3).setMaxWidth(0);
        t.getColumnModel().getColumn(4).setMinWidth(0);
        t.getColumnModel().getColumn(4).setMaxWidth(0);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        t.setRowSorter(sorter);

        Runnable doFilter = () -> {
            String text = txtSearch.getText().trim();
            int typeIdx = cbType.getSelectedIndex();
            int genderIdx = cbGender.getSelectedIndex();

            List<RowFilter<Object, Object>> filters = new ArrayList<>();

            if (!text.isEmpty()) {
                try {
                    int idVal = Integer.parseInt(text);
                    List<RowFilter<Object, Object>> orFilters = new ArrayList<>();
                    orFilters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, idVal, 0));
                    orFilters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 2));
                    filters.add(RowFilter.orFilter(orFilters));
                } catch (NumberFormatException e) {
                    filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text), 2));
                }
            }

            if (typeIdx > 0) {
                try {
                    int val = Integer.parseInt(cbType.getSelectedItem().toString().split(" - ")[0]);
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, val, 3));
                } catch (Exception e) {
                }
            }

            if (genderIdx > 0) {
                try {
                    int val = Integer.parseInt(cbGender.getSelectedItem().toString().split(" - ")[0]);
                    filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, val, 4));
                } catch (Exception e) {
                }
            }

            if (filters.isEmpty())
                sorter.setRowFilter(null);
            else
                sorter.setRowFilter(RowFilter.andFilter(filters));
        };

        txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                doFilter.run();
            }
        });
        cbType.addActionListener(e -> doFilter.run());
        cbGender.addActionListener(e -> doFilter.run());

        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = t.getSelectedRow();
                    if (r != -1) {
                        int modelRow = t.convertRowIndexToModel(r);
                        callback.onSelect((int) m.getValueAt(modelRow, 0), (String) m.getValueAt(modelRow, 2));
                        d.dispose();
                    }
                }
            }
        });

        d.add(pFilter, BorderLayout.NORTH);
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        d.setVisible(true);
    }

    private void showOptionSearchDialog(JDialog parent, ItemCallback callback) {
        JDialog d = new JDialog(parent, "Tìm kiếm Option", true);
        d.setSize(600, 500);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(parent);

        JTextField txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Nhập tên option..."));

        DefaultTableModel m = new DefaultTableModel(new String[] { "ID", "Tên Option" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (Map.Entry<Integer, String> entry : optionTemplateMap.entrySet()) {
            m.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }

        JTable t = new JTable(m);
        t.setRowHeight(25);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        t.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0)
                    sorter.setRowFilter(null);
                else
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = t.getSelectedRow();
                    if (r != -1) {
                        callback.onSelect((int) t.getValueAt(r, 0), (String) t.getValueAt(r, 1));
                        d.dispose();
                    }
                }
            }
        });

        d.add(txtSearch, BorderLayout.NORTH);
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        d.setVisible(true);
    }

    private void saveGiftcodeToDB(Integer id, String code, String count, String expired, int type,
            DefaultTableModel itemModel, JDialog dialog) {
        JSONArray detailArr = new JSONArray();
        for (int i = 0; i < itemModel.getRowCount(); i++) {
            try {
                JSONObject itemObj = new JSONObject();
                itemObj.put("temp_id", Integer.parseInt(itemModel.getValueAt(i, 0).toString()));
                itemObj.put("quantity", Integer.parseInt(itemModel.getValueAt(i, 3).toString()));
                String optStr = itemModel.getValueAt(i, 4).toString();
                itemObj.put("options", JSONValue.parse(optStr));
                detailArr.add(itemObj);
            } catch (Exception e) {
            }
        }

        new Thread(() -> {
            try (Connection conn = getConnection()) {
                String query;
                if (id == null) {
                    query = "INSERT INTO giftcode (code, count_left, detail, datecreate, expired, type) VALUES (?, ?, ?, NOW(), ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, code);
                        ps.setInt(2, Integer.parseInt(count));
                        ps.setString(3, detailArr.toJSONString());
                        ps.setString(4, expired);
                        ps.setInt(5, type);
                        ps.executeUpdate();
                    }
                } else {
                    int dbId = (int) model.getValueAt(id, 0);
                    query = "UPDATE giftcode SET code=?, count_left=?, detail=?, expired=?, type=? WHERE id=?";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, code);
                        ps.setInt(2, Integer.parseInt(count));
                        ps.setString(3, detailArr.toJSONString());
                        ps.setString(4, expired);
                        ps.setInt(5, type);
                        ps.setInt(6, dbId);
                        ps.executeUpdate();
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    loadDataFromDB();
                    JOptionPane.showMessageDialog(this, "Lưu thành công!");
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi lưu DB: " + e.getMessage()));
            }
        }).start();
    }
}
