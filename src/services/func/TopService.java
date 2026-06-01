package services.func;

import Top.TopPowerManager;
import Top.TopTaskManager;
import consts.ConstSQL;

import java.io.IOException;

import jdbc.DBConnecter;
import player.Player;
import server.Manager;
import network.Message;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import jdbc.NDVDB;
import jdbc.daos.NDVSqlFetcher;

import matches.TOP;
import services.ItemService;
import services.TaskService;
import utils.Util;

public class TopService {

    private static TopService instance;

    public static TopService gI() {
        if (instance == null) {
            instance = new TopService();
        }
        return instance;
    }

    public void updateTop() {
        if (Manager.timeRealTop + (2 * 60 * 1000) < System.currentTimeMillis()) {
            Manager.timeRealTop = System.currentTimeMillis();
            try (Connection con = DBConnecter.getConnectionServer()) {
                Manager.topNV = Manager.realTop(ConstSQL.TOP_NV, con);
//                Manager.topDC = Manager.realTop(ConstSQL.TOP_DC, con);
                Manager.topVDST = Manager.realTop(ConstSQL.TOP_VDST, con);
                Manager.topWHIS = Manager.realTop(ConstSQL.TOP_WHIS, con);
                Manager.topSM = Manager.realTop(ConstSQL.TOP_SM, con);
                Manager.topNap = Manager.realTop(ConstSQL.TOP_NAP, con);
                Manager.topSSM = Manager.realTop(ConstSQL.TOP_SO_SU_MENH, con);
                Manager.topTet = Manager.realTop(ConstSQL.TOP_TET, con);
                Manager.topSD= Manager.realTop(ConstSQL.TOP_SD, con);
//                Manager.topHP = Manager.realTop(ConstSQL.TOP_HP, con);
//                Manager.topKI = Manager.realTop(ConstSQL.TOP_KI, con);
                Manager.topbossday = Manager.realTop(ConstSQL.TOP_BOSS_DAY, con);
//                Manager.topbang = Manager.realTop(ConstSQL.TOP_BANG, con);
//                Manager.topDuaSM = Manager.realTop(ConstSQL.TOP_DUA_SM, con);
//                Manager.topDuaNap = Manager.realTop(ConstSQL.TOP_DUA_NAP, con);
            } catch (Exception ignored) {
                Logger.error("Lỗi đọc top");
            }
        }
    }

