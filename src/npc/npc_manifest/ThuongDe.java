package npc.npc_manifest;
import boss.BossID;


/**
 *
 * @author EMTI
 */
import consts.ConstNpc;
import item.Item;
import models.Training.TrainingService;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.Service;
import services.func.ChangeMapService;
import services.func.LuckyRound;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class ThuongDe extends Npc {

    public ThuongDe(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 45 -> {
                    if (player.clan != null && player.clan.ConDuongRanDoc != null && player.joinCDRD
                            && player.clan.ConDuongRanDoc.allMobsDead && !player.talkToThuongDe) {
                        Service.gI().sendThongBao(player, "Hãy xuống gặp thần mèo Karin");
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy xuống gặp thần mèo Karin", "OK");
                        return;
                    }
                    switch (player.levelLuyenTap) {
                        case 2 ->
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Pôpô là đệ tử của ta, luyện tập với Pôpô con sẽ có thêm nhiều kinh nghiệm\nđánh bại được Pôpô ta sẽ dạy võ công cho con",
                                    player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động",
                                    "Tập luyện\nvới\nMr.PôPô", "Thách đấu\nMr.PôPô", "Đến\nKaio", "Quay ngọc\nMay mắn");
                        case 3 ->
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Từ nay con sẽ là đệ tử của ta. Ta sẽ truyền cho con tất cả tuyệt kĩ",
                                    player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động",
                                    "Tập luyện\nvới\nThượng Đế", "Thách đấu\nThượng Đế", "Đến\nKaio",
                                    "Quay ngọc\nMay mắn");
                        default ->
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Con đã mạnh hơn ta, ta sẽ chỉ đường cho con đến Kaio\nđể gặp thần Vũ Trụ Phương Bắc\nNgài là thần cai quản vũ trụ này, hãy theo ngài ấy học võ công.",
                                    player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động",
                                    "Tập luyện\nvới\nMr.PôPô", "Tập luyện\nvới\nThượng Đế", "Đến\nKaio",
                                    "Quay ngọc\nMay mắn");
                    }
                }
                case 141 ->
                    this.createOtherMenu(player, 0,
                            "Hãy nắm lấy tay ta mau!", "về\nthần điện");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 45 -> {
                    if (player.iDMark.isBaseMenu()) {
                        switch (select) {
                            case 0 -> {
                                if (player.clan != null && player.clan.ConDuongRanDoc != null && player.joinCDRD
                                        && player.clan.ConDuongRanDoc.allMobsDead && !player.talkToThuongDe) {
                                    player.talkToThuongDe = true;
                                    return;
                                }
                                if (player.dangKyTapTuDong) {
                                    player.dangKyTapTuDong = false;
                                    NpcService.gI().createTutorial(player, tempId, avartar,
                                            "Con đã hủy thành công đăng ký tập tự động\ntừ giờ con muốn tập Offline hãy tự đến đây trước");
                                    return;
                                }
                                this.createOtherMenu(player, 2001,
                                        "Đăng ký để mỗi khi Offline quá 30 phút, con sẽ được tự động luyện tập với tốc độ 1280 sức mạnh mỗi phút",
                                        "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
                            }
                            case 1 -> {
                                switch (player.levelLuyenTap) {
                                    case 3 ->
                                        this.createOtherMenu(player, 2002,
                                                "Con có chắc muốn tập luyện ?\nTập luyện với ta sẽ tăng 160 sức mạnh mỗi phút",
                                                "Đồng ý\nluyện tập", "Không\nđồng ý");
                                    default ->
                                        this.createOtherMenu(player, 2002,
                                                "Con có chắc muốn tập luyện ?\nTập luyện với Mr.PôPô sẽ tăng 80 sức mạnh mỗi phút",
                                                "Đồng ý\nluyện tập", "Không\nđồng ý");
                                }
                            }
                            case 2 -> {
                                switch (player.levelLuyenTap) {
                                    case 2 ->
                                        this.createOtherMenu(player, 2003,
                                                "Con có chắc muốn thách đấu ?\nNếu thắng Mr.PôPô sẽ được tập với ta, tăng 160 sức mạnh mỗi phút",
                                                "Đồng ý\ngiao đấu", "Không\nđồng ý");
                                    case 3 ->
                                        this.createOtherMenu(player, 2003,
                                                "Con có chắc muốn thách đấu ?\nNếu thắng được ta, con sẽ được học võ với người mạnh hơn ta để tăng đến 320 sức mạnh mỗi phút",
                                                "Đồng ý\ngiao đấu", "Không\nđồng ý");
                                    default ->
                                        this.createOtherMenu(player, 2003,
                                                "Con có chắc muốn tập luyện ?\nTập luyện với ta sẽ tăng 160 sức mạnh mỗi phút",
                                                "Đồng ý\nluyện tập", "Không\nđồng ý");
                                }
                            }
                            case 3 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 48, -1, 354);
                            case 4 ->
                                this.createOtherMenu(player, ConstNpc.MENU_CHOOSE_LUCKY_ROUND,
                                        "Con muốn làm gì nào?", "Quay bằng\nVàng",
                                        "Quay Bằng\nNgọc Xanh",
                                        "Quay bằng\nThỏi vàng",
                                        "Rương phụ\n("
                                                + (player.inventory.itemsBoxCrackBall.size()
                                                        - InventoryService.gI().getCountEmptyListItem(
                                                                player.inventory.itemsBoxCrackBall))
                                                + " món)",
                                        "Xóa hết\ntrong rương");
                        }
                    } else if (player.iDMark.getIndexMenu() == 2001) {
                        switch (select) {
                            case 0 ->
                                NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.TAP_TU_DONG);
                            case 1 -> {
                                player.mapIdDangTapTuDong = mapId;
                                player.dangKyTapTuDong = true;
                                NpcService.gI().createTutorial(player, tempId, avartar,
                                        "Từ giờ, quá 30 phút Offline con sẽ được tự động luyện tập");
                            }
                        }

                    } else if (player.iDMark.getIndexMenu() == 2002) {
                        switch (player.levelLuyenTap) {
                            case 3 ->
                                TrainingService.gI().callBoss(player, BossID.THUONG_DE, false);
                            default ->
                                TrainingService.gI().callBoss(player, BossID.MRPOPO, false);
                        }
                    } else if (player.iDMark.getIndexMenu() == 2003) {
                        switch (player.levelLuyenTap) {
                            case 2 ->
                                TrainingService.gI().callBoss(player, BossID.MRPOPO, true);
                            case 3 ->
                                TrainingService.gI().callBoss(player, BossID.THUONG_DE, true);
                            default ->
                                TrainingService.gI().callBoss(player, BossID.THUONG_DE, false);
                        }
                    } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_CHOOSE_LUCKY_ROUND) {
                        switch (select) {
                            case 0 ->
                                LuckyRound.gI().openCrackBallUI(player, (byte) 0);
                            case 1 ->
                                LuckyRound.gI().openCrackBallUI(player, LuckyRound.USING_RUBY);
                            case 2 ->
                                LuckyRound.gI().openCrackBallUI(player, LuckyRound.USING_TICKET);
                            case 3 ->
                                ShopService.gI().opendShop(player, "ITEMS_LUCKY_ROUND", true);
                            case 4 ->
                                NpcService.gI().createMenuConMeo(player,
                                        ConstNpc.CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND, this.avartar,
                                        "Con có chắc muốn xóa hết vật phẩm trong rương phụ? Sau khi xóa "
                                                + "sẽ không thể khôi phục!",
                                        "Đồng ý", "Hủy bỏ");
                            case 5 ->
                                TopService.showListTop(player, 9);
                            case 6 ->
                                this.createOtherMenu(player,
                                        1,
                                        "Nhận quà tích lũy lượt quay ở đây!!! \n"
                                                + "Số lượt quay hiện tại: " + player.pointtet + " Lượt\n"
                                                + "Mốc hiện tại đã nhận: " + player.event.getEventPointQuyLao(),
                                        "Xem Mốc Quà", "Hủy bỏ");

                        }
                    }
                    switch (player.iDMark.getIndexMenu()) {
                        case 1 -> {
                            switch (select) {
                                case 0 ->
                                    this.createOtherMenu(player,
                                            2,
                                            "Nhận quà tích lũy lượt quay ở đây!!! \n"
                                                    + "Số lượt quay hiện tại: " + player.pointtet + " Lượt\n"
                                                    + "Mốc Nhận đã nhận " + player.event.getEventPointQuyLao() + " \n",
                                            "Mốc 100",
                                            "Mốc 200",
                                            "Mốc 500",
                                            "Mốc 1000",
                                            "Mốc 1500",
                                            "Mốc 2000",
                                            "Mốc 3000",
                                            "Mốc 5000",

                                            "Mốc 10000");

                            }
                        }
                        case 2 -> {
                            switch (select) {
                                case 0 ->
                                    this.createOtherMenu(player,
                                            100,
                                            "Quà tích lũy mốc 100 gồm!!! \n"
                                                    + "x25 dưa hấu\n"
                                                    + "x3 ngọc rồng 3s\n"
                                                    + "x10 thỏi vàng\n",
                                            "Bú vội", "Hủy bỏ");
                                case 1 ->
                                    this.createOtherMenu(player,
                                            200,
                                            "Quà tích lũy mốc 200 gồm!!! \n"
                                                    + "x5 lõi Siêu Cấp\n"
                                                    + "x5 ngọc rồng 3s\n"
                                                    + "x50 dưa hấu\n",
                                            "Bú vội", "Hủy bỏ");
                                case 2 ->
                                    this.createOtherMenu(player,
                                            501,
                                            "Quà tích lũy mốc 500 gồm!!! \n"
                                                    + "x10 Đá Ngũ sắc\n"
                                                    + "x100 dưa hấu\n"
                                                    + "x3 thẻ đen\n"
                                                    + "x20 đá bảo vệ",
                                            "Bú vội", "Hủy bỏ");
                                case 3 ->
                                    this.createOtherMenu(player,
                                            1000,
                                            "Quà tích lũy mốc 1000 gồm!!! \n"
                                                    + "Đeo lưng labubu\n"
                                                    + "400 dưa hấu\n"
                                                    + "x5 Túi Mảnh Thiên sứ",
                                            "Bú vội", "Hủy bỏ");
                                case 4 ->
                                    this.createOtherMenu(player,
                                            1500,
                                            "Quà tích lũy mốc 1500 gồm!!! \n"
                                                    + "Hộp cao cấp, mở chọn set thần linh\n"
                                                    + "600 dưa hấu\n"
                                                    + "40 đá ngũ sắc\n"
                                                    + "x10 Túi Mảnh Thiên Sứ\n"
                                                    + "5 Thẻ Đen",
                                            "Bú vội", "Hủy bỏ");
                                case 5 ->
                                    this.createOtherMenu(player,
                                            2000,
                                            "Quà tích lũy mốc 2000 gồm!!! \n"
                                                    + "Cải trang broly\n",

                                            "Broly Trái Đất", "Broly Namec", "Broly Xayda");
                                case 6 ->
                                    this.createOtherMenu(player,
                                            3000,
                                            "Quà tích lũy mốc 3000 gồm!!! \n"
                                                    + "Random 50, 100, 200k coin\n"
                                                    + "Cải trang mị nương\n"

                                            ,
                                            "Bú vội", "Hủy bỏ");
                                case 7 ->
                                    this.createOtherMenu(player,
                                            5000,
                                            "Quà tích lũy mốc 5000 gồm!!! \n"
                                                    + "Ván bay Tên Lửa cá mập\n"
                                                    + "random 100,200,500k coin\n"
                                                    + "Pet Serberus\n",
                                            "Bú vội", "Hủy bỏ");
                                case 8 ->
                                    this.createOtherMenu(player,
                                            10000,
                                            "Quà tích lũy mốc 10.000 gồm!!! \n"
                                                    + "500k coin\n"
                                                    + "Cải Trang Supper GoHan\n"
                                                    + "x99 Hộp Mảnh Thiên Sứ\n"
                                                    + "x300 Đá Bảo Vệ",
                                            "Bú vội", "Hủy bỏ");
                            }
                        }
                        case 100 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 3) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 3 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 100 && player.event.getEventPointQuyLao() == 0) {
                                        player.event.setEventPointQuyLao(100);
                                        Item duahau = ItemService.gI().createNewItem((short) 1839, 25);
                                        Item tv = ItemService.gI().createNewItem((short) 457, 10);
                                        Item bas = ItemService.gI().createNewItem((short) 16, 3);
                                        InventoryService.gI().addItemBag(player, duahau);
                                        InventoryService.gI().addItemBag(player, bas);
                                        InventoryService.gI().addItemBag(player, tv);
                                        InventoryService.gI().sendItemBag(player);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x10 " + tv.template.name);
                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được x25 " + duahau.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x3 " + bas.template.name);
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 200 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 3) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 3 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 200 && player.event.getEventPointQuyLao() == 100) {
                                        player.event.setEventPointQuyLao(200);
                                        Item duahau = ItemService.gI().createNewItem((short) 1839, 50);
                                        Item tv = ItemService.gI().createNewItem((short) 758, 5);
                                        Item bas = ItemService.gI().createNewItem((short) 16, 5);
                                        InventoryService.gI().addItemBag(player, duahau);
                                        InventoryService.gI().addItemBag(player, bas);
                                        InventoryService.gI().addItemBag(player, tv);
                                        InventoryService.gI().sendItemBag(player);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x5 " + tv.template.name);
                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được x50 " + duahau.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x5 " + bas.template.name);
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 501 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 4) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 4 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 500 && player.event.getEventPointQuyLao() == 200) {
                                        player.event.setEventPointQuyLao(500);
                                        Item duahau = ItemService.gI().createNewItem((short) 1839, 100);
                                        Item dns = ItemService.gI().createNewItem((short) 674, 10);
                                        Item theden = ItemService.gI().createNewItem((short) 1796, 3);
                                        Item dabv = ItemService.gI().createNewItem((short) 987, 20);
                                        InventoryService.gI().addItemBag(player, duahau);
                                        InventoryService.gI().addItemBag(player, theden);
                                        InventoryService.gI().addItemBag(player, dns);
                                        InventoryService.gI().addItemBag(player, dabv);
                                        InventoryService.gI().sendItemBag(player);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x10 " + dns.template.name);
                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được x25 " + duahau.template.name);
                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được x3 " + theden.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x20 " + dabv.template.name);
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 1000 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 3) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 3 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 1000 && player.event.getEventPointQuyLao() == 500) {
                                        player.event.setEventPointQuyLao(1000);
                                        Item duahau = ItemService.gI().createNewItem((short) 1839, 400);
                                        Item tts = ItemService.gI().createNewItem((short) 1173, 5);
                                        Item lbb = ItemService.gI().createNewItem((short) 1694, 1);
                                        lbb.itemOptions.add(new Item.ItemOption(50, 12));
                                        lbb.itemOptions.add(new Item.ItemOption(77, 12));
                                        lbb.itemOptions.add(new Item.ItemOption(103, 12));
                                        lbb.itemOptions.add(new Item.ItemOption(5, 6));
                                        lbb.itemOptions.add(new Item.ItemOption(117, 3));
                                        InventoryService.gI().addItemBag(player, duahau);
                                        InventoryService.gI().addItemBag(player, tts);
                                        InventoryService.gI().addItemBag(player, lbb);

                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được x400 " + duahau.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x5 " + tts.template.name);
                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được Đeo lưng " + lbb.template.name);
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 1500 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 5) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 5 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 1500 && player.event.getEventPointQuyLao() == 1000) {
                                        Item qcc = ItemService.gI().createNewItem((short) 1228, 1);
                                        player.event.setEventPointQuyLao(1500);
                                        Item duahau = ItemService.gI().createNewItem((short) 1839, 600);
                                        Item tts = ItemService.gI().createNewItem((short) 1173, 10);
                                        Item dns = ItemService.gI().createNewItem((short) 674, 40);
                                        Item theden = ItemService.gI().createNewItem((short) 1796, 5);
                                        InventoryService.gI().addItemBag(player, duahau);
                                        InventoryService.gI().addItemBag(player, qcc);
                                        InventoryService.gI().addItemBag(player, tts);
                                        InventoryService.gI().addItemBag(player, dns);
                                        InventoryService.gI().addItemBag(player, theden);
                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được x400 " + duahau.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x5 " + tts.template.name);
                                        Service.gI().sendThongBao(player,
                                                "Bạn đã nhận được x5 " + theden.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x40 " + dns.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được Hộp quà cao cấp ");
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 2000 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 2000 && player.event.getEventPointQuyLao() == 1500) {
                                        player.event.setEventPointQuyLao(2000);
                                        Item brolytraidat = ItemService.gI().createNewItem((short) 1018, 1);
                                        brolytraidat.itemOptions.add(new Item.ItemOption(50, 25));
                                        brolytraidat.itemOptions.add(new Item.ItemOption(77, 20));
                                        brolytraidat.itemOptions.add(new Item.ItemOption(103, 20));
                                        brolytraidat.itemOptions.add(new Item.ItemOption(0, 1000));
                                        brolytraidat.itemOptions.add(new Item.ItemOption(5, 10));
                                        InventoryService.gI().addItemBag(player, brolytraidat);
                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player, "Bạn đã nhận được cải trang Broly Trái Đất ");
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }
                                case 1 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 2000 && player.event.getEventPointQuyLao() == 1500) {
                                        player.event.setEventPointQuyLao(2000);
                                        Item broly = ItemService.gI().createNewItem((short) 1019, 1);
                                        broly.itemOptions.add(new Item.ItemOption(50, 20));
                                        broly.itemOptions.add(new Item.ItemOption(77, 20));
                                        broly.itemOptions.add(new Item.ItemOption(103, 25));
                                        broly.itemOptions.add(new Item.ItemOption(7, 20000));
                                        broly.itemOptions.add(new Item.ItemOption(5, 10));
                                        InventoryService.gI().addItemBag(player, broly);
                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player, "Bạn đã nhận được cải trang Broly Namec ");
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }
                                case 2 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 2000 && player.event.getEventPointQuyLao() == 1500) {
                                        player.event.setEventPointQuyLao(2000);
                                        Item broly = ItemService.gI().createNewItem((short) 1020, 1);
                                        broly.itemOptions.add(new Item.ItemOption(50, 20));
                                        broly.itemOptions.add(new Item.ItemOption(77, 25));
                                        broly.itemOptions.add(new Item.ItemOption(103, 20));
                                        broly.itemOptions.add(new Item.ItemOption(6, 20000));
                                        broly.itemOptions.add(new Item.ItemOption(5, 10));
                                        InventoryService.gI().addItemBag(player, broly);
                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player, "Bạn đã nhận được cải trang Broly Xayda ");
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 3000 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 2) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 2 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 3000 && player.event.getEventPointQuyLao() == 2000) {
                                        player.event.setEventPointQuyLao(3000);
                                        Item tien = ItemService.gI().createNewItem((short) Util.nextInt(1792, 1794), 1);
                                        Item it = ItemService.gI().createNewItem((short) 860, 1);
                                        it.itemOptions.add(new Item.ItemOption(50, 24));
                                        it.itemOptions.add(new Item.ItemOption(103, 20));
                                        it.itemOptions.add(new Item.ItemOption(77, 20));
                                        it.itemOptions.add(new Item.ItemOption(117, 12));
                                        it.itemOptions.add(new Item.ItemOption(5, 10));

                                        InventoryService.gI().addItemBag(player, tien);
                                        InventoryService.gI().addItemBag(player, it);
                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player, "Bạn đã nhận được  " + tien.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được " + it.template.name);

                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 5000 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 3) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 3 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 5000 && player.event.getEventPointQuyLao() == 3000) {
                                        player.event.setEventPointQuyLao(5000);
                                        Item tien = ItemService.gI().createNewItem((short) Util.nextInt(1793, 1795), 1);
                                        Item tenlua = ItemService.gI().createNewItem((short) 1603, 1);
                                        tenlua.itemOptions.add(new Item.ItemOption(50, 12));
                                        tenlua.itemOptions.add(new Item.ItemOption(103, 12));
                                        tenlua.itemOptions.add(new Item.ItemOption(77, 12));
                                        tenlua.itemOptions.add(new Item.ItemOption(5, 11));
                                        Item pet = ItemService.gI().createNewItem((short) 1654, 1);
                                        pet.itemOptions.add(new Item.ItemOption(50, 10));
                                        pet.itemOptions.add(new Item.ItemOption(103, 10));
                                        pet.itemOptions.add(new Item.ItemOption(77, 10));
                                        pet.itemOptions.add(new Item.ItemOption(5, 12));
                                        pet.itemOptions.add(new Item.ItemOption(117, 5));
                                        InventoryService.gI().addItemBag(player, tien);
                                        InventoryService.gI().addItemBag(player, tenlua);
                                        InventoryService.gI().addItemBag(player, pet);
                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player, "Bạn đã nhận được  " + tien.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được Ván vay Tên Lửa Cá Mập ");
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được Pet SerBerus");

                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                        case 10000 -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) < 3) {
                                        Service.gI().sendThongBao(player, "Cần ít nhất 3 ô trống trong hành trang");
                                        return;

                                    }
                                    if (player.pointtet >= 10000 && player.event.getEventPointQuyLao() == 5000) {
                                        player.event.setEventPointQuyLao(10000);
                                        Item tien = ItemService.gI().createNewItem((short) 1795, 1);
                                        Item dabv = ItemService.gI().createNewItem((short) 987, 300);
                                        Item gohan = ItemService.gI().createNewItem((short) 1840, 1);
                                        gohan.itemOptions.add(new Item.ItemOption(50, 28));
                                        gohan.itemOptions.add(new Item.ItemOption(103, 25));
                                        gohan.itemOptions.add(new Item.ItemOption(77, 25));
                                        gohan.itemOptions.add(new Item.ItemOption(5, 15));
                                        gohan.itemOptions.add(new Item.ItemOption(117, 10));

                                        InventoryService.gI().addItemBag(player, tien);
                                        InventoryService.gI().addItemBag(player, gohan);
                                        InventoryService.gI().addItemBag(player, dabv);
                                        InventoryService.gI().sendItemBag(player);

                                        Service.gI().sendThongBao(player, "Bạn đã nhận được  " + tien.template.name);
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được Cải trang Supper GoHan ");
                                        Service.gI().sendThongBao(player, "Bạn đã nhận được x300 Đá bảo vệ ");

                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Bạn chưa đủ điều kiện, hoặc đã nhận mốc này rồi");
                                    }
                                }

                            }
                        }
                    }
                }

                case 141 -> {
                    switch (select) {
                        case 0 -> {
                            if (player.clan == null || player.clan.ConDuongRanDoc == null
                                    || !player.clan.ConDuongRanDoc.allMobsDead) {
                                Service.gI().sendThongBao(player, "Chưa hạ hết đối thủ");
                                return;
                            }
                            ChangeMapService.gI().changeMapYardrat(player,
                                    ChangeMapService.gI().getMapCanJoin(player, 45), 295, 408);
                            Service.gI().sendThongBao(player, "Hãy xuống gặp thần mèo Karin");
                        }
                    }
                }
            }
        }
    }
}