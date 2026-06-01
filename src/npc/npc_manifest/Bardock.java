package npc.npc_manifest;

import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstTask;
import npc.Npc;
import player.Player;
import services.NpcService;
import services.Service;
import services.TaskService;
import shop.ShopService;
import utils.Util;

public class Bardock extends Npc {

    public Bardock(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                 int taskId = TaskService.gI().getIdTask(player);
                        switch (taskId) {
                            case ConstTask.TASK_31_2->this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Tôi tên là Bardock,người Xayda,Hành tinh của tôi vừa bị Fide phá Hủy\n"
                                    + "Không biết tại sao tôi thoát chết...\n"
                                    + "Và xuất hiện ở nơi này nữa\n"
                                    + "Tôi đang bị thương cậu có thể giúp tôi hạ đám lĩnh ngoài kia không?","Ok để anh","Không bé");
                         
                            default -> this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Tìm tôi làm gì cút");
                            
                
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
                        TaskService.gI().doneTask(player, ConstTask.TASK_31_2);
                        Service.gI().sendThongBao(player, "Chiến tiếp thôi");
                }
            }
        }
    }
}
