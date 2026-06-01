package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import server.Manager;
import services.InventoryService;
import services.ItemService;
import services.RewardService;
import services.Service;
import utils.Util;

/**
 *
 * @author Admin
 */
public class NangCapKichHoatVip {

    public static boolean isDoThanLinh(Item item) {
        if (item.template.id >= 555 && item.template.id <= 567) {
            return true;
        }
        return false;
    }

    public static boolean isDoHD(Item item) {
        if (item.template.id >= 650 && item.template.id <= 662) {
            return true;
        }
        return false;
    }

    public static boolean isDoLevelMax(Item item) {
        if (item.template.strRequire >= 200000000) {
            return true;
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 5) {
            Item trangBi = null;
            Item daKichHoat = null;
            Item dns = null;
            Item tv = null;
            Item co4la = null;
            int qualityda = 1;
            int qualityTv = 1;
            int qualityNs = 1;
             int quality4la = 1;
            for (Item item : player.combine.itemsCombine) {
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id >= 127 && io.optionTemplate.id <= 144 || io.optionTemplate.id >= 241) {
                        trangBi = item;
                        break;
                    }
                }
                if (trangBi != null && isDoLevelMax(trangBi)) {
                    qualityTv = 150;
                    qualityNs = 5;
                    qualityda = 5;
                    quality4la = 5;
                } else if (trangBi != null && isDoThanLinh(trangBi)) {
                    qualityTv = 300;
                    qualityNs = 10;
                    qualityda = 10;
                    quality4la = 10;
                } else if (trangBi != null && isDoHD(trangBi)) {
                    qualityTv = 500;
                    qualityNs = 20;
                    qualityda = 20;
                    quality4la = 20;
                } else {
                    trangBi = null;
                }
                if (item.template.id == 1740 && item.quantity >= qualityda) {
                    daKichHoat = item;
                } else if (item.template.id == 457 && item.quantity >= qualityTv) {
                    tv = item;
                } else if (item.template.id == 674 && item.quantity >= qualityNs) {
                    dns = item;
                }else if (item.template.id == 1635 && item.quantity >= quality4la) {
                    co4la = item;
                }
            }
            player.combine.goldCombine = 500_000_000;
            int goldCombie = player.combine.goldCombine;
            if (trangBi != null && dns != null && daKichHoat != null && tv != null && tv.quantity >= 150 && daKichHoat.quantity >= 5 && dns.quantity >= 5) {
                String npcSay = "Sau khi cường hoá, sẽ được nâng cấp trang bị thành trang bị Thần Linh Kích hoạt";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Cường hoá\n" + Util.numberToMoney(goldCombie) + " vàng", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị kich hoat Đạt tối đa và  đá kích hoạt và  thỏi vàng ,  đá ngũ sắc và cỏ 4 lá");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị kich hoat Đạt tối đa và  đá kích hoạt và  thỏi vàng ,  đá ngũ sắc và cỏ 4 lá");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() == 5) {
            int gold = player.combine.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold - player.inventory.gold) + " vàng nữa");
                Service.gI().sendMoney(player);
                return;
            }
            int qualityda = 1;
            int qualityTv = 1;
            int qualityNs = 1;
            int quality4la = 1;
            Item co4la =null;
            Item trangBi = null;
            Item daKichHoat = null;
            Item tv = null;
            Item dns = null;
            int tile = 0;
            for (Item item : player.combine.itemsCombine) {
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id >= 127 && io.optionTemplate.id <= 144 || io.optionTemplate.id >= 241) {
                        trangBi = item;
                        break;
                    }
                }
                if (trangBi != null && isDoLevelMax(trangBi)) {
                    qualityTv = 150;
                    qualityNs = 5;
                    qualityda = 5;
                    quality4la = 5;
                    tile = 30;
                } else if (trangBi != null && isDoThanLinh(trangBi)) {
                    qualityTv = 300;
                    qualityNs = 10;
                    qualityda = 10;
                    quality4la = 10;
                    tile = 20;
                } else if (trangBi != null && isDoHD(trangBi)) {
                    qualityTv = 500;
                    qualityNs = 20;
                    qualityda = 20;
                    quality4la = 20;
                    tile = 10;
                }
                if (item.template.id == 1740 && item.quantity >= qualityda) {
                    daKichHoat = item;
                } else if (item.template.id == 457 && item.quantity >= qualityTv) {
                    tv = item;
                } else if (item.template.id == 674 && item.quantity >= qualityNs) {
                    dns = item;
                } else if (item.template.id == 1635 && item.quantity >= quality4la) {
                    co4la = item;
                }
                   
            }
            Item newItem = null;
            if (trangBi.template.type == 4) {
                
                int gender = player.gender;
               
                if (gender == 3) {
                    gender = 0;
                }
                int index = indexSkh(trangBi.template.id, Manager.trangBiKichHoatVipRada[gender]) + 1;
                if (index == Manager.trangBiKichHoatVipRada[gender].length) {
                    Service.gI().sendThongBaoOK(player, "Max rồi DMM!!!");
                    return;
                }
                newItem = ItemService.gI().createNewItem((short) Manager.trangBiKichHoatVipRada[gender][index]);
            } else {
                short[] listitem = null;
                switch (trangBi.template.type) {
                    case 0 -> {
                        listitem = Manager.trangBiKichHoatVipAo[trangBi.template.gender];
                        break;
                    }
                    case 1 -> {
                        listitem = Manager.trangBiKichHoatVipQuan[trangBi.template.gender];
                        break;
                    }
                    case 2 -> {
                        listitem = Manager.trangBiKichHoatVipGang[trangBi.template.gender];
                        break;
                    }
                    case 3 -> {
                        listitem = Manager.trangBiKichHoatVipJay[trangBi.template.gender];
                        break;
                    }
                }
                int index = indexSkh(trangBi.template.id, listitem) + 1;
                if (index == listitem.length) {
                    Service.gI().sendThongBaoOK(player, "Max rồi DMM!!!");
                    return;
                }
                newItem = ItemService.gI().createNewItem((short) (listitem[index]));
            }
            for (Item.ItemOption op : trangBi.itemOptions) {
                if (op.optionTemplate.id >= 127 && op.optionTemplate.id <= 144 || op.optionTemplate.id >= 241) {
                    newItem.itemOptions.add(new Item.ItemOption(op.optionTemplate.id, op.param));
                }
            }

            if (Util.isTrue(tile, 100)) {
                RewardService.gI()
                        .initBaseOptionClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);
                InventoryService.gI()
                        .addItemBag(player, newItem);
                InventoryService.gI()
                        .subQuantityItemsBag(player, trangBi, 1);
                 InventoryService.gI()
                    .subQuantityItemsBag(player, tv, qualityTv);
            InventoryService.gI()
                    .subQuantityItemsBag(player, co4la, quality4la);
            InventoryService.gI()
                    .subQuantityItemsBag(player, daKichHoat, qualityda);
            InventoryService.gI()
                    .subQuantityItemsBag(player, dns, qualityNs);
            CombineService.gI()
                    .sendEffectSuccessCombine(player);
            InventoryService.gI()
                    .sendItemBag(player);
            Service.gI()
                    .sendMoney(player);
            CombineService.gI()
                    .reOpenItemCombine(player);
            } else {
                Service.gI().sendThongBaoOK(player, "Xit!!!");
                InventoryService.gI()
                    .subQuantityItemsBag(player, tv, qualityTv);
            InventoryService.gI()
                    .subQuantityItemsBag(player, co4la, quality4la);
            InventoryService.gI()
                    .subQuantityItemsBag(player, daKichHoat, qualityda);
            InventoryService.gI()
                    .subQuantityItemsBag(player, dns, qualityNs);
             InventoryService.gI()
                    .sendItemBag(player);
            Service.gI()
                    .sendMoney(player);
            CombineService.gI()
                    .reOpenItemCombine(player);
            }
//            InventoryService.gI()
//                    .subQuantityItemsBag(player, tv, qualityTv);
//            InventoryService.gI()
//                    .subQuantityItemsBag(player, co4la, quality4la);
//            InventoryService.gI()
//                    .subQuantityItemsBag(player, daKichHoat, qualityda);
//            InventoryService.gI()
//                    .subQuantityItemsBag(player, dns, qualityNs);
//            CombineService.gI()
//                    .sendEffectSuccessCombine(player);
//            InventoryService.gI()
//                    .sendItemBag(player);
//            Service.gI()
//                    .sendMoney(player);
//            CombineService.gI()
//                    .reOpenItemCombine(player);
        }
    }

    public static int indexSkh(int itemId, short[] listId) {
        for (int i = 0; i < listId.length; i++) {
            if (itemId == listId[i]) {
                if (i >= listId.length) {
                    return listId.length - 1;
                }
                return i;
            }
        }
        return -1;
    }
}
