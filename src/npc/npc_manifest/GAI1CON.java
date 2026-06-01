package npc.npc_manifest;


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

public class GAI1CON extends Npc {

    public GAI1CON(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 5 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Năm mới chưa có bồ thì kém!!!\n"
                            + "Lì xì sử dụng nhận 1 điểm sự kiện\n"
                            + "Đổi Hộp quà tết cần x10 lì xì + 1 thỏi vàng, sử dụng nhận 12 điểm sự kiện\n"
                            + "Đổi Giỏ quà tết cần 30 Đồng Vạn, Sự, Như, Ý và 3 thỏi vàng mở nhận 99 điểm sự kiện\n"
                            + "Donate Đê!!! Đói rồi",
                            "Đổi Hộp Quà Tết",
                            "Đổi cải trang",
                            "Đổi Giỏ quà tết",
                            "Tới Map\n Đêm 30",
                           
                            "SHOP SK"
                    //, "Nhiệm vụ\nhộ tống", "Từ chối", "Nhận\nthưởng"
                    );
                } case 176 ->{
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Trở về thôi!!!",
                            "Về\nĐảo Kame", "Đóng");
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
                        if (select == 0) {
                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item tv = InventoryService.gI().findItemBag(player, 457);
                            Item lixi = InventoryService.gI().findItemBag(player, 717);

                            if (lixi == null || lixi.quantity < 10) {
                                Service.gI().sendThongBao(player, "Cần 10 bao lì xì");
                                return;
                            }
                            if (lixi == null || lixi.quantity < 1) {
                                Service.gI().sendThongBao(player, "Cần 1 thỏi vàng");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, tv, 1);
                            InventoryService.gI().subQuantityItemsBag(player, lixi, 10);
                            Item quatet = ItemService.gI().createNewItem((short) 1187, 1);
                            InventoryService.gI().addItemBag(player, quatet);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được 1 hộp quà tết");

                        }
                        if (select == 1) {

                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item tv = InventoryService.gI().findItemBag(player, 457);
                            Item manhct = InventoryService.gI().findItemBag(player, 720);

                            if (manhct == null || manhct.quantity < 99) {
                                Service.gI().sendThongBao(player, "Cần 99 mảnh cải trang");
                                return;
                            }
                            if (tv == null || tv.quantity < 100) {
                                Service.gI().sendThongBao(player, "Cần 100 thỏi vàng");
                                return;
                            }

                            InventoryService.gI().subQuantityItemsBag(player, manhct, 99);
                            InventoryService.gI().subQuantityItemsBag(player, tv, 100);
                            Item ct = ItemService.gI().createNewItem((short) 1684, 1);
                            ct.itemOptions.add(new Item.ItemOption(50, 30));
                            ct.itemOptions.add(new Item.ItemOption(77, 30));
                            ct.itemOptions.add(new Item.ItemOption(103, 30));
                            ct.itemOptions.add(new Item.ItemOption(5, 15));
   
                            ct.itemOptions.add(new Item.ItemOption(117, 10));
                            if (Util.isTrue(30, 100)) {
                                ct.itemOptions.add(new Item.ItemOption(156, 20));
                            } else if (Util.isTrue(30, 100)) {
                                ct.itemOptions.add(new Item.ItemOption(157, 20));
                            } else {
                                ct.itemOptions.add(new Item.ItemOption(158, 20));
                            }

                            InventoryService.gI().addItemBag(player, ct);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được Cải trang Võ Sư Lân");
                        }
                        if(select == 2){
                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item tv = InventoryService.gI().findItemBag(player, 457);
                            Item van = InventoryService.gI().findItemBag(player, 1488);
                            Item su = InventoryService.gI().findItemBag(player, 1489);
                            Item nhu = InventoryService.gI().findItemBag(player, 1490);
                            Item y = InventoryService.gI().findItemBag(player, 1491);

                            if (tv == null || tv.quantity < 3) {
                                Service.gI().sendThongBao(player, "Cần 3 thỏi vàng");
                                return;
                            }
                            if (van == null || van.quantity < 30) {
                                Service.gI().sendThongBao(player, "Cần 30 Xu chữ vạn");
                                return;
                            }
                             if (su == null || su.quantity < 30) {
                                Service.gI().sendThongBao(player, "Cần 30 Xu chữ sự");
                                return;
                            }
                              if (nhu == null || nhu.quantity < 30) {
                                Service.gI().sendThongBao(player, "Cần 30 Xu chữ như");
                                return;
                            }
                              if (y == null || y.quantity < 30) {
                                Service.gI().sendThongBao(player, "Cần 30 Xu chữ ý");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, tv, 3);
                            InventoryService.gI().subQuantityItemsBag(player, van, 30);
                            InventoryService.gI().subQuantityItemsBag(player, su, 30);
                            InventoryService.gI().subQuantityItemsBag(player, nhu, 30);
                            InventoryService.gI().subQuantityItemsBag(player, y, 30);
                            Item quatet = ItemService.gI().createNewItem((short) 1766, 1);
                            InventoryService.gI().addItemBag(player, quatet);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được 1 Giỏ quà Tết");
                        }
//                        if (select == 4) {
//                            TopService.showListTop(player, 9);
//                        }
                        if(select ==3){
                            if (player.nPoint.power <= 10_000_000_000L) {
                                    Service.gI().sendThongBao(player, "Yêu cầu sức mạnh đạt 10 tỉ");
                                    return;
                                }
                            ChangeMapService.gI().changeMapNonSpaceship(player, 176, 346, 180);
                        }
                        if(select ==4){
                            
                        ShopService.gI().opendShop(player, "SHOPTET", false);
                       
                        }

                    }
                    case 176 ->{
                        if (select == 0) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 5, Util.nextInt(700, 800), 432);
                        }
                    }
                }

            }
        }
    }
}
