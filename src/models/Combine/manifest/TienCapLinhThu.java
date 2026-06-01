package models.Combine.manifest;

import consts.ConstNpc;
import consts.ConstTaskBadges;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;
import java.util.Random;
import task.Badges.BadgesTaskService;

public class TienCapLinhThu {
    private static final int[] UPGRADE_ITEM_QUANTITY = {5, 10, 20};
    private static final int[] GOLD_BAR_QUANTITY = {1, 3, 10}; 
    private static final int GOLD_BAR_ID = 457;
    private static final int[] SUCCESS_RATE = {20, 10, 1};
    private static final int[] OPTION = {5,7,9};
     private static final int[][] LINH_THU = {
        {1811, 1642, 1643, 1652, 1655, 1664, 1695, 1490, 1489, 1492, 1493, 1494},
        {1778, 1776, 1644, 1645, 1646, 1647, 1807, 1742},
        {1779, 1648, 1651, 1653, 1812},
        {1649, 1491,1650,1744}
    };
    public static boolean isLinhThu(Item item) {
      
        for (int i = 0; i < LINH_THU.length; i++) {
            for (int j = 0;j<LINH_THU[i].length;j++) {
                if (item.template.id == LINH_THU[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static void showInfoCombine(Player player) {
        if (player.combine == null || player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBaoOK(player, "Cần đủ số lượng");
            return;
        }

        Item linhThu = null;
        Item upgradeItem = null;
        Item goldBar = null;

        for (Item item : player.combine.itemsCombine) {
            if (isLinhThu(item)) {
                linhThu = item;
            } else if (item.template.id == 1641) {
                upgradeItem = item;
            } else if (item.template.id == GOLD_BAR_ID) {
                goldBar = item;
            }
        }
       

        if (linhThu == null || upgradeItem == null || goldBar == null) {
            Service.gI().sendThongBaoOK(player, "Cần 1 Linh thú, đủ số lượng Mảnh cải trang và Thỏi vàng.");
            return;
        }

        int linhThuTier = getLinhThuTier(linhThu);
        if (linhThuTier == -1 || linhThuTier >= LINH_THU.length - 1) {
            Service.gI().sendThongBaoOK(player, "Linh thú đã đạt cấp tối đa!");
            return;
        }

        int requiredItems = UPGRADE_ITEM_QUANTITY[linhThuTier];
        int requiredGoldBars = GOLD_BAR_QUANTITY[linhThuTier];
        int successRate = SUCCESS_RATE[linhThuTier];
        
        String npcSay = "Nâng cấp cần x" + requiredItems + " Mảnh cải trang và " + requiredGoldBars + " Thỏi vàng với tỷ lệ thành công: " + successRate + "%";
        CombineService.gI().TrungAcMa.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Tiến hóa", "Từ chối");
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBaoOK(player, "Cần đủ số lượng");
            return;
        }

        Item linhThu = null;
        Item upgradeItem = null;
        Item goldBar = null;

        for (Item item : player.combine.itemsCombine) {
            if (isLinhThu(item)) {
                linhThu = item;
            } else if (item.template.id == 1641) {
                upgradeItem = item;
            } else if (item.template.id == GOLD_BAR_ID) {
                goldBar = item;
            }
        }

        if (linhThu == null || upgradeItem == null || goldBar == null) {
            Service.gI().sendThongBaoOK(player, "Cần 1 Linh thú, đủ số lượng Mảnh cải trang và Thỏi vàng.");
            return;
        }

        int linhThuTier = getLinhThuTier(linhThu);
        if (linhThuTier == -1 || linhThuTier >= LINH_THU.length - 1) {
            Service.gI().sendThongBaoOK(player, "Linh thú đã đạt cấp tối đa!");
            return;
        }

        int requiredItems = UPGRADE_ITEM_QUANTITY[linhThuTier];
        int requiredGoldBars = GOLD_BAR_QUANTITY[linhThuTier];
        int successRate = SUCCESS_RATE[linhThuTier];

        if (upgradeItem.quantity < requiredItems || goldBar.quantity < requiredGoldBars) {
            Service.gI().sendThongBaoOK(player, "Không đủ nguyên liệu để tiến hóa.");
            return;
        }

        if (Util.isTrue(successRate, 100)) {
            if(linhThuTier+1==3){
                BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.LINH_THU_TRUYEN_KY, 1);
            }
            int[] nextTierLinhThu = LINH_THU[linhThuTier + 1];
            int newLinhThuId = nextTierLinhThu[new Random().nextInt(0,nextTierLinhThu.length-1)];
            Item newItem = ItemService.gI().createNewItem((short) newLinhThuId);
            CombineService.gI().sendEffectSuccessCombine(player);
            newItem.itemOptions.add(new ItemOption(72,OPTION[linhThuTier]));
            InventoryService.gI().subQuantityItemsBag(player, linhThu, 1);
            InventoryService.gI().addItemBag(player, newItem);
            InventoryService.gI().subQuantityItemsBag(player, upgradeItem, requiredItems);
            InventoryService.gI().subQuantityItemsBag(player, goldBar, requiredGoldBars);
            CombineService.gI().TrungAcMa.npcChat(player, "Chúc mừng, Linh thú đã tiến hóa!");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            InventoryService.gI().subQuantityItemsBag(player, upgradeItem, requiredItems);
            InventoryService.gI().subQuantityItemsBag(player, goldBar, requiredGoldBars);
            CombineService.gI().TrungAcMa.npcChat(player, "Thật tiếc, tiến hóa thất bại!");
        }
        
        InventoryService.gI().sendItemBag(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static int getLinhThuTier(Item item) {
        for (int i = 0; i < LINH_THU.length; i++) {
            for (int id : LINH_THU[i]) {
                if (item.template.id == id) {
                    return i;
                }
            }
        }
        return -1;
    }
}
