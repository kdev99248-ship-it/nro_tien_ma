package bot;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import server.Maintenance;
import utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jdbc.DBConnecter;

public class BotManager implements Runnable {

    private static BotManager instance;
    private final List<Bot> bots = new CopyOnWriteArrayList<>();

    public static BotManager gI() {
        if (instance == null) {
            instance = new BotManager();
        }
        return instance;
    }

    public BotManager() {
    }

    public void addBot(Bot bot) {
        this.bots.add(bot);
    }

    public void removeBot(Bot bot) {
        this.bots.remove(bot);
    }

    public void reload() {
        for (Bot bot : bots) {
            if (bot.zone != null) {
                services.func.ChangeMapService.gI().exitMap(bot);
            }
            bot.dispose();
        }
        this.bots.clear();
        this.init();
    }

    public void init() {
        try (Connection con = DBConnecter.getConnectionServer(); 
             PreparedStatement ps = con.prepareStatement("SELECT * FROM data_bot WHERE active = 1"); 
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                short head = rs.getShort("head");
                short body = rs.getShort("body");
                short leg = rs.getShort("leg");
                int mapId = rs.getInt("map_id");
                int gender = rs.getInt("gender");
                createBot(id, name, head, body, leg, mapId, gender);
            }
        } catch (SQLException e) {
            Logger.error("Error loading bots from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createBot(int id, String name, short head, short body, short leg, int mapId, int gender) {
        try {
            Bot bot = new Bot(id, name, head, body, leg, gender);
            bot.setOutfit(head, body, leg);
            // Default zone 0
            bot.joinMap(mapId, 0);
            addBot(bot);
        } catch (Exception e) {
            Logger.error("Error creating bot " + name + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long st = System.currentTimeMillis();
                for (Bot bot : bots) {
                    try {
                        // Tối ưu: Skip update bot khi zone không có player thật
                        if (bot.zone != null && !bot.zone.hasRealPlayer()) {
                            continue;
                        }
                        bot.update();
                    } catch (Exception e) {
                        Logger.error("Error updating bot " + bot.name);
                        e.printStackTrace();
                    }
                }
                long time = 1000 - (System.currentTimeMillis() - st);
                if (time > 0) {
                    Thread.sleep(time);
                }
            } catch (Exception e) {
                Logger.error("Error in BotManager loop");
            }
        }
    }
}
