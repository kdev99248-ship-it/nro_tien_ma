package npc.npc_manifest;

/**
 *
 * @author EMTI
 */
import consts.ConstNpc;
import item.Item;
import java.util.Random;
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

public class NgoKhong extends Npc {

    public NgoKhong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 122 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Test!!!\n"
//                            + "Khi pem quái ở nhs sẽ rớt các chữ giải khai phong ấn\n"
//                            +"Tiêu diệt boss Black Wokong sẽ sẽ rớt quả hồng đào và tỷ lệ thấp rớt Hồng đào hắc hóa\n"
//                            + " Giải phong ấn thường cần  x9 giải khai phong ấn đc 1 điểm và các phần quà ngẫu nhiên\n"
//                            + "Giải Phong ấn sơ cấp cần x99 giải khai phong ấn x10 quả hồng đào nhận 15 điểm và các phần quà ngẫu nhiên\n"
//                            + "Giải phong ấn cao cấp cần x20 quả hồng đào Hắc Hóa nhận 99 điểm và các phần quà ngẫu nhiên",
//                            "Giải phong ấn thường",
//                            "Giải phong ấn sơ cấp",
//                            "Giải phong ấn cao cấp",
//                            "BXH",
//                           
//                            "SHOP SK"
                    //, "Nhiệm vụ\nhộ tống", "Từ chối", "Nhận\nthưởng"
                    );
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
                    case 122 -> {
                        if (select == 0) {
                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item giai = InventoryService.gI().findItemBag(player, 537);
                            Item khai = InventoryService.gI().findItemBag(player, 538);
                            Item phong = InventoryService.gI().findItemBag(player, 539);
                            Item an = InventoryService.gI().findItemBag(player, 540);



                            if (giai == null || giai.quantity < 9) {
                                Service.gI().sendThongBao(player, "Cần 9 chữ giải");
                                return;
                            }
                            if (khai == null || khai.quantity < 9) {
                                Service.gI().sendThongBao(player, "Cần 9 chữ khai");
                                return;
                            }
                            if (phong == null || phong.quantity < 9) {
                                Service.gI().sendThongBao(player, "Cần 9 chữ phong");
                                return;
                            }
                            if (an == null || an.quantity < 9) {
                                Service.gI().sendThongBao(player, "Cần 9 chữ an");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, giai, 9);
                            InventoryService.gI().subQuantityItemsBag(player, khai, 9);
                            InventoryService.gI().subQuantityItemsBag(player, phong, 9);
                            InventoryService.gI().subQuantityItemsBag(player, an, 9);
                            int qua = 0;
                           
                            Random random = new Random();
                            int rand = random.nextInt(100);
                            if(rand >=0 &&rand <=10){
                                qua = 16;
                              
                            }else if(rand>10&&rand<=20){
                                qua = 1846;
                             
                            }else if(rand>20&&rand<=50){
                                qua = Util.nextInt(829,841);
                            }else if(rand>50&&rand<=70){
                                qua = 1707;
                            }else if(rand>70&&rand<=95){
                                qua =543;
                            }else {
                                qua = 542;
                            }
                             Item quatet = ItemService.gI().createNewItem((short) qua, 1);
                            InventoryService.gI().addItemBag(player, quatet);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được x1 "+ quatet.template.name+" và 1 Điểm sự kiện");
//                            player.pointtet+=1;
                        }
                        if (select == 1) {

                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item giai = InventoryService.gI().findItemBag(player, 537);
                            Item khai = InventoryService.gI().findItemBag(player, 538);
                            Item phong = InventoryService.gI().findItemBag(player, 539);
                            Item an = InventoryService.gI().findItemBag(player, 540);
                             Item hd = InventoryService.gI().findItemBag(player, 541);



                            if (giai == null || giai.quantity < 99) {
                                Service.gI().sendThongBao(player, "Cần 99 chữ giải");
                                return;
                            }
                            if (khai == null || khai.quantity < 99) {
                                Service.gI().sendThongBao(player, "Cần 99 chữ khai");
                                return;
                            }
                            if (phong == null || phong.quantity < 99) {
                                Service.gI().sendThongBao(player, "Cần 99 chữ phong");
                                return;
                            }
                            if (an == null || an.quantity < 99) {
                                Service.gI().sendThongBao(player, "Cần 9 chữ an");
                                return;
                            }
                             if (hd == null || hd.quantity < 10) {
                                Service.gI().sendThongBao(player, "Cần 10 Quả hồng đào");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, giai, 99);
                            InventoryService.gI().subQuantityItemsBag(player, khai, 99);
                            InventoryService.gI().subQuantityItemsBag(player, phong, 99);
                            InventoryService.gI().subQuantityItemsBag(player, an, 99);
                            InventoryService.gI().subQuantityItemsBag(player, hd, 10);
                            int qua = 0;
                            int sl = 1;
                            Random random = new Random();
                            int rand = random.nextInt(100);
                            if(rand >=0 &&rand <=10){
                                qua = 15;
                                 
                            }else if(rand>10&&rand<=20){
                                qua = 457;
                                sl = Util.nextInt(10,20);
                            }else if(rand>20&&rand<=30){
                                qua = Util.nextInt(1732,1736);
                            }else if(rand>30&&rand<=40){
                                qua = 1707;
                                sl = Util.nextInt(1,5);
                            }else if(rand>400&&rand<=50){
                                qua =543;
                                sl = Util.nextInt(1,10);
                            }else if(rand>50&&rand<=60){
                                qua = 542;
                            }else if(rand>60&&rand<=70){
                                qua  = 1846;
                            }else if(rand>70&&rand<=80){
                                qua  = 1750;
                            }else if(rand>80&&rand<=88){
                                qua  = 1751;
                            }else if(rand>88&&rand<=96){
                                qua  = 1752;
                            }else{
                                qua = 1757;
                            }
                                    
                             Item quatet = ItemService.gI().createNewItem((short) qua, sl);
                            InventoryService.gI().addItemBag(player, quatet);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được x"+sl+" "+ quatet.template.name+" và 15 Điểm sự kiện");
//                            player.pointtet+=15;
                        }
//                        if (select == 4) {
//                            TopService.showListTop(player, 9);
//                        }
                        if(select ==2){
                            if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item hdc = InventoryService.gI().findItemBag(player, 542);
                            if (hdc == null || hdc.quantity < 20) {
                                Service.gI().sendThongBao(player, "Cần 20 Hồng đào hắc hóa");
                                return;
                            }
                            
                            InventoryService.gI().subQuantityItemsBag(player, hdc, 20);
                            int qua = 0;
                            int sl = 1;
                            Random random = new Random();
                            int rand = random.nextInt(100);
                            if(rand >=0 &&rand <=10){
                                qua = 14;
                              
                            }else if(rand>10&&rand<=20){
                                qua = 1846;
                                sl = Util.nextInt(1,5);
                             
                            }else if(rand>20&&rand<=30){
                                qua = 543;
                                sl = Util.nextInt(5,20);
                                  
                            }else if(rand>30&&rand<=40){
                                qua = 1700;
                            }else if(rand>40&&rand<=60){
                                qua =1707;
                                sl = Util.nextInt(5, 10);
                            }else if(rand>60&&rand<=70) {
                                qua = 457;
                                sl = Util.nextInt(10,30);
                            }else if(rand>70&&rand<=80){
                                qua = 1386;
                            }else if(rand>80&&rand<=90){
                                qua = 1502;
                            }else if(rand>90&&rand<=98){
                                qua = 1252;
                            }else{
                                qua = 1847;
                            }
                                    
                            Item quatet = ItemService.gI().createNewItem((short) qua, sl);
                            if(qua == 1700){
                                 quatet.itemOptions.add(new Item.ItemOption(50, Util.nextInt(20,40)));
                                 quatet.itemOptions.add(new Item.ItemOption(77, Util.nextInt(20,40)));
                                 quatet.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20,40)));
                                  quatet.itemOptions.add(new Item.ItemOption(117, Util.nextInt(10,20)));
                                   quatet.itemOptions.add(new Item.ItemOption(5, Util.nextInt(10,20)));
                                    quatet.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1,3)));
                            }
                            if(qua ==1386||qua==1502){
                                quatet.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10,20)));
                                 quatet.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10,20)));
                                 quatet.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20,20)));
                                  quatet.itemOptions.add(new Item.ItemOption(Util.nextInt(156,158), Util.nextInt(10,20)));
                                   quatet.itemOptions.add(new Item.ItemOption(5, Util.nextInt(5,12)));
                                    quatet.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1,3)));
                            }
                            if(qua==1252){
                               quatet.itemOptions.add(new Item.ItemOption(50, 12));
                                quatet.itemOptions.add(new Item.ItemOption(77, 12));
                                quatet.itemOptions.add(new Item.ItemOption(103, 12));
                                quatet.itemOptions.add(new Item.ItemOption(5, 12));
                                  
                                 
                            }
                            if(qua==1847){
                                quatet.itemOptions.add(new Item.ItemOption(50, 15));
                                quatet.itemOptions.add(new Item.ItemOption(77, 15));
                                quatet.itemOptions.add(new Item.ItemOption(103, 15));
                                quatet.itemOptions.add(new Item.ItemOption(5, 10));
                                quatet.itemOptions.add(new Item.ItemOption(190, 15));
                                  
                            }
                            
                            InventoryService.gI().addItemBag(player, quatet);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được x"+sl+" "+ quatet.template.name+" và 99 Điểm sự kiện");
//                            player.pointtet+=99;
                        }
                        if(select ==4){
                            
                        ShopService.gI().opendShop(player, "SHOPbg", false);
                       
                        }
                        if(select==3){
                            TopService.showListTop(player, 9);
                        }

                    }
                    
                }

            }
        }
    }
}
