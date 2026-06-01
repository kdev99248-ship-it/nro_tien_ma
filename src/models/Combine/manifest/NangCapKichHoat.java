package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import models.Combine.CombineService;
import player.Player;
import server.Manager;
import services.InventoryService;
import services.ItemService;
import services.RewardService;
import services.Service;
import utils.Util;

import java.util.Random;

public class NangCapKichHoat {

    public static boolean isDoThanLinh(Item item) {
    if (item == null || item.template == null) {
        return false; // Tránh lỗi NullPointerException
    }
    return item.template.id >= 555 && item.template.id <= 567;
}

    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 3) {
            boolean allThanLinh = player.combine.itemsCombine.stream().allMatch(NangCapKichHoat::isDoThanLinh);
            if (allThanLinh) {
                int goldCombie = 1_000_000_000;
                player.combine.goldCombine = goldCombie;
                String npcSay = "Sau khi nâng cấp, bạn sẽ nhận được một trang bị mới ngẫu nhiên.";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Nâng cấp\n" + Util.numberToMoney(goldCombie) + " vàng", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 3 món đồ Thần Linh để nâng cấp!");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 3 món đồ Thần Linh để nâng cấp!");
        }
    }

    public static void startCombineNew(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 3) {
            boolean allThanLinh = player.combine.itemsCombine.stream().allMatch(NangCapKichHoat::isDoThanLinh);
            if (!allThanLinh) {
                Service.gI().sendThongBaoOK(player, "Cần 3 món đồ Thần Linh để nâng cấp!");
                return;
            }

            int goldCombie = 1_000_000_000;
            player.combine.goldCombine = goldCombie;

            if (player.inventory.gold < goldCombie) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(goldCombie - player.inventory.gold) + " vàng nữa");
                Service.gI().sendMoney(player);
                return;
            }

            int itemType = player.combine.itemsCombine.get(0).template.type;
            int[] listId = getListId(player.gender, itemType);
            if (listId.length == 0) {
                Service.gI().sendThongBaoOK(player, "Không tìm thấy danh sách nâng cấp phù hợp!");
                return;
            }
            
            short newItemId = getRandomItemId(listId);
            Item newItem = ItemService.gI().createNewItem(newItemId);

            RewardService.gI().initBaseOptionClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);
            int[] selectedOptions = getSelectedOptions(newItem.template.gender, player.gender);

            if (Util.isTrue(35, 100)) {
                newItem.itemOptions.add(new ItemOption(selectedOptions[0], 0));
                newItem.itemOptions.add(new ItemOption(selectedOptions[1], 0));
            } else if(Util.isTrue(35,100)) {
                if (Util.isTrue(35, 100)) {
                    newItem.itemOptions.add(new ItemOption(selectedOptions[2], 0));
                    newItem.itemOptions.add(new ItemOption(selectedOptions[3], 0));
                } else {
                    newItem.itemOptions.add(new ItemOption(selectedOptions[4], 0));
                    newItem.itemOptions.add(new ItemOption(selectedOptions[5], 0));
                }
            }else{
                 newItem.itemOptions.add(new ItemOption(selectedOptions[6], 0));
                 newItem.itemOptions.add(new ItemOption(selectedOptions[7], 0));
                 newItem.itemOptions.add(new ItemOption(selectedOptions[8], 0));
                 newItem.itemOptions.add(new ItemOption(selectedOptions[9], 0));
            }

            InventoryService.gI().addItemBag(player, newItem);
            player.combine.itemsCombine.forEach(item -> InventoryService.gI().subQuantityItemsBag(player, item, 1));
            player.inventory.gold -=goldCombie;
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }

    private static int[] getListId(int gender, int itemType) {
        if (itemType == 4) {
            return Manager.Rada;
        }
        return switch (gender) {
            case 0 -> Manager.doTraiDathd[itemType];
            case 1 -> Manager.doNamechd[itemType];
            case 2 -> Manager.doXaydahd[itemType];
            default -> new int[0];
        };
    }

    private static short getRandomItemId(int[] listId) {
        Random random = new Random();
        if (listId.length == 0) {
            throw new IllegalArgumentException("Danh sách vật phẩm trống!");
        }
        if (Util.isTrue(10, 100)) {
            return (short) listId[listId.length - 1];
        } else {
            return (short) listId[random.nextInt(listId.length - 1)];
        }
    }

    private static int[] getSelectedOptions(int itemGender, int playerGender) {
        int[] maleOptions = {129, 141, 127, 139, 128, 140, 245, 246, 247, 248};
        int[] femaleOptions = {132, 144, 131, 143, 251, 254, 237, 238, 239, 240};
        int[] otherOptions = {135, 138, 133, 136, 134, 137, 241, 242, 243, 244};

        if ((itemGender == 0 || itemGender == 3) && playerGender == 0) {
            return maleOptions;
        } else if ((itemGender == 1 || itemGender == 3) && playerGender == 1) {
            return femaleOptions;
        } else {
            return otherOptions;
        }
    }
}
