package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import player.Player;
import services.InventoryService;
import services.Service;

public class EpSaoTrangBi {

    public static int getGem(int star) {
        return switch (star) {
            case 7 ->
                200;
            case 8 ->
                300;
            default ->
                10;
        };
    }

    public static void showInfoCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị có lỗ sao pha lê và 1 loại ngọc để ép vào.");
            return;
        }
        Item trangBi = null;
        Item daPhaLe = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.canPhaLeHoa()) {
                trangBi = item;
            } else if (item.isDaPhaLeEpSao()) {
                daPhaLe = item;
            }
        }
        if (trangBi == null || !trangBi.isNotNullItem() || daPhaLe == null || !daPhaLe.isNotNullItem()) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị có lỗ sao pha lê và 1 loại ngọc để ép vào.");
            return;
        }
        int star = trangBi.getOptionParam(102);
        int starEmpty = trangBi.getOptionParam(107);
       
        
        
        if (star >= starEmpty) {
            Service.gI().sendDialogMessage(player, "Cần 1 trang bị có lỗ sao pha lê và 1 loại ngọc để ép vào.");
            return;
        }
        
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append(trangBi.template.name).append("\n");
        text.append(ConstFont.BOLD_DARK).append(star >= 7 ? trangBi.getOptionInfoCuongHoa(daPhaLe) : trangBi.getOptionInfo(daPhaLe)).append("\n");
        text.append(player.inventory.getGemAndRuby() < getGem(star) ? ConstFont.BOLD_RED : ConstFont.BOLD_BLUE).append("Cần 10 ngọc");
        if (player.inventory.getGemAndRuby() < getGem(star)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\n" + (getGem(star) - player.inventory.getGemAndRuby()) + " ngọc");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Nâng cấp\n" + getGem(star) + " ngọc", "Từ chối");
    }

    public static void epSaoTrangBi(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }
        Item trangBi = null;
        Item daPhaLe = null;
        for (Item item : player.combine.itemsCombine) {
            if (item.canPhaLeHoa()) {
                trangBi = item;
            } else if (item.isDaPhaLeEpSao()) {
                daPhaLe = item;
            }
        }
        if (trangBi == null || !trangBi.isNotNullItem() || daPhaLe == null || !daPhaLe.isNotNullItem()) {
            return;
        }
        int star = trangBi.getOptionParam(102);
        int starEmpty = trangBi.getOptionParam(107);
        int cuongHoa = trangBi.getOptionParam(228);
        if (star >= starEmpty
                ||  star >= 8 && cuongHoa < star + 1
                || player.inventory.getGemAndRuby() < getGem(star)) {
            return;
        }
        trangBi.addOptionParam(102, 1);
        if (star >= 7) {
            
            trangBi.itemOptions.add(new Item.ItemOption(daPhaLe.getOptionDaPhaLe().optionTemplate.id, daPhaLe.getOptionDaPhaLe().param));
        } else {
            trangBi.addOptionParam(daPhaLe.getOptionDaPhaLe().optionTemplate.id, daPhaLe.getOptionDaPhaLe().param);
        }
        player.inventory.subGemAndRuby(getGem(star));
        InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);
        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
