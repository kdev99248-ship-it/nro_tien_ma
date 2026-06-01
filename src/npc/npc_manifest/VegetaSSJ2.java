package npc.npc_manifest;

import consts.ConstMenu;
import consts.ConstNpc;
import item.Item;
import static java.awt.SystemColor.menu;
import models.Combine.CombineService;
import static models.WorldMartialArtsTournament.WorldMartialArtsTournamentService.menu;
import npc.Npc;
import player.Player;
import services.InventoryService;
import shop.ShopService;

public class VegetaSSJ2 extends Npc { // Sửa lại tên class cho đúng

    public VegetaSSJ2(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (canOpenNpc(pl)) { // Kiểm tra điều kiện mở NPC
            boolean check =true;
            int id;
            String name = "";
            String chiso1 = "";
            String chiso2 = "";
            String chiso3 = "";
            String kchiso = "";
            Item item = pl.inventory.itemsBody.get(5);
            if (item == null || item.template == null){
                id  =0;
                name = "Đang không mặc cải trang";
            }else{
                id = item.template.id;
                name = item.template.name;
            }
            switch(id){
                case 1416,1746,1752,1758:
                    chiso1 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    chiso2 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1417:
                    chiso1 = "\b|5|Biến khỉ +5% HP,SĐ";
                    chiso2 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1418:
                    chiso1 = "\b|5|Biến khỉ +5% HP,SĐ";
                    chiso2 = "\b|5|Tăng 5% Sát thương Đấm Galick";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1419:
                    chiso1 = "\b|5|Biến khỉ +5% HP,SĐ";
                    chiso2 = "\b|5|Tăng 5% Sát thương Đấm Galick";
                    chiso3 = "\b|5|Tăng 10% sát thương bom";
                    break;
                case 1420:
                    chiso1 = "\b|5|Biến khỉ +7% HP,SĐ";
                    chiso2 = "\b|5|Tăng 7% Sát thương Đấm Galick";
                    chiso3 = "\b|5|Tăng 15% sát thương bom";
                    break;
                case 1421:
                    chiso1 = "\b|5|Biến khỉ +10% HP,SĐ";
                    chiso2 = "\b|5|Tăng 10% Sát thương Đấm Galick";
                    chiso3 = "\b|5|Tăng 20% sát thương bom";
                    break;
                case 1747:
                    chiso1 = "\b|5|Tăng 5% sát thương kaioken";
                    chiso2 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1748:
                    chiso1 = "\b|5|Tăng 5% sát thương kaioken";
                    chiso2 = "\b|5|Tăng 5% sát thương kame";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1749:
                    chiso1 = "\b|5|Tăng 5% sát thương kaioken";
                    chiso2 = "\b|5|Tăng 5% sát thương kame";
                    chiso3 = "\b|5|Tăng 5% sát thương chí mạng";
                    break;
                case 1750:
                    chiso1 = "\b|5|Tăng 7% sát thương kaioken";
                    chiso2 = "\b|5|Tăng 7% sát thương kame";
                    chiso3 = "\b|5|Tăng 10% sát thương chí mạng";
                    break;
                case 1751:
                    chiso1 = "\b|5|Tăng 10% sát thương kaioken";
                    chiso2 = "\b|5|Tăng 10% sát thương kame";
                    chiso3 = "\b|5|Tăng 15% sát thương chí mạng";
                    break;
                case 1753:
                    chiso1 = "\b|5|Tăng 5% Sát thương Liên Hoàn";
                    chiso2 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1754:
                    chiso1 = "\b|5|Tăng 5% Sát thương Liên Hoàn";
                    chiso2 = "\b|5|Tăng 5% Tốc ĐỘ Đánh";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1755:
                    chiso1 = "\b|5|Tăng 5% Sát thương Liên Hoàn";
                    chiso2 = "\b|5|Tăng 5% Tốc ĐỘ Đánh";
                    chiso3 = "\b|5|Tăng 10% Sát thương laze";
                    break;
                case 1756:
                    chiso1 = "\b|5|Tăng 7% Sát thương Liên Hoàn";
                    chiso2 = "\b|5|Tăng 7% Tốc ĐỘ Đánh";
                    chiso3 = "\b|5|Tăng 15% Sát thương laze";
                    break;
                case 1757:
                    chiso1 = "\b|5|Tăng 10% Sát thương Liên Hoàn";
                    chiso2 = "\b|5|Tăng 10% Tốc ĐỘ Đánh";
                    chiso3 = "\b|5|Tăng 20% Sát thương laze";
                    break;
                case 1759:
                    chiso1 = "\b|5|Đấm thường tăng 1%HP,KI tối đa 10%";
                    chiso2 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1760:
                    chiso1 = "\b|5|Đấm thường tăng 1%HP,KI tối đa 10%";
                    chiso2 = "\b|5|Đấm thường tăng 1%SĐ tối đa 10%";
                    chiso3 = "\b|3|Nâng cấp để mở khóa thuộc tính";
                    break;
                case 1761:
                    chiso1 = "\b|5|Đấm thường tăng 1%HP,KI tối đa 10%";
                    chiso2 = "\b|5|Đấm thường tăng 1%SĐ tối đa 10%";
                    chiso3 = "\b|5|Đấm thường tăng 1%CM và SĐCM tối đa 5%";
                    break;
                case 1762:
                    chiso1 = "\b|5|Đấm thường tăng 1%HP,KI tối đa 15%";
                    chiso2 = "\b|5|Đấm thường tăng 1%SĐ tối đa 15%";
                    chiso3 = "\b|5|Đấm thường tăng 1%CM và SĐCM tối đa 8%";
                    break;
                 case 1763:
                    chiso1 = "\b|5|Đấm thường tăng 1%HP,KI tối đa 20%";
                    chiso2 = "\b|5|Đấm thường tăng 1%SĐ tối đa 20%";
                    chiso3 = "\b|5|Đấm thường tăng 1%CM và SĐCM tối đa 12%";
                    break;
                case 1119:
                    chiso1 = "\b|5|Trạng thái 1 Biến khỉ tăng 20%HP và 10% Sát thương bom";
                    chiso2 = "\b|5|Trạng thái 2 Biến khỉ tăng 20%HP và 10% Sát thương laze";
                    chiso3 = "\b|5|Trang thái 3 Biến khỉ tăng 20%SĐ và 10% Tốc Đánh";
                     break;
               
                    default:
                    kchiso = "Không phải cải trang có thuộc tính ẩn";
                    check  = false;
                    break;
                    
                    
            }
                if(check){
                     this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                    "Nơi Đây giúp nâng cấp cải trang trở lên vip hơn"
                            + "\b|3|Cải trang đang mặc: "+name+"\n"
                            +"\b|1| Chỉ số ẩn 1: "+ chiso1 +"\n"
                            +"\b|1| Chỉ số ẩn 2: "+ chiso2 +"\n"
                            +"\b|1| Chỉ số ẩn 3: "+ chiso3 +"\n"
                             , "Shop Vegeta","Cải trang VIP","Tiến Cấp CT");
                 
                }else{
                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                    "Nơi Đây giúp nâng cấp cải trang trở lên vip hơn"
                             + "\b|3|Cải trang đang mặc: "+name+"\n"
                              +"\b|1| Chỉ số ẩn: "+ kchiso +"\n"
                            + "Ngươi đang mặc", "Shop Vegeta","Cải trang VIP","Tiến Cấp CT");
                }
      
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
              switch (player.iDMark.getIndexMenu()) {
                  case ConstNpc.BASE_MENU -> {
                      switch (select) {
                          case 0-> { // Xử lý khi người chơi chọn Shop
                                 ShopService.gI().opendShop(player, "VegetaShop", true);
                            }
                          case 1->{
                                ShopService.gI().opendShop(player, "Manh_CT", true);
                             }
                          case 2->{
                                  this.createOtherMenu(player, ConstMenu.TIEN_CAP_CAITRANG,
                                  "Cải trang tối thượng ở đây", "Tiến cấp Vegeta","Tiến Cấp Trunk","Tiến cấp Namec","Tiến Cấp Broly");
                            }
                          
                      }
                  }
                case ConstMenu.TIEN_CAP_CAITRANG->{
                      switch (select) {
                          case 0->{
                              createOtherMenu(player, ConstMenu.TIEN_CAP_VEGETA, "Ngươi muốn nâng cấp Vegeta?", "Tiến Cấp", "Từ chối");
                              
                          }
                          case 1->{
                              createOtherMenu(player, ConstMenu.TIEN_CAP_TRUNK, "Ngươi muốn nâng cấp Trunk?", "Tiến Cấp", "Từ chối");
                          }
                           case 2->{
                              createOtherMenu(player, ConstMenu.TIEN_CAP_NAMEC, "Ngươi muốn nâng cấp Namec?", "Tiến Cấp", "Từ chối");
                          }
                           case 3->{
                              createOtherMenu(player, ConstMenu.TIEN_CAP_BROLY, "Ngươi muốn nâng cấp Namec?", "Tiến Cấp", "Từ chối");
                          }
                      }
                  }
                case ConstMenu.TIEN_CAP_VEGETA-> {
                            if (select == 0) {
                                CombineService.gI().openTabCombine(player, CombineService.TIEN_CAP_VEGETA);
                            }
                        }
                case ConstMenu.TIEN_CAP_TRUNK -> {
                            if (select == 0) {
                                CombineService.gI().openTabCombine(player, CombineService.TIEN_CAP_TRUNK);
                            }
                        }
                
                case ConstMenu.TIEN_CAP_NAMEC -> {
                      if (select == 0) {
                                CombineService.gI().openTabCombine(player, CombineService.TIEN_CAP_NAMEC);
                            }
                 }
                case ConstMenu.TIEN_CAP_BROLY-> {
                      if (select == 0) {
                                CombineService.gI().openTabCombine(player, CombineService.TIEN_CAP_BROLY);
                            }
                 }
                 case ConstNpc.MENU_START_COMBINE -> {
                     switch (player.combine.typeCombine) {
                          case CombineService.TIEN_CAP_VEGETA,CombineService.TIEN_CAP_TRUNK ,CombineService.TIEN_CAP_NAMEC,CombineService.TIEN_CAP_BROLY->{
                               switch (select) {
                                        case 0 ->
                                           CombineService.gI().startCombine(player);
                                          
                                            
                                       
                                    }
                          }
                     }
                 }
              }
           
        }
    }
}
