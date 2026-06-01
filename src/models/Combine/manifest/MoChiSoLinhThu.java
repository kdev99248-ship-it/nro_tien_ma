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
import java.util.HashSet;
import java.util.Random;

public class MoChiSoLinhThu {
   
    private static final int[][] OPTION = {{0,6,7,5,27,28,50,77,103},{500,5000,5000,5,5000,5000,5,5,5},//Thường
                            {0,6,7,5,27,28,50,77,103},{800,8000,8000,8,8000,8000,8,8,8},//tinh anh
                            {0,6,7,5,27,28,50,77,103,156,157,158},{1200,12000,12000,12,12000,12000,12,12,12,10,10,10},//huyen thoại
                            {0,6,7,5,27,28,50,77,103,156,157,158,163,181,161,189,190},{2000,20000,20000,20,20000,20000,20,20,20,20,20,20,20,20,20,20,20}//TruyenKi         
            };
    private static final int[][] LINH_THU = {
        {1811, 1642, 1643, 1652, 1655, 1664, 1695, 1490, 1489, 1492, 1493, 1494},
        {1778, 1776, 1644, 1645, 1646, 1647, 1807, 1742},
        {1779, 1648, 1651, 1653, 1812},
        {1649, 1491,1650,1744}
    };

    private static final int[] GOLD_BAR_QUANTITY = {10, 25, 30, 50};
    private static final int SUCCESS_RATE = 50;
    
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
        if (player.combine == null || player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBaoOK(player, "Cần 1 Linh thú gốc, 1 Linh thú cùng loại và  Thỏi vàng.");
            return;
        }
        
        Item linhThuGoc = null;
        Item linhThuNangCap = null;
        Item goldBar = null;

        for (Item item : player.combine.itemsCombine) {
            if (isLinhThu(item)) {
                if (linhThuGoc == null) {
                    linhThuGoc = item;
                } else {
                    linhThuNangCap = item;
                }
            } else {
                goldBar = item;
            }
        }

        if (linhThuGoc == null || linhThuNangCap == null || goldBar == null) {
            Service.gI().sendThongBaoOK(player, "Cần 1 Linh thú gốc, 1 Linh thú cùng loại và  Thỏi vàng.");
            return;
        }
        if(getLinhThuTier(linhThuGoc)!=getLinhThuTier(linhThuNangCap)){
            Service.gI().sendThongBaoOK(player, "Cần linh thú cùng loại để mở chỉ số");
            return;
        }
        int linhThuTier = getLinhThuTier(linhThuGoc);
        int requiredGoldBars = GOLD_BAR_QUANTITY[linhThuTier];

        String npcSay = "Nâng cấp cần " + requiredGoldBars + " Thỏi vàng.";
        CombineService.gI().TrungAcMa.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Mở chỉ số", "Từ chối");
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 3) {
            Service.gI().sendThongBaoOK(player, "Cần 1 Linh thú gốc, 1 Linh thú cùng loại và  Thỏi vàng.");
            return;
        }

        Item linhThuGoc = null;
        Item linhThuNangCap = null;
        Item goldBar = null;

        for (Item item : player.combine.itemsCombine) {
            if (isLinhThu(item)) {
                if (linhThuGoc == null) {
                    linhThuGoc = item;
                } else {
                    linhThuNangCap = item;
                }
            } else {
                goldBar = item;
            }
        }

        if (linhThuGoc == null || linhThuNangCap == null || goldBar == null) {
            Service.gI().sendThongBaoOK(player, "Cần 1 Linh thú gốc, 1 Linh thú cùng loại và Thỏi vàng.");
            return;
        }
        if(getLinhThuTier(linhThuGoc)!=getLinhThuTier(linhThuNangCap)){
            Service.gI().sendThongBaoOK(player, "Cần linh thú cùng loại để mở chỉ số");
            return;
        }
        int linhThuTier = getLinhThuTier(linhThuGoc);
        int requiredGoldBars = GOLD_BAR_QUANTITY[linhThuTier];

        if (goldBar.quantity < requiredGoldBars) {
            Service.gI().sendThongBaoOK(player, "Không đủ Thỏi vàng để Mở chỉ số.");
            return;
        }

       
            CombineService.gI().sendEffectSuccessCombine(player);
            linhThuGoc.itemOptions.clear();
            if(linhThuTier==0){
                int x = Util.nextInt(0,OPTION[0].length-1);
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[0][x], Util.nextInt(1,OPTION[1][x])));
                linhThuGoc.itemOptions.add(new ItemOption(72, 1));
              
            }else if(linhThuTier==1){
                int x = Util.nextInt(0,OPTION[2].length-1);
                int y = x;
                while(y==x){
                    y = Util.nextInt(0,OPTION[2].length-1);
                }
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[2][x], Util.nextInt(1,OPTION[3][x])));
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[2][y], Util.nextInt(1,OPTION[3][y])));
                linhThuGoc.itemOptions.add(new ItemOption(72, 5));
            }else if(linhThuTier==2){
                 int x = Util.nextInt(0,OPTION[4].length-1);
                int y = x;
                while(y==x){
                    y = Util.nextInt(0,OPTION[4].length-1);
                }
                 int z = y;
                while(z==y||z==x){
                    z =  Util.nextInt(0,OPTION[4].length-1);
                }
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[4][x], Util.nextInt(1,OPTION[5][x])));
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[4][y], Util.nextInt(1,OPTION[5][y])));
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[4][z], Util.nextInt(1,OPTION[5][z])));
                linhThuGoc.itemOptions.add(new ItemOption(72, 7));
            }else if(linhThuTier==3){
                 int x = Util.nextInt(0,OPTION[6].length-1);
                int y = x;
                while(y==x){
                    y = Util.nextInt(0,OPTION[6].length-1);
                }
                 int z = y;
                while(z==y||z==x){
                    z =  Util.nextInt(0,OPTION[6].length-1);
                }
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[6][x], Util.nextInt(1,OPTION[7][x])));
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[6][y], Util.nextInt(1,OPTION[7][y])));
                linhThuGoc.itemOptions.add(new ItemOption(OPTION[6][z], Util.nextInt(1,OPTION[7][z])));
                linhThuGoc.itemOptions.add(new ItemOption(72, 9));
            }
            
            CombineService.gI().TrungAcMa.npcChat(player, "Mở chỉ số thành công!");
        
         InventoryService.gI().subQuantityItemsBag(player, linhThuNangCap, 1);
            InventoryService.gI().subQuantityItemsBag(player, goldBar, requiredGoldBars);
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
        return 0;
    }
}
