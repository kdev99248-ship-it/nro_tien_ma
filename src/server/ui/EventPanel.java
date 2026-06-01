package server.ui;

import utils.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.border.CompoundBorder;

public class EventPanel extends JPanel {

    private JTextArea txtHistory;
    private final List<JCheckBox> checkBoxes = new ArrayList<>();

    // Danh sách tên sự kiện
    private static final String[] EVENT_NAMES = {
            "1. Halloween", "2. 8/3 Quốc Tế PN", "3. Giáng Sinh", "4. Tết Nguyên Đán",
            "5. Trung Thu", "6. Giỗ Tổ", "7. Top Up (Mặc định)", "8. Pokemon",
            "9. 20/11", "10. Phở Anh Hai"
    };

    public EventPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        // --- 1. Checkbox Panel (Khu vực chọn sự kiện) ---
        JPanel checkPanel = new JPanel(new GridLayout(0, 2, 15, 10));
        checkPanel.setOpaque(false);

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                " Chọn sự kiện chạy song song ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(0, 102, 204));
        checkPanel.setBorder(new CompoundBorder(titledBorder, new EmptyBorder(15, 15, 15, 15)));

        List<Integer> savedIds = loadEventConfig();

        for (int i = 0; i < EVENT_NAMES.length; i++) {
            JCheckBox chk = new JCheckBox(EVENT_NAMES[i]);
            chk.setOpaque(false);
            chk.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (savedIds.contains(i + 1))
                chk.setSelected(true);
            chk.putClientProperty("id", i + 1);
            checkBoxes.add(chk);
            checkPanel.add(chk);
        }

        // --- 2. Button & Hint Panel ---
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);

        JLabel lblHint = new JLabel("(*Lưu ý: Hệ thống sẽ tự động Restart sau 10 giây sau khi lưu thành công)");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(220, 53, 69));
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblHint.setBorder(new EmptyBorder(0, 0, 10, 0));

        JButton btnApply = ServerGuiUtils.createStyledButton("LƯU CẤU HÌNH & KÍCH HOẠT NGAY", new Color(0, 123, 255),
                Color.WHITE);
        btnApply.setPreferredSize(new Dimension(300, 50));
        btnApply.setMaximumSize(new Dimension(500, 50));
        btnApply.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnApply.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnApply.addActionListener(e -> {
            List<Integer> selected = checkBoxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(chk -> (int) chk.getClientProperty("id"))
                    .collect(Collectors.toList());

            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 sự kiện!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Xác nhận lưu thay đổi?\nServer sẽ được khởi động lại sau 10 giây.",
                        "Xác nhận hệ thống", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    doiSuKienVaRestart(selected);
                }
            }
        });

        actionPanel.add(lblHint);
        actionPanel.add(btnApply);

        // --- 3. Log Area ---
        txtHistory = new JTextArea();
        txtHistory.setEditable(false);
        txtHistory.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtHistory.setBackground(new Color(245, 245, 245));
        txtHistory.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollLog = new JScrollPane(txtHistory);
        TitledBorder logBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                " Lịch sử hoạt động ");
        scrollLog.setBorder(logBorder);
        scrollLog.setPreferredSize(new Dimension(0, 180));

        JPanel bottomContainer = new JPanel(new BorderLayout(0, 20));
        bottomContainer.setOpaque(false);
        bottomContainer.add(actionPanel, BorderLayout.NORTH);
        bottomContainer.add(scrollLog, BorderLayout.CENTER);

        add(checkPanel, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);

        log("Đã tải cấu hình sự kiện hiện tại: " + savedIds.toString());
    }

    public void doiSuKienVaRestart(List<Integer> eventIds) {
        log("Đang tiến hành lưu cấu hình vào active_event.txt...");
        saveEventConfig(eventIds);
        log("Cấu hình đã lưu. Chuẩn bị Restart...");
        triggerCustomRestart();
    }

    private void triggerCustomRestart() {
        int delaySeconds = 10;
        System.out.println(">>> [EventPanel] Requesting restart in " + delaySeconds + "s...");

        try {
            String currentDir = System.getProperty("user.dir");
            String osName = System.getProperty("os.name").toLowerCase();

            ProcessBuilder pb;
            if (osName.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "cmd", "/c",
                        "title Server Restarting... && echo Server se khoi dong lai sau 10s... && timeout /t "
                                + delaySeconds + " /nobreak && run.bat");
            } else {
                pb = new ProcessBuilder("bash", "-c", "sleep " + delaySeconds + "; ./run.sh &");
            }

            pb.directory(new File(currentDir));
            pb.start();

            ServerManagerUI.REQUEST_AUTO_RESTART = false;

            System.out.println(">>> Bye bye! Exiting Java...");
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thực thi Restart: " + e.getMessage());
        }
    }

    private void saveEventConfig(List<Integer> eventIds) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("active_event.txt"))) {
            String line = eventIds.stream().map(String::valueOf).collect(Collectors.joining("-"));
            pw.println(line);
            pw.flush();
        } catch (IOException e) {
            Logger.error("Lỗi lưu file active_event.txt: " + e.getMessage());
        }
    }

    public List<Integer> loadEventConfig() {
        List<Integer> ids = new ArrayList<>();
        File f = new File("active_event.txt");
        if (!f.exists()) {
            ids.add(7);
            return ids;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine();
            if (line != null && !line.isEmpty()) {
                for (String part : line.split("-")) {
                    try {
                        ids.add(Integer.parseInt(part.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
        }
        if (ids.isEmpty())
            ids.add(7);
        return ids;
    }

    private void log(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        txtHistory.append("[" + time + "] " + msg + "\n");
        txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
    }
}
