package services.func;

/*
 *
 *
 * @author EMTI
 */
import item.Item;
import java.io.IOException;
import player.Player;
import network.Message;
import services.RewardService;
import services.Service;
import java.util.List;
import services.InventoryService;
import services.ItemService;

public class LuckyRound {

    private static final byte MAX_ITEM_IN_BOX = 100;

    // 1 gem and ruby
    public static final byte USING_RUBY = 2;
    public static final byte USING_GOLD = 0;
    public static final byte USING_TICKET = 1;

    private static final int PRICE_RUBY = 10;
    private static final long PRICE_GOLD = 500000000L;
    private static final int PRICE_TICKET = 1;
    private static final int TICKET = 457;

    private static LuckyRound instance;

    public static LuckyRound gI() {
        if (instance == null) {
            instance = new LuckyRound();
        }
        return instance;
    }

    public void openCrackBallUI(Player pl, byte type) {
        pl.iDMark.setTypeLuckyRound(type);
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(0);
            msg.writer().writeByte(7);
            for (int i = 0; i < 7; i++) {
                msg.writer().writeShort(419 + i);
            }
            msg.writer().writeByte(type); // type price
            if (type == USING_TICKET) {
                msg.writer().writeInt(PRICE_TICKET);
                msg.writer().writeShort(TICKET);
            } else {
                msg.writer().writeInt((int) (type == USING_RUBY ? PRICE_RUBY : PRICE_GOLD)); // price
                msg.writer().writeShort(-1); // id ticket
            }
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void readOpenBall(Player player, Message msg) {
        try {
            msg.reader().readByte(); // type
            byte count = msg.reader().readByte();
            switch (player.iDMark.getTypeLuckyRound()) {
                case USING_RUBY:
                    openBallByGem(player, count);
                    break;
                case USING_GOLD:
                    openBallByGold(player, count);
                    break;
                case USING_TICKET:
                    openBallByTicket(player, count);
                    break;
            }
        } catch (Exception e) {
            openCrackBallUI(player, player.iDMark.getTypeLuckyRound());
        }
    }

    private void openBallByGem(Player player, byte count) {
        int gemNeed = (count * PRICE_RUBY);
        // player.pointtet += count;

        if (player.inventory.gem < gemNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc để mở");
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                player.inventory.gem -= gemNeed;

                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, false);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByGold(Player player, byte count) {
        long goldNeed = (count * PRICE_GOLD);
        // player.pointtet += count;
        if (player.inventory.gold < goldNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng để mở");
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                player.inventory.gold -= goldNeed;
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, false);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByTicket(Player player, byte count) {
        int ticketNeed = (count * PRICE_TICKET);
        Item ticket = InventoryService.gI().findItemBag(player, TICKET);
        if (ticket == null || ticket.quantity < ticketNeed) {
            Service.gI().sendThongBao(player,
                    "Bạn không đủ " + ItemService.gI().createNewItem((short) TICKET).template.name + " để quay");
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                InventoryService.gI().subQuantityItemsBag(player, ticket, ticketNeed);
                InventoryService.gI().sendItemBag(player);
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void sendReward(Player player, List<Item> items) {
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.iconID);
            }
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void addItemToBox(Player player, List<Item> items) {
        for (Item item : items) {
            player.inventory.itemsBoxCrackBall.add(item);
        }
    }
}