    public static void showListTopPower(Player player) {
        TopPowerManager.getInstance().load();
        List<Player> list = TopPowerManager.getInstance().getList();
        list.sort((p1, p2) -> Long.compare(p2.nPoint.power, p1.nPoint.power));
        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 10");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < 10; i++) {
                Player top = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt(i + 1);
                msg.writer().writeShort(top.getHead());
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(top.getBody());
                msg.writer().writeShort(top.getLeg());
                msg.writer().writeUTF(top.name);
                msg.writer().writeUTF("Sức mạnh: " + Util.numberFormatLouis(top.nPoint.power));
                msg.writer().writeUTF("...");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void showListTopTask(Player player) {
        TopTaskManager.getInstance().load();
        List<Player> list = TopTaskManager.getInstance().getList();
        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100");
            msg.writer().writeByte(list.size());
            for (int i = 0; i < list.size(); i++) {
                Player top = list.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt(i + 1);
                msg.writer().writeShort(top.getHead());

                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(top.getBody());
                msg.writer().writeShort(top.getLeg());
                msg.writer().writeUTF(top.name);
                msg.writer().writeUTF(NDVSqlFetcher.loadById(top.id).playerTask.taskMain.name);
                msg.writer().writeUTF("...");
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static String getTopNap() {
        StringBuffer sb = new StringBuffer("");
        PreparedStatement ps;
        ResultSet rs;
        try {
            Connection conn = DBConnecter.getConnectionServer();
            ps = conn.prepareStatement(ConstSQL.TOP_DUA_NAP);
            conn.setAutoCommit(false);

            rs = ps.executeQuery();
            byte i = 1;
            while (rs.next()) {
                sb.append(i).append(".").append(rs.getString("name")).append(": ").append(rs.getString("danap")).append(" Đã Nạp\b");
                i++;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

   public static String getTopSM() {
    StringBuilder sb = new StringBuilder();
    PreparedStatement ps = null;
    ResultSet rs = null;
    Connection conn = null;

    try {
        conn = DBConnecter.getConnectionServer();
        ps = conn.prepareStatement(ConstSQL.TOP_DUA_SM);
        conn.setAutoCommit(false);

        rs = ps.executeQuery();
        byte i = 1;
        while (rs.next()) {
            sb.append(i).append(". ").append(rs.getString("name"))
              .append(": ").append(rs.getString("sm"))
              .append(" Sức Mạnh\n"); // Sửa `\b` thành `\n`
            i++;
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        // Đảm bảo đóng tất cả tài nguyên
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    return sb.toString();
}

public static void showListTopBang(Player player) {
    List<String> topBangList = getTopBangList(); // Lấy danh sách từ CSDL
    Message msg = null;
    
    try {
        msg = new Message(-96); // Tạo message với opcode -96
        msg.writer().writeByte(1); // ID danh sách Top Bang
        msg.writer().writeUTF("Top Bang");
        msg.writer().writeByte(topBangList.size()); // Số lượng bang hội
        
        for (int i = 0; i < topBangList.size(); i++) {
            String[] data = topBangList.get(i).split("\\|"); // Tách dữ liệu
            String name = data[0];
            int powerPoint = Integer.parseInt(data[1]);

            msg.writer().writeInt(i + 1); // Xếp hạng
            msg.writer().writeUTF(name); // Tên Bang
            msg.writer().writeUTF("Power: " + powerPoint); // Điểm sức mạnh
        }

        player.sendMessage(msg); // Gửi danh sách đến người chơi
        msg.cleanup();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (msg != null) {
            msg.cleanup();
        }
    }
}
public static List<String> getTopBangList() {
    List<String> list = new ArrayList<>();
    PreparedStatement ps;
    ResultSet rs;
    try {
        Connection conn = DBConnecter.getConnectionServer();
        ps = conn.prepareStatement(ConstSQL.TOP_BANG);
        rs = ps.executeQuery();

        while (rs.next()) {
            String name = rs.getString("NAME");
            int powerPoint = rs.getInt("power_point");
            list.add(name + "|" + powerPoint);
        }
        
        rs.close();
        ps.close();
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}

    public static String getTopQuocVuong() {
        StringBuffer sb = new StringBuffer("");
        PreparedStatement ps;
        ResultSet rs;
        try {
            Connection conn = DBConnecter.getConnectionServer();
            ps = conn.prepareStatement(ConstSQL.TOP_DUA_QUOC_VUONG);
            conn.setAutoCommit(false);
            rs = ps.executeQuery();
            byte i = 1;
            while (rs.next()) {
                int id = rs.getInt("accountId");
                String username = rs.getString("name");
                sb.append(i).append(".").append(id).append("-").append(username).append(": sở hữu ").append(rs.getString("thoi_vang")).append(" ").append(ItemService.gI().getTemplate(consts.ConstTranhNgocNamek.ITEM_TRANH_NGOC).name).append("\b");
                i++;
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static void showListTop(Player player, int select) {
        List<TOP> tops = Manager.topNV;
        switch (select) {
            case 0 ->
                tops = Manager.topNV;
            case 1 ->
                tops = Manager.topDC;
            case 2 ->
                tops = Manager.topSM;
            case 3 ->
                tops = Manager.topWHIS;
            case 4 ->
                tops = Manager.topNap;
            case 5 ->
                tops = Manager.topVDST;
            case 7 ->
                tops = Manager.topSSM;
//            case 6 ->
//                tops = Manager.topDuaNap;
            case 8 ->
                tops = Manager.topSD;
            case 9 ->
                tops = Manager.topTet;
            case 10 ->
                tops = Manager.topbossday;
            case 11 ->
                tops = Manager.topHP;
            case 12 ->
                tops = Manager.topKI;
            

        }
        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 10");
            msg.writer().writeByte(tops.size());
            for (int i = 0; i < tops.size(); i++) {
                TOP top = tops.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt(i + 1);
                msg.writer().writeShort(top.getHead());
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(top.getBody());
                msg.writer().writeShort(top.getLeg());
                msg.writer().writeUTF(top.getName());
                switch (select) {
                    case 0 -> {
                        msg.writer().writeUTF(TaskService.gI().getTaskMainById(player, top.getNv()).name.substring(0, TaskService.gI().getTaskMainById(player, top.getNv()).name.length() > 20 ? 20 : TaskService.gI().getTaskMainById(player, top.getNv()).name.length()) + "...");
                        msg.writer().writeUTF(TaskService.gI().getTaskMainById(player, top.getNv()).subTasks.get(top.getSubnv()).name + " - " + getTimeLeft(top.getLasttime()));
                    }
                    case 1 -> {
                        msg.writer().writeUTF("Chơi đồ " + top.getDicanh() + " lần");
                        msg.writer().writeUTF("Gia nhập juventus " + top.getJuventus() + " lần");
                    }
                    case 2 -> {
                        msg.writer().writeUTF("" + Util.numberToMoney(top.getPower()) + " Sức mạnh");
                        msg.writer().writeUTF("" + top.getPower() + " Sức mạnh");
                    }
                    case 3 -> {
                        msg.writer().writeUTF("LV:" + top.getLevel() + " với " + Util.roundToTwoDecimals(top.getTime() / 1000d) + " giây");
                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                    }
                    case 4 -> {
                        msg.writer().writeUTF("" + Util.numberToMoney(top.getCash()) + " VNĐ");
                        msg.writer().writeUTF("" + top.getCash() + " VNĐ");
                    }
                    case 5 -> {
                        msg.writer().writeUTF("Đã thử thách " + top.getDivdst() + " Lần");
                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                    }
//                    case 6 -> {
//                        msg.writer().writeUTF("" + Util.numberToMoney(top.getCash()) + " VNĐ");
//                        msg.writer().writeUTF("" + top.getCash() + " VNĐ");
//                    }
                    case 7 -> {
                        msg.writer().writeUTF("Số điểm sổ sứ mệnh" + top.getDiemsm());
//                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                        msg.writer().writeUTF("" + top.getDiemsm() + " Điểm");
                    }
                    case 8 -> {
                        msg.writer().writeUTF("Sức Đánh : " + top.getSd());
//                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                        msg.writer().writeUTF("" + top.getSd() + "  SĐ");
                    }
                    case 9 -> {
                        msg.writer().writeUTF("Điểm SB" + top.getDiemtet());
//                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                        msg.writer().writeUTF("" + top.getDiemtet() + " Điểm");
                    }
                    case 10 -> {
                        msg.writer().writeUTF("Điểm boss Ngày:  " + top.getBossday());
//                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                        msg.writer().writeUTF("" + top.getBossday() + "  điểm");
                    }
                    case 11 -> {
                        msg.writer().writeUTF("HP:  " + top.getHp());
//                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                        msg.writer().writeUTF("" + top.getHp() + " HP");
                    }
                    case 12 -> {
                        msg.writer().writeUTF("KI:  " + top.getKi());
//                        msg.writer().writeUTF(getTimeLeft(top.getLasttime()));
                        msg.writer().writeUTF("" + top.getKi() + " KI");
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static String getTimeLeft(long lastTime) {
        int secondsPassed = (int) ((System.currentTimeMillis() - lastTime) / 1000);

        if (secondsPassed > 86400) {
            return (secondsPassed / 86400) + " ngày trước";
        } else if (secondsPassed > 3600) {
            return (secondsPassed / 3600) + " giờ trước";
        } else if (secondsPassed > 60) {
            return (secondsPassed / 60) + " phút trước";
        } else {
            return secondsPassed + " giây trước";
        }
    }

}
