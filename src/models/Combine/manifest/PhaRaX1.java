package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

public class PhaRaX1 {

    public static boolean isDoThanLinh(Item item) {
        return item.template.id >= 555 && item.template.id <= 567;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 1) {
            Item trangBi = null;
            for (Item item : player.combine.itemsCombine) {
                 if (isDoThanLinh(item)){
                     trangBi = item;
                 }
            }
            if (trangBi!=null) {
               CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                        "Phân Rã ngay để nhận thỏi vàng","Phân rã");
            } else {
                Service.gI().sendThongBaoOK(player, "Chỉ có thể phân rã trang bị thần linh!");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Vui lòng chọn một trang bị thần linh để phân rã!");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 1) {
            Item trangBi = player.combine.itemsCombine.get(0);
            if (isDoThanLinh(trangBi)) {
                int soLuongThoiVang = Util.nextInt(1, 10); // Số lượng ngẫu nhiên từ 1 đến 10
                InventoryService.gI().subQuantityItemsBag(player, trangBi, 1);
                
                Item thoiVang = ItemService.gI().createNewItem((short) 457, soLuongThoiVang);
                InventoryService.gI().addItemBag(player, thoiVang);
                CombineService.gI()
                    .reOpenItemCombine(player);
                Service.gI().sendThongBaoOK(player, "Bạn đã phân rã 1 đồ thần linh và nhận được " + soLuongThoiVang + " thỏi vàng!");
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBaoOK(player, "Chỉ có thể phân rã trang bị thần linh!");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Vui lòng chọn một trang bị thần linh để phân rã!");
        }
    }
}
