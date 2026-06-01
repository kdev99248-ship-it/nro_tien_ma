package npc.npc_manifest;

/**
 *
 * @author EMTI
 */
import consts.ConstNpc;
import consts.ConstTask;
import item.Item;
import java.util.Random;
import jdbc.daos.PlayerDAO;
import map.Map;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.MapService;
import services.NpcService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import shop.ShopService;
import utils.Util;

public class KyNgo extends Npc {

    private final byte COUNT_CHANGE = 1;
    private int count;

    public KyNgo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private void checkyngo(Player player) {
        
        count++;
        if (this.count >= COUNT_CHANGE) {
            count = 0;
            this.map.npcs.remove(this);
            Map kyngo = MapService.gI().getMapForKyNgo();
            this.mapId = kyngo.mapId;
            this.cx = Util.nextInt(100, kyngo.mapWidth - 100);
            this.cy = kyngo.yPhysicInTop(this.cx, 0);
            this.map = kyngo;
            Service.gI().sendThongBao(player, "Có Duyên sẽ gặp lại");
            this.map.npcs.add(this);
            
            System.out.println("kỳ Ngộ xuất hiện tại map " + kyngo.mapName);
        }

    }

    @Override
    public void openBaseMenu(Player player) {

        player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
        if (this.mapId != player.zone.map.mapId) {
            Service.gI().sendThongBao(player, "Có thằng đớp trc rồi,Hẹn gặp lại");
            Service.gI().hideWaitDialog(player);
            return;
        }
         if (InventoryService.gI().canOpenKyNgo(player)){
                
                 this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "May mắn lắm mới gặp được ta đó?? Tặng quà cho ta hay nhận chùa??", "Chan Chùa", "Tặng Tiền","SHOP Kỳ Ngộ");
             }else{
              this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "May mắn lắm mới gặp được ta đó?? Tặng quà cho ta hay nhận chùa??", "Chan Chùa", "Tặng Tiền");
         }
       

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (player.iDMark.isBaseMenu()) {
            switch (select) {
                case 0 -> {
                     this.createOtherMenu(player, 1,
                "Gặp Được ta là cái duyên mà ngươi chỉ chan chùa, ngươi có chắc không ?", "Đồng Ý", "Từ Chối");
                }
                case 1 -> {
                     this.createOtherMenu(player, 2,
                "\b|5|Tùy vào lòng hảo tâm của ngươi ta sẽ cho ngươi các phần quà ngẫu nhiên ?\n"
                        + "\n\b|7|Bạn đang có :" + player.getSession().cash + " VND\n|4|", "Chan 5k", "Cho Hẳn 20k","50k Cầm lấy", "Từ chối");

                }
                case 2->{
                    int shop = Util.nextInt(1,4);
                    if(shop==1){
                         ShopService.gI().opendShop(player, "Ky_ngo1", false);
                    }else if(shop==2){
                        ShopService.gI().opendShop(player, "Ky_ngo2", false);
                    }else if(shop==3){
                         ShopService.gI().opendShop(player, "ky_ngo3", false);
                    }else{
                         ShopService.gI().opendShop(player, "ky_ngo4", false);
                    }
                    checkyngo(player);
                }
                default ->
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        }else if(player.iDMark.getIndexMenu() ==1){
              switch (select) {
                  case 0->{
                      int[] list  ={17,18,19,20,457,1837,16,1636,1731};
                       Item bas = ItemService.gI().createNewItem((short) list[Util.nextInt(0,list.length-1)],1);
                        InventoryService.gI().addItemBag(player, bas);
                        InventoryService.gI().sendItemBag(player);
                         Service.gI().sendThongBao(player, "Ta tặng ngươi x1 "+ bas.template.name+" Tạm biệt nha thằng kẹt xỉn");
                         checkyngo(player);
                  }
                      
              }
        }else if(player.iDMark.getIndexMenu() ==2){
              switch (select) {
                  case 0->{
                       if (player.getSession().cash < 5000) {
                             Service.gI().sendThongBao(player, "5k không có bày đặt");
                            return;
                        }
                        PlayerDAO.subcash(player, 5000);
                       int sl = 1;
                      int[] list  ={14,15,457,1796,457,1837,16,1788,1800,1808,1823,1860,1536,1636,648,1727,1728,1830};
                     
                      int qua = list[Util.nextInt(0,list.length-1)];
                      if(qua==16||qua==1636){
                          sl = Util.nextInt(3,8);
                      }
                      if(qua==1800||qua==1808||qua==1823||qua==1860||qua==1796){
                          sl = Util.nextInt(5,15);
                      }
                      if(qua==457){
                          sl =Util.nextInt(30,80);
                      }
                       Item bas = ItemService.gI().createNewItem((short) qua,sl);
                        InventoryService.gI().addItemBag(player, bas);
                        InventoryService.gI().sendItemBag(player);
                         Service.gI().sendThongBao(player, "Ta tặng ngươi x"+sl+" " +bas.template.name+" Xia Xìa");
                         checkyngo(player); 
                  }
                   case 1->{
                       if (player.getSession().cash < 20000) {
                             Service.gI().sendThongBao(player, "20k Không Có à??");
                            return;
                        }
                        PlayerDAO.subcash(player, 20000);
                       int sl = 1;
                      int[] list  ={1536,457,1636,720,16,1800,1808,1823,1860,1796};
                     
                      int qua = list[Util.nextInt(0,list.length-1)];
                      if(Util.isTrue(10,100)){
                          qua = 1792;
                      }
                      if(qua==16||qua==1636){
                          sl = Util.nextInt(10,20);
                      }
                      if(qua==720){
                          sl = Util.nextInt(5,15);
                      }
                      if(qua==1800||qua==1808||qua==1823||qua==1860||qua==1796){
                          sl = Util.nextInt(20,50);
                      }
                      if(qua==457){
                          sl =Util.nextInt(150,400);
                      }
                       Item bas = ItemService.gI().createNewItem((short) qua,sl);
                        InventoryService.gI().addItemBag(player, bas);
                        InventoryService.gI().sendItemBag(player);
                         Service.gI().sendThongBao(player, "Ta tặng ngươi x"+sl+" " +bas.template.name+" Xia Xìa");
                         checkyngo(player); 
                  }
                   case 2->{
                       if (player.getSession().cash < 50000) {
                             Service.gI().sendThongBao(player, "50k Không Có à??");
                            return;
                        }
                        PlayerDAO.subcash(player, 50000);
                       int sl = 1;
                      int[] list  ={457,720,1228};
                     
                      int qua = list[Util.nextInt(0,list.length-1)];
                      if(Util.isTrue(10,100)){
                         Random random = new Random();
                         int rand = random.nextInt(100);
                         if(rand<50){
                             qua=1793;
                         }else if(rand<80){
                             if(player.gender==0){
                                 qua=1746;
                             }else if(player.gender==2){
                                 qua=1416;
                             }else{
                                 qua =1752;
                             }
                         }else{
                             qua = 1758;
                         }
                      }
                      
                       if(qua==720){
                          sl =Util.nextInt(15,40);
                      }
                      if(qua==457){
                          sl =Util.nextInt(300,1000);
                      }
                       Item bas = ItemService.gI().createNewItem((short) qua,sl);
                       if(qua==1758||qua==1752||qua==1416||qua==1746){
                           bas.itemOptions.add(new Item.ItemOption(50,20 ));
                           bas.itemOptions.add(new Item.ItemOption(77,20 ));
                           bas.itemOptions.add(new Item.ItemOption(103,20 ));
                           bas.itemOptions.add(new Item.ItemOption(5,5 ));
                           bas.itemOptions.add(new Item.ItemOption(72,1 ));
                      }
                        InventoryService.gI().addItemBag(player, bas);
                        InventoryService.gI().sendItemBag(player);
                         Service.gI().sendThongBao(player, "Ta tặng ngươi x"+sl+" " +bas.template.name+"Đại gia");
                         checkyngo(player); 
                  }
                      
              }
        }
    }
}
