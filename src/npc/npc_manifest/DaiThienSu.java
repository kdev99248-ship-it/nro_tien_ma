package npc.npc_manifest;


import java.sql.Connection;
import java.sql.PreparedStatement;
import consts.ConstDataEventNAP;
import consts.ConstDataEventSM;
import consts.ConstMenu;
import consts.ConstNpc;
import item.Item;
import npc.Npc;
import java.util.logging.Logger;
import player.Player;
import server.Manager;
import services.NpcService;
import services.Service;
import java.util.logging.Level;
import java.sql.SQLException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.sql.ResultSet;
import java.util.Calendar;
import jdbc.DBConnecter;

import matches.TOP;
import org.json.simple.JSONValue;
import player.ArchivementSucManh;
import services.InventoryService;
import services.ItemService;
import services.func.TopService;
import utils.Util;

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstMenu.MENU_SHOW,
                //                "|2|Sự kiện đua Top\n"
                //                + "diễn ra từ " + Manager.timeStartDuaTop + " đến " + Manager.timeEndDuaTop + "\n"
                //                + "Giải thưởng khủng chưa từng có, xem chi tiết tại diễn đàn, fanpage\n"
                //                + Manager.demTimeSuKien(),
                "|0|Đang diễn ra sự kiện đua top mùa 1\n|3|"
                + "Thể lệ đua top mùa 1:\n|4|"
                + "- Thời gian tạo acc tại thời điểm sự kiện diễn ra\n"
                + "- Quà sẽ được gửi về hộp thư tại nhà hoặc quy lão kame sau khi sự kiện hết thúc!",
                "Đua Top\n Sức mạnh", "Đua top\n Nạp", "Top Nhiệm Vụ","Săn boss ngày","Top Sức Đánh");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
