package npc.npc_manifest;
import boss.BossID;


/**
 *
 * @author EMTI
 */
import consts.ConstNpc;
import consts.ConstTaskBadges;
import consts.cn;
import item.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jdbc.daos.PlayerDAO;
import models.Combine.CombineService;
import models.Training.TrainingService;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.Service;
import services.func.ChangeMapService;
import shop.ShopService;
import task.Badges.BadgesTaskService;
import utils.Util;

public class NangDe extends Npc {

    public NangDe(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if(player.pet!=null){
                
            this.createOtherMenu(player, ConstNpc.BASE_MENU, ""
                    +"Với mỗi đệ khác nhau sẽ cần 1 loại đá khác nhau để  và thỏi vàng để nâng cấp\n"
                    +"Đệ tử đạt level tối đa là level 10\n"
                    +"Mỗi level sẽ tăng thêm 1% chỉ số hợp thể\n"
                    +"\b|7|Khi nâng cấp sẽ có tỷ lệ thành công là 40%\n"
                    +"\b|7|Map nghĩa địa nơi up các loại đá nâng đệ, cần đệ 80 tỷ hãy cẩn trọng trong đó, nếu chết sẽ ko thể hồi sinh và boss rất nguy hiểm\n",
                    
                    "Nâng cấp Đệ tử",
                    "Shop đệ tử",
                    "Đổi Skill Đệ","Tới Nghĩa Địa");
        }else{
             Service.gI().sendThongBao(player, "Cần có đệ tử để mở khóa tính năng này");
        }
           
        }else{
             Service.gI().sendThongBao(player, "Cần có đệ tử để mở khóa tính năng này");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                   
                    case 0 -> {
                         int type = player.pet.typePet;
                         int id,csht = 0;
                         int danc = 1739; 
                         int lv = player.pet.level;
                         int sl =lv+1;
                         String str = "Chưa mở khóa";
                         if(type==1){
                             csht = 10;
                         }else if(type>1){
                             csht =20;
                             if(type==2){
                                 danc = 1800;
                             }else if(type ==3){
                                 danc = 1808;
                             }else{
                                 danc = 1860;
                             }
                         }
                        Item dancap = ItemService.gI().createNewItem((short) danc);
                        csht+=lv;
                        
                        if(type>=2){
                            if(lv<5){
                            this.createOtherMenu(player, 887, "\b|7| Thông tin đệ tử:\n"
                                 +"\b|5| Tên Đệ: "+player.pet.name +"\n"
                                 +"\b|1| Nâng cấp cần đá: "+ dancap.template.name+"\n"
                                 +"\b|3| Tăng chỉ số hợp thể: "+ csht +" %\n"
                                 +"\b|5| Cấp hiện tại "+lv+"\n"
                                 +"\b|7| Nâng chỉ sô "+lv+"/5 để mở khóa"
                                 +"\b|7| Nâng chỉ số đặc biệt "+lv+"/10 để mở khóa"
                        ,          
                               "Nâng cấp"
                              
                              );
                            }else if(lv<10){
                                this.createOtherMenu(player, 887, "\b|7| Thông tin đệ tử:\n"
                                 +"\b|3| Tên Đệ: "+player.pet.name +"\n"
                                 +"\b|1| Nâng cấp cần đá: "+ dancap.template.name+"\n"
                                 +"\b|3| Tăng chỉ số hợp thể: "+ csht+" %\n"
                                 +"\b|5| Cấp hiện tại "+lv+"\n"
                                
                                 +"\b|5| Chọn Option Đã mở khỏa"
                                 +"\b|7| Nâng chỉ số đặc biệt "+lv+"/10 để mở khóa"
                        ,
                               "Nâng cấp",
                               
                               "Chọn Option");
                            }else{
                                this.createOtherMenu(player, 887, "\b|7| Thông tin đệ tử:\n"
                                 +"\b|3| Tên Đệ: "+player.pet.name +"\n"
                                 +"\b|1| Nâng cấp cần đá: "+ dancap.template.name+"\n"
                                 +"\b|3| Tăng chỉ số hợp thể: "+ csht+" %\n"
                                 +"\b|5| Cấp hiện tại "+lv+"\n"
                                 +"\b|5| Chọn Option Đã mở khỏa"
                                 +"\b|5| Nâng chỉ số đặc biệt Đã mở khóa"
                        ,
                               "Nâng cấp Đệ tử",
                               "Chọn Option",
                               "Nâng Option");
                            }
                        }else{
                            if(player.pet.level<10){
                            this.createOtherMenu(player, 887, "\b|7| Thông tin đệ tử:\n"
                                 
                                 +"\b|5| Tên Đệ: "+player.pet.name +"\n"
                                  +"\b|1| Nâng cấp cần đá: "+ dancap.template.name+"\n"
                                 +"\b|3| Tăng chỉ số hợp thể: "+ csht+" %\n"
                                 +"\b|5| Cấp hiện tại "+lv+"\n"
                               
                        ,
                               "Nâng cấp"
                               );
                            }else{
                                this.createOtherMenu(player, 887, "\b|7| Thông tin đệ tử:\n"
                                 +"\b|5| Tên Đệ: "+player.pet.name +"\n"
                                  +"\b|1| Nâng cấp cần đá: "+ dancap.template.name+"\n"
                                 +"\b|3| Tăng chỉ số hợp thể: "+ csht+" %\n"
                                 +"\b|5| Cấp hiện tại "+lv+"\n"
                                
                        ,
                               "Nâng cấp"
                               );
                            }
                        }break;
                    }
                        
                    case 1->{
                        ShopService.gI().opendShop(player, "SHOP_DE", false);
                        break;
                    }
                    case 2->{
                        if (player.getSession() != null) {
                            this.createOtherMenu(player, 888,
                                    "|0|Lưu ý: Đổi Skill đệ bằng tiền nạp sẽ mất VND\n|7|"
                                    + "Bạn có: " + player.getSession().cash + " VND",
                                    //Menu CHọn
                                    "Đổi skill 2-3\n <" + cn.skill23 + ">", "Đổi skill 2-4\n <" + cn.skill24 + ">","Đổi skill 5\n <" + cn.skill5);

                        }
                        break;
                    }
                    case 3->{
                        if (player.pet.nPoint.power<80_000_000_000L) {
                         Service.gI().sendThongBao(player, "Cố mẹ gắng đi e, đệ 80 tỷ đã");
                            return;
                        }
                         
                        ChangeMapService.gI().changeMapNonSpaceship(player, 181, 615, 288);
                        break;
                    }
                           
                    
                }
            }else if (player.iDMark.getIndexMenu() == 887) {
                 switch (select) {
                     case 0:
                        int lv = player.pet.level;
                        int type = player.pet.typePet;
                        int id = 0;
                        int  sl = player.pet.level+1;
                        if(type==2){
                             id = 1800;
                         }else if(type ==3){
                             id = 1808;                           
                         }else if(type==4){
                             id = 1860;
                         }
                         else  {
                             id = 1739;                          
                         }
                        Item tv = InventoryService.gI().findItemBag(player, 457);
                        Item dancde = InventoryService.gI().findItemBag(player, id);
                        if (tv == null || tv.quantity < sl) {
                            Service.gI().sendThongBao(player, "Cần "+sl+" tv");
                            return;
                        }
                        if(dancde == null|| dancde.quantity<sl){
                            Service.gI().sendThongBao(player, "Cần Đá nâng, Để nâng cấp đệ");
                            return;
                        }
                        if(player.pet.level>=10){
                                Service.gI().sendThongBao(player, "Pet đã đạt cấp tối đa");
                                return;
                            }
                        if(Util.isTrue(40,100)){
                            player.pet.level++;
                            String newName = player.pet.name.replaceAll("\\[.*?\\]", "") + "[Cấp " + player.pet.level+ "]";
                            player.pet.name = newName;
                            InventoryService.gI().subQuantityItemsBag(player, tv, sl);
                            InventoryService.gI().subQuantityItemsBag(player, dancde, sl);
                            InventoryService.gI().sendItemBag(player);
                            ChangeMapService.gI().exitMap(player.pet);
                            if(dancde.quantity>=sl){
                            this.createOtherMenu(player, 887, "\b|7| Thông tin đệ tử:\n"
                                    
                                     +"\b|5|Nâng cấp thành công!!!\n\n"
                                    +"\b|5| Cấp hiện tại "+player.pet.level+"\n"
                                   
                                    +"\b|1| Cấp tiếp theo cần: x" +sl +" "+ dancde.template.name +" và thỏi vàng\n"  ,
                                    "Nâng cấp");
                        }
                        }else{
                            InventoryService.gI().subQuantityItemsBag(player, tv, sl);
                            InventoryService.gI().subQuantityItemsBag(player, dancde, sl);
                            InventoryService.gI().sendItemBag(player);
                            
                            if(dancde.quantity>=sl){
                            this.createOtherMenu(player, 887, "\b|7| Thông tin đệ tử:\n"
                                    
                                    +"\b|3|Nhọ!!!\n\n"
                                    +"\b|5| Cấp hiện tại "+player.pet.level+"\n"
                                    +"\b|1| Cấp tiếp theo cần: x" +sl +" "+ dancde.template.name +" và thỏi vàng\n"  ,
                                    "Nâng cấp");
                        }
                        }break;
                     case 1:
                         int op = player.optde;
                         if(player.pet.typePet ==3){
                              this.createOtherMenu(player, 890, "\b|7| Lựa chọn option tăng thêm khi hợp thể\n"
                                      +"\b|7| Chỉ số tăng thêm hiện tại "+op +"%\n"
                                      +"\b|3| Lựa chọn hiện tại, Option: "+ (player.choice > 0 ? player.choice : "Chưa lựa chọn")+"\n"
                                      +"\b|5|Option 1 tăng Sát thương đấm galick \n "
                                      +"\b|5|Option 2 tăng HP,SĐ khi biến khỉ\n "
                                      +"\b|5|Option 3 tăng  sát thương bom\n ",
                                      "Option 1","Option 2","Option 3");
                         }
                         else if(player.pet.typePet==4){
                              this.createOtherMenu(player, 890, "\b|7| Lựa chọn option tăng thêm khi hợp thể\n"
                                      +"\b|7| Chỉ số tăng thêm hiện tại "+op +"%\n"
                                      +"\b|3| Lựa chọn hiện tại, Option: "+ (player.choice > 0 ? player.choice : "Chưa lựa chọn")+"\n"
                                      +"\b|5|Option 1 tăng Sát thương Liên Hoàn \n "
                                      +"\b|5|Option 2 tăng Sát thương đẻ trứng\n "
                                      +"\b|5|Option 3 tăng  sát thương laze\n ",
                                      "Option 1","Option 2","Option 3");
                         } else{
                             this.createOtherMenu(player, 890, "\b|7| Lựa chọn option tăng thêm khi hợp thể\n"
                                      +"\b|7| Chỉ số tăng thêm hiện tại "+op +"%\n"
                                      +"\b|3| Lựa chọn hiện tại, Option: "+ (player.choice > 0 ? player.choice : "Chưa lựa chọn")+"\n"
                                      +"\b|5|Option 1 tăng Sát KameJoko \n "
                                      +"\b|5|Option 2 tăng Sát thương Kaioken\n "
                                      +"\b|5|Option 3 tăng  sát thương Quả cầu kinh khi\n ",
                                      "Option 1","Option 2","Option 3");
                         }
                         break;
                     case 2:
                         this.createOtherMenu(player, 891, "\b|7| Nâng cấp option sẽ tiêu tốn Tinh Thạch\n"
                                 +"\b|5|Chỉ số tăng thêm hiện Tại "+ player.optde+"/"+(player.pet.nPoint.limitPower+1) +"%\n"
                                 +"\b|1| Cấp tiếp theo cần x"+(player.optde+1)*2+" Tinh thạch và thỏi vàng",
                                 "Tăng chỉ số","Đóng");
                         break;
                                  
                        
                 }
                 
            } else if (player.iDMark.getIndexMenu() == 891) {
                  switch (select) {
                      case 0:
                          int sl =(player.optde+1)*2;
                          Item tv = InventoryService.gI().findItemBag(player, 457);
                          Item dancde = InventoryService.gI().findItemBag(player, 1823);
                          if (tv == null || tv.quantity < sl) {
                            Service.gI().sendThongBao(player, "Cần "+sl+" tv");
                            return;
                        }
                        if(dancde == null|| dancde.quantity<sl){
                            Service.gI().sendThongBao(player, "Cần Tinh Thạch để nâng option");
                            return;
                        }
                        if(player.optde>=player.pet.nPoint.limitPower){
                            Service.gI().sendThongBao(player, "Đã đạt cấp tối đa, hãy up đệ thêm để mở thêm giới hạn");
                            return;
                        }
                        player.optde++;
                         Service.gI().sendThongBao(player, "Nâng cấp thành công");
                         InventoryService.gI().subQuantityItemsBag(player, tv, sl);
                            InventoryService.gI().subQuantityItemsBag(player, dancde, sl);
                            InventoryService.gI().sendItemBag(player);
                        
                        break;
                         
                  }
            }
            else if (player.iDMark.getIndexMenu() == 890) {
                
                 switch (select) {
                     
                     case 0:
                         player.choice = 1;
                         if(player.pet.typePet==3){
                             Service.gI().sendThongBao(player, "\b|7| Đổi thành công Option 1 tăng Sát thương đấm galick");
                         }else if(player.pet.typePet==4){
                             Service.gI().sendThongBao(player, "\b|7| Đổi thành công Option 1 tăng Sát thương Liên Hoàn");
                         }else{
                              Service.gI().sendThongBao(player, "\b|7| Đổi thành công Option 1 tăng Sát thương Kamejoko");
                         }
                         break;
                     case 1:
                         player.choice = 2;
                         if(player.pet.typePet==3){
                             Service.gI().sendThongBao(player, "\b|5| Đổi thành công Option 2 tăng HP,SĐ khi biến khỉ");
                         }else if(player.pet.typePet==4){
                             Service.gI().sendThongBao(player, "\b|5| Đổi thành công Option 2 tăng Sát thương đẻ trứng");
                         }else{
                              Service.gI().sendThongBao(player, "\b|5| Đổi thành công Option 2 tăng Sát thương Kaioken");
                         }
                         break;
                     case 2:
                         player.choice = 3;
                         if(player.pet.typePet==3){
                             Service.gI().sendThongBao(player, "\b|3| Đổi thành công Option 3 tăng  sát thương bom");
                         }else if(player.pet.typePet==4){
                             Service.gI().sendThongBao(player, "\b|3| Option 3 tăng  sát thương laze");
                         }else{
                              Service.gI().sendThongBao(player, "\b|3| Đổi thành côngOption 3 tăng  sát thương Quả cầu kinh khi");
                         }
                         break;
                 }
            }
            
            else if (player.iDMark.getIndexMenu() == 888) {
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
            }
        }
    }
}