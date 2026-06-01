package server.ui;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import jdbc.DBConnecter;

public class TopupRewardPanel extends JPanel {

    // Danh sách bảng
    private final Map<String, String> tableMap = new HashMap<>();
    private String currentTable = "moc_nap";

    // --- CACHE DỮ LIỆU ---
    private final Map<Integer, String> itemTemplateMap = new HashMap<>();
    private final Map<Integer, Integer> itemIconIdMap = new HashMap<>();
    private final Map<Integer, String> optionTemplateMap = new HashMap<>();

    // --- CACHE ICON ---
    private final Map<Integer, ImageIcon> iconCache = new HashMap<>();
    private final Map<Integer, Boolean> noIconCache = new HashMap<>();

    // UI Components
    private DefaultListModel<String> listModel;
    private JList<String> listMoc;
    private JPanel pDetailContainer;
    private JComboBox<String> cbTableSelector;
    private Map<Integer, String> currentTableCache = new HashMap<>();

    // Colors
    private final Color CL_PRIMARY = new Color(0, 120, 215);
    private final Color CL_DANGER = new Color(220, 53, 69);
    private final Color CL_SUCCESS = new Color(40, 167, 69);
    private final Color CL_WARNING = new Color(255, 193, 7);
    private final Color CL_BG_LIGHT = new Color(248, 249, 250);

    public TopupRewardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Cấu hình bảng
        tableMap.put("Mốc Nạp Tích Lũy (moc_nap)", "moc_nap");
        tableMap.put("Đua Top Nạp (moc_nap_top)", "moc_nap_top");
        tableMap.put("Mốc Săn Boss (moc_san_boss)", "moc_san_boss");
        tableMap.put("Mốc Sức Mạnh (moc_suc_manh)", "moc_suc_manh");
        tableMap.put("Đua Top Sức Mạnh (moc_suc_manh_top)", "moc_suc_manh_top");

