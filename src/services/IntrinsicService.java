package services;

/*
 *
 *
 * @author EMTI
 */

import consts.ConstNpc;
import intrinsic.Intrinsic;
import item.Item;
import player.Player;
import server.Manager;
import network.Message;
import utils.Util;
import java.util.List;

public class IntrinsicService {

    private static IntrinsicService I;
    private static final int[] COST_OPEN = {10, 20, 40, 80, 160, 320, 640, 1280};

    public static IntrinsicService gI() {
        if (IntrinsicService.I == null) {
            IntrinsicService.I = new IntrinsicService();
        }
        return IntrinsicService.I;
    }

    public List<Intrinsic> getIntrinsics(byte playerGender) {
        switch (playerGender) {
            case 0:
                return Manager.INTRINSIC_TD;
            case 1:
                return Manager.INTRINSIC_NM;
            default:
                return Manager.INTRINSIC_XD;
        }
    }

    public Intrinsic getIntrinsicById(int id) {
        for (Intrinsic intrinsic : Manager.INTRINSICS) {
            if (intrinsic.id == id) {
                return new Intrinsic(intrinsic);
            }
        }
        return null;
    }

    public void sendInfoIntrinsic(Player player) {
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.playerIntrinsic.intrinsic.icon);
            msg.writer().writeUTF(player.playerIntrinsic.intrinsic.getName());
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showAllIntrinsic(Player player) {
        List<Intrinsic> listIntrinsic = getIntrinsics(player.gender);
        Message msg;
        try {
            msg = new Message(112);
            msg.writer().writeByte(1);
            msg.writer().writeByte(1); //count tab
            msg.writer().writeUTF("Nội tại");
            msg.writer().writeByte(listIntrinsic.size() - 1);
            for (int i = 1; i < listIntrinsic.size(); i++) {
                msg.writer().writeShort(listIntrinsic.get(i).icon);
                msg.writer().writeUTF(listIntrinsic.get(i).getDescription());
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
public void settltd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_TLTD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nThiên Xin Hăn", "Set\nGenki", "Set\nKamejoko", "Từ chối");

    }
public void setkhtd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_KHTD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nThiên Xin Hăn", "Set\nGenki", "Set\nKamejoko", "Từ chối");

    }



    public void settlnm(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_TLNM, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nLiên Hoàn", "Set\nỐc Tiêu", "Set\nPikkoro Daimao", "Từ chối");

    }
    public void setkhnm(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_KHNM, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nLiên Hoàn", "Set\nỐc Tiêu", "Set\nPikkoro Daimao", "Từ chối");

    }

    public void settlxd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_TLXD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nKakarot", "Set\nCadic", "Set\nNappa", "Từ chối");

    }
    public void setkhxd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_KHXD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nKakarot", "Set\nCadic", "Set\nNappa", "Từ chối");

    }

    public void sethdtd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_HDTD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nTien Xin Han", "Set\nGenki", "Set\nKamejoko", "Từ chối");

    }

    public void sethdnm(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_HDNM, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nLiên Hoàn", "Set\nỐc Tiêu", "Set\nPikkoro Daimao","Từ chối");

    }

    public void sethdxd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_HDXD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nKakarot", "Set\nCadic", "Set\nNappa", "Từ chối");

    }
    public void settd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_TD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nThiên Xin Hăn", "Set\nGenki", "Set\nKamejoko", "Từ chối");

    }

    public void setnm(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_NM, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nLiên Hoàn", "Set\nỐc Tiêu", "Set\nPikkoro Daimao", "Từ chối");

    }

    public void setxd(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.SET_XD, -1,
                "chọn lẹ đi để tau đi chơi với ny", "Set\nKakarot", "Set\nCadic", "Set\nNappa", "Từ chối");

    }
    public void showMenu(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.INTRINSIC, -1,
                "Nội tại là một kỹ năng bị động hỗ trợ đặc biệt\nBạn có muốn mở hoặc thay đổi nội tại không?",
                "Xem\ntất cả\nNội Tại", "Mở\nNội Tại", "Mở VIP", "Từ chối");
    }

    public void showConfirmOpen(Player player) {
//        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC, -1, "Bạn muốn đổi Nội Tại khác\nvới giá là "
//                + COST_OPEN[player.playerIntrinsic.countOpen] + " Tr vàng ?", "Mở\nNội Tại", "Từ chối");
NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC, -1, "Bạn muốn đổi Nội Tại khác\nvới "
                + (COST_OPEN[player.playerIntrinsic.countOpen]/10) + " Phiếu nội tại?", "Mở\nNội Tại", "Từ chối");
    }

    public void showConfirmOpenVip(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_OPEN_INTRINSIC_VIP, -1,
                "Bạn có muốn mở Nội Tại\nvới giá là 5 Thỏi Vàng và\ntái lập giá vàng quay lại ban đầu không?", "Mở\nNội VIP", "Từ chối");
    }

    private void changeIntrinsic(Player player) {
        List<Intrinsic> listIntrinsic = getIntrinsics(player.gender);
        player.playerIntrinsic.intrinsic = new Intrinsic(listIntrinsic.get(Util.nextInt(1, listIntrinsic.size() - 1)));
        player.playerIntrinsic.intrinsic.param1 = (short) Util.nextInt(player.playerIntrinsic.intrinsic.paramFrom1, player.playerIntrinsic.intrinsic.paramTo1);
        player.playerIntrinsic.intrinsic.param2 = (short) Util.nextInt(player.playerIntrinsic.intrinsic.paramFrom2, player.playerIntrinsic.intrinsic.paramTo2);
        Service.gI().sendThongBao(player, "Bạn nhận được Nội tại:\n" + player.playerIntrinsic.intrinsic.getName().substring(0, player.playerIntrinsic.intrinsic.getName().indexOf(" [")));
        sendInfoIntrinsic(player);
    }

    public void open(Player player) {
        if (player.nPoint.power >= 10000000000L) {
            Item pnt = InventoryService.gI().findItemBag(player, 1397);
//            int goldRequire = COST_OPEN[player.playerIntrinsic.countOpen] * 1000000;
        int sl = 0;
        if(pnt!=null){
            sl = pnt.quantity;
        }
         int goldRequire = COST_OPEN[player.playerIntrinsic.countOpen] /10;
         if (pnt == null || pnt.quantity < goldRequire) {
                 Service.gI().sendThongBao(player, "Bạn không đủ phiếu nội tại , còn thiếu "
                        + Util.numberToMoney(goldRequire -sl) + " vàng nữa");
             }else{
               InventoryService.gI().subQuantityItemsBag(player, pnt, goldRequire);
                InventoryService.gI().sendItemBag(player);
              
                changeIntrinsic(player);
                if (player.playerIntrinsic.countOpen < COST_OPEN.length - 1) {
                    player.playerIntrinsic.countOpen++;
                }
         }
//            if (player.inventory.gold >= goldRequire) {
//                player.inventory.gold -= goldRequire;
//                PlayerService.gI().sendInfoHpMpMoney(player);
//                changeIntrinsic(player);
//                if (player.playerIntrinsic.countOpen < COST_OPEN.length - 1) {
//                    player.playerIntrinsic.countOpen++;
//                }
//            } else {
//                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
//                        + Util.numberToMoney(goldRequire - player.inventory.gold) + " vàng nữa");
//            }
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu sức mạnh tối thiểu 10 tỷ");
        }
    }

    public void openVip(Player player) {
        if (player.nPoint.power >= 10000000000L) {
            Item tv = InventoryService.gI().findItemBag(player, 457);
            int sl = 0;
           if(tv!=null){
            sl = tv.quantity;
        }
           if(tv==null||tv.quantity<5){
               Service.gI().sendThongBao(player, "Bạn không có Đủ thỏi vàng, còn thiếu "
                        + (5 - sl) + " THỎI VÀNG");
           }else{
               InventoryService.gI().subQuantityItemsBag(player, tv, 5);
               InventoryService.gI().sendItemBag(player);                
                changeIntrinsic(player);
                player.playerIntrinsic.countOpen = 0;
           }
//            int gemRequire = 10;
//            if (player.inventory.gem >= 10) {
//                player.inventory.gem -= gemRequire;
//                player.nPoint.kimcuongday+=gemRequire;
//                PlayerService.gI().sendInfoHpMpMoney(player);
//                changeIntrinsic(player);
//                player.playerIntrinsic.countOpen = 0;
//            } else {
//                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc, còn thiếu "
//                        + (gemRequire - player.inventory.gem) + " ngọc nữa");
//            }
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu sức mạnh tối thiểu 10 tỷ");
        }
    }

}
