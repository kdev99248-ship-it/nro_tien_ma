package npc.npc_manifest;

import config.EventConfig;
import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;

public class NoiBanh extends Npc {

    // Item IDs
    private static final int THOI_VANG = 457;
    private static final int COM_NEP = 1214;
    private static final int LA_DONG = 1217;
    private static final int SOI_COI = 1218;
    private static final int BANH_CHUNG = 1219;

    public NoiBanh(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (EventConfig.LUNAR_NEW_YEAR) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Nồi bánh chưng đang sôi sùng sục!\n"
                                + "Nguyên liệu cần:\n"
                                + "- 10 Thỏi vàng\n"
                                + "- 5 Cơm nếp\n"
                                + "- 5 Lá dong\n"
                                + "- 5 Sợi cói\n"
                                + "Bạn muốn nấu bánh chưng không?",
                        "Nấu bánh\nchưng", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                if (EventConfig.LUNAR_NEW_YEAR && select == 0) {
                    nauBanhChung(player);
                }
            }
        }
    }

    private void nauBanhChung(Player player) {
        // Kiểm tra ô trống
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy");
            return;
        }

        // Tìm nguyên liệu
        Item thoiVang = InventoryService.gI().findItemBag(player, THOI_VANG);
        Item comNep = InventoryService.gI().findItemBag(player, COM_NEP);
        Item laDong = InventoryService.gI().findItemBag(player, LA_DONG);
        Item soiCoi = InventoryService.gI().findItemBag(player, SOI_COI);

        // Kiểm tra đủ nguyên liệu
        if (thoiVang == null || thoiVang.quantity < 10) {
            Service.gI().sendThongBao(player, "Bạn cần 10 thỏi vàng");
            return;
        }
        if (comNep == null || comNep.quantity < 5) {
            Service.gI().sendThongBao(player, "Bạn cần 5 cơm nếp");
            return;
        }
        if (laDong == null || laDong.quantity < 5) {
            Service.gI().sendThongBao(player, "Bạn cần 5 lá dong");
            return;
        }
        if (soiCoi == null || soiCoi.quantity < 5) {
            Service.gI().sendThongBao(player, "Bạn cần 5 sợi cói");
            return;
        }

        // Trừ nguyên liệu
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, 10);
        InventoryService.gI().subQuantityItemsBag(player, comNep, 5);
        InventoryService.gI().subQuantityItemsBag(player, laDong, 5);
        InventoryService.gI().subQuantityItemsBag(player, soiCoi, 5);

        // Tạo bánh chưng
        Item banhChung = ItemService.gI().createNewItem((short) BANH_CHUNG);
        InventoryService.gI().addItemBag(player, banhChung);
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendThongBao(player, "Nấu bánh chưng thành công!");
    }
}
