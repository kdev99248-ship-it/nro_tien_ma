package npc.npc_manifest;


import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import services.func.ChangeMapService;
import shop.ShopService;
import utils.Util;

public class JoyBoy extends Npc {

    public JoyBoy(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (mapId) {
                case 29 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Ước Mơ của ta là trở thành vua hải tặc\n"
                            + "Tên người cá đã trộm xu hải tặc của ta hãy jup ta tiêu diệt hắn",
                            "Tới SkyPiea", "Của Hàng", "Giao Vỏ Xò"
                    );
                }
                case 203 -> {
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi có thể farm vỏ xò ở nơi này bằng cách mặc quần đi biển và không mặc cải trang với áo\n"
                            + "Hãy cẩn thận với tên người cá Along",
                            "Về\nVề Nam Kame", "Đóng");
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
                    case 29 -> {
                        if (select == 0) {
                             ChangeMapService.gI().goToSkyPear(player);
                            ChangeMapService.gI().changeMapNonSpaceship(player, 203, 249, 306);
                        }
                        if(select ==1){
                             ShopService.gI().opendShop(player, "SHOP_HE", true);
                        }
                        
                        if(select ==2){
                            Item vooc = InventoryService.gI().findItemBag(player, 695);
                            Item voxo = InventoryService.gI().findItemBag(player, 696);
                            Item concua = InventoryService.gI().findItemBag(player, 697);
                            Item saobien = InventoryService.gI().findItemBag(player, 698);
                            int slvo = 0,slvx = 0,slcc =0,slsb  =0;
                            if(vooc!=null){
                                slvo = vooc.quantity;
                            }
                            if(voxo!=null){
                                slvx = voxo.quantity;
                            }
                            if(concua!=null){
                                slcc = concua.quantity;
                            }
                            if(saobien!=null){
                                slsb = saobien.quantity;
                            }
                            createOtherMenu(player,1, "Thu Thập x99 các loại để nhận rương Hải tặc mở ra những vật phẩm quý giá\n"
                            + "\b|5|Số Vỏ ốc: "+ slvo+ "\n"
                            + "\b|5|Số Vỏ sò: "+ slvx+ "\n"
                            + "\b|5|Số con cua: "+ slcc+ "\n"
                            + "\b|5|Số sao biển: "+ slsb+ "\n"
                            +"\b|3| Đổi rương bạc cần x20 mỗi loại, rương vàng cần x99 mỗi loại",
                                    
                                    
                            " Rương Bạc", " Rương vàng");
                        }
                        
                    }
                    case 203 -> {
                        if (select == 0) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 29, Util.nextInt(700, 800), 432);
                        }
                    }
                   
                }
            }else if (player.iDMark.getIndexMenu() == 1){
                switch (select) {
                      case 0:{
                          if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item vooc = InventoryService.gI().findItemBag(player, 695);
                            Item voxo = InventoryService.gI().findItemBag(player, 696);
                            Item concua = InventoryService.gI().findItemBag(player, 697);
                            Item saobien = InventoryService.gI().findItemBag(player, 698);
                            if(vooc==null||vooc==null||concua==null||saobien==null){
                                Service.gI().sendThongBao(player, "Cần đủ số lượng các vật phẩm");
                                return;
                            }
                            if(vooc.quantity<20||voxo.quantity<20||concua.quantity<20||saobien.quantity<20){
                                Service.gI().sendThongBao(player, "cần x20 mỗi loại");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, vooc, 20);
                            InventoryService.gI().subQuantityItemsBag(player, voxo, 20);
                             InventoryService.gI().subQuantityItemsBag(player, concua, 20);
                             InventoryService.gI().subQuantityItemsBag(player, saobien, 20);
                            Item quatet = ItemService.gI().createNewItem((short) 699, 1);
                            InventoryService.gI().addItemBag(player, quatet);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được 1 Rương bạc");
                            break;
                      }
                      case 1:{
                           if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang");
                                return;
                            }
                            Item vooc = InventoryService.gI().findItemBag(player, 695);
                            Item voxo = InventoryService.gI().findItemBag(player, 696);
                            Item concua = InventoryService.gI().findItemBag(player, 697);
                            Item saobien = InventoryService.gI().findItemBag(player, 698);
                            if(vooc==null||vooc==null||concua==null||saobien==null){
                                Service.gI().sendThongBao(player, "Cần đủ số lượng các vật phẩm");
                                return;
                            }
                            if(vooc.quantity<99||voxo.quantity<99||concua.quantity<99||saobien.quantity<99){
                                Service.gI().sendThongBao(player, "cần x99 mỗi loại");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, vooc, 99);
                            InventoryService.gI().subQuantityItemsBag(player, voxo, 99);
                             InventoryService.gI().subQuantityItemsBag(player, concua, 99);
                             InventoryService.gI().subQuantityItemsBag(player, saobien, 99);
                            Item quatet = ItemService.gI().createNewItem((short) 700, 1);
                            InventoryService.gI().addItemBag(player, quatet);
                            InventoryService.gI().sendItemBag(player);
                            Service.gI().sendThongBao(player, "Bạn nhận được 1 vàng");
                            break;
                      }
                }
            }
           
                }
            }
}  
            
            
        


