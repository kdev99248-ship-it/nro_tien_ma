package npc.npc_manifest;

import npc.Npc;
import player.Player;
import shop.ShopService;

public class ToriBot extends Npc {

    public ToriBot(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, 1, "Cửa hàng chuyên kì trân dị bảo không ở đâu có được", "Cửa hàng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (select) {
                case 0 -> ShopService.gI().opendShop(player, "TORI_BOT", true);
            }
        }
    }
}
