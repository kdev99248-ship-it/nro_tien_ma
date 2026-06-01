package models.TetEvent;

import consts.ConstNpc;
import item.Item;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.Service;
import utils.Util;
import config.EventConfig;

public class TetEventService {

    private static TetEventService instance;

    public static final int MANG_CAU = 1177;
    public static final int DUA = 1178;
    public static final int DU_DU = 1179;
    public static final int XOAI = 1180;
    public static final int SUNG = 1181;
    public static final int MAM_NGU_QUA = 1182;
    public static final int BAO_LI_XI = 1183;

    public static TetEventService gI() {
        if (instance == null) {
            instance = new TetEventService();
        }
        return instance;
    }

    public void openMenuTet(Player pl) {
        if (!EventConfig.LUNAR_NEW_YEAR) {
            Service.gI().sendThongBao(pl, "Sự kiện Tết đã kết thúc");
            return;
        }
        NpcService.gI().createMenuConMeo(pl, ConstNpc.MENU_TET_EVENT, -1,
                "Con muốn đổi Mâm ngũ quả hay muốn làm gì?",
                "Đổi Mâm\nngũ quả", "Từ chối");
    }

    public void exchangeMamNguQua(Player pl) {
        if (!EventConfig.LUNAR_NEW_YEAR) {
            Service.gI().sendThongBao(pl, "Sự kiện Tết đã kết thúc");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
            return;
        }

        if (InventoryService.gI().findItemBag(pl, MANG_CAU) != null
                && InventoryService.gI().findItemBag(pl, DUA) != null
                && InventoryService.gI().findItemBag(pl, DU_DU) != null
                && InventoryService.gI().findItemBag(pl, XOAI) != null
                && InventoryService.gI().findItemBag(pl, SUNG) != null) {

            try {
                InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, MANG_CAU), 1);
                InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, DUA), 1);
                InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, DU_DU), 1);
                InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, XOAI), 1);
                InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, SUNG), 1);

                Item mamNguQua = ItemService.gI().createNewItem((short) MAM_NGU_QUA);
                InventoryService.gI().addItemBag(pl, mamNguQua);
                InventoryService.gI().sendItemBag(pl);
                Service.gI().sendThongBao(pl, "Đổi mâm ngũ quả thành công");
            } catch (Exception e) {
                Service.gI().sendThongBao(pl, "Có lỗi xảy ra");
                e.printStackTrace();
            }

        } else {
            Service.gI().sendThongBao(pl, "Bạn thiếu nguyên liệu (Mãng cầu, Dừa, Đu đủ, Xoài, Sung)");
        }
    }

    public void useMamNguQua(Player pl, Item item) {
        if (!EventConfig.LUNAR_NEW_YEAR) {
            Service.gI().sendThongBao(pl, "Sự kiện Tết đã kết thúc");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);

        // Reward logic
        int ratio = Util.nextInt(100);
        if (ratio < 40) { // 40% Vàng
            int gold = Util.nextInt(100000, 500000);
            pl.inventory.gold += gold;
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + Util.numberToMoney(gold) + " vàng");
        } else if (ratio < 70) { // 30% Ngọc xanh
            int gem = Util.nextInt(10, 50);
            pl.inventory.gem += gem;
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + gem + " ngọc xanh");
        } else if (ratio < 90) { // 20% Dưa hấu (hồi HP/KI)
            Item melon = ItemService.gI().createNewItem((short) 51); // Dưa hấu
            melon.quantity = 5;
            InventoryService.gI().addItemBag(pl, melon);
            InventoryService.gI().sendItemBag(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được 5 Dưa hấu");
        } else { // 10% Item hiếm (Cải trang Áo dài hoặc Hết sẩy)
            // Example: Áo dài Tết (tempId customized needed, using placeholder or generic
            // outfit)
            // Using Pet com com as a place holder for rare item or just a random item
            short[] rareItems = { 380, 381, 382, 383, 384, 385 }; // CSKB, Cuong no, Bo huyet, etc.
            short itemId = rareItems[Util.nextInt(rareItems.length)];
            Item rare = ItemService.gI().createNewItem(itemId);
            InventoryService.gI().addItemBag(pl, rare);
            InventoryService.gI().sendItemBag(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + rare.template.name);
        }
    }

    public void useBaoLiXi(Player pl, Item item) {
        if (!EventConfig.LUNAR_NEW_YEAR) {
            Service.gI().sendThongBao(pl, "Sự kiện Tết đã kết thúc");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) == 0) {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);

        // Reward logic
        int ratio = Util.nextInt(100);
        if (ratio < 60) { // 60% Vàng
            int gold = Util.nextInt(500000, 2000000); // 500k - 2m gold
            pl.inventory.gold += gold;
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + Util.numberToMoney(gold) + " vàng");
        } else if (ratio < 95) { // 35% Hồng ngọc
            int ruby = Util.nextInt(20, 100);
            pl.inventory.ruby += ruby;
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + ruby + " hồng ngọc");
        } else { // 5% Ngọc xanh
            int gem = Util.nextInt(10, 20);
            pl.inventory.gem += gem;
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + gem + " ngọc xanh");
        }
    }
}
