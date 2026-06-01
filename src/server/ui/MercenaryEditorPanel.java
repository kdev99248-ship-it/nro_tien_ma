package server.ui;

import jdbc.DBConnecter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class MercenaryEditorPanel extends JPanel {

    // --- Data classes ---
    private static class MercenaryData {
        int id;
        String name;
        int planetType;
        int price30Min, price1Hour, price5Hour;
        long hp, mp, dame;
        int def, crit;
        int head, body, leg;
        int gender;
        boolean canAttackBoss;
        boolean active;
    }

    private static class DisguiseItem {
        int id;
        String name;
        int iconId;
        int head, body, leg;
        int gender;
        ImageIcon icon;
    }

    // --- Fields ---
    private final List<MercenaryData> mercenaries = new ArrayList<>();
    private final List<DisguiseItem> disguises = new ArrayList<>();
    private final Map<Integer, ImageIcon> iconCache = new HashMap<>();

    private JTable mercTable;
    private DefaultTableModel mercTableModel;

    // Form fields
    private JTextField txtName, txtHp, txtMp, txtDame, txtDef, txtCrit;
    private JTextField txtPrice30, txtPrice1h, txtPrice5h;
    private JTextField txtHead, txtBody, txtLeg;
    private JComboBox<String> cbPlanet, cbGender;
    private JCheckBox chkCanAttackBoss, chkActive;
    private JLabel lblDisguisePreview;
    private JButton btnSelectDisguise, btnSave, btnReload, btnAdd, btnDelete;

    private MercenaryData selectedMerc = null;

    // Icon path
    private static final String ICON_DIR = "data/icon/x4";

    public MercenaryEditorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel title = ServerGuiUtils.createStyledLabel("Quản lý Lính Đánh Thuê", 22, true);
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

        // Split: Left = Table, Right = Editor
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(520);
        splitPane.setResizeWeight(0.45);

        // --- LEFT: Table ---
        JPanel tablePanel = createTablePanel();
        splitPane.setLeftComponent(tablePanel);

        // --- RIGHT: Editor ---
        JPanel editorPanel = createEditorPanel();
        splitPane.setRightComponent(new JScrollPane(editorPanel));

        add(splitPane, BorderLayout.CENTER);

        // --- Event handlers ---
        setupEventHandlers();

        // Load data
        loadData();
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(ServerGuiUtils.createSectionBorder("Danh sách Lính Đánh Thuê"));

        String[] columns = { "ID", "Tên", "Hành tinh", "HP", "Dame", "Đánh Boss" };
        mercTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        mercTable = new JTable(mercTableModel);
        mercTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mercTable.setRowHeight(30);
        mercTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mercTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        mercTable.getTableHeader().setBackground(new Color(230, 242, 255));
        mercTable.setGridColor(new Color(230, 230, 230));

        // Center align for some columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        mercTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        mercTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        mercTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Column widths
        mercTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        mercTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        mercTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        mercTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        mercTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        mercTable.getColumnModel().getColumn(5).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(mercTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(ServerGuiUtils.createSectionBorder("Chỉnh sửa"));

        // --- Basic info ---
        JPanel basicPanel = new JPanel(new GridBagLayout());
        basicPanel.setBackground(Color.WHITE);
        basicPanel.setBorder(ServerGuiUtils.createSectionBorder("Thông tin cơ bản"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtName = new JTextField(15);
        cbPlanet = new JComboBox<>(new String[] { "0 - Trái Đất", "1 - Xayda", "2 - Namec" });
        cbGender = new JComboBox<>(new String[] { "0 - Trái Đất", "1 - Xayda", "2 - Namec" });
        chkCanAttackBoss = new JCheckBox("Có thể đánh Boss");
        chkActive = new JCheckBox("Hoạt động");
        chkActive.setSelected(true);

        int row = 0;
        addFormRow(basicPanel, gbc, row++, "Tên:", txtName);
        addFormRow(basicPanel, gbc, row++, "Hành tinh:", cbPlanet);
        addFormRow(basicPanel, gbc, row++, "Giới tính:", cbGender);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        basicPanel.add(chkCanAttackBoss, gbc);
        row++;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        basicPanel.add(chkActive, gbc);

        panel.add(basicPanel);
        panel.add(Box.createVerticalStrut(8));

        // --- Stats ---
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(ServerGuiUtils.createSectionBorder("Chỉ số"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtHp = new JTextField(12);
        txtMp = new JTextField(12);
        txtDame = new JTextField(12);
        txtDef = new JTextField(12);
        txtCrit = new JTextField(12);

        row = 0;
        addFormRow(statsPanel, gbc, row++, "HP:", txtHp);
        addFormRow(statsPanel, gbc, row++, "MP:", txtMp);
        addFormRow(statsPanel, gbc, row++, "Dame:", txtDame);
        addFormRow(statsPanel, gbc, row++, "Def:", txtDef);
        addFormRow(statsPanel, gbc, row++, "Crit %:", txtCrit);

        panel.add(statsPanel);
        panel.add(Box.createVerticalStrut(8));

        // --- Pricing ---
        JPanel pricePanel = new JPanel(new GridBagLayout());
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setBorder(ServerGuiUtils.createSectionBorder("Giá thuê (Thỏi vàng)"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtPrice30 = new JTextField(8);
        txtPrice1h = new JTextField(8);
        txtPrice5h = new JTextField(8);

        row = 0;
        addFormRow(pricePanel, gbc, row++, "30 phút:", txtPrice30);
        addFormRow(pricePanel, gbc, row++, "1 giờ:", txtPrice1h);
        addFormRow(pricePanel, gbc, row++, "5 giờ:", txtPrice5h);

        panel.add(pricePanel);
        panel.add(Box.createVerticalStrut(8));

        // --- Appearance ---
        JPanel appearPanel = new JPanel(new GridBagLayout());
        appearPanel.setBackground(Color.WHITE);
        appearPanel.setBorder(ServerGuiUtils.createSectionBorder("Ngoại hình (Head/Body/Leg)"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtHead = new JTextField(8);
        txtBody = new JTextField(8);
        txtLeg = new JTextField(8);

        row = 0;
        addFormRow(appearPanel, gbc, row++, "Head ID:", txtHead);
        addFormRow(appearPanel, gbc, row++, "Body ID:", txtBody);
        addFormRow(appearPanel, gbc, row++, "Leg ID:", txtLeg);

        // Disguise button + preview
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        btnSelectDisguise = ServerGuiUtils.createStyledButton("Chọn Cải Trang...", new Color(102, 51, 153),
                Color.WHITE);
        appearPanel.add(btnSelectDisguise, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        lblDisguisePreview = new JLabel("Chưa chọn cải trang");
        lblDisguisePreview.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDisguisePreview.setForeground(Color.GRAY);
        appearPanel.add(lblDisguisePreview, gbc);

        panel.add(appearPanel);
        panel.add(Box.createVerticalStrut(12));

        // --- Save button ---
        JPanel saveBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveBtnPanel.setBackground(Color.WHITE);
        btnSave = ServerGuiUtils.createStyledButton("💾 Lưu thay đổi", new Color(0, 120, 215), Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtnPanel.add(btnSave);
        panel.add(saveBtnPanel);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, gbc);
    }

    private void setupEventHandlers() {
        // Table selection
        mercTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = mercTable.getSelectedRow();
                if (row >= 0 && row < mercenaries.size()) {
                    selectedMerc = mercenaries.get(row);
                    populateForm(selectedMerc);
                }
            }
        });

        // Reload
        btnReload.addActionListener(e -> loadData());

        // Add new
        btnAdd.addActionListener(e -> addNewMercenary());

        // Delete
        btnDelete.addActionListener(e -> deleteSelectedMercenary());

        // Save
        btnSave.addActionListener(e -> saveCurrentMercenary());

        // Select disguise
        btnSelectDisguise.addActionListener(e -> openDisguiseSelector());
    }

    // ============================================
    // DATA LOADING
    // ============================================

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                loadMercenaries();
                loadDisguises();
                return null;
            }

            @Override
            protected void done() {
                refreshTable();
                if (!mercenaries.isEmpty()) {
                    mercTable.setRowSelectionInterval(0, 0);
                } else {
                    clearForm();
                }
            }
        };
        worker.execute();
    }

    private void loadMercenaries() {
        mercenaries.clear();
        String sql = "SELECT * FROM mercenary_template ORDER BY planet_type, id";
        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MercenaryData m = new MercenaryData();
                m.id = rs.getInt("id");
                m.name = rs.getString("name");
                m.planetType = rs.getInt("planet_type");
                m.price30Min = rs.getInt("price_30min");
                m.price1Hour = rs.getInt("price_1hour");
                m.price5Hour = rs.getInt("price_5hour");
                m.hp = rs.getLong("hp");
                m.mp = rs.getLong("mp");
                m.dame = rs.getLong("dame");
                m.def = rs.getInt("def");
                m.crit = rs.getInt("crit");
                m.head = rs.getInt("head");
                m.body = rs.getInt("body");
                m.leg = rs.getInt("leg");
                m.gender = rs.getInt("gender");
                m.canAttackBoss = rs.getBoolean("can_attack_boss");
                m.active = rs.getBoolean("active");
                mercenaries.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(
                    () -> JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu lính đánh thuê:\n" + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ImageIcon loadIconFromDisk(int iconId) {
        if (iconCache.containsKey(iconId)) {
            return iconCache.get(iconId);
        }
        try {
            File iconFile = new File(ICON_DIR, iconId + ".png");
            if (iconFile.exists()) {
                BufferedImage img = ImageIO.read(iconFile);
                if (img != null) {
                    Image scaled = img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaled);
                    iconCache.put(iconId, icon);
                    return icon;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        iconCache.put(iconId, null);
        return null;
    }

    // ============================================
    // TABLE / FORM
    // ============================================

    private void refreshTable() {
        mercTableModel.setRowCount(0);
        String[] planets = { "Trái Đất", "Xayda", "Namec" };
        for (MercenaryData m : mercenaries) {
            String planet = m.planetType >= 0 && m.planetType < planets.length ? planets[m.planetType] : "?";
            mercTableModel.addRow(new Object[] {
                    m.id,
                    m.name,
                    planet,
                    formatNumber(m.hp),
                    formatNumber(m.dame),
                    m.canAttackBoss ? "✓" : "✗"
            });
        }
    }

    private void populateForm(MercenaryData m) {
        txtName.setText(m.name);
        cbPlanet.setSelectedIndex(Math.min(m.planetType, cbPlanet.getItemCount() - 1));
        cbGender.setSelectedIndex(Math.min(m.gender, cbGender.getItemCount() - 1));
        chkCanAttackBoss.setSelected(m.canAttackBoss);
        chkActive.setSelected(m.active);

        txtHp.setText(String.valueOf(m.hp));
        txtMp.setText(String.valueOf(m.mp));
        txtDame.setText(String.valueOf(m.dame));
        txtDef.setText(String.valueOf(m.def));
        txtCrit.setText(String.valueOf(m.crit));

        txtPrice30.setText(String.valueOf(m.price30Min));
        txtPrice1h.setText(String.valueOf(m.price1Hour));
        txtPrice5h.setText(String.valueOf(m.price5Hour));

        txtHead.setText(String.valueOf(m.head));
        txtBody.setText(String.valueOf(m.body));
        txtLeg.setText(String.valueOf(m.leg));

        lblDisguisePreview.setIcon(null);
        lblDisguisePreview.setText("Head=" + m.head + " Body=" + m.body + " Leg=" + m.leg);
    }

    private void clearForm() {
        selectedMerc = null;
        txtName.setText("");
        cbPlanet.setSelectedIndex(0);
        cbGender.setSelectedIndex(0);
        chkCanAttackBoss.setSelected(false);
        chkActive.setSelected(true);
        txtHp.setText("");
        txtMp.setText("");
        txtDame.setText("");
        txtDef.setText("");
        txtCrit.setText("");
        txtPrice30.setText("");
        txtPrice1h.setText("");
        txtPrice5h.setText("");
        txtHead.setText("");
        txtBody.setText("");
        txtLeg.setText("");
        lblDisguisePreview.setIcon(null);
        lblDisguisePreview.setText("Chưa chọn cải trang");
    }

    // ============================================
    // DISGUISE SELECTOR DIALOG
    // ============================================

    private void openDisguiseSelector() {
        if (disguises.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu cải trang. Hãy tải lại dữ liệu.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chọn Cải Trang",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(700, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));
        ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTextField txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        dialog.add(searchPanel, BorderLayout.NORTH);

        // Disguise list
        DefaultListModel<DisguiseItem> listModel = new DefaultListModel<>();
        for (DisguiseItem d : disguises) {
            listModel.addElement(d);
        }

        JList<DisguiseItem> disguiseList = new JList<>(listModel);
        disguiseList.setFixedCellHeight(45);
        disguiseList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        disguiseList.setCellRenderer(new DisguiseCellRenderer());
        disguiseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(disguiseList);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Search filter
        txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                String filter = txtSearch.getText().trim().toLowerCase();
                listModel.clear();
                for (DisguiseItem d : disguises) {
                    if (filter.isEmpty()
                            || d.name.toLowerCase().contains(filter)
                            || String.valueOf(d.id).contains(filter)) {
                        listModel.addElement(d);
                    }
                }
            }
        });

        // Bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        JButton btnSelect = ServerGuiUtils.createStyledButton("Chọn", new Color(0, 120, 215), Color.WHITE);
        JButton btnCancel = ServerGuiUtils.createStyledButton("Hủy", new Color(108, 117, 125), Color.WHITE);
        btnPanel.add(btnSelect);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        // Double-click to select
        disguiseList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    applyDisguise(disguiseList.getSelectedValue(), dialog);
                }
            }
        });

        btnSelect.addActionListener(e -> applyDisguise(disguiseList.getSelectedValue(), dialog));
        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void applyDisguise(DisguiseItem disguise, JDialog dialog) {
        if (disguise == null) {
            JOptionPane.showMessageDialog(dialog, "Hãy chọn một cải trang!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        txtHead.setText(String.valueOf(disguise.head));
        txtBody.setText(String.valueOf(disguise.body));
        txtLeg.setText(String.valueOf(disguise.leg));
        lblDisguisePreview.setText(disguise.name + " (ID:" + disguise.id + ")");
        if (disguise.icon != null) {
            lblDisguisePreview.setIcon(disguise.icon);
        } else {
            lblDisguisePreview.setIcon(null);
        }
        dialog.dispose();
    }

    // ============================================
    // CRUD OPERATIONS
    // ============================================

    private void saveCurrentMercenary() {
        if (selectedMerc == null) {
            JOptionPane.showMessageDialog(this, "Chọn một lính đánh thuê trước!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Read form
            selectedMerc.name = txtName.getText().trim();
            selectedMerc.planetType = cbPlanet.getSelectedIndex();
            selectedMerc.gender = cbGender.getSelectedIndex();
            selectedMerc.canAttackBoss = chkCanAttackBoss.isSelected();
            selectedMerc.active = chkActive.isSelected();
            selectedMerc.hp = Long.parseLong(txtHp.getText().trim());
            selectedMerc.mp = Long.parseLong(txtMp.getText().trim());
            selectedMerc.dame = Long.parseLong(txtDame.getText().trim());
            selectedMerc.def = Integer.parseInt(txtDef.getText().trim());
            selectedMerc.crit = Integer.parseInt(txtCrit.getText().trim());
            selectedMerc.price30Min = Integer.parseInt(txtPrice30.getText().trim());
            selectedMerc.price1Hour = Integer.parseInt(txtPrice1h.getText().trim());
            selectedMerc.price5Hour = Integer.parseInt(txtPrice5h.getText().trim());
            selectedMerc.head = Integer.parseInt(txtHead.getText().trim());
            selectedMerc.body = Integer.parseInt(txtBody.getText().trim());
            selectedMerc.leg = Integer.parseInt(txtLeg.getText().trim());

            // Validate
            if (selectedMerc.name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save to DB
            String sql = "UPDATE mercenary_template SET name=?, planet_type=?, price_30min=?, price_1hour=?, " +
                    "price_5hour=?, hp=?, mp=?, dame=?, def=?, crit=?, head=?, body=?, leg=?, gender=?, " +
                    "can_attack_boss=?, active=? WHERE id=?";
            try (Connection con = DBConnecter.getConnectionServer();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, selectedMerc.name);
                ps.setInt(2, selectedMerc.planetType);
                ps.setInt(3, selectedMerc.price30Min);
                ps.setInt(4, selectedMerc.price1Hour);
                ps.setInt(5, selectedMerc.price5Hour);
                ps.setLong(6, selectedMerc.hp);
                ps.setLong(7, selectedMerc.mp);
                ps.setLong(8, selectedMerc.dame);
                ps.setInt(9, selectedMerc.def);
                ps.setInt(10, selectedMerc.crit);
                ps.setInt(11, selectedMerc.head);
                ps.setInt(12, selectedMerc.body);
                ps.setInt(13, selectedMerc.leg);
                ps.setInt(14, selectedMerc.gender);
                ps.setBoolean(15, selectedMerc.canAttackBoss);
                ps.setBoolean(16, selectedMerc.active);
                ps.setInt(17, selectedMerc.id);
                ps.executeUpdate();
            }

            // Refresh table
            refreshTable();

            // Re-select
            for (int i = 0; i < mercenaries.size(); i++) {
                if (mercenaries.get(i).id == selectedMerc.id) {
                    mercTable.setRowSelectionInterval(i, i);
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, "Đã lưu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ! Kiểm tra lại các trường số.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewMercenary() {
        String name = JOptionPane.showInputDialog(this, "Nhập tên lính đánh thuê mới:", "Thêm mới",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty())
            return;

        try {
            // Find next ID
            int nextId = 1;
            for (MercenaryData m : mercenaries) {
                if (m.id >= nextId)
                    nextId = m.id + 1;
            }

            String sql = "INSERT INTO mercenary_template (id, name, planet_type, price_30min, price_1hour, price_5hour, "
                    +
                    "hp, mp, dame, def, crit, head, body, leg, gender, can_attack_boss, active) " +
                    "VALUES (?, ?, 0, 1, 2, 5, 1000000, 500000, 100000, 1000, 10, 285, 286, 287, 0, 0, 1)";
            try (Connection con = DBConnecter.getConnectionServer();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, nextId);
                ps.setString(2, name.trim());
                ps.executeUpdate();
            }

            loadData();
            JOptionPane.showMessageDialog(this, "Đã thêm lính đánh thuê \"" + name.trim() + "\" (ID: " + nextId + ")",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedMercenary() {
        if (selectedMerc == null) {
            JOptionPane.showMessageDialog(this, "Chọn một lính đánh thuê trước!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa \"" + selectedMerc.name + "\" (ID: " + selectedMerc.id + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            String sql = "DELETE FROM mercenary_template WHERE id=?";
            try (Connection con = DBConnecter.getConnectionServer();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, selectedMerc.id);
                ps.executeUpdate();
            }

            loadData();
            JOptionPane.showMessageDialog(this, "Đã xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================
    // UTILITY
    // ============================================

    private String formatNumber(long num) {
        if (num >= 1_000_000_000)
            return String.format("%.1fB", num / 1_000_000_000.0);
        if (num >= 1_000_000)
            return String.format("%.1fM", num / 1_000_000.0);
        if (num >= 1_000)
            return String.format("%.1fK", num / 1_000.0);
        return String.valueOf(num);
    }

    // ============================================
    // DISGUISE LIST RENDERER
    // ============================================

    private static class DisguiseCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof DisguiseItem d) {
                String genderStr = switch (d.gender) {
                    case 0 -> "TĐ";
                    case 1 -> "XD";
                    case 2 -> "NM";
                    default -> "All";
                };
                lbl.setText(String.format("[%d] %s  (%s)  H:%d B:%d L:%d",
                        d.id, d.name, genderStr, d.head, d.body, d.leg));
                if (d.icon != null) {
                    lbl.setIcon(d.icon);
                } else {
                    lbl.setIcon(null);
                }
                lbl.setIconTextGap(10);
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            return lbl;
        }
    }
}
