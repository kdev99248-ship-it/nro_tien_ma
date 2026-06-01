package npc.npc_manifest;


import clan.Clan;
import clan.ClanMember;
import consts.ConstNpc;
import consts.ConstTask;
import item.Item;
import java.util.ArrayList;
import npc.Npc;
import player.Player;
import server.Client;
import services.ClanService;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class GiuMaDauBo extends Npc {
public int qua =1;

    public GiuMaDauBo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            
            Clan clan = player.clan;

            if(clan!=null){
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi đang muốn tìm mảnh vỡ và mảnh hồn bông tai Porata trong truyền thuyết, ta sẽ đưa ngươi đến đó ?",
                     "OK","Chức năng bang hội","Nhiệm vụ Bang\n[" + player.playerTask.clanTask.leftTask + "/" + ConstTask.MAX_CLAN_TASK + "]");
            }else{
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi đang muốn tìm mảnh vỡ và mảnh hồn bông tai Porata trong truyền thuyết, ta sẽ đưa ngươi đến đó ?",
                     "OK");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
             if (player.iDMark.isBaseMenu()) {
            switch (select) {
//                case 0 -> {
//                }
                case 0 -> {
                    if (player.nPoint.power <= 100_000_000_000L) {
                                    Service.gI().sendThongBao(player, "Yêu cầu sức mạnh đạt 100 tỉ");
                                    return;
                                }
                    player.type = 5;
                    player.maxTime = 5;
                    Service.gI().Transport(player);
                }
                case 1->{
                     Clan clan = player.clan;
                    if(clan.level <2){
                        this.createOtherMenu(player, 111, "Bạn đang ở bang "+ player.clan.name+"\n"
                            
                            +"\b|5|Cấp độ bang: "+ player.clan.level+ "\n"
                            +"\b|3|Bang chủ: "+ player.clan.getLeader().name +"\n"
                            +"Mỗi ngày điểm danh sẽ nhận: "+ qua*player.clan.level+" xu bang\n"
                            +"\b|7|Tăng 20% TNSM  Level "+clan.level+ "/2 Để mở khóa\n"
                            +"\b|7|Tăng 1%SĐ,HP,KI Level "+ clan.level+"/3 Để mở khóa\n"
                            +"\b|7|Shop Bang Hội Level "+ clan.level + "/5"+" Để mở khoá\n"
                            +"\b|7|Tăng 5%SĐ,HP,KI Level "+ clan.level+"/8 Để mở khóa\n"
                            +"\b|7|Nâng chỉ số level "+ clan.level +"/10"+" Để mở khóa\n  "
                            
                            
                            , 
                            
                    "Điểm danh", "Nâng cấp \nBang hội","Quyên Góp\n Bang Hội");
                    }else if(clan.level<3){
                        this.createOtherMenu(player, 111, "Bạn đang ở bang "+ player.clan.name+"\n"
                           
                            +"\b|5|Cấp độ bang: "+ player.clan.level+ "\n"
                            +"\b|3|Bang chủ: "+ player.clan.getLeader().name +"\n"
                            +"Mỗi ngày điểm danh sẽ nhận: "+ qua*player.clan.level+" xu bang\n"
                            +"\b|1|Tăng 20% TNSM Đã Mở Khóa\n"
                            +"\b|7|Tăng 1%SĐ,HP,KI Level "+ clan.level+"/3 Để mở khóa\n"
                            +"\b|7|Shop Bang Hội Level "+ clan.level + "/5"+" Để mở khoá\n"
                            +"\b|7|Tăng 5%SĐ,HP,KI Level "+ clan.level+"/8 Để mở khóa\n"
                            +"\b|7|Nâng chỉ số level "+ clan.level +"/10"+" Để mở khóa\n  "
                            
                            
                            , 
                            
                    "Điểm danh", "Nâng cấp \nBang hội","Quyên Góp\n Bang Hội");
                         
                    }else if(clan.level<5){
                        this.createOtherMenu(player, 111, "Bạn đang ở bang "+ player.clan.name+"\n"
                           
                            +"\b|5|Cấp độ bang: "+ player.clan.level+ "\n"
                            +"\b|3|Bang chủ: "+ player.clan.getLeader().name +"\n"
                            +"Mỗi ngày điểm danh sẽ nhận: "+ qua*player.clan.level+" xu bang\n"
                            +"\b|1|Tăng 20% TNSM Đã mở khóa\n"
                            +"\b|1|Tăng 1%SĐ,HP,KI Đã mở khóa\n"
                            +"\b|7|Shop Bang Hội Level "+ clan.level + "/5"+" Để mở khoá\n"
                            +"\b|7|Tăng 5%SĐ,HP,KI Level "+ clan.level+"/8 Để mở khóa\n"
                            +"\b|7|Nâng chỉ số level "+ clan.level +"/10"+" Để mở khóa\n  "
                            
                            
                            , 
                            
                    "Điểm danh", "Nâng cấp \nBang hội","Quyên Góp\n Bang Hội");
                    }else if(clan.level<8){
                        this.createOtherMenu(player, 111, "Bạn đang ở bang "+ player.clan.name+"\n"
                           
                            +"\b|5|Cấp độ bang: "+ player.clan.level+ "\n"
                            +"\b|3|Bang chủ: "+ player.clan.getLeader().name +"\n"
                            +"Mỗi ngày điểm danh sẽ nhận: "+ qua*player.clan.level+" xu bang\n"
                            +"\b|1|Tăng 20% TNSM Đã mở khóa\n"
                            +"\b|1|Tăng 1%SĐ,HP,KI Đã mở khóa\n"
                            +"\b|1|Shop Bang Hội Đã mở khóa\n"
                            +"\b|7|Tăng 5%SĐ,HP,KI Level "+ clan.level+"/8 Để mở khóa\n"
                            +"\b|7|Nâng chỉ số level "+ clan.level +"/10"+" Để mở khóa\n  "
                            
                            
                            , 
                            
                    "Điểm danh", "Nâng cấp \nBang hội","Quyên Góp\n Bang Hội","SHOP BANG");
                    }else if(clan.level<10){
                        this.createOtherMenu(player, 111, "Bạn đang ở bang "+ player.clan.name+"\n"
                           
                            +"\b|5|Cấp độ bang: "+ player.clan.level+ "\n"
                            +"\b|3|Bang chủ: "+ player.clan.getLeader().name +"\n"
                            +"Mỗi ngày điểm danh sẽ nhận: "+ qua*player.clan.level+" xu bang\n"
                            +"\b|1|Tăng 20% TNSM Đã mở khóa\n"
                            +"\b|1|Tăng 1%SĐ,HP,KI Đã mở khóa\n"
                            +"\b|1|Shop Bang Hội Đã mở khóa\n"
                            +"\b|1|Tăng 5%SĐ,HP,KI Đã mở khóa\n"
                            +"\b|7|Nâng chỉ số level "+ clan.level +"/10"+" Để mở khóa\n  "
                            
                            
                            , 
                            
                    "Điểm danh", "Nâng cấp \nBang hội","Quyên Góp\n Bang Hội","SHOP BANG");
                    }else{
                        this.createOtherMenu(player, 111, "Bạn đang ở bang "+ player.clan.name+"\n"
                            
                            +"\b|5|Cấp độ bang: "+ player.clan.level+ "\n"
                            +"\b|3|Bang chủ: "+ player.clan.getLeader().name +"\n"
                            +"Mỗi ngày điểm danh sẽ nhận: "+ qua*player.clan.level+" xu bang\n"
                            +"\b|1|Tăng 20% TNSM Đã mở khóa\n"
                            +"\b|1|Tăng 1%SĐ,HP,KI Đã mở khóa\n"
                            +"\b|1|Shop Bang Hội Đã mở khóa\n"
                            +"\b|1|Tăng 5%SĐ,HP,KI Đã mở khóa\n"
                            +"\b|1|Nâng chỉ số Đã mở khóa\n  "
                            
                            
                            , 
                            
                    "Điểm danh", "Nâng cấp \nBang hội","Quyên Góp\n Bang Hội","SHOP BANG","Nâng chỉ số");
                    }
                
                }
//                case 2->{
//                      TopService.showListTopBang(player);
//                      break;
//                }
                case 2->{
                    if (player.playerTask.clanTask.template != null) {
                                            if (player.playerTask.clanTask.isDone()) {
                                                createOtherMenu(player, 113, "Nhiệm vụ đã hoàn thành, hãy nhận " + ((player.playerTask.clanTask.level + 1) * 10) + " capsule bang", "Nhận\nthưởng", "Đóng");
                                                break;
                                            }
                                            createOtherMenu(player, 113, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
                                        } else {
                                            TaskService.gI().changeClanTask(this, player, (byte) Util.nextInt(5));
                                        }
                }
                
                
            }
            
             }else if(player.iDMark.getIndexMenu() == 111){
                 switch (select) {
                     case 0:
                         if(!player.checkin){
                             
                             int sl = qua*player.clan.level;
                             Item xub = ItemService.gI().createNewItem((short)1854, sl);
                             InventoryService.gI().addItemBag(player, xub);
                             InventoryService.gI().sendItemBag(player);
                             Service.gI().sendThongBao(player, "Bạn nhận được x"+sl+" xu bang");
                             player.checkin = true;
                         }else{
                              Service.gI().sendThongBao(player, "Nhận rồi, Hốc vừa!!!");
                         }
                         break;
                     case 1:
                     { Clan clan = player.clan;
                         clan = player.clan;
                                if (clan != null) {
                                    int level = clan.level;
                                    if (clan.isLeader(player)) {
                                        if (level > 10) {
                                            Service.gI().sendThongBao(player, "Đang ở cấp độ cao nhất.");
                                            return;
                                        }
                                        String npcSay = "Cần " + Util.chiaNho(ClanService.gI().capsule(clan)) + " capsule bang [đang có " + Util.chiaNho(clan.capsuleClan) + " capsule bang] để nâng cấp bang hội lên cấp " + (level + 1);
                                        npcSay += "\n+1 tối đa số lượng thành viên";
                                        if (level > 1) {
                                            npcSay += "\n+1 ô trống tối đa rương bang.";
                                        }
                                        
                                        createOtherMenu(player, 112, npcSay, "Đồng ý", "Từ chối");
                                    }else{
                                        Service.gI().sendThongBao(player, "Chỉ có bang chủ mới đủ đẳng cấp, lom dom bước!!");
                                    }
                                }break;
                     }
                     case 2: 
                         if(player.getSession().actived){
                         createOtherMenu(player, 115, "Quyên góp bang hội để nhận ngay Chiến lực bang và xu bang\n"
                                 +"\b|1| Chan nhẹ: Nhận 1 cs bang và 9 Capsule Bang\n"
                                 +"\b|3| Chan Vừa: Nhận 5 cs bang và 49 Capsule Bang\n"
                                 +"\b|5| Chan Mạnh: Nhận 10 cs Bang và 100 Capsule Bang\n"
                                 , "Chan nhẹ\n10 thỏi vàng", "Chan vừa\n 49 Thỏi vàng","Chan Mạnh luôn!\n 99 Thỏi vàng");
                         }else{
                             Service.gI().sendThongBao(player, "Cần mở thành viên để chan!");
                         }
                                                break;
                     case 3:
                          ShopService.gI().opendShop(player, "SHOP_BANG", false);
                          break;
                     case 4:
                          createOtherMenu(player, 116, "Nâng cấp bang hội jup ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                  +"Mỗi lần nâng sẽ tiêu hao 1 xu bang,Nâng crit sẽ tiêu hao 10 xu bang"
                                  +"\b|7|Tăng thêm HP: "+ player.hpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|5|Tăng thêm MP: "+ player.mpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|3|Tăng thêm DAME: "+ player.damebang+"/"+ 200*player.clan.level + "\n"
                                  +"\b|7|Tăng thêm Crit: "+ player.critbang+"/"+ player.clan.level + "\n"
                                  ,"Nâng HP\n+100HP","Nâng MP\n+100","Nâng DAME\n+5","Nâng Crit\n+1");
                          
                          break;
                         
                 }
        }else if(player.iDMark.getIndexMenu() == 112){
           
                        Clan clan = player.clan;
                        if (clan != null) {
                            if (clan.isLeader(player)) {
                                if (clan.level > 9) {
                                    Service.gI().sendThongBao(player, "Đang ở cấp độ cao nhất.");
                                    return;
                                }
                                int capsuleCan = ClanService.gI().capsule(clan);
                                int capsuleBang = clan.capsuleClan;
                                if (capsuleBang >= capsuleCan) {
                                    clan.capsuleClan -= capsuleCan;
                                    clan.level++;
                                    clan.maxMember++;
                                    Service.gI().sendThongBao(player, "Chúc mừng bang hội của bạn đã lên cấp " + (clan.level));
                                    for (ClanMember cm : player.clan.getMembers()) {
                                        Player pl = Client.gI().getPlayer(cm.id);
                                        if (pl != null) {
                                            ClanService.gI().sendMyClan(player);
                                        }
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không đủ capsule bang, cần " + Util.chiaNho(capsuleCan - capsuleBang) + " capsule bang nữa.");
                                }
                            }
                        }
        }else if(player.iDMark.getIndexMenu() == 113){
            if (player.playerTask.clanTask.template != null) {
                            switch (select) {
                                case 0 -> {
                                    if (player.playerTask.clanTask.isDone()) {
                                        TaskService.gI().payClanTask(player);
                                    }
                                }
                                case 1 -> {
                                    if (!player.playerTask.clanTask.isDone()) {
                                        createOtherMenu(player, 114, "Bạn có chắc muốn hủy nhiệm vụ này?\nNếu hủy nhiệm vụ bạn sẽ mất 1 lượt nhiệm vụ trong ngày.", "Đồng ý", "Từ chối");
                                    }
                                }
                            }
                        }
        }else if(player.iDMark.getIndexMenu() == 114){
            if (player.playerTask.clanTask.template != null) {
                            if (select == 0 && !player.playerTask.clanTask.isDone()) {
                                TaskService.gI().removeClanTask(player);
                            }
                        }
        }else if(player.iDMark.getIndexMenu() == 115){
            switch (select) {
                case 0->{
                    Item tv = InventoryService.gI().findItemBag(player, 457);
                         
                            if (tv == null || tv.quantity < 10) {
                                Service.gI().sendThongBao(player, "Cần 10 thỏi vàng");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, tv, 10);
                            Item xubang = ItemService.gI().createNewItem((short) 1854, 1);
                             InventoryService.gI().addItemBag(player, xubang);
                            InventoryService.gI().sendItemBag(player);
                            
                            player.clan.capsuleClan+=9;
                            for (ClanMember cm : player.clan.getMembers()) {
                        if (cm.id == player.id) {
                            cm.memberPoint += 9;
                            cm.clanPoint += 9;
                            break;
                        }
                    }
                    for (ClanMember cm : player.clan.getMembers()) {
                        Player pl = Client.gI().getPlayer(cm.id);
                        if (pl != null) {
                            ClanService.gI().sendMyClan(player);
                        }
                    }
                            Service.gI().sendThongBao(player, "Bạn nhận được x1 xu bang và 9 Capsule Bang");
                            break;
                }
                case 1->{
                    Item tv = InventoryService.gI().findItemBag(player, 457);
                         
                            if (tv == null || tv.quantity < 49) {
                                Service.gI().sendThongBao(player, "Cần 49 thỏi vàng");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, tv, 49);
                            Item xubang = ItemService.gI().createNewItem((short) 1854, 5);
                             InventoryService.gI().addItemBag(player, xubang);
                            InventoryService.gI().sendItemBag(player);
                            player.clan.capsuleClan+=49;
                            for (ClanMember cm : player.clan.getMembers()) {
                        if (cm.id == player.id) {
                            cm.memberPoint +=49;
                            cm.clanPoint += 49;
                            break;
                        }
                    }
                    for (ClanMember cm : player.clan.getMembers()) {
                        Player pl = Client.gI().getPlayer(cm.id);
                        if (pl != null) {
                            ClanService.gI().sendMyClan(player);
                        }
                    }
                            Service.gI().sendThongBao(player, "Bạn nhận được x5 xu bang và 49 Capsule Bang");
                              break;
                }
                case 2->{
                    Item tv = InventoryService.gI().findItemBag(player, 457);
                         
                            if (tv == null || tv.quantity < 99) {
                                Service.gI().sendThongBao(player, "Cần 99 thỏi vàng");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, tv, 99);
                            Item xubang = ItemService.gI().createNewItem((short) 1854, 10);
                             InventoryService.gI().addItemBag(player, xubang);
                            InventoryService.gI().sendItemBag(player);
                            player.clan.capsuleClan+=100;
                            for (ClanMember cm : player.clan.getMembers()) {
                        if (cm.id == player.id) {
                            cm.memberPoint += 100;
                            cm.clanPoint += 100;
                            break;
                        }
                    }
                    for (ClanMember cm : player.clan.getMembers()) {
                        Player pl = Client.gI().getPlayer(cm.id);
                        if (pl != null) {
                            ClanService.gI().sendMyClan(player);
                        }
                    }
                            Service.gI().sendThongBao(player, "Bạn nhận được x10 xu bang và 100 điểm chiến lực bang");
                              break;
                }
                
            }
            
        }else if(player.iDMark.getIndexMenu() == 116){
            switch (select) {
                
                 case 0->{
                      Item xub = InventoryService.gI().findItemBag(player, 1854);
                      if (xub== null ) {
                                Service.gI().sendThongBao(player, "Cần 1 xu bang");
                                return;
                            }
                      if(player.hpbang<5000*player.clan.level){
                            InventoryService.gI().subQuantityItemsBag(player, xub, 1);
                            player.hpbang+=100;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                            createOtherMenu(player, 116, "Nâng cấp bang hội jup ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                  +"Mỗi lần nâng sẽ tiêu hao 1 xu bang,Nâng crit sẽ tiêu hao 10 xu bang"
                                  +"\b|7|Tăng thêm HP: "+ player.hpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|5|Tăng thêm MP: "+ player.mpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|3|Tăng thêm DAME: "+ player.damebang+"/"+ 200*player.clan.level + "\n"
                                  +"\b|7|Tăng thêm Crit: "+ player.critbang+"/"+ player.clan.level + "\n"
                                  ,"Nâng HP\n+100HP","Nâng MP\n+100","Nâng DAME\n+5","Nâng Crit\n+1");
                          
                          break;
                      }else{
                           Service.gI().sendThongBao(player, "Chỉ số thêm đã max cần nâng cấp bang hội để mở giới hạn");
                      }
                 }
                 case 1->{
                      Item xub = InventoryService.gI().findItemBag(player, 1854);
                      if (xub== null ) {
                                Service.gI().sendThongBao(player, "Cần 1 xu bang");
                                return;
                            }
                      if(player.mpbang<5000*player.clan.level){
                            InventoryService.gI().subQuantityItemsBag(player, xub, 1);
                            player.mpbang+=100;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                        createOtherMenu(player, 116, "Nâng cấp bang hội jup ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                  +"Mỗi lần nâng sẽ tiêu hao 1 xu bang,Nâng crit sẽ tiêu hao 10 xu bang"
                                  +"\b|7|Tăng thêm HP: "+ player.hpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|5|Tăng thêm MP: "+ player.mpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|3|Tăng thêm DAME: "+ player.damebang+"/"+ 200*player.clan.level + "\n"
                                  +"\b|7|Tăng thêm Crit: "+ player.critbang+"/"+ player.clan.level + "\n"
                                  ,"Nâng HP\n+100HP","Nâng MP\n+100","Nâng DAME\n+5","Nâng Crit\n+1");
                          
                          break;
                      }else{
                           Service.gI().sendThongBao(player, "Chỉ số thêm đã max cần nâng cấp bang hội để mở giới hạn");
                      }
                 }case 2->{
                      Item xub = InventoryService.gI().findItemBag(player, 1854);
                      if (xub== null ) {
                                Service.gI().sendThongBao(player, "Cần 1 xu bang");
                                return;
                            }
                      if(player.damebang<200*player.clan.level){
                            InventoryService.gI().subQuantityItemsBag(player, xub, 1);
                            player.damebang+=5;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                        createOtherMenu(player, 116, "Nâng cấp bang hội jup ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                  +"Mỗi lần nâng sẽ tiêu hao 1 xu bang,Nâng crit sẽ tiêu hao 10 xu bang"
                                  +"\b|7|Tăng thêm HP: "+ player.hpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|5|Tăng thêm MP: "+ player.mpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|3|Tăng thêm DAME: "+ player.damebang+"/"+ 200*player.clan.level + "\n"
                                  +"\b|7|Tăng thêm Crit: "+ player.critbang+"/"+ player.clan.level + "\n"
                                  ,"Nâng HP\n+100HP","Nâng MP\n+100","Nâng DAME\n+5","Nâng Crit\n+1");
                          
                          break;
                      }else{
                           Service.gI().sendThongBao(player, "Chỉ số thêm đã max cần nâng cấp bang hội để mở giới hạn");
                      }
                 }
                 case 3->{
                      Item xub = InventoryService.gI().findItemBag(player, 1854);
                      if (xub == null||xub.quantity<10 ) {
                                Service.gI().sendThongBao(player, "Cần 10 xu bang");
                                return;
                            }
                      if(player.critbang<player.clan.level){
                            InventoryService.gI().subQuantityItemsBag(player, xub, 10);
                            player.critbang+=1;
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Nâng cấp chỉ số thành công");
                        createOtherMenu(player, 116, "Nâng cấp bang hội jup ngươi mở khóa giới hạn nâng cấp chỉ số\n"
                                  +"Mỗi lần nâng sẽ tiêu hao 1 xu bang,Nâng crit sẽ tiêu hao 10 xu bang"
                                  +"\b|7|Tăng thêm HP: "+ player.hpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|5|Tăng thêm MP: "+ player.mpbang+"/"+ 5000*player.clan.level + "\n"
                                  +"\b|3|Tăng thêm DAME: "+ player.damebang+"/"+ 200*player.clan.level + "\n"
                                  +"\b|7|Tăng thêm Crit: "+ player.critbang+"/"+ player.clan.level + "\n"
                                  ,"Nâng HP\n+100HP","Nâng MP\n+100","Nâng DAME\n+5","Nâng Crit\n+1");
                          
                          break;
                      }else{
                           Service.gI().sendThongBao(player, "Chỉ số thêm đã max cần nâng cấp bang hội để mở giới hạn");
                      }
                 }
                 
            }
        }
        }
    }
}
