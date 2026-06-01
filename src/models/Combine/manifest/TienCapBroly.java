package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;
import java.util.Random;

public class TienCapBroly {
    
    private static final int[] UPGRADE_ITEM_QUANTITY = {10, 15, 20, 25, 30, 35};
    private static final int[] GOLD_BAR_QUANTITY = {10, 15, 20, 25, 30, 35}; // Số lượng Thỏi vàng cần thiết
    private static final int GOLD_BAR_ID = 457; // ID của Thỏi vàng
    private static final int[] SUCCESS_RATE={100,80,50,30,10,5};
    private static final int[][] OPTIONS = {
            {50, 22, 77, 22, 103, 22, 5, 6, 72, 2},
            {50, 24, 77, 24, 103, 24, 5, 7, 72, 3},
            {50, 26, 77, 26, 103, 26, 5, 8, 72, 4},
            {50, 28, 77, 28, 103, 28, 5, 9, 72, 5},
            {50, 30, 77, 30, 103, 30, 5, 12, 72, 6},
            {50, 30, 77, 30, 103, 30, 5, 15, 72, 7}
    };

   

    public static boolean isTrunk(Item item) {
        return item.template.id >= 1758 && item.template.id <= 1763;
    }
    
    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 3) {
            Item trunkMask = null;
            Item upgradeItem = null;
            Item goldBar = null;

            for (Item item : player.combine.itemsCombine) {
                if (isTrunk(item)) {
                    trunkMask = item;
                } else if (item.template.id == 720) {
                    upgradeItem = item;
                } else if (item.template.id == GOLD_BAR_ID) {
                    goldBar = item;
                }
            }

            player.combine.goldCombine = 200_000_000;
            if (trunkMask != null && upgradeItem != null && goldBar != null) {
                int upgradeCount = trunkMask.template.id - 1758;
                int requiredItems = UPGRADE_ITEM_QUANTITY[upgradeCount];
                int requiredGoldBars = GOLD_BAR_QUANTITY[upgradeCount];
                int success = SUCCESS_RATE[upgradeCount];
                if (upgradeItem.quantity < requiredItems) {
                    Service.gI().sendThongBaoOK(player, "Cần " + requiredItems + " Mảnh cải trang.");
                    return;
                }
                
                if (goldBar.quantity < requiredGoldBars) {
                    Service.gI().sendThongBaoOK(player, "Cần " + requiredGoldBars + " Thỏi vàng.");
                    return;
                }
                
                String npcSay = "Nâng cấp cần x" +requiredItems+" mảnh cải trang và "+requiredGoldBars+ " Thỏi Vàng với Tỷ lệ thành công:" +  success + "%";
                CombineService.gI().vegeta.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Tiến hóa\n" + Util.numberToMoney(player.combine.goldCombine) + " vàng", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 1 cải trang Broly, đủ số lượng Mảnh Cải trang và Thỏi vàng.");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 cải trang Broly, đủ số lượng Mảnh Cải trang và Thỏi vàng.");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() == 3) {
            int gold = player.combine.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold - player.inventory.gold) + " vàng nữa");
                Service.gI().sendMoney(player);
                return;
            }

            Item trunkMask = null;
            Item upgradeItem = null;
            Item goldBar = null;

            for (Item item : player.combine.itemsCombine) {
                if (isTrunk(item)) {
                    trunkMask = item;
                } else if (item.template.id == 720) {
                    upgradeItem = item;
                } else if (item.template.id == GOLD_BAR_ID) {
                    goldBar = item;
                }
            }

            if (trunkMask == null || upgradeItem == null || goldBar == null) {
                Service.gI().sendThongBaoOK(player, "Cần 1 cải trang Broly, đủ số lượng Mảnh cải trang và Thỏi vàng.");
                return;
            }

            int newId = trunkMask.template.id + 1;
            if (newId > 1763) {
                Service.gI().sendThongBaoOK(player, "Cải trang Broly đã đạt cấp tối đa!");
                return;
            }
            
            int upgradeCount = trunkMask.template.id - 1758;
            int requiredItems = UPGRADE_ITEM_QUANTITY[upgradeCount];
            int requiredGoldBars = GOLD_BAR_QUANTITY[upgradeCount];
            int success = SUCCESS_RATE[upgradeCount];
            if (upgradeItem.quantity < requiredItems || goldBar.quantity < requiredGoldBars) {
                Service.gI().sendThongBaoOK(player, "Không đủ nguyên liệu để tiến hóa.");
                return;
            }

            

            if (Util.isTrue(success,100)) {
                Item newItem = ItemService.gI().createNewItem((short) newId);
                for (int i = 0; i < OPTIONS[upgradeCount].length; i += 2) {
                    newItem.itemOptions.add(new ItemOption(OPTIONS[upgradeCount][i], OPTIONS[upgradeCount][i + 1]));
                }
                CombineService.gI().sendEffectSuccessCombine(player);
                InventoryService.gI().subQuantityItemsBag(player, trunkMask, 1);
                InventoryService.gI().addItemBag(player, newItem);
                InventoryService.gI().subQuantityItemsBag(player, upgradeItem, requiredItems);
                InventoryService.gI().subQuantityItemsBag(player, goldBar, requiredGoldBars);
               
                Service.gI().sendMoney(player);
               CombineService.gI().vegeta.npcChat(player, "Chúc mừng thằng rank!!");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                InventoryService.gI().subQuantityItemsBag(player, upgradeItem, requiredItems);
                InventoryService.gI().subQuantityItemsBag(player, goldBar, requiredGoldBars);
                CombineService.gI().vegeta.npcChat(player, "Em Đen lắm!!");
                Service.gI().sendMoney(player);
               
            }
             InventoryService.gI().sendItemBag(player);
            CombineService.gI().reOpenItemCombine(player);
            
        }
    }
}
