package server;

import linhManager.EmtiManager;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import jdbc.daos.EventDAO;
import models.Consign.ConsignShopManager;
import network.SessionManager;
import services.ClanService;
import utils.Logger;

public class ServerManagerUI_Old extends JFrame {

    private Preferences preferences;
    private JLabel ssCountLabel;
    private JLabel plCountLabel;
    private JLabel threadCountLabel;
    private JLabel messageLabel;
    private JLabel countdownLabel;
    private JLabel info;
    private JTextField minutesField;
    private JTextField expRateField;
    private Timer countdownTimer;
    private int remainingSeconds;

    public ServerManagerUI_Old() {
        preferences = Preferences.userNodeForPackage(ServerManagerUI_Old.class);
        setTitle("Server Managerment");
        setSize(360, 220);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        initUI();
        startMonitoring();
        ServerManager.gI().run();
        EmtiManager.getInstance().startAutoSave();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        getContentPane().add(panel);

        JButton maintenanceButton = new JButton("Bảo trì");
        maintenanceButton.addActionListener(e -> showMaintenanceDialog());
        panel.add(maintenanceButton);

        panel.add(new JLabel("Cài đặt giờ bảo trì"));

        try (BufferedReader reader = new BufferedReader(new FileReader("maintenanceConfig.txt"))) {
            int hours = Integer.parseInt(reader.readLine());
            int minutes = Integer.parseInt(reader.readLine());
            int seconds = Integer.parseInt(reader.readLine());

            JComboBox<Integer> hoursComboBox = createTimeComboBox(-1, 23, hours);
            JComboBox<Integer> minutesComboBox = createTimeComboBox(-1, 59, minutes);
            JComboBox<Integer> secondsComboBox = createTimeComboBox(-1, 59, seconds);

            panel.add(hoursComboBox);
            panel.add(minutesComboBox);
            panel.add(secondsComboBox);

            JButton scheduleBtn = new JButton("Hẹn giờ bảo trì");
            scheduleBtn.addActionListener(e -> scheduleMaintenance(hoursComboBox, minutesComboBox, secondsComboBox));
            panel.add(scheduleBtn);

            /*if (hours != -1 && minutes != -1 && seconds != -1) {
                scheduleMaintenance(hoursComboBox, minutesComboBox, secondsComboBox);
            }*/

        } catch (IOException e) {
        }

        JButton saveButton = new JButton("Lưu Data");
        saveButton.addActionListener(e -> saveAllDataAndExit());
        panel.add(saveButton);

        JButton clearFw = new JButton("clearFw");
        clearFw.addActionListener(e -> {
            network.server.EMTIServer.firewall.clear();
            network.server.EMTIServer.firewallDownDataGame.clear();
            JOptionPane.showMessageDialog(this, "Đã clear firewall");
        });
        panel.add(clearFw);

        panel.add(new JLabel("EXP Rate (1-100):"));
        expRateField = new JTextField(String.valueOf(Manager.RATE_EXP_SERVER), 5);
        panel.add(expRateField);

        JButton saveExpRateBtn = new JButton("Save EXP Rate");
        saveExpRateBtn.addActionListener(e -> saveExpRate());
        panel.add(saveExpRateBtn);

        messageLabel = new JLabel();
        panel.add(messageLabel);

        countdownLabel = new JLabel();
        panel.add(countdownLabel);

        info = new JLabel();
        panel.add(info);

        threadCountLabel = new JLabel("Số Thread: ");
        panel.add(threadCountLabel);

        plCountLabel = new JLabel("Online: ");
        panel.add(plCountLabel);

        ssCountLabel = new JLabel("Session: ");
        panel.add(ssCountLabel);

        messageLabel.setText("Server đang chạy");
        setVisible(true);
    }

    private JComboBox<Integer> createTimeComboBox(int start, int end, int selectedValue) {
        DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>();
        for (int i = start; i <= end; i++) model.addElement(i);
        JComboBox<Integer> comboBox = new JComboBox<>(model);
        comboBox.setSelectedItem(selectedValue);
        return comboBox;
    }

    private void saveExpRate() {
        try {
            int newRate = Integer.parseInt(expRateField.getText().trim());
            if (newRate < 1 || newRate > 100) {
                JOptionPane.showMessageDialog(this, "Giá trị EXP phải từ 1 đến 100!");
                return;
            }
            Manager.RATE_EXP_SERVER = (byte) newRate;
            JOptionPane.showMessageDialog(this, "Cập nhật EXP thành công: " + newRate + "x");
            Logger.success("EXP rate set to " + newRate);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
        }
    }

    private void scheduleMaintenance(JComboBox<Integer> hoursBox, JComboBox<Integer> minutesBox, JComboBox<Integer> secondsBox) {
        int h = (int) hoursBox.getSelectedItem();
        int m = (int) minutesBox.getSelectedItem();
        int s = (int) secondsBox.getSelectedItem();
        if (h == -1 || m == -1 || s == -1) {
            JOptionPane.showMessageDialog(this, "Chạy server không cần hẹn bảo trì?");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("maintenanceConfig.txt"))) {
            writer.write(h + "\n" + m + "\n" + s + "\n");
        } catch (IOException e) {
        }

        info.setText("Đã cài đặt bảo trì lúc " + h + ":" + m + ":" + s);
        AtomicBoolean triggered = new AtomicBoolean(false);

        new Thread(() -> {
            while (!triggered.get()) {
                try {
                    LocalTime now = LocalTime.now();
                    if (now.getHour() == h && now.getMinute() == m && now.getSecond() == s) {
                        performMaintenance();
                        triggered.set(true);
                    }
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                }
            }
        }).start();
    }

    private void performMaintenance() {
        Maintenance.gI().start(15);
    }

    private void showMaintenanceDialog() {
        int result = JOptionPane.showConfirmDialog(this, "Bắt đầu bảo trì?", "Bảo trì", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            Logger.error("Server tiến hành bảo trì");
            Maintenance.gI().start(15);
        }
    }

    private void confirmExit() {
        int result = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát chương trình?", "Thoát", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void saveAllDataAndExit() {
        Logger.success("Đang lưu data...");
        network.server.EMTIServer.gI().stopConnect();
        Maintenance.isRunning = false;

        try {
            ClanService.gI().close();
            Thread.sleep(1000);
            Logger.success("Lưu dữ liệu bang hội thành công");
        } catch (Exception e) {
            Logger.error("Lỗi lưu dữ liệu bang hội");
        }

        try {
            ConsignShopManager.gI().save();
            Thread.sleep(1000);
            Logger.success("Lưu dữ liệu ký gửi thành công");
        } catch (Exception e) {
            Logger.error("Lỗi lưu dữ liệu ký gửi");
        }

        try {
            Client.gI().close();
            EventDAO.save();
            Thread.sleep(1000);
            Logger.success("Lưu dữ liệu người dùng thành công");
        } catch (Exception e) {
            Logger.error("Lỗi lưu dữ liệu người dùng");
        }

        System.exit(0);
    }

    private void startMonitoring() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            threadCountLabel.setText("Số thread: " + Thread.activeCount());
        }, 1, 1, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            plCountLabel.setText("Online: " + Client.gI().getPlayers().size());
        }, 5, 1, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            ssCountLabel.setText("Session: " + SessionManager.gI().getSessions().size());
        }, 5, 1, TimeUnit.SECONDS);
    }
}
