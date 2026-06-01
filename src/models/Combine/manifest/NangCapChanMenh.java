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

public class NangCapChanMenh {

   
    private static final int MAX_LEVEL_ID = 1836;
    
    private static final int[] ITEM_IDS = {1828, 1829, 1830, 1831, 1832, 1833, 1834, 1835, 1836};
    private static final int BASE_ITEM_ID = 1828;
    private static final int[] GEM_QUANTITY ={1000,1500,2000,2500,3000,3500,4000,4500,5000};//kim cương mất
    private static final int[] UPGRADE_ITEM_QUANTITY = {5, 10, 15, 20, 25, 30, 35, 40, 45};//đa hoàng kim
    private static final int[] SUCCESS_RATES = {100, 80, 60, 40, 25, 15, 10, 5, 1};//tỷ lệ thành công
    
    private static final int[][] ITEM_OPTIONS = {
        
        {50, 77, 103, 3}, // Chân mệnh 2: 3%
        {50, 77, 103, 5}, // Chân mệnh 3: 5%
        {50, 77, 103, 8}, // Chân mệnh 4: 8%
        {50, 77, 103, 11}, // Chân mệnh 5: 11%
        {50, 77, 103, 13}, // Chân mệnh 6: 13%
        {50, 77, 103, 15}, // Chân mệnh 7: 15%
        {50, 77, 103, 17}, // Chân mệnh 8: 17%
        {50, 77, 103, 20}  // Chân mệnh 9: 20%
    };
    
    public static boolean isChanMenh(Item item){
        return item.template.id >= 1828 && item.template.id <= 1836;
    }
    
    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 2) {
            Item chanMenh = null;
            Item upgradeItem = null;
           
            for (Item item : player.combine.itemsCombine) {
                if (isChanMenh(item)) {
                    chanMenh = item;
                } else if (item.template.id == 1837) {
                    upgradeItem = item;
                }
            }

            if (chanMenh != null && upgradeItem != null) {
                int upgradeLevel = chanMenh.template.id - BASE_ITEM_ID;
                int requiredItems = UPGRADE_ITEM_QUANTITY[upgradeLevel];
                int slgem = GEM_QUANTITY[upgradeLevel];
                if(player.inventory.gem<slgem){
                    Service.gI().sendThongBaoOK(player, "Cần " + slgem + " Ngọc xanh.");
                    return ;
                }
                if (upgradeItem.quantity < requiredItems) {
                    Service.gI().sendThongBaoOK(player, "Cần " + requiredItems + " Đá Hoàng Kim.");
                    return;
                }
                 int successRate = SUCCESS_RATES[upgradeLevel];
                String npcSay = "Cần x" + requiredItems+ " Đá hoàng kim và "+slgem +" ngọc xanh với tỷ lệ" + " tỷ lệ: "+successRate+  " %";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Nâng cấp", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 1 Chân Mệnh và đủ số lượng Đá Hoàng Kim.");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 Chân Mệnh và đủ số lượng Đá Hoàng Kim.");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            Item chanMenh = null;
            Item upgradeItem = null;

            for (Item item : player.combine.itemsCombine) {
                if (isChanMenh(item)) {
                    chanMenh = item;
                } else if (item.template.id == 1837) {
                    upgradeItem = item;
                }
            }

            if (chanMenh == null || upgradeItem == null) {
                Service.gI().sendThongBaoOK(player, "Cần 1 Chân Mệnh và đá hoàng kim");
                return;
            }
            
            if (chanMenh.template.id == MAX_LEVEL_ID) {
                Service.gI().sendThongBaoOK(player, "Chân Mệnh đã đạt cấp tối đa!");
                return;
            }
             
            int upgradeLevel = chanMenh.template.id - BASE_ITEM_ID;
            int requiredItems = UPGRADE_ITEM_QUANTITY[upgradeLevel];
            int successRate = SUCCESS_RATES[upgradeLevel];
            int slgem = GEM_QUANTITY[upgradeLevel];
            if (upgradeItem.quantity < requiredItems) {
                Service.gI().sendThongBaoOK(player, "Cần x"+requiredItems+" Đá hoàng kim"+" tỷ lệ: "+successRate+" %" );
                return;
            }
      
           if(player.inventory.gem<slgem){
                    Service.gI().sendThongBaoOK(player, "Cần " + slgem + " Ngọc xanh.");
                    return ;
                }
            if (Util.isTrue(successRate,100)) {
                int newId = chanMenh.template.id + 1;
                Item newItem = ItemService.gI().createNewItem((short) newId);
                
                for (int i = 0; i < ITEM_OPTIONS[upgradeLevel].length - 1; i++) {
                    newItem.itemOptions.add(new ItemOption(ITEM_OPTIONS[upgradeLevel][i], ITEM_OPTIONS[upgradeLevel][3]));
                }
                player.inventory.gem-=slgem;
                InventoryService.gI().subQuantityItemsBag(player, chanMenh, 1);
                InventoryService.gI().subQuantityItemsBag(player, upgradeItem, requiredItems);
                InventoryService.gI().addItemBag(player, newItem);
                CombineService.gI().sendEffectSuccessCombine(player);
               
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                
                player.inventory.gem-=slgem;
                InventoryService.gI().subQuantityItemsBag(player, upgradeItem, requiredItems);
            }

            InventoryService.gI().sendItemBag(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
