package npc.npc_manifest;

import npc.Npc;
import player.Player;
import shop.ShopService;
import consts.ConstNpc;

public class Velra extends Npc {
    public Velra(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            // Hiển thị menu khi người chơi click vào NPC
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Chào cậu, tôi có bán vài món đồ hịn đây!",
                    "Cửa hàng", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0: // Khi người chơi chọn "Cửa hàng"
                        // "Hạt giống" phải khớp với tag_name trong database
                        ShopService.gI().opendShop(player, "Linh thú", false);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}