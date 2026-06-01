package npc.npc_manifest;

import npc.Npc;
import player.Player;
import consts.ConstNpc;
import services.NpcService;

public class NhaBep extends Npc {
    public NhaBep(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Chào mừng bạn đến với nhà bếp, bạn muốn chế biến món gì?",
                    "Chế biến", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        // Gửi sự kiện mở Panel Type Chế Biến cho Client (TYPE_CHE_BIEN = 30)
                        // TODO: Gọi hàm mở shop/panel với type mới
                        // Tạm thời send msg mở panel
                        openPanelCheBien(player);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void openPanelCheBien(Player player) {
        // Implement gửi thông báo mở loại Panel custom
        // Hoặc tạo một function trong ShopService hoặc NpcService
        // Format giống như gửi mở rương / item.
        // Phải viết phương thức mở panel trên NpcService hoặc player.
        // Tạm thời, gọi Message hoặc NpcService
        // Thông tin để gửi openPanel tuỳ thuộc vào cách hệ thống gửi menu / list
        NpcService.gI().sendPanelCheBien(player);
    }
}
