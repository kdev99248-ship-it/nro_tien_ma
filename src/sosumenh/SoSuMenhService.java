package sosumenh;

import item.Item;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jdbc.DBConnecter;
import jdbc.daos.NDVSqlFetcher;
import lombok.Getter;
import network.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Archivement;
import player.Player;
import services.ItemService;
import services.Service;
import utils.Logger;

/**
 * @author BTH Cute Phô Mai Que
 */
public class SoSuMenhService {

    public void receive(int index, Player player) {
        Archivement achievement = player.archivementList.get(index);

        if (achievement == null) {
            Service.gI().sendThongBao(player, "Không có phần thưởng");
            return;
        }

        if (achievement.isRecieve) {
            Service.gI().sendThongBaoOK(player, "Nhận rồi đừng nhận nữa");
            return;
        }
        sendMessage(player, index);
        achievement.setRecieve(true);
        giveReward(player, index + 1, "items");
        Service.gI().sendThongBao(player, "Nhận thành công, vui lòng kiểm tra hòm thư");
    }

    public void receiveVip(int index, Player player) {
        Archivement achievement = player.archivementList.get(index);

        if (achievement == null) {
            Service.gI().sendThongBao(player, "Không có phần thưởng");
            return;
        }

        if (achievement.isRecieve) {
            Service.gI().sendThongBaoOK(player, "Nhận rồi đừng nhận nữa");
            return;
        }
        sendMessage(player, index);
        achievement.setRecieve(true);
        giveReward(player, index + 1, "items2");
        Service.gI().sendThongBao(player, "Nhận thành công, vui lòng kiểm tra hòm thư");
    }

    private void sendMessage(Player player, int index) {
        Message msg = null;
        try {
            msg = new Message(-76);
            msg.writer().writeByte(1); // Action
            msg.writer().writeByte(index); // Index
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(this.getClass(), e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void giveReward(Player player, int index, String rewardColumn) {
        try ( Connection connection = DBConnecter.getConnectionServer();  PreparedStatement ps = connection.prepareStatement("SELECT * FROM so_su_menh_reward")) {
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int level = rs.getInt("level");
                    if (level == index) {
                        JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString(rewardColumn));
                        for (Object obj : dataArray) {
                            JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                            Item item = createItem(dataObject);
                            player.inventory.itemsMailBox.add(item);
                        }
                        if (NDVSqlFetcher.updateMailBox(player)) {
                            Service.gI().sendThongBao(player, "Bạn vừa nhận quà về mail thành công");
                            if (rewardColumn.equals("items")) {
                                player.sosumenhplayer.reward[level - 1] = true;
                            } else {
                                player.sosumenhplayer.rewardVip[level - 1] = true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Item createItem(JSONObject dataObject) {
        int tempId = Integer.parseInt(dataObject.get("temp_id").toString());
        int quantity = Integer.parseInt(dataObject.get("quantity").toString());
        Item item = ItemService.gI().createNewItem((short) tempId);
        item.quantity = quantity;

        JSONArray optionsArray = (JSONArray) dataObject.get("options");
        for (Object optionObj : optionsArray) {
            JSONObject optionObject = (JSONObject) optionObj;
            int param = Integer.parseInt(optionObject.get("param").toString());
            int optionId = Integer.parseInt(optionObject.get("id").toString());
            item.itemOptions.add(new Item.ItemOption(optionId, param));
        }
        return item;
    }

    public void loadAchievements(Player player, boolean isVip) {
        if (player.getSession() == null) {
            return;
        }
        player.archivementList.clear();
        for (int i = 0; i < 20; i++) {
            Archivement achievement = new Archivement();
            achievement.setInfo1("Cấp sổ: " + (i + 1));
            achievement.setInfo2("Cấp hiện tại: " + player.sosumenhplayer.getLevel() + "/" + (i + 1));
            achievement.setFinish(player.sosumenhplayer.getLevel() >= (i + 1));
            if (isVip) {
                achievement.setFinish(player.sosumenhplayer.getLevel() >= (i + 1) && isVip == player.sosumenhplayer.isVip());
            }
            achievement.setMoney((short) 0);
            achievement.setRecieve(isVip ? player.sosumenhplayer.rewardVip[i] : player.sosumenhplayer.reward[i]);
            player.archivementList.add(achievement);
        }
        show(player, isVip ? 4 : 3);
    }

    public void show(Player player, int type) {
        Message msg = null;
        try {
            msg = new Message(-76);
            msg.writer().writeByte(0); // Action
            msg.writer().writeByte(player.archivementList.size());

            for (Archivement achievement : player.archivementList) {
                msg.writer().writeUTF(achievement.getInfo1());
                msg.writer().writeUTF(achievement.getInfo2());
                msg.writer().writeShort(achievement.getMoney());
                msg.writer().writeBoolean(achievement.isFinish);
                msg.writer().writeBoolean(achievement.isRecieve);
            }

            player.sendMessage(msg);
            player.typeRecvieArchiment = type;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
    @Getter
    private static final SoSuMenhService instance = new SoSuMenhService();
}
