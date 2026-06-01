package npc.npc_manifest;


import consts.ConstNpc;
import consts.ConstTask;
import consts.ConstTaskBadges;
import consts.cn;
import item.Item;
import jdbc.daos.PlayerDAO;
import network.Message;
import npc.Npc;
import npc.specialnpc.MabuEgg;
import player.Pet;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PetService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import shop.ShopService;
import task.Badges.BadgesTaskService;
import utils.Util;

public class Napa extends Npc {

    public Napa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                switch (mapId) {
                    case 0,7,14,203 ->
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "\b|1|Đây là nơi ngươi có thể đổi bất cứ thứ gì"
                                + "\nMiễn là ngươi có tiền"
                                + "\b\n|3| Nạp VND giá trị ( cứ 20k được <20.000 VND> trong game)"
                                
                                + "\b|3|Lưu ý: Chỉ giao dịch nạp tiền qua duy nhất qua admin\n"
                                + "mọi rủi ro tự chịu nếu không chấp hành."
                                + "\n\b|7|Bạn đang có :" + player.getSession().cash + " VND\n|4|"
                                ,
                                
//                                "Menu VND",
                                "Shop",
                              
                               
                                " Mở thành viên",
                               
                                "Đổi skill đệ",
                                "Mua đệ VIP");
                    default ->
                        super.openBaseMenu(player);
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        ShopService.gI().opendShop(player, "BARDOCK_SHOP", false);
                        break;
//                   
//                  
//                   

