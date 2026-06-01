package npc.npc_manifest;

import clan.Clan;
import consts.ConstNpc;
import consts.mocnap;
import item.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdbc.DBConnecter;
import jdbc.daos.NDVSqlFetcher;
import models.TreasureUnderSea.TreasureUnderSea;
import models.TreasureUnderSea.TreasureUnderSeaService;
import npc.Npc;
import static npc.NpcFactory.PLAYERID_OBJECT;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Archivement;
import player.Player;
import player.LinhDanhThue;
import player.mercenary.MercenaryManager;
import player.mercenary.MercenaryTemplate;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.RewardService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import shop.ShopService;
import utils.Util;

public class QuyLaoKame extends Npc {

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
            menu.add("Nói\nchuyện");
            menu.add("Học Skill\nnăng mới");
            menu.add("Quà Mốc Nạp");
            menu.add("Hộp Thư");
            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward) {
                RewardService.gI().rewardLancon(player);
                return;
            }
            switch (player.iDMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                            ArrayList<String> menu = new ArrayList<>();
                            menu.add("Nhiệm vụ");
                            menu.add("Học\nKỹ năng");
                            Clan clan = player.clan;
                            if (clan != null) {
                                menu.add("Về khu\nvực bang");
                                if (clan.isLeader(player)) {
                                    menu.add("Giải tán\nBang hội");
                                }
                            }
                            menu.add("Kho báu\ndưới biển");
                            menu.add("Thuê lính");
                            String[] menus = menu.toArray(String[]::new);

                            this.createOtherMenu(player, 0,
                                    "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menus);
                        }
                        case 1 -> {
                            Service.gI().sendThongBao(player, "Chưa mở");
                        }
                        case 2 -> {
                            this.createOtherMenu(player, 1115, "Nạp đạt mốc nhận quà he :3", "Xem quà mốc nạp",
                                    "Nhận quà mốc nạp", "Đóng");
                        }
                        case 3 -> {
                            this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                    "|0|Tình yêu như một dây đàn\n"
                                            + "Tình vừa được thì đàn đứt dây\n"
                                            + "Đứt dây này anh thay dây khác\n"
                                            + "Mất em rồi anh biết thay ai?",
                                    "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                            - InventoryService.gI()
                                                    .getCountEmptyListItem(player.inventory.itemsMailBox))
                                            + " món)",
                                    "Xóa Hết\nHòm Thư", "Đóng");
                        }
                    }
                }
                case ConstNpc.MAIL_BOX -> {
                    switch (select) {
                        case 0:
                            ShopService.gI().opendShop(player, "ITEMS_MAIL_BOX", true);
                            break;
                        case 1:
                            NpcService.gI().createMenuConMeo(player,
                                    ConstNpc.CONFIRM_REMOVE_ALL_ITEM_MAIL_BOX, this.avartar,
                                    "|3|Bạn chắc muốn xóa hết vật phẩm trong hòm thư?\n"
                                            + "|7|Sau khi xóa sẽ không thể khôi phục!",
                                    "Đồng ý", "Hủy bỏ");
                            break;
                        case 2:
                            break;
                    }
                }
                case 1115 -> {
                    switch (select) {
                        case 0:
                            JSONArray dataArray;
                            JSONObject dataObject;
                            PreparedStatement ps = null;
                            ResultSet rs = null;
                            StringBuilder sb = new StringBuilder();
                            sb.append("|0|꧁__Nạp tích lũy để nhận quà theo mốc_꧂\n");
                            try (Connection con2 = DBConnecter.getConnectionServer()) {
                                ps = con2.prepareStatement("SELECT * FROM moc_nap");
                                rs = ps.executeQuery();

                                while (rs.next()) {
                                    dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                                    sb.append("◥_____________________◤\n|7|");
                                    sb.append("✎▶Mốc Nạp ").append(Archivement.GIADOLACHIADOI[rs.getInt("id") - 1])
                                            .append("◀\n|0|");
                                    sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");

                                    for (int i = 0; i < dataArray.size(); i++) {
                                        dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                                        int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                                        int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                                        JSONArray optionsArray = (JSONArray) dataObject.get("options");

                                        sb.append("▷ x").append(quantity).append(" ")
                                                .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");

                                        if (optionsArray != null) {
                                            for (int j = 0; j < optionsArray.size(); j++) {
                                                JSONObject optionObject = (JSONObject) optionsArray.get(j);
                                                int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                                                int param = Integer.parseInt(String.valueOf(optionObject.get("param")));

                                                String optionTemplateName = ItemService.gI()
                                                        .getItemOptionTemplate(optionId).name;
                                                String formattedOption = optionTemplateName.replace("#",
                                                        String.valueOf(param));

                                                sb.append(formattedOption).append("\n");
                                            }
                                        }
                                        sb.append("\n|0|");
                                    }
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(QuyLaoKame.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Service.gI().sendThongBaoFromAdmin(player, sb.toString());
                            break;
                        case 1:
                            if (player.getSession().actived) {
                                Archivement.gI().getAchievement(player);
                            } else {
                                Service.gI().sendThongBao(player,
                                        "Mở thành viên tại King Kong đi rồi qua đây nhận nhe baby!");
                            }
                            break;
                        case 2:
                            break;
                    }
                }
                case 0 -> {
                    // Logic xác định menu item dựa trên thứ tự lúc tạo menu
                    int menuIndex = 0;

                    // 0. Nhiệm vụ
                    if (select == menuIndex++) {
                        NpcService.gI().createTutorial(player, tempId, avartar,
                                player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                        return;
                    }

                    // 1. Học Kỹ năng
                    if (select == menuIndex++) {
                        Service.gI().sendThongBao(player, "Bạn đã học hết các kỹ năng");
                        return;
                    }

                    Clan clan = player.clan;
                    if (clan != null) {
                        // 2. Về khu vực bang (nếu có bang)
                        if (select == menuIndex++) {
                            if (player.nPoint.power <= 100_000_000_000L) {
                                Service.gI().sendThongBao(player, "Yêu cầu sức mạnh đạt 100 tỉ");
                                return;
                            }
                            ChangeMapService.gI().changeMapNonSpaceship(player, 156, Util.nextInt(392, 400), 192);
                            return;
                        }

                        // 3. Giải tán bang (nếu là leader)
                        if (clan.isLeader(player)) {
                            if (select == menuIndex++) {
                                createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý",
                                        "Từ chối");
                                return;
                            }
                        }
                    }

                    // Kho báu dưới biển
                    if (select == menuIndex++) {
                        if (player.clan != null && player.clan.BanDoKhoBau != null) {
                            this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                    "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                    "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                        } else {
                            this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                    "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                    "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                        }
                        return;
                    }

                    // Thuê lính
                    if (select == menuIndex++) {
                        this.createOtherMenu(player, ConstNpc.MENU_MERCENARY,
                                "Em trai muốn thuê loại lính đánh thuê nào?"
                                        + "\nHướng dẫn:\nChat 'tan cong' để lính đánh\nChat 'stop' để lính dừng",
                                "Lính \nSăn Boss", "Lính\nThường");
                        return;
                    }
                }
                case 3 -> {
                    Clan clan = player.clan;
                    if (clan != null) {
                        if (clan.isLeader(player)) {
                            if (select == 0) {
                                Input.gI().createFormGiaiTanBangHoi(player);
                            }
                        }
                    }
                }
                case ConstNpc.MENU_OPENED_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                ChangeMapService.gI().goToDBKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }
                    }
                }
                case ConstNpc.MENU_OPEN_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                Input.gI().createFormChooseLevelBDKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }
                    }
                }
                case ConstNpc.MENU_ACCEPT_GO_TO_BDKB -> {
                    switch (select) {
                        case 0 ->
                            TreasureUnderSeaService.gI().openBanDoKhoBau(player,
                                    Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                    }
                }
                case ConstNpc.MENU_MERCENARY -> {
                    // select: 0 = Lính đánh Boss, 1 = Lính thường
                    boolean isBossHunter = (select == 0);

                    // Lưu lựa chọn (1: Boss Hunter, 0: Normal)
                    player.iDMark.setMercenaryId(isBossHunter ? 1 : 0);

                    // Lấy template đại diện để hiển thị giá
                    MercenaryTemplate temp = MercenaryManager.gI().getRandomTemplate(isBossHunter);
                    if (temp == null) {
                        Service.gI().sendThongBao(player, "Hiện chưa có lính loại này!");
                        return;
                    }

                    String title = isBossHunter ? "Thuê Lính Săn Boss (Random)" : "Thuê Lính Thường (Random)";
                    String priceInfo = title + "\n|7|Giá thuê tham khảo (Thanh toán bằng Thỏi Vàng):";

                    String opt30min = "30 phút\n(" + temp.getPrice30Min() + " thỏi)";
                    String opt1hour = "1 giờ\n(" + temp.getPrice1Hour() + " thỏi)";
                    String opt5hour = "5 giờ\n(" + temp.getPrice5Hour() + " thỏi)";

                    this.createOtherMenu(player, ConstNpc.MENU_MERCENARY_SELECT_TIME,
                            priceInfo, opt30min, opt1hour, opt5hour, "Đóng");
                }
                case ConstNpc.MENU_MERCENARY_SELECT_TIME -> {
                    if (select < 0 || select > 2) {
                        return;
                    }

                    if (player.linhDanhThueList.size() >= 2) {
                        player.iDMark.setMercenaryDuration(select);

                        String[] options = new String[player.linhDanhThueList.size() + 1];
                        for (int i = 0; i < player.linhDanhThueList.size(); i++) {
                            LinhDanhThue ldt = player.linhDanhThueList.get(i);
                            long secondsLeft = (ldt.getExpireTime() - System.currentTimeMillis()) / 1000;
                            long minutes = secondsLeft / 60;
                            options[i] = ldt.name + "\n(" + minutes + " phút)";
                        }
                        options[player.linhDanhThueList.size()] = "Đóng";

                        this.createOtherMenu(player, ConstNpc.MENU_MERCENARY_REPLACE,
                                "Con đã có đủ 2 lính. Con muốn thay thế lính nào?", options);
                        return;
                    }

                    summonMercenary(player, select);
                }
                case ConstNpc.MENU_MERCENARY_REPLACE -> {
                    if (select >= 0 && select < player.linhDanhThueList.size()) {
                        LinhDanhThue ldt = player.linhDanhThueList.get(select);
                        ldt.dispose();
                        if (player.linhDanhThueList.contains(ldt)) {
                            player.linhDanhThueList.remove(ldt);
                        }
                        summonMercenary(player, player.iDMark.getMercenaryDuration());
                    }
                }
            }
        }
    }

    private void summonMercenary(Player player, int durationOption) {
        boolean isBossHunter = (player.iDMark.getMercenaryId() == 1);
        MercenaryTemplate template = MercenaryManager.gI().getRandomTemplate(isBossHunter);

        if (template == null) {
            Service.gI().sendThongBao(player, "Lỗi: Không tìm thấy lính phù hợp");
            return;
        }

        int price = template.getPriceByDuration(durationOption);
        int goldBarId = 457;
        Item goldBar = InventoryService.gI().findItemBag(player, goldBarId);

        if (goldBar == null || goldBar.quantity < price) {
            Service.gI().sendThongBao(player, "Con không đủ thỏi vàng! Cần " + price + " thỏi vàng.");
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, goldBar, price);
        Service.gI().sendMoney(player);

        LinhDanhThue ldt = new LinhDanhThue(player, template, durationOption);
        player.linhDanhThueList.add(ldt);
        ldt.joinMapMaster();

        String durationText = switch (durationOption) {
            case 0 -> "30 phút";
            case 1 -> "1 giờ";
            case 2 -> "5 giờ";
            default -> "";
        };

        String typeInfo = isBossHunter ? "|1|Lính Săn Boss" : "|7|Lính Thường";
        Service.gI().sendThongBao(player, "|2|Thuê thành công!\n"
                + typeInfo + ": " + template.getName()
                + "\nThời gian: " + durationText
                + "\nHP: " + Util.numberToMoney(ldt.nPoint.hpMax)
                + "\nDame: " + Util.numberToMoney(ldt.nPoint.dame)
                + "\n|7|Hướng dẫn:"
                + "\nChat 'tan cong' để lính đánh"
                + "\nChat 'stop' để lính dừng");
    }
}