//            switch (select) {

            switch (player.iDMark.getIndexMenu()) {
                case ConstMenu.MENU_SHOW:
                    switch (select) {
                        case 0:
                            if (ConstDataEventSM.isRunningSK == false) {
                                Service.gI().sendThongBao(player, "Sự kiện top Sức mạnh đã kết thúc");
                                return;
                            }
                            this.createOtherMenu(player, 1115, "|0|Đua top sức mạnh", "Xem Top","Xem thường", "Đóng");

                            break;
                        case 1:
                            if (ConstDataEventNAP.isRunningSK == false) {
                                Service.gI().sendThongBao(player, "Sự kiện top Nạp đã kết thúc");
                                return;
                            }
                            this.createOtherMenu(player, 1116, "|0|Đua Top Nạp", "Xem Top","Xem Thưởng", "Đóng");
                            break;
                        case 2:
                            TopService.showListTop(player, 0);
                            break;
                        case 3:
                              
                             int rank = Manager.getRankByName(player.name);
                             if(rank>=1&&rank<=10){
                             this.createOtherMenu(player, 1117, "Top săn boss sẽ được làm mới vào mỗi 20h hàng ngày\n"
                                     +"\b|5| TOP 1: 20.000 coin Game\n"
                                     +"\b|3| TOP 2: 10.000 coin Game\n"
                                     +"\b|3| TOP 3: 5.000 coin Game\n"
                                     +"\b|5| TOP 4-10: 2.000 coin Game\n"
                                     + "Bạn đang ở \nTop: "+ rank, "Xem Top","Xem Thưởng", "Nhận quà");
                             }else{
                                 this.createOtherMenu(player, 1117, "Top săn boss sẽ được làm mới vào mỗi 0h hàng ngày\n"
                                     +"Bạn không nằm trong top cố gắng lên đi chứ? ra ngoài kia săn boss đi chứ??", "Xem Top","Xem Thưởng");
                                 
                             }
                            
                            break;
                              
                             
                        case 4:
                            TopService.showListTop(player, 8);
                            break;
                        case 5:
                            TopService.showListTop(player, 12);
                            break;
                    }
                    break;
                case 1117:
                     switch (select) {
                         case 0:
                           
                            TopService.showListTop(player, 10);
                            break;
                         case 1:
                             NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_TOP_NV);
                             break;
                         case 2:
                              
                               Calendar calendar = Calendar.getInstance();
                              int hour = calendar.get(Calendar.HOUR_OF_DAY);
                              if(!player.rewardbossday){
                              if (hour >= 20 && hour <= 21) {
                                  int rank = Manager.getRankByName(player.name);
                                
                                  int tien = 0;
                                  if(rank==1){
                                 
                                   tien = 1791;
                                 
                                  }else if(rank==2){
                                  
                                   tien = 1790;
                                  }else if(rank ==3){
                                  
                                   tien = 1789;
                                  }else {
                                   
                                   tien =1788;
                               }
                              
                                Item tiens = ItemService.gI().createNewItem((short)tien, 1);
                              
                                InventoryService.gI().addItemBag(player, tiens);
                                InventoryService.gI().sendItemBag(player);
                                Service.gI().sendThongBao(player, "Bạn nhận được "+tiens.template.name);
                                player.rewardbossday = true;
                                 
                        }else if(hour<20){
                            Service.gI().sendThongBao(player, "Chưa tới h nhận thưởng");
                        }else{
                             Service.gI().sendThongBao(player, "Đã quá tiem nhận thưởng vui lòng quay lại vào ngày mai");
                        }
                              }else{
                                    Service.gI().sendThongBao(player, "Nay bố nhận rồi còn đâu");
                              }
                     }
                     break;
                case 1115:
                    switch (select) {
                        case 0:
                            Service.gI().sendThongBaoOK(player, TopService.getTopSM());
                            break;
                        case 1: {
                            JSONArray dataArray;
                            JSONObject dataObject;
                            PreparedStatement ps = null;
                            ResultSet rs = null;
                            StringBuilder sb = new StringBuilder();
                            sb.append("|0|꧁TOP SỨC MẠNH:\n"
                                    + "Chỉ dành cho những tài khoản tạo từ thời điểm " + ConstDataEventSM.HOUR_OPEN + "H" + ConstDataEventSM.MIN_OPEN + " ngày " + ConstDataEventSM.DATE_OPEN + "/" + ConstDataEventSM.MONTH_OPEN + "/2025\n"
                                    + "Nhận quà vào ngày " + ConstDataEventSM.HOUR_END + "H" + ConstDataEventSM.MIN_END + " ngày " + ConstDataEventSM.DATE_END + "/" + ConstDataEventSM.MONTH_END + "/2025,  Quà sẽ về hộp thư người chơi꧂\n");
                            try ( Connection con2 = DBConnecter.getConnectionServer()) {
                                ps = con2.prepareStatement("SELECT * FROM moc_suc_manh_top");
                                rs = ps.executeQuery();

                                while (rs.next()) {
                                    dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                                    sb.append("◥_____________________◤\n|7|");
                                    sb.append("✎▶TOP ").append(rs.getInt("id"))
                                            .append("◀\n|0|");
                                    sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");

                                    for (int i = 0; i < dataArray.size(); i++) {
                                        dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                                        int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                                        int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                                        JSONArray optionsArray = (JSONArray) dataObject.get("options");

                                        sb.append("▷ x").append(quantity).append(" ")
                                                .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");

                                        if (optionsArray != null) {
                                            for (int j = 0; j < optionsArray.size(); j++) {
                                                JSONObject optionObject = (JSONObject) optionsArray.get(j);
                                                int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                                                int param = Integer.parseInt(String.valueOf(optionObject.get("param")));

                                                String optionTemplateName = ItemService.gI().getItemOptionTemplate(optionId).name;
                                                String formattedOption = optionTemplateName.replace("#", String.valueOf(param));

                                                sb.append(formattedOption).append("\n");
                                            }
                                        }
                                        sb.append("\n|0|");
                                    }
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(DaiThienSu.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Service.gI().sendThongBaoFromAdmin(player, sb.toString());
                        }
                        break;
//                        case 2: {
//                            JSONArray dataArray;
//                            JSONObject dataObject;
//                            PreparedStatement ps = null;
//                            ResultSet rs = null;
//                            StringBuilder sb = new StringBuilder();
//                            sb.append("|0|꧁__Reset mỗi thứ 2 đầu tuần__꧂\n");
//                            try ( Connection con2 = DBConnecter.getConnectionServer()) {
//                                ps = con2.prepareStatement("SELECT * FROM moc_suc_manh");
//                                rs = ps.executeQuery();
//
//                                while (rs.next()) {
//                                    dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
//                                    sb.append("◥_____________________◤\n|7|");
//                                    sb.append("✎▶Mốc Sức Mạnh ").append(Util.numberToMoney(ArchivementSucManh.POWERGIFT[rs.getInt("id") - 1]))
//                                            .append("◀\n|0|");
//                                    sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");
//
//                                    for (int i = 0; i < dataArray.size(); i++) {
//                                        dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
//                                        int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
//                                        int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
//                                        JSONArray optionsArray = (JSONArray) dataObject.get("options");
//
//                                        sb.append("▷ x").append(quantity).append(" ")
//                                                .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");
//
//                                        if (optionsArray != null) {
//                                            for (int j = 0; j < optionsArray.size(); j++) {
//                                                JSONObject optionObject = (JSONObject) optionsArray.get(j);
//                                                int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
//                                                int param = Integer.parseInt(String.valueOf(optionObject.get("param")));
//
//                                                String optionTemplateName = ItemService.gI().getItemOptionTemplate(optionId).name;
//                                                String formattedOption = optionTemplateName.replace("#", String.valueOf(param));
//
//                                                sb.append(formattedOption).append("\n");
//                                            }
//                                        }
//                                        sb.append("\n|0|");
//                                    }
//                                }
//                            } catch (SQLException ex) {
//                                Logger.getLogger(DaiThienSu.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//
//                            Service.gI().sendThongBaoFromAdmin(player, sb.toString());
//                        }
//                        break;
//                        case 3:
//                            if (player.getSession().actived) {
//                                ArchivementSucManh.gI().getAchievement(player);
//                            } else {
//                                Service.gI().sendThongBao(player, "Mở thành viên tại bardock đi rồi qua đây nhận nhe baby!");
//                            }
//                            break;
                    }
                    break;
                case 1116:
                    switch (select) {
                        case 0:
                            Service.gI().sendThongBaoOK(player, TopService.getTopNap());
                            break;
                        case 1: {
                            JSONArray dataArray;
                            JSONObject dataObject;
                            PreparedStatement ps = null;
                            ResultSet rs = null;
                            StringBuilder sb = new StringBuilder();
                            sb.append("|0|꧁TOP NẠP:\n"
                                    + "Tài khoản cần nạp 100K trở lên để có thể vô top\n"
                                    + "Nhận quà vào ngày " + ConstDataEventNAP.HOUR_END + "H" + ConstDataEventNAP.MIN_END + " ngày " + ConstDataEventNAP.DATE_END + "/" + ConstDataEventNAP.MONTH_END + "/2024, OPEN quà sẽ về hộp thư người chơi꧂\n");
                            try ( Connection con2 = DBConnecter.getConnectionServer()) {
                                ps = con2.prepareStatement("SELECT * FROM moc_nap_top");
                                rs = ps.executeQuery();

                                while (rs.next()) {
                                    dataArray = (JSONArray) JSONValue.parse(rs.getString("detail"));
                                    sb.append("◥_____________________◤\n|7|");
                                    sb.append("✎▶TOP ").append(rs.getInt("id"))
                                            .append("◀\n|0|");
                                    sb.append("◢¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯◣\n|0|");

                                    for (int i = 0; i < dataArray.size(); i++) {
                                        dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                                        int tempid = Integer.parseInt(String.valueOf(dataObject.get("temp_id")));
                                        int quantity = Integer.parseInt(String.valueOf(dataObject.get("quantity")));
                                        JSONArray optionsArray = (JSONArray) dataObject.get("options");

                                        sb.append("▷ x").append(quantity).append(" ")
                                                .append(ItemService.gI().getTemplate(tempid).name).append("\n|4|");

                                        if (optionsArray != null) {
                                            for (int j = 0; j < optionsArray.size(); j++) {
                                                JSONObject optionObject = (JSONObject) optionsArray.get(j);
                                                int optionId = Integer.parseInt(String.valueOf(optionObject.get("id")));
                                                int param = Integer.parseInt(String.valueOf(optionObject.get("param")));

                                                String optionTemplateName = ItemService.gI().getItemOptionTemplate(optionId).name;
                                                String formattedOption = optionTemplateName.replace("#", String.valueOf(param));

                                                sb.append(formattedOption).append("\n");
                                            }
                                        }
                                        sb.append("\n|0|");
                                    }
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(DaiThienSu.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            Service.gI().sendThongBaoFromAdmin(player, sb.toString());
                        }
                        break;
                  }
                    break;

            }
        }
    }
}