        loadCacheData();
        initUI();
        loadDataFromTable(currentTable);
    }

    private Connection getConnection() throws SQLException {
        return DBConnecter.getConnectionServer();
    }

    private void loadCacheData() {
        new Thread(() -> {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT id, name, icon_id FROM item_template")) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        itemTemplateMap.put(id, rs.getString("name"));
                        itemIconIdMap.put(id, rs.getInt("icon_id"));
                    }
                }
                try (ResultSet rs = stmt.executeQuery("SELECT id, name FROM item_option_template")) {
                    while (rs.next())
                        optionTemplateMap.put(rs.getInt("id"), rs.getString("name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getItemName(int id) {
        return itemTemplateMap.getOrDefault(id, "Unknown [" + id + "]");
    }

    private String getOptionName(int id) {
        return optionTemplateMap.getOrDefault(id, "Option [" + id + "]");
    }

    private ImageIcon getItemIcon(int itemId) {
        if (iconCache.containsKey(itemId))
            return iconCache.get(itemId);
        if (noIconCache.containsKey(itemId))
            return null;

        try {
            int iconId = itemIconIdMap.getOrDefault(itemId, -1);
            if (iconId == -1) {
                noIconCache.put(itemId, true);
                return null;
            }

            String[] zoomLevels = { "x4", "x3", "x2", "x1" };
            File f = null;
            for (String zoom : zoomLevels) {
                f = new File("data/icon/" + zoom + "/" + iconId + ".png");
                if (f.exists())
                    break;
            }

            if (f != null && f.exists()) {
                BufferedImage img = ImageIO.read(f);
                Image dimg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(dimg);
                iconCache.put(itemId, icon);
                return icon;
            }
        } catch (Exception e) {
        }
        noIconCache.put(itemId, true);
        return null;
    }

    private void initUI() {
        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pTop.setBackground(Color.WHITE);
        pTop.add(new JLabel("Chọn bảng dữ liệu: "));

        cbTableSelector = new JComboBox<>(tableMap.keySet().toArray(new String[0]));
        cbTableSelector.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cbTableSelector.setPreferredSize(new Dimension(250, 30));
        cbTableSelector.addActionListener(e -> {
            String selected = (String) cbTableSelector.getSelectedItem();
            currentTable = tableMap.get(selected);
            loadDataFromTable(currentTable);
        });
        pTop.add(cbTableSelector);
        add(pTop, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        listMoc = new JList<>(listModel);
        listMoc.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listMoc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listMoc.setFixedCellHeight(35);
        listMoc.setSelectionBackground(new Color(230, 240, 255));
        listMoc.setSelectionForeground(Color.BLACK);

        listMoc.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listMoc.getSelectedIndex() != -1) {
                String selected = listMoc.getSelectedValue();
                try {
                    int start = selected.lastIndexOf("ID:") + 3;
                    int end = selected.lastIndexOf(")");
                    int id = Integer.parseInt(selected.substring(start, end).trim());
                    loadDetail(id);
                } catch (Exception ex) {
                }
            }
        });

        JScrollPane scrollList = new JScrollPane(listMoc);
        scrollList.setBorder(new LineBorder(new Color(200, 200, 200)));

        JPanel pLeft = new JPanel(new BorderLayout());
        pLeft.setPreferredSize(new Dimension(250, 0));

        JPanel pLeftTool = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton btnAddMoc = createButton("Thêm Mới", CL_PRIMARY);
        JButton btnDelMoc = createButton("Xóa Mốc", CL_DANGER);

        btnAddMoc.addActionListener(e -> addNewRowToTable());
        btnDelMoc.addActionListener(e -> deleteCurrentRow());

        pLeftTool.add(btnAddMoc);
        pLeftTool.add(btnDelMoc);

        pLeft.add(new JLabel("  DANH SÁCH MỐC"), BorderLayout.NORTH);
        pLeft.add(scrollList, BorderLayout.CENTER);
        pLeft.add(pLeftTool, BorderLayout.SOUTH);

        pDetailContainer = new JPanel();
        pDetailContainer.setLayout(new BoxLayout(pDetailContainer, BoxLayout.Y_AXIS));
        pDetailContainer.setBackground(Color.WHITE);

        JScrollPane scrollDetail = new JScrollPane(pDetailContainer);
        scrollDetail.setBorder(new LineBorder(new Color(200, 200, 200)));
        scrollDetail.getVerticalScrollBar().setUnitIncrement(16);
        scrollDetail.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDetail.getViewport().setBackground(Color.WHITE);

        JPanel pRight = new JPanel(new BorderLayout());
        JLabel lblHeaderRight = new JLabel("  CHI TIẾT QUÀ TẶNG");
        lblHeaderRight.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderRight.setPreferredSize(new Dimension(0, 35));
        pRight.add(lblHeaderRight, BorderLayout.NORTH);
        pRight.add(scrollDetail, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pLeft, pRight);
        split.setDividerLocation(260);
        split.setDividerSize(5);
        add(split, BorderLayout.CENTER);

        JPanel pBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pBottom.setBackground(Color.WHITE);
        pBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton btnSave = createButton("LƯU THAY ĐỔI VÀO DB", CL_SUCCESS);
        btnSave.setPreferredSize(new Dimension(200, 40));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnSave.addActionListener(e -> saveCurrentMoc());

        pBottom.add(btnSave);
        add(pBottom, BorderLayout.SOUTH);
    }

    private void addNewRowToTable() {
        String input = JOptionPane.showInputDialog(this,
                "Nhập ID Mốc mới (Số nguyên):", "0");

        if (input != null && !input.isEmpty()) {
            try {
                int newId = Integer.parseInt(input.trim());
                if (currentTableCache.containsKey(newId)) {
                    JOptionPane.showMessageDialog(this, "ID này đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                new Thread(() -> {
                    String sql = "INSERT INTO " + currentTable + " (id, detail) VALUES (?, '[]')";
                    try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, newId);
                        ps.executeUpdate();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "Thêm thành công ID: " + newId);
                            loadDataFromTable(currentTable);
                        });
                    } catch (Exception e) {
                        SwingUtilities
                                .invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi thêm: " + e.getMessage()));
                    }
                }).start();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số nguyên!");
            }
        }
    }

    private void deleteCurrentRow() {
        if (listMoc.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Chọn mốc cần xóa!");
            return;
        }
        String selected = listMoc.getSelectedValue();
        int id = Integer.parseInt(selected.split("ID:")[1].replace(")", "").trim());

        int confirm = JOptionPane.showConfirmDialog(this, "Xóa Mốc ID: " + id + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                String sql = "DELETE FROM " + currentTable + " WHERE id = ?";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Đã xóa ID: " + id);
                        loadDataFromTable(currentTable);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void loadDataFromTable(String tableName) {
        listModel.clear();
        currentTableCache.clear();
        pDetailContainer.removeAll();
        pDetailContainer.repaint();

        new Thread(() -> {
            String sql = "SELECT id, detail FROM " + tableName + " ORDER BY id ASC";
            try (Connection conn = getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String detail = rs.getString("detail");
                    currentTableCache.put(id, detail);
                    String display;
                    if (tableName.contains("top"))
                        display = "Top " + id + " (ID:" + id + ")";
                    else
                        display = "Mốc " + String.format("%,d", id) + " (ID:" + id + ")";
                    SwingUtilities.invokeLater(() -> listModel.addElement(display));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadDetail(int mocId) {
        pDetailContainer.removeAll();
        String json = currentTableCache.get(mocId);
        if (json == null)
            json = "[]";

        try {
            Object parsed = JSONValue.parse(json);
            if (parsed instanceof JSONArray) {
                JSONArray arr = (JSONArray) parsed;
                for (Object e : arr) {
                    JSONObject obj = (JSONObject) e;
                    pDetailContainer.add(new ItemRow(obj));
                }
            }
        } catch (Exception e) {
        }

        JButton btnAdd = createButton("+ THÊM VẬT PHẨM MỚI", CL_PRIMARY);
        btnAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdd.setMaximumSize(new Dimension(2000, 35));
        btnAdd.addActionListener(e -> {
            pDetailContainer.add(new ItemRow(null), pDetailContainer.getComponentCount() - 1);
            pDetailContainer.revalidate();
            pDetailContainer.repaint();
        });

        JPanel pBtnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pBtnWrapper.setBackground(Color.WHITE);
        pBtnWrapper.setBorder(new EmptyBorder(10, 0, 10, 0));
        pBtnWrapper.add(btnAdd);
        pBtnWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        pDetailContainer.add(pBtnWrapper);
        pDetailContainer.revalidate();
        pDetailContainer.repaint();
    }

    private void saveCurrentMoc() {
        if (listMoc.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mốc cần lưu!");
            return;
        }
        String selected = listMoc.getSelectedValue();
        int id = Integer.parseInt(selected.split("ID:")[1].replace(")", "").trim());

        JSONArray newArr = new JSONArray();
        for (Component c : pDetailContainer.getComponents()) {
            if (c instanceof ItemRow) {
                ItemRow row = (ItemRow) c;
                JSONObject obj = row.toJson();
                if (obj != null)
                    newArr.add(obj);
            }
        }

        new Thread(() -> {
            String sql = "UPDATE " + currentTable + " SET detail = ? WHERE id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newArr.toJSONString());
                ps.setInt(2, id);
                ps.executeUpdate();
                currentTableCache.put(id, newArr.toJSONString());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lưu thành công!"));
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi lưu: " + e.getMessage()));
            }
        }).start();
    }

    private class ItemRow extends JPanel {
        private JTextField txtId = new JTextField(4);
        private JLabel lblIcon = new JLabel();
        private JLabel lblName = new JLabel("---");
        private JTextField txtQty = new JTextField("1", 3);
        private JTextField txtOpts = new JTextField();
        private JTextArea txtOptPreview = new JTextArea();

        public ItemRow(JSONObject data) {
            setLayout(new GridBagLayout());
            setBackground(CL_BG_LIGHT);
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                    new EmptyBorder(5, 5, 5, 5)));

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(2, 3, 2, 3);
            g.weighty = 0;

            g.gridx = 0;
            g.weightx = 0;
            add(new JLabel("ID:"), g);
            g.gridx = 1;
            add(txtId, g);

            g.gridx = 2;
            lblIcon.setPreferredSize(new Dimension(24, 24));
            add(lblIcon, g);

            g.gridx = 3;
            JButton btnSearch = createButton("Tìm", CL_PRIMARY);
            btnSearch.setPreferredSize(new Dimension(55, 25));
            btnSearch.addActionListener(e -> showSearchItemDialog());
            add(btnSearch, g);

            g.gridx = 4;
            g.weightx = 0.2;
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblName.setForeground(new Color(0, 100, 0));
            lblName.setPreferredSize(new Dimension(80, 20));
            add(lblName, g);

            g.gridx = 5;
            g.weightx = 0;
            add(new JLabel("SL:"), g);
            g.gridx = 6;
            add(txtQty, g);

            g.gridx = 7;
            g.weightx = 1.0;
            g.fill = GridBagConstraints.BOTH;
            g.weighty = 1.0;

            txtOptPreview.setEditable(false);
            txtOptPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            txtOptPreview.setForeground(Color.DARK_GRAY);
            txtOptPreview.setBackground(new Color(245, 245, 245));
            txtOptPreview.setLineWrap(false);

            JScrollPane scrollOpt = new JScrollPane(txtOptPreview);
            scrollOpt.setBorder(new LineBorder(Color.LIGHT_GRAY));
            scrollOpt.setPreferredSize(new Dimension(100, 60));

            add(scrollOpt, g);

            g.fill = GridBagConstraints.HORIZONTAL;
            g.weighty = 0;

            g.gridx = 8;
            g.weightx = 0;
            JButton btnEditOpt = createButton("Option", CL_WARNING);
            btnEditOpt.setForeground(Color.BLACK);
            btnEditOpt.setPreferredSize(new Dimension(70, 25));
            btnEditOpt.addActionListener(e -> showOptionManagerDialog());
            add(btnEditOpt, g);

            g.gridx = 9;
            JButton btnDel = createButton("Xóa", CL_DANGER);
            btnDel.setPreferredSize(new Dimension(55, 25));
            btnDel.addActionListener(e -> {
                Container p = getParent();
                p.remove(this);
                p.revalidate();
                p.repaint();
            });
            add(btnDel, g);

            txtId.getDocument().addDocumentListener(new SimpleDocumentListener() {
                @Override
                public void update() {
                    updateItemInfo();
                }
            });

            if (data != null) {
                try {
                    if (data.containsKey("temp_id"))
                        txtId.setText(data.get("temp_id").toString());
                    if (data.containsKey("quantity"))
                        txtQty.setText(data.get("quantity").toString());

                    if (data.containsKey("options")) {
                        StringBuilder sb = new StringBuilder();
                        StringBuilder preview = new StringBuilder();
                        JSONArray opts = (JSONArray) data.get("options");
                        for (Object oe : opts) {
                            JSONObject o = (JSONObject) oe;
                            int id = Integer.parseInt(o.get("id").toString());
                            int param = Integer.parseInt(o.get("param").toString());
                            sb.append(id).append("-").append(param).append(";");
                            preview.append(getOptionName(id).replace("#", param + "")).append("\n");
                        }
                        if (sb.length() > 0)
                            sb.setLength(sb.length() - 1);
                        txtOpts.setText(sb.toString());

                        txtOptPreview.setText(preview.toString());
                        txtOptPreview.setCaretPosition(0);
                    }
                } catch (Exception e) {
                }
            }
        }

        private void updateItemInfo() {
            try {
                String text = txtId.getText().trim();
                if (!text.isEmpty()) {
                    int i = Integer.parseInt(text);
                    lblName.setText(getItemName(i));
                    ImageIcon icon = getItemIcon(i);
                    lblIcon.setIcon(icon);
                } else {
                    lblName.setText("---");
                    lblIcon.setIcon(null);
                }
            } catch (Exception ex) {
                lblName.setText("Sai ID");
                lblIcon.setIcon(null);
            }
        }

        private void showSearchItemDialog() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tìm kiếm Item", true);
            d.setSize(600, 550);
            d.setLocationRelativeTo(null);

            JTextField search = new JTextField();
            search.setBorder(BorderFactory.createTitledBorder("Nhập tên item để lọc..."));

            DefaultTableModel m = new DefaultTableModel(new String[] { "ID", "Icon", "Tên Vật Phẩm" }, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }

                public Class<?> getColumnClass(int c) {
                    return c == 1 ? Icon.class : Object.class;
                }
            };

            itemTemplateMap.forEach((k, v) -> {
                m.addRow(new Object[] { k, getItemIcon(k), v });
            });

            JTable t = new JTable(m);
            t.setRowHeight(30);
            t.getColumnModel().getColumn(0).setPreferredWidth(60);
            t.getColumnModel().getColumn(1).setPreferredWidth(40);
            t.getColumnModel().getColumn(2).setPreferredWidth(350);

            TableRowSorter<DefaultTableModel> s = new TableRowSorter<>(m);
            t.setRowSorter(s);

            search.getDocument().addDocumentListener(new SimpleDocumentListener() {
                @Override
                public void update() {
                    String text = search.getText();
                    if (text.trim().length() == 0)
                        s.setRowFilter(null);
                    else {
                        try {
                            s.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                        } catch (Exception ex) {
                        }
                    }
                }
            });

            t.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int r = t.getSelectedRow();
                        if (r != -1) {
                            int modelRow = t.convertRowIndexToModel(r);
                            txtId.setText(t.getModel().getValueAt(modelRow, 0).toString());
                            d.dispose();
                        }
                    }
                }
            });

            d.add(search, BorderLayout.NORTH);
            d.add(new JScrollPane(t), BorderLayout.CENTER);
            d.setVisible(true);
        }

        private void showOptionManagerDialog() {
            JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quản lý Option", true);
            d.setSize(650, 500);
            d.setLocationRelativeTo(null);
            d.setLayout(new BorderLayout());

            DefaultTableModel mOpt = new DefaultTableModel(new String[] { "ID", "Tên Option", "Param" }, 0) {
                public boolean isCellEditable(int r, int c) {
                    return c == 2;
                }
            };

            String raw = txtOpts.getText().trim();
            if (!raw.isEmpty()) {
                for (String p : raw.split(";")) {
                    try {
                        String[] kv = p.split("-");
                        int id = Integer.parseInt(kv[0]);
                        int param = Integer.parseInt(kv[1]);
                        mOpt.addRow(new Object[] { id, getOptionName(id), param });
                    } catch (Exception ex) {
                    }
                }
            }

            JTable tOpt = new JTable(mOpt);
            tOpt.setRowHeight(28);

            JPanel pTool = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnAddOpt = createButton("Tìm & Thêm", CL_PRIMARY);
            JButton btnDelOpt = createButton("Xóa dòng", CL_DANGER);
            pTool.add(btnAddOpt);
            pTool.add(btnDelOpt);

            btnAddOpt.addActionListener(e -> {
                JDialog dSearch = new JDialog(d, "Tìm Option", true);
                dSearch.setSize(500, 500);
                dSearch.setLocationRelativeTo(d);

                JTextField txtSearchOpt = new JTextField();
                txtSearchOpt.setBorder(BorderFactory.createTitledBorder("Nhập tên option..."));

                DefaultTableModel mSearch = new DefaultTableModel(new String[] { "ID", "Tên Option" }, 0) {
                    public boolean isCellEditable(int r, int c) {
                        return false;
                    }
                };
                optionTemplateMap.forEach((k, v) -> mSearch.addRow(new Object[] { k, v }));
                JTable tSearch = new JTable(mSearch);
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mSearch);
                tSearch.setRowSorter(sorter);

                txtSearchOpt.getDocument().addDocumentListener(new SimpleDocumentListener() {
                    @Override
                    public void update() {
                        String text = txtSearchOpt.getText();
                        if (text.trim().length() == 0)
                            sorter.setRowFilter(null);
                        else {
                            try {
                                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                            } catch (Exception ex) {
                            }
                        }
                    }
                });

                tSearch.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent ev) {
                        if (ev.getClickCount() == 2) {
                            int r = tSearch.getSelectedRow();
                            if (r != -1) {
                                int modelRow = tSearch.convertRowIndexToModel(r);
                                int id = (int) tSearch.getModel().getValueAt(modelRow, 0);
                                String name = (String) tSearch.getModel().getValueAt(modelRow, 1);
                                String paramStr = JOptionPane.showInputDialog(dSearch,
                                        "Nhập chỉ số (Param) cho:\n" + name, "0");
                                if (paramStr != null) {
                                    try {
                                        int param = Integer.parseInt(paramStr);
                                        mOpt.addRow(new Object[] { id, name, param });
                                    } catch (Exception ex) {
                                    }
                                }
                                dSearch.dispose();
                            }
                        }
                    }
                });

                dSearch.add(txtSearchOpt, BorderLayout.NORTH);
                dSearch.add(new JScrollPane(tSearch), BorderLayout.CENTER);
                dSearch.setVisible(true);
            });

            btnDelOpt.addActionListener(e -> {
                int r = tOpt.getSelectedRow();
                if (r != -1)
                    mOpt.removeRow(r);
            });

            JPanel pBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnConfirm = createButton("Lưu & Đóng", CL_SUCCESS);
            btnConfirm.addActionListener(e -> {
                StringBuilder sb = new StringBuilder();
                StringBuilder preview = new StringBuilder();
                for (int i = 0; i < mOpt.getRowCount(); i++) {
                    int id = Integer.parseInt(mOpt.getValueAt(i, 0).toString());
                    int param = Integer.parseInt(mOpt.getValueAt(i, 2).toString());
                    String name = mOpt.getValueAt(i, 1).toString();
                    sb.append(id).append("-").append(param).append(";");
                    preview.append(name.replace("#", param + "")).append("\n");
                }
                if (sb.length() > 0)
                    sb.setLength(sb.length() - 1);
                txtOpts.setText(sb.toString());
                txtOptPreview.setText(preview.toString());
                txtOptPreview.setCaretPosition(0);
                d.dispose();
            });
            pBot.add(btnConfirm);

            d.add(pTool, BorderLayout.NORTH);
            d.add(new JScrollPane(tOpt), BorderLayout.CENTER);
            d.add(pBot, BorderLayout.SOUTH);
            d.setVisible(true);
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("temp_id", Integer.parseInt(txtId.getText().trim()));
                obj.put("quantity", Integer.parseInt(txtQty.getText().trim()));
                JSONArray opts = new JSONArray();
                String raw = txtOpts.getText().trim();
                if (!raw.isEmpty()) {
                    for (String p : raw.split(";")) {
                        String[] kv = p.split("-");
                        if (kv.length == 2) {
                            JSONObject o = new JSONObject();
                            o.put("id", Integer.parseInt(kv[0].trim()));
                            o.put("param", Integer.parseInt(kv[1].trim()));
                            opts.add(o);
                        }
                    }
                }
                obj.put("options", opts);
                return obj;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(5, 10, 5, 10));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
