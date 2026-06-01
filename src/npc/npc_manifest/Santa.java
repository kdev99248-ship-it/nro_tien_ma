package npc.npc_manifest;
 
import consts.ConstNpc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import npc.Npc;
import player.Player;
import services.InventoryService;
import shop.ShopService;

public class Santa extends Npc {

    public Santa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Cửa hàng",
                    "Mở rộng\nHành trang\nRương đồ",
                    "Tiệm\nHớt tóc",
                    "Danh\nhiệu"
                    
                    
            ));

            
             if (InventoryService.gI().canOpenSantagg(player)){
                
                 menu.add(4,"Cửa Hàng giảm giá");
                 
             }

            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
                if (player.iDMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> // Cửa hàng
                            ShopService.gI().opendShop(player, "SANTA", false);
                        case 1 -> // Mở rộng hành trang
                            ShopService.gI().opendShop(player, "SANTA_MO_RONG_HANH_TRANG", false);
                        case 2 -> // Tiệm hớt tóc
                            ShopService.gI().opendShop(player, "SANTA_HEAD", false);
                        case 3 -> // Danh hiệu
                            ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
                        
                        case 4 -> { // Cửa hàng giảm giá
                            if (InventoryService.gI().canOpenSantagg(player)) {
                                createOtherMenu(player, 6, "Cửa Hàng giảm giá, bán các loại thời trang với giá rẻ", "Mua Ngay", "Từ chối");
                            } else {
                                createOtherMenu(player, 6, "Cần có phiếu giảm giá...\n Rồi hẵng tới đây!!", "OK");
                            }
                        }
                    }
                } else if (player.iDMark.getIndexMenu() == 6) { // Xử lý index 6 sau case 5
                    if (select == 0 && InventoryService.gI().canOpenSantagg(player)) {
                        ShopService.gI().opendShop(player, "SANTAGG", false);
                    }
                }
            }
        }
    }
}
