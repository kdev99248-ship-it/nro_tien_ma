package server.ui;

import jdbc.DBConnecter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class BotManagerPanel extends JPanel {

    private static class BotData {
        int id;
        String name;
        int head, body, leg;
        int mapId;
        int gender;
        boolean active;
    }

    private static class ToggleSwitch extends JPanel {
        private boolean active = false;
        private Color onColor = new Color(76, 175, 80);
        private Color offColor = new Color(200, 200, 200);

        public ToggleSwitch() {
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public void setSelected(boolean active) {
            this.active = active;
            repaint();
        }

        public boolean isSelected() {
            return active;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = 36;
            int height = 18;
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;

            g2.setColor(active ? onColor : offColor);
            g2.fillRoundRect(x, y, width, height, height, height);

            int circleDia = height - 4;
            int circleX = active ? x + width - circleDia - 2 : x + 2;
            int circleY = y + 2;

            g2.setColor(Color.WHITE);
            g2.fillOval(circleX, circleY, circleDia, circleDia);
            g2.dispose();
        }
    }

    private static class ToggleSwitchRenderer extends ToggleSwitch implements javax.swing.table.TableCellRenderer {
        public ToggleSwitchRenderer() {
            super();
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Boolean) {
                setSelected((Boolean) value);
            }
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }

    private static class ToggleSwitchEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private ToggleSwitch toggleSwitch;

        public ToggleSwitchEditor() {
            toggleSwitch = new ToggleSwitch();
            toggleSwitch.setOpaque(true);
            toggleSwitch.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    toggleSwitch.setSelected(!toggleSwitch.isSelected());
                    stopCellEditing();
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return toggleSwitch.isSelected();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof Boolean) {
                toggleSwitch.setSelected((Boolean) value);
            }
            toggleSwitch.setBackground(table.getSelectionBackground());
            return toggleSwitch;
        }
    }

    private static class DisguiseItem {
        int id;
        String name;
        int iconId;
        int head, body, leg;
        int gender;
        ImageIcon icon;
    }

    private static class MapTemplateItem {
        int id;
        String name;

        public MapTemplateItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "[" + id + "] " + name;
        }
    }

    private final List<BotData> bots = new ArrayList<>();
    private final List<DisguiseItem> disguises = new ArrayList<>();
    private final Map<Integer, ImageIcon> iconCache = new HashMap<>();

    private JTable botTable;
    private DefaultTableModel botTableModel;

    private JTextField txtName, txtHead, txtBody, txtLeg;
    private JComboBox<MapTemplateItem> cbMapId;
    private JComboBox<String> cbGender;
    private JCheckBox chkActive;
    private JPanel canvasPanel;
    private BotCanvas botCanvas;
    private JButton btnSelectDisguise, btnSave, btnReload, btnAdd, btnDelete;

    private BotData selectedBot = null;
    private static final String ICON_DIR = "data/icon/x4";

    public BotManagerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel title = ServerGuiUtils.createStyledLabel("Quản lý Bot", 22, true);
        title.setForeground(new Color(0, 102, 204));
        headerPanel.add(title, BorderLayout.WEST);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setBackground(Color.WHITE);
        btnReload = ServerGuiUtils.createStyledButton("⟳ Tải lại", new Color(0, 120, 215), Color.WHITE);
        btnAdd = ServerGuiUtils.createStyledButton("＋ Thêm mới", new Color(40, 167, 69), Color.WHITE);
        btnDelete = ServerGuiUtils.createStyledButton("✕ Xóa", new Color(220, 53, 69), Color.WHITE);
        headerBtns.add(btnReload);
        headerBtns.add(btnAdd);
        headerBtns.add(btnDelete);
        headerPanel.add(headerBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.4);

        splitPane.setLeftComponent(createTablePanel());
        splitPane.setRightComponent(new JScrollPane(createEditorPanel()));

        add(splitPane, BorderLayout.CENTER);

        setupEventHandlers();
        loadData();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(ServerGuiUtils.createSectionBorder("Danh sách Bot"));

        String[] columns = { "ID", "Tên", "Map", "Hành tinh", "Trạng thái" };
        botTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 4; 
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
        };

        botTableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 4) {
                int row = e.getFirstRow();
                if (row >= 0 && row < botTableModel.getRowCount()) {
                    boolean isActive = (Boolean) botTableModel.getValueAt(row, 4);
                    int botId = (Integer) botTableModel.getValueAt(row, 0);
                    
                    // Update in DB
                    try (java.sql.Connection con = jdbc.DBConnecter.getConnectionServer();
                         java.sql.PreparedStatement ps = con.prepareStatement("UPDATE data_bot SET active=? WHERE id=?")) {
                        ps.setInt(1, isActive ? 1 : 0);
                        ps.setInt(2, botId);
                        ps.executeUpdate();
                        
                        // update local list
                        for (BotData b : bots) {
                            if (b.id == botId) {
                                b.active = isActive;
                                if (selectedBot != null && selectedBot.id == botId) {
                                    chkActive.setSelected(isActive);
                                }
                                break;
                            }
                        }
                        
                        // reload server bots
                        bot.BotManager.gI().reload();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        botTable = new JTable(botTableModel);
        botTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        botTable.setRowHeight(30);
        botTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        botTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        botTable.getTableHeader().setBackground(new Color(230, 242, 255));
        
        botTable.setDefaultRenderer(Boolean.class, new ToggleSwitchRenderer());
        botTable.setDefaultEditor(Boolean.class, new ToggleSwitchEditor());

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        botTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        botTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);

        JScrollPane scrollPane = new JScrollPane(botTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(ServerGuiUtils.createSectionBorder("Chỉnh sửa / Thêm mới"));

        // Basic Info Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(ServerGuiUtils.createSectionBorder("Thông tin Bot"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(15);
        cbMapId = new JComboBox<>();
        cbGender = new JComboBox<>(new String[] { "0 - Trái Đất", "1 - Xayda", "2 - Namec" });
        txtHead = new JTextField(8);
        txtBody = new JTextField(8);
        txtLeg = new JTextField(8);
        chkActive = new JCheckBox("Kích hoạt vòng lặp");
        chkActive.setBackground(Color.WHITE);

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Tên Bot:", txtName);
        addFormRow(formPanel, gbc, row++, "Map:", cbMapId);
        addFormRow(formPanel, gbc, row++, "Hành tinh:", cbGender);
        addFormRow(formPanel, gbc, row++, "Trạng thái:", chkActive);
        addFormRow(formPanel, gbc, row++, "Head ID:", txtHead);
        addFormRow(formPanel, gbc, row++, "Body ID:", txtBody);
        addFormRow(formPanel, gbc, row++, "Leg ID:", txtLeg);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        btnSelectDisguise = ServerGuiUtils.createStyledButton("🔍 Chọn Cải Trang", new Color(102, 51, 153), Color.WHITE);
        formPanel.add(btnSelectDisguise, gbc);

        panel.add(formPanel);
        panel.add(Box.createVerticalStrut(10));

        // Canvas Panel for Image
        canvasPanel = new JPanel(new BorderLayout());
        canvasPanel.setBorder(ServerGuiUtils.createSectionBorder("Canvas Hiển Thị Bot"));
        botCanvas = new BotCanvas();
        canvasPanel.add(botCanvas, BorderLayout.CENTER);

        panel.add(canvasPanel);
        panel.add(Box.createVerticalStrut(15));

        // Save Button
        JPanel saveBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveBtnPanel.setBackground(Color.WHITE);
        btnSave = ServerGuiUtils.createStyledButton("💾 Lưu", new Color(0, 120, 215), Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtnPanel.add(btnSave);
        panel.add(saveBtnPanel);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, gbc);
    }

    private void setupEventHandlers() {
        botTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = botTable.getSelectedRow();
                if (row >= 0 && row < bots.size()) {
                    selectedBot = bots.get(row);
                    populateForm(selectedBot);
                }
            }
        });

        btnReload.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> addNewBot());
        btnDelete.addActionListener(e -> deleteSelectedBot());
        btnSave.addActionListener(e -> saveCurrentBot());
        btnSelectDisguise.addActionListener(e -> openDisguiseSelector());
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                loadMapTemplates();
                loadBots();
                loadDisguises();
                return null;
            }
            @Override
            protected void done() {
                refreshTable();
                if (!bots.isEmpty()) {
                    botTable.setRowSelectionInterval(0, 0);
                } else {
                    clearForm();
                }
            }
        };
        worker.execute();
    }

    private void loadBots() {
        bots.clear();
        String sql = "SELECT * FROM data_bot ORDER BY id";
        try (Connection con = DBConnecter.getConnectionServer();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BotData b = new BotData();
                b.id = rs.getInt("id");
                b.name = rs.getString("name");
                b.head = rs.getInt("head");
                b.body = rs.getInt("body");
                b.leg = rs.getInt("leg");
                b.mapId = rs.getInt("map_id");
                b.gender = rs.getInt("gender");
                b.active = true;
                try {
                    b.active = rs.getInt("active") != 0;
                } catch (Exception ignored) { }
                bots.add(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMapTemplates() {
        cbMapId.removeAllItems();
        String sql = "SELECT id, name FROM map_template ORDER BY id";
        try (Connection con = DBConnecter.getConnectionServer();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cbMapId.addItem(new MapTemplateItem(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDisguises() {
        disguises.clear();
        String sql = "SELECT id, name, icon_id, head, body, leg, gender FROM item_template WHERE type = 5 ORDER BY id";
        try (Connection con = DBConnecter.getConnectionServer();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DisguiseItem d = new DisguiseItem();
                d.id = rs.getInt("id");
                d.name = rs.getString("name");
                d.iconId = rs.getInt("icon_id");
                d.head = rs.getInt("head");
                d.body = rs.getInt("body");
                d.leg = rs.getInt("leg");
                d.gender = rs.getInt("gender");
                d.icon = loadIconFromDisk(d.iconId);
                disguises.add(d);
            }
        } catch (Exception e) {}
    }

    private ImageIcon loadIconFromDisk(int iconId) {
        if (iconCache.containsKey(iconId)) return iconCache.get(iconId);
        try {
            File f = new File(ICON_DIR, iconId + ".png");
            if (f.exists()) {
                Image scaled = ImageIO.read(f).getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaled);
                iconCache.put(iconId, icon);
                return icon;
            }
        } catch (Exception e) {}
        iconCache.put(iconId, null);
        return null;
    }

    private void refreshTable() {
        botTableModel.setRowCount(0);
        String[] planets = { "Trái Đất", "Xayda", "Namec" };
        for (BotData b : bots) {
            String planet = b.gender >= 0 && b.gender < planets.length ? planets[b.gender] : "?";
            String mapDisplay = String.valueOf(b.mapId);
            for (int i = 0; i < cbMapId.getItemCount(); i++) {
                if (cbMapId.getItemAt(i).id == b.mapId) {
                    mapDisplay = cbMapId.getItemAt(i).toString();
                    break;
                }
            }
            botTableModel.addRow(new Object[] { b.id, b.name, mapDisplay, planet, b.active });
        }
    }

    private void populateForm(BotData b) {
        txtName.setText(b.name);
        // Chọn map khớp với b.mapId
        for (int i = 0; i < cbMapId.getItemCount(); i++) {
            if (cbMapId.getItemAt(i).id == b.mapId) {
                cbMapId.setSelectedIndex(i);
                break;
            }
        }
        cbGender.setSelectedIndex(Math.min(b.gender, cbGender.getItemCount() - 1));
        chkActive.setSelected(b.active);
        txtHead.setText(String.valueOf(b.head));
        txtBody.setText(String.valueOf(b.body));
        txtLeg.setText(String.valueOf(b.leg));
        botCanvas.loadBotImages(b.head, b.body, b.leg);
    }

    private void clearForm() {
        selectedBot = null;
        txtName.setText("");
        if (cbMapId.getItemCount() > 0) cbMapId.setSelectedIndex(0);
        cbGender.setSelectedIndex(0);
        chkActive.setSelected(true);
        txtHead.setText("1778");
        txtBody.setText("1776");
        txtLeg.setText("1777");
        botCanvas.loadBotImages(1778, 1776, 1777);
    }

    private void openDisguiseSelector() {
        if (disguises.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu cải trang.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chọn Cải Trang Bot", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel marginPanel = new JPanel(new BorderLayout(8, 8));
        marginPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtSearch = new JTextField();
        marginPanel.add(txtSearch, BorderLayout.NORTH);

        DefaultListModel<DisguiseItem> listModel = new DefaultListModel<>();
        for (DisguiseItem d : disguises) listModel.addElement(d);

        JList<DisguiseItem> list = new JList<>(listModel);
        list.setFixedCellHeight(40);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DisguiseItem d) {
                    lbl.setText(String.format("[%d] %s (H:%d B:%d L:%d)", d.id, d.name, d.head, d.body, d.leg));
                    if (d.icon != null) lbl.setIcon(d.icon);
                }
                return lbl;
            }
        });

        marginPanel.add(new JScrollPane(list), BorderLayout.CENTER);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String filter = txtSearch.getText().trim().toLowerCase();
                listModel.clear();
                for (DisguiseItem d : disguises) {
                    if (filter.isEmpty() || d.name.toLowerCase().contains(filter) || String.valueOf(d.id).contains(filter)) {
                        listModel.addElement(d);
                    }
                }
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        JButton btnSelect = ServerGuiUtils.createStyledButton("Chọn", new Color(0, 120, 215), Color.WHITE);
        btnSelect.addActionListener(e -> {
            DisguiseItem d = list.getSelectedValue();
            if (d != null) {
                txtHead.setText(String.valueOf(d.head));
                txtBody.setText(String.valueOf(d.body));
                txtLeg.setText(String.valueOf(d.leg));
                botCanvas.loadBotImages(d.head, d.body, d.leg);
                dialog.dispose();
            }
        });

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) btnSelect.doClick();
            }
        });

        JPanel botPanel = new JPanel();
        botPanel.add(btnSelect);
        marginPanel.add(botPanel, BorderLayout.SOUTH);

        dialog.add(marginPanel);
        dialog.setVisible(true);
    }

    private void saveCurrentBot() {
        if (selectedBot == null) return;
        try {
            selectedBot.name = txtName.getText().trim();
            if (cbMapId.getSelectedItem() != null) {
                selectedBot.mapId = ((MapTemplateItem) cbMapId.getSelectedItem()).id;
            }
            selectedBot.gender = cbGender.getSelectedIndex();
            selectedBot.head = Integer.parseInt(txtHead.getText().trim());
            selectedBot.body = Integer.parseInt(txtBody.getText().trim());
            selectedBot.leg = Integer.parseInt(txtLeg.getText().trim());
            selectedBot.active = chkActive.isSelected();

            String sql = "UPDATE data_bot SET name=?, map_id=?, gender=?, head=?, body=?, leg=?, active=? WHERE id=?";
            try (Connection con = DBConnecter.getConnectionServer();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, selectedBot.name);
                ps.setInt(2, selectedBot.mapId);
                ps.setInt(3, selectedBot.gender);
                ps.setInt(4, selectedBot.head);
                ps.setInt(5, selectedBot.body);
                ps.setInt(6, selectedBot.leg);
                ps.setInt(7, selectedBot.active ? 1 : 0);
                ps.setInt(8, selectedBot.id);
                ps.executeUpdate();
            }

            refreshTable();
            bot.BotManager.gI().reload();
            JOptionPane.showMessageDialog(this, "Lưu thành công!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void addNewBot() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên bot mới:");
        if (name == null || name.trim().isEmpty()) return;

        try {
            String sql = "INSERT INTO data_bot (name, head, body, leg, map_id, gender, active) VALUES (?, 1778, 1776, 1777, 0, 0, 1)";
            try (Connection con = DBConnecter.getConnectionServer();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name.trim());
                ps.executeUpdate();
            }
            loadData();
            bot.BotManager.gI().reload();
            JOptionPane.showMessageDialog(this, "Thêm bot mới thành công!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm: " + ex.getMessage());
        }
    }

    private void deleteSelectedBot() {
        if (selectedBot == null) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa bot: " + selectedBot.name + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM data_bot WHERE id=?";
                try (Connection con = DBConnecter.getConnectionServer();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, selectedBot.id);
                    ps.executeUpdate();
                }
                loadData();
                bot.BotManager.gI().reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());
            }
        }
    }

    private class BotCanvas extends JPanel {
        private Image imgHead, imgBody, imgLeg;
        private int finalDxHead, finalDyHead, finalDxBody, finalDyBody, finalDxLeg, finalDyLeg;

        public BotCanvas() {
            setPreferredSize(new Dimension(200, 200));
            setBackground(new Color(245, 245, 250));
        }

        public void loadBotImages(int headId, int bodyId, int legId) {
            imgHead = imgBody = imgLeg = null;
            finalDxHead = finalDyHead = finalDxBody = finalDyBody = finalDxLeg = finalDyLeg = 0;
            
            // Dựa trên CHAR_INFO của CreateBossScr cho Frame 0 (Dung 1):
            // Head (Type 0): index 0, frameDx = -13, frameDy = 34
            // Leg (Type 1): index 1, frameDx = -8, frameDy = 10
            // Body (Type 2): index 1, frameDx = -9, frameDy = 16
            loadPart(headId, 0, "head", -13, 34);
            loadPart(bodyId, 1, "body", -9, 16);
            loadPart(legId, 1, "leg", -8, 10);
            repaint();
        }

        private void loadPart(int partId, int detailIndex, String type, int skelDx, int skelDy) {
            String sql = "SELECT data FROM part WHERE id = ?";
            try (Connection con = DBConnecter.getConnectionServer();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, partId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String data = rs.getString("data").replaceAll("\\\"", "");
                    JSONArray dataArray = (JSONArray) JSONValue.parse(data);
                    if (dataArray != null && dataArray.size() > detailIndex) {
                        JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(detailIndex)));
                        if (pd != null && pd.size() >= 3) {
                            int iconId = Integer.parseInt(String.valueOf(pd.get(0)));
                            int partDx = Integer.parseInt(String.valueOf(pd.get(1)));
                            int partDy = Integer.parseInt(String.valueOf(pd.get(2)));
                            
                            // Công thức tính tọa độ x4 chuẩn: x = (frameDx + part.dx) * 4, y = (-frameDy + part.dy) * 4
                            int fDx = (skelDx + partDx) * 4;
                            int fDy = (-skelDy + partDy) * 4;
                            
                            File f = new File(ICON_DIR, iconId + ".png");
                            if (f.exists()) {
                                Image img = ImageIO.read(f);
                                if (type.equals("head")) { imgHead = img; finalDxHead = fDx; finalDyHead = fDy; }
                                else if (type.equals("body")) { imgBody = img; finalDxBody = fDx; finalDyBody = fDy; }
                                else if (type.equals("leg")) { imgLeg = img; finalDxLeg = fDx; finalDyLeg = fDy; }
                            }
                        }
                    }
                }
            } catch (Exception e) {}
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Tạo một khung xOy cố định. Gốc tọa độ O (originX, originY) ở giữa canvas
            int originX = getWidth() / 2;
            int originY = getHeight() / 2 + 40; 

            // Thứ tự vẽ chuẩn: Leg -> Body -> Head
            // Vẽ chân (Leg) đầu tiên (lớp thấp nhất)
            if (imgLeg != null) {
                g.drawImage(imgLeg, originX + finalDxLeg, originY + finalDyLeg, null);
            }
            // Vẽ thân (Body) ở lớp giữa
            if (imgBody != null) {
                g.drawImage(imgBody, originX + finalDxBody, originY + finalDyBody, null);
            }
            // Vẽ đầu (Head) nằm ngoài cùng (cao nhất)
            if (imgHead != null) {
                g.drawImage(imgHead, originX + finalDxHead, originY + finalDyHead, null);
            }
        }
    }
}
