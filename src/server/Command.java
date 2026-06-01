package server;
import boss.BossManager;


/**
 * @author EMTI
 */
import EMTI.SystemMetrics;
import boss.OtherBossManager;
import boss.RedRibbonHQManager;
import boss.TreasureUnderSeaManager;
import boss.SnakeWayManager;
import boss.GasDestroyManager;
import boss.TrungThuEventManager;
import consts.ConstNpc;
import item.Item;

import java.util.List;

import minigame.LuckyNumber.LuckyNumber;
import models.GiftCode.GiftCodeManager;
import models.ShenronEvent.ShenronEvent;
import models.ShenronEvent.ShenronEventManager;
import network.SessionManager;
import player.Pet;
import player.Player;
import player.PlayerClone;
import player.LinhDanhThue;
import player.badges.BadgesData;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PetService;
import services.Service;
import services.SkillService;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import skill.Skill;

public class Command {

    private static Command instance;

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    public void chat(Player player, String text) {
        if (!check(player, text)) {
            Service.gI().chat(player, text);
        }
    }

    public boolean check(Player player, String text) {
        if (player.isAdmin()) {
            if (text.equals("giftcode")) {
                models.GiftCode.GiftCodeService.gI().updateGiftCode();
                GiftCodeManager.gI().checkInfomationGiftCode(player);
                return true;
            } else if (text.equals("next nv")) {
                TaskService.gI().sendNextTaskMain(player);
                return true;
            } else if (text.startsWith("next ") && text.endsWith(" nv")) {
                try {
                    int n = Integer.parseInt(text.replace("next ", "").replace(" nv", ""));
                    for (int i = 0; i < n; i++) {
                        TaskService.gI().sendNextTaskMain(player);
                    }
                    Service.gI().sendThongBao(player, "Đã bỏ qua " + n + " nhiệm vụ");
                    return true;
                } catch (Exception e) {
                }
                return true;
            } else if (text.equals("mapboss")) {
                BossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapbroly")) {
                BossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapantrom")) {
                BossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapboss2")) {
                OtherBossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapdt")) {
                RedRibbonHQManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapbdkb")) {
                TreasureUnderSeaManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapcdrd")) {
                SnakeWayManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapkghd")) {
                GasDestroyManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("maptrungthu")) {
                TrungThuEventManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("hsk")) {
                Service.gI().releaseCooldownSkill(player);
                return true;
            } else if (text.startsWith("sp")) {
                try {
                    long power = Long.parseLong(text.replaceAll("sp", ""));
                    Service.gI().addSMTN(player, (byte) 2, power, false);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (text.equals("battu")) {
                if (player.isBattu) {
                    player.isBattu = false;
                } else {
                    player.isBattu = true;
                }
                Service.gI().sendThongBao(player, "Bất tử" + (player.isBattu ? ": ON" : ": OFF"));
                return true;
            } else if (text.startsWith("dt")) {
                try {
                    long power = Long.parseLong(text.replaceAll("dt", ""));
                    Service.gI().addSMTN(player.pet, (byte) 2, power, false);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (text.equals("skilldacbiet")) {
                System.out.println("skilldacbiet");
                try {
                    switch (player.gender) {
                        case 0 -> {
                            SkillService.gI().learSkillSpecial(player, Skill.SUPER_KAME, 6);
                        }
                        case 2 -> {
                            SkillService.gI().learSkillSpecial(player, Skill.LIEN_HOAN_CHUONG, 6);
                        }
                        default -> {
                            SkillService.gI().learSkillSpecial(player, Skill.MA_PHONG_BA, 6);
                        }
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (text.equals("phanthan")) {
                System.out.println("phanthan");
                switch (player.gender) {
                    case 0 -> {
                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
                    }
                    case 2 -> {
                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
                    }
                    default -> {
                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
                    }
                }
                return true;
            } else if (text.equals("tanghinh")) {
                // System.out.println("[TANG_HINH_DEBUG] Learning skill Tang Hinh for " +
                // player.name);
                SkillService.gI().learSkillSpecial(player, Skill.TANG_HINH, 7);
                Service.gI().sendThongBao(player, "Đã học skill Tàng Hình!");
                return true;
            } else if (text.equals("dragon")) {
                ShenronEvent shenron = new ShenronEvent();
                shenron.setPlayer(player);
                ShenronEventManager.gI().add(shenron);
                player.shenronEvent = shenron;
                shenron.setZone(player.zone);
                shenron.activeShenron(true, ShenronEvent.DRAGON_EVENT);
                shenron.sendWhishesShenron();
                return true;
            } else if (text.equals("admin")) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                        "|0|Time start: " + ServerManager.timeStart + "\nClients: " + Client.gI().getPlayers().size()
                                + " người chơi\n Sessions: " + SessionManager.gI().getNumSession() + "\nThreads: "
                                + Thread.activeCount() + " luồng" + "\n" + SystemMetrics.ToString(),
                        "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "Call Broly", "Buff VND",
                        "Buff\nhộp thư", "Lệnh cmd", "Đóng");
                return true;

            } else if (text.equals("vnd")) {
                Input.gI().createFormBuffVND(player);
                return true;
            } else if (text.equals("daucatmoi")) {
                for (int i = 0; i < 10; i++) {
                    ServerNotify.gI().notify("BOSS Nro vừa xuất hiện tại nhà anh ấy");
                }
                return true;
            } else if (text.startsWith("m ")) {
                int mapId = Integer.parseInt(text.replace("m ", ""));
                ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
                return true;
            }
            if (text.startsWith("dmg")) {
                try {
                    long dameg = Integer.parseInt(text.replaceAll("dmg", ""));
                    player.nPoint.dameg = dameg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("hpg")) {
                try {
                    long hpg = Integer.parseInt(text.replaceAll("hpg", ""));
                    player.nPoint.hpg = hpg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("mpg")) {
                try {
                    long mpg = Integer.parseInt(text.replaceAll("mpg", ""));
                    player.nPoint.mpg = mpg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("defg")) {
                try {
                    int defg = Integer.parseInt(text.replaceAll("defg", ""));
                    player.nPoint.defg = defg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("crg")) {
                try {
                    int critg = Integer.parseInt(text.replaceAll("crg", ""));
                    player.nPoint.critg = critg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("ntask")) {
                try {
                    int idTask = Integer.parseInt(text.replaceAll("ntask", ""));
                    player.playerTask.taskMain.id = idTask - 1;
                    player.playerTask.taskMain.index = 0;
                    TaskService.gI().sendNextTaskMain(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("badges_")) {
                int idBadges = Integer.parseInt(text.replaceAll("badges_", ""));
                player.badges.idBadges = idBadges;
            }
            if (text.startsWith("kq")) {
                Service.gI().sendThongBao(player, "Kết quả Lucky Round tiếp theo là: " + LuckyNumber.RESULT);
                return true;
            }
            if (text.startsWith("danhhieu_")) {
                int idGender = Integer.parseInt(text.replaceAll("danhhieu_", ""));
                BadgesData data = new BadgesData(player, idGender, 5);
                return true;
            }
            if (text.startsWith("gender_")) {
                byte idGender = Byte.parseByte(text.replaceAll("gender_", ""));
                player.gender = idGender;
                return true;
            }
            if (text.startsWith("i")) {
                String[] parts = text.split(" ");
                if (parts.length >= 3) {
                    short id = Short.parseShort(parts[1]);
                    int quantity = Integer.parseInt(parts[2]);
                    if (id < 0 || id >= Manager.ITEM_TEMPLATES.size()) {
                        Service.gI().sendThongBao(player, "Mã vật phẩm không hợp lệ");
                        return true;
                    }
                    Item item = ItemService.gI().createNewItem(id, quantity);
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                    if (!ops.isEmpty()) {
                        item.itemOptions = ops;
                    }
                    InventoryService.gI().addItemBag(player, item);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player,
                            "GET " + item.template.name + " [" + item.template.id + "] SUCCESS !");
                    return true;
                } else {
                    Service.gI().sendThongBao(player, "Lỗi");
                    return true;
                }
            } // else if (text.startsWith("i ")) {
              // int itemId = Integer.parseInt(text.replace("i ", ""));
              // Item item = ItemService.gI().createNewItem(((short) itemId));
              // List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short)
              // itemId);
              // if (!ops.isEmpty()) {
              // item.itemOptions = ops;
              // }
              // InventoryService.gI().addItemBag(player, item);
              // InventoryService.gI().sendItemBag(player);
              // Service.gI().sendThongBao(player, "GET " + item.template.name + " [" +
              // item.template.id + "] SUCCESS !");
              // return true;
              // }
            else if (text.equals("item")) {
                Input.gI().createFormGiveItem(player);
                return true;
            } else if (text.equals("getitem")) {
                Input.gI().createFormGetItem(player);
                return true;
            } else if (text.equals("d")) {
                Service.gI().setPos(player, player.location.x, player.location.y + 10);
                return true;
            }
        }
        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        } /*
           * else if (text.equals("rsp")) { // hồi all skill, Ki
           * Service.gI().releaseCooldownSkill(player.pet);
           * return true;
           * }
           */

        if (player.pet != null) {
            switch (text) {
                case "di theo", "follow" ->
                    player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" ->
                    player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" ->
                    player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" ->
                    player.pet.changeStatus(Pet.GOHOME);
                case "bien hinh" ->
                    player.pet.transform();
            }
        }
        // lệnh lính đánh thuê
        if (!player.linhDanhThueList.isEmpty()) {
            if (text.equals("tan cong") || text.equals("attack") || text.equals("giet") || text.equals("bao ve")) {
                for (LinhDanhThue ldt : player.linhDanhThueList) {
                    ldt.setAttackMode(true);
                }
            }
            if (text.equals("dung") || text.equals("stop") || text.equals("ve nha") || text.equals("follow")
                    || text.equals("di theo")) {
                for (LinhDanhThue ldt : player.linhDanhThueList) {
                    ldt.setAttackMode(false);
                }
            }
        }

        // Lệnh điều khiển Phân Thân
        if (player.clone != null) {
            switch (text) {
                case "giet", "kill" -> {
                    player.clone.setAttackMode(true);
                    Service.gI().sendThongBao(player, "Phân thân bắt đầu tấn công!");
                    return true;
                }
                case "dung", "stop", "thoi" -> {
                    player.clone.setAttackMode(false);
                    Service.gI().sendThongBao(player, "Phân thân đã dừng tấn công!");
                    return true;
                }
            }
        }
        return false;
    }
}