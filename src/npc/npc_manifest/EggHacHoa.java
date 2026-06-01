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

public class EggHacHoa extends Npc { // Sửa lại tên class cho đúng

    public EggHacHoa(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (canOpenNpc(pl)) { // Kiểm tra điều kiện mở NPC
          
                
                    this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                    "Linh Thú là Loài vật đi theo jup ngươi tăng sức mạnh \n"
                     +"\b|5|Có 4 cấp bậc linh thú khi triệu hồi\n"
                     +"\b|4|Linh thú thường Có 1 dòng chỉ số\n"
                     +"\b|1|Linh Thú Tinh anh có 2 dòng chỉ số\n"
                     +"\b|3|Linh thú huyền thoại có 3 dòng chỉ số\n"
                     +"\b|7|Linh thú truyền kì Có 3 dòng chỉ số và option ẩn\n"
                     +"\b|1| Bạn có thể Phân rã và nâng cấp linh thú tại đây"
                             
                            
                           ,"Nâng cấp","Mở chỉ số","Phân rã\n Linh thú","Info linh thú\n Đang Mang");
                
      
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
              switch (player.iDMark.getIndexMenu()) {
                  case ConstNpc.BASE_MENU -> {
                      switch (select) {
                          case 0-> { // Xử lý khi người chơi chọn Shop
                                this.createOtherMenu(player, ConstMenu.NANG_CAP_LINH_THU,
                                  "Nâng cấp Linh Thú Sẽ giúp Linh Thú có Tỷ lệ tăng lên 1 bậc\n"
                                          + "\b|5|Linh Thú Thường Lên Tinh Anh Tiêu Tốn 5 mảnh Hồn và 1 thỏi vàng tỷ lệ thành công 20%\n"
                                          + "\b|7|Linh Thú Tinh Anh Lên Huyền Thoại Tiêu Tốn 10 mảnh Hồn và 3 thỏi vàng tỷ lệ thành công 10%\n"
                                          + "\b|3|Linh Thú Huyền Thoại Lên Truyền Kỳ Tiêu Tốn 20 mảnh Hồn và 10 thỏi vàng tỷ lệ thành công 1%\n"
                                          
                                        , "Triển","Từ chối");
                            }
                          case 1->{
                                this.createOtherMenu(player, ConstMenu.MO_CHI_SO_LINH_THU,
                                  "\b|5|Mở chỉ số linh thú sẽ tiêu hao 1 linh thú cùng loạivà 10 thỏi vàng"
                                          
                                        , "Mở Thôi","Từ chối");
                             }
                          case 2->{
                                  this.createOtherMenu(player, ConstMenu.PHAN_RA_LINH_THU,
                                  "\b|3|linh thú cấp bậc càng cao sẽ phân rã được càng nhiều Hồn Linh Thú\n"
                                          +"\b|5|Thường 1-3 Hồn linh thú\n"
                                          +"\b|5|Tinh Anh 1-10 Hồn linh thú\n"
                                           +"\b|5|Huyền Thoại 1-30 Hồn linh thú\n"
                                           +"\b|5|Truyền Kì 1-100 Hồn linh thú\n"
                                        , "Phân Rã","Từ chối");
                            }
                          case 3->{
                                boolean check =true;
                                int id;
                                String name = "";
                                String chiso1 = "Không có";
                                String capbac ="Không có";
                                
                               Item item = player.inventory.itemsBody.get(11);
                               if (item == null || item.template == null){
                                   id  =0;
                                   name = "Đang không có linh thú";
                               }else{
                                   id = item.template.id;
                                   name = item.template.name;
                               }
                                switch(id){
                                      case 1811,1642,1643,1652,1655,1664,1695,1490,1489,1492,1493,1494:
                                          capbac = "Thường";
                                          break;
                                      case 1778,1776,1644,1645,1646,1647,1807,1742:
                                          capbac = "\b|5|Tinh Anh";
                                          break;
                                      case 1779,1648,1651,1653,1812:
                                          capbac = "\b|7|Huyền Thoại";
                                          break;
                                      case 1649,1491,1650,1744:
                                          capbac = "\b|3|Truyền Kì";
                                          if(id==1649){
                                              chiso1 ="\b|3| Tốc Đánh + 20%";
                                          }
                                          if(id==1491){
                                              chiso1 ="\b|3| Gây Thiêu Đốt lên boss thêm sát thương bằng 0.05%hp boss ";
                                          }
                                          if(id==1650){
                                               chiso1 ="\b|3| Tăng 20% HP,KI,SĐ cho đệ";
                                          }
                                           if(id==1744){
                                               chiso1 ="\b|3| Tăng 2% Sức đánh với mỗi người trong khu";
                                          }
                                          break;
                                          default:
                   
                                         
                                         break;


                                }
                                 
                     this.createOtherMenu(player, 0,
                    "Thông Tin Linh Thú Đang Mang"
                            + "\b|3|Linh Thú Đang Mang: "+name+"\n"
                            +"\b|1| Cấp Bậc: "+ capbac +"\n"
                            +"\b|1| Chỉ số ẩn : "+ chiso1 +"\n"
                           
                             , "Đóng");
                 
                
                          }
                          
                      }
                  }
                case ConstMenu.NANG_CAP_LINH_THU->{
                      if (select == 0) {
                                CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_LINH_THU);
                            }
                  }
                case ConstMenu.PHAN_RA_LINH_THU->{
                      if (select == 0) {
                                CombineService.gI().openTabCombine(player, CombineService.PHAN_RA_LINH_THU);
                            }
                  }
                case ConstMenu.MO_CHI_SO_LINH_THU->{
                      if (select == 0) {
                                CombineService.gI().openTabCombine(player, CombineService.MO_CHI_SO_LINH_THU);
                            }
                  }
                
                 case ConstNpc.MENU_START_COMBINE -> {
                     switch (player.combine.typeCombine) {
                          case CombineService.NANG_CAP_LINH_THU,CombineService.MO_CHI_SO_LINH_THU,CombineService.PHAN_RA_LINH_THU->{
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