                    case 1:
                        if (player.getSession() != null) {
                            this.createOtherMenu(player, 782,
                                    "\b|2|Mở thành viên 20k  \n Nhận cải trang Buma thám hiểm 2 ngày và 1 hộp mù baby Three\n\b|7|, +\"Hoặc ngươi có thể mở thành viên Free bằng cách hoàn thành nhiệm vụ Fide\n"
                                            + "Bạn đã nạp :\n"
                                    +"Hoặc ngươi có thể mở thành viên Free bằng cách hoàn thành nhiệm vụ Fide\n"
                                    + "" + player.getSession().cash + " đồng\n|4|"
                                    ,
                                    "Mở\n20k", "Mở Free");
                        }
                        break;
                    case 2:
                        if (player.getSession() != null) {
                            this.createOtherMenu(player, 888,
                                    "|0|Lưu ý: Đổi Skill đệ bằng tiền nạp sẽ mất VND\n|7|"
                                    + "Bạn có: " + player.getSession().cash + " VND",
                                    //Menu CHọn
                                    "Đổi skill 2-3\n <" + cn.skill23 + ">", "Đổi skill 2-4\n <" + cn.skill24 + ">","Đổi skill 5\n <" + cn.skill5);

                        }
                        break;
                    case 3:
                         if (player.getSession() != null) {
                             this.createOtherMenu(player, 999,"Mua đệ Songoku,Vegeta,Picolo sẽ tăng 20% chỉ số khi hợp thể\n"
                                     + "Cần có đệ trước khi mua\n"
                                     +" Ngoài ra bạn có thể săn đệ từ boss Broly Angry\n"
                                     +"Tháo đồ khi mua đệ\n đệ Vip sẽ cùng hành linh với đệ đang có\n"
                                     +"\b|3|Đệ Songoku nâng cấp sẽ tăng chỉ số cho Trái Đất\n"
                                     +"\b|5|Đệ Vegeta nâng cấp tăng chỉ số cho XayDa\n"
                                     +"\b|7|Đệ picolo nâng cấp sẽ tăng chỉ số cho namec\n",
                                     "Songoku\n50k","Vegeta\n50k","Picolo\n50k", "Xem xét");
                                     
                         }

                }
            } else if (player.iDMark.getIndexMenu() ==999) {
                switch (select) {
                    case 0:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if(player.pet.typePet==2){
                            Service.gI().sendThongBao(player, "Ngươi có đệ Songoku rồi mà????");
                                return;
                        }
                       if (player.getSession().cash < 50000) {
                             Service.gI().sendThongBao(player, "50k của ta đâu!!");
                            return;
                        }
                       
                       if( PlayerDAO.subcash(player, 50000)){
                          
                           ChangeMapService.gI().exitMap(player.pet);
                           player.pet.typePet = 2;
                           player.pet.name = "Songoku";
                         
                           Service.gI().sendThongBao(player, "Đổi thành công đệ Songoku");
                            
                       }
                        break;
                    case 1:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if(player.pet.typePet==3){
                            Service.gI().sendThongBao(player, "Ngươi có đệ Vegeta rồi mà????");
                                return;
                        }
                       if (player.getSession().cash < 50000) {
                             Service.gI().sendThongBao(player, "50k của ta đâu!!");
                            return;
                        }
                       
                       if( PlayerDAO.subcash(player, 50000)){
                          
                           ChangeMapService.gI().exitMap(player.pet);
                           player.pet.typePet = 3;
                           player.pet.name = "Vegeta";
                         
                           Service.gI().sendThongBao(player, "Đổi thành công đệ Vegeta");
                            
                       }
                        break;
                         case 2:
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi cần phải có đệ mới sử dụng được chức năng này?");
                            return;
                        }
                        for (Item item : player.pet.inventory.itemsBody) {
                            if (item.isNotNullItem()) {
                                Service.gI().sendThongBao(player, "Cần bỏ đồ đệ tử đang mặc để sử dụng chức năng?");
                                return;
                            }
                        }
                        if(player.pet.typePet==4){
                            Service.gI().sendThongBao(player, "Ngươi có đệ Picolo rồi  mà????");
                                return;
                        }
                       if (player.getSession().cash < 50000) {
                             Service.gI().sendThongBao(player, "50k của ta đâu!!");
                            return;
                        }
                       
                       if( PlayerDAO.subcash(player, 50000)){
                          
                           ChangeMapService.gI().exitMap(player.pet);
                           player.pet.typePet = 4;
                           player.pet.name = "Picolo";
                         
                           Service.gI().sendThongBao(player, "Đổi thành công đệ Picolo");
                            
                       }
                        break;
                    

                }
            } else if (player.iDMark.getIndexMenu() == 888) {
                switch (select) {
                    case 0: //thay chiêu 2-3 đệ tử
                        if (player.getSession() != null && player.getSession().cash < cn.skill23) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.skill23 + " VND");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.skill23)) {
                            if (player.pet != null) {
                                if (player.pet.playerSkill.skills.get(1).skillId != -1) {
                                    player.pet.openSkill2();
                                    if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                        player.pet.openSkill3();
                                    }
                                    Service.gI().sendThongBao(player, "Đổi skill 2-3 đệ thành công");
                                } else {
                                    Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");

                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");

                            }
                        }
                        break;
                    case 1: //thay chiêu 2-4 đệ tử
                        if (player.getSession() != null && player.getSession().cash < cn.skill24) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.skill24 + " VND");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.skill24)) {
                            if (player.pet != null) {
                                if (player.pet.playerSkill.skills.get(1).skillId != -1) {
                                    player.pet.openSkill2();
                                    if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                        player.pet.openSkill4();
                                    }
                                    Service.gI().sendThongBao(player, "Đổi skill 2-4 đệ thành công");

                                } else {
                                    Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");

                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");

                            }
                        }
                        break;
                        case 2: //thay chiêu 5đệ tử
                        if (player.getSession() != null && player.getSession().cash < cn.skill5) {
                            Service.gI().sendThongBao(player, "Bạn không đủ " + cn.skill5+ " VND");
                            return;
                        }

                        if (PlayerDAO.subcash(player, cn.skill5)) {
                            if (player.pet != null) {
                                if (player.pet.playerSkill.skills.get( 4).skillId != -1) {
                                    player.pet.openSkill5();
                                    
                                    Service.gI().sendThongBao(player, "Đổi skill 5 đệ thành công");

                                } else {
                                    Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 5 chứ!");

                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");

                            }
                        }
                        break;

                }
            } else if (player.iDMark.getIndexMenu() == 777) {
                switch (select) {
                    case 0:
                        Input.gI().createFormDoiThoiVang(player);
                        break;
                    case 1:
                        Input.gI().createFormDoiNgocXanh(player);
                        break;
                    case 2:
                        Input.gI().createFormDoiNgocHong(player);
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == 782) {
                switch (select) {
                    case 0:
                        if (player.getSession() != null && player.getSession().actived) {
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên rồi");
                            return;
                        }
                        if (player.getSession() != null && player.getSession().cash < 20000) {
                             Service.gI().sendThongBao(player, "Thiếu tiền rồi anh zai!!");
                            return;
                        }
                        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                    Service.gI().sendThongBaoOK(player, "Cần 1 ô hành trang để mở");
                                    return;
                                }
                        PlayerDAO.subcash(player, 20000);
                        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.FAN_CUNG, 1);
                        Item ct = ItemService.gI().createNewItem((short) 1121,1);
                        Item hm = ItemService.gI().createNewItem((short) 1430,1);
                        ct.itemOptions.add(new Item.ItemOption(50, 24));                   
                        ct.itemOptions.add(new Item.ItemOption(77, 22)); 
                        ct.itemOptions.add(new Item.ItemOption(103, 20)); 
                        ct.itemOptions.add(new Item.ItemOption(95, 20)); 
                        ct.itemOptions.add(new Item.ItemOption(96, 20)); 
                         ct.itemOptions.add(new Item.ItemOption(101, 20)); 
                        ct.itemOptions.add(new Item.ItemOption(93, 2)); 
                        InventoryService.gI().addItemBag(player, ct);
                         InventoryService.gI().addItemBag(player, hm);
                        InventoryService.gI().sendItemBag(player);
                        PlayerDAO.updateActive(player, 1);
                           
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên thành công,cải trang Buma thám hiểm và hộp mù baby three");
                        

                        break;
                    case 1:
                        if (player.getSession() != null && player.getSession().actived) {
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên rồi");
                            return;
                        }
                        if (TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
                            Service.gI().sendThongBao(player, "Cần hoàn thành nhiệm vụ fide để mở khóa free");
                            return;
                        }
                        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                    Service.gI().sendThongBaoOK(player, "Cần 1 ô hành trang để mở");
                                    return;
                                }
                        PlayerDAO.updateActive(player, 1);
                           
                            Service.gI().sendThongBao(player, "Bạn đã mở thành viên thành công");
                        break;

                }
            } else if (player.iDMark.getIndexMenu() == 0) {
                switch (mapId) {
                }
            }

        }
    }
}
