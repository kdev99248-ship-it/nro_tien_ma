package npc.npc_manifest;

import consts.ConstNpc;
import consts.ConstPlayer;
import npc.Npc;
import player.Player;
import services.NpcService;
import services.Service;
import services.TaskService;
import shop.ShopService;

public class Bulma extends Npc {

    public Bulma(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (player.gender != 0) {
                    NpcService.gI().createTutorial(player, tempId, this.avartar, "Xin lỗi cưng, chị chỉ bán đồ cho người Trái Đất");
                } else if (!player.inventory.itemsDaBan.isEmpty()) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Cậu cần trang bị gì cứ đến chỗ tôi nhé",
                            "Cửa\nhàng",
                            "Shop\n Đệ Tử",
                            "Mua lại\nvật phẩm\nđã bán [" + player.inventory.itemsDaBan.size() + "/20]");
                } else {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Cậu cần trang bị gì cứ đến chỗ tôi nhé", "Cửa\nhàng");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {
                        //Shop
                        if (player.gender == ConstPlayer.TRAI_DAT) {
                            ShopService.gI().opendShop(player, "BUNMA", true);
                        } else {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Xin lỗi cưng, chị chỉ bán đồ cho người Trái Đất", "Đóng");
                        }
                    }
                    case 1->{
                        if(player.pet!=null){
                        if(player.pet.gender ==ConstPlayer.TRAI_DAT){
                             ShopService.gI().opendShop(player, "BUNMA", true);
                        }else if(player.pet.gender ==ConstPlayer.XAYDA){
                            ShopService.gI().opendShop(player, "APPULE", true);
                        }else{
                            ShopService.gI().opendShop(player, "DENDE", true);
                        }
                    }else{
                            Service.gI().sendThongBao(player, "Cần có đệ tử để sử dụng chức năng này");
                        }
               }
                    case 2 -> {
                        if (!player.inventory.itemsDaBan.isEmpty()) {
                            ShopService.gI().opendShop(player, "ITEMS_DABAN", true);
                        }
                    }
                }
            }
        }
    }
}
