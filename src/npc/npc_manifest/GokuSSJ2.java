package npc.npc_manifest;

/**
 *
 * @author EMTI
 */
import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;

public class GokuSSJ2 extends Npc {

    public GokuSSJ2(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy cố gắng luyện tập\nThu thập 999 bí kiếp để đổi trang phục Yardrat nhé!",
                    "Nhận\nthưởng", "OK");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (select == 0) {
                int soluong = InventoryService.gI().getParam(player, 31, 590);
                if (soluong >= 999) {
                    InventoryService.gI().subParamItemsBag(player, 590, 31, 999);
                    Item yardart = ItemService.gI().createNewItem((short) (player.gender + 592));
                    yardart.itemOptions.add(new Item.ItemOption(47, 400));
                    yardart.itemOptions.add(new Item.ItemOption(97, 10));
                    
                   
                    yardart.itemOptions.add(new Item.ItemOption(108, 10));
                    InventoryService.gI().addItemBag(player, yardart);
                    InventoryService.gI().sendItemBag(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được võ phục của người Yardrat");
                }
            }
            if(select ==1){
                Service.gI().sendThongBao(player, "Chưa được các con vợ!!!");
            }
        }
    }
}
