package npc.npc_manifest;

/**
 *
 * @author EMTI
 */
import consts.ConstNpc;
import item.Item;
import models.SuperRank.SuperRankManager;
import models.SuperRank.SuperRankService;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.Service;
import services.func.ChangeMapService;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class Billbn extends Npc {

    public Billbn(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 44 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Địa ngục là nơi khăc nhiệt ngươi hãy cân nhắc nếu muốn tới!!!\n"
                            ,
                            "Tới Địa Ngục"
                            
                    //, "Nhiệm vụ\nhộ tống", "Từ chối", "Nhận\nthưởng"
                    );
                } case 174 ->{
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Trở về thôi!!!",
                            "Quay Về", "Đóng");
                }

                default ->
                    super.openBaseMenu(player);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (mapId) {
                    case 44 -> {
                        if (select == 0) {
                            if (player.nPoint.power <= 100_000_000_000L) {
                                    Service.gI().sendThongBao(player, "Yêu cầu sức mạnh đạt 100 tỉ");
                                    return;
                                }
                            ChangeMapService.gI().changeMapNonSpaceship(player, 174, 346, 180);
                        }
                        
                        

                    }
                    case 174 ->{
                        if (select == 0) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 44, Util.nextInt(700, 800), 432);
                        }
                    }
                }

            }
        }
    }
}
