package services;

/*
 *
 *
 * @author EMTI
 */

import consts.ConstNpc;
import npc.Npc;
import npc.NpcFactory;
import player.Player;
import server.Manager;
import network.Message;
import utils.Logger;

public class NpcService {

    private static NpcService i;

    public static NpcService gI() {
        if (i == null) {
            i = new NpcService();
        }
        return i;
    }

    public void createMenuRongThieng(Player player, int indexMenu, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.RONG_THIENG, -1, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.CON_MEO, avatar, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String[] menuSelect,
            Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        createMenuConMeo(player, indexMenu, avatar, npcSay, menuSelect);
    }

    private void createMenu(Player player, int indexMenu, byte npcTempId, int avatar, String npcSay,
            String... menuSelect) {
        if (player == null || !player.isPl() || player.iDMark == null) {
            return;
        }
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(npcTempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(NpcService.class, e);
        }
    }

    public void createTutorial(Player player, int avatar, String npcSay) {
        Message msg;
        try {
            msg = new Message(38);
            msg.writer().writeShort(ConstNpc.CON_MEO);
            msg.writer().writeUTF(npcSay);
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createTutorial(Player player, int tempId, int avatar, String npcSay) {
        Message msg;
        try {
            msg = new Message(38);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public int getAvatar(int npcId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == npcId) {
                return npc.avartar;
            }
        }
        return 1139;
    }

    public void createBigMessage(Player player, int avatar, String npcSay, byte type, String select, String confirn) {
        Message msg;
        try {
            msg = new Message(-70);
            msg.writer().writeShort(avatar);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(type);
            if (type == 1) {
                msg.writer().writeUTF(confirn);// select
                msg.writer().writeUTF(select);// string Select
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ex) {
        }
    }

    public void sendPanelCheBien(Player player) {
        sendPanelCheBien(player, (byte) 0);
    }

    public void updateCookingSlots(Player player) {
        sendPanelCheBien(player, (byte) 1);
    }

    private void sendPanelCheBien(Player player, byte action) {
        Message msg;
        try {
            msg = new Message(-114);
            msg.writer().writeByte(action); // type action: 0 = full, 1 = update only
            if (action == 0) {
                msg.writer().writeByte(server.Manager.ITEM_NHA_BEP.size());
                for (item.ItemNhaBep inb : server.Manager.ITEM_NHA_BEP) {
                    msg.writer().writeShort(inb.id);
                    msg.writer().writeShort((short) inb.item_id);
                    msg.writer().writeInt(inb.thoi_gian_nau);
                    msg.writer().writeShort(inb.don_gia_id);
                    msg.writer().writeInt(inb.gia);
                    msg.writer().writeByte(inb.nguyen_lieu.length);
                    for (int i = 0; i < inb.nguyen_lieu.length; i++) {
                        msg.writer().writeShort(inb.nguyen_lieu[i]);
                        msg.writer().writeShort(inb.soluong_nguyen_lieu[i]);
                    }
                }
            }
            // gửi data cooking
            String cookingData = player.dataCooking != null ? player.dataCooking : "";
            msg.writer().writeUTF(cookingData);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception ex) {
            Logger.logException(NpcService.class, ex);
        }
    }
}
