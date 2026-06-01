package npc.npc_manifest;

import config.EventConfig;
import models.TetEvent.TetEventService;
import consts.ConstNpc;
import consts.ConstTask;
import item.Item;
import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import shop.ShopService;
import utils.Util;

public class BumaTH extends Npc {

    public BumaTH(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 5 -> {
                    int vip = player.vip;
                    String bacvip = "\b|3|chưa mỏ khóa vip";
                    String dmk = "\b|5|Đã mở khóa";
                    switch (vip) {
                        case 1:
                            bacvip = "\b|5| Vé quan tâm";
                            break;
                        case 2:
                            bacvip = "\b|5| Mâm 3";
                            break;
                        case 3:
                            bacvip = "\b|5| Mâm 2";
                            break;
                        case 4:
                            bacvip = "\b|5| Mâm 1";
                            break;
                    }
                    if (vip == 0) {
                        createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chan Quỹ Giúp duy trì game và giúp ngươi được buồng trưởng quan tâm hơn\n"
                                        + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n",
                                "Mua vip");
                    } else if (vip == 1) {
                        createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chan Quỹ Giúp duy trì game và giúp ngươi được buồng trưởng quan tâm hơn\n"
                                        + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                                        + "\b|1| Tăng 20 TNSM Cho đệ tử và sư phụ" + dmk + " \n",
                                "Mua vip");
                    } else if (vip == 2) {
                        createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chan Quỹ Giúp duy trì game và giúp ngươi được buồng trưởng quan tâm hơn\n"
                                        + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                                        + "\b|1| Tăng x2 thỏi vàng lụm được và tỷ lệ đập đồ Cho đệ tử và sư phụ " + dmk
                                        + " \n"
                                        + "\b|1| Tăng x2 sản thỏi vàng lụm được và tỷ lệ đập đồ  " + dmk + " \n",
                                "Mua vip");
                    } else if (vip == 3) {
                        createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chan Quỹ Giúp duy trì game và giúp ngươi được buồng trưởng quan tâm hơn\n"
                                        + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                                        + "\b|1| Tăng 20 TNSM Cho đệ tử và sư phụ " + dmk + " \n"
                                        + "\b|1| Tăng x2 thỏi vàng lụm được và tỷ lệ đập đồ  " + dmk + " \n"
                                        + "\b|1| SHOP VIP " + dmk + " \n",
                                "Mua vip", "SHOP VIP");
                    } else {
                        createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chan Quỹ Giúp duy trì game và giúp ngươi được buồng trưởng quan tâm hơn\n"
                                        + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                                        + "\b|1| Tăng 20 TNSM Cho đệ tử và sư phụ " + dmk + " \n"
                                        + "\b|1| Tăng x2 thỏi vàng lụm được và tỷ lệ đập đồ  " + dmk + " \n"
                                        + "\b|1| SHOP VIP " + dmk + " \n"
                                        + "\b|1| Máp Vùng Đất Hủy Diệt" + dmk + "\n",
                                "Mua vip", "SHOP VIP", "Vùng Đất Hủy Diệt");
                    }
                }
                case 169 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Ra khỏi ngôi làng này sẽ gặp ngọn núi ngũ hành sơn",
                            "Về\n Đảo Kame", "Đóng");
                }
                case 42, 43, 44 -> {
                    if (EventConfig.LUNAR_NEW_YEAR) {
                        createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chào mừng bạn đến với sự kiện Tết!\nBạn muốn đổi mâm ngũ quả hay mua sắm?",
                                "Đổi mâm\nngũ quả", "Cửa hàng", "Đóng");
                    }
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
                    case 5 -> {
                        int vip = player.vip;
                        String bacvip = "\b|3|chưa mỏ khóa vip";
                        String dmk = "\b|5|Đã mở khóa";
                        switch (vip) {
                            case 1:
                                bacvip = "\b|5| Vé quan tâm";
                                break;
                            case 2:
                                bacvip = "\b|5| Mâm 3";
                                break;
                            case 3:
                                bacvip = "\b|5| Mâm 2";
                                break;
                            case 4:
                                bacvip = "\b|5| Mâm 1";
                                break;
                        }
                        if (select == 0) {
                            createOtherMenu(player, 1,
                                    "Chan Quỹ Giúp duy trì game và giúp ngươi được buồng trưởng quan tâm hơn\n"
                                            + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n",
                                    "Quan tâm", "Mâm 3 ", "Mâm 2", "Mâm 1");
                        }
                        if (select == 1) {
                            ShopService.gI().opendShop(player, "Shop_Vip", false);
                        }
                        if (select == 2) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 169, 50, 384);
                        }
                    }
                    case 169 -> {
                        if (select == 0) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 5, Util.nextInt(700, 800), 432);
                        }
                    }
                    case 42, 43, 44 -> {
                        if (EventConfig.LUNAR_NEW_YEAR) {
                            if (select == 0) { // Đổi mâm ngũ quả
                                exchangeMamNguQua(player);
                            } else if (select == 1) { // Cửa hàng
                                Service.gI().sendThongBao(player, "Cửa hàng sẽ sớm được ra mắt!");
                            }
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 1) {
                int vip = player.vip;
                String bacvip = "\b|3|chưa mỏ khóa vip";
                String dmk = "\b|5|Đã mở khóa";
                switch (vip) {
                    case 1:
                        bacvip = "\b|5| Vé quan tâm";
                        break;
                    case 2:
                        bacvip = "\b|5| Mâm 3";
                        break;
                    case 3:
                        bacvip = "\b|5| Mâm 2";
                        break;
                    case 4:
                        bacvip = "\b|5| Mâm 1";
                        break;
                }
                switch (select) {
                    case 0 -> createOtherMenu(player, 11,
                            "Đóng vé quan tâm giúp ngươi next tới nhiệm vụ Tiêu diệt KUKU\n"
                                    + "\n\b|7|Bạn đang có :" + player.getSession().cash + " VND\n|4|"
                                    + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                                    + " \b|3| Mua ngay 50.000 Vnđ\n"
                                    + "\b|1| Hoặc ngươi có thể mở khóa free sau khi hoàn thành Nhiệm vụ Fide\n"
                                    + "\b|5| Tăng 20TNSM, 10% hút HpKI cho Đệ tử \n"
                                    + "\b|5| Mở Khóa Danh hiệu Vip cho đệ và nhận x10 3s\n",
                            "chan ngay ", "Chan Free");
                    case 1 -> createOtherMenu(player, 12, "Đóng mâm 3 giúp ngươi:\n"
                            + "\b|1| Hoặc ngươi có thể mở khóa free sau khi hoàn thành Nhiệm vụ Xên Hoàn Thiện\n"
                            + "\n\b|7|Bạn đang có :" + player.getSession().cash + " VND\n|4|"
                            + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                            + "\b|3| Mua Ngay: " + (100_000 - ((player.vip > 0) ? 50_000 : 0)) + "\n"
                            + "\b|1| Tăng 50% sát thương Kamejoko cho đệ\n"
                            + "\b|5| Tăng x2 Tỷ lệ đập đồ và sản lượng vàng từ quái \n"
                            + "\b|5| Mở Khóa Danh hiệu Vip 2 và nhận x1 Giáp luyện tập cấp 4\n",
                            "chan ngay ", "Chan Free");
                    case 2 -> createOtherMenu(player, 13, "Đóng mâm 2 giúp ngươi:\n"
                            + "\n\b|7|Bạn đang có :" + player.getSession().cash + " VND\n|4|"
                            + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                            + "\b|3| Mua Ngay: " + (150_000 - ((player.vip > 0) ? 50_000 * player.vip : 0)) + "\n"
                            + "\b|5|Mở Khóa Shop VIP\n"
                            + "\b|5| Kháng lạnh cho đệ\n"
                            + "\b|5| Mở Khóa Danh hiệu vip cho đệ và x10 Rương kích hoạt vải thô Random\n",
                            "chan ngay", "Đóng");
                    case 3 -> createOtherMenu(player, 14, "Đóng mâm 1 giúp ngươi:\n"
                            + "\n\b|7|Bạn đang có :" + player.getSession().cash + " VND\n|4|"
                            + "\b|1|Cấp vip ngươi hiện tại: " + bacvip + " \n"
                            + "\b|3| Mua Ngay: " + (200_000 - ((player.vip > 0) ? 50_000 * player.vip : 0)) + "\n"
                            + "\b|5|Mở Khóa Map Vip\n"
                            + "\b|5|Tăng 10% SĐ,HP, KI cho đệ\n"
                            + "\b|5| Mở Khóa Danh hiệu Vip cho đệ và nhận x1 Cánh Bán thần\n",
                            "chan ngay ", "Đóng");
                }
            } else if (player.iDMark.getIndexMenu() == 11) {
                switch (select) {
                    case 0 -> {
                        if (player.vip > 0) {
                            Service.gI().sendThongBao(player, "Ngáo ngơ quan tâm rồi");
                            return;
                        }
                        if (player.getSession().cash < 50000) {
                            Service.gI().sendThongBao(player, "50k của ta đâu!!");
                            return;
                        }
                        if (PlayerDAO.subcash(player, 50000)) {
                            if (TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
                                player.playerTask.taskMain.id = 20;
                                player.playerTask.taskMain.index = 0;
                                TaskService.gI().sendNextTaskMain(player);
                            }
                            player.vip = 1;
                            Item bas = ItemService.gI().createNewItem((short) 16, 10);
                            InventoryService.gI().addItemBag(player, bas);
                            InventoryService.gI().sendItemBag(player);
                            ChangeMapService.gI().exitMap(player.pet);
                            Service.gI().sendThongBao(player, "Đóng quan tâm thành công và nhận x10 ngọc rồng 3 sao");
                        }
                    }
                    case 1 -> {
                        if (player.vip > 0) {
                            Service.gI().sendThongBao(player, "Ngáo ngơ quan tâm  rồi");
                            return;
                        }
                        if (TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
                            Service.gI().sendThongBao(player, "Cần hoàn thành nhiệm vụ fide để mở khóa free");
                            return;
                        } else {
                            player.vip = 1;
                            Item bas = ItemService.gI().createNewItem((short) 16, 110);
                            InventoryService.gI().addItemBag(player, bas);
                            InventoryService.gI().sendItemBag(player);
                            ChangeMapService.gI().exitMap(player.pet);
                            Service.gI().sendThongBao(player, "Đóng quan tâm thành công và nhận x10 ngọc rồng 3sao");
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 12) {
                switch (select) {
                    case 0 -> {
                        if (player.vip > 1) {
                            Service.gI().sendThongBao(player, "Ngáo ngơ Mâm 3 rồi");
                            return;
                        }
                        if (player.getSession().cash < 100_000 - player.vip * 50_000) {
                            Service.gI().sendThongBao(player,
                                    "còn thiếu " + (100_000 - player.vip * 50_000) + " Để nâng mâm");
                            return;
                        }
                        int cashsub = 100_000 - player.vip * 50_000;
                        if (PlayerDAO.subcash(player, cashsub)) {
                            player.vip = 2;
                            Item bas = ItemService.gI().createNewItem((short) 1745, 1);
                            bas.itemOptions.add(new Item.ItemOption(9, 100));
                            InventoryService.gI().addItemBag(player, bas);
                            InventoryService.gI().sendItemBag(player);
                            ChangeMapService.gI().exitMap(player.pet);
                            Service.gI().sendThongBao(player, "Nâng Mâm 3 thành công và nhận x1 Giáp Luyện tập 4");
                        }
                    }
                    case 1 -> {
                        if (player.vip > 1) {
                            Service.gI().sendThongBao(player, "Ngáo ngơ Mâm 3 rồi");
                            return;
                        }
                        if (TaskService.gI().getIdTask(player) < ConstTask.TASK_28_3) {
                            Service.gI().sendThongBao(player,
                                    "Cần hoàn thành nhiệm vụ Xên Hoàn thiện để lên mâm 3 Free");
                            return;
                        } else {
                            player.vip = 2;
                            Item bas = ItemService.gI().createNewItem((short) 1745, 1);
                            bas.itemOptions.add(new Item.ItemOption(9, 100));
                            InventoryService.gI().addItemBag(player, bas);
                            InventoryService.gI().sendItemBag(player);
                            ChangeMapService.gI().exitMap(player.pet);
                            Service.gI().sendThongBao(player, "Nâng Mâm 3 thành công và nhận x1 Giáp Luyện tập 4");
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 13) {
                switch (select) {
                    case 0 -> {
                        if (player.vip > 2) {
                            Service.gI().sendThongBao(player, "Ngáo ngơ Mâm 2 rồi");
                            return;
                        }
                        if (player.getSession().cash < 150_000 - player.vip * 50_000) {
                            Service.gI().sendThongBao(player,
                                    "còn thiếu " + (150_000 - player.vip * 50_000) + " Để nâng mâm");
                            return;
                        }
                        int cashsub = 150_000 - player.vip * 50_000;
                        if (PlayerDAO.subcash(player, cashsub)) {
                            player.vip = 3;
                            Item bas = ItemService.gI().createNewItem((short) 1536, 10);
                            InventoryService.gI().addItemBag(player, bas);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player,
                                    "Nâng Mâm 2 thành công và nhận x10 Hòm kích hoạt vải thô");
                            ChangeMapService.gI().exitMap(player.pet);
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == 14) {
                switch (select) {
                    case 0 -> {
                        if (player.vip > 3) {
                            Service.gI().sendThongBao(player, "Ngáo ngơ Mâm 1 rồi");
                            return;
                        }
                        if (player.getSession().cash < 200_000 - player.vip * 50_000) {
                            Service.gI().sendThongBao(player,
                                    "còn thiếu " + (200_000 - player.vip * 50_000) + " Để nâng mâm");
                            return;
                        }
                        int cashsub = 100_000 - player.vip * 50_000;
                        if (PlayerDAO.subcash(player, cashsub)) {
                            player.vip = 4;
                            Item bas = ItemService.gI().createNewItem((short) 1638, 1);
                            ChangeMapService.gI().exitMap(player.pet);
                            bas.itemOptions.add(new Item.ItemOption(50, 13));
                            bas.itemOptions.add(new Item.ItemOption(77, 13));
                            bas.itemOptions.add(new Item.ItemOption(103, 13));
                            bas.itemOptions.add(new Item.ItemOption(5, 10));
                            InventoryService.gI().addItemBag(player, bas);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng Mâm 1 thành công và nhận x1 Cánh bán thần");
                        }
                    }
                }
            }
        }
    }

    private void exchangeMamNguQua(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đầy");
            return;
        }

        Item thoiVang = InventoryService.gI().findItemBag(player, 457);
        Item mangCau = InventoryService.gI().findItemBag(player, TetEventService.MANG_CAU);
        Item dua = InventoryService.gI().findItemBag(player, TetEventService.DUA);
        Item duDu = InventoryService.gI().findItemBag(player, TetEventService.DU_DU);
        Item xoai = InventoryService.gI().findItemBag(player, TetEventService.XOAI);
        Item sung = InventoryService.gI().findItemBag(player, TetEventService.SUNG);

        if (thoiVang != null && thoiVang.quantity >= 5
                && mangCau != null && mangCau.quantity >= 10
                && dua != null && dua.quantity >= 10
                && duDu != null && duDu.quantity >= 10
                && xoai != null && xoai.quantity >= 10
                && sung != null && sung.quantity >= 10) {

            InventoryService.gI().subQuantityItemsBag(player, thoiVang, 5);
            InventoryService.gI().subQuantityItemsBag(player, mangCau, 10);
            InventoryService.gI().subQuantityItemsBag(player, dua, 10);
            InventoryService.gI().subQuantityItemsBag(player, duDu, 10);
            InventoryService.gI().subQuantityItemsBag(player, xoai, 10);
            InventoryService.gI().subQuantityItemsBag(player, sung, 10);

            Item mamNguQua = ItemService.gI().createNewItem((short) TetEventService.MAM_NGU_QUA);
            InventoryService.gI().addItemBag(player, mamNguQua);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendThongBao(player, "Đổi mâm ngũ quả thành công");
        } else {
            Service.gI().sendThongBao(player,
                    "Bạn cần 5 thỏi vàng và 10 quả mỗi loại (Mãng cầu, Dừa, Đu đủ, Xoài, Sung)");
        }
    }
}
