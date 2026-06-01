package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

public class PhanRaLinhThu {
  private static final int[][] LINH_THU = {
        {1811, 1642, 1643, 1652, 1655, 1664, 1695, 1490, 1489, 1492, 1493, 1494},
        {1778, 1776, 1644, 1645, 1646, 1647, 1807, 1742},
        {1779, 1648, 1651, 1653, 1812},
        {1649, 1491,1650,1744}
    };
  private static int getLinhThuTier(Item item) {
        for (int i = 0; i < LINH_THU.length; i++) {
            for (int id : LINH_THU[i]) {
                if (item.template.id == id) {
                    return i;
                }
            }
        }
        return 0;
    }
private static final int[]QuantilyHonLinhThu ={3,10,30,100};
  public static boolean isLinhThu(Item item) {
        for (int[] linhThuGroup : LINH_THU) {
            for (int id : linhThuGroup) {
                if (item.template.id == id) {
                    return true;
                }
            }
        }
        return false;
    }
   
    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 1) {
            Item trangBi = null;
            for (Item item : player.combine.itemsCombine) {
                 if (isLinhThu(item)){
                     trangBi = item;
                 }
            }
            if (trangBi!=null) {
               CombineService.gI().TrungAcMa.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                        "Phân Rã ngay để nhận Hồn Linh Thu","Phân rã");
            } else {
                Service.gI().sendThongBaoOK(player, "Chỉ có thể phân rã Linh Thú!");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Vui lòng chọn một Linh Thú để phân rã!");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 1) {
            Item trangBi = player.combine.itemsCombine.get(0);
            if (isLinhThu(trangBi)) {
                   CombineService.gI().sendEffectSuccessCombine(player);
                int idex = getLinhThuTier(trangBi);
                int soLuongThoiVang = Util.nextInt(0, QuantilyHonLinhThu[idex]); 
                InventoryService.gI().subQuantityItemsBag(player, trangBi, 1);
                
                Item thoiVang = ItemService.gI().createNewItem((short) 1641, soLuongThoiVang);
                InventoryService.gI().addItemBag(player, thoiVang);
                CombineService.gI()
                    .reOpenItemCombine(player);
                Service.gI().sendThongBaoOK(player, "Bạn đã phân rã Linh Thú và nhận được " + soLuongThoiVang + " Hồn Linh Thú!");
                InventoryService.gI().sendItemBag(player);
            } else {
                Service.gI().sendThongBaoOK(player, "Chỉ có thể phân rã Linh Thu!");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Vui lòng một Linh Thú để phân rã!");
        }
    }
}
