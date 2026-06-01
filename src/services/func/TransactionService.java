package services.func;

/*
 *
 *
 * @author EMTI
 */
import EMTI.Functions;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;
import player.Player;
import network.Message;
import server.Client;
import server.Maintenance;
import services.Service;
import utils.Logger;
import utils.TimeUtil;
import utils.Util;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TransactionService implements Runnable {

    private static final int TIME_DELAY_TRADE = 30000;

    static final Map<Player, Trade> PLAYER_TRADE = new HashMap<>();

    private static final byte SEND_INVITE_TRADE = 0;
    private static final byte ACCEPT_TRADE = 1;
    private static final byte ADD_ITEM_TRADE = 2;
    private static final byte CANCEL_TRADE = 3;
    private static final byte LOCK_TRADE = 5;
    private static final byte ACCEPT = 7;

    private static TransactionService i;

    private TransactionService() {
    }

    public static TransactionService gI() {
        if (i == null) {
            i = new TransactionService();
            new Thread(i).start();
        }
        return i;
    }

    public void controller(Player pl, Message msg) {
        try {
            byte action = msg.reader().readByte();
            int playerId = -1;
            Player plMap = null;
            Trade trade = PLAYER_TRADE.get(pl);
            if (pl.baovetaikhoan) {
                Service.gI().sendThongBao(pl, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                return;
            }
            if (!pl.getSession().actived) {
                Service.gI().sendThongBao(pl, "Vui lòng kích hoạt thành viên");
                return;
            }
            if (action == SEND_INVITE_TRADE) {
                pl.iDMark.setTransactionWP(false);
                pl.iDMark.setTransactionWVP(false);
            }
//            if (pl.iDMark.isTransactionWP()) {
//                TransactionWPService.gI().controller(pl, action, msg);
//            }
//            if (pl.iDMark.isTransactionWVP()) {
//                TransactionWVPService.gI().controller(pl, action, msg, plMap);
//                return;
//            }
            switch (action) {
                case SEND_INVITE_TRADE:
                case ACCEPT_TRADE:
                    playerId = msg.reader().readInt();
                    plMap = pl.zone.getPlayerInMap(playerId);
                    if (plMap != null && plMap.isPl()) {
                        if (plMap.tradeWVP) {
                            return;
                        }
                        trade = PLAYER_TRADE.get(pl);
                        if (trade == null) {
                            trade = PLAYER_TRADE.get(plMap);
                        }
                        if (trade == null) {
                            if (action == SEND_INVITE_TRADE) {
                                if (Util.canDoWithTime(pl.iDMark.getLastTimeTrade(), TIME_DELAY_TRADE)
                                        && Util.canDoWithTime(plMap.iDMark.getLastTimeTrade(), TIME_DELAY_TRADE)) {
                                    boolean checkLogout1 = false;
                                    boolean checkLogout2 = false;
                                    try ( Connection con = DBConnecter.getConnectionServer()) {
                                        checkLogout1 = PlayerDAO.checkLogout(con, pl);
                                        checkLogout2 = PlayerDAO.checkLogout(con, plMap);
                                    } catch (Exception e) {
                                    }
                                    if (checkLogout1) {
                                        Client.gI().kickSession(pl.getSession());
                                        break;
                                    }
                                    if (checkLogout2) {
                                        Client.gI().kickSession(plMap.getSession());
                                        break;
                                    }
                                    if (pl.nPoint.power < 1_000_000_000L) {
                                        Service.gI().sendThongBao(pl, "Đạt 1 tỷ SM moi giao dich duoc");
                                        return;
                                    }
                                    if (plMap.nPoint.power < 1_000_000_000L) {
                                        Service.gI().sendThongBao(plMap, "Đạt 1 tỷ SM moi giao dich duoc");
                                        return;
                                    }
                                    pl.iDMark.setLastTimeTrade(System.currentTimeMillis());
                                    pl.iDMark.setPlayerTradeId((int) plMap.id);
                                    sendInviteTrade(pl, plMap);
                                } else {
                                    Service.gI().sendThongBao(pl, "Thử lại sau "
                                            + TimeUtil.getTimeLeft(Math.max(pl.iDMark.getLastTimeTrade(), plMap.iDMark.getLastTimeTrade()), TIME_DELAY_TRADE / 1000));
                                }
                            } else {
                                if (plMap.iDMark.getPlayerTradeId() == pl.id) {
                                    trade = new Trade(pl, plMap);
                                    trade.openTabTrade();
                                }
                            }

                        } else {
                            Service.gI().sendThongBao(pl, "Không thể thực hiện");
                        }
                    }
                    break;
                case ADD_ITEM_TRADE:
                    if (trade != null) {
                        byte index = msg.reader().readByte();
                        int quantity = msg.reader().readInt();
                        if (quantity < 0) {
                            Service.gI().sendThongBao(pl, "Không thể thực hiện");
                            trade.cancelTrade();
                            break;
                        }
                        if (quantity == 0) {//do
                            quantity = 1;
                        }
                        if (index != -1 && quantity > Trade.QUANLITY_MAX) {
                            Service.gI().sendThongBao(pl, "Đã quá giới hạn giao dịch...");
                            trade.cancelTrade();
                            break;
                        }
                        trade.addItemTrade(pl, index, quantity);
                    }
                    break;
                case CANCEL_TRADE:
                    if (trade != null) {
                        trade.cancelTrade();
                    }
                    break;
                case LOCK_TRADE:
                    if (Maintenance.isRunning) {
                        trade.cancelTrade();
                        break;
                    }
                    if (trade != null) {
                        trade.lockTran(pl);
                    }
                    break;
                case ACCEPT:
                    if (Maintenance.isRunning) {
                        trade.cancelTrade();
                        break;
                    }
                    if (trade != null) {
                        trade.acceptTrade();
                        if (trade.accept == 1) {
                            Service.gI().sendThongBao(pl, "Xin chờ đối phương đồng ý");
                        } else if (trade.accept == 2) {
                            trade.dispose();
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Logger.logException(this.getClass(), e);
        }
    }

    /**
     * Mời giao dịch
     */
    private void sendInviteTrade(Player plInvite, Player plReceive) {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) plInvite.id);
            plReceive.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    /**
     * Hủy giao dịch
     *
     * @param player
     */
    public void cancelTrade(Player player) {
        Trade trade = PLAYER_TRADE.get(player);
        if (trade != null) {
            trade.cancelTrade();
        }
    }

    public boolean check(Player player) {
        return PLAYER_TRADE.get(player) != null;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long start = System.currentTimeMillis();
                Set<Map.Entry<Player, Trade>> entrySet = PLAYER_TRADE.entrySet();
                for (Map.Entry entry : entrySet) {
                    ((Trade) entry.getValue()).update();
                }
                Functions.sleep(Math.max(300 - (System.currentTimeMillis() - start), 10));
            } catch (Exception e) {
            }
        }
    }
}
